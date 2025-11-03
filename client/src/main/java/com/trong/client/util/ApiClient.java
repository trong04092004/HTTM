package com.trong.client.util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trong.model.ConclusionHistory;
import com.trong.model.FraudLog;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder; // <-- THÊM IMPORT
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ApiClient {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    private static final String BASE_URL = "http://localhost:8080";

    public static final String VIDEO_STREAM_URL_FORMAT = BASE_URL + "/streamVideo/%s";


    public static String uploadVideo(File videoFile) throws IOException {
        // ... (Giữ nguyên hàm này) ...
        String boundary = Long.toHexString(System.currentTimeMillis());
        URL url = new URL(BASE_URL + "/processVideo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream output = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(videoFile.getName()).append("\"\r\n");
            writer.append("Content-Type: ").append(Files.probeContentType(videoFile.toPath()))
                    .append("\r\n\r\n").flush();
            Files.copy(videoFile.toPath(), output);
            output.flush();
            writer.append("\r\n").flush();

            writer.append("--").append(boundary).append("--").append("\r\n").flush();
        }
        return readResponse(conn);
    }

    public static String postJson(String urlString, String json) throws IOException {
        // ... (Giữ nguyên hàm này) ...
        URL url = new URL(urlString); // URL đầy đủ
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return readResponse(conn);
    }

    public static String getVideoHistory(int userId) throws IOException {
        // ... (Giữ nguyên hàm này) ...
        URL url = new URL(BASE_URL + "/getVideoHistory?userId=" + userId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        return readResponse(conn);
    }

    public static String getFraudLogs(int processingLogId) throws IOException {
        // ... (Giữ nguyên hàm này) ...
        URL url = new URL(BASE_URL + "/getFraudLogs?processingLogId=" + processingLogId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        return readResponse(conn);
    }

    public static String getProcessingLogDetails(int processingLogId) throws IOException {
        // ... (Giữ nguyên hàm này) ...
        URL url = new URL(BASE_URL + "/getProcessingLogDetails?processingLogId=" + processingLogId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        return readResponse(conn);
    }

    public static String editFraudLog(FraudLog log) throws IOException {
        // ... (Giữ nguyên hàm này) ...
        String jsonPayload = gson.toJson(log);
        return postJson(BASE_URL + "/editFraudLog", jsonPayload);
    }

    public static String saveConclusionHistory(ConclusionHistory history) throws IOException {
        // ... (Giữ nguyên hàm này) ...
        String jsonPayload = gson.toJson(history);
        return postJson(BASE_URL + "/saveConclusionHistory", jsonPayload);
    }

    public static String editFraudLogBatch(List<FraudLog> logs) throws IOException {
        // ... (GiSửa ữ nguyên hàm này) ...
        String jsonPayload = gson.toJson(logs);
        return postJson(BASE_URL + "/editFraudLogBatch", jsonPayload);
    }

    public static String saveConclusionHistoryBatch(List<ConclusionHistory> histories) throws IOException {
        // ... (Giữ nguyên hàm này) ...
        String jsonPayload = gson.toJson(histories);
        return postJson(BASE_URL + "/saveConclusionHistoryBatch", jsonPayload);
    }

    // === THÊM HÀM MỚI TẠI ĐÂY ===
    /**
     * Cập nhật trạng thái của ProcessingLog
     */
    public static String updateProcessingLogStatus(int processingLogId, String status) throws IOException {
        String encodedStatus = URLEncoder.encode(status, StandardCharsets.UTF_8);
        String url = BASE_URL + "/updateProcessingLogStatus?processingLogId=" + processingLogId + "&status=" + encodedStatus;

        // Gửi một POST request với body rỗng, vì logic nằm trong URL params
        return postJson(url, "{}");
    }
    // =============================

    private static String readResponse(HttpURLConnection conn) throws IOException {
        // ... (Giữ nguyên hàm này) ...
        int responseCode = conn.getResponseCode();
        InputStream stream = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}

