module com.trong.common {
    requires com.google.gson;

    exports com.trong.model;
    opens com.trong.model to com.google.gson;
}
