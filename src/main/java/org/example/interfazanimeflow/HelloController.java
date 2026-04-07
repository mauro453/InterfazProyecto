package org.example.interfazanimeflow;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URI;
import java.net.http.*;
import javafx.application.Platform;

public class HelloController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblStatus;

    private final HttpClient client = HttpClient.newHttpClient();

    @FXML
    protected void onLoginClick() {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        // JSON manual para el login
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", user, pass);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        // El login devolvió el Usuario (puedes ver el ID en response.body())
                        Platform.runLater(() -> {
                            lblStatus.setText("¡Bienvenido!");
                            lblStatus.setStyle("-fx-text-fill: green;");
                            // Aquí llamarías a un método para cambiar de pantalla
                        });
                    } else {
                        Platform.runLater(() -> {
                            lblStatus.setText("Error: Credenciales inválidas");
                            lblStatus.setStyle("-fx-text-fill: red;");
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> lblStatus.setText("Servidor apagado"));
                    return null;
                });
    }
}