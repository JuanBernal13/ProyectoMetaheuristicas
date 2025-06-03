package com.ejemplo.algoritmo;

import com.ejemplo.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Algoritmo Constructivo Adaptativo para la programación de carga de vehículos
 * eléctricos.
 * Implementa múltiples heurísticas y selecciona la mejor basándose en
 * evaluación multiobjetivo.
 * 
 * Objetivos:
 * 1. Minimizar el Costo Total de Operación (energía + penalizaciones por
 * retrasos)
 * 2. Maximizar el Valor de Carga Entregado al Cliente
 */
public class ConstructivoAdaptativo {

    private final TestSystem testSystem;
    private final double resolucionTiempo;
    private final int limiteTransformador;
    private final double eficienciaGlobal;
    private final ConstructivoAdaptativoAuxiliar auxiliar;

    // Sistema de logging
    private final ChargingLogger logger;

    // Sistema de captura de datos temporales para gráficas
    private final DatosTemporales datosTemporales;

    // Configuración de pesos para evaluación multiobjetivo
    private final Map<String, Double> pesosEvaluacion;

    // Estado de la simulación
    private double tiempoActual;
    private List<VehiculoSimulacion> vehiculosActivos;
    private Map<Integer, Boolean> cargadoresDisponibles;
    private List<EnergyPrice> preciosEnergia;
    private int indicePrecioActual;

    // Resultados y estadísticas
    private SolucionConstructiva mejorSolucion;
    private Map<String, Integer> contadorHeuristicas;
    private List<ResultadoIteracion> historialIteraciones;

    public ConstructivoAdaptativo(TestSystem testSystem) {
        this.testSystem = testSystem;
        this.resolucionTiempo = testSystem.getParkingConfig().getTimeResolution();
        this.limiteTransformador = testSystem.getParkingConfig().getTransformerLimit();
        this.eficienciaGlobal = testSystem.getParkingConfig().getEfficiency();
        this.auxiliar = new ConstructivoAdaptativoAuxiliar(testSystem);

        // Inicializar sistema de logging (salida por consola activada)
        this.logger = new ChargingLogger(true);

        // Inicializar sistema de captura de datos temporales
        this.datosTemporales = new DatosTemporales(
                testSystem.getParkingConfig().getTransformerLimit(),
                testSystem.getParkingConfig().getChargers().size());

        // Configurar el logger en el auxiliar
        this.auxiliar.setLogger(this.logger);

        // Inicializar pesos para evaluación multiobjetivo
        this.pesosEvaluacion = new HashMap<>();
        pesosEvaluacion.put("costo_energia", -1.0); // Minimizar
        pesosEvaluacion.put("valor_carga_entregada", 1.5); // Maximizar
        pesosEvaluacion.put("equidad", 0.8); // Maximizar
        pesosEvaluacion.put("eficiencia_cargadores", 1.0); // Maximizar

        this.contadorHeuristicas = new HashMap<>();
        this.historialIteraciones = new ArrayList<>();

        inicializarContadorHeuristicas();
    }

    /**
     * Ejecuta el algoritmo constructivo adaptativo completo
     */
    public SolucionConstructiva ejecutar() {
        System.out.println("🔄 Iniciando Algoritmo Constructivo Adaptativo...");
        long tiempoInicio = System.currentTimeMillis();

        inicializar();

        while (!todosVehiculosProcesados()) {
            // Obtener vehículos que han llegado pero no han sido asignados
            List<VehiculoSimulacion> vehiculosEnEspera = obtenerVehiculosEnEspera();
            List<Integer> cargadoresLibres = obtenerCargadoresDisponibles();

            // Log de inicio de iteración
            logger.logIterationStart(tiempoActual, vehiculosEnEspera.size(), cargadoresLibres.size());

            if (!vehiculosEnEspera.isEmpty() && hayCargadoresDisponibles()) {
                // Generar soluciones candidatas usando diferentes heurísticas
                List<AsignacionCandidata> candidatos = generarSolucionesCandidatas(vehiculosEnEspera);

                // Log de generación de heurísticas
                logger.logHeuristicsGeneration(candidatos);

                // Seleccionar la mejor asignación
                long evaluationStart = System.currentTimeMillis();
                AsignacionCandidata mejorAsignacion = seleccionarMejorAsignacion(candidatos);
                long evaluationTime = System.currentTimeMillis() - evaluationStart;

                // Log de selección de heurística
                logger.logHeuristicSelection(mejorAsignacion, evaluationTime);

                // Aplicar la asignación seleccionada
                aplicarAsignacion(mejorAsignacion);

                // Registrar resultado de la iteración
                registrarIteracion(mejorAsignacion);
            }

            // Avanzar el tiempo y actualizar estado
            avanzarTiempo();

            // Log de resumen de iteración
            logIterationSummary();
        }

        // Finalizar y construir solución
        long tiempoFin = System.currentTimeMillis();
        finalizarSolucion(tiempoFin - tiempoInicio);

        // Log de estadísticas finales
        logger.logFinalStatistics(
                mejorSolucion.getPorcentajeCargaEntregado(),
                mejorSolucion.getEnergiaTotalEntregada(),
                mejorSolucion.getEnergiaTotalRequerida());

        System.out.println("✅ Algoritmo Constructivo Adaptativo completado!");
        return mejorSolucion;
    }

