module com.trong.server {
    requires java.sql;
    requires jdk.httpserver;
    requires com.trong.common;
    requires com.google.gson;
    requires commons.fileupload;
    requires javafx.graphics;

    exports com.trong.server;
    opens com.trong.server.Controller to com.google.gson;
    opens com.trong.server.DAO to com.google.gson;
}
