package com.mycompany.restaurante.Modelo;

import java.util.regex.Pattern;

/**
 * Encapsula los datos fiscales del cliente necesarios para generar un CFDI.
 * Incluye lógica de validación mediante expresiones regulares para asegurar 
 * que el RFC, Correo y Código Postal cumplan con los estándares oficiales.
 * 
 * @author Citlaly
 * @version 1.0
 */
public class DatosFacturacion {

    // PATRONES DE VALIDACIÓN 

    /** 
     * Patrón para RFC (México). 
     * Soporta personas físicas (13 caracteres) y morales (12 caracteres).
     */
    private static final Pattern PATRON_RFC =
            Pattern.compile("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");

    /** Patrón para correo electrónico estándar: algo@dominio.extensión */
    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    /** Patrón para código postal mexicano: exactamente 5 dígitos numéricos. */
    private static final Pattern PATRON_CP =
            Pattern.compile("^\\d{5}$");

    // CAMPOS 
    private String nombreRazonSocial;
    private String rfc;
    private String codigoPostal;
    private String correo;
    private String regimenFiscal;
    private String usoCfdi;

    /**
     * Constructor vacío para inicialización parcial o frameworks de persistencia.
     */
    public DatosFacturacion() {}

    /**
     * Constructor con todos los parámetros para crear un perfil fiscal completo.
     * 
     * @param nombreRazonSocial Nombre legal o comercial del cliente.
     * @param rfc               Registro Federal de Contribuyentes.
     * @param codigoPostal      CP del domicilio fiscal.
     * @param correo            Dirección de email para envío de XML/PDF.
     * @param regimenFiscal     Régimen ante el SAT (ej. Sueldos y Salarios).
     * @param usoCfdi           Clave de uso del CFDI (ej. G03 - Gastos en general).
     */
    public DatosFacturacion(String nombreRazonSocial, String rfc, String codigoPostal,
                             String correo, String regimenFiscal, String usoCfdi) {
        this.nombreRazonSocial = nombreRazonSocial;
        this.rfc = rfc;
        this.codigoPostal = codigoPostal;
        this.correo = correo;
        this.regimenFiscal = regimenFiscal;
        this.usoCfdi = usoCfdi;
    }

    //  MÉTODOS DE VALIDACIÓN

    /**
     * Valida si una cadena de texto tiene el formato de un RFC oficial.
     * 
     * @param rfc El RFC a validar.
     * @return {@code true} si cumple con el patrón oficial, {@code false} en caso contrario.
     */
    public static boolean rfcValido(String rfc) {
        if (rfc == null || rfc.isBlank()) return false;
        return PATRON_RFC.matcher(rfc.trim().toUpperCase()).matches();
    }

    /**
     * Valida si una cadena de texto tiene un formato de correo electrónico válido.
     * 
     * @param correo El email a validar.
     * @return {@code true} si el formato es correcto.
     */
    public static boolean correoValido(String correo) {
        if (correo == null || correo.isBlank()) return false;
        return PATRON_CORREO.matcher(correo.trim()).matches();
    }

    /**
     * Valida si el código postal consta exactamente de 5 dígitos numéricos.
     * 
     * @param cp El código postal a validar.
     * @return {@code true} si es un CP válido para México.
     */
    public static boolean cpValido(String cp) {
        if (cp == null || cp.isBlank()) return false;
        return PATRON_CP.matcher(cp.trim()).matches();
    }

    //  Getters & Setters 

    /** 
     * @return El nombre o razón social del cliente. 
     */
    public String getNombreRazonSocial() {
        return nombreRazonSocial;
    }
    
    /** 
     * @param nombreRazonSocial Nuevo nombre o razón social. 
     */
    public void setNombreRazonSocial(String nombreRazonSocial) {
        this.nombreRazonSocial = nombreRazonSocial;
    }

    /** 
     * @return El RFC registrado. 
     */
    public String getRfc() {
        return rfc;
    }
    /** 
     * @param rfc Nuevo RFC (será validado externamente). 
     */
    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    /**
     * @return El código postal fiscal.
     */
    public String getCodigoPostal() {
        return codigoPostal;
    }
    /**
     * @param codigoPostal Nuevo código postal.
     */
    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    /**
     * @return El correo electrónico de contacto.
     */
    public String getCorreo() {
        return correo;
    }
    /**
     * @param correo Nuevo correo electrónico.
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * @return El régimen fiscal del contribuyente.
     */
    public String getRegimenFiscal() {
        return regimenFiscal;
    }
    /**
     * @param v Clave del régimen fiscal.
     */
    public void setRegimenFiscal(String regimenFiscal) {
        this.regimenFiscal = regimenFiscal;
    }

    /**
     * @return La clave de uso del CFDI. 
     */
    public String getUsoCfdi() { return usoCfdi; }
    /** 
     * @param usoCfdi Clave de uso (ej. G01, G03, P01). 
     */
    public void setUsoCfdi(String usoCfdi) {
        this.usoCfdi = usoCfdi;
    }
}