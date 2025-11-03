module com.trong.gianlan {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.trong.gianlan to javafx.fxml;
    exports com.trong.gianlan;
}