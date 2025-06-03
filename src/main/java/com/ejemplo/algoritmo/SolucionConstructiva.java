package com.ejemplo.algoritmo;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Representa la solución completa generada por el algoritmo constructivo
 */
public class SolucionConstructiva {

    private Map<Integer, Integer> asignacionesFinal;
    private double costoTotalOperacion;
    private double valorCargaEntregada;
    private double energiaTotalEntregada;
    private double energiaTotalRequerida;
    private double porcentajeCargaEntregado;
    private double tiempoTotalEjecucion; // en milisegundos
    private int vehiculosAtendidos;
    private int vehiculosCompletados;
    private double porcentajeCompletitud;

    // Métricas detalladas
    private double costoEnergia;
    private double penalizacionRetrasos;
    private double eficienciaPromedio;
    private double tiempoEsperaPromedio;
    private double utilizacionCargadores;

    // Estadísticas por heurística
    private Map<String, Integer> usoHeuristicas;
    private String heuristicaMasEfectiva;

    // Detalles de vehículos
    private List<VehiculoSimulacion> estadoFinalVehiculos;

    public SolucionConstructiva() {
        this.costoTotalOperacion = 0.0;
        this.valorCargaEntregada = 0.0;
        this.energiaTotalEntregada = 0.0;
        this.energiaTotalRequerida = 0.0;
        this.porcentajeCargaEntregado = 0.0;
        this.tiempoTotalEjecucion = 0.0;
        this.vehiculosAtendidos = 0;
        this.vehiculosCompletados = 0;
        this.porcentajeCompletitud = 0.0;
        this.costoEnergia = 0.0;
        this.penalizacionRetrasos = 0.0;
        this.eficienciaPromedio = 0.0;
        this.tiempoEsperaPromedio = 0.0;
        this.utilizacionCargadores = 0.0;
    }

    /**
     * Calcula el valor objetivo principal (multiobjetivo)
     */
    public double getValorObjetivo() {
        // Función multiobjetivo: maximizar valor de carga - minimizar costos
        // Alineado con el modelo de Python: - (1.0 * costo_total) + (0.5 *
        // eficiencia_total)
        return (0.5 * valorCargaEntregada) - (1.0 * costoTotalOperacion);
    }

    /**
     * Calcula la eficiencia general de la solución
     */
    public double getEficienciaGeneral() {
        if (vehiculosAtendidos == 0)
            return 0.0;
        return (valorCargaEntregada / costoTotalOperacion) * (porcentajeCompletitud / 100.0);
    }

    /**
     * Genera un resumen de la solución
     */
    public String generarResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("=== RESUMEN DE SOLUCIÓN CONSTRUCTIVA ===\n");
        resumen.append(String.format("Valor Objetivo: %.2f\n", getValorObjetivo()));
        resumen.append(String.format("Costo Total: %.2f EUR\n", costoTotalOperacion));
        resumen.append(String.format("Valor Carga Entregada: %.2f\n", valorCargaEntregada));
        resumen.append(String.format("Energía Entregada: %.2f kWh\n", energiaTotalEntregada));
        resumen.append(String.format("Energía Requerida: %.2f kWh\n", energiaTotalRequerida));
        resumen.append(String.format("Porcentaje Carga Entregado: %.1f%%\n", porcentajeCargaEntregado));
        resumen.append(String.format("Vehículos Atendidos: %d\n", vehiculosAtendidos));
        resumen.append(String.format("Vehículos Completados: %d\n", vehiculosCompletados));
        resumen.append(String.format("Completitud Promedio: %.1f%%\n", porcentajeCompletitud));
        resumen.append(String.format("Eficiencia General: %.3f\n", getEficienciaGeneral()));
        resumen.append(String.format("Tiempo Ejecución: %.2f ms\n", tiempoTotalEjecucion));

        if (heuristicaMasEfectiva != null) {
            resumen.append(String.format("Heurística Más Efectiva: %s\n", heuristicaMasEfectiva));
        }

