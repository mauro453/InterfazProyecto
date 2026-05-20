package org.example.interfazanimeflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
// Al estar en carpetas espejadas, basta con poner el nombre del archivo
// HelloApplication.java - Línea 13
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/interfazanimeflow/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 450);
        stage.setTitle("MyAnimeList - Desktop");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
