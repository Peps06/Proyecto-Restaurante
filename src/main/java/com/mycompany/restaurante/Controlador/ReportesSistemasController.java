/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Controlador;
import com.mycompany.restaurante.DAO.ReporteDAO;
import com.mycompany.restaurante.Modelo.ReporteVenta;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * @author mrubi
 */
public class ReportesSistemasController implements Initializable {

    @FXML private ComboBox<String> cmbTipoReporte;
    @FXML private DatePicker dpFecha;
    @FXML private Button btnGenerar;
    
    @FXML private TableView<ReporteVenta> tablaReportes;
    @FXML private TableColumn<ReporteVenta, Integer> colIdPedido;
    @FXML private TableColumn<ReporteVenta, LocalDate> colFecha;
    @FXML private TableColumn<ReporteVenta, String> colProducto;
    @FXML private TableColumn<ReporteVenta, Integer> colCantidad;
    @FXML private TableColumn<ReporteVenta, Double> colTotal;

    // Instancia del DAO para conectar a MySQL
    private ReporteDAO reporteDAO;
    // Lista observable para manejar los datos en la TableView
    private ObservableList<ReporteVenta> listaReportes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Inicializar el DAO
        reporteDAO = new ReporteDAO();
        
        // 2. Llenar el ComboBox de opciones
        cmbTipoReporte.setItems(FXCollections.observableArrayList("Diario", "Mensual"));
        cmbTipoReporte.getSelectionModel().selectFirst(); // Selecciona "Diario" por defecto
        
        // 3. Poner la fecha de hoy por defecto en el DatePicker
        dpFecha.setValue(LocalDate.now());
        
        // 4. Configurar las columnas para que sepan qué dato jalar de la entidad ReporteVenta
        colIdPedido.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    @FXML
    private void handleGenerarReporte(ActionEvent event) {
        String tipo = cmbTipoReporte.getValue();
        LocalDate fechaSeleccionada = dpFecha.getValue();

        // Validación preventiva de UX: que el usuario no deje la fecha vacía
        if (fechaSeleccionada == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione una fecha válida.");
            return;
        }

        List<ReporteVenta> resultado;

        // Evaluamos el tipo de reporte seleccionado
        if ("Diario".equals(tipo)) {
            resultado = reporteDAO.obtenerReporteDiario(fechaSeleccionada);
        } else { // "Mensual"
            int anio = fechaSeleccionada.getYear();
            int mes = fechaSeleccionada.getMonthValue();
            resultado = reporteDAO.obtenerReporteMensual(anio, mes);
        }

        // Validamos si la consulta regresó registros en el almacén de datos
        if (resultado.isEmpty()) {
            mostrarAlerta("Información", "No existen registros de ventas para el periodo seleccionado.");
            tablaReportes.setItems(FXCollections.emptyObservableList()); // Limpia la tabla si estaba llena
        } else {
            // Pasamos los datos a la lista observable y los montamos en la TableView
            listaReportes = FXCollections.observableArrayList(resultado);
            tablaReportes.setItems(listaReportes);
        }
    }

    // Método auxiliar para lanzar alertas visuales de forma rápida
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

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
    @FXML
private void handleExportarPDF(ActionEvent event) {
    // 1. Validar que la tabla no esté vacía
    if (tablaReportes.getItems().isEmpty()) {
        mostrarAlerta("Advertencia", "No hay datos en la tabla para exportar.");
        return;
    }

    // 2. Configurar el selector de archivos (Donde guardar)
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Guardar Reporte PDF");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf"));
    fileChooser.setInitialFileName("Reporte_Ventas_" + cmbTipoReporte.getValue() + "_" + dpFecha.getValue().toString() + ".pdf");
    
    // Obtener la ventana actual
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    File destino = fileChooser.showSaveDialog(stage);

    if (destino != null) {
        Document documento = new Document();
        try {
            PdfWriter.getInstance(documento, new FileOutputStream(destino));
            documento.open();

            // Encabezado del PDF
            documento.add(new Paragraph("SAVEURS PARIS - REPORTE DE SISTEMAS"));
            documento.add(new Paragraph("Tipo de Reporte: " + cmbTipoReporte.getValue()));
            documento.add(new Paragraph("Fecha de Consulta: " + dpFecha.getValue().toString()));
            documento.add(new Paragraph("------------------------------------------------------------------------------------------\n\n"));

            // Crear una tabla en el PDF con 5 columnas (igual que tu TableView)
            PdfPTable tablaPdf = new PdfPTable(5);
            tablaPdf.setWidthPercentage(100); // Que ocupe todo el ancho disponible

            // Títulos de las columnas en el PDF
            tablaPdf.addCell(new PdfPCell(new Phrase("ID Pedido")));
            tablaPdf.addCell(new PdfPCell(new Phrase("Fecha")));
            tablaPdf.addCell(new PdfPCell(new Phrase("Producto")));
            tablaPdf.addCell(new PdfPCell(new Phrase("Cantidad")));
            tablaPdf.addCell(new PdfPCell(new Phrase("Total Venta")));

            // Recorrer los datos de tu TableView e insertarlos en el PDF
            double granTotal = 0;
            for (ReporteVenta fila : tablaReportes.getItems()) {
                tablaPdf.addCell(String.valueOf(fila.getIdPedido()));
                tablaPdf.addCell(fila.getFecha().toString());
                tablaPdf.addCell(fila.getProducto());
                tablaPdf.addCell(String.valueOf(fila.getCantidad()));
                tablaPdf.addCell("$" + String.valueOf(fila.getTotal()));
                granTotal += fila.getTotal();
            }

            // Agregar la tabla al documento
            documento.add(tablaPdf);
            
            // Añadir el total general abajo de la tabla
            documento.add(new Paragraph("\nTotal acumulado en el periodo: $" + granTotal));

            documento.close();
            mostrarAlerta("Éxito", "El reporte PDF se ha guardado correctamente.");

        } catch (DocumentException | java.io.FileNotFoundException e) {
            mostrarAlerta("Error", "Ocurrió un error al generar el archivo PDF.");
            e.printStackTrace();
        }
    }
}

