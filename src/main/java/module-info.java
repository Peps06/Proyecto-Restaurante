module com.mycompany.restaurante {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires itextpdf;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
   

    opens com.mycompany.restaurante.fxml to javafx.fxml;
    opens com.mycompany.restaurante.Controlador to javafx.fxml;
    //opens com.mycompany.restaurante.Modelo to javafx.base;
    opens com.mycompany.restaurante.Modelo to javafx.base, org.apache.poi.ooxml;

    exports com.mycompany.restaurante;
    exports com.mycompany.restaurante.Controlador;
    exports com.mycompany.restaurante.DAO;
    exports com.mycompany.restaurante.Modelo;
}
