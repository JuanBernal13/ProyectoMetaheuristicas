package com.ejemplo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Representa un cargador individual en la estación de carga.
 * Define las características técnicas y económicas del cargador.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Charger {

    /** Identificador único del cargador */
    private int chargerId;

    /** Potencia máxima del cargador (en kW) */
    private int power;

    /** Tipo de cargador ("AC" o "DC") */
    private String type;

    /** Costo de instalación del cargador (unidades monetarias) */
    private int installationCost;

    /**
     * Costo operativo del cargador por hora (unidades monetarias/hora).
     * Este costo es adicional al costo de energía.
     */
    private double operationCostPerHour;

    /**
     * Eficiencia del cargador (fracción).
     * Ej: 0.92 significa 92% de eficiencia en la conversión de energía.
     */
    private double efficiency;

    /** Lista de modelos de VE compatibles con este cargador */
    private List<String> compatibleVehicles;

    @Override
    public String toString() {
        return "Charger{" +
                "chargerId=" + chargerId +
                ", power=" + power + "kW" +
                ", type='" + type + '\'' +
                ", installationCost=" + installationCost + "EUR" +
                ", operationCostPerHour=" + operationCostPerHour + "EUR/h" +
                ", efficiency=" + (efficiency * 100) + "%" +
                ", compatibleVehicles=" + compatibleVehicles.size() + " modelos" +
                '}';
    }
}