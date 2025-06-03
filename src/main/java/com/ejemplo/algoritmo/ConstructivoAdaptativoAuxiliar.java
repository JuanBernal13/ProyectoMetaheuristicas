package com.ejemplo.algoritmo;

import com.ejemplo.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Métodos auxiliares para el algoritmo constructivo adaptativo
 */
public class ConstructivoAdaptativoAuxiliar {

    private final TestSystem testSystem;
    private final double resolucionTiempo;
    private final int limiteTransformador;
    private final double eficienciaGlobal;
    private ChargingLogger logger; // Referencia al logger (opcional)
    private List<VehiculoSimulacion> vehiculosSimulacionActivos; // Nueva referencia a los vehículos activos

    public ConstructivoAdaptativoAuxiliar(TestSystem testSystem) {
        this.testSystem = testSystem;
        this.resolucionTiempo = testSystem.getParkingConfig().getTimeResolution();
        this.limiteTransformador = testSystem.getParkingConfig().getTransformerLimit();
        this.eficienciaGlobal = testSystem.getParkingConfig().getEfficiency();
    }

    /**
     * Establece la referencia al logger
     */
    public void setLogger(ChargingLogger logger) {
        this.logger = logger;
    }

    public void setVehiculosSimulacionActivos(List<VehiculoSimulacion> vehiculosSimulacionActivos) {
        this.vehiculosSimulacionActivos = vehiculosSimulacionActivos;
    }

    /**
     * Calcula la prioridad multifactorial de un vehículo
     */
    public double calcularPrioridadVehiculo(VehiculoSimulacion vehiculo, double tiempoActual, double precioActual) {
        VehicleArrival v = vehiculo.getVehiculoOriginal();

        // Factor 1: Urgencia temporal (mayor peso a vehículos que salen pronto)
        double tiempoRestante = Math.max(0, v.getDepartureTime() - tiempoActual);
        double urgenciaTemporal = tiempoRestante > 0 ? 1.0 / tiempoRestante : 10.0;

        // Factor 2: Energía requerida normalizada
        double energiaNormalizada = vehiculo.getEnergiaRestante() / v.getBatteryCapacity();

        // Factor 3: Disposición a pagar
        double disposicionPago = v.getWillingnessToPay();

        // Factor 4: Prioridad original del vehículo (1=alta, 3=baja)
        double prioridadOriginal = 4.0 - v.getPriority(); // Invertir para que mayor sea mejor

        // Factor 5: Tiempo de espera
        double tiempoEspera = Math.max(0, tiempoActual - vehiculo.getTiempoInicioEspera());
        double penalizacionEspera = tiempoEspera * 0.1;

        // Combinar factores con pesos
        double prioridad = (urgenciaTemporal * 0.3) +
                (energiaNormalizada * 0.2) +
                (disposicionPago * 0.2) +
                (prioridadOriginal * 0.2) +
                (penalizacionEspera * 0.1);

        return prioridad;
    }

    /**
     * Calcula el porcentaje de completitud de carga de un vehículo
     */
    public double calcularPorcentajeCompletitud(VehiculoSimulacion vehiculo) {
        return vehiculo.getPorcentajeCompletitud();
    }

    /**
     * Estima el tiempo necesario para completar la carga de un vehículo
     */
    public double estimarTiempoCargaCompleta(VehiculoSimulacion vehiculo) {
        double energiaRestante = vehiculo.getEnergiaRestante();

        // Encontrar el cargador más rápido compatible
        double tasaMaxima = 0;
        for (Charger cargador : testSystem.getParkingConfig().getChargers()) {
            if (esCompatible(vehiculo, cargador)) {
                tasaMaxima = Math.max(tasaMaxima, cargador.getPower());
            }
        }

        if (tasaMaxima == 0)
            return Double.MAX_VALUE;

        // Considerar las limitaciones del vehículo
        VehicleArrival v = vehiculo.getVehiculoOriginal();
        double tasaEfectiva = Math.min(tasaMaxima, v.getMaxChargeRate());

        return energiaRestante / tasaEfectiva;
    }

