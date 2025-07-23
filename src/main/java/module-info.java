module com.example.polygonnewsdesktop {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;

    opens com.example.polygonnewsdesktop to javafx.fxml;
    exports com.example.polygonnewsdesktop;
    exports com.example.polygonnewsdesktop.Controller;
    opens com.example.polygonnewsdesktop.Controller to javafx.fxml;
}