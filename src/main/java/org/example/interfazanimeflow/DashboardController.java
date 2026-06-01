package org.example.interfazanimeflow;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class DashboardController {

    @FXML private FlowPane containerAnimes;
    @FXML private Button btnCerrarSesion;
    @FXML private TextField txtBuscar;
    @FXML private Button btnAñadirAnime; // <-- NUEVO: Vinculado con el nuevo botón del FXML

    private Long usuarioId;
    private Anime[] listaAnimesCompleta;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
        cargarAnimesDesdeServidor();
    }

    @FXML
    public void initialize() {
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarYMostrarAnimes(newValue);
        });
    }

    private void cargarAnimesDesdeServidor() {
        String url = "http://localhost:8080/api/animes/usuario/" + this.usuarioId;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            listaAnimesCompleta = mapper.readValue(response.body(), Anime[].class);

                            Platform.runLater(() -> {
                                filtrarYMostrarAnimes("");
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void filtrarYMostrarAnimes(String textoBusqueda) {
        if (listaAnimesCompleta == null) return;

        containerAnimes.getChildren().clear();

        String filtro = textoBusqueda.toLowerCase().trim();

        for (Anime anime : listaAnimesCompleta) {
            if (filtro.isEmpty() || anime.getTitulo().toLowerCase().contains(filtro)) {
                VBox card = crearTarjetaAnime(anime);
                containerAnimes.getChildren().add(card);
            }
        }
    }

    private VBox crearTarjetaAnime(Anime anime) {
        VBox card = new VBox();
        card.setPrefSize(180, 290);
        card.setStyle("-fx-background-color: #374151; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 4);");
        card.setAlignment(Pos.TOP_CENTER);
        card.setSpacing(8);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(210);
        imageView.setPreserveRatio(false);

        if (anime.getPortada() != null && !anime.getPortada().trim().isEmpty()) {
            try {
                String cleanBase64 = anime.getPortada().trim().replaceAll("\\s+", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                imageView.setImage(new Image(new ByteArrayInputStream(imageBytes)));
            } catch (Exception e) {
                // Imagen por defecto si falla el Base64
            }
        }

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(180, 210);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageView.setClip(clip);

        Label lblTitulo = new Label(anime.getTitulo());
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        lblTitulo.setWrapText(false);
        lblTitulo.setMaxWidth(160);
        VBox.setMargin(lblTitulo, new Insets(0, 8, 0, 8));

        Label lblPuntuacion = new Label("⭐ " + (anime.getPuntuacion() != null ? anime.getPuntuacion() : "N/A"));
        lblPuntuacion.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 13;");

        if (anime.getDescripcion() != null) {
            Tooltip tooltip = new Tooltip(anime.getDescripcion());
            tooltip.setStyle("-fx-font-size: 12;");
            Tooltip.install(card, tooltip);
        }

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #4B5563; -fx-background-radius: 12; -fx-scale-x: 1.03; -fx-scale-y: 1.03; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 12, 0, 0, 6);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #374151; -fx-background-radius: 12; -fx-scale-x: 1.0; -fx-scale-y: 1.0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 4);"));

        card.getChildren().addAll(imageView, lblTitulo, lblPuntuacion);
        return card;
    }

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

    // <-- NUEVO MÉTODO: Controla la apertura de la ventana flotante modal para registrar animes
    @FXML
    protected void onAñadirAnimeClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("nuevo-anime.fxml"));
            Parent root = loader.load();

            // Configuramos la ventana flotante (Stage)
            Stage stage = new Stage();
            stage.setTitle("AnimeFlow - Añadir Anime");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Hace que la ventana sea modal (bloquea la de atrás hasta que se cierre)
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Pasamos los datos del ID y la referencia de este controlador para refrescar la lista al guardar
            NuevoAnimeController nuevoAnimeController = loader.getController();
            nuevoAnimeController.setData(this.usuarioId, this);

            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir la ventana de añadir anime: " + e.getMessage());
            e.printStackTrace();
        }
    }
}