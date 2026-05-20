package org.example.interfazanimeflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        // Modifica los números aquí: 400 de ancho, 550 de alto
        Scene scene = new Scene(fxmlLoader.load(), 400, 550);
        stage.setTitle("AnimeFlow - Login");
        stage.setScene(scene);
        stage.setResizable(false); // Tip: Evita que el usuario la estire y se rompa el diseño
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
