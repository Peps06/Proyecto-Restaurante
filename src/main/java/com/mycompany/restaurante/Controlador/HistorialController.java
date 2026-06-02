/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Controlador;

/**
 *
 * @author mrubi
 */


import com.mycompany.restaurante.DAO.HistorialDAO;
import com.mycompany.restaurante.Modelo.HistorialOrden;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class HistorialController implements Initializable {

    @FXML private DatePicker dpFechaHistorial;
    @FXML private TextField txtIdOrden;
    @FXML private Button btnBuscar;

    @FXML private TableView<HistorialOrden> tablaHistorial;
    @FXML private TableColumn<HistorialOrden, Integer> colIdOrden;
    @FXML private TableColumn<HistorialOrden, Integer> colMesa;
    @FXML private TableColumn<HistorialOrden, String> colMesero;
    @FXML private TableColumn<HistorialOrden, LocalDateTime> colFechaHora;
    @FXML private TableColumn<HistorialOrden, String> colEstado;
    @FXML private TableColumn<HistorialOrden, Double> colTotal;

    private HistorialDAO historialDAO;
    private ObservableList<HistorialOrden> listaHistorial;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Aquí debes pasarle tu conexión activa a la base de datos.
        // Como ejemplo usamos una simulación, pero pon la instancia de conexión que use tu equipo:
        // this.historialDAO = new HistorialDAO(ConexionBD.getConexion());
        
        // 2. Colocar la fecha de hoy por defecto
        dpFechaHistorial.setValue(LocalDate.now());

        // 3. Vincular las columnas de la TableView con las variables del modelo HistorialOrden
        colIdOrden.setCellValueFactory(new PropertyValueFactory<>("idOrden"));
        colMesa.setCellValueFactory(new PropertyValueFactory<>("idMesa"));
        colMesero.setCellValueFactory(new PropertyValueFactory<>("mesero"));
        colFechaHora.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    @FXML
    private void handleBuscar(ActionEvent event) {
        String idTexto = txtIdOrden.getText().trim();
        
        // Caso A: Si el usuario escribió un ID de orden en el TextField
        if (!idTexto.isEmpty()) {
            try {
                int idOrden = Integer.parseInt(idTexto);
                HistorialOrden orden = historialDAO.obtenerOrdenPorId(idOrden);
                
                if (orden != null) {
                    listaHistorial = FXCollections.observableArrayList(orden);
                    tablaHistorial.setItems(listaHistorial);
                } else {
                    mostrarAlerta("Información", "No se encontró ninguna orden con el ID: " + idOrden);
                    tablaHistorial.setItems(FXCollections.emptyObservableList());
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Por favor, ingresa un número válido para el ID de orden.");
            }
        } 
        // Caso B: Si el campo de texto está vacío, busca de forma general por la fecha del DatePicker
        else {
            LocalDate fechaSeleccionada = dpFechaHistorial.getValue();
            if (fechaSeleccionada == null) {
                mostrarAlerta("Advertencia", "Seleccione una fecha para realizar la búsqueda.");
                return;
            }
            
            List<HistorialOrden> resultado = historialDAO.obtenerHistorialPorFecha(fechaSeleccionada);
            if (resultado.isEmpty()) {
                mostrarAlerta("Información", "No hay registros de órdenes para la fecha seleccionada.");
                tablaHistorial.setItems(FXCollections.emptyObservableList());
            } else {
                listaHistorial = FXCollections.observableArrayList(resultado);
                tablaHistorial.setItems(listaHistorial);
            }
        }
    }

    @FXML
    private void handleExportarPDF(ActionEvent event) {
        // Aquí meteremos la estructura de iText para el historial
        if (tablaHistorial.getItems().isEmpty()) {
            mostrarAlerta("Advertencia", "No hay datos que exportar.");
            return;
        }
    }

    @FXML
    private void handleExportarExcel(ActionEvent event) {
        // Aquí meteremos la estructura de Apache POI para el historial
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Rutas de navegación de tu menú lateral izquierdo
    private void cambiarPantalla(ActionEvent event, String fxmlPath, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Node nodoOrigen = (Node) event.getSource();
            Stage stageActual = (Stage) nodoOrigen.getScene().getWindow();
            Scene nuevaEscena = new Scene(root);
            stageActual.setScene(nuevaEscena);
            stageActual.setTitle(titulo + " - Saveurs Paris");
            stageActual.centerOnScreen();
            stageActual.show();
        } catch (IOException e) {
            System.err.println("Error al cargar pantalla: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML private void handleEmpleados(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionarEmpleado.fxml", "Gestionar Empleado"); }
    @FXML private void handleMenu(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionMenu.fxml", "Gestionar Menu"); }
    @FXML private void handleAlmacen(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionAlmacen.fxml", "Gestionar Almacen"); }
    @FXML private void handleReportes(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/Reportes.fxml", "Reportes"); }
    @FXML private void handleHistorial(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/Historial.fxml", "Historial de pedidos"); }
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        // Crear la alerta de confirmación
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        // Mostrar y esperar respuesta
        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Usamos tu método genérico que ya maneja el try-catch por dentro
            cambiarPantalla(event, "/com/mycompany/restaurante/fxml/LoginPantalla.fxml", "Iniciar sesión");
        }
    }
}