    /**
     * Genera múltiples soluciones candidatas usando diferentes heurísticas
     */
    private List<AsignacionCandidata> generarSolucionesCandidatas(List<VehiculoSimulacion> vehiculosEnEspera) {
        List<AsignacionCandidata> candidatos = new ArrayList<>();

        // Heurística 1: EDF - Earliest Deadline First
        candidatos.add(generarAsignacionEDF(vehiculosEnEspera));

        // Heurística 2: Highest Priority
        candidatos.add(generarAsignacionPrioridadAlta(vehiculosEnEspera));

        // Heurística 3: Fairness - Low Completion
        candidatos.add(generarAsignacionEquidad(vehiculosEnEspera));

        // Heurística 4: SJF - Shortest Job First
        candidatos.add(generarAsignacionTrabajoCorto(vehiculosEnEspera));

        // Heurística 5: Price Reactive
        candidatos.add(generarAsignacionReactivaPrecio(vehiculosEnEspera));

        // Heurística 6: Local Search (mejora de la mejor hasta ahora)
        candidatos.add(generarAsignacionBusquedaLocal(vehiculosEnEspera));

        // Heurística 7: Exploration (aleatoria con probabilidad baja)
        if (Math.random() < 0.1) { // 10% probabilidad de exploración
            candidatos.add(generarAsignacionExploratoria(vehiculosEnEspera));
        }

        return candidatos;
    }

    /**
     * Heurística 1: EDF - Earliest Deadline First
     * Prioriza vehículos con tiempo de salida más temprano
     */
    private AsignacionCandidata generarAsignacionEDF(List<VehiculoSimulacion> vehiculosEnEspera) {
        List<VehiculoSimulacion> vehiculosOrdenados = vehiculosEnEspera.stream()
                .sorted((v1, v2) -> {
                    int comparacion = Double.compare(v1.getVehiculoOriginal().getDepartureTime(),
                            v2.getVehiculoOriginal().getDepartureTime());
                    if (comparacion == 0) {
                        // Desempate por tiempo de llegada (FIFO)
                        return Double.compare(v1.getVehiculoOriginal().getArrivalTime(),
                                v2.getVehiculoOriginal().getArrivalTime());
                    }
                    return comparacion;
                })
                .collect(Collectors.toList());

        return crearAsignacionOptima(vehiculosOrdenados, "EDF");
    }

    /**
     * Heurística 2: Highest Priority
     * Utiliza función de prioridad multifactorial
     */
    private AsignacionCandidata generarAsignacionPrioridadAlta(List<VehiculoSimulacion> vehiculosEnEspera) {
        double precioActual = auxiliar.obtenerPrecioEnergia(tiempoActual);

        List<VehiculoSimulacion> vehiculosOrdenados = vehiculosEnEspera.stream()
                .sorted((v1, v2) -> Double.compare(
                        auxiliar.calcularPrioridadVehiculo(v2, tiempoActual, precioActual),
                        auxiliar.calcularPrioridadVehiculo(v1, tiempoActual, precioActual)))
                .collect(Collectors.toList());

        return crearAsignacionOptima(vehiculosOrdenados, "HighestPriority");
    }

    /**
     * Heurística 3: Fairness - Low Completion
     * Prioriza vehículos con menor porcentaje de carga completada
     */
    private AsignacionCandidata generarAsignacionEquidad(List<VehiculoSimulacion> vehiculosEnEspera) {
        List<VehiculoSimulacion> vehiculosOrdenados = vehiculosEnEspera.stream()
                .sorted((v1, v2) -> Double.compare(
                        auxiliar.calcularPorcentajeCompletitud(v1),
                        auxiliar.calcularPorcentajeCompletitud(v2)))
                .collect(Collectors.toList());

        return crearAsignacionOptima(vehiculosOrdenados, "Fairness");
    }

