package org.example.interfazanimeflow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import javafx.application.Platform;

public class HelloController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblStatus;
    @FXML
    private Button btnLogin; // Asegúrate de importar javafx.scene.control.Button

    private final HttpClient client = HttpClient.newHttpClient();

    @FXML
    protected void onLoginClick() {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", user, pass);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            lblStatus.setText("¡Bienvenido!");
                            lblStatus.setStyle("-fx-text-fill: green;");

                            // LLAMADA AL CAMBIO DE PANTALLA
                            cambiarAPantallaDashboard();
                        });
                    } else {
                        Platform.runLater(() -> {
                            lblStatus.setText("Error: Credenciales inválidas");
                            lblStatus.setStyle("-fx-text-fill: red;");
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> lblStatus.setText("Error de conexión con el servidor"));
                    return null;
                });
    }

    // HelloController.java

    private void cambiarAPantallaDashboard() {
        // Usamos Platform.runLater por si vienes de una respuesta asíncrona del servidor
        Platform.runLater(() -> {
            try {
                // 1. Cargar el nuevo archivo FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
                Parent root = loader.load();

                // 2. Obtener la ventana actual a través del botón
                Stage stage = (Stage) btnLogin.getScene().getWindow();

                // 3. Crear la nueva escena y aplicarla
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("AnimeFlow - Dashboard");
                stage.show();

            } catch (IOException e) {
                lblStatus.setText("Error al cargar la pantalla");
                e.printStackTrace();
            }
        });
    }
}