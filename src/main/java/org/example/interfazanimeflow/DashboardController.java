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

    // Cambiamos la tabla por nuestro nuevo contenedor de tarjetas
    @FXML private FlowPane containerAnimes;
    @FXML private Button btnCerrarSesion;

    private Long usuarioId;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
        cargarAnimesDesdeServidor();
    }

    @FXML
    public void initialize() {
        // Ya no necesitamos inicializar columnas estáticas de tabla. ¡Más limpio!
    }

    private void cargarAnimesDesdeServidor() {
        String url = "http://localhost:8080/api/animes/usuario/" + this.usuarioId;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            Anime[] animesArray = mapper.readValue(response.body(), Anime[].class);
                            Platform.runLater(() -> {
                                containerAnimes.getChildren().clear(); // Limpiamos catálogo
                                for (Anime anime : animesArray) {
                                    // Creamos una tarjeta visual por cada anime recibido
                                    VBox card = crearTarjetaAnime(anime);
                                    containerAnimes.getChildren().add(card);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    // EL TRUCO DE MAGIA: Construye el diseño visual tipo MyAnimeList para cada anime
    private VBox crearTarjetaAnime(Anime anime) {
        VBox card = new VBox();
        card.setPrefSize(180, 290);
        // Estilo moderno: Fondo oscuro suave, bordes redondeados y efecto hover de escala
        card.setStyle("-fx-background-color: #374151; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 4);");
        card.setAlignment(Pos.TOP_CENTER);
        card.setSpacing(8);

        // 1. Contenedor de la Imagen (Esquinas superiores redondeadas)
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(210);
        imageView.setPreserveRatio(false); // Forzamos escala de cartelera exacta

        if (anime.getPortada() != null && !anime.getPortada().trim().isEmpty()) {
            try {
                String cleanBase64 = anime.getPortada().trim().replaceAll("\\s+", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                imageView.setImage(new Image(new ByteArrayInputStream(imageBytes)));
            } catch (Exception e) {
                // Imagen por defecto si falla el Base64
            }
        }

        // Clip para recortar la imagen con los bordes redondeados de la tarjeta arriba
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(180, 210);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageView.setClip(clip);

        // 2. Título del Anime
        Label lblTitulo = new Label(anime.getTitulo());
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        lblTitulo.setWrapText(false); // Evita romper diseño
        lblTitulo.setMaxWidth(160);
        VBox.setMargin(lblTitulo, new Insets(0, 8, 0, 8));

        // 3. Puntuación (Estilo MyAnimeList ⭐ 8.5)
        Label lblPuntuacion = new Label("⭐ " + (anime.getPuntuacion() != null ? anime.getPuntuacion() : "N/A"));
        lblPuntuacion.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 13;");

        // Tooltip opcional para ver la descripción al pasar el ratón por encima
        if (anime.getDescripcion() != null) {
            Tooltip tooltip = new Tooltip(anime.getDescripcion());
            tooltip.setStyle("-fx-font-size: 12;");
            Tooltip.install(card, tooltip);
        }

        // Efecto visual interactivo al pasar el ratón por encima (Hover)
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
}