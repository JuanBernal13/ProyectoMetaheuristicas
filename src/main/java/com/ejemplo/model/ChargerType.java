package com.ejemplo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Representa un tipo de cargador con sus características.
 * Diccionario de referencia para tipos de cargadores disponibles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargerType {

    /** Potencia máxima del cargador (en kW) */
    private int power;

    /** Tipo de cargador ("AC" o "DC") */
    private String type;

    /** Costo de instalación (unidades monetarias) */
    private int installationCost;

    /** Costo operativo por hora (unidades monetarias/hora) */
    private double operationCost;

    /** Eficiencia del cargador (fracción) */
    private double efficiency;

    /** Lista de modelos de VE compatibles */
    private List<String> compatibleVehicles;

    @Override
    public String toString() {
        return "ChargerType{" +
                "power=" + power + "kW" +
                ", type='" + type + '\'' +
                ", installationCost=" + installationCost + "EUR" +
                ", operationCost=" + operationCost + "EUR/h" +
                ", efficiency=" + (efficiency * 100) + "%" +
                ", compatibleVehicles=" + compatibleVehicles.size() + " modelos" +
                '}';
    }
}