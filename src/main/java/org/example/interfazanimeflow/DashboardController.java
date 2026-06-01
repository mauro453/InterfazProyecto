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
import java.util.Optional;

public class DashboardController {

    @FXML private FlowPane containerAnimes;
    @FXML private Button btnCerrarSesion;
    @FXML private TextField txtBuscar;
    @FXML private Button btnAñadirAnime;

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

        // 1. Configuración de la Imagen de Portada
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

        // 2. Capa StackPane para superponer los tres puntos sobre la imagen
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(180, 210);

        Button btnMenu = new Button("⋮");
        btnMenu.setStyle("-fx-background-color: rgba(17, 24, 39, 0.7); "
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 16; "
                + "-fx-background-radius: 15; "
                + "-fx-padding: 2 8 4 8; "
                + "-fx-cursor: hand;");

        StackPane.setAlignment(btnMenu, Pos.TOP_RIGHT);
        StackPane.setMargin(btnMenu, new Insets(8, 8, 0, 0));

        // Menú flotante al pulsar los tres puntos
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #1F2937; -fx-border-color: #4B5563; -fx-background-radius: 4;");

        // ACCIÓN DE EDICIÓN REAL
        MenuItem itemEditar = new MenuItem("✏️ Editar");
        itemEditar.setStyle("-fx-text-fill: white;");
        itemEditar.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("nuevo-anime.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("AnimeFlow - Editar Anime");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

                NuevoAnimeController nuevoAnimeController = loader.getController();
                nuevoAnimeController.setData(this.usuarioId, this);
                nuevoAnimeController.setAnimeAEditar(anime);

                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // ACCIÓN DE ELIMINACIÓN REAL
        MenuItem itemEliminar = new MenuItem("🗑️ Eliminar");
        itemEliminar.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        itemEliminar.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Eliminar Anime");
            alert.setHeaderText("¿Estás seguro de que quieres eliminar \"" + anime.getTitulo() + "\"?");
            alert.setContentText("Esta acción no se puede deshacer.");

            alert.getDialogPane().setStyle("-fx-background-color: #1F2937;");
            alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
            alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #1F2937;");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String url = "http://localhost:8080/api/animes/" + anime.getId();
                HttpRequest deleteRequest = HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();

                client.sendAsync(deleteRequest, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            Platform.runLater(() -> {
                                if (response.statusCode() == 200 || response.statusCode() == 204) {
                                    cargarAnimesDesdeServidor();
                                } else {
                                    System.out.println("Error al borrar el anime: " + response.statusCode());
                                }
                            });
                        });
            }
        });

        contextMenu.getItems().addAll(itemEditar, itemEliminar);

        btnMenu.setOnAction(e -> {
            contextMenu.show(btnMenu, javafx.geometry.Side.BOTTOM, 0, 0);
            e.consume();
        });

        imageContainer.getChildren().addAll(imageView, btnMenu);

        // 3. Título del Anime
        Label lblTitulo = new Label(anime.getTitulo());
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        lblTitulo.setWrapText(false);
        lblTitulo.setMaxWidth(160);
        VBox.setMargin(lblTitulo, new Insets(0, 8, 0, 8));

        // 4. Puntuación
        Label lblPuntuacion = new Label("⭐ " + (anime.getPuntuacion() != null ? anime.getPuntuacion() : "N/A"));
        lblPuntuacion.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 13;");

        if (anime.getDescripcion() != null) {
            Tooltip tooltip = new Tooltip(anime.getDescripcion());
            tooltip.setStyle("-fx-font-size: 12;");
            Tooltip.install(card, tooltip);
        }

        // Efectos Visuales (Hover)
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #4B5563; -fx-background-radius: 12; -fx-scale-x: 1.03; -fx-scale-y: 1.03; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 12, 0, 0, 6);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #374151; -fx-background-radius: 12; -fx-scale-x: 1.0; -fx-scale-y: 1.0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 4);"));

        card.setOnMouseClicked(e -> {
            if (e.getTarget() == btnMenu) return;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("detalle-anime.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("AnimeFlow - Información de " + anime.getTitulo());
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

                DetalleAnimeController detalleController = loader.getController();
                detalleController.setAnime(anime);

                stage.show();
            } catch (IOException ex) {
                System.out.println("Error al abrir los detalles del anime: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(imageContainer, lblTitulo, lblPuntuacion);
        return card;
    }

    // =========================================================================
    // ¡AQUÍ ESTÁ EL CAMBIO!: DIRECCIÓN DIRECTA A LOGIN.FXML
    // =========================================================================
    @FXML
    protected void onCerrarSesionClick() {
        Platform.runLater(() -> {
            try {
                // Cambiado para que cargue directamente login.fxml en lugar de inicio.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();

                // Cargamos la escena limpia del login sin forzar tamaños harcoded si no quieres
                stage.setScene(new Scene(root));
                stage.setTitle("AnimeFlow - Iniciar Sesión");
                stage.centerOnScreen(); // Centra el login en el monitor para que quede ordenado
                stage.show();
            } catch (IOException e) {
                System.out.println("Error al cerrar sesión de vuelta al login: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    protected void onAñadirAnimeClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("nuevo-anime.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("AnimeFlow - Añadir Anime");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            NuevoAnimeController nuevoAnimeController = loader.getController();
            nuevoAnimeController.setData(this.usuarioId, this);

            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir la ventana de añadir anime: " + e.getMessage());
            e.printStackTrace();
        }
    }
}