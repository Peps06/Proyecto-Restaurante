module com.mycompany.dulceria {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.mycompany.dulceria to javafx.fxml, javafx.graphics;
    opens com.mycompany.dulceria.Controlador to javafx.fxml;
    exports com.mycompany.dulceria;
}
