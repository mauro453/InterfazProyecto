package org.example.interfazanimeflow;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class DashboardController {

    @FXML private TableView<Anime> tablaAnimes;
    @FXML private TableColumn<Anime, String> colTitulo;
    @FXML private TableColumn<Anime, Integer> colPuntuacion;
    @FXML private TableColumn<Anime, String> colDescripcion;
    @FXML private TableColumn<Anime, String> colFoto; // Tu columna de portada en el FXML
    @FXML private Button btnCerrarSesion;

    // Variable para saber qué usuario está conectado
    private Long usuarioId;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // 1. ESTE MÉTODO RECIBE EL ID DESDE EL LOGIN Y ARRANCA LA CARGA
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
        cargarAnimesDesdeServidor(); // Se ejecuta SOLO cuando ya sabemos el ID
    }

    @FXML
    public void initialize() {
        // Configurar columnas de texto
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colPuntuacion.setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Configurar columna de la imagen (apuntando a 'portada')
        colFoto.setCellValueFactory(new PropertyValueFactory<>("portada"));
        colFoto.setCellFactory(new Callback<TableColumn<Anime, String>, TableCell<Anime, String>>() {
            @Override
            public TableCell<Anime, String> call(TableColumn<Anime, String> param) {
                return new TableCell<Anime, String>() {
                    private final ImageView imageView = new ImageView();
                    {
                        imageView.setFitWidth(60);
                        imageView.setFitHeight(80);
                        imageView.setPreserveRatio(true);
                    }

                    @Override
                    protected void updateItem(String base64Portada, boolean empty) {
                        super.updateItem(base64Portada, empty);
                        if (empty || base64Portada == null || base64Portada.trim().isEmpty()) {
                            setGraphic(null);
                        } else {
                            try {
                                String cleanBase64 = base64Portada.trim().replaceAll("\\s+", "");
                                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                                Image image = new Image(bis);
                                imageView.setImage(image);
                                setGraphic(imageView);
                            } catch (Exception e) {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });

        // Forzar tamaño alto de las filas para las fotos
        tablaAnimes.setRowFactory(tv -> {
            TableRow<Anime> row = new TableRow<>();
            row.prefHeightProperty().setValue(90);
            row.setMinHeight(90);
            return row;
        });

        // NOTA: Ya no llamamos a cargarAnimesDesdeServidor() aquí adentro.
    }

    // 2. MÉTODO QUE FILTRA LOS ANIMES POR EL ID DEL USUARIO
    private void cargarAnimesDesdeServidor() {
        // Modificamos la URL para enviarle el ID del usuario al Backend
        // Cambia esto en tu JavaFX:
        String url = "http://localhost:8080/api/animes/usuario/" + this.usuarioId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            Anime[] animesArray = mapper.readValue(response.body(), Anime[].class);
                            Platform.runLater(() -> {
                                tablaAnimes.getItems().clear();
                                tablaAnimes.getItems().addAll(animesArray);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    // 3. MÉTODO PARA VOLVER AL LOGIN (CERRAR SESIÓN)
    @FXML
    protected void onCerrarSesionClick() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("AnimeFlow - Login");
                stage.show();
            } catch (IOException e) {
                System.out.println("Error al cerrar sesión: " + e.getMessage());
            }
        });
    }
}