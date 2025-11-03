package com.trong.client.Controller;

import com.trong.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML private Label labelWelcome;
    @FXML private Label labelSubtitle;
    @FXML private Button btnLogout;

    private User currentUser;
    public void setUser(User user) {
        this.currentUser = user;
        labelWelcome.setText("Chào mừng bạn trở lại, " + user.getFullName() + "!");
        labelSubtitle.setText("Nền tảng xử lý video và cảnh báo gian lận của bạn.");
    }

    @FXML
    private void handleHome(ActionEvent event) {
        switchScene("/fxml/Home.fxml", "Trang chủ");
    }

    @FXML
    private void handleUploadVideo(ActionEvent event) {
        switchScene("/fxml/UploadVideo.fxml", "Tải video lên");
    }

    @FXML
    private void handleVideoHistory(ActionEvent event) {
        switchScene("/fxml/VideoHistory.fxml", "Lịch sử video đã xử lý");
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene("/fxml/Login.fxml", "Đăng nhập hệ thống");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) labelWelcome.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);

            // Truyền user nếu cần cho các controller khác
            Object controller = loader.getController();
            if (controller instanceof UploadVideoController uploadController) {
                uploadController.setUser(currentUser);
            } else if (controller instanceof VideoHistoryController historyController) {
                historyController.setUser(currentUser);
            } else if (controller instanceof HomeController homeController) {
                homeController.setUser(currentUser);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
