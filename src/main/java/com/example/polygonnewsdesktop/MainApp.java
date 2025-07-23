package com.example.polygonnewsdesktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/polygonnewsdesktop/view/main-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene( root,1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/example/polygonnewsdesktop/style/main.css").toExternalForm());
        stage.setTitle("Polygon News Desktop");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}