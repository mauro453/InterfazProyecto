package org.example.interfazanimeflow;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URI;
import java.net.http.*;
import com.fasterxml.jackson.databind.ObjectMapper; // Necesitarás esta librería
import java.util.Arrays;
import javafx.application.Platform;
import javafx.fxml.FXML;

public class DashboardController {
    @FXML private TableView<Anime> tablaAnimes;
    @FXML private TableColumn<Anime, String> colTitulo;
    @FXML private TableColumn<Anime, Integer> colPuntuacion;
    @FXML private TableColumn<Anime, String> colDescripcion;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper(); // Para convertir JSON a Objetos

    @FXML
    public void initialize() {
        // 1. Vincular columnas con atributos de la clase Anime
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colPuntuacion.setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // 2. Cargar los datos desde el servidor
        cargarAnimesDesdeServidor();
    }

    private void cargarAnimesDesdeServidor() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/animes")) // Asegúrate de que esta sea tu URL
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            // Convertir el JSON (Array) en una lista de objetos Anime
                            Anime[] animesArray = mapper.readValue(response.body(), Anime[].class);

                            Platform.runLater(() -> {
                                ObservableList<Anime> datos = FXCollections.observableArrayList(Arrays.asList(animesArray));
                                tablaAnimes.setItems(datos);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    @FXML
    protected void onLogoutClick() {
        System.out.println("Botón logout pulsado");
        // Lógica para volver atrás
    }
}