module com.mycompany.restaurante {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.restaurante to javafx.fxml;
    opens com.mycompany.restaurante.Controlador to javafx.fxml;

    exports com.mycompany.restaurante;
    exports com.mycompany.restaurante.Controlador;
}
