package com.trong.server.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken; // <-- THÊM IMPORT
import com.sun.net.httpserver.HttpExchange;
import com.trong.model.FraudLog;
import com.trong.server.DAO.FraudLogDAO;
import com.trong.server.Service.RequestParser; // (Đảm bảo bạn đã tạo file này)

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type; // <-- THÊM IMPORT
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class FraudLogController {

    private final FraudLogDAO fraudLogDAO = new FraudLogDAO();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    // (Hàm này đã có và giữ nguyên)
    public void handleGetFraudLogs(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Only GET allowed\"}");
            return;
        }
        try {
            Map<String, String> params = RequestParser.parseQuery(exchange.getRequestURI().getQuery());
            int processingLogId = Integer.parseInt(params.get("processingLogId"));
            List<FraudLog> logs = fraudLogDAO.getFraudLogsByProcessingLogId(processingLogId);

            String jsonResponse = gson.toJson(logs);
            sendResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleEditFraudLog(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Only POST allowed\"}");
            return;
        }
        try {
            String body = RequestParser.parseBody(exchange);
            FraudLog log = gson.fromJson(body, FraudLog.class);
            boolean success = fraudLogDAO.editFraudLog(log);

            if (success) {
                sendResponse(exchange, 200, "{\"success\":true}");
            } else {
                sendResponse(exchange, 500, "{\"error\":\"Lỗi khi cập nhật FraudLog\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleEditFraudLogBatch(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Only POST allowed\"}");
            return;
        }
        try {
            String body = RequestParser.parseBody(exchange);
            Type listType = new TypeToken<List<FraudLog>>(){}.getType();
            List<FraudLog> logs = gson.fromJson(body, listType);
            boolean success = fraudLogDAO.editFraudLogBatch(logs);
            if (success) {
                sendResponse(exchange, 200, "{\"success\":true}");
            } else {
                sendResponse(exchange, 500, "{\"error\":\"Lỗi khi cập nhật hàng loạt FraudLog\"}");
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

