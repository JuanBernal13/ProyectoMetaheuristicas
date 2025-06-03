package com.ejemplo.mapper;

import com.ejemplo.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper para convertir archivos JSON del sistema de carga de vehículos
 * eléctricos a objetos Java.
 * Convierte la estructura JSON completa incluyendo precios de energía, llegadas
 * de vehículos,
 * configuración de estacionamiento, marcas de vehículos y tipos de cargadores.
 */
public class JsonMapper {
    private final ObjectMapper objectMapper;

    public JsonMapper() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Mapea el archivo JSON completo a un objeto TestSystem.
     * Procesa todas las secciones del JSON: test_number, energy_prices, arrivals,
     * parking_config, car_brands y charger_types.
     * 
     * @param jsonFilePath Ruta al archivo JSON (ej: "test_system_1.json")
     * @return TestSystem completamente poblado con todos los datos del JSON
     * @throws IOException Si hay problemas leyendo el archivo JSON
     */
    public TestSystem mapJsonToTestSystem(String jsonFilePath) throws IOException {
        File jsonFile = new File(jsonFilePath);

        // Verificar que el archivo existe
        if (!jsonFile.exists()) {
            throw new IOException("El archivo JSON no existe: " + jsonFilePath);
        }

        // Verificar que el archivo no está vacío
        if (jsonFile.length() == 0) {
            throw new IOException("El archivo JSON está vacío: " + jsonFilePath);
        }

        JsonNode rootNode = objectMapper.readTree(jsonFile);

        // Verificar que el JSON se parseó correctamente
        if (rootNode == null || rootNode.isNull()) {
            throw new IOException("El archivo JSON no se pudo parsear correctamente: " + jsonFilePath);
        }

        TestSystem testSystem = new TestSystem();

        // Mapear test_number con validación
        JsonNode testNumberNode = rootNode.get("test_number");
        if (testNumberNode == null) {
            throw new IOException("Campo 'test_number' no encontrado en el JSON: " + jsonFilePath);
        }
        testSystem.setTestNumber(testNumberNode.asInt());

        // Mapear energy_prices con validación
        JsonNode energyPricesNode = rootNode.get("energy_prices");
        if (energyPricesNode == null) {
            throw new IOException("Campo 'energy_prices' no encontrado en el JSON: " + jsonFilePath);
        }
        testSystem.setEnergyPrices(mapEnergyPrices(energyPricesNode));

        // Mapear arrivals con validación
        JsonNode arrivalsNode = rootNode.get("arrivals");
        if (arrivalsNode == null) {
            throw new IOException("Campo 'arrivals' no encontrado en el JSON: " + jsonFilePath);
        }
        testSystem.setArrivals(mapVehicleArrivals(arrivalsNode));

        // Mapear parking_config con validación
        JsonNode parkingConfigNode = rootNode.get("parking_config");
        if (parkingConfigNode == null) {
            throw new IOException("Campo 'parking_config' no encontrado en el JSON: " + jsonFilePath);
        }
        testSystem.setParkingConfig(mapParkingConfig(parkingConfigNode));

        // Mapear car_brands con validación
        JsonNode carBrandsNode = rootNode.get("car_brands");
        if (carBrandsNode == null) {
            throw new IOException("Campo 'car_brands' no encontrado en el JSON: " + jsonFilePath);
        }
        testSystem.setCarBrands(mapCarBrands(carBrandsNode));

        // Mapear charger_types con validación
        JsonNode chargerTypesNode = rootNode.get("charger_types");
        if (chargerTypesNode == null) {
            throw new IOException("Campo 'charger_types' no encontrado en el JSON: " + jsonFilePath);
        }
        testSystem.setChargerTypes(mapChargerTypes(chargerTypesNode));

        return testSystem;
    }