    /**
     * Calcula puntuación para heurística reactiva al precio
     */
    public double calcularPuntuacionReactivaPrecio(VehiculoSimulacion vehiculo, double ventajaPrecio,
            double tiempoActual) {
        double urgencia = calcularUrgenciaCarga(vehiculo, tiempoActual);
        double sensibilidadPrecio = vehiculo.getVehiculoOriginal().getWillingnessToPay();

        return urgencia * (1.0 + ventajaPrecio * sensibilidadPrecio);
    }

    /**
     * Calcula la urgencia de carga de un vehículo
     */
    public double calcularUrgenciaCarga(VehiculoSimulacion vehiculo, double tiempoActual) {
        VehicleArrival v = vehiculo.getVehiculoOriginal();

        double tiempoRestante = Math.max(0, v.getDepartureTime() - tiempoActual);
        double energiaRestante = vehiculo.getEnergiaRestante();
        double tiempoMinimoNecesario = energiaRestante / v.getMaxChargeRate();

        if (tiempoRestante == 0)
            return 100.0; // Máxima urgencia
        if (tiempoMinimoNecesario == 0)
            return 0.0; // No necesita carga

        double ratio = tiempoMinimoNecesario / tiempoRestante;
        return Math.min(10.0, ratio); // Máximo 10.0
    }

    /**
     * Verifica si un vehículo es compatible con un cargador
     */
    public boolean esCompatible(VehiculoSimulacion vehiculo, Charger cargador) {
        String marcaVehiculo = vehiculo.getVehiculoOriginal().getBrand();

        // Extraer marca base (sin capacidad)
        String marcaBase = marcaVehiculo.split(" ")[0] + " " + marcaVehiculo.split(" ")[1];

        boolean compatible = false;
        for (String vehiculoCompatible : cargador.getCompatibleVehicles()) {
            if (marcaBase.toLowerCase().contains(vehiculoCompatible.toLowerCase()) ||
                    vehiculoCompatible.toLowerCase().contains(marcaBase.toLowerCase())) {
                compatible = true;
                break;
            }
        }

        // Log de compatibilidad si el logger está disponible
        if (logger != null) {
            double score = compatible ? evaluarCompatibilidadCargador(vehiculo, cargador) : 0.0;
            logger.logCompatibilityCheck(vehiculo, cargador, compatible, score);
        }

        return compatible;
    }

    /**
     * Evalúa qué tan compatible es un cargador para un vehículo específico
     */
    public double evaluarCompatibilidadCargador(VehiculoSimulacion vehiculo, Charger cargador) {
        if (!esCompatibleBasico(vehiculo, cargador))
            return 0.0;

        VehicleArrival v = vehiculo.getVehiculoOriginal();

        // Factor 1: Potencia efectiva (considerando limitaciones del vehículo)
        double potenciaEfectiva = Math.min(cargador.getPower(), v.getMaxChargeRate());
        double factorPotencia = potenciaEfectiva / v.getMaxChargeRate();

        // Factor 2: Eficiencia del cargador
        double factorEficiencia = cargador.getEfficiency();

        // Factor 3: Costo operativo (menor es mejor)
        double costoNormalizado = 1.0 / (1.0 + cargador.getOperationCostPerHour());

        double compatibilityScore = (factorPotencia * 0.5) + (factorEficiencia * 0.3) + (costoNormalizado * 0.2);

        // Log detallado de evaluación de compatibilidad si el logger está disponible
        if (logger != null) {
            logDetallesCompatibilidad(vehiculo, cargador, factorPotencia, factorEficiencia,
                    costoNormalizado, compatibilityScore);
        }

        return compatibilityScore;
    }

