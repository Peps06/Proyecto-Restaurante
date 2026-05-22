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

/**
 *
 * @author Dana
 */

public class RegistrarReservaController {

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
    
    private final MesasDAO mesasDAO = new MesasDAO();
    private List<Integer> mesasSeleccionadas = new ArrayList<>();
    
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

    @FXML
    public void initialize() {
        vincularBotonesMesa();
        cargarEstadoMesas();
    }

    private void vincularBotonesMesa() {
        Button[] botones = obtenerArregloMesas();
        for (int i = 0; i < botones.length; i++) {
            final int numMesa = i + 1;
            botones[i].setOnAction(e -> handleClickMesa(numMesa));
        }
    }

    /**
     * Consulta la base de datos y aplica el estilo visual correspondiente 
     * a cada botón de mesa según su estado actual.
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

    private Button[] obtenerArregloMesas() {
        return new Button[] {
            btnMesa1, btnMesa2, btnMesa3,  btnMesa4,
            btnMesa5, btnMesa6, btnMesa7,  btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
    }

    // Guardar la referencia de la reserva original si estamos editando
    private Reservacion reservaOriginal = null;
    
    private Reservacion reservaResultado = null;

    /**
     * Se llama desde la ventana principal cuando le damos a "Editar".
     * Precarga los datos en los campos de texto.
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
     * Devuelve la reserva al controlador principal (null si el usuario canceló)
     */
    public Reservacion getReserva() {
        return reservaResultado;
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
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
                    "El campo 'Contraseña' no puede estar vacio.");
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
            
            // 3. Crear el objeto con la información (preservando el ID, estado y mesa si editamos)
            int id = (reservaOriginal != null) ? reservaOriginal.getId() : 0;
            int idMesa = mesasSeleccionadas.get(0); 
            String estado = (reservaOriginal != null) ? reservaOriginal.getEstado() : "Pendiente";

            if (!com.mycompany.restaurante.DAO.ReservacionDAO.validarDisponibilidad(idMesa, fecha, hora, id)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Mesa no disponible", 
                    "No se puede reservar la mesa " + idMesa + " a las " + hora + ".\n" +
                    "Existe otra reserva activa con menos de 3 horas de diferencia.");
                return; // Cortamos la ejecución, no se guarda ni se cierra
            }

            reservaResultado = new Reservacion(id, nombre, telefono, fecha, hora, numPersonas, idMesa, estado);

            // 4. Cerrar
            cerrarVentana();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de formato", "El número de personas debe ser un número entero.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        reservaResultado = null; // Devolvemos nulo indicando que se canceló
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    private void marcarError(TextField campo) {
        campo.setStyle(ESTILO_ERROR);
        campo.requestFocus();
    }
}