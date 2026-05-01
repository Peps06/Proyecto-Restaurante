/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Modelo;

/**
 *
 * @author mrubi
 */
public class Asistencia {
    private int idAsistencia;
    private int idEmpleado;
    private String fecha;
    private String horaEntrada;
    private String horaSalida;
    private String estado;

    // Constructor para la tabla
    public Asistencia(String fecha, String horaEntrada, String horaSalida, String estado) {
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.estado = estado;
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
}