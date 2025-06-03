package com.ejemplo.algoritmo;

import com.ejemplo.model.VehicleArrival;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un vehículo durante la simulación del algoritmo constructivo
 */
public class VehiculoSimulacion {

    public enum EstadoVehiculo {
        ESPERANDO, // Esperando asignación a cargador
        CARGANDO, // Actualmente cargando
        COMPLETADO, // Carga completa
        RETIRADO // Vehículo retirado de la estación
    }

    // Clase para almacenar el historial de uso de cargadores
    public static class HistorialCargador {
        private final int cargadorId;
        private final double tiempoInicio;
        private double tiempoFin;
        private final String razonCambio;
        private boolean fuePreemptado;

        public HistorialCargador(int cargadorId, double tiempoInicio, String razonAsignacion) {
            this.cargadorId = cargadorId;
            this.tiempoInicio = tiempoInicio;
            this.tiempoFin = -1;
            this.razonCambio = razonAsignacion;
            this.fuePreemptado = false;
        }

        // Getters
        public int getCargadorId() {
            return cargadorId;
        }

        public double getTiempoInicio() {
            return tiempoInicio;
        }

        public double getTiempoFin() {
            return tiempoFin;
        }

        public String getRazonCambio() {
            return razonCambio;
        }

        public boolean isFuePreemptado() {
            return fuePreemptado;
        }

        // Setters
        public void setTiempoFin(double tiempoFin) {
            this.tiempoFin = tiempoFin;
        }

        public void setFuePreemptado(boolean fuePreemptado) {
            this.fuePreemptado = fuePreemptado;
        }

        public double getDuracion() {
            return tiempoFin > 0 ? tiempoFin - tiempoInicio : 0;
        }
    }

    private final VehicleArrival vehiculoOriginal;
    private EstadoVehiculo estado;
    private double energiaActual;
    private Integer cargadorAsignado;
    private double tiempoInicioEspera;
    private double tiempoInicioCarga;
    private double tiempoFinCarga;
    private boolean cargaCompleta;
    private double costoEnergiaAcumulado; // Nueva métrica: costo acumulado de energía
    private double costoOperacionAcumulado; // Nueva métrica: costo acumulado de operación
    private double urgenciaCarga;
    private double prioridadNormalizada; // Nueva métrica: prioridad normalizada 1-10

    // Nuevo: historial de cargadores usados
    private List<HistorialCargador> historialCargadores;
    private int numeroPreempciones;

    public VehiculoSimulacion(VehicleArrival vehiculoOriginal) {
        this.vehiculoOriginal = vehiculoOriginal;
        this.estado = EstadoVehiculo.ESPERANDO;
        this.energiaActual = 0.0;
        this.cargadorAsignado = null;
        this.tiempoInicioEspera = vehiculoOriginal.getArrivalTime();
        this.tiempoInicioCarga = -1;
        this.tiempoFinCarga = -1;
        this.cargaCompleta = false;
        this.costoEnergiaAcumulado = 0.0; // Inicializar nuevo campo
        this.costoOperacionAcumulado = 0.0; // Inicializar nuevo campo
        this.urgenciaCarga = 0.0;
        this.prioridadNormalizada = 0.0; // Inicializar prioridad normalizada
        this.historialCargadores = new ArrayList<>();
        this.numeroPreempciones = 0;
    }

    /**
     * Registra el uso de un nuevo cargador
     */
    public void asignarCargador(int cargadorId, double tiempo, String razon) {
        // Si había un cargador anterior, cerrar su registro
        if (!historialCargadores.isEmpty()) {
            HistorialCargador ultimo = historialCargadores.get(historialCargadores.size() - 1);
            if (ultimo.getTiempoFin() < 0) {
                ultimo.setTiempoFin(tiempo);
            }
        }

        // Agregar nuevo cargador al historial
        historialCargadores.add(new HistorialCargador(cargadorId, tiempo, razon));
        this.cargadorAsignado = cargadorId;

        if (this.tiempoInicioCarga < 0) {
            this.tiempoInicioCarga = tiempo;
        }
    }

    /**
     * Registra una preempción
     */
    public void registrarPreempcion(double tiempo, String razon) {
        if (!historialCargadores.isEmpty()) {
            HistorialCargador ultimo = historialCargadores.get(historialCargadores.size() - 1);
            ultimo.setTiempoFin(tiempo);
            ultimo.setFuePreemptado(true);
        }
        this.numeroPreempciones++;
        this.cargadorAsignado = null;
    }

    /**
     * Finaliza el uso del cargador actual
     */
    public void finalizarCargador(double tiempo) {
        if (!historialCargadores.isEmpty()) {
            HistorialCargador ultimo = historialCargadores.get(historialCargadores.size() - 1);
            if (ultimo.getTiempoFin() < 0) {
                ultimo.setTiempoFin(tiempo);
            }
        }
    }

