package com.ejemplo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa la llegada de un vehículo eléctrico (VE) a la estación de carga.
 * Cada objeto contiene toda la información necesaria para la simulación de
 * carga.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleArrival {

    /** Identificador numérico único para cada vehículo */
    private int id;

    /** Hora (en horas) de llegada del vehículo a la estación */
    private double arrivalTime;

    /** Hora (en horas) en la que el vehículo planea salir */
    private double departureTime;

    /** Marca y modelo del VE (ej: "Renault Zoe 52kWh") */
    private String brand;

    /** Capacidad total de la batería del VE (en kWh) */
    private int batteryCapacity;

    /** Cantidad de energía (en kWh) que el VE necesita cargar */
    private double requiredEnergy;

    /** Tasa de carga mínima aceptable para el VE (en kW) */
    private double minChargeRate;

    /** Tasa de carga máxima que la batería del VE puede aceptar (en kW) */
    private int maxChargeRate;

    /** Tasa de carga máxima del VE usando un cargador AC (en kW) */
    private double acChargeRate;

    /** Tasa de carga máxima del VE usando un cargador DC (en kW) */
    private int dcChargeRate;

    /**
     * Nivel de prioridad del VE.
     * Un número menor usualmente indica mayor prioridad.
     */
    private int priority;

    /**
     * Factor multiplicador de la disposición a pagar del usuario respecto al precio
     * base de la energía.
     * Ej: 1.1 representa un 10% más del precio base.
     */
    private double willingnessToPay;

    /**
     * Eficiencia de carga del VE.
     * Porcentaje de energía suministrada que se almacena efectivamente en la
     * batería.
     * Ej: 0.88 significa 88% de eficiencia.
     */
    private double efficiency;

    @Override
    public String toString() {
        return "VehicleArrival{" +
                "id=" + id +
                ", arrivalTime=" + arrivalTime + "h" +
                ", departureTime=" + departureTime + "h" +
                ", brand='" + brand + '\'' +
                ", batteryCapacity=" + batteryCapacity + "kWh" +
                ", requiredEnergy=" + requiredEnergy + "kWh" +
                ", priority=" + priority +
                ", efficiency=" + (efficiency * 100) + "%" +
                '}';
    }
}