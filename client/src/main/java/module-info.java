module com.trong.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires com.trong.common;
    requires javafx.media;

    exports com.trong.client;
    opens com.trong.client.Controller to javafx.fxml;
}
