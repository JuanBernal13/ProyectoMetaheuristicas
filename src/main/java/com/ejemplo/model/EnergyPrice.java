package com.ejemplo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un precio de energía en un momento específico del tiempo.
 * Define los precios de la energía a lo largo del tiempo para la simulación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnergyPrice {

    /**
     * Momento en el tiempo (generalmente en horas desde el inicio de la
     * simulación).
     * Por ejemplo: 1.25 h representa 1 hora y 15 minutos.
     */
    private double time;

    /**
     * Costo de la energía en ese momento específico.
     * Generalmente en unidades monetarias por MWh o kWh (ej: EUR/MWh).
     */
    private double price;

    @Override
    public String toString() {
        return "EnergyPrice{" +
                "time=" + time + " h" +
                ", price=" + price + " EUR/MWh" +
                '}';
    }
}