package com.ejemplo.algoritmo;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase para capturar y almacenar datos temporales durante la ejecución del
 * algoritmo
 * para generar gráficas de evolución
 */
public class DatosTemporales {

    // Clase para almacenar un punto de datos en el tiempo
    public static class PuntoTemporal {
        private final double tiempo;
        private final double cargaTransformador;
        private final int cargadoresOcupados;
        private final int vehiculosCargando;
        private final int vehiculosEsperando;
        private final int vehiculosCompletados;
        private final double energiaTotalEntregada;
        private final double porcentajeOcupacion;

        public PuntoTemporal(double tiempo, double cargaTransformador, int cargadoresOcupados,
                int vehiculosCargando, int vehiculosEsperando, int vehiculosCompletados,
                double energiaTotalEntregada, int totalCargadores) {
            this.tiempo = tiempo;
            this.cargaTransformador = cargaTransformador;
            this.cargadoresOcupados = cargadoresOcupados;
            this.vehiculosCargando = vehiculosCargando;
            this.vehiculosEsperando = vehiculosEsperando;
            this.vehiculosCompletados = vehiculosCompletados;
            this.energiaTotalEntregada = energiaTotalEntregada;
            this.porcentajeOcupacion = totalCargadores > 0 ? (cargadoresOcupados * 100.0) / totalCargadores : 0.0;
        }

        // Getters
        public double getTiempo() {
            return tiempo;
        }

        public double getCargaTransformador() {
            return cargaTransformador;
        }

        public int getCargadoresOcupados() {
            return cargadoresOcupados;
        }

        public int getVehiculosCargando() {
            return vehiculosCargando;
        }

        public int getVehiculosEsperando() {
            return vehiculosEsperando;
        }

        public int getVehiculosCompletados() {
            return vehiculosCompletados;
        }

        public double getEnergiaTotalEntregada() {
            return energiaTotalEntregada;
        }

        public double getPorcentajeOcupacion() {
            return porcentajeOcupacion;
        }
    }

    private final List<PuntoTemporal> puntosTempo;
    private final int limiteTransformador;
    private final int totalCargadores;

    public DatosTemporales(int limiteTransformador, int totalCargadores) {
        this.puntosTempo = new ArrayList<>();
        this.limiteTransformador = limiteTransformador;
        this.totalCargadores = totalCargadores;
    }

    /**
     * Registra un punto de datos en el tiempo actual
     */
    public void registrarPunto(double tiempo, double cargaTransformador, int cargadoresOcupados,
            int vehiculosCargando, int vehiculosEsperando, int vehiculosCompletados,
            double energiaTotalEntregada) {
        PuntoTemporal punto = new PuntoTemporal(tiempo, cargaTransformador, cargadoresOcupados,
                vehiculosCargando, vehiculosEsperando, vehiculosCompletados,
                energiaTotalEntregada, totalCargadores);
        puntosTempo.add(punto);
    }

    /**
     * Obtiene todos los puntos temporales registrados
     */
    public List<PuntoTemporal> getPuntosTemporales() {
        return new ArrayList<>(puntosTempo);
    }

    /**
     * Limpia todos los datos temporales
     */
    public void limpiar() {
        puntosTempo.clear();
    }

    /**
     * Obtiene estadísticas generales
     */
    public String generarResumen() {
        if (puntosTempo.isEmpty()) {
            return "No hay datos temporales disponibles.";
        }

        double tiempoTotal = puntosTempo.get(puntosTempo.size() - 1).getTiempo();
        double cargaMaxima = puntosTempo.stream().mapToDouble(PuntoTemporal::getCargaTransformador).max().orElse(0);
        double ocupacionMaxima = puntosTempo.stream().mapToDouble(PuntoTemporal::getPorcentajeOcupacion).max()
                .orElse(0);
        double energiaFinal = puntosTempo.get(puntosTempo.size() - 1).getEnergiaTotalEntregada();

        return String.format(
                "📊 RESUMEN DE EVOLUCIÓN TEMPORAL:\n" +
                        "   ⏰ Tiempo total simulado: %.2f h\n" +
                        "   ⚡ Carga máxima transformador: %.1f kW (límite: %d kW)\n" +
                        "   🔌 Ocupación máxima cargadores: %.1f%%\n" +
                        "   🔋 Energía total entregada: %.2f kWh\n" +
                        "   📈 Puntos de datos capturados: %d",
                tiempoTotal, cargaMaxima, limiteTransformador, ocupacionMaxima, energiaFinal, puntosTempo.size());
    }

    public int getLimiteTransformador() {
        return limiteTransformador;
    }

    public int getTotalCargadores() {
        return totalCargadores;
    }
}