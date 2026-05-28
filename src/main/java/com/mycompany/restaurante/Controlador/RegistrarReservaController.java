package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.Modelo.Reservacion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.mycompany.restaurante.DAO.MesasDAO;
import com.mycompany.restaurante.DAO.ReservacionDAO;
import com.mycompany.restaurante.Modelo.Mesa;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import javafx.scene.control.DateCell;

/**
 * Controlador para la creación y edición de reservaciones en el sistema Saveurs Paris.
 * Permite capturar la información del cliente, validar la disponibilidad de los horarios
 * con un margen de seguridad de 3 horas e interactuar con la cuadrícula física de mesas.
 * * @author Dana
 * @version 1.0
 */
public class RegistrarReservaController {

    // CONTROLES DE LA INTERFAZ FXML
    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private DatePicker txtFecha;
    @FXML private TextField txtHora;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtNoPersonas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    
    // BOTONES DE MESAS (1 AL 12)
    @FXML private Button btnMesa1, btnMesa2, btnMesa3, btnMesa4;
    @FXML private Button btnMesa5, btnMesa6, btnMesa7, btnMesa8;
    @FXML private Button btnMesa9, btnMesa10, btnMesa11, btnMesa12;
    
    // ESTADO INTERNO Y ACCESO A DATOS
    private final MesasDAO mesasDAO = new MesasDAO();
    private List<Integer> mesasSeleccionadas = new ArrayList<>();
    private Reservacion reservaOriginal = null;
    private Reservacion reservaResultado = null;
    
    // ESTILOS CSS BASADOS EN LA INTERFAZ
    private static final String ESTILO_MESA_LIBRE = 
        "-fx-background-color: #627096; -fx-border-color: #627096; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;";
    
    private static final String ESTILO_MESA_OCUPADA =
        "-fx-background-color: #8a3636; -fx-border-color: #8a3636; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;" +
        "-fx-text-fill: #0a132b;";
        
    private static final String ESTILO_MESA_RESERVADA = 
        "-fx-background-color: #8a3636; -fx-border-color: #8a3636; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;";
        
    private static final String ESTILO_MESA_SELECCIONADA = 
        "-fx-background-color: #C9A84C; -fx-border-color: #C9A84C; -fx-text-fill: #0a132b;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;";
    
    private static final String ESTILO_ERROR  =
        "-fx-background-color: #FFF0F0; -fx-border-color: #cc0000; -fx-border-width: 2; -fx-border-radius: 5 5 5 5;";

