package com.trong.server.Controller;

import com.sun.net.httpserver.HttpExchange;
import com.trong.server.DAO.UserDAO;
import com.trong.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UserController {
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    public void handleLogin(HttpExchange exchange) throws IOException {
        // Chỉ chấp nhận POST
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Map.of(
                    "success", false,
                    "message", "Phương thức không được hỗ trợ (chỉ POST)"
            ));
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            User requestUser = gson.fromJson(reader, User.class);

            if (requestUser == null || requestUser.getUsername() == null || requestUser.getPassword() == null) {
                sendResponse(exchange, 400, Map.of(
                        "success", false,
                        "message", "Thiếu thông tin đăng nhập"
                ));
                return;
            }

            User foundUser = userDAO.checkLogin(requestUser);
            if (foundUser != null) {
                sendResponse(exchange, 200, Map.of(
                        "success", true,
                        "user", foundUser
                ));
            } else {
                sendResponse(exchange, 200, Map.of(
                        "success", false,
                        "message", "Sai tài khoản hoặc mật khẩu"
                ));
            }

        } catch (JsonSyntaxException e) {
            LOGGER.warning("Lỗi JSON không hợp lệ: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("success", false, "message", "Dữ liệu JSON không hợp lệ"));
        } catch (Exception e) {
            LOGGER.severe("Lỗi xử lý đăng nhập: " + e.getMessage());
            sendResponse(exchange, 500, Map.of("success", false, "message", "Lỗi máy chủ nội bộ"));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Map<String, Object> data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
