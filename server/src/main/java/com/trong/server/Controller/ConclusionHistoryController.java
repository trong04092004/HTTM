package com.trong.server.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.trong.model.ConclusionHistory;
import com.trong.server.DAO.ConclusionHistoryDAO;
import com.trong.server.Service.RequestParser;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConclusionHistoryController {

    private final ConclusionHistoryDAO historyDAO = new ConclusionHistoryDAO();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    public void handleSaveConclusionHistory(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Only POST allowed\"}");
            return;
        }
        try {
            String body = RequestParser.parseBody(exchange);
            ConclusionHistory history = gson.fromJson(body, ConclusionHistory.class);

            boolean success = historyDAO.saveConclusionHistory(history);

            if (success) {
                sendResponse(exchange, 200, "{\"success\":true}");
            } else {
                sendResponse(exchange, 500, "{\"error\":\"Lỗi khi lưu ConclusionHistory\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleSaveConclusionHistoryBatch(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Only POST allowed\"}");
            return;
        }
        try {
            String body = RequestParser.parseBody(exchange);

            Type listType = new TypeToken<List<ConclusionHistory>>(){}.getType();
            List<ConclusionHistory> histories = gson.fromJson(body, listType);

            boolean success = historyDAO.saveConclusionHistoryBatch(histories);

            if (success) {
                sendResponse(exchange, 200, "{\"success\":true}");
            } else {
                sendResponse(exchange, 500, "{\"error\":\"Lỗi khi lưu hàng loạt ConclusionHistory\"}");
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

