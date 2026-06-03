package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.ListaEsperaDAO;
import com.mycompany.restaurante.DAO.MesasDAO;
import com.mycompany.restaurante.Modelo.ClienteEspera;
import com.mycompany.restaurante.Modelo.Mesa;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador para la asignación de mesas físicas dentro del sistema Saveurs Paris.
 * Administra el flujo de ocupación tanto para clientes que provienen de la lista de espera
 * como para ingresos directos desde la vista del mapa de mesas libres.
 * * @author Dana
 * @version 1.0
 */
public class AsignarMesaController {

    // CONTROLES DE LA INTERFAZ FXML
    @FXML private TextField txtNombreCliente;
    @FXML private TextField txtPersonas;
    @FXML private ComboBox<String> cmbMesas;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    // ESTADO INTERNO Y ACCESO A DATOS
    private ClienteEspera clienteEspera = null;
    private Integer idMesaFija = null;
    private final MesasDAO mesasDAO = new MesasDAO();

    /**
     * MODO 1: Carga los datos de un cliente que se encuentra en la lista de espera,
     * bloqueando la edición de sus datos personales y filtrando las mesas disponibles.
     * * @param cliente Objeto ClienteEspera que contiene el registro de procedencia.
     */
    public void setDatosDesdeEspera(ClienteEspera cliente) {
        this.clienteEspera = cliente;
        
        txtNombreCliente.setText(cliente.getNombreCliente());
        txtNombreCliente.setDisable(true);
        txtPersonas.setText(String.valueOf(cliente.getNumeroPersonas()));
        txtPersonas.setDisable(true);

        cargarMesasLibres();
    }

    /**
     * MODO 2: Fija la mesa seleccionada cuando el operador hace clic directamente 
     * sobre un espacio libre dentro del mapa de mesas, inhabilitando el ComboBox de selección.
     * * @param idMesa Identificador numérico único de la mesa seleccionada en el mapa.
     */
    public void setDatosDesdeMapa(int idMesa) {
        this.idMesaFija = idMesa;
        
        // Bloqueamos la mesa porque ya le dio clic a una en específico
        cmbMesas.getItems().add(idMesa + "-Mesa");
        cmbMesas.getSelectionModel().selectFirst();
        cmbMesas.setDisable(true);
    }

    /**
     * Consulta la base de datos para recuperar las mesas con estado 'Libre' 
     * e insertarlas como opciones elegibles dentro del ComboBox.
     */
    private void cargarMesasLibres() {
        List<Mesa> todasLasMesas = mesasDAO.obtenerTodasLasMesas();
        ObservableList<String> mesasLibres = FXCollections.observableArrayList();

        for (Mesa m : todasLasMesas) {
            if ("Libre".equals(m.getEstado())) {
                mesasLibres.add(m.getIdMesa() + " - Capacidad: " + m.getCapacidad());
            }
        }
        cmbMesas.setItems(mesasLibres);
        
        if (mesasLibres.isEmpty()) {
            cmbMesas.setPromptText("Sin mesas libres");
            btnConfirmar.setDisable(true);
        }
    }

    /**
     * Evalúa la selección de la mesa, actualiza su estado operativo a 'Ocupada' en la BD
     * y remueve al comensal de la lista de espera si corresponde.
     * * @param event Evento de disparo vinculado al botón de confirmación.
     */
    @FXML
    private void handleGuardar(ActionEvent event) {
        // 1. Validar que los campos no estén vacíos
        if (txtNombreCliente.getText().trim().isEmpty() || txtPersonas.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Ingresa el nombre y número de personas.");
            return;
        }

        int numPersonas;
        try {
            numPersonas = Integer.parseInt(txtPersonas.getText().trim());
            if (numPersonas <= 0) {
                 mostrarAlerta(Alert.AlertType.WARNING, "Número inválido", "El número de personas debe ser mayor a 0.");
                 return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Formato incorrecto", "El número de personas debe ser un valor numérico.");
            return;
        }

        // 2. Determinar qué mesa se va a ocupar
        int idMesaAAsignar;
        if (idMesaFija != null) {
            idMesaAAsignar = idMesaFija;
        } else {
            String seleccion = cmbMesas.getValue();
            if (seleccion == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Elige una mesa.");
                return;
            }
            idMesaAAsignar = Integer.parseInt(seleccion.split(" ")[0]);
        }

        // 3. Determinar la capacidad
        Mesa mesa = mesasDAO.obtenerMesaPorId(idMesaAAsignar);
        
        if (mesa != null) {
            if (numPersonas > mesa.getCapacidad()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Capacidad excedida", 
                    "La Mesa " + idMesaAAsignar + " tiene capacidad máxima para " + mesa.getCapacidad() + " personas.\n" +
                    "No puedes sentar a un grupo de " + numPersonas + " allí.");
                return; // Detenemos la asignación
            }
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se encontró la información de la mesa.");
            return;
        }
        // 4. Si pasa la validación, ocupamos la mesa en la BD
        mesasDAO.actualizarEstadoMesa(idMesaAAsignar, "Ocupada");

        // 5. Si venía de la lista de espera, actualizar su estado a "Asignado"
        if (clienteEspera != null) {
            ListaEsperaDAO.actualizarEstado(clienteEspera.getIdEspera(), "Asignado");
        }

        mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "La Mesa " + idMesaAAsignar + " ha sido asignada correctamente.");
        cerrarVentana();
    }

    /**
     * Cancela la operación actual sin realizar modificaciones en la base de datos y cierra el modal.
     * * @param event Evento de disparo vinculado al botón Cancelar.
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarVentana();
    }

    /**
     * Recupera el contenedor de la ventana actual (Stage) a través del botón cancelar para cerrarlo.
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    /**
     * Despliega un cuadro de diálogo modal en la pantalla para interactuar con el operador del sistema.
     * * @param tipo El tipo de categorización o icono del aviso ({@code WARNING}, {@code INFORMATION}, etc.).
     * @param titulo Texto para la barra superior del recuadro.
     * @param mensaje Mensaje o texto principal del aviso.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}