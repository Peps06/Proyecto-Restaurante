package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.MesasDAO;
import com.mycompany.restaurante.DAO.PedidoDAO;
import com.mycompany.restaurante.Modelo.Mesa;
import com.mycompany.restaurante.Modelo.OrdenItem;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador de la pantalla principal de gestión de mesas para el Mesero.
 * Permite visualizar el mapa de mesas del restaurante identificando su estado
 * mediante tres colores distintos, y navegar hacia la toma de pedidos.
 *
 * Estados visuales:
 * Azul   (#627096) → Libre: sin cliente asignado.
 * Café   (#6A500F) → Ocupada sin orden: el recepcionista asignó al cliente
 * pero el mesero aún no ha registrado el pedido.
 * Rojo   (#8A3636) → Ocupada con orden: ya existe una orden abierta en BD.
 *
 * @author Citlaly
 * @version 2.0
 */
public class MesasDisponiblesController implements Initializable {

    private static final Logger LOG = Logger.getLogger(MesasDisponiblesController.class.getName());
    
    @FXML private TableView<OrdenItem> tablaVerPedido;
    @FXML private TableColumn<OrdenItem, String> colProducto;
    @FXML private TableColumn<OrdenItem, Integer> colCantidad;
    
    @FXML private Label labelIdOrden;

    @FXML private Button btnMesa1,  btnMesa2,  btnMesa3,  btnMesa4;
    @FXML private Button btnMesa5,  btnMesa6,  btnMesa7,  btnMesa8;
    @FXML private Button btnMesa9,  btnMesa10, btnMesa11, btnMesa12;

    @FXML private Button btnDisponibilidad;
    @FXML private Button btnRealizarPedido;
    @FXML private Button btnGestionar;
    @FXML private Button btnNuevoPlato;
    @FXML private Button btnCancelarPlato;
    @FXML private Button btnCerrarSesion;
    
    private int idOrdenActual = 0;
    private int mesaParaPedido = 0;
    private int idEmpleadoSesion;
    
    private static final String ESTILO_MESA_LIBRE =
        "-fx-background-color: #627096;" +
        "-fx-border-color: #627096;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";

    /** Ocupada pero sin orden registrada por el mesero aún. */
    private static final String ESTILO_MESA_SIN_ORDEN =
        "-fx-background-color: #6A500F;" +
        "-fx-border-color: #6A500F;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";

    /** Ocupada con orden abierta en BD. */
    private static final String ESTILO_MESA_CON_ORDEN =
        "-fx-background-color: #8a3636;" +
        "-fx-border-color: #8a3636;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";

    private static final String ESTILO_BTN_ACTIVO =
        "-fx-background-color: #2c3b62;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";

    private static final String ESTILO_BTN_INACTIVO =
        "-fx-background-color: #8b1a1a;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;" +
        "-fx-text-fill: #d4c5b0;";

    private final MesasDAO mesasDAO = new MesasDAO();
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnDisponibilidad.setStyle(ESTILO_BTN_ACTIVO);
        
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        
        vincularBotonesMesa();
        cargarEstadoMesas();
    }

    /**
     * Consulta la BD y pinta cada botón de mesa según su estado.
     *
     * Para las mesas en estado "Ocupada" se hace una consulta adicional
     * a la tabla ordenes para determinar si ya existe una orden abierta:
     * - Con orden abierta → ESTILO_MESA_CON_ORDEN (#8A3636 rojo)
     * - Sin orden abierta → ESTILO_MESA_SIN_ORDEN (#6A500F café)
     */
    private void cargarEstadoMesas() {
        Button[] botones = obtenerArregloMesas();
        List<Mesa> mesas = mesasDAO.obtenerTodasLasMesas();

        for (Mesa mesa : mesas) {
            int idx = mesa.getIdMesa() - 1;
            if (idx < 0 || idx >= botones.length) continue;

            switch (mesa.getEstado()) {
                case "Libre" -> botones[idx].setStyle(ESTILO_MESA_LIBRE);

                case "Ocupada" -> {
                    int idOrden = PedidoDAO.obtenerOrdenAbiertaPorMesa(mesa.getIdMesa());
                    if (idOrden > 0) {
                        botones[idx].setStyle(ESTILO_MESA_CON_ORDEN);
                    } else {
                        botones[idx].setStyle(ESTILO_MESA_SIN_ORDEN);
                    }
                }

                case "Reservada" -> botones[idx].setStyle(ESTILO_MESA_LIBRE);

                default -> botones[idx].setStyle(ESTILO_MESA_LIBRE);
            }
        }
    }

    /**
     * Asigna el evento clic a cada botón de mesa.
     */
    private void vincularBotonesMesa() {
        Button[] botones = obtenerArregloMesas();
        for (int i = 0; i < botones.length; i++) {
            final int numMesa = i + 1;
            botones[i].setOnAction(e -> handleClickMesa(numMesa));
        }
    }

    /**
     * Procesa el clic en una mesa según su estado actual.
     *
     * Libre → ofrece registrar pedido.
     * Ocupada → informa al mesero sobre el estado:
     * · Sin orden: puede tomar el pedido.
     * · Con orden: la orden ya fue registrada.
     *
     * @param numMesa Número de mesa clicada (1-12).
     */
    private void handleClickMesa(int numMesa) {
        this.mesaParaPedido = numMesa;
        
        Mesa mesa = mesasDAO.obtenerMesaPorId(numMesa);
        idOrdenActual = PedidoDAO.obtenerOrdenAbiertaPorMesa(numMesa);
        limpiarInterfazOrden();

        if (mesa == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Mesa no encontrada",
                "No se encontró la Mesa " + numMesa + " en el sistema.");
            return;
        }

        switch (mesa.getEstado()) {

            case "Libre" -> {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Mesa sin Asignar",
                        "La Mesa " + numMesa + " aún no ha sido asignada por el recepcionista\n" +
                        "El recepcionista es el único que puede asignar mesas.");
            }

            case "Ocupada" -> {
                int idOrden = PedidoDAO.obtenerOrdenAbiertaPorMesa(numMesa);

                if (idOrden > 0) {
                    mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Mesa con orden activa",
                        "La Mesa " + numMesa + " ya tiene una orden registrada (#" + idOrden + ").\n" +
                        "Si deceas añadir o cancelar algún plato ingresa modifica desde los botones.");
                } else {
                    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmacion.setTitle("Mesa ocupada sin orden");
                    confirmacion.setHeaderText("Mesa " + numMesa + " · Ocupada pero sin orden");
                    confirmacion.setContentText(
                        "El cliente ya fue asignado a esta mesa pero aún no se ha tomado el pedido.\n\n" +
                        "¿Desea registrar el pedido ahora?"
                    );

                    ButtonType btnIr = new ButtonType("Sí, tomar pedido");
                    ButtonType btnCancelar = new ButtonType("Cancelar",
                                                             javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                    confirmacion.getButtonTypes().setAll(btnIr, btnCancelar);

                    Optional<ButtonType> resultado = confirmacion.showAndWait();

                    if (resultado.isPresent() && resultado.get() == btnIr) {
                        mesaParaPedido = numMesa;
                        navegarARealizarPedido(numMesa);
                    }
                }
                
                labelIdOrden.setText("Orden #" + idOrden);
                ObservableList<OrdenItem> orden = PedidoDAO.obtenerDetalleOrden(idOrden);
                tablaVerPedido.setItems(orden);
            }

            case "Reservada" -> mostrarAlerta(Alert.AlertType.INFORMATION,
                "Mesa reservada",
                "Mesa " + numMesa + " está reservada.\n" +
                "El recepcionista debe confirmar la llegada del cliente antes de tomar el pedido.");

            default -> mostrarAlerta(Alert.AlertType.WARNING,
                "Estado desconocido",
                "La Mesa " + numMesa + " tiene un estado no reconocido: " + mesa.getEstado());
        }
    }
    
    /**
     * Valida la mesa seleccionada y abre el entorno modal para añadir un nuevo platillo 
     * a la comanda activa, refrescando la vista de pedidos al finalizar.
     * @param event Evento de acción del botón disparado por el usuario.
     */
    @FXML
    private void handleNuevoPlato(ActionEvent event) {
        LOG.info("funciona el actionEvent");
        // Valida si seleccionó una mesa previamente
        if (this.mesaParaPedido == 0) {
            mostrarAlerta(Alert.AlertType.WARNING, 
                "Seleccione una mesa", 
                "Por favor, haga clic sobre una mesa antes de intentar añadir un platillo.");
            return;
        }

        // Valida si la mesa tiene una orden activa para poder "añadir"
        int idOrden = PedidoDAO.obtenerOrdenAbiertaPorMesa(this.mesaParaPedido);
        if (idOrden == 0) {
            // Opcional: Si está ocupada sin orden o libre, redirigimos normalmente a crear una nueva orden
            LOG.info("Abriendo toma de pedido para una nueva orden en Mesa " + this.mesaParaPedido);
        } else {
            LOG.info("Añadiendo platillos a la Orden #" + idOrden + " de la Mesa " + this.mesaParaPedido);
        }

        navegarARealizarPedido(this.mesaParaPedido);

        if (idOrden > 0) {
            ObservableList<OrdenItem> ordenActualizada = PedidoDAO.obtenerDetalleOrden(idOrden);
            tablaVerPedido.setItems(ordenActualizada);
        }
    }
    
    /**
     * Valida la selección de mesa y platillo para proceder con la confirmación y 
     * disminución/cancelación de una unidad del artículo seleccionado en la BD.
     * @param event Evento de acción del botón disparado por el usuario.
     */
    @FXML
    private void handleCancelarPlato(ActionEvent event) {
        // 1. Valida si seleccionó una mesa previamente (Igual que en handleNuevoPlato)
        if (this.mesaParaPedido == 0) {
            mostrarAlerta(Alert.AlertType.WARNING, 
                "Seleccione una mesa", 
                "Por favor, haga clic sobre una mesa antes de intentar cancelar un platillo.");
            return;
        }

        // 2. Valida si la mesa tiene una orden activa
        int idOrden = PedidoDAO.obtenerOrdenAbiertaPorMesa(this.mesaParaPedido);
        if (idOrden == 0) {
            LOG.info(() -> "Intento de cancelar platillo en Mesa " + this.mesaParaPedido + " sin orden activa.");
            mostrarAlerta(Alert.AlertType.WARNING, 
                "Sin orden activa", 
                "La Mesa " + this.mesaParaPedido + " no tiene un pedido registrado del cual cancelar platillos.");
            return;
        }

        // 3. Valida si seleccionó un platillo de la tabla de la derecha
        OrdenItem seleccionado = tablaVerPedido.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, 
                "Platillo no seleccionado", 
                "Por favor, seleccione un platillo de la lista de la orden para cancelar.");
            return;
        }

        // 4. Confirmación de cancelación
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Confirmar Cancelación");
        confirmar.setHeaderText("Cancelar 1x " + seleccionado.getProducto());
        confirmar.setContentText("¿Estás seguro de que deseas restar una unidad de este platillo de la orden?");

        Optional<ButtonType> respuesta = confirmar.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            
            LOG.info(() -> "Cancelando 1 unidad de " + seleccionado.getProducto() + " de la Orden #" + idOrden);
            
            // 5. Llamada al DAO para hacer el UPDATE/DELETE en la base de datos
            boolean exito = PedidoDAO.cancelarPlatillo(idOrden, seleccionado.getProducto());

            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Cancelación Exitosa", "Se canceló una unidad del platillo correctamente.");
                
                // 6. Refrescar la tabla con la orden actualizada
                ObservableList<OrdenItem> ordenActualizada = PedidoDAO.obtenerDetalleOrden(idOrden);
                tablaVerPedido.setItems(ordenActualizada);
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error en BD", "No se pudo cancelar el platillo. Verifique si aún existe en la orden.");
            }
        } else {
            LOG.info("El mesero abortó la cancelación del platillo.");
        }
    }

    @FXML
    private void handleDisponibilidad(ActionEvent event) {
        btnDisponibilidad.setStyle(ESTILO_BTN_ACTIVO);
        btnRealizarPedido.setStyle(ESTILO_BTN_INACTIVO);
        btnGestionar.setStyle(ESTILO_BTN_INACTIVO);
        cargarEstadoMesas();
    }

    /**
     * Redirige al mesero hacia el módulo general de toma y registro de pedidos.
     * @param event Evento de navegación lanzado por el menú lateral.
     */
    @FXML
    private void handleRealizarPedido(ActionEvent event) {
        cambiarPantalla("/com/mycompany/restaurante/fxml/RegistrarPedidoPantalla.fxml",
                 "Realizar Pedido - Saveurs Paris", event);
    }

    /**
     * Redirige al mesero hacia el panel de visualización, tracking y edición de comandas existentes.
     * @param event Evento de navegación lanzado por el menú lateral.
     */
    @FXML
    private void handleGestionar(ActionEvent event) {
        cambiarPantalla("/com/mycompany/restaurante/fxml/GestionarPedidoPantalla.fxml",
                 "Gestionar Pedido - Saveurs Paris", event);
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource(
                    "/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            alerta.close();
        }
    }

    /**
     * Limpia los campos de texto cuando no hay una orden activa.
     */
    private void limpiarInterfazOrden() {
        tablaVerPedido.getItems().clear();
        labelIdOrden.setText("Sin orden activa");
        idOrdenActual = 0;
    }
    
    /**
     * Abre RegistrarPedidoPantalla.fxml de forma modal pasando el número de mesa.
     *
     * @param numMesa Mesa seleccionada para el pedido.
     */
    private void navegarARealizarPedido(int numMesa) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/mycompany/restaurante/fxml/RegistrarPedidoPantalla.fxml"
            ));
            Parent root = loader.load();

            RegistrarPedidoController ctrl = loader.getController();
            ctrl.setMesaSeleccionada(numMesa);

            Stage nuevoStage = new Stage();
            nuevoStage.setScene(new Scene(root));
            nuevoStage.setTitle("Realizar Pedido · Mesa " + numMesa + " - Saveurs Paris");
            nuevoStage.initModality(Modality.APPLICATION_MODAL);
            nuevoStage.initOwner(btnMesa1.getScene().getWindow());
            nuevoStage.setResizable(false);
            nuevoStage.showAndWait();

            cargarEstadoMesas();
        } catch (IOException e) {
            LOG.severe("[MesasDisponiblesController] No se pudo cargar RegistrarPedidoPantalla: "
                       + e.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error de navegación",
                "No se pudo abrir la pantalla de pedido.\n" +
                "Verifique que RegistrarPedidoPantalla.fxml exista en la ruta correcta.");
        }
    }

    /**
     * Agrupa los 12 botones de mesa en orden para iterarlos fácilmente.
     */
    private Button[] obtenerArregloMesas() {
        return new Button[] {
            btnMesa1, btnMesa2, btnMesa3,  btnMesa4,
            btnMesa5, btnMesa6, btnMesa7,  btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
    }

    /**
     * Método genérico para cambiar de pantalla dentro del mismo Stage.
     */
    private void cambiarPantalla(String rutaFxml, String titulo, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(rutaFxml));
            Stage stageActual = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stageActual.setScene(new Scene(root));
            stageActual.setTitle(titulo);
            stageActual.show();
        } catch (IOException e) {
            LOG.severe("[MesasDisponiblesController] No se pudo cargar: " + rutaFxml);
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error de navegación",
                "No se pudo cargar la pantalla ZIP solicitada.");
        }
    }

    /**
     * Inyecta el ID único del empleado que ha iniciado sesión para asociarlo a las operaciones.
     * @param id Identificador numérico del empleado en sesión.
     */
    public void setIdEmpleadoSesion(int id) {
        this.idEmpleadoSesion = id;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(contenido);
        a.showAndWait();
    }
}