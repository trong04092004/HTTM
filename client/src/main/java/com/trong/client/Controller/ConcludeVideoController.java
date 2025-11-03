package com.trong.client.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.trong.client.util.ApiClient;
import com.trong.model.ConclusionHistory;
import com.trong.model.FraudLog;
import com.trong.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent; // Import ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane; // THÊM MỚI
import javafx.scene.layout.VBox; // THÊM MỚI
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration; // THÊM MỚI

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ConcludeVideoController {

    @FXML private Button btnLogout;
    @FXML private Button btnBack;
    @FXML private MediaView mediaView;
    @FXML private TableView<FraudLog> tblFraudLog;
    @FXML private TableColumn<FraudLog, String> colStartTime;
    @FXML private TableColumn<FraudLog, String> colEndTime;
    @FXML private TableColumn<FraudLog, String> colBehavior;
    @FXML private TableColumn<FraudLog, Void> colAction;
    @FXML private Button btnRefuseConclude;
    @FXML private Button btnConfirmConclude;
    @FXML private StackPane videoContainer;
    @FXML private VBox controlsBox;
    @FXML private Button btnPlayPause;
    @FXML private Slider sliderTime;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblTotalTime;


    private User currentUser;
    private int processingLogId;
    private MediaPlayer mediaPlayer;
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();
    private final ObservableList<FraudLog> fraudLogsList = FXCollections.observableArrayList();
    @FXML
    public void initialize() {
        tblFraudLog.setItems(fraudLogsList);

        colStartTime.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        formatSecondsWithMillis(cellData.getValue().getStartTimeSeconds())
                ));

        colEndTime.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        formatSecondsWithMillis(cellData.getValue().getEndTimeSeconds())
                ));

        colBehavior.setCellValueFactory(new PropertyValueFactory<>("behaviorType"));

        colAction.setCellFactory(createActionCellFactory());
        btnBack.setOnAction(e -> handleBack());
        btnConfirmConclude.setOnAction(e -> handleConcludeAll(true));
        btnRefuseConclude.setOnAction(e -> handleConcludeAll(false));

        controlsBox.setVisible(false); // Ẩn thanh điều khiển khi bắt đầu
    }
    public void loadData(User user, int processingLogId) {
        this.currentUser = user;
        this.processingLogId = processingLogId;

        loadFraudLogsFromServer();
        loadVideoMedia();
    }
    private void loadFraudLogsFromServer() {
        tblFraudLog.setPlaceholder(new Label("Đang tải chi tiết vi phạm..."));

        new Thread(() -> {
            try {
                String jsonResponse = ApiClient.getFraudLogs(this.processingLogId);
                Type listType = new TypeToken<List<FraudLog>>(){}.getType();
                List<FraudLog> logs = gson.fromJson(jsonResponse, listType);

                Platform.runLater(() -> {
                    fraudLogsList.setAll(logs);
                    if (logs.isEmpty()) {
                        tblFraudLog.setPlaceholder(new Label("Không tìm thấy vi phạm nào."));
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không thể tải chi tiết: " + e.getMessage())
                );
            }
        }).start();
    }

    private void loadVideoMedia() {
        new Thread(() -> {
            try {
                String jsonResponse = ApiClient.getProcessingLogDetails(this.processingLogId);
                Map<String, Object> details = gson.fromJson(jsonResponse, Map.class);
                String videoFileName = (String) details.get("videoUrl");

                if (videoFileName != null && !videoFileName.isEmpty()) {
                    String streamUrl = String.format(ApiClient.VIDEO_STREAM_URL_FORMAT, videoFileName);

                    Platform.runLater(() -> {
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.dispose();
                        }
                        Media media = new Media(streamUrl);
                        mediaPlayer = new MediaPlayer(media);
                        mediaView.setMediaPlayer(mediaPlayer);
                        setupVideoControls();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private String formatSeconds(double totalSeconds) {
        if (totalSeconds < 0 || Double.isNaN(totalSeconds)) return "00:00";
        long totalSecLong = (long) totalSeconds;
        long minutes = totalSecLong / 60;
        long seconds = totalSecLong % 60;
        return String.format("%02d:%02d", minutes, seconds);
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
            if (!sliderTime.isValueChanging()) {
                sliderTime.setValue(newTime.toSeconds());
            }
            lblCurrentTime.setText(formatSeconds(newTime.toSeconds()));
        });

        // Tua video khi người dùng kéo thanh trượt
        sliderTime.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (sliderTime.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });

        // Tua video khi người dùng nhấp vào thanh trượt
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
    private Callback<TableColumn<FraudLog, Void>, TableCell<FraudLog, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button btnConfirm = new Button("Xác nhận");
            private final Button btnEdit = new Button("Sửa");
            private final HBox pane = new HBox(8, btnConfirm, btnEdit); // 8px spacing

            {
                btnConfirm.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 11; -fx-cursor: hand;");
                btnEdit.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-size: 11; -fx-cursor: hand;");

                btnConfirm.setOnAction(event -> {
                    FraudLog log = getTableView().getItems().get(getIndex());
                    handleConfirm(log);
                });

                btnEdit.setOnAction(event -> {
                    FraudLog log = getTableView().getItems().get(getIndex());
                    handleEdit(log);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
    }

    private void handleConfirm(FraudLog log) {
        String oldStatus = log.getConclusionStatus();

        log.setConclusionStatus("Đã xác nhận");
        log.setFinalVerdict("Cheating");
        log.setConcluderDate(new Date());

        ConclusionHistory history = new ConclusionHistory();
        history.setOldStatus(oldStatus);
        history.setNewStatus(log.getConclusionStatus());
        history.setConclusionTimestamp(new Date());
        history.setVerdictChanges("User confirmed log");
        history.setFraudLog(log);
        history.setUser(currentUser);

        sendUpdatesToServer(log, history, "Xác nhận");
    }

    private void handleEdit(FraudLog log) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditLog.fxml"));
            Scene scene = new Scene(loader.load());

            EditLogController controller = loader.getController();
            // Truyền ID của video (processingLogId) để EditLogController
            // biết đường quay lại chính xác
            controller.loadLogToEdit(log, currentUser, this.processingLogId);

            // Lấy Stage hiện tại và set Scene mới
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Sửa Log Vi Phạm");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở trang Sửa.");
        }
    }

    @FXML
    private void handleConcludeAll(boolean isConfirming) {
        String newStatus = isConfirming ? "Đã xác nhận" : "Đã từ chối";
        String newVerdict = isConfirming ? "Cheating" : "Not Cheating";
        String verdictChangeLog = isConfirming ? "User confirmed all" : "User refused all";
        String processingLogStatus = isConfirming ? "Completed" : "Refused";

        List<FraudLog> logsToUpdate = new ArrayList<>();
        List<ConclusionHistory> historiesToSave = new ArrayList<>();

        for (FraudLog log : fraudLogsList) {

            String oldStatus = log.getConclusionStatus();

            log.setConclusionStatus(newStatus);
            log.setFinalVerdict(newVerdict);
            log.setConcluderDate(new Date());
            logsToUpdate.add(log);

            ConclusionHistory history = new ConclusionHistory();
            history.setOldStatus(oldStatus);
            history.setNewStatus(newStatus);
            history.setConclusionTimestamp(new Date());
            history.setVerdictChanges(verdictChangeLog);
            history.setFraudLog(log);
            history.setUser(currentUser);
            historiesToSave.add(history);
        }
        if (logsToUpdate.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có log nào để xác nhận.");
            return;
        }
        sendBatchUpdatesToServer(logsToUpdate, historiesToSave, newStatus, processingLogStatus);
    }

    private void sendUpdatesToServer(FraudLog log, ConclusionHistory history, String actionType) {
        tblFraudLog.setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                ApiClient.editFraudLog(log);
                ApiClient.saveConclusionHistory(history);

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", actionType + " log thành công.");
                    tblFraudLog.refresh();
                    tblFraudLog.setDisable(false);
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", actionType + " log thất bại: " + e.getMessage());
                    tblFraudLog.setDisable(false);
                });
            }
        });
    }

    private void sendBatchUpdatesToServer(List<FraudLog> logs, List<ConclusionHistory> histories, String actionType, String processingLogStatus) {
        tblFraudLog.setDisable(true);
        btnConfirmConclude.setDisable(true);
        btnRefuseConclude.setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                ApiClient.editFraudLogBatch(logs);
                ApiClient.saveConclusionHistoryBatch(histories);
                ApiClient.updateProcessingLogStatus(this.processingLogId, processingLogStatus);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã " + actionType + " " + logs.size() + " log và cập nhật video thành " + processingLogStatus + ".");
                    handleBack();
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", actionType + " thất bại: " + e.getMessage());
                    tblFraudLog.setDisable(false);

                    btnConfirmConclude.setDisable(false);
                    btnRefuseConclude.setDisable(false);
                });
            }
        });
    }

    private String formatSecondsWithMillis(double totalSeconds) {
        if (totalSeconds < 0) return "00:00.000";
        long minutes = (long) (totalSeconds / 60.0);
        double secondsPart = totalSeconds % 60.0;
        return String.format("%02d:%06.3f", minutes, secondsPart);
    }

    @FXML
    private void handleBack() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VideoHistory.fxml"));
            Scene scene = new Scene(loader.load());

            VideoHistoryController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Lịch sử video đã xử lý");
        } catch (IOException e) {
            e.printStackTrace();
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
        handleBack();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        switchScene("/fxml/Login.fxml", "Đăng nhập hệ thống");
    }

    private void switchScene(String fxmlPath, String title) {
        // Dừng video trước khi chuyển cảnh
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnLogout.getScene().getWindow(); // Dùng btnLogout để lấy Stage
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