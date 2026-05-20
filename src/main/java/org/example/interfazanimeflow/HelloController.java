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
import org.example.interfazanimeflow.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper; // <-- IMPORTANTE: Faltaba esta importación

public class HelloController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblStatus;
    @FXML private Button btnLogin;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper(); // <-- SOLUCIÓN: Instanciamos el mapper aquí

    @FXML
    protected void onLoginClick() {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        // Creamos el JSON para la autenticación
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
                            try {
                                // 1. Mapeamos el JSON al objeto Usuario usando el mapper que acabamos de definir arriba
                                Usuario usuarioLogueado = mapper.readValue(response.body(), Usuario.class);

                                // 2. Extraemos el ID del usuario conectado
                                Long idUsuario = usuarioLogueado.getId();

                                // 3. Cargamos el archivo FXML del Dashboard
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
                                Parent root = loader.load();

                                // 4. Obtenemos el controlador del Dashboard y le inyectamos el ID para filtrar sus animes
                                DashboardController dashboardController = loader.getController();
                                dashboardController.setUsuarioId(idUsuario);

                                // 5. Cambiamos la escena de la ventana actual para mostrar el panel
                                Stage stage = (Stage) btnLogin.getScene().getWindow();
                                stage.setScene(new Scene(root, 1050, 720));
                                stage.setTitle("AnimeFlow - Panel Principal");
                                stage.centerOnScreen();
                                stage.show();

                            } catch (IOException e) {
                                lblStatus.setText("Error al cargar la pantalla");
                                System.out.println("Error de lectura JSON o FXML: " + e.getMessage());
                                e.printStackTrace();
                            } catch (Exception e) {
                                lblStatus.setText("Error inesperado");
                                e.printStackTrace();
                            }
                        });
                    } else {
                        // Si las credenciales fallan, avisamos al usuario en la interfaz
                        Platform.runLater(() -> {
                            lblStatus.setText("Usuario o contraseña incorrectos");
                            System.out.println("Credenciales incorrectas. Código: " + response.statusCode());
                        });
                    }
                });
    }
    @FXML
    public void initialize() {
        // Animación hover para el botón de login
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle("-fx-background-color: #2563EB; -fx-background-radius: 8; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 15;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle("-fx-background-color: #3B82F6; -fx-background-radius: 8; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 15;"));
    }
}
