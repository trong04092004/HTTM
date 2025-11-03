package com.trong.server.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.trong.model.VideoHistoryDTO;
import com.trong.server.DAO.ProcessingLogDAO;
import com.trong.server.DAO.ProcessingSummaryDAO;
import com.trong.server.Service.RequestParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessingSummaryController {

    private final ProcessingSummaryDAO historyDAO = new ProcessingSummaryDAO();
    private final ProcessingLogDAO logDAO = new ProcessingLogDAO();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    public void handleGetHistory(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Only GET allowed\"}");
            return;
        }

        try {
            Map<String, String> params = RequestParser.parseQuery(exchange.getRequestURI().getQuery());
            int userId = Integer.parseInt(params.get("userId"));

            List<VideoHistoryDTO> history = historyDAO.getHistorySummaryForUser(userId);
            String jsonResponse = gson.toJson(history);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleGetProcessingLogDetails(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Only GET allowed\"}");
            return;
        }
        try {
            Map<String, String> params = RequestParser.parseQuery(exchange.getRequestURI().getQuery());
            int processingLogId = Integer.parseInt(params.get("processingLogId"));
            Map<String, Object> details = logDAO.getLogAndVideoDetails(processingLogId);

            if (details.isEmpty()) {
                sendResponse(exchange, 404, "{\"error\":\"Không tìm thấy log chi tiết\"}");
                return;
            }
            String jsonResponse = gson.toJson(details);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleUpdateProcessingLogStatus(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Only POST allowed\"}");
            return;
        }
        try {
            // Lấy params từ URL (ví dụ: /updateProcessingLogStatus?processingLogId=12&status=Completed)
            Map<String, String> params = RequestParser.parseQuery(exchange.getRequestURI().getQuery());
            int processingLogId = Integer.parseInt(params.get("processingLogId"));
            String status = params.get("status");

            if (status == null || status.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing status\"}");
                return;
            }

            boolean success = logDAO.updateStatus(processingLogId, status);

            if (success) {
                sendResponse(exchange, 200, "{\"success\":true}");
            } else {
                sendResponse(exchange, 500, "{\"error\":\"Lỗi khi cập nhật trạng thái ProcessingLog\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }


    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

