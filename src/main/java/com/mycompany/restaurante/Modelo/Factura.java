package com.mycompany.restaurante.Modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Representa una factura fiscal (CFDI) generada por el sistema.
 *
 * El folio único se genera automáticamente en el constructor
 * usando un UUID recortado + timestamp, simulando la producción
 * de un comprobante fiscal digital.
 *
 * la factura queda asociada a la mesa y al total
 * de la cuenta; la cuenta se marca como "facturada".
 * 
 * @author Citlaly
 */
public class Factura {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final String folio; // Folio único autogenerado
    private final int numMesa;
    private final double total;
    private final DatosFacturacion datosFiscales;
    private final LocalDateTime fechaHora;
    private boolean cuentaFacturada;

    public Factura(int numMesa, double total, DatosFacturacion datosFiscales) {
        this.numMesa = numMesa;
        this.total = total;
        this.datosFiscales = datosFiscales;
        this.fechaHora = LocalDateTime.now();
        this.folio = generarFolio();
        this.cuentaFacturada = true;   // postcondición: cuenta marcada como facturada
    }

    /**
     * Genera un folio con formato:
     *   SP-YYYYMMDD-HHmmss-XXXX
     * donde XXXX son los primeros 4 caracteres de un UUID en mayúsculas.
     */
    private String generarFolio() {
        String uuid4 = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return "SP-" + FMT.format(fechaHora) + "-" + uuid4;
    }

    // Getters

    public String getFolio() {return folio;}
    public int getNumMesa() {return numMesa;}
    public double getTotal() {return total;}
    public DatosFacturacion getDatosFiscales() {return datosFiscales;}
    public LocalDateTime getFechaHora() {return fechaHora;}
    public boolean isCuentaFacturada() {return cuentaFacturada;}

    @Override
    public String toString() {
        return "Factura{folio='" + folio + "', mesa=" + numMesa +
               ", total=" + total + ", rfc=" + datosFiscales.getRfc() + "}";
    }
}
