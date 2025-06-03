package com.ejemplo.algoritmo;

/**
 * Representa el resultado de una iteración del algoritmo constructivo
 */
public class ResultadoIteracion {

    private double tiempo;
    private String heuristicaSeleccionada;
    private int vehiculosAsignados;
    private int cargadoresUtilizados;
    private double valorEvaluacion;
    private double costoIteracion;
    private double energiaEntregada;
    private AsignacionCandidata mejorAsignacion;

    public ResultadoIteracion(double tiempo) {
        this.tiempo = tiempo;
        this.vehiculosAsignados = 0;
        this.cargadoresUtilizados = 0;
        this.valorEvaluacion = 0.0;
        this.costoIteracion = 0.0;
        this.energiaEntregada = 0.0;
    }

    /**
     * Genera un resumen de la iteración
     */
    public String generarResumen() {
        return String.format("Tiempo: %.2fh | Heurística: %s | Vehículos: %d | Valor: %.3f",
                tiempo, heuristicaSeleccionada, vehiculosAsignados, valorEvaluacion);
    }

    // Getters y Setters
    public double getTiempo() {
        return tiempo;
    }

    public void setTiempo(double tiempo) {
        this.tiempo = tiempo;
    }

    public String getHeuristicaSeleccionada() {
        return heuristicaSeleccionada;
    }

    public void setHeuristicaSeleccionada(String heuristicaSeleccionada) {
        this.heuristicaSeleccionada = heuristicaSeleccionada;
    }

    public int getVehiculosAsignados() {
        return vehiculosAsignados;
    }

    public void setVehiculosAsignados(int vehiculosAsignados) {
        this.vehiculosAsignados = vehiculosAsignados;
    }

    public int getCargadoresUtilizados() {
        return cargadoresUtilizados;
    }

    public void setCargadoresUtilizados(int cargadoresUtilizados) {
        this.cargadoresUtilizados = cargadoresUtilizados;
    }

    public double getValorEvaluacion() {
        return valorEvaluacion;
    }

    public void setValorEvaluacion(double valorEvaluacion) {
        this.valorEvaluacion = valorEvaluacion;
    }

    public double getCostoIteracion() {
        return costoIteracion;
    }

    public void setCostoIteracion(double costoIteracion) {
        this.costoIteracion = costoIteracion;
    }

    public double getEnergiaEntregada() {
        return energiaEntregada;
    }

    public void setEnergiaEntregada(double energiaEntregada) {
        this.energiaEntregada = energiaEntregada;
    }

    public AsignacionCandidata getMejorAsignacion() {
        return mejorAsignacion;
    }

    public void setMejorAsignacion(AsignacionCandidata mejorAsignacion) {
        this.mejorAsignacion = mejorAsignacion;
    }
}