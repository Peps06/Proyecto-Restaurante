package com.mycompany.restaurante.Modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Representa una factura fiscal (CFDI) generada por el sistema.
 *
 * La clase gestiona la creación de un folio único automático combinando un 
 * timestamp con un identificador UUID, simulando la validez de un comprobante fiscal.
 * Al instanciarse, vincula el consumo de una mesa con los datos legales del cliente.
 * 
 * @author Citlaly
 * @version 1.0
 */
public class Factura {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final String folio; // Folio único autogenerado
    private final int numMesa;
    private final double total;
    private final DatosFacturacion datosFiscales;
    private final LocalDateTime fechaHora;
    private boolean cuentaFacturada;

    /**
     * Crea una nueva factura y genera automáticamente su folio único.
     * 
     * @param numMesa       Identificador de la mesa que solicita la factura.
     * @param total         Importe total a pagar.
     * @param datosFiscales Objeto con la información fiscal del cliente.
     */
    public Factura(int numMesa, double total, DatosFacturacion datosFiscales) {
        this.numMesa = numMesa;
        this.total = total;
        this.datosFiscales = datosFiscales;
        this.fechaHora = LocalDateTime.now();
        this.folio = generarFolio();
        this.cuentaFacturada = true;   // postcondición: cuenta marcada como facturada
    }

    /**
     * Genera un identificador de folio con el formato: SP-YYYYMMDD-HHmmss-XXXX.
     * Utiliza los primeros 4 caracteres de un UUID aleatorio para garantizar la unicidad.
     * 
     * @return Una cadena de texto que representa el folio único de la factura.
     */
    private String generarFolio() {
        String uuid4 = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return "SP-" + FMT.format(fechaHora) + "-" + uuid4;
    }

   /**@return El folio autogenerado de la factura. */
    public String getFolio() { return folio; }

    /** @return El número de la mesa facturada. */
    public int getNumMesa() { return numMesa; }

    /** @return El monto total de la factura. */
    public double getTotal() { return total; }

    /** @return Los datos fiscales del cliente asociados a esta factura. */
    public DatosFacturacion getDatosFiscales() { return datosFiscales; }

    /** @return La fecha y hora de emisión. */
    public LocalDateTime getFechaHora() { return fechaHora; }

    /** @return true si la cuenta ha sido marcada como facturada. */
    public boolean isCuentaFacturada() { return cuentaFacturada; }

    /**
     * Retorna una representación textual simplificada de la factura.
     * @return String con el folio, mesa, total y RFC del cliente.
     */
    @Override
    public String toString() {
        return "Factura{folio='" + folio + "', mesa=" + numMesa +
               ", total=" + total + ", rfc=" + datosFiscales.getRfc() + "}";
    }
}
