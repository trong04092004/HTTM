package com.trong.client.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.trong.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    private static final String LOGIN_API = "http://localhost:8080/login";
    private final Gson gson = new Gson();

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            User loggedUser = sendLoginRequest(user);

            if (loggedUser != null) {
                lblStatus.setText("Đăng nhập thành công!");
                openHome(loggedUser);
            } else {
                lblStatus.setText("Sai tài khoản hoặc mật khẩu!");
            }

        } catch (Exception e) {
            lblStatus.setText("Không thể kết nối đến server!");
            e.printStackTrace();
        }
    }

    private User sendLoginRequest(User user) throws IOException {
        URL url = new URL(LOGIN_API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = gson.toJson(user).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line.trim());

            JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
            if (json.get("success").getAsBoolean()) {
                return gson.fromJson(json.get("user"), User.class);
            }
        }

        return null;
    }

    private void openHome(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            Scene scene = new Scene(loader.load());

            // Truyền dữ liệu người dùng vào HomeController
            HomeController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Trang chủ - Xin chào " + user.getFullName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
