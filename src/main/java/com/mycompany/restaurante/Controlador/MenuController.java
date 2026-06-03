package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.ProductoDAO;
import com.mycompany.restaurante.Modelo.Producto;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

/**
 * Controlador principal para la visualización del catálogo dinámico de Saveurs Paris.
 * Se encarga de transformar los registros almacenados en la base de datos en tarjetas 
 * visuales de tipo "Polaroid", organizándolos de forma automática dentro de una 
 * cuadrícula (GridPane) interactiva.
 * * @author Dana
 * @version 1.0
 */
public class MenuController {

    // COMPONENTES DE LA INTERFAZ GRÁFICA INYECTADOS POR FXML
    @FXML private GridPane gridMenu;
    @FXML private ImageView idvolver;
    @FXML private Label cantidadPedidos;
    @FXML private Button btnTodos;
    @FXML private Button btnEntradas;
    @FXML private Button btnPlatillos;
    @FXML private Button btnPostres;
    @FXML private Button btnBebidas;
    
    private static final String ESTILO_BTN_ACTIVO =
        "-fx-background-color: #2c3b62; -fx-text-fill: #d4c5b0; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_BTN_INACTIVO =
        "-fx-background-color: #8b1a1a; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10; -fx-text-fill: #d4c5b0;";

    /**
     * Inicializa el entorno de la vista ejecutando la construcción 
     * dinámica de la cartelera del menú.
     */
    @FXML
    public void initialize() {
        btnTodos.setUserData("Todos");
        btnEntradas.setUserData("Entrada");     
        btnPlatillos.setUserData("Plato fuerte");  
        btnPostres.setUserData("Postre");      
        btnBebidas.setUserData("Bebida");       

        cargarMenuDinamico("Todos");
    }

    /**
     * Recupera todos los productos activos de la BD y los renderiza de forma individual,
     * programando la matriz de coordenadas con una distribución máxima de 4 columnas por fila.
     */
    private void cargarMenuDinamico(String categoria) {
        gridMenu.getChildren().clear(); 

        List<Producto> productos = ProductoDAO.obtenerPorTipo(categoria);

        int columna = 0;
        int fila = 0;

        for (Producto p : productos) {
            // Contenedor principal de la tarjeta
            VBox tarjeta = new VBox();
            tarjeta.setAlignment(Pos.CENTER);
            tarjeta.setSpacing(5);
            tarjeta.setPadding(new Insets(10));

            // Título del artículo
            Label lblNombre = new Label(p.getNombre());
            lblNombre.setStyle("-fx-text-fill: #f5efe6; -fx-font-family: 'Candara'; -fx-font-size: 18px;");

            // Contenedor de la imagen 
            VBox marcoImagen = new VBox();
            marcoImagen.setAlignment(Pos.CENTER);
            marcoImagen.setPrefSize(156, 148);
            marcoImagen.getStyleClass().add("marco-polaroid"); 
            
            ImageView imgView = new ImageView();
            imgView.setFitHeight(130);
            imgView.setFitWidth(130);
            imgView.setPreserveRatio(true);
            
            // Cargar la imagen desde la BD o una por defecto si está vacía
            try {
                if (p.getImagen() != null && !p.getImagen().isEmpty()) {
                    imgView.setImage(new Image(p.getImagen()));
                } else {
                    imgView.setImage(new Image(getClass().getResourceAsStream("/img/PlatoVacio.jpg")));
                }
            } catch (Exception e) {
                System.out.println("No se pudo cargar la imagen: " + p.getImagen());
            }
            marcoImagen.getChildren().add(imgView);

            // Despliegue de precio
            Label lblPrecio = new Label("$" + p.getPrecio());
            lblPrecio.setStyle("-fx-text-fill: #f5efe6; -fx-font-family: 'Candara'; -fx-font-size: 18px;");

            // Despliegue de descripción texturizada
            Label lblDesc = new Label(p.getDescripcion());
            lblDesc.setStyle("-fx-text-fill: #f5efe6; -fx-font-family: 'Candara'; -fx-font-size: 12px;");
            lblDesc.setWrapText(true); // Ajuste automático multilínea
            lblDesc.setAlignment(Pos.CENTER);
            lblDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            lblDesc.setMaxWidth(230);

            tarjeta.getChildren().addAll(lblNombre, marcoImagen, lblPrecio, lblDesc);

            gridMenu.add(tarjeta, columna, fila);

            columna++;
            if (columna == 4) {
                columna = 0;
                fila++;
            }
        }
    }
    
    // 3. Agrega el handler de los botones
    @FXML
    private void handleFiltroCategoria(ActionEvent event) {
        Button clickeado = (Button) event.getSource();
        String categoria = (String) clickeado.getUserData(); // Usa el texto del botón como filtro

        // Marcar el botón activo y los demás inactivos
        for (Button btn : new Button[]{btnTodos, btnEntradas, btnPlatillos, btnPostres, btnBebidas}) {
            btn.setStyle(ESTILO_BTN_INACTIVO);
        }
        clickeado.setStyle(ESTILO_BTN_ACTIVO);

        cargarMenuDinamico(categoria);
    }

    /**
     * Detecta el clic en el botón de retroceso y redirige al operador a la interfaz de inicio de sesión.
     * @param event Evento de clic de ratón (MouseEvent) capturado sobre el control gráfico.
     */
    @FXML
    private void handleRegresarLogin(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
            Node nodoOrigen = (Node) event.getSource();
            Stage stageActual = (Stage) nodoOrigen.getScene().getWindow();
            stageActual.setScene(new Scene(root));
            stageActual.setTitle("Iniciar sesión - Saveurs Paris");
            stageActual.centerOnScreen();
            stageActual.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}