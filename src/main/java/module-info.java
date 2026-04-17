module com.mycompany.restaurante {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.restaurante to javafx.fxml;
    exports com.mycompany.restaurante;
}
