package com.mycompany.restaurante.Modelo;

/**
 *
 * @author Rubi
 */
public class Asistencia {
    private int idAsistencia;
    private int idEmpleado;
    private String fecha;
    private String horaEntrada;
    private String horaSalida;
    private String estado;
    private String turno;

    // Constructor para la tabla
    public Asistencia(String fecha, String horaEntrada, String horaSalida, String estado, String turno) {
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.estado = estado;
        this.turno = turno;
    }

    // Getters 
    public String getFecha() {
        return fecha; }
    public String getHoraEntrada() {
        return horaEntrada; }
    public String getHoraSalida() {
        return horaSalida; }
    public String getEstado() {
        return estado; }
    public String getTurno() {
        return turno; }
    
    // Setters 
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }
    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }
    public void setEstado(String estado) {
        this.estado = estado; 
    }
    public void setTurno(String turno) {
        this.turno = turno; 
    }
    
}