    /**
     * Inicializa el controlador vinculando los eventos de la cuadrícula y cargando sus colores básicos.
     */
    @FXML
    public void initialize() {
        vincularBotonesMesa();
        cargarEstadoMesas();
        // Bloquear fechas pasadas en el DatePicker
        txtFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate hoy = LocalDate.now();
                
                // Si la fecha evaluada es antes de hoy, la deshabilitamos
                if (date.isBefore(hoy)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3; -fx-text-fill: #a9a9a9;"); // Se pone gris
                }
            }
        });
    }

    /**
     * Enlaza dinámicamente la acción de clic a cada uno de los 12 botones de la cuadrícula.
     */
    private void vincularBotonesMesa() {
        Button[] botones = obtenerArregloMesas();
        for (int i = 0; i < botones.length; i++) {
            final int numMesa = i + 1;
            botones[i].setOnAction(e -> handleClickMesa(numMesa));
        }
    }

    /**
     * Sincroniza visualmente los botones de la interfaz con los estados reales ("Libre", "Ocupada", "Reservada") de la BD.
     */
    private void cargarEstadoMesas() {
        Button[] botones = obtenerArregloMesas();
        List<Mesa> mesas = mesasDAO.obtenerTodasLasMesas();

        if (reservaOriginal != null && mesasSeleccionadas.isEmpty()) {
            mesasSeleccionadas.add(reservaOriginal.getIdMesa());
        }

        for (Mesa mesa : mesas) {
            int idx = mesa.getIdMesa() - 1; 
            if (idx < 0 || idx >= botones.length) continue;

            if (mesasSeleccionadas.contains(mesa.getIdMesa())) {
                botones[idx].setStyle(ESTILO_MESA_SELECCIONADA);
                continue;
            }

            // Pintar según estado en la BD
            switch (mesa.getEstado()) {
                case "Libre" -> botones[idx].setStyle(ESTILO_MESA_LIBRE);
                case "Ocupada" -> botones[idx].setStyle(ESTILO_MESA_OCUPADA);
                case "Reservada" -> botones[idx].setStyle(ESTILO_MESA_RESERVADA);
                default -> botones[idx].setStyle(ESTILO_MESA_LIBRE);
            }
        }
    }

    /**
     * Procesa la selección o deselección de una mesa evaluando previamente las reglas horarias de la BD.
     * @param numMesa Número correlativo de la mesa presionada (1-12).
     */
    private void handleClickMesa(int numMesa) {
        Mesa mesa = mesasDAO.obtenerMesaPorId(numMesa);

        if (mesa == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error", "No se encontró la Mesa " + numMesa);
            return;
        }

        // 1. Verificar si la mesa ya está seleccionada (para deseleccionarla)
        if (mesasSeleccionadas.contains(numMesa)) {
            mesasSeleccionadas.remove(Integer.valueOf(numMesa));
            cargarEstadoMesas();
            return; // Si se deselecciona, no necesitamos validar nada más
        }
        
        // 2. Si se quiere SELECCIONAR, primero obligamos a ingresar fecha y hora
        if (txtFecha.getValue() == null || txtHora.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos", 
                "Por favor, ingrese Fecha y Hora antes de seleccionar una mesa para verificar disponibilidad.");
            return;
        }
        
        LocalDate fecha = txtFecha.getValue();
        String hora = txtHora.getText().trim();
        int idReservaActual = (reservaOriginal != null) ? reservaOriginal.getId() : 0;
        
        // 3. Validar disponibilidad con la regla de 3 horas
        boolean estaLibre = ReservacionDAO.validarDisponibilidad(numMesa, fecha, hora, idReservaActual);
        
        if (!estaLibre) {
             mostrarAlerta(Alert.AlertType.INFORMATION, 
                "Mesa no disponible", 
                "La Mesa " + numMesa + " ya se encuentra Reservada en el horario especificado o en un rango menor a 3 horas. Por favor elija otra o cambie el horario.");
            return;
        }
        
        mesasSeleccionadas.clear(); 
        mesasSeleccionadas.add(numMesa); 
        cargarEstadoMesas();
    }

    /**
     * Agrupa los botones inyectados en una estructura de arreglo indexada para facilitar su recorrido.
     * @return Arreglo estructurado de botones contenedores de las mesas.
     */
    private Button[] obtenerArregloMesas() {
        return new Button[] {
            btnMesa1, btnMesa2, btnMesa3,  btnMesa4,
            btnMesa5, btnMesa6, btnMesa7,  btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
    }

    /**
     * Precarga la información de una reserva existente en el formulario para habilitar el modo edición.
     * @param reserva Objeto Reservacion con los datos vigentes a modificar.
     */
    public void cargarDatos(Reservacion reserva) {
        this.reservaOriginal = reserva;
        
        lblTitulo.setText("Editar reserva"); // Cambiamos el título
        
        txtNombre.setText(reserva.getNombreCliente());
        txtFecha.setValue(reserva.getFecha());
        txtHora.setText(reserva.getHora().substring(0, 5)); 
        txtTelefono.setText(reserva.getTelefono());
        txtNoPersonas.setText(String.valueOf(reserva.getNumeroPersonas()));
        mesasSeleccionadas.clear();
        mesasSeleccionadas.add(reserva.getIdMesa());
        cargarEstadoMesas();
    }

    /**
     * Proporciona la entidad construida o modificada al flujo o controlador que invocó la ventana modal.
     * @return El objeto Reservacion procesado, o {@code null} si la operación fue cancelada.
     */
    public Reservacion getReserva() {
        return reservaResultado;
    }

    /**
     * Valida de manera exhaustiva las restricciones de los campos de texto, formatos y confirmación final de guardado.
     * @param event Evento de disparo vinculado al botón de confirmación.
     */
    @FXML
    private void handleGuardar(ActionEvent event) {
        // Limpiamos los estilos de error previos
        txtNombre.setStyle("");
        txtNoPersonas.setStyle("");
        txtTelefono.setStyle("");

        // 1. Validar que no haya campos vacíos
        if (txtNombre.getText().trim().isEmpty()){
            marcarError(txtNombre);
            mostrarAlerta(Alert.AlertType.ERROR,"Todos los campos son obligatorios",
                    "El campo 'Nombre' no puede estar vacio.");
            return;
        }
        
        if (txtNoPersonas.getText().trim().isEmpty()){
            marcarError(txtNoPersonas);
            mostrarAlerta(Alert.AlertType.ERROR,"Todos los campos son obligatorios",
                    "El campo 'Número de personas' no puede estar vacio.");
            return;
        }
        
        if (txtTelefono.getText().trim().isEmpty()){
            marcarError(txtTelefono);
            mostrarAlerta(Alert.AlertType.ERROR,"Todos los campos son obligatorios",
                    "El campo 'Teléfono' no puede estar vacio.");
            return;
        }
        
        String tel = txtTelefono.getText().trim();
        
        if (!tel.matches("\\d{10}")) {
            marcarError(txtTelefono);
            mostrarAlerta(Alert.AlertType.ERROR,"Error en teléfono", "El teléfono debe contener exactamente 10 dígitos numéricos.");
            return;
        }
        
        if (mesasSeleccionadas.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin mesa", "Por favor, selecciona una mesa de la cuadrícula.");
            return;
        }

        try {
            // 2. Extraer datos
            String nombre = txtNombre.getText().trim();
            LocalDate fecha = txtFecha.getValue();
            String hora = txtHora.getText().trim();
            String telefono = txtTelefono.getText().trim();
            int numPersonas = Integer.parseInt(txtNoPersonas.getText().trim());

            if (numPersonas <= 0) {
                 marcarError(txtNoPersonas);
                 mostrarAlerta(Alert.AlertType.WARNING, "Número inválido", "El número de personas debe ser mayor a 0.");
                 return;
            }
            
            int id = (reservaOriginal != null) ? reservaOriginal.getId() : 0;
            int idMesa = mesasSeleccionadas.get(0); 
            String estado = (reservaOriginal != null) ? reservaOriginal.getEstado() : "Pendiente";

            // 3. Validar capacidad de la mesa
            Mesa mesa = mesasDAO.obtenerMesaPorId(idMesa);
            if (mesa != null) {
                if (numPersonas > mesa.getCapacidad()) {
                    marcarError(txtNoPersonas);
                    mostrarAlerta(Alert.AlertType.ERROR, "Capacidad excedida", 
                        "La Mesa " + idMesa + " tiene capacidad máxima para " + mesa.getCapacidad() + " personas.\n" +
                        "No puedes registrar una reserva de " + numPersonas + " personas allí.");
                    return; // Detenemos la ejecución
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se encontró la información de la mesa seleccionada.");
                return;
            }

            // 4. Validar disponibilidad de horario (3 horas)
            if (!com.mycompany.restaurante.DAO.ReservacionDAO.validarDisponibilidad(idMesa, fecha, hora, id)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Mesa no disponible", 
                    "No se puede reservar la mesa " + idMesa + " a las " + hora + ".\n" +
                    "Existe otra reserva activa con menos de 3 horas de diferencia.");
                return;
            }

            // 5. Crear objeto final y cerrar
            reservaResultado = new Reservacion(id, nombre, telefono, fecha, hora, numPersonas, idMesa, estado);
            cerrarVentana();

        } catch (NumberFormatException e) {
            marcarError(txtNoPersonas);
            mostrarAlerta(Alert.AlertType.ERROR, "Error de formato", "El número de personas debe ser un número entero.");
        }
    }

    /**
     * Anula el proceso actual de recolección y cierra el entorno modal de manera segura.
     * @param event Evento de disparo vinculado al botón Cancelar.
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        reservaResultado = null; // Devolvemos nulo indicando que se canceló
        cerrarVentana();
    }

    /**
     * Recupera el contenedor principal (Stage) del entorno gráfico actual para finalizar su ejecución.
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    /**
     * Lanza ventanas de diálogo modales estandarizadas en la pantalla para alertar o informar al operador.
     * @param tipo El tipo de categorización o icono del aviso ({@code ERROR}, {@code WARNING}, etc.).
     * @param titulo Texto para la barra superior del recuadro.
     * @param mensaje Cuerpo descriptivo de la advertencia o error.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    /**
     * Aplica el borde de color rojizo y asigna el foco operativo al control de texto que falló en sus reglas de validación.
     * @param campo El componente gráfico del tipo TextField afectado.
     */
    private void marcarError(TextField campo) {
        campo.setStyle(ESTILO_ERROR);
        campo.requestFocus();
    }
}