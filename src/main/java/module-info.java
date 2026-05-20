module org.example.interfazanimeflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http; // OBLIGATORIO para conectar con el Backend

    // Si usas ControlsFX o ValidatorFX que pusiste en el POM:
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires com.fasterxml.jackson.databind;

    opens org.example.interfazanimeflow to javafx.fxml;
    exports org.example.interfazanimeflow;
}