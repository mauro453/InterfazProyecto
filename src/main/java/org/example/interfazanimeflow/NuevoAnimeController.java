package org.example.interfazanimeflow;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NuevoAnimeController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtPuntuacion;
    @FXML private TextArea txtDescripcion;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Button btnSeleccionarFoto;
    @FXML private Label lblNombreArchivo;

    private Long usuarioId;
    private DashboardController dashboardController;
    private File fotoSeleccionada;

    // Almacenará el objeto si se abre en modo edición
    private Anime animeEdicion;

    private final HttpClient client = HttpClient.newHttpClient();

    public void setData(Long usuarioId, DashboardController controller) {
        this.usuarioId = usuarioId;
        this.dashboardController = controller;
    }

    // Método para activar el "Modo Edición" rellenando los campos
    public void setAnimeAEditar(Anime anime) {
        this.animeEdicion = anime;

        // Rellenamos el formulario con los datos que ya existen
        txtTitulo.setText(anime.getTitulo());
        txtPuntuacion.setText(String.valueOf(anime.getPuntuacion()));
        txtDescripcion.setText(anime.getDescripcion() != null ? anime.getDescripcion() : "");
        btnGuardar.setText("Actualizar Anime");

        if (anime.getPortada() != null && !anime.getPortada().trim().isEmpty()) {
            lblNombreArchivo.setText("Mantener portada actual");
        }
    }

    @FXML
    public void initialize() {
        btnGuardar.setOnAction(e -> guardarAnime());
    }

    // Acción para abrir el explorador de archivos del sistema operativo
    @FXML
    protected void onSeleccionarFotoClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Portada del Anime");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.jpeg", "*.png")
        );

        Stage stage = (Stage) btnSeleccionarFoto.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            this.fotoSeleccionada = file;
            lblNombreArchivo.setText(file.getName());
        }
    }

    private void guardarAnime() {
        String titulo = txtTitulo.getText().trim();
        String puntuacionStr = txtPuntuacion.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (titulo.isEmpty() || puntuacionStr.isEmpty()) {
            lblError.setText("El título y la puntuación son obligatorios.");
            return;
        }

        try {
            int puntuacion = Integer.parseInt(puntuacionStr);
            if (puntuacion < 1 || puntuacion > 10) {
                lblError.setText("La puntuación debe ser entre 1 y 10.");
                return;
            }

            // Generamos un código único para separar los campos del formulario Multipart
            String boundary = "---AnimeFlowBoundary" + UUID.randomUUID().toString();

            // Creamos el cuerpo binario de la petición HTTP
            byte[] body = buildMultipartBody(boundary, titulo, puntuacion, descripcion);

            String urlString = "http://localhost:8080/api/animes/upload";
            String metodo = "POST";

            if (animeEdicion != null) {
                // CORREGIDO: Ajustado exactamente a la ruta @PutMapping("/{id}") de tu Spring Boot
                urlString = "http://localhost:8080/api/animes/" + animeEdicion.getId();
                metodo = "PUT";
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .method(metodo, HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            // Tu backend devuelve 200 OK con el objeto mapeado al actualizar o guardar
                            if (response.statusCode() == 200 || response.statusCode() == 204) {
                                dashboardController.setUsuarioId(usuarioId); // Refresca catálogo
                                Stage stage = (Stage) btnGuardar.getScene().getWindow();
                                stage.close();
                            } else {
                                lblError.setText("Error en el servidor: " + response.statusCode());
                            }
                        });
                    });

        } catch (NumberFormatException e) {
            lblError.setText("La puntuación debe ser un número entero.");
        } catch (IOException e) {
            lblError.setText("Error al procesar el archivo de imagen.");
            e.printStackTrace();
        }
    }

    private byte[] buildMultipartBody(String boundary, String titulo, int puntuacion, String descripcion) throws IOException {
        List<byte[]> byteArrays = new ArrayList<>();
        String separator = "--" + boundary + "\r\n";

        // Campo: titulo
        byteArrays.add((separator + "Content-Disposition: form-data; name=\"titulo\"\r\n\r\n" + titulo + "\r\n").getBytes());
        // Campo: puntuacion
        byteArrays.add((separator + "Content-Disposition: form-data; name=\"puntuacion\"\r\n\r\n" + puntuacion + "\r\n").getBytes());
        // Campo: descripcion
        byteArrays.add((separator + "Content-Disposition: form-data; name=\"descripcion\"\r\n\r\n" + descripcion + "\r\n").getBytes());
        // Campo: usuarioId (Spring Boot lo ignorará de forma segura en el PUT, genial para reutilizar el método)
        byteArrays.add((separator + "Content-Disposition: form-data; name=\"usuarioId\"\r\n\r\n" + usuarioId + "\r\n").getBytes());

        // Campo: file (Archivo de imagen adjunto)
        if (fotoSeleccionada != null && fotoSeleccionada.exists()) {
            String fileHeader = separator +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + fotoSeleccionada.getName() + "\"\r\n" +
                    "Content-Type: " + Files.probeContentType(fotoSeleccionada.toPath()) + "\r\n\r\n";
            byteArrays.add(fileHeader.getBytes());
            byteArrays.add(Files.readAllBytes(fotoSeleccionada.toPath()));
            byteArrays.add("\r\n".getBytes());
        }

        // Fin del cuerpo
        byteArrays.add(("--" + boundary + "--\r\n").getBytes());

        int totalLength = byteArrays.stream().mapToInt(arr -> arr.length).sum();
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] arr : byteArrays) {
            System.arraycopy(arr, 0, result, currentIndex, arr.length);
            currentIndex += arr.length;
        }
        return result;
    }
}