package org.example.interfazanimeflow;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class DetalleAnimeController {

    @FXML private ImageView imgPortada;
    @FXML private Label lblPuntuacion;
    @FXML private Label lblTitulo;
    @FXML private Label lblDescripcion;

    public void setAnime(Anime anime) {
        lblTitulo.setText(anime.getTitulo());
        lblPuntuacion.setText("⭐ " + (anime.getPuntuacion() != null ? anime.getPuntuacion() : "N/A") + " / 10");
        lblDescripcion.setText(anime.getDescripcion() != null && !anime.getDescripcion().isEmpty()
                ? anime.getDescripcion()
                : "Este anime no contiene ninguna descripción o sinopsis todavía.");

        // Cargamos la carátula si existe en Base64
        if (anime.getPortada() != null && !anime.getPortada().trim().isEmpty()) {
            try {
                String cleanBase64 = anime.getPortada().trim().replaceAll("\\s+", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                imgPortada.setImage(new Image(new ByteArrayInputStream(imageBytes)));
            } catch (Exception e) {
                // Si falla o la imagen está corrupta, se queda vacía o puedes poner una por defecto
            }
        }

        // Clip sutil para redondear también las esquinas superiores de la portada gigante
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(220, 310);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imgPortada.setClip(clip);
    }
}