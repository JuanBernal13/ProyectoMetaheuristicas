package com.ejemplo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa las características de una marca/modelo de vehículo eléctrico.
 * Lista de referencia con características de modelos de VE para la simulación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarBrand {

    /** Nombre del modelo (String) */
    private String modelName;

    /** Capacidad de la batería (kWh) */
    private int batteryCapacity;

    /**
     * Estado de carga (SoC) mínimo/típico de llegada (fracción).
     * Ej: 0.2 = 20% de carga al llegar
     */
    private double minSocArrival;

    /** Tasa máxima de carga AC (kW) */
    private double maxAcChargeRate;

    /** Tasa máxima de carga DC (kW) */
    private int maxDcChargeRate;

    /**
     * Eficiencia de carga del vehículo (fracción).
     * Ej: 0.85 = 85% de eficiencia en el proceso de carga
     */
    private double chargingEfficiency;

    @Override
    public String toString() {
        return "CarBrand{" +
                "modelName='" + modelName + '\'' +
                ", batteryCapacity=" + batteryCapacity + "kWh" +
                ", minSocArrival=" + (minSocArrival * 100) + "%" +
                ", maxAcChargeRate=" + maxAcChargeRate + "kW" +
                ", maxDcChargeRate=" + maxDcChargeRate + "kW" +
                ", chargingEfficiency=" + (chargingEfficiency * 100) + "%" +
                '}';
    }
}