    /**
     * Heurística 4: SJF - Shortest Job First
     * Prioriza vehículos que tardarán menos en completar la carga
     */
    private AsignacionCandidata generarAsignacionTrabajoCorto(List<VehiculoSimulacion> vehiculosEnEspera) {
        List<VehiculoSimulacion> vehiculosOrdenados = vehiculosEnEspera.stream()
                .sorted((v1, v2) -> Double.compare(
                        auxiliar.estimarTiempoCargaCompleta(v1),
                        auxiliar.estimarTiempoCargaCompleta(v2)))
                .collect(Collectors.toList());

        return crearAsignacionOptima(vehiculosOrdenados, "SJF");
    }

    /**
     * Heurística 5: Price Reactive
     * Considera precios de energía y urgencia de carga
     */
    private AsignacionCandidata generarAsignacionReactivaPrecio(List<VehiculoSimulacion> vehiculosEnEspera) {
        double precioActual = auxiliar.obtenerPrecioEnergia(tiempoActual);
        double precioPromedio = auxiliar.calcularPrecioPromedio();
        double ventajaPrecio = Math.max(0, (precioPromedio - precioActual) / precioPromedio);

        List<VehiculoSimulacion> vehiculosOrdenados = vehiculosEnEspera.stream()
                .sorted((v1, v2) -> Double.compare(
                        auxiliar.calcularPuntuacionReactivaPrecio(v2, ventajaPrecio, tiempoActual),
                        auxiliar.calcularPuntuacionReactivaPrecio(v1, ventajaPrecio, tiempoActual)))
                .collect(Collectors.toList());

        return crearAsignacionOptima(vehiculosOrdenados, "PriceReactive");
    }

    /**
     * Heurística 6: Local Search
     * Mejora la mejor solución encontrada hasta ahora
     */
    private AsignacionCandidata generarAsignacionBusquedaLocal(List<VehiculoSimulacion> vehiculosEnEspera) {
        AsignacionCandidata asignacionBase = generarAsignacionPrioridadAlta(vehiculosEnEspera);
        AsignacionCandidata asignacionMejorada = auxiliar.aplicarBusquedaLocal(asignacionBase, vehiculosEnEspera);
        asignacionMejorada.setNombreHeuristica("LocalSearch");
        return asignacionMejorada;
    }

    /**
     * Heurística 7: Exploration (Aleatoria)
     * Asignación aleatoria para exploración
     */
    private AsignacionCandidata generarAsignacionExploratoria(List<VehiculoSimulacion> vehiculosEnEspera) {
        List<VehiculoSimulacion> vehiculosAleatorios = new ArrayList<>(vehiculosEnEspera);
        Collections.shuffle(vehiculosAleatorios);
        return crearAsignacionOptima(vehiculosAleatorios, "Exploration");
    }

    /**
     * Crea asignación óptima respetando restricciones
     */
    private AsignacionCandidata crearAsignacionOptima(List<VehiculoSimulacion> vehiculosOrdenados,
            String nombreHeuristica) {
        AsignacionCandidata asignacion = new AsignacionCandidata();
        asignacion.setNombreHeuristica(nombreHeuristica);

        List<Integer> cargadoresLibres = obtenerCargadoresDisponibles();
        Map<Integer, Integer> asignaciones = new HashMap<>();

        int numAsignaciones = Math.min(vehiculosOrdenados.size(), cargadoresLibres.size());

        for (int i = 0; i < numAsignaciones; i++) {
            VehiculoSimulacion vehiculo = vehiculosOrdenados.get(i);

            // Encontrar el mejor cargador compatible para este vehículo
            Integer mejorCargador = auxiliar.encontrarMejorCargadorCompatible(vehiculo, cargadoresLibres);

            if (mejorCargador != null) {
                asignaciones.put(vehiculo.getVehiculoOriginal().getId(), mejorCargador);
                cargadoresLibres.remove(mejorCargador);
            }
        }

        asignacion.setAsignaciones(asignaciones);
        return asignacion;
    }

    /**
     * Evalúa y selecciona la mejor asignación candidata
     */
    private AsignacionCandidata seleccionarMejorAsignacion(List<AsignacionCandidata> candidatos) {
        AsignacionCandidata mejorCandidato = null;
        double mejorPuntuacion = Double.NEGATIVE_INFINITY;

        for (AsignacionCandidata candidato : candidatos) {
            double puntuacion = evaluarAsignacion(candidato);
            candidato.setValorEvaluacion(puntuacion);

            if (puntuacion > mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejorCandidato = candidato;
            }
        }

        if (mejorCandidato != null) {
            contadorHeuristicas.put(mejorCandidato.getNombreHeuristica(),
                    contadorHeuristicas.getOrDefault(mejorCandidato.getNombreHeuristica(), 0) + 1);
        }

        return mejorCandidato;
    }

