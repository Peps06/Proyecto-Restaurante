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
 * 
 * @author Dana
 */
public class AsignarMesaController {

    @FXML private TextField txtNombreCliente;
    @FXML private TextField txtPersonas;
    @FXML private ComboBox<String> cmbMesas;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    private ClienteEspera clienteEspera = null;
    private Integer idMesaFija = null;
    private final MesasDAO mesasDAO = new MesasDAO();

    /**
     * MODO 1: Se llama desde la Lista de Espera.
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
     * MODO 2: Se llama desde el Mapa de Mesas al dar clic en una mesa Libre.
     */
    public void setDatosDesdeMapa(int idMesa) {
        this.idMesaFija = idMesa;
        
        // Bloqueamos la mesa porque ya le dio clic a una en específico
        cmbMesas.getItems().add(idMesa + "-Mesa");
        cmbMesas.getSelectionModel().selectFirst();
        cmbMesas.setDisable(true);
    }

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

    @FXML
    private void handleGuardar(ActionEvent event) {
        // Validar que los campos no estén vacíos (para cuando es cliente nuevo)
        if (txtNombreCliente.getText().trim().isEmpty() || txtPersonas.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Ingresa el nombre y número de personas.");
            return;
        }

        // Determinar que mesa se va a ocupar
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

        // 1. Ocupar la mesa en la BD
        mesasDAO.actualizarEstadoMesa(idMesaAAsignar, "Ocupada");

        // 2. Si venía de la lista de espera, actualizar su estado a "Asignado"
        if (clienteEspera != null) {
            ListaEsperaDAO.actualizarEstado(clienteEspera.getIdEspera(), "Asignado");
        }

        mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "La Mesa " + idMesaAAsignar + " ha sido asignada.");
        cerrarVentana();
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
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
}