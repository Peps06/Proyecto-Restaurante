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
     * @param idOrden → ID de la orden asociada.
     * @param idEmpleado → ID del cajero que registra.
     * @param montoTotal → Importe total a cobrar.
     * @param efectivo → Efectivo físico que entregó el cliente.
     */
    public static Pago efectivo(int idOrden, int idEmpleado, double montoTotal, double efectivo) {
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
     * @param idOrden → ID de la orden asociada.
     * @param idEmpleado → ID del cajero que registra.
     * @param montoTotal → Importe total a cobrar (= monto cargado a tarjeta).
     * @param tipoTarjeta → {@code "Debito"} o {@code "Credito"}.
     */
    public static Pago tarjeta(int idOrden, int idEmpleado, double montoTotal, String tipoTarjeta) {
        Pago p = new Pago();
        p.idOrden = idOrden;
        p.idEmpleado = idEmpleado;
        p.montoTotal = montoTotal;
        p.tarjeta = montoTotal;
        p.tipoTarjeta = tipoTarjeta;
        p.formaPago = "Tarjeta";
        return p;
    }
    
    /** Constructor vacío necesario para mapeo desde ResultSet. */
    public Pago() {}

    // GETTERS Y SETTERS

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public Double getEfectivo() {
        return efectivo;
    }

    public void setEfectivo(Double efectivo) {
        this.efectivo = efectivo;
    }

    public Double getCambio() {
        return cambio;
    }

    public void setCambio(Double cambio) {
        this.cambio = cambio;
    }

    public Double getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(Double tarjeta) {
        this.tarjeta = tarjeta;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    @Override
    public String toString() {
        return "Pago{id=" + idPago +
               ", orden=" + idOrden +
               ", forma=" + formaPago +
               ", total=" + montoTotal + "}";
    }
}
