package com.trong.client.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.trong.client.util.ApiClient;
import com.trong.model.User;
import com.trong.model.VideoHistoryDTO; // Sử dụng DTO thật
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Controller cho VideoHistory.fxml
 * Tải và hiển thị danh sách video đã xử lý.
 */
public class VideoHistoryController {

    @FXML private TableView<VideoHistoryDTO> tblProcessSummary;
    @FXML private TableColumn<VideoHistoryDTO, Integer> colIdVideo;
    @FXML private TableColumn<VideoHistoryDTO, String> colTenVideo;
    @FXML private TableColumn<VideoHistoryDTO, String> colNgayTaiLen;
    @FXML private TableColumn<VideoHistoryDTO, String> colTrangThai;
    @FXML private TableColumn<VideoHistoryDTO, Integer> colSoViPham;
    @FXML private TableColumn<VideoHistoryDTO, Void> colHanhDong;

    @FXML private Button btnLogout;

    @FXML private Label navHome;
    @FXML private Label navUpload;
    @FXML private Label navHistory;

    private User currentUser;
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");


    @FXML
    public void initialize() {
        colIdVideo.setCellValueFactory(new PropertyValueFactory<>("idVideo"));
        colTenVideo.setCellValueFactory(new PropertyValueFactory<>("tenVideo"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThaiXuLy"));
        colSoViPham.setCellValueFactory(new PropertyValueFactory<>("soViPham"));

        colNgayTaiLen.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getNgayTaiLen();
            String formattedDate = (date != null) ? dateFormat.format(date) : "";
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });

        colTrangThai.setCellFactory(col -> new StatusBadgeCell());
        colHanhDong.setCellFactory(createActionCellFactory());

        btnLogout.setOnAction(e -> handleLogout());
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadHistoryData();
    }
    private void loadHistoryData() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có thông tin người dùng.");
            return;
        }

        tblProcessSummary.setPlaceholder(new Label("Đang tải dữ liệu lịch sử..."));
        new Thread(() -> {
            try {

                String jsonResponse = ApiClient.getVideoHistory(currentUser.getIdUser());


                Type listType = new TypeToken<List<VideoHistoryDTO>>(){}.getType();
                List<VideoHistoryDTO> historyList = gson.fromJson(jsonResponse, listType);


                Platform.runLater(() -> {
                    ObservableList<VideoHistoryDTO> data = FXCollections.observableArrayList(historyList);
                    tblProcessSummary.setItems(data);
                    if (data.isEmpty()) {
                        tblProcessSummary.setPlaceholder(new Label("Không tìm thấy lịch sử nào."));
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không thể tải lịch sử: " + e.getMessage())
                );
            }
        }).start();
    }

    private Callback<TableColumn<VideoHistoryDTO, Void>, TableCell<VideoHistoryDTO, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button btn = new Button("Xem chi tiết");
            {
                btn.setStyle(
                        "-fx-background-color: #ffffff; -fx-border-color: #ced4da; " +
                                "-fx-border-radius: 4; -fx-background-radius: 4; " +
                                "-fx-font-size: 12px; -fx-padding: 4 8 4 8; -fx-cursor: hand;"
                );
                btn.setOnAction(event -> {
                    VideoHistoryDTO rowData = getTableView().getItems().get(getIndex());
                    handleViewDetail(rowData.getIdPL()); // Gọi hàm chuyển scene
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };
    }

    private void handleViewDetail(int processingLogId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConcludeVideo.fxml"));
            Scene scene = new Scene(loader.load());
            ConcludeVideoController controller = loader.getController();
            controller.loadData(currentUser, processingLogId);

            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Kết luận gian lận Video");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở trang chi tiết.");
        }
    }
    private static class StatusBadgeCell extends TableCell<VideoHistoryDTO, String> {
        private final Label badgeLabel = new Label();
        private final HBox wrapper = new HBox(badgeLabel);

        public StatusBadgeCell() {
            wrapper.setAlignment(Pos.CENTER_LEFT);
            badgeLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            badgeLabel.setTextFill(Color.WHITE);
        }

        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            if (empty || status == null) {
                setGraphic(null);
                return;
            }

            badgeLabel.setText(status);
            String bgColor;

            if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Đã xử lý xong")) {
                bgColor = "#28a745"; // Xanh
            } else if (status.equalsIgnoreCase("Processing") || status.equalsIgnoreCase("Đang xử lý")) {
                bgColor = "#ffc107"; // Vàng
            } else if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Cần xem xét")) {
                bgColor = "#dc3545"; // Đỏ
            } else {
                bgColor = "#6c757d"; // Xám
            }

            badgeLabel.setStyle(commonStyle(bgColor));
            setGraphic(wrapper);
        }

        private String commonStyle(String bg) {
            return "-fx-background-color: " + bg + ";" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 3 8 3 8;" +
                    "-fx-text-fill: white;";
        }
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
    private void handleLogout() {
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
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể chuyển trang: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}