    /**
     * Actualiza la energía cargada y el costo
     */
    public void actualizarCarga(double potenciaEntregada, double precioEnergia,
            double eficienciaCharger, double eficienciaVehicle,
            double resolucionTiempo, double costoOperacionCargador) {
        // Calculate actual energy charged considering both efficiencies
        double energiaCargadaReal = potenciaEntregada * resolucionTiempo * eficienciaCharger * eficienciaVehicle;
        this.energiaActual += energiaCargadaReal;

        // Calculate energy cost
        double costoEnergiaIntervalo = potenciaEntregada * resolucionTiempo * precioEnergia;
        this.costoEnergiaAcumulado += costoEnergiaIntervalo;

        // Calculate operational cost for the interval
        double costoOperacionIntervalo = resolucionTiempo * costoOperacionCargador;
        this.costoOperacionAcumulado += costoOperacionIntervalo;

        // Verificar si se completó la carga
        if (this.energiaActual >= this.vehiculoOriginal.getRequiredEnergy()) {
            this.cargaCompleta = true;
            this.estado = EstadoVehiculo.COMPLETADO;
        }
    }

    /**
     * Calcula el porcentaje de completitud de la carga
     */
    public double getPorcentajeCompletitud() {
        return Math.min(100.0, (energiaActual / vehiculoOriginal.getRequiredEnergy()) * 100.0);
    }

    /**
     * Verifica si el vehículo debe salir en el tiempo dado
     */
    public boolean debeSalir(double tiempo) {
        return tiempo >= vehiculoOriginal.getDepartureTime();
    }

    /**
     * Verifica si el vehículo ya ha llegado en el tiempo dado
     */
    public boolean haLlegado(double tiempo) {
        return tiempo >= vehiculoOriginal.getArrivalTime();
    }

    /**
     * Calcula el tiempo restante antes de la salida
     */
    public double getTiempoRestanteAntesSalida(double tiempoActual) {
        return Math.max(0, vehiculoOriginal.getDepartureTime() - tiempoActual);
    }

    /**
     * Calcula la energía restante por cargar
     */
    public double getEnergiaRestante() {
        return Math.max(0, vehiculoOriginal.getRequiredEnergy() - energiaActual);
    }

    // Getters y Setters
    public VehicleArrival getVehiculoOriginal() {
        return vehiculoOriginal;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public double getEnergiaActual() {
        return energiaActual;
    }

    public void setEnergiaActual(double energiaActual) {
        this.energiaActual = energiaActual;
    }

    public Integer getCargadorAsignado() {
        return cargadorAsignado;
    }

    public void setCargadorAsignado(Integer cargadorAsignado) {
        this.cargadorAsignado = cargadorAsignado;
    }

    public double getTiempoInicioEspera() {
        return tiempoInicioEspera;
    }

    public void setTiempoInicioEspera(double tiempoInicioEspera) {
        this.tiempoInicioEspera = tiempoInicioEspera;
    }

    public double getTiempoInicioCarga() {
        return tiempoInicioCarga;
    }

    public void setTiempoInicioCarga(double tiempoInicioCarga) {
        this.tiempoInicioCarga = tiempoInicioCarga;
    }

    public double getTiempoFinCarga() {
        return tiempoFinCarga;
    }

    public void setTiempoFinCarga(double tiempoFinCarga) {
        this.tiempoFinCarga = tiempoFinCarga;
    }

    public boolean isCargaCompleta() {
        return cargaCompleta;
    }

    public void setCargaCompleta(boolean cargaCompleta) {
        this.cargaCompleta = cargaCompleta;
    }

    public double getCostoAcumulado() {
        return costoEnergiaAcumulado + costoOperacionAcumulado;
    }

    public double getCostoEnergiaAcumulado() {
        return costoEnergiaAcumulado;
    }

    public void setCostoEnergiaAcumulado(double costoEnergiaAcumulado) {
        this.costoEnergiaAcumulado = costoEnergiaAcumulado;
    }

    public double getCostoOperacionAcumulado() {
        return costoOperacionAcumulado;
    }

    public void setCostoOperacionAcumulado(double costoOperacionAcumulado) {
        this.costoOperacionAcumulado = costoOperacionAcumulado;
    }

    public double getUrgenciaCarga() {
        return urgenciaCarga;
    }

    public void setUrgenciaCarga(double urgenciaCarga) {
        this.urgenciaCarga = urgenciaCarga;
    }

    public double getPrioridadNormalizada() {
        return prioridadNormalizada;
    }

    public void setPrioridadNormalizada(double prioridadNormalizada) {
        this.prioridadNormalizada = prioridadNormalizada;
    }

    public List<HistorialCargador> getHistorialCargadores() {
        return historialCargadores;
    }

    public int getNumeroPreempciones() {
        return numeroPreempciones;
    }

    public void setNumeroPreempciones(int numeroPreempciones) {
        this.numeroPreempciones = numeroPreempciones;
    }
}