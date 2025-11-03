package com.trong.client.Controller;

import com.trong.client.util.ApiClient;
import com.trong.model.ConclusionHistory;
import com.trong.model.FraudLog;
import com.trong.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class EditLogController {
    @FXML private Button btnLogout;
    @FXML private Label labelCurrentType;
    @FXML private TextField txtStartTime;
    @FXML private TextField txtEndTime;
    @FXML private ComboBox<String> comboNewType;
    @FXML private Button btnBack;
    @FXML private Button btnSave;

    private FraudLog currentLog;
    private User currentUser;
    private int processingLogId; // THÊM BIẾN NÀY để biết đường quay lại

    @FXML
    public void initialize() {
        comboNewType.setItems(FXCollections.observableArrayList(
                "cheating",
                "looking_away",
                "using_phone",
                "no_behavior",
                "other"
        ));
    }

    public void loadLogToEdit(FraudLog log, User user, int processingLogId) {
        this.currentLog = log;
        this.currentUser = user;
        this.processingLogId = processingLogId; // LƯU LẠI ID ĐỂ DÙNG KHI QUAY LẠI

        labelCurrentType.setText(log.getBehaviorType());
        txtStartTime.setText(String.format("%.3f", log.getStartTimeSeconds()));
        txtEndTime.setText(String.format("%.3f", log.getEndTimeSeconds()));
        comboNewType.setValue(log.getBehaviorType());
    }

    @FXML
    private void handleSave() {
        double newStartTime, newEndTime;
        String newType = comboNewType.getValue();

        try {
            newStartTime = Double.parseDouble(txtStartTime.getText().replace(",", "."));
            newEndTime = Double.parseDouble(txtEndTime.getText().replace(",", "."));
            if (newType == null || newType.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn loại vi phạm mới.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời gian bắt đầu/kết thúc phải là con số (ví dụ: 10.5).");
            return;
        }

        String oldStatus = currentLog.getConclusionStatus();
        String oldChanges = String.format("Time: %.3f-%.3f, Type: %s",
                currentLog.getStartTimeSeconds(),
                currentLog.getEndTimeSeconds(),
                currentLog.getBehaviorType());

        String newChanges = String.format("Time: %.3f-%.3f, Type: %s",
                newStartTime,
                newEndTime,
                newType);
        currentLog.setStartTimeSeconds(newStartTime);
        currentLog.setEndTimeSeconds(newEndTime);
        currentLog.setBehaviorType(newType);
        currentLog.setConclusionStatus("Đã sửa");
        currentLog.setConcluderDate(new Date());
        ConclusionHistory history = new ConclusionHistory();
        history.setOldStatus(oldStatus);
        history.setNewStatus("Đã sửa");
        history.setConclusionTimestamp(new Date());
        history.setVerdictChanges(String.format("Sửa từ [%s] thành [%s]", oldChanges, newChanges));
        history.setFraudLog(currentLog);
        history.setUser(currentUser);
        btnSave.setDisable(true);
        CompletableFuture.runAsync(() -> {
            try {
                ApiClient.editFraudLog(currentLog);
                ApiClient.saveConclusionHistory(history);
                // Khi lưu thành công, tự động gọi hàm quay lại
                Platform.runLater(this::handleBack);

            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Sửa log thất bại: " + e.getMessage());
                    btnSave.setDisable(false);
                });
            }
        });
    }
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConcludeVideo.fxml"));
            Scene scene = new Scene(loader.load());

            ConcludeVideoController controller = loader.getController();
            // Tải lại dữ liệu cho trang kết luận với ID đã lưu
            controller.loadData(currentUser, this.processingLogId);

            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Kết luận gian lận Video");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể quay lại trang chi tiết.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
        switchScene("/fxml/VideoHistory.fxml", "Lịch sử video");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene("/fxml/Login.fxml", "Đăng nhập hệ thống");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);

            // Truyền thông tin User qua scene mới (nếu cần)
            Object controller = loader.getController();
            if (controller instanceof HomeController) {
                ((HomeController) controller).setUser(currentUser);
            } else if (controller instanceof UploadVideoController) {
                ((UploadVideoController) controller).setUser(currentUser);
            } else if (controller instanceof VideoHistoryController) {
                ((VideoHistoryController) controller).setUser(currentUser);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể chuyển trang: " + e.getMessage());
        }
    }
}