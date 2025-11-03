package com.trong.client.Controller;

import com.google.gson.Gson;
import com.trong.client.util.ApiClient;
import com.trong.model.FraudLog;
import com.trong.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UploadVideoController {

    @FXML private Button btnSelectVideo, btnProcess, btnSave, btnLogout;
    @FXML private TableView<FraudLog> tableViolations;
    @FXML private TableColumn<FraudLog, String> colStart, colEnd, colType;
    @FXML private MediaView mediaView;
    @FXML private VBox resultBox;

    @FXML private StackPane videoContainer;
    @FXML private VBox controlsBox;
    @FXML private Button btnPlayPause;
    @FXML private Slider sliderTime;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblTotalTime;

    private MediaPlayer mediaPlayer;
    private File selectedFile;
    private final ObservableList<FraudLog> fraudLogs = FXCollections.observableArrayList();
    private User currentUser;


    private String formatSeconds(double totalSeconds) {
        if (totalSeconds < 0 || Double.isNaN(totalSeconds)) return "00:00";
        long totalSecLong = (long) totalSeconds;
        long minutes = totalSecLong / 60;
        long seconds = totalSecLong % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @FXML
    public void initialize() {
        colStart.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        formatSeconds(data.getValue().getStartTimeSeconds())
                ));
        colEnd.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        formatSeconds(data.getValue().getEndTimeSeconds())
                ));
        colType.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getBehaviorType() != null ? data.getValue().getBehaviorType() : ""));
        resultBox.setVisible(false);
        resultBox.setManaged(false);
        controlsBox.setVisible(false);
    }

    @FXML
    private void handleSelectVideo(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn video cần tải lên");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov", "*.mkv")
        );
        selectedFile = chooser.showOpenDialog(((Stage) btnSelectVideo.getScene().getWindow()));
        if (selectedFile != null) playVideo(selectedFile);
    }

    private void playVideo(File file) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setAutoPlay(true);
            setupVideoControls();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi phát video", "Không thể phát video: " + e.getMessage());
        }
    }
    private void setupVideoControls() {
        if (mediaPlayer == null) return;
        mediaPlayer.setOnPlaying(() -> btnPlayPause.setText("⏸"));
        mediaPlayer.setOnPaused(() -> btnPlayPause.setText("▶"));
        mediaPlayer.setOnStopped(() -> btnPlayPause.setText("▶"));
        mediaPlayer.setOnEndOfMedia(() -> btnPlayPause.setText("▶"));
        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = mediaPlayer.getMedia().getDuration();
            if (!totalDuration.isUnknown()) {
                sliderTime.setMax(totalDuration.toSeconds());
                lblTotalTime.setText(formatSeconds(totalDuration.toSeconds()));
            }
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!sliderTime.isValueChanging()) { // isValueChanging = true khi người dùng đang kéo
                sliderTime.setValue(newTime.toSeconds());
            }
            lblCurrentTime.setText(formatSeconds(newTime.toSeconds()));
        });

        sliderTime.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (sliderTime.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });
        sliderTime.setOnMouseClicked(event -> {
            mediaPlayer.seek(Duration.seconds(sliderTime.getValue()));
        });
    }

    @FXML
    private void handlePlayPause(ActionEvent event) {
        if (mediaPlayer == null) return;
        MediaPlayer.Status status = mediaPlayer.getStatus();

        if (status == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        } else {
            // Nếu video đã kết thúc, tua về 0 trước khi phát
            if (status == MediaPlayer.Status.STOPPED || status == MediaPlayer.Status.PAUSED) {
                if (mediaPlayer.getCurrentTime().equals(mediaPlayer.getTotalDuration())) {
                    mediaPlayer.seek(Duration.ZERO);
                }
            }
            mediaPlayer.play();
        }
    }

    @FXML
    private void showControls() {
        controlsBox.setVisible(true);
    }

    @FXML
    private void hideControls() {
        controlsBox.setVisible(false);
    }
    @FXML
    private void handleProcessVideo(ActionEvent event) {
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn video trước!");
            return;
        }
        btnProcess.setDisable(true);
        new Thread(() -> {
            try {
                String jsonResponse = ApiClient.uploadVideo(selectedFile);
                Platform.runLater(() -> showResults(jsonResponse));
            } catch (IOException e) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Lỗi Server", "Không thể gửi video tới server: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> btnProcess.setDisable(false));
            }
        }).start();
    }

    private void showResults(String jsonResponse) {
        try {
            Gson gson = new Gson();
            Map<?, ?> root = gson.fromJson(jsonResponse, Map.class);
            Object logsObj = root.get("logs");
            if (logsObj instanceof List<?>) {
                List<?> logs = (List<?>) logsObj;
                fraudLogs.clear();
                for (Object o : logs) {
                    if (!(o instanceof Map)) continue;
                    Map<?, ?> log = (Map<?, ?>) o;
                    double startSec = parseDoubleFromML(log.get("startTime"));
                    double endSec = parseDoubleFromML(log.get("endTime"));
                    String behavior = log.get("behavior") != null ? log.get("behavior").toString() : "";
                    FraudLog fl = new FraudLog();
                    fl.setStartTimeSeconds(startSec);
                    fl.setEndTimeSeconds(endSec);
                    fl.setBehaviorType(behavior);
                    fl.setConclusionStatus("Detected");
                    fl.setConcluderDate(new Date());
                    fl.setFinalVerdict("Cheating");
                    fraudLogs.add(fl);
                }
                tableViolations.setItems(fraudLogs);
                resultBox.setVisible(true);
                resultBox.setManaged(true);
                // SỬA LỖI
                showAlert(Alert.AlertType.INFORMATION, "Hoàn thành",
                        "Phát hiện " + fraudLogs.size() + " hành vi gian lận.");
            } else {
                // SỬA LỖI
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Server chưa trả về dữ liệu logs hợp lệ.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // SỬA LỖI
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi đọc dữ liệu trả về từ server:\n" + e.getMessage());
        }
    }

    private double parseDoubleFromML(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @FXML
    private void handleSaveResults(ActionEvent event) {
        if (fraudLogs.isEmpty()) {
            // SỬA LỖI
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dữ liệu để lưu!");
            return;
        }

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin người dùng. Không thể lưu.");
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> videoInfo = new HashMap<>();

        videoInfo.put("url", selectedFile != null ? selectedFile.getName() : "unknown.mp4");
        videoInfo.put("userId", currentUser.getIdUser());

        payload.put("video", videoInfo);

        List<Map<String, Object>> logs = new ArrayList<>();
        for (FraudLog fl : fraudLogs) {
            Map<String, Object> lm = new HashMap<>();
            lm.put("startTime", fl.getStartTimeSeconds());
            lm.put("endTime", fl.getEndTimeSeconds());
            lm.put("behavior", fl.getBehaviorType());
            lm.put("finalVerdict", fl.getFinalVerdict());
            lm.put("conclusionStatus", fl.getConclusionStatus());
            logs.add(lm);
        }
        payload.put("logs", logs);

        String json = new Gson().toJson(payload);

        btnSave.setDisable(true);
        new Thread(() -> {
            try {
                String resp = ApiClient.postJson("http://localhost:8080/saveFraudLogs", json);
                Platform.runLater(() -> {
                    if (resp != null && resp.contains("success")) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Kết quả đã được lưu thành công!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Lưu thất bại. Server trả về: " + resp);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi lưu kết quả: " + ex.getMessage()));
            } finally {
                Platform.runLater(() -> btnSave.setDisable(false));
            }
        }).start();
    }


    @FXML
    private void handleHome(ActionEvent event) {
        switchScene("/fxml/Home.fxml", "Trang chủ");
    }

    @FXML
    private void handleHistory(ActionEvent event) {
        switchScene("/fxml/VideoHistory.fxml", "Lịch sử video");
    }

    @FXML
    private void handleUploadVideo(ActionEvent event) {
        // Đã ở trang này, không cần làm gì
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene("/fxml/Login.fxml", "Đăng nhập hệ thống");
    }

    private void switchScene(String fxmlPath, String title) {
        // Dừng video đang phát (QUAN TRỌNG)
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            Object controller = loader.getController();
            if (controller instanceof UploadVideoController)
                ((UploadVideoController) controller).setUser(currentUser);
            else if (controller instanceof HomeController)
                ((HomeController) controller).setUser(currentUser);
            else if (controller instanceof VideoHistoryController)
                ((VideoHistoryController) controller).setUser(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể chuyển trang: " + e.getMessage());
        }
    }

    public void setUser(User currentUser) {
        this.currentUser = currentUser;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}