package com.ejemplo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa las restricciones de la red eléctrica del estacionamiento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GridConstraints {
    private double maxPowerPerPhase; // Potencia máxima permitida por fase (kW)
    private double voltageDropLimit; // Límite máximo de caída de voltaje (fracción)
    private double powerFactorLimit; // Límite mínimo del factor de potencia
    private Double systemEfficiency; // Eficiencia general del sistema (puede ser null)

    @Override
    public String toString() {
        return "GridConstraints{" +
                "maxPowerPerPhase=" + maxPowerPerPhase +
                ", voltageDropLimit=" + voltageDropLimit +
                ", powerFactorLimit=" + powerFactorLimit +
                ", systemEfficiency=" + systemEfficiency +
                '}';
    }
}