    /**
     * Mapea la lista de precios de energía.
     * Cada elemento contiene un tiempo y un precio asociado.
     */
    private List<EnergyPrice> mapEnergyPrices(JsonNode energyPricesNode) {
        List<EnergyPrice> energyPrices = new ArrayList<>();

        for (JsonNode priceNode : energyPricesNode) {
            EnergyPrice energyPrice = new EnergyPrice();
            energyPrice.setTime(priceNode.get("time").asDouble());
            energyPrice.setPrice(priceNode.get("price").asDouble() / 100.0);
            energyPrices.add(energyPrice);
        }

        return energyPrices;
    }

    /**
     * Mapea la lista de llegadas de vehículos.
     * Cada llegada contiene toda la información necesaria sobre un vehículo:
     * tiempos, capacidades, tasas de carga, prioridad, etc.
     */
    private List<VehicleArrival> mapVehicleArrivals(JsonNode arrivalsNode) {
        List<VehicleArrival> arrivals = new ArrayList<>();

        for (JsonNode arrivalNode : arrivalsNode) {
            VehicleArrival arrival = new VehicleArrival();
            arrival.setId(arrivalNode.get("id").asInt());
            arrival.setArrivalTime(arrivalNode.get("arrival_time").asDouble());
            arrival.setDepartureTime(arrivalNode.get("departure_time").asDouble());
            arrival.setBrand(arrivalNode.get("brand").asText());
            arrival.setBatteryCapacity(arrivalNode.get("battery_capacity").asInt());
            arrival.setRequiredEnergy(arrivalNode.get("required_energy").asDouble());
            arrival.setMinChargeRate(arrivalNode.get("min_charge_rate").asDouble());
            arrival.setMaxChargeRate(arrivalNode.get("max_charge_rate").asInt()); // int según JSON
            arrival.setAcChargeRate(arrivalNode.get("ac_charge_rate").asDouble());
            arrival.setDcChargeRate(arrivalNode.get("dc_charge_rate").asInt()); // int según JSON
            arrival.setPriority(arrivalNode.get("priority").asInt());
            arrival.setWillingnessToPay(arrivalNode.get("willingness_to_pay").asDouble());
            arrival.setEfficiency(arrivalNode.get("efficiency").asDouble());
            arrivals.add(arrival);
        }

        return arrivals;
    }

    /**
     * Mapea la configuración del estacionamiento.
     * Incluye número de espacios, límite del transformador, eficiencia,
     * lista de cargadores y restricciones de la red.
     */
    private ParkingConfig mapParkingConfig(JsonNode parkingConfigNode) {
        ParkingConfig parkingConfig = new ParkingConfig();

        parkingConfig.setNSpots(parkingConfigNode.get("n_spots").asInt());
        parkingConfig.setTransformerLimit(parkingConfigNode.get("transformer_limit").asInt()); // int según JSON
        parkingConfig.setEfficiency(parkingConfigNode.get("efficiency").asDouble());
        parkingConfig.setTimeResolution(parkingConfigNode.get("time_resolution").asDouble());

        // Mapear chargers
        parkingConfig.setChargers(mapChargers(parkingConfigNode.get("chargers")));

        // Mapear grid_constraints
        parkingConfig.setGridConstraints(mapGridConstraints(parkingConfigNode.get("grid_constraints")));

        return parkingConfig;
    }

    /**
     * Mapea la lista de cargadores disponibles en el estacionamiento.
     * Cada cargador tiene ID, potencia, tipo, costos y vehículos compatibles.
     */
    private List<Charger> mapChargers(JsonNode chargersNode) {
        List<Charger> chargers = new ArrayList<>();

        for (JsonNode chargerNode : chargersNode) {
            Charger charger = new Charger();
            charger.setChargerId(chargerNode.get("charger_id").asInt());
            charger.setPower(chargerNode.get("power").asInt()); // int según JSON
            charger.setType(chargerNode.get("type").asText());
            charger.setInstallationCost(chargerNode.get("installation_cost").asInt()); // int según JSON
            charger.setOperationCostPerHour(chargerNode.get("operation_cost_per_hour").asDouble());
            charger.setEfficiency(chargerNode.get("efficiency").asDouble());

            // Mapear compatible_vehicles
            List<String> compatibleVehicles = new ArrayList<>();
            for (JsonNode vehicleNode : chargerNode.get("compatible_vehicles")) {
                compatibleVehicles.add(vehicleNode.asText());
            }
            charger.setCompatibleVehicles(compatibleVehicles);

            chargers.add(charger);
        }

        return chargers;
    }

