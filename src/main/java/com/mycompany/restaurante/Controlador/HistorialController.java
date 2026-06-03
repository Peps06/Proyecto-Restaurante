package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.HistorialDAO;
import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.HistorialOrden;
import java.net.URL;
import java.sql.SQLException;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Rubi
 * @version 1.0
 */

public class HistorialController implements Initializable {

    @FXML private DatePicker dpFechaHistorial;
    @FXML private RadioButton rbMatutino;
    @FXML private RadioButton rbVespertino;
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
    // Inicializar el DAO atrapando la SQLException que arroja tu clase ConexionDB
    try {
        this.historialDAO = new HistorialDAO(ConexionDB.getConexion());
    } catch (SQLException e) {
        System.err.println("¡Error de conexión con MySQL en el módulo de Historial!");
        e.printStackTrace();
        mostrarAlerta("Error de Base de Datos", "No se pudo conectar con el servidor de MySQL. Verifica que esté encendido.");
    }
    
    // Colocar la fecha de hoy por defecto
    dpFechaHistorial.setValue(LocalDate.now());

    // Vincular columnas de la TableView
    colIdOrden.setCellValueFactory(new PropertyValueFactory<>("idOrden"));
    colMesa.setCellValueFactory(new PropertyValueFactory<>("idMesa"));
    colMesero.setCellValueFactory(new PropertyValueFactory<>("mesero"));
    colFechaHora.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
    colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
    colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
}

    @FXML
    private void handleBuscar(ActionEvent event) {
    LocalDate fechaSeleccionada = dpFechaHistorial.getValue();
    if (fechaSeleccionada == null) {
        mostrarAlerta("Advertencia", "Por favor, seleccione una fecha válida.");
        return;
    }

    // Determinar qué turno está seleccionado
    String turnoSeleccionado = "";
    if (rbMatutino.isSelected()) {
        turnoSeleccionado = "Matutino";
    } else if (rbVespertino.isSelected()) {
        turnoSeleccionado = "Vespertino";
    } else {
        mostrarAlerta("Advertencia", "Por favor, seleccione un turno para filtrar.");
        return;
    }

    // Consultar a la base de datos con ambos filtros
    List<HistorialOrden> resultado = historialDAO.obtenerHistorialPorFechaYTurno(fechaSeleccionada, turnoSeleccionado);
    
    if (resultado.isEmpty()) {
        mostrarAlerta("Información", "No hay registros de órdenes para el turno " + turnoSeleccionado + " en la fecha elegida.");
        tablaHistorial.setItems(FXCollections.emptyObservableList());
    } else {
        listaHistorial = FXCollections.observableArrayList(resultado);
        tablaHistorial.setItems(listaHistorial);
    }
}
    @FXML
    private void handleExportarPDF(ActionEvent event) {
    if (tablaHistorial.getItems().isEmpty()) {
        mostrarAlerta("Advertencia", "No hay registros en la tabla para exportar a PDF.");
        return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Guardar Historial PDF");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf"));
    fileChooser.setInitialFileName("Historial_Pedidos_" + dpFechaHistorial.getValue().toString() + ".pdf");

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    File destino = fileChooser.showSaveDialog(stage);

    if (destino != null) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(destino));
            document.open();

            // Encabezado del PDF
            document.add(new Paragraph("SAVEURS PARIS - HISTORIAL DE PEDIDOS"));
            String turnoLog = rbMatutino.isSelected() ? "Matutino" : "Vespertino";
            document.add(new Paragraph("Fecha: " + dpFechaHistorial.getValue().toString() + "  |  Turno: " + turnoLog));
            document.add(new Paragraph(" ")); // Espacio en blanco

            // Crear tabla de 6 columnas (id, mesa, mesero, fecha, estado, total)
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // Encabezados de columnas
            String[] headers = {"ID Orden", "Mesa", "Mesero", "Fecha/Hora", "Estado", "Total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header));
                table.addCell(cell);
            }

            // Llenar datos desde la TableView
            for (HistorialOrden orden : tablaHistorial.getItems()) {
                table.addCell(String.valueOf(orden.getIdOrden()));
                table.addCell(String.valueOf(orden.getIdMesa()));
                table.addCell(orden.getMesero());
                table.addCell(orden.getFechaHora().toString().replace("T", " "));
                table.addCell(orden.getEstado());
                table.addCell(String.format("$%.2f", orden.getTotal()));
            }

            document.add(table);
            mostrarAlerta("Éxito", "El historial se ha exportado correctamente a PDF.");

        } catch (DocumentException | java.io.FileNotFoundException e) {
            mostrarAlerta("Error", "Ocurrió un error al generar el archivo PDF.");
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}

    @FXML
    private void handleExportarExcel(ActionEvent event) {
    if (tablaHistorial.getItems().isEmpty()) {
        mostrarAlerta("Advertencia", "No hay registros en la tabla para exportar a Excel.");
        return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Guardar Historial Excel");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Excel (*.xlsx)", "*.xlsx"));
    fileChooser.setInitialFileName("Historial_Pedidos_" + dpFechaHistorial.getValue().toString() + ".xlsx");

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    File destino = fileChooser.showSaveDialog(stage);

    if (destino != null) {
        try (Workbook workbook = new XSSFWorkbook()) {
            String turnoLog = rbMatutino.isSelected() ? "Matutino" : "Vespertino";
            Sheet pagina = workbook.createSheet("Pedidos " + turnoLog);

            // Cabecera de la hoja
            Row filaEncabezado = pagina.createRow(0);
            String[] columnas = {"ID Orden", "Mesa", "Mesero", "Fecha/Hora", "Estado", "Total"};
            
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = filaEncabezado.createCell(i);
                celda.setCellValue(columnas[i]);
            }

            // Llenar filas de datos
            int numeroFila = 1;
            for (HistorialOrden orden : tablaHistorial.getItems()) {
                Row filaData = pagina.createRow(numeroFila++);
                
                filaData.createCell(0).setCellValue(orden.getIdOrden());
                filaData.createCell(1).setCellValue(orden.getIdMesa());
                filaData.createCell(2).setCellValue(orden.getMesero());
                filaData.createCell(3).setCellValue(orden.getFechaHora().toString().replace("T", " "));
                filaData.createCell(4).setCellValue(orden.getEstado());
                filaData.createCell(5).setCellValue(orden.getTotal());
            }

            // Autoajustar el tamaño de las columnas
            for (int i = 0; i < columnas.length; i++) {
                pagina.autoSizeColumn(i);
            }

            // Escribir el archivo final
            try (FileOutputStream fileOut = new FileOutputStream(destino)) {
                workbook.write(fileOut);
            }

            mostrarAlerta("Éxito", "El historial se ha exportado correctamente a Excel.");

        } catch (IOException e) {
            mostrarAlerta("Error", "Ocurrió un error al generar el archivo de Excel.");
            e.printStackTrace();
        }
    }
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
