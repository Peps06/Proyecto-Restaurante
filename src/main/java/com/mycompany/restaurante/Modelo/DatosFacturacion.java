package com.mycompany.restaurante.Modelo;

/**
 * Encapsula los datos fiscales que el cliente proporciona para generar su factura (CFDI).
 * 
 * @author Citlaly
 */
public class DatosFacturacion {

    private String nombreRazonSocial;
    private String rfc;
    private String codigoPostal;
    private String correo;
    private String regimenFiscal;
    private String usoCfdi;

    public DatosFacturacion() {}

    public DatosFacturacion(String nombreRazonSocial, String rfc, String codigoPostal,
                             String correo, String regimenFiscal, String usoCfdi) {
        this.nombreRazonSocial = nombreRazonSocial;
        this.rfc = rfc;
        this.codigoPostal = codigoPostal;
        this.correo = correo;
        this.regimenFiscal = regimenFiscal;
        this.usoCfdi = usoCfdi;
    }

    // Getters & Setters

    public String getNombreRazonSocial() { return nombreRazonSocial; }
    public void setNombreRazonSocial(String nombreRazonSocial) { this.nombreRazonSocial = nombreRazonSocial; }

    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRegimenFiscal() { return regimenFiscal; }
    public void setRegimenFiscal(String regimenFiscal) { this.regimenFiscal = regimenFiscal; }

    public String getUsoCfdi() { return usoCfdi; }
    public void setUsoCfdi(String usoCfdi) { this.usoCfdi = usoCfdi; }

    @Override
    public String toString() {
        return "DatosFacturacion{" +
                "nombre='" + nombreRazonSocial + '\'' +
                ", rfc='" + rfc + '\'' +
                ", cp='" + codigoPostal + '\'' +
                ", correo='" + correo + '\'' +
                ", regimen='" + regimenFiscal + '\'' +
                ", usoCfdi='" + usoCfdi + '\'' +
                '}';
    }
}