    /**
     * Función de evaluación multiobjetivo
     */
    private double evaluarAsignacion(AsignacionCandidata asignacion) {
        if (asignacion.getAsignaciones().isEmpty()) {
            return 0.0;
        }

        // Calcular componentes individuales
        double costoEnergia = auxiliar.calcularCostoEnergia(asignacion, tiempoActual);
        double valorCargaEntregada = auxiliar.calcularValorCargaEntregada(asignacion, tiempoActual);
        double equidad = calcularEquidad(asignacion);
        double eficienciaCargadores = calcularEficienciaCargadores(asignacion);

        // Combinar con pesos
        double puntuacionTotal = (costoEnergia * pesosEvaluacion.get("costo_energia")) +
                (valorCargaEntregada * pesosEvaluacion.get("valor_carga_entregada")) +
                (equidad * pesosEvaluacion.get("equidad")) +
                (eficienciaCargadores * pesosEvaluacion.get("eficiencia_cargadores"));

        // Log de evaluación multiobjetivo
        logger.logMultiObjectiveEvaluation(asignacion, costoEnergia, valorCargaEntregada,
                equidad, puntuacionTotal);

        return puntuacionTotal;
    }

    // Métodos auxiliares para cálculos específicos
    // [Continuaré con la implementación de estos métodos en la siguiente parte]

    /**
     * Inicialización del algoritmo
     */
    private void inicializar() {
        this.tiempoActual = 0.0;
        this.vehiculosActivos = new ArrayList<>();
        this.cargadoresDisponibles = new HashMap<>();
        this.preciosEnergia = new ArrayList<>(testSystem.getEnergyPrices());
        this.indicePrecioActual = 0;

        // Inicializar todos los cargadores como disponibles
        for (Charger cargador : testSystem.getParkingConfig().getChargers()) {
            cargadoresDisponibles.put(cargador.getChargerId(), true);
        }

        // Crear objetos de simulación para todos los vehículos
        for (VehicleArrival vehiculo : testSystem.getArrivals()) {
            vehiculosActivos.add(new VehiculoSimulacion(vehiculo));
        }

        // Set the list of active simulated vehicles in the auxiliary class
        this.auxiliar.setVehiculosSimulacionActivos(this.vehiculosActivos);

        // Calcular prioridades normalizadas para cada vehículo
        calcularPrioridadesNormalizadas();
    }

    private void calcularPrioridadesNormalizadas() {
        for (VehiculoSimulacion vehiculoSim : vehiculosActivos) {
            VehicleArrival v = vehiculoSim.getVehiculoOriginal();
            int prioridadOriginal = v.getPriority();
            double tiempoEstancia = v.getDepartureTime() - v.getArrivalTime();
            double energiaRequerida = v.getRequiredEnergy();

            double presionTemporal = (tiempoEstancia > 0) ? energiaRequerida / tiempoEstancia : 1.0; // Avoid division
                                                                                                     // by zero

            double prioridadBase;
            if (prioridadOriginal == 1) {
                prioridadBase = 2;
            } else if (prioridadOriginal == 2) {
                prioridadBase = 5;
            } else { // prioridadOriginal == 3
                prioridadBase = 8;
            }

            double factorPresion = Math.min(2.0, presionTemporal / 10.0);
            double prioridadNormalizada = Math.min(10.0, Math.max(1.0, prioridadBase + factorPresion));

            vehiculoSim.setPrioridadNormalizada(prioridadNormalizada);
        }
    }

    private void inicializarContadorHeuristicas() {
        contadorHeuristicas.put("EDF", 0);
        contadorHeuristicas.put("HighestPriority", 0);
        contadorHeuristicas.put("Fairness", 0);
        contadorHeuristicas.put("SJF", 0);
        contadorHeuristicas.put("PriceReactive", 0);
        contadorHeuristicas.put("LocalSearch", 0);
        contadorHeuristicas.put("Exploration", 0);
    }

    // Getters para acceder a los resultados
    public SolucionConstructiva getMejorSolucion() {
        return mejorSolucion;
    }

    public Map<String, Integer> getContadorHeuristicas() {
        return contadorHeuristicas;
    }

    public List<ResultadoIteracion> getHistorialIteraciones() {
        return historialIteraciones;
    }

    private boolean todosVehiculosProcesados() {
        return vehiculosActivos.stream().allMatch(v -> v.getEstado() == VehiculoSimulacion.EstadoVehiculo.COMPLETADO ||
                v.getEstado() == VehiculoSimulacion.EstadoVehiculo.RETIRADO);
    }

