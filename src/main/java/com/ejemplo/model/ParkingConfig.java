package com.ejemplo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Representa la configuración del estacionamiento/estación de carga.
 * Contiene toda la información sobre la infraestructura de carga disponible.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingConfig {

    /** Número total de plazas de aparcamiento/carga */
    private int nSpots;

    /** Lista de cargadores disponibles en la estación */
    private List<Charger> chargers;

    /** Límite máximo de potencia del transformador de la estación (en kW) */
    private int transformerLimit;

    /**
     * Eficiencia general del sistema de la estación (fracción).
     * Representa las pérdidas del sistema eléctrico completo.
     */
    private double efficiency;

    /**
     * Intervalo de tiempo de la simulación (en horas).
     * Ej: 0.25 h = 15 minutos
     */
    private double timeResolution;

    /** Restricciones de la red eléctrica */
    private GridConstraints gridConstraints;

    @Override
    public String toString() {
        return "ParkingConfig{" +
                "nSpots=" + nSpots +
                ", chargers=" + chargers.size() + " cargadores" +
                ", transformerLimit=" + transformerLimit + "kW" +
                ", efficiency=" + (efficiency * 100) + "%" +
                ", timeResolution=" + timeResolution + "h" +
                ", gridConstraints=" + gridConstraints +
                '}';
    }
}