    /**
     * Mapea las restricciones de la red eléctrica.
     * Incluye límites de potencia, caída de voltaje, factor de potencia
     * y eficiencia del sistema (que puede ser null).
     */
    private GridConstraints mapGridConstraints(JsonNode gridConstraintsNode) {
        GridConstraints gridConstraints = new GridConstraints();

        gridConstraints.setMaxPowerPerPhase(gridConstraintsNode.get("max_power_per_phase").asDouble());
        gridConstraints.setVoltageDropLimit(gridConstraintsNode.get("voltage_drop_limit").asDouble());
        gridConstraints.setPowerFactorLimit(gridConstraintsNode.get("power_factor_limit").asDouble());

        // system_efficiency puede ser null en el JSON
        JsonNode systemEfficiencyNode = gridConstraintsNode.get("system_efficiency");
        if (systemEfficiencyNode != null && !systemEfficiencyNode.isNull()) {
            gridConstraints.setSystemEfficiency(systemEfficiencyNode.asDouble());
        }

        return gridConstraints;
    }

    /**
     * Mapea la lista de marcas de vehículos.
     * NOTA: car_brands es un array de arrays en el JSON, donde cada elemento es:
     * [nombre_modelo, capacidad_bateria, soc_minimo, carga_ac_max, carga_dc_max,
     * eficiencia]
     */
    private List<CarBrand> mapCarBrands(JsonNode carBrandsNode) {
        List<CarBrand> carBrands = new ArrayList<>();

        for (JsonNode brandNode : carBrandsNode) {
            CarBrand carBrand = new CarBrand();
            carBrand.setModelName(brandNode.get(0).asText()); // [0] = nombre
            carBrand.setBatteryCapacity(brandNode.get(1).asInt()); // [1] = capacidad
            carBrand.setMinSocArrival(brandNode.get(2).asDouble()); // [2] = SoC mínimo
            carBrand.setMaxAcChargeRate(brandNode.get(3).asDouble()); // [3] = AC max
            carBrand.setMaxDcChargeRate(brandNode.get(4).asInt()); // [4] = DC max (int según JSON)
            carBrand.setChargingEfficiency(brandNode.get(5).asDouble()); // [5] = eficiencia
            carBrands.add(carBrand);
        }

        return carBrands;
    }

    /**
     * Mapea el diccionario de tipos de cargadores.
     * Cada tipo tiene una clave (ej: "AC_7kW") y sus características técnicas y
     * económicas.
     */
    private Map<String, ChargerType> mapChargerTypes(JsonNode chargerTypesNode) {
        Map<String, ChargerType> chargerTypes = new HashMap<>();

        chargerTypesNode.fieldNames().forEachRemaining(typeName -> {
            JsonNode typeNode = chargerTypesNode.get(typeName);
            ChargerType chargerType = new ChargerType();

            chargerType.setPower(typeNode.get("power").asInt()); // int según JSON
            chargerType.setType(typeNode.get("type").asText());
            chargerType.setInstallationCost(typeNode.get("installation_cost").asInt()); // int según JSON
            chargerType.setOperationCost(typeNode.get("operation_cost").asDouble());
            chargerType.setEfficiency(typeNode.get("efficiency").asDouble());

            // Mapear compatible_vehicles
            List<String> compatibleVehicles = new ArrayList<>();
            for (JsonNode vehicleNode : typeNode.get("compatible_vehicles")) {
                compatibleVehicles.add(vehicleNode.asText());
            }
            chargerType.setCompatibleVehicles(compatibleVehicles);

            chargerTypes.put(typeName, chargerType);
        });

        return chargerTypes;
    }
}