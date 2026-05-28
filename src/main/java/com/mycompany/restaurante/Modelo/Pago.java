package com.mycompany.restaurante.Modelo;

import java.time.LocalDateTime;

/**
 * Representa un registro de pago dentro del sistema Saveurs Paris.
 * Mapea directamente con la tabla {@code pagos} de la base de datos.
 *
 * @author Citlaly
 * @version 1.0
 */
public class Pago {
    
    private int idPago;
    private int idOrden;
    private int idEmpleado;
    private double montoTotal;
    private Double efectivo;
    private Double cambio;
    private Double tarjeta;
    private String tipoTarjeta;
    private String formaPago;
    private LocalDateTime fechaHora;

    /**
     * Constructor para pago en Efectivo.
     * El cambio se calcula automáticamente como {@code efectivoRecibido - montoTotal}.
     *
     * @param idOrden Identificador de la orden asociada.
     * @param idEmpleado Identificador del cajero que registra.
     * @param montoTotal Importe total a cobrar.
     * @param efectivo Efectivo físico que entregó el cliente.
     * @return Una instancia de {@code Pago} parametrizada para efectivo.
     */
    public static Pago efectivo(int idOrden, int idEmpleado, double montoTotal,
            double efectivo) {
        Pago p = new Pago();
        p.idOrden = idOrden;
        p.idEmpleado = idEmpleado;
        p.montoTotal = montoTotal;
        p.efectivo = efectivo;
        p.cambio = Math.max(0, efectivo - montoTotal);
        p.formaPago = "Efectivo";
        return p;
    }

    /**
     * Constructor para pago con Tarjeta (débito o crédito).
     * El monto cargado a la tarjeta es igual al total.
     *
     * @param idOrden Identificador de la orden asociada.
     * @param idEmpleado Identificador del cajero que registra.
     * @param montoTotal Importe total a cobrar (= monto cargado a tarjeta).
     * @param tipoTarjeta El tipo de tarjeta utilizado: {@code "Debito"} o {@code "Credito"}.
     * @return Una instancia de {@code Pago} parametrizada para tarjeta electrónica.
     */
    public static Pago tarjeta(int idOrden, int idEmpleado, double montoTotal,
            String tipoTarjeta) {
        Pago p = new Pago();
        p.idOrden = idOrden;
        p.idEmpleado = idEmpleado;
        p.montoTotal = montoTotal;
        p.tarjeta = montoTotal;
        p.tipoTarjeta = tipoTarjeta;
        p.formaPago = "Tarjeta";
        return p;
    }
    
    /** Constructor vacío para mapeo e instanciación desde un ResultSet de SQL. */
    public Pago() {}

    // GETTERS Y SETTERS

    /**
     * Obtiene el identificador único del pago.
     * 
     * @return El identificador numérico de este registro.
     */
    public int getIdPago() {
        return idPago;
    }

    /**
     * Establece el identificador único del pago.
     * 
     * @param idPago El nuevo identificador del registro.
     */
    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    /**
     * Obtiene el identificador de la orden relacionada.
     * 
     * @return El ID de la orden.
     */
    public int getIdOrden() {
        return idOrden;
    }

    /**
     * Vincula este pago a una orden específica.
     * 
     * @param idOrden El ID de la orden correspondiente.
     */
    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    /**
     * Obtiene el identificador del empleado involucrado.
     * 
     * @return El ID del cajero.
     */
    public int getIdEmpleado() {
        return idEmpleado;
    }

    /**
     * Asigna el empleado responsable de la transacción.
     * 
     * @param idEmpleado El ID del cajero asignado.
     */
    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    /**
     * Obtiene el importe total que liquida este objeto.
     * 
     * @return Monto total de la cuenta.
     */
    public double getMontoTotal() {
        return montoTotal;
    }

    /**
     * Modifica o asigna el monto total a pagar de la transacción.
     * 
     * @param montoTotal Nuevo importe total de la cuenta.
     */
    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    /**
     * Obtiene el efectivo entregado por el cliente.
     * 
     * @return Cantidad de efectivo recibido; puede ser {@code null}.
     */
    public Double getEfectivo() {
        return efectivo;
    }

    /**
     * Establece la cantidad recibida en efectivo.
     * 
     * @param efectivo Suma en papel moneda provista por el receptor.
     */
    public void setEfectivo(Double efectivo) {
        this.efectivo = efectivo;
    }

    /**
     * Obtiene el cambio derivado de la entrega en efectivo.
     * 
     * @return El remanente a regresar al cliente.
     */
    public Double getCambio() {
        return cambio;
    }

    /**
     * Establece la cantidad de dinero a regresar como cambio.
     * 
     * @param cambio Diferencia calculada a devolver.
     */
    public void setCambio(Double cambio) {
        this.cambio = cambio;
    }

    /**
     * Obtiene el monto total cobrado mediante el terminal electrónico.
     * 
     * @return El cargo hecho a la tarjeta.
     */
    public Double getTarjeta() {
        return tarjeta;
    }

    /**
     * Establece la suma a cargar al plástico bancario.
     * 
     * @param tarjeta El monto de la transacción bancaria.
     */
    public void setTarjeta(Double tarjeta) {
        this.tarjeta = tarjeta;
    }

    /**
     * Obtiene la categoría de la tarjeta del cliente (Crédito o Débito).
     * 
     * @return Una cadena descriptiva de la naturaleza plástica.
     */
    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    /**
     * Establece la categoría de la tarjeta utilizada.
     * 
     * @param tipoTarjeta El tipo de tarjeta utilizado en la transacción.
     */
    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    /**
     * Recupera la etiqueta legible con la forma en que se saldó el cobro.
     * 
     * @return La forma de pago registrada.
     */
    public String getFormaPago() {
        return formaPago;
    }

    /**
     * Define de forma estricta la forma física o virtual del pago.
     * 
     * @param formaPago Descripción del método de pago (Efectivo/Tarjeta).
     */
    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    /**
     * Obtiene la marca temporal exacta de la transacción en el servidor/sistema.
     * 
     * @return El objeto temporal del instante de persistencia.
     */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /**
     * Establece el instante cronológico en el que se efectúa la transacción.
     * 
     * @param fechaHora Fecha y hora del sistema para el registro.
     */
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    /**
     * Serializa una representación simplificada de la entidad en formato de texto.
     * 
     * @return Una cadena de texto conteniendo los atributos esenciales del pago.
     */
    @Override
    public String toString() {
        return "Pago{id=" + idPago +
               ", orden=" + idOrden +
               ", forma=" + formaPago +
               ", total=" + montoTotal + "}";
    }
}