    private List<VehiculoSimulacion> obtenerVehiculosEnEspera() {
        return vehiculosActivos.stream()
                .filter(v -> v.haLlegado(tiempoActual) &&
                        v.getEstado() == VehiculoSimulacion.EstadoVehiculo.ESPERANDO &&
                        !v.debeSalir(tiempoActual))
                .collect(Collectors.toList());
    }

    private boolean hayCargadoresDisponibles() {
        return cargadoresDisponibles.values().stream().anyMatch(disponible -> disponible);
    }

    private List<Integer> obtenerCargadoresDisponibles() {
        return cargadoresDisponibles.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void aplicarAsignacion(AsignacionCandidata asignacion) {
        for (Map.Entry<Integer, Integer> entry : asignacion.getAsignaciones().entrySet()) {
            Integer vehiculoId = entry.getKey();
            Integer cargadorId = entry.getValue();

            // Encontrar el vehículo y asignar el cargador
            VehiculoSimulacion vehiculo = vehiculosActivos.stream()
                    .filter(v -> v.getVehiculoOriginal().getId() == vehiculoId)
                    .findFirst()
                    .orElse(null);

            Charger cargador = auxiliar.obtenerCargadorPorId(cargadorId);

            if (vehiculo != null && cargador != null) {
                // Verificar factibilidad antes de asignar
                String factibilityReason = verificarFactibilidad(vehiculo, cargador);
                boolean esfactible = factibilityReason.equals("FACTIBLE");

                // Log de verificación de factibilidad
                logger.logFeasibilityCheck(vehiculo, cargador, esfactible, factibilityReason);

                if (esfactible) {
                    // Log de asignación exitosa
                    logger.logVehicleAssignment(vehiculo, cargador, tiempoActual);

                    // Volver al método original
                    vehiculo.setCargadorAsignado(cargadorId);
                    vehiculo.setEstado(VehiculoSimulacion.EstadoVehiculo.CARGANDO);
                    vehiculo.setTiempoInicioCarga(tiempoActual);
                    cargadoresDisponibles.put(cargadorId, false);
                }
            }
        }
    }

    private void avanzarTiempo() {
        tiempoActual += resolucionTiempo;

        // Verificar restricciones del transformador antes de simular carga
        verificarRestriccionesTransformador();

        // Simular carga de vehículos activos
        for (VehiculoSimulacion vehiculo : vehiculosActivos) {
            if (vehiculo.getEstado() == VehiculoSimulacion.EstadoVehiculo.CARGANDO) {
                simularCargaVehiculo(vehiculo);
            }

            // Verificar si el vehículo debe salir
            if (vehiculo.debeSalir(tiempoActual)) {
                // Log de salida de vehículo
                logger.logVehicleDeparture(vehiculo, tiempoActual);

                if (vehiculo.getCargadorAsignado() != null) {
                    cargadoresDisponibles.put(vehiculo.getCargadorAsignado(), true);
                }
                vehiculo.setEstado(VehiculoSimulacion.EstadoVehiculo.RETIRADO);
                vehiculo.setTiempoFinCarga(tiempoActual);
            }
        }

        // Verificar restricciones de la red
        verificarRestriccionesRed();

        // Capturar datos temporales para gráficas
        capturarDatosTemporales();
    }

    private void simularCargaVehiculo(VehiculoSimulacion vehiculo) {
        Charger cargador = auxiliar.obtenerCargadorPorId(vehiculo.getCargadorAsignado());
        if (cargador == null)
            return;

        VehicleArrival v = vehiculo.getVehiculoOriginal();
        double potenciaEfectiva = Math.min(cargador.getPower(), v.getMaxChargeRate());
        double precioActual = auxiliar.obtenerPrecioEnergia(tiempoActual);

        // Log de progreso de carga
        logger.logChargingProgress(vehiculo, potenciaEfectiva, precioActual,
                cargador.getEfficiency(), tiempoActual);

        vehiculo.actualizarCarga(potenciaEfectiva, precioActual, cargador.getEfficiency(),
                v.getEfficiency(), resolucionTiempo, cargador.getOperationCostPerHour());

        if (vehiculo.isCargaCompleta()) {
            // Log de finalización de carga
            logger.logChargingCompletion(vehiculo, tiempoActual, true);

            cargadoresDisponibles.put(vehiculo.getCargadorAsignado(), true);
            vehiculo.setEstado(VehiculoSimulacion.EstadoVehiculo.COMPLETADO);
            vehiculo.setTiempoFinCarga(tiempoActual);
        }
    }

    private void registrarIteracion(AsignacionCandidata mejorAsignacion) {
        ResultadoIteracion resultado = new ResultadoIteracion(tiempoActual);
        resultado.setHeuristicaSeleccionada(mejorAsignacion.getNombreHeuristica());
        resultado.setVehiculosAsignados(mejorAsignacion.getAsignaciones().size());
        resultado.setValorEvaluacion(mejorAsignacion.getValorEvaluacion());
        resultado.setMejorAsignacion(mejorAsignacion);

        historialIteraciones.add(resultado);
    }

    private void finalizarSolucion(long tiempoEjecucion) {
        mejorSolucion = new SolucionConstructiva();
        mejorSolucion.setTiempoTotalEjecucion(tiempoEjecucion);

        // Calcular métricas finales
        calcularMetricasFinales();

        // Determinar heurística más efectiva
        String heuristicaMasUsada = contadorHeuristicas.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Ninguna");

        mejorSolucion.setHeuristicaMasEfectiva(heuristicaMasUsada);
        mejorSolucion.setUsoHeuristicas(new HashMap<>(contadorHeuristicas));
        mejorSolucion.setEstadoFinalVehiculos(new ArrayList<>(vehiculosActivos));
    }

    private void calcularMetricasFinales() {
        double costoTotalOperacion = 0.0; // This will now represent total_cost in Python
        double costoEnergia = 0.0; // Corresponds to costo_energia in Python
        double costoOperacionCargadores = 0.0; // Corresponds to costo_operativo in Python
        double bonificacionPrioridad = 0.0; // Corresponds to bonificacion_prioridad in Python

        double valorCargaEntregadaJava = 0.0; // This will represent eficiencia_total in Python
        double energiaTotal = 0.0;
        double energiaTotalRequerida = 0.0;
        int vehiculosCompletados = 0;
        int vehiculosAtendidos = 0;
        double completitudTotal = 0.0;
        // Removed penalizacionRetrasos calculation, setting it to 0 in mejorSolucion
        // for comparability.

        for (VehiculoSimulacion vehiculo : vehiculosActivos) {
            energiaTotalRequerida += vehiculo.getVehiculoOriginal().getRequiredEnergy();
        }

        for (VehiculoSimulacion vehiculo : vehiculosActivos) {
            if (vehiculo.getCargadorAsignado() != null) {
                vehiculosAtendidos++;
                costoTotalOperacion += vehiculo.getCostoAcumulado(); // Sum of energy and operational costs
                costoEnergia += vehiculo.getCostoEnergiaAcumulado(); // Energy cost
                costoOperacionCargadores += vehiculo.getCostoOperacionAcumulado(); // Operational cost

                energiaTotal += vehiculo.getEnergiaActual();

                // Python's bonificacion_prioridad: 0.1 * self.prioridades[i] * self.E[i]
                bonificacionPrioridad += 0.1 * vehiculo.getPrioridadNormalizada() * vehiculo.getEnergiaActual();

                if (vehiculo.isCargaCompleta()) {
                    vehiculosCompletados++;
                }

                completitudTotal += vehiculo.getPorcentajeCompletitud();
            }
        }

        double porcentajeCargaEntregado = energiaTotalRequerida > 0 ? (energiaTotal / energiaTotalRequerida) * 100.0
                : 0.0;

        // Populate SolucionConstructiva
        mejorSolucion.setCostoTotalOperacion(costoTotalOperacion); // total_cost in Python
        mejorSolucion.setCostoEnergia(costoEnergia); // costo_energia in Python
        mejorSolucion.setPenalizacionRetrasos(0.0); // Set to 0 for comparability with Python objective

        // This will be equivalent to eficiencia_total in Python
        valorCargaEntregadaJava = energiaTotal + bonificacionPrioridad;
        mejorSolucion.setValorCargaEntregada(valorCargaEntregadaJava);

        mejorSolucion.setEnergiaTotalEntregada(energiaTotal);
        mejorSolucion.setEnergiaTotalRequerida(energiaTotalRequerida);
        mejorSolucion.setPorcentajeCargaEntregado(porcentajeCargaEntregado);
        mejorSolucion.setVehiculosAtendidos(vehiculosAtendidos);
        mejorSolucion.setVehiculosCompletados(vehiculosCompletados);
        mejorSolucion.setPorcentajeCompletitud(vehiculosAtendidos > 0 ? completitudTotal / vehiculosAtendidos : 0.0);
    }

    private double calcularEquidad(AsignacionCandidata asignacion) {
        // Medir equidad como la distribución de carga entre vehículos
        if (asignacion.getAsignaciones().isEmpty())
            return 0.0;

        List<Double> completitudes = new ArrayList<>();
        for (Integer vehiculoId : asignacion.getAsignaciones().keySet()) {
            VehiculoSimulacion vehiculo = vehiculosActivos.stream()
                    .filter(v -> v.getVehiculoOriginal().getId() == vehiculoId)
                    .findFirst()
                    .orElse(null);

            if (vehiculo != null) {
                completitudes.add(vehiculo.getPorcentajeCompletitud());
            }
        }

        if (completitudes.isEmpty())
            return 0.0;

        double promedio = completitudes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double varianza = completitudes.stream()
                .mapToDouble(c -> Math.pow(c - promedio, 2))
                .average()
                .orElse(0.0);

        return 100.0 / (1.0 + varianza); // Mayor equidad = menor varianza
    }

    private double calcularEficienciaCargadores(AsignacionCandidata asignacion) {
        if (asignacion.getAsignaciones().isEmpty())
            return 0.0;

        double eficienciaPromedio = 0.0;
        int contador = 0;

        for (Integer cargadorId : asignacion.getAsignaciones().values()) {
            Charger cargador = auxiliar.obtenerCargadorPorId(cargadorId);
            if (cargador != null) {
                eficienciaPromedio += cargador.getEfficiency();
                contador++;
            }
        }

        return contador > 0 ? eficienciaPromedio / contador : 0.0;
    }

    /**
     * Verifica la factibilidad de asignar un vehículo a un cargador
     */
    private String verificarFactibilidad(VehiculoSimulacion vehiculo, Charger cargador) {
        // 1. Verificar compatibilidad
        if (!auxiliar.esCompatible(vehiculo, cargador)) {
            return "Vehículo incompatible con tipo de cargador";
        }

        // 2. Verificar que el vehículo no haya salido ya
        if (vehiculo.debeSalir(tiempoActual)) {
            return "Vehículo ya debería haber salido";
        }

        // 3. Verificar tiempo disponible
        double tiempoDisponible = vehiculo.getVehiculoOriginal().getDepartureTime() - tiempoActual;
        if (tiempoDisponible <= 0) {
            return "Sin tiempo disponible para carga";
        }

        // 4. Verificar restricciones del transformador
        double potenciaAdicional = Math.min(cargador.getPower(),
                vehiculo.getVehiculoOriginal().getMaxChargeRate());
        double cargaActualTransformador = calcularCargaActualTransformador();

        if (cargaActualTransformador + potenciaAdicional > limiteTransformador) {
            return String.format("Excede límite transformador: %.1f + %.1f > %d kW",
                    cargaActualTransformador, potenciaAdicional, limiteTransformador);
        }

        // 5. Verificar restricciones de la red (si están disponibles)
        GridConstraints gridConstraints = testSystem.getParkingConfig().getGridConstraints();
        if (gridConstraints != null) {
            // Simplificación: verificar potencia máxima por fase
            double potenciaPorFase = (cargaActualTransformador + potenciaAdicional) / 3.0; // Asumiendo 3 fases
            if (potenciaPorFase > gridConstraints.getMaxPowerPerPhase()) {
                return String.format("Excede potencia máxima por fase: %.1f > %.1f kW",
                        potenciaPorFase, gridConstraints.getMaxPowerPerPhase());
            }
        }

        return "FACTIBLE";
    }

    /**
     * Calcula la carga actual del transformador
     */
    private double calcularCargaActualTransformador() {
        double cargaTotal = 0.0;

        for (VehiculoSimulacion vehiculo : vehiculosActivos) {
            if (vehiculo.getEstado() == VehiculoSimulacion.EstadoVehiculo.CARGANDO) {
                Charger cargador = auxiliar.obtenerCargadorPorId(vehiculo.getCargadorAsignado());
                if (cargador != null) {
                    VehicleArrival v = vehiculo.getVehiculoOriginal();
                    cargaTotal += Math.min(cargador.getPower(), v.getMaxChargeRate());
                }
            }
        }

        return cargaTotal;
    }

    /**
     * Verifica restricciones del transformador
     */
    private void verificarRestriccionesTransformador() {
        double cargaActual = calcularCargaActualTransformador();
        boolean violacion = cargaActual > limiteTransformador;

        logger.logTransformerConstraint(cargaActual, limiteTransformador, violacion);

        if (violacion) {
            // Implementar estrategia de reducción de carga si es necesario
            implementarReduccionCarga(cargaActual - limiteTransformador);
        }
    }

    /**
     * Verifica restricciones de la red eléctrica
     */
    private void verificarRestriccionesRed() {
        GridConstraints constraints = testSystem.getParkingConfig().getGridConstraints();
        if (constraints == null)
            return;

        double cargaTotal = calcularCargaActualTransformador();

        // Simulación simplificada de parámetros de red
        double potenciaPorFase = cargaTotal / 3.0; // Asumiendo distribución trifásica equilibrada
        double caidaVoltaje = potenciaPorFase * 0.001; // Simplificación: 0.1% por kW
        double factorPotencia = Math.max(0.85, 1.0 - (cargaTotal * 0.0001)); // Simplificación

        logger.logGridConstraints(constraints, potenciaPorFase, caidaVoltaje, factorPotencia);
    }

    /**
     * Implementa reducción de carga cuando se excede el límite del transformador
     */
    private void implementarReduccionCarga(double exceso) {
        // Estrategia: reducir potencia de vehículos con menor prioridad
        List<VehiculoSimulacion> vehiculosCargando = vehiculosActivos.stream()
                .filter(v -> v.getEstado() == VehiculoSimulacion.EstadoVehiculo.CARGANDO)
                .sorted((v1, v2) -> Integer.compare(v2.getVehiculoOriginal().getPriority(),
                        v1.getVehiculoOriginal().getPriority()))
                .collect(Collectors.toList());

        double reduccionAcumulada = 0.0;

        for (VehiculoSimulacion vehiculo : vehiculosCargando) {
            if (reduccionAcumulada >= exceso)
                break;

            // Implementar preempción si es necesario
            if (vehiculo.getVehiculoOriginal().getPriority() == 3) { // Prioridad baja
                Charger cargadorActual = auxiliar.obtenerCargadorPorId(vehiculo.getCargadorAsignado());

                logger.logPreemption(vehiculo, null, cargadorActual,
                        "Reducción de carga por exceso en transformador");

                // Calcular reducción antes de desasignar
                double potenciaReducida = Math.min(cargadorActual.getPower(),
                        vehiculo.getVehiculoOriginal().getMaxChargeRate());

                // Desasignar temporalmente
                cargadoresDisponibles.put(vehiculo.getCargadorAsignado(), true);
                vehiculo.setCargadorAsignado(null);
                vehiculo.setEstado(VehiculoSimulacion.EstadoVehiculo.ESPERANDO);

                reduccionAcumulada += potenciaReducida;
            }
        }
    }

    /**
     * Log de resumen de iteración
     */
    private void logIterationSummary() {
        int activeVehicles = (int) vehiculosActivos.stream()
                .filter(v -> v.getEstado() != VehiculoSimulacion.EstadoVehiculo.RETIRADO)
                .count();

        int chargingVehicles = (int) vehiculosActivos.stream()
                .filter(v -> v.getEstado() == VehiculoSimulacion.EstadoVehiculo.CARGANDO)
                .count();

        int completedVehicles = (int) vehiculosActivos.stream()
                .filter(v -> v.getEstado() == VehiculoSimulacion.EstadoVehiculo.COMPLETADO)
                .count();

        double totalEnergyDelivered = vehiculosActivos.stream()
                .mapToDouble(VehiculoSimulacion::getEnergiaActual)
                .sum();

        logger.logIterationSummary(tiempoActual, activeVehicles, chargingVehicles,
                completedVehicles, totalEnergyDelivered);
    }

    /**
     * Captura datos temporales para generar gráficas
     */
    private void capturarDatosTemporales() {
        // Calcular métricas actuales
        double cargaTransformador = calcularCargaActualTransformador();

        int cargadoresOcupados = (int) cargadoresDisponibles.values().stream()
                .mapToLong(disponible -> disponible ? 0 : 1).sum();

        int vehiculosCargando = (int) vehiculosActivos.stream()
                .filter(v -> v.getEstado() == VehiculoSimulacion.EstadoVehiculo.CARGANDO)
                .count();

        int vehiculosEsperando = (int) vehiculosActivos.stream()
                .filter(v -> v.getEstado() == VehiculoSimulacion.EstadoVehiculo.ESPERANDO)
                .count();

        int vehiculosCompletados = (int) vehiculosActivos.stream()
                .filter(v -> v.getEstado() == VehiculoSimulacion.EstadoVehiculo.COMPLETADO)
                .count();

        double energiaTotalEntregada = vehiculosActivos.stream()
                .mapToDouble(VehiculoSimulacion::getEnergiaActual)
                .sum();

        // Registrar punto temporal
        datosTemporales.registrarPunto(tiempoActual, cargaTransformador, cargadoresOcupados,
                vehiculosCargando, vehiculosEsperando, vehiculosCompletados, energiaTotalEntregada);
    }

    /**
     * Obtener los datos temporales capturados
     */
    public DatosTemporales getDatosTemporales() {
        return datosTemporales;
    }

    /**
     * Obtener el logger para acceso externo
     */
    public ChargingLogger getLogger() {
        return logger;
    }
}