        return resumen.toString();
    }

    // Getters y Setters
    public Map<Integer, Integer> getAsignacionesFinal() {
        return asignacionesFinal;
    }

    public void setAsignacionesFinal(Map<Integer, Integer> asignacionesFinal) {
        this.asignacionesFinal = asignacionesFinal;
    }

    public double getCostoTotalOperacion() {
        return costoTotalOperacion;
    }

    public void setCostoTotalOperacion(double costoTotalOperacion) {
        this.costoTotalOperacion = costoTotalOperacion;
    }

    public double getValorCargaEntregada() {
        return valorCargaEntregada;
    }

    public void setValorCargaEntregada(double valorCargaEntregada) {
        this.valorCargaEntregada = valorCargaEntregada;
    }

    public double getEnergiaTotalEntregada() {
        return energiaTotalEntregada;
    }

    public void setEnergiaTotalEntregada(double energiaTotalEntregada) {
        this.energiaTotalEntregada = energiaTotalEntregada;
    }

    public double getTiempoTotalEjecucion() {
        return tiempoTotalEjecucion;
    }

    public void setTiempoTotalEjecucion(double tiempoTotalEjecucion) {
        this.tiempoTotalEjecucion = tiempoTotalEjecucion;
    }

    public int getVehiculosAtendidos() {
        return vehiculosAtendidos;
    }

    public void setVehiculosAtendidos(int vehiculosAtendidos) {
        this.vehiculosAtendidos = vehiculosAtendidos;
    }

    public int getVehiculosCompletados() {
        return vehiculosCompletados;
    }

    public void setVehiculosCompletados(int vehiculosCompletados) {
        this.vehiculosCompletados = vehiculosCompletados;
    }

    public double getPorcentajeCompletitud() {
        return porcentajeCompletitud;
    }

    public void setPorcentajeCompletitud(double porcentajeCompletitud) {
        this.porcentajeCompletitud = porcentajeCompletitud;
    }

    public double getCostoEnergia() {
        return costoEnergia;
    }

    public void setCostoEnergia(double costoEnergia) {
        this.costoEnergia = costoEnergia;
    }

    public double getPenalizacionRetrasos() {
        return penalizacionRetrasos;
    }

    public void setPenalizacionRetrasos(double penalizacionRetrasos) {
        this.penalizacionRetrasos = penalizacionRetrasos;
    }

    public double getEficienciaPromedio() {
        return eficienciaPromedio;
    }

    public void setEficienciaPromedio(double eficienciaPromedio) {
        this.eficienciaPromedio = eficienciaPromedio;
    }

    public double getTiempoEsperaPromedio() {
        return tiempoEsperaPromedio;
    }

    public void setTiempoEsperaPromedio(double tiempoEsperaPromedio) {
        this.tiempoEsperaPromedio = tiempoEsperaPromedio;
    }

    public double getUtilizacionCargadores() {
        return utilizacionCargadores;
    }

    public void setUtilizacionCargadores(double utilizacionCargadores) {
        this.utilizacionCargadores = utilizacionCargadores;
    }

    public Map<String, Integer> getUsoHeuristicas() {
        return usoHeuristicas;
    }

    public void setUsoHeuristicas(Map<String, Integer> usoHeuristicas) {
        this.usoHeuristicas = usoHeuristicas;
    }

    public String getHeuristicaMasEfectiva() {
        return heuristicaMasEfectiva;
    }

    public void setHeuristicaMasEfectiva(String heuristicaMasEfectiva) {
        this.heuristicaMasEfectiva = heuristicaMasEfectiva;
    }

    public List<VehiculoSimulacion> getEstadoFinalVehiculos() {
        return estadoFinalVehiculos;
    }

    public void setEstadoFinalVehiculos(List<VehiculoSimulacion> estadoFinalVehiculos) {
        this.estadoFinalVehiculos = estadoFinalVehiculos;
    }

    public double getEnergiaTotalRequerida() {
        return energiaTotalRequerida;
    }

    public void setEnergiaTotalRequerida(double energiaTotalRequerida) {
        this.energiaTotalRequerida = energiaTotalRequerida;
    }

    public double getPorcentajeCargaEntregado() {
        return porcentajeCargaEntregado;
    }

    public void setPorcentajeCargaEntregado(double porcentajeCargaEntregado) {
        this.porcentajeCargaEntregado = porcentajeCargaEntregado;
    }

    /**
     * Crea una copia profunda de esta solución
     */
    public SolucionConstructiva clonar() {
        SolucionConstructiva clon = new SolucionConstructiva();

        // Copiar valores primitivos que existen
        clon.costoTotalOperacion = this.costoTotalOperacion;
        clon.valorCargaEntregada = this.valorCargaEntregada;
        clon.energiaTotalEntregada = this.energiaTotalEntregada;
        clon.energiaTotalRequerida = this.energiaTotalRequerida;
        clon.porcentajeCargaEntregado = this.porcentajeCargaEntregado;
        clon.tiempoTotalEjecucion = this.tiempoTotalEjecucion;
        clon.vehiculosAtendidos = this.vehiculosAtendidos;
        clon.vehiculosCompletados = this.vehiculosCompletados;
        clon.porcentajeCompletitud = this.porcentajeCompletitud;
        clon.costoEnergia = this.costoEnergia;
        clon.penalizacionRetrasos = this.penalizacionRetrasos;
        clon.eficienciaPromedio = this.eficienciaPromedio;
        clon.utilizacionCargadores = this.utilizacionCargadores;
        clon.tiempoEsperaPromedio = this.tiempoEsperaPromedio;

        // Copiar Strings
        clon.heuristicaMasEfectiva = this.heuristicaMasEfectiva;

        // Copiar colecciones (crear nuevas instancias)
        if (this.usoHeuristicas != null) {
            clon.usoHeuristicas = new HashMap<>(this.usoHeuristicas);
        }

        if (this.estadoFinalVehiculos != null) {
            clon.estadoFinalVehiculos = new ArrayList<>(this.estadoFinalVehiculos);
        }

        return clon;
    }
}