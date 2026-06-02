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
 * Rono   (#8A3636) → Ocupada sin orden: el recepcionista asignó al cliente
 *                     pero el mesero aún no ha registrado el pedido.
 * Verde   (#407A48) → Ocupada con orden: ya existe una orden abierta en BD.
 * Dorado  (#C9A84C) → Cobrada: la cuenta fue saldada pero los comensales
 *                     aún no se retiraron; el mesero puede liberarla.
 * 
 * @author Citlaly
 * @version 2.1
 */
public class MesasDisponiblesController implements Initializable {

    private static final Logger LOG =
            Logger.getLogger(MesasDisponiblesController.class.getName());
    
    @FXML private TableView<OrdenItem> tablaVerPedido;
    @FXML private TableColumn<OrdenItem, String> colProducto;
    @FXML private TableColumn<OrdenItem, Integer> colCantidad;
    
    @FXML private Label labelIdOrden;
    
    @FXML private Label txtMesa1, txtMesa2, txtMesa3, txtMesa4;
    @FXML private Label txtMesa5, txtMesa6, txtMesa7, txtMesa8;
    @FXML private Label txtMesa9, txtMesa10, txtMesa11, txtMesa12;

    @FXML private Button btnMesa1, btnMesa2, btnMesa3, btnMesa4;
    @FXML private Button btnMesa5, btnMesa6, btnMesa7, btnMesa8;
    @FXML private Button btnMesa9, btnMesa10, btnMesa11, btnMesa12;

    @FXML private Button btnDisponibilidad;
    @FXML private Button btnRealizarPedido;
    @FXML private Button btnGestionar;
    @FXML private Button btnNuevoPlato;
    @FXML private Button btnCancelarPlato;
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnMarcarMesaLibre;
    
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
        "-fx-background-color: #8A3636;" +
        "-fx-border-color: #8A3636" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";

    /** Ocupada con orden abierta en BD. */
    private static final String ESTILO_MESA_CON_ORDEN =
        "-fx-background-color: #407A48;" +
        "-fx-border-color: #407A48;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    
    private static final String ESTILO_MESA_COBRADA =
    "-fx-background-color: #C9A84C;"
    + "-fx-border-color: #C9A84C;"
    + "-fx-text-fill: #1a1e2e;"
    + "-fx-background-radius: 10 10 10 10;"
    + "-fx-border-radius: 10 10 10 10;";

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
    
    /**
     * Inicializa los componentes al cargar la vista.
     * activa el estilo del botón de disponibilidad, vincula las columnas de la
     * tabla y carga el estado actual de todas las mesas desde la base de datos.
     * 
     * @param url
     * @param rb
     */
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
     * - Con orden abierta → ESTILO_MESA_CON_ORDEN
     * - Sin orden abierta → ESTILO_MESA_SIN_ORDEN
     */
    private void cargarEstadoMesas() {
        Button[] botones = obtenerArregloMesas();
        List<Mesa> mesas = mesasDAO.obtenerTodasLasMesas();

        for (Mesa mesa : mesas) {
            int idx = mesa.getIdMesa() - 1;
            if (idx < 0 || idx >= botones.length) continue;

            setTextoMesa(botones[idx], mesa.getIdMesa(), mesa.getCapacidad());

            switch (mesa.getEstado()) {
                case "Libre" ->
                    botones[idx].setStyle(ESTILO_MESA_LIBRE);

                case "Ocupada" -> {
                    int idOrden = PedidoDAO.obtenerOrdenAbiertaPorMesa(mesa.getIdMesa());
                    botones[idx].setStyle(idOrden > 0
                        ? ESTILO_MESA_CON_ORDEN
                        : ESTILO_MESA_SIN_ORDEN);
                }

                case "Cobrada" ->
                    botones[idx].setStyle(ESTILO_MESA_COBRADA);

                case "Reservada" ->
                    botones[idx].setStyle(ESTILO_MESA_LIBRE);

                default ->
                    botones[idx].setStyle(ESTILO_MESA_LIBRE);
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
            botones[i].setOnAction(e -> handleClickMesa(numMesa, botones));
        }
    }

    /**
     * Procesa el clic en una mesa según su estado actual.
     *
     * Libre → ofrece registrar pedido.
     * Ocupada → informa al mesero sobre el estado:
     * · Sin orden: puede tomar el pedido.
     * · Con orden: la orden ya fue registrada.
     * Cobrada → informa que la cuenta ya fue saldada y puede liberarse
     *
     * @param numMesa Número de mesa clicada (1-12).
     */
    private void handleClickMesa(int numMesa, Button[] mesas) {
        this.mesaParaPedido = numMesa;
        
        Mesa mesa = mesasDAO.obtenerMesaPorId(numMesa);
        idOrdenActual = PedidoDAO.obtenerOrdenAbiertaPorMesa(numMesa);
        limpiarInterfazOrden();
        cargarEstadoMesas();
        Button botonActual = mesas[numMesa - 1];
        botonActual.setStyle(botonActual.getStyle() + ESTILO_BTN_ACTIVO);

        if (mesa == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Mesa no encontrada",
                "No se encontró la Mesa " + numMesa + " en el sistema.");
            return;
        }

        switch (mesa.getEstado()) {

            case "Libre" -> {}

            case "Ocupada" -> {
                int idOrden = PedidoDAO.obtenerOrdenAbiertaPorMesa(numMesa);

                if (idOrden > 0) {
                } else {
                    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmacion.setTitle("Mesa ocupada sin orden");
                    confirmacion.setHeaderText("Mesa " + numMesa + " · Ocupada pero sin orden");
                    confirmacion.setContentText(
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
        
            case "Cobrada" ->
                mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Mesa cobrada",
                    "La Mesa " + numMesa + " ya fue cobrada o facturada.\n"
                    + "Selecciónala y usa el botón 'Marcar como libre' "
                    + "una vez que los clientes se hayan retirado.");


            case "Reservada" -> {}

            default -> mostrarAlerta(Alert.AlertType.WARNING,
                "Estado desconocido",
                "La Mesa " + numMesa + " tiene un estado no reconocido: " + mesa.getEstado());
        }
    }
    
    /**
     * Abre la pantalla de toma de pedido para añadir un nuevo plato a la mesa
     * actualmente seleccionada.
     * Valida que haya una mesa elegida antes de proceder.
     *
     * @param event Evento de acción del botón disparado por el usuario.
     */
    @FXML
    private void handleNuevoPlato(ActionEvent event) {
        LOG.info("handleNuevoPlato activado.");
 
        if (this.mesaParaPedido == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Seleccione una mesa",
                "Por favor, haga clic sobre una mesa antes de "
                + "intentar añadir un platillo.");
            return;
        }
 
        int idOrden = PedidoDAO.obtenerOrdenAbiertaPorMesa(this.mesaParaPedido);
        
        if (idOrden > 0) {
        String estado = PedidoDAO.obtenerEstadoOrden(idOrden); // método faltante
        if (!"Abierta".equals(estado)) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "No disponible",
                "Esta mesa ya fue cobrada. No se pueden añadir más platillos.");
            return;
        }
    }
        
        if (idOrden == 0) {
            LOG.info("Abriendo toma de pedido para nueva orden en Mesa "
                   + this.mesaParaPedido);
        } else {
            LOG.info("Añadiendo platillos a la Orden #" + idOrden
                   + " de la Mesa " + this.mesaParaPedido);
        }
        
        
 
        navegarARealizarPedido(this.mesaParaPedido);
 
        // Refrescamos la tabla si ya había una orden activa
        if (idOrden > 0) {
            ObservableList<OrdenItem> ordenActualizada =
                    PedidoDAO.obtenerDetalleOrden(idOrden);
            tablaVerPedido.setItems(ordenActualizada);
        }
    }
    
    /**
     * Valida la selección de mesa y platillo para proceder con la confirmación y 
     * disminución/cancelación de una unidad del artículo seleccionado en la BD.
     * 
     * @param event Evento de acción del botón disparado por el usuario.
     */
    @FXML
    private void handleCancelarPlato(ActionEvent event) {
        // 1. Valida si seleccionó una mesa previamente (Igual que en handleNuevoPlato)
        if (this.mesaParaPedido == 0) {
            mostrarAlerta(Alert.AlertType.WARNING, 
                "Seleccione una mesa", 
                "Por favor, haga clic sobre una mesa antes de "
                + "intentar cancelar un platillo.");
            return;
        }

        // 2. Valida si la mesa tiene una orden activa
        int idOrden = PedidoDAO.obtenerOrdenAbiertaPorMesa(this.mesaParaPedido);
        if (idOrden == 0) {
            LOG.info(() -> "Intento de cancelar platillo en Mesa "
                    + this.mesaParaPedido + " sin orden activa.");
            mostrarAlerta(Alert.AlertType.WARNING, 
                "Sin orden activa", 
                "La Mesa " +
                this.mesaParaPedido +
                " no tiene un pedido registrado del cual cancelar platillos.");
            return;
        }

        // 3. Valida si seleccionó un platillo de la tabla de la derecha
        OrdenItem seleccionado = tablaVerPedido.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, 
                "Platillo no seleccionado", 
                "Por favor, seleccione un platillo de la lista "
                + "de la orden para cancelar.");
            return;
        }

        // 4. Confirmación de cancelación
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Confirmar Cancelación");
        confirmar.setHeaderText("Cancelar 1x " + seleccionado.getProducto());
        confirmar.setContentText(
            "¿Estás seguro de que deseas restar una unidad de este "
            + "platillo de la orden?");

        Optional<ButtonType> respuesta = confirmar.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            
            LOG.info(() -> "Cancelando 1 unidad de " +
                    seleccionado.getProducto() +
                    " de la Orden #" +
                    idOrden);
            
            // 5. Llamada al DAO para hacer el UPDATE/DELETE en la base de datos
            boolean exito = PedidoDAO.cancelarPlatillo(idOrden,
                    seleccionado.getProducto());

            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Cancelación Exitosa",
                    "Se canceló una unidad del platillo correctamente.");
                
                // 6. Refrescar la tabla con la orden actualizada
                ObservableList<OrdenItem> ordenActualizada =
                        PedidoDAO.obtenerDetalleOrden(idOrden);
                tablaVerPedido.setItems(ordenActualizada);
            } else {
                mostrarAlerta(Alert.AlertType.ERROR,
                    "Error en BD",
                    "No se pudo cancelar el platillo. "
                    + "Verifique si aún existe en la orden.");
            }
        } else {
            LOG.info("El mesero abortó la cancelación del platillo.");
        }
    }
    
    /**
     * Libera físicamente una mesa cuyo estado es 'Cobrada', cambiándola a 'Libre'.
     * Esto indica que los comensales se retiraron y la mesa está disponible para
     * nuevos clientes.
     *
     * Condiciones para que la acción proceda:
     * - Debe haber una mesa seleccionada (mesaParaPedido != 0).
     * - El estado actual de la mesa en BD debe ser 'Cobrada'.
     *
     * Siempre pide confirmación antes de ejecutar el cambio.
     *
     * @param event Evento de acción del botón {@code btnMarcarMesaLibre}.
     */
    @FXML
    private void handleMarcarLibre(ActionEvent event) {
        // 1. Validar que haya una mesa seleccionada
        if (mesaParaPedido == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Sin mesa seleccionada",
                "Por favor, haga clic sobre una mesa antes de "
                + "intentar liberarla.");
            return;
        }
 
        // 2. Verificar que la mesa esté en estado 'Cobrada' 
        Mesa mesa = mesasDAO.obtenerMesaPorId(mesaParaPedido);
        if (mesa == null) {
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error",
                "No se encontró la Mesa " + mesaParaPedido + " en el sistema.");
            return;
        }
 
        if (!"Cobrada".equals(mesa.getEstado())) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Acción no permitida",
                "La Mesa " + mesaParaPedido
                + " no puede liberarse porque su estado actual es '"
                + mesa.getEstado() + "'.\n\n"
                + "Solo se pueden liberar mesas en estado 'Cobrada'.");
            return;
        }
 
        // 3. Pedir confirmación antes de liberar 
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar liberación de mesa");
        confirmacion.setHeaderText("Liberar Mesa " + mesaParaPedido);
        confirmacion.setContentText(
            "¿Confirmas que los comensales ya se retiraron y "
            + "la Mesa " + mesaParaPedido + " está lista para nuevos clientes?");
 
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
 
            boolean exito = mesasDAO.actualizarEstadoMesa(mesaParaPedido, "Libre");
 
            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Mesa liberada",
                    "La Mesa " + mesaParaPedido
                    + " ahora está disponible para nuevos clientes.");
 
                // Limpiar selección y refrescar el mapa de mesas
                limpiarInterfazOrden();
                mesaParaPedido = 0;
                cargarEstadoMesas();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al liberar mesa",
                    "No se pudo actualizar el estado de la mesa en la base de datos.\n"
                    + "Intente nuevamente.");
            }
        }
    }

    /**
     * Actualiza los estilos del menú y recarga el estado del mapa de mesas.
     *
     * @param event Evento de acción del botón de disponibilidad.
     */
    @FXML
    private void handleDisponibilidad(ActionEvent event) {
        btnDisponibilidad.setStyle(ESTILO_BTN_ACTIVO);
        btnRealizarPedido.setStyle(ESTILO_BTN_INACTIVO);
        btnGestionar.setStyle(ESTILO_BTN_INACTIVO);
        cargarEstadoMesas();
    }

    /**
     * Redirige al mesero hacia el módulo general de toma y registro de pedidos.
     * 
     * @param event Evento de navegación lanzado por el menú lateral.
     */
    @FXML
    private void handleRealizarPedido(ActionEvent event) {
        cambiarPantalla("/com/mycompany/restaurante/fxml/RegistrarPedidoPantalla.fxml",
                 "Realizar Pedido - Saveurs Paris", event);
    }

    /**
     * Redirige al mesero hacia el panel de visualización, tracking y edición
     * de comandas existentes.
     * 
     * @param event Evento de navegación lanzado por el menú lateral.
     */
    @FXML
    private void handleGestionar(ActionEvent event) {
        cambiarPantalla("/com/mycompany/restaurante/fxml/GestionarPedidoPantalla.fxml",
                 "Gestionar Pedido - Saveurs Paris", event);
    }

    /**
     * Valida mediante confirmación el cierre de sesión y redirige al Login.
     *
     * @param event Evento lanzado por el botón de cerrar sesión.
     */
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
     * Agrupa los 12 botones de mesa en un arreglo indexado para iterarlos.
     *
     * @return Arreglo de botones en orden de idMesa (1-12).
     */
    private Button[] obtenerArregloMesas() {
        return new Button[] {
            btnMesa1, btnMesa2, btnMesa3, btnMesa4,
            btnMesa5, btnMesa6, btnMesa7, btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
    }
    
    /**
     * Agrupa los 12 labels de las mesas en un arreglo indexado para iterarlos.
     * 
     * @return Arreglo de label en orden de idMesa (1-12)
     */
   
    /**
     * Método genérico para cambiar de pantalla dentro del mismo Stage.
     *
     * @param rutaFxml Ruta del archivo FXML de destino.
     * @param titulo Título para la barra del Stage.
     * @param event Evento origen para obtener el Stage actual.
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
     * Inyecta el ID del empleado que inició sesión para asociarlo a las operaciones.
     * 
     * @param id Identificador numérico del empleado en sesión.
     */
    public void setIdEmpleadoSesion(int id) {
        this.idEmpleadoSesion = id;
    }
    
    /**
    * Asigna al botón un gráfico con dos líneas:
    * número de mesa (grande) y capacidad (pequeña).
    */
   private void setTextoMesa(Button btn, int numMesa, int capacidad) {
       javafx.scene.control.Label lblNumero =
               new javafx.scene.control.Label(String.valueOf(numMesa));
       lblNumero.setStyle("-fx-font-size: 18px;" + 
                          "-fx-font-weight: bold;" + 
                          "-fx-text-fill: #d4c5b0;");

       javafx.scene.control.Label lblCapacidad =
               new javafx.scene.control.Label(capacidad + " 👥");
       lblCapacidad.setStyle("-fx-font-size: 10px;" +
                             "-fx-text-fill: #d4c5b0;");

       javafx.scene.layout.VBox vbox =
               new javafx.scene.layout.VBox(2, lblNumero, lblCapacidad);
       vbox.setAlignment(javafx.geometry.Pos.CENTER);

       btn.setText(""); // limpia el texto nativo del botón
       btn.setGraphic(vbox);
   }

    /**
     * Despliega un cuadro de diálogo modal estándar para alertar al usuario.
     *
     * @param tipo Tipo de alerta ({@code INFORMATION}, {@code WARNING}, etc.).
     * @param titulo Texto de la barra del diálogo.
     * @param contenido Mensaje principal visible para el usuario.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(contenido);
        a.showAndWait();
    }
}