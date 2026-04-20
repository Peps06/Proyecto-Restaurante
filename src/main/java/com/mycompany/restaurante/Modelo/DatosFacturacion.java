package com.mycompany.restaurante.Modelo;

import java.util.regex.Pattern;

/**
 * Encapsula los datos fiscales del cliente para generar un CFDI.
 * 
 * @author Citlaly
 */
public class DatosFacturacion {

    //  Patrones de validación 

    private static final Pattern PATRON_RFC =
            Pattern.compile("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");

    /** Correo: algo@dominio.extensión (mínimo 2 en extensión). */
    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    /** Código postal mexicano: exactamente 5 dígitos numéricos. */
    private static final Pattern PATRON_CP =
            Pattern.compile("^\\d{5}$");

    //  Campos 
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

    //  Métodos de validación estáticos 

    public static boolean rfcValido(String rfc) {
        if (rfc == null || rfc.isBlank()) return false;
        return PATRON_RFC.matcher(rfc.trim().toUpperCase()).matches();
    }

    public static boolean correoValido(String correo) {
        if (correo == null || correo.isBlank()) return false;
        return PATRON_CORREO.matcher(correo.trim()).matches();
    }

    public static boolean cpValido(String cp) {
        if (cp == null || cp.isBlank()) return false;
        return PATRON_CP.matcher(cp.trim()).matches();
    }

    //  Getters & Setters 

    public String getNombreRazonSocial() { return nombreRazonSocial; }
    public void   setNombreRazonSocial(String v) { this.nombreRazonSocial = v; }

    public String getRfc() { return rfc; }
    public void   setRfc(String v) { this.rfc = v; }

    public String getCodigoPostal() { return codigoPostal; }
    public void   setCodigoPostal(String v) { this.codigoPostal = v; }

    public String getCorreo() { return correo; }
    public void   setCorreo(String v) { this.correo = v; }

    public String getRegimenFiscal() { return regimenFiscal; }
    public void   setRegimenFiscal(String v) { this.regimenFiscal = v; }

    public String getUsoCfdi() { return usoCfdi; }
    public void   setUsoCfdi(String v) { this.usoCfdi = v; }
}