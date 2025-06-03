package com.ejemplo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Clase principal que representa todo el sistema de prueba de simulación de
 * carga de vehículos eléctricos. Contiene todos los datos necesarios para
 * ejecutar una simulación completa del sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSystem {

    /** Identificador numérico único para el conjunto de datos de prueba */
    private int testNumber;

    /** Lista de precios de energía a lo largo del tiempo */
    private List<EnergyPrice> energyPrices;

    /** Lista de llegadas de vehículos eléctricos */
    private List<VehicleArrival> arrivals;

    /** Configuración de la estación de carga */
    private ParkingConfig parkingConfig;

    /** Lista de características de modelos de VE */
    private List<CarBrand> carBrands;

    /** Diccionario de tipos de cargadores */
    private Map<String, ChargerType> chargerTypes;

    @Override
    public String toString() {
        return "TestSystem{" +
                "testNumber=" + testNumber +
                ", energyPrices=" + energyPrices.size() + " puntos" +
                ", arrivals=" + arrivals.size() + " vehículos" +
                ", parkingConfig=" + parkingConfig +
                ", carBrands=" + carBrands.size() + " marcas" +
                ", chargerTypes=" + chargerTypes.size() + " tipos" +
                '}';
    }
}