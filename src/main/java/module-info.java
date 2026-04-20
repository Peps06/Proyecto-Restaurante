module com.mycompany.restaurante {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.restaurante.fxml to javafx.fxml;
    opens com.mycompany.restaurante.Controlador to javafx.fxml;
    opens com.mycompany.restaurante.Modelo to javafx.base;

    exports com.mycompany.restaurante;
    exports com.mycompany.restaurante.Controlador;
}