@FXML
private void handleExportarExcel(ActionEvent event) {
    // 1. Validar si hay datos en la tabla
    if (tablaReportes.getItems().isEmpty()) {
        mostrarAlerta("Advertencia", "No hay datos en la tabla para exportar.");
        return;
    }

    // 2. Configurar el selector de archivos para guardar como .xlsx
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Guardar Reporte Excel");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Excel (*.xlsx)", "*.xlsx"));
    fileChooser.setInitialFileName("Reporte_Ventas_" + cmbTipoReporte.getValue() + "_" + dpFecha.getValue().toString() + ".xlsx");

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    File destino = fileChooser.showSaveDialog(stage);

    if (destino != null) {
        // Crear el libro de trabajo de Excel (.xlsx)
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet pagina = workbook.createSheet("Ventas " + cmbTipoReporte.getValue());

            // 3. Crear la fila del encabezado (Fila 0)
            Row filaEncabezado = pagina.createRow(0);
            String[] columnas = {"ID Pedido", "Fecha", "Producto", "Cantidad", "Total Venta"};
            
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = filaEncabezado.createCell(i);
                celda.setCellValue(columnas[i]);
            }

            // 4. Llenar el cuerpo del Excel con los datos de tu TableView
            int numeroFila = 1;
            double granTotal = 0;
            
            for (ReporteVenta filaReporte : tablaReportes.getItems()) {
                Row filaData = pagina.createRow(numeroFila++);
                
                filaData.createCell(0).setCellValue(filaReporte.getIdPedido());
                filaData.createCell(1).setCellValue(filaReporte.getFecha().toString());
                filaData.createCell(2).setCellValue(filaReporte.getProducto());
                filaData.createCell(3).setCellValue(filaReporte.getCantidad());
                filaData.createCell(4).setCellValue(filaReporte.getTotal());
                
                granTotal += filaReporte.getTotal();
            }

            // 5. Añadir fila de Total General al final
            Row filaTotal = pagina.createRow(numeroFila + 1);
            filaTotal.createCell(3).setCellValue("Total Acumulado:");
            filaTotal.createCell(4).setCellValue(granTotal);

            // Autoajustar el ancho de las columnas para que se vea ordenado
            for (int i = 0; i < columnas.length; i++) {
                pagina.autoSizeColumn(i);
            }

            // 6. Escribir el archivo en el disco
            try (FileOutputStream fileOut = new FileOutputStream(destino)) {
                workbook.write(fileOut);
            }

            mostrarAlerta("Éxito", "El reporte de Excel se ha guardado correctamente.");

        } catch (IOException e) {
            mostrarAlerta("Error", "Ocurrió un error al generar el archivo de Excel.");
            e.printStackTrace();
        }
    }
}

    // Métodos de acceso rápido para los botones del menú lateral
    @FXML private void handleEmpleados(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionarEmpleado.fxml", "Gestionar Empleado"); }
    @FXML private void handleMenu(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionMenu.fxml", "Gestionar Menu"); }
    @FXML private void handleAlmacen(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionAlmacen.fxml", "Gestionar Almacen"); }
    @FXML private void handleReportes(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/Reportes.fxml", "Reportes"); }
    @FXML private void handleHistorial(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/Historial.fxml", "Historial de Pedidos"); }
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
