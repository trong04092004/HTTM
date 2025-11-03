package com.trong.server.Controller;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.trong.model.*;
import com.trong.server.DAO.*;
import com.trong.server.Service.VideoService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.Map;


public class VideoController {
    private final Gson gson = new Gson();
    private final VideoDAO videoDAO = new VideoDAO();
    private final ProcessingLogDAO processingDAO = new ProcessingLogDAO();
    private final FraudLogDAO fraudDAO = new FraudLogDAO();

    private static final Path VIDEO_STORAGE_PATH = Paths.get("video_storage");

    private final VideoService videoService = new VideoService();


    public void handleProcessVideo(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Only POST allowed");
            return;
        }
        try {
            File videoFile = videoService.saveUploadedFile(exchange);
            String mlResponse = videoService.sendToMLServer(videoFile);
            sendResponse(exchange, 200, mlResponse);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, gson.toJson(Map.of("error", "Lỗi xử lý file: " + e.getMessage())));
        }
    }
    public void handleSaveResults(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Only POST allowed");
            return;
        }

        try {

            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"))
                    .lines().collect(Collectors.joining("\n"));

            JsonObject requestJson = gson.fromJson(requestBody, JsonObject.class);
            JsonObject videoJson = requestJson.getAsJsonObject("video");
            JsonArray logsJson = requestJson.getAsJsonArray("logs");

            Video video = new Video();
            video.setUrl(videoJson.get("url").getAsString());
            video.setUploadDate(new java.util.Date());
            video.setDuration(videoJson.has("duration") ? videoJson.get("duration").getAsInt() : 0);

            if (videoJson.has("userId")) {
                int userId = videoJson.get("userId").getAsInt();
                User user = new User();
                user.setIdUser(userId);
                video.setUser(user);
            } else {
                System.err.println("Cảnh báo: handleSaveResults nhận được request thiếu 'userId'.");
                throw new Exception("Yêu cầu lưu thiếu userId");
            }

            int videoId = videoDAO.insertVideo(video);
            if (videoId == -1) throw new Exception("Không thể lưu Video vào CSDL.");


            ProcessingLog plog = new ProcessingLog();
            plog.setVideoId(videoId);
            plog.setStatus("Pending");
            plog.setOutputPath("logs/" + video.getUrl().replace(".mp4", "_log.json"));
            plog.setStartTime(new java.util.Date());
            plog.setEndTime(new java.util.Date());
            int processId = processingDAO.insertProcessingLog(plog);
            if (processId == -1) throw new Exception("Không thể lưu ProcessingLog vào CSDL.");


            for (JsonElement el : logsJson) {
                JsonObject log = el.getAsJsonObject();
                FraudLog fraud = new FraudLog();
                fraud.setProcessingLogId(processId);
                double startSec = log.get("startTime").getAsDouble();
                double endSec = log.get("endTime").getAsDouble();
                fraud.setStartTimeSeconds(startSec);
                fraud.setEndTimeSeconds(endSec);
                fraud.setBehaviorType(log.get("behavior").getAsString());
                fraud.setConclusionStatus("Detected");
                fraud.setConcluderDate(null);
                fraud.setFinalVerdict(null);

                fraudDAO.insertFraudLog(fraud);
            }
            sendResponse(exchange, 200, gson.toJson(Map.of("success", true)));

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, gson.toJson(Map.of("error", "Lỗi server khi lưu CSDL: " + e.getMessage())));
        }
    }

    public void handleStreamVideo(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String fileName = requestPath.substring("/streamVideo/".length());

        if (fileName.isEmpty() || fileName.contains("..")) {
            sendError(exchange, 400, "Invalid file name");
            return;
        }

        try {

            Path filePath = VIDEO_STORAGE_PATH.resolve(fileName).toAbsolutePath().normalize();

            if (!filePath.startsWith(VIDEO_STORAGE_PATH.toAbsolutePath())) {
                sendError(exchange, 403, "Access denied");
                return;
            }

            File videoFile = filePath.toFile();

            if (!videoFile.exists() || videoFile.isDirectory()) {
                System.err.println("Video Streamer: Không tìm thấy file: " + filePath);
                sendError(exchange, 404, "File not found");
                return;
            }

            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = "video/mp4";
            }

            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(videoFile.length()));
            exchange.sendResponseHeaders(200, videoFile.length());

            try (OutputStream os = exchange.getResponseBody()) {
                Files.copy(filePath, os);
            }

        } catch (InvalidPathException e) {
            sendError(exchange, 400, "Invalid path");
        } catch (IOException e) {
            // Lỗi này bình thường nếu client đóng video
        } catch (Exception e) {
            System.err.println("Lỗi Stream Video không xác định: " + e.getMessage());
            e.printStackTrace();
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }


    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        byte[] response = message.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}