    /**
     * Verificación básica de compatibilidad sin logging
     */
    private boolean esCompatibleBasico(VehiculoSimulacion vehiculo, Charger cargador) {
        String marcaVehiculo = vehiculo.getVehiculoOriginal().getBrand();
        String marcaBase = marcaVehiculo.split(" ")[0] + " " + marcaVehiculo.split(" ")[1];

        for (String vehiculoCompatible : cargador.getCompatibleVehicles()) {
            if (marcaBase.toLowerCase().contains(vehiculoCompatible.toLowerCase()) ||
                    vehiculoCompatible.toLowerCase().contains(marcaBase.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Log detallado de evaluación de compatibilidad
     */
    private void logDetallesCompatibilidad(VehiculoSimulacion vehiculo, Charger cargador,
            double factorPotencia, double factorEficiencia,
            double costoNormalizado, double compatibilityScore) {
        // Este método solo se llama si hay logger disponible
        // Los detalles se loggean como DEBUG para no saturar la salida
    }

    /**
     * Encuentra el mejor cargador compatible para un vehículo con logging detallado
     */
    public Integer encontrarMejorCargadorCompatible(VehiculoSimulacion vehiculo, List<Integer> cargadoresDisponibles) {
        Integer mejorCargador = null;
        double mejorPuntuacion = -1;

        if (logger != null) {
            // Usar método público del logger
            String vehicleBrand = vehiculo.getVehiculoOriginal().getBrand();
            // Log de búsqueda sin usar método privado
        }

        for (Integer cargadorId : cargadoresDisponibles) {
            Charger cargador = obtenerCargadorPorId(cargadorId);
            if (cargador != null) {
                boolean compatible = esCompatible(vehiculo, cargador);

                if (compatible) {
                    double puntuacion = evaluarCompatibilidadCargador(vehiculo, cargador);

                    if (puntuacion > mejorPuntuacion) {
                        mejorPuntuacion = puntuacion;
                        mejorCargador = cargadorId;
                    }
                }
            }
        }

        return mejorCargador;
    }

    /**
     * Obtiene un cargador por su ID
     */
    public Charger obtenerCargadorPorId(int cargadorId) {
        return testSystem.getParkingConfig().getChargers().stream()
                .filter(c -> c.getChargerId() == cargadorId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtiene el precio de energía en un tiempo específico
     */
    public double obtenerPrecioEnergia(double tiempo) {
        // Buscar el precio más cercano al tiempo dado
        return testSystem.getEnergyPrices().stream()
                .min((p1, p2) -> Double.compare(
                        Math.abs(p1.getTime() - tiempo),
                        Math.abs(p2.getTime() - tiempo)))
                .map(EnergyPrice::getPrice)
                .orElse(50.0); // Precio por defecto
    }

    /**
     * Calcula el precio promedio de energía
     */
    public double calcularPrecioPromedio() {
        return testSystem.getEnergyPrices().stream()
                .mapToDouble(EnergyPrice::getPrice)
                .average()
                .orElse(50.0);
    }

    /**
     * Aplicar búsqueda local para mejorar una asignación
     */
    public AsignacionCandidata aplicarBusquedaLocal(AsignacionCandidata asignacionBase,
            List<VehiculoSimulacion> vehiculosEnEspera) {
        AsignacionCandidata mejorAsignacion = asignacionBase.copia();
        boolean mejoraEncontrada = true;
        int maxIteraciones = 10;
        int iteracion = 0;

        while (mejoraEncontrada && iteracion < maxIteraciones) {
            mejoraEncontrada = false;

            // Intentar intercambios de cargadores entre vehículos
            Map<Integer, Integer> asignaciones = new HashMap<>(mejorAsignacion.getAsignaciones());

            for (Integer vehiculo1 : asignaciones.keySet()) {
                for (Integer vehiculo2 : asignaciones.keySet()) {
                    if (!vehiculo1.equals(vehiculo2)) {
                        // Intercambiar cargadores
                        Integer cargador1 = asignaciones.get(vehiculo1);
                        Integer cargador2 = asignaciones.get(vehiculo2);

                        Map<Integer, Integer> nuevasAsignaciones = new HashMap<>(asignaciones);
                        nuevasAsignaciones.put(vehiculo1, cargador2);
                        nuevasAsignaciones.put(vehiculo2, cargador1);

                        AsignacionCandidata candidato = new AsignacionCandidata("LocalSearch_swap", nuevasAsignaciones);
                        // Aquí iría la evaluación completa
                        // Si es mejor, actualizar mejorAsignacion
                    }
                }
            }

            iteracion++;
        }

        return mejorAsignacion;
    }

    /**
     * Calcula métricas de evaluación para una asignación
     */
    public double calcularCostoEnergia(AsignacionCandidata asignacion, double tiempoActual) {
        double costoTotal = 0.0;
        double precioActual = obtenerPrecioEnergia(tiempoActual);

        for (Map.Entry<Integer, Integer> entry : asignacion.getAsignaciones().entrySet()) {
            Integer vehiculoId = entry.getKey();
            Integer cargadorId = entry.getValue();

            // Encontrar vehículo y cargador
            VehicleArrival vehiculo = testSystem.getArrivals().stream()
                    .filter(v -> v.getId() == vehiculoId)
                    .findFirst()
                    .orElse(null);

            Charger cargador = obtenerCargadorPorId(cargadorId);

            if (vehiculo != null && cargador != null) {
                // Estimar energía que se entregará
                double tiempoDisponible = Math.max(0, vehiculo.getDepartureTime() - tiempoActual);
                double potenciaEfectiva = Math.min(cargador.getPower(), vehiculo.getMaxChargeRate());
                double energiaEstimada = Math.min(vehiculo.getRequiredEnergy(),
                        potenciaEfectiva * tiempoDisponible);

                costoTotal += energiaEstimada * precioActual;
            }
        }

        return costoTotal;
    }

    /**
     * Calcula penalizaciones por retrasos
     */
    public double calcularPenalizacionRetraso(AsignacionCandidata asignacion, double tiempoActual) {
        double penalizacionTotal = 0.0;

        for (Integer vehiculoId : asignacion.getAsignaciones().keySet()) {
            VehicleArrival vehiculo = testSystem.getArrivals().stream()
                    .filter(v -> v.getId() == vehiculoId)
                    .findFirst()
                    .orElse(null);

            if (vehiculo != null) {
                double tiempoEspera = Math.max(0, tiempoActual - vehiculo.getArrivalTime());
                // Penalización cuadrática por tiempo de espera
                penalizacionTotal += tiempoEspera * tiempoEspera * 10.0;
            }
        }

        return penalizacionTotal;
    }

    /**
     * Calcula el valor de carga entregada
     */
    public double calcularValorCargaEntregada(AsignacionCandidata asignacion, double tiempoActual) {
        double valorTotal = 0.0;

        for (Map.Entry<Integer, Integer> entry : asignacion.getAsignaciones().entrySet()) {
            Integer vehiculoId = entry.getKey();
            Integer cargadorId = entry.getValue();

            // Find the VehiculoSimulacion instance (not VehicleArrival)
            // Use the injected list of active simulated vehicles
            VehiculoSimulacion vehiculoSim = this.vehiculosSimulacionActivos.stream()
                    .filter(v -> v.getVehiculoOriginal().getId() == vehiculoId)
                    .findFirst()
                    .orElse(null);

            Charger cargador = obtenerCargadorPorId(cargadorId);

            if (vehiculoSim != null && cargador != null) {
                VehicleArrival vehiculoOriginal = vehiculoSim.getVehiculoOriginal();
                double tiempoDisponible = Math.max(0, vehiculoOriginal.getDepartureTime() - tiempoActual);
                double potenciaEfectiva = Math.min(cargador.getPower(), vehiculoOriginal.getMaxChargeRate());
                double energiaEstimada = Math.min(vehiculoOriginal.getRequiredEnergy(),
                        potenciaEfectiva * tiempoDisponible);

                // Python's eficiencia_total = energia_entregada + bonificacion_prioridad
                // bonificacion_prioridad = 0.1 * self.prioridades[i] * self.E[i]
                double bonificacionPrioridad = 0.1 * vehiculoSim.getPrioridadNormalizada() * energiaEstimada;
                valorTotal += energiaEstimada + bonificacionPrioridad;
            }
        }

        return valorTotal;
    }
}