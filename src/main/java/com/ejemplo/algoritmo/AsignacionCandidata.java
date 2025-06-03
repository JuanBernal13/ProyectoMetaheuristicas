package com.ejemplo.algoritmo;

import java.util.Map;

/**
 * Representa una asignación candidata de vehículos a cargadores
 * generada por una heurística específica
 */
public class AsignacionCandidata {

    private String nombreHeuristica;
    private Map<Integer, Integer> asignaciones; // vehiculoId -> cargadorId
    private double valorEvaluacion;
    private long tiempoComputo; // en milisegundos

    public AsignacionCandidata() {
        this.valorEvaluacion = 0.0;
        this.tiempoComputo = 0;
    }

    public AsignacionCandidata(String nombreHeuristica, Map<Integer, Integer> asignaciones) {
        this.nombreHeuristica = nombreHeuristica;
        this.asignaciones = asignaciones;
        this.valorEvaluacion = 0.0;
        this.tiempoComputo = 0;
    }

    /**
     * Obtiene el cargador asignado a un vehículo específico
     */
    public Integer getCargadorAsignado(int vehiculoId) {
        return asignaciones.get(vehiculoId);
    }

    /**
     * Verifica si un vehículo tiene asignación
     */
    public boolean tieneAsignacion(int vehiculoId) {
        return asignaciones.containsKey(vehiculoId) && asignaciones.get(vehiculoId) != null;
    }

    /**
     * Cuenta el número total de asignaciones
     */
    public int getNumeroAsignaciones() {
        return asignaciones.size();
    }

    /**
     * Crea una copia de esta asignación
     */
    public AsignacionCandidata copia() {
        AsignacionCandidata copia = new AsignacionCandidata();
        copia.nombreHeuristica = this.nombreHeuristica + "_copy";
        copia.asignaciones = Map.copyOf(this.asignaciones);
        copia.valorEvaluacion = this.valorEvaluacion;
        copia.tiempoComputo = this.tiempoComputo;
        return copia;
    }

    @Override
    public String toString() {
        return String.format("AsignacionCandidata{heuristica='%s', asignaciones=%d, valor=%.3f}",
                nombreHeuristica, asignaciones.size(), valorEvaluacion);
    }

    // Getters y Setters
    public String getNombreHeuristica() {
        return nombreHeuristica;
    }

    public void setNombreHeuristica(String nombreHeuristica) {
        this.nombreHeuristica = nombreHeuristica;
    }

    public Map<Integer, Integer> getAsignaciones() {
        return asignaciones;
    }

    public void setAsignaciones(Map<Integer, Integer> asignaciones) {
        this.asignaciones = asignaciones;
    }

    public double getValorEvaluacion() {
        return valorEvaluacion;
    }

    public void setValorEvaluacion(double valorEvaluacion) {
        this.valorEvaluacion = valorEvaluacion;
    }

    public long getTiempoComputo() {
        return tiempoComputo;
    }

    public void setTiempoComputo(long tiempoComputo) {
        this.tiempoComputo = tiempoComputo;
    }
}