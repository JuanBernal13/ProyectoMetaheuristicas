package com.ejemplo.algoritmo;

import com.ejemplo.model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sistema de logging avanzado para el algoritmo de carga de vehículos
 * eléctricos.
 * Captura y registra todas las operaciones importantes: preempción, cambios de
 * cargadores,
 * factibilidades, asignaciones, heurísticas, etc.
 */
public class ChargingLogger {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private final boolean consoleOutput;
    private final List<String> logHistory;
    private int iterationCounter;

    // Feature flag para controlar si se muestran los logs
    private boolean showLogs;

    // Contadores de eventos
    private int preemptionCount;
    private int chargerSwapCount;
    private int feasibilityCheckCount;
    private int assignmentCount;
    private int vehicleCompletionCount;

    public ChargingLogger(boolean consoleOutput) {
        this.consoleOutput = consoleOutput;
        this.logHistory = new ArrayList<>();
        this.iterationCounter = 0;
        this.showLogs = false; // Por defecto no muestra los logs
        resetCounters();
    }

    private void resetCounters() {
        this.preemptionCount = 0;
        this.chargerSwapCount = 0;
        this.feasibilityCheckCount = 0;
        this.assignmentCount = 0;
        this.vehicleCompletionCount = 0;
    }

    /**
     * Establece si se deben mostrar los logs en consola
     * 
     * @param showLogs true para mostrar logs, false para silenciarlos
     */
    public void setShowLogs(boolean showLogs) {
        this.showLogs = showLogs;
    }

    /**
     * Obtiene el estado actual de la feature flag
     * 
     * @return true si los logs se están mostrando, false si están silenciados
     */
    public boolean isShowingLogs() {
        return showLogs;
    }

    /**
     * Método de log principal (ahora público para uso externo)
     */
    public void log(String level, String category, String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logEntry = String.format("[%s] %s <%s> %s", timestamp, level, category, message);

        logHistory.add(logEntry);

        // Solo mostrar en consola si la feature flag está activada
        if (consoleOutput && showLogs) {
            System.out.println(logEntry);
        }
    }

    /**
     * Log de inicio de nueva iteración
     */
    public void logIterationStart(double currentTime, int vehiclesWaiting, int availableChargers) {
        this.iterationCounter++;
        log("INFO", "ITERATION",
                String.format(
                        "🔄 Iteración %d iniciada - Tiempo: %.2f h | Vehículos esperando: %d | Cargadores disponibles: %d",
                        this.iterationCounter, currentTime, vehiclesWaiting, availableChargers));
    }

    /**
     * Log de generación de heurísticas candidatas
     */
    public void logHeuristicsGeneration(List<AsignacionCandidata> candidates) {
        log("INFO", "HEURISTICS", String.format("🧠 Generadas %d soluciones candidatas:", candidates.size()));

        for (int i = 0; i < candidates.size(); i++) {
            AsignacionCandidata candidate = candidates.get(i);
            log("INFO", "HEURISTICS", String.format("   %d. %s: %d asignaciones, valor=%.3f",
                    i + 1, candidate.getNombreHeuristica(),
                    candidate.getNumeroAsignaciones(), candidate.getValorEvaluacion()));
        }
    }

    /**
     * Log de selección de mejor heurística
     */
    public void logHeuristicSelection(AsignacionCandidata selected, double evaluationTime) {
        log("SUCCESS", "HEURISTICS", String.format("🎯 Heurística seleccionada: %s",
                selected.getNombreHeuristica()));
        log("INFO", "HEURISTICS", String.format("   📊 Valor evaluación: %.4f",
                selected.getValorEvaluacion()));
        log("INFO", "HEURISTICS", String.format("   ⏱️  Tiempo evaluación: %.2f ms", evaluationTime));
    }

    /**
     * Log de verificación de factibilidad
     */
    public void logFeasibilityCheck(VehiculoSimulacion vehicle, Charger charger, boolean feasible, String reason) {
        feasibilityCheckCount++;

        String status = feasible ? "✅ FACTIBLE" : "❌ NO FACTIBLE";
        log("DEBUG", "FEASIBILITY", String.format("%s: Vehículo %d → Cargador %d",
                status, vehicle.getVehiculoOriginal().getId(), charger.getChargerId()));

        if (!feasible) {
            log("WARN", "FEASIBILITY", String.format("   🚫 Razón: %s", reason));
        } else {
            double potenciaEfectiva = (double) Math.min(charger.getPower(),
                    vehicle.getVehiculoOriginal().getMaxChargeRate());
            log("DEBUG", "FEASIBILITY", String.format("   ✓ Potencia efectiva: %.1f kW", potenciaEfectiva));
        }
    }

    /**
     * Log de compatibilidad entre vehículo y cargador
     */
    public void logCompatibilityCheck(VehiculoSimulacion vehicle, Charger charger, boolean compatible, double score) {
        String vehicleBrand = vehicle.getVehiculoOriginal().getBrand();
        String compatibleVehicles = String.join(", ", charger.getCompatibleVehicles());

        if (compatible) {
            log("DEBUG", "COMPATIBILITY", String.format("✅ %s compatible con Cargador %d (score: %.3f)",
                    vehicleBrand, charger.getChargerId(), score));
        } else {
            log("WARN", "COMPATIBILITY", String.format("❌ %s NO compatible con Cargador %d",
                    vehicleBrand, charger.getChargerId()));
            log("DEBUG", "COMPATIBILITY", String.format("   🔌 Cargador acepta: %s", compatibleVehicles));
        }
    }

    /**
     * Log de asignación de vehículo a cargador
     */
    public void logVehicleAssignment(VehiculoSimulacion vehicle, Charger charger, double currentTime) {
        assignmentCount++;

        VehicleArrival v = vehicle.getVehiculoOriginal();
        log("SUCCESS", "ASSIGNMENT", String.format("🔗 ASIGNACIÓN: Vehículo %d (%s) → Cargador %d",
                v.getId(), v.getBrand(), charger.getChargerId()));

        log("INFO", "ASSIGNMENT", String.format("   ⏰ Tiempo asignación: %.3f h", currentTime));
        log("INFO", "ASSIGNMENT", String.format("   🔋 Energía requerida: %.2f kWh", vehicle.getEnergiaRestante()));
        log("INFO", "ASSIGNMENT", String.format("   ⚡ Potencia disponible: %d kW", charger.getPower()));
        log("INFO", "ASSIGNMENT", String.format("   🚪 Tiempo salida: %.3f h", v.getDepartureTime()));

        double timeAvailable = v.getDepartureTime() - currentTime;
        double maxPossibleEnergy = Math.min(charger.getPower(), v.getMaxChargeRate()) * timeAvailable;
        double completionPossible = (maxPossibleEnergy / vehicle.getEnergiaRestante()) * 100;

        log("INFO", "ASSIGNMENT", String.format("   📈 Máxima energía posible: %.2f kWh (%.1f%% completitud)",
                maxPossibleEnergy, Math.min(100, completionPossible)));
    }

    /**
     * Log de preempción (vehículo debe ceder cargador)
     */
    public void logPreemption(VehiculoSimulacion currentVehicle, VehiculoSimulacion newVehicle,
            Charger charger, String reason) {
        preemptionCount++;

        log("WARN", "PREEMPTION", String.format("🔄 PREEMPCIÓN en Cargador %d", charger.getChargerId()));
        log("WARN", "PREEMPTION", String.format("   📤 Vehículo saliente: %d (%s, %.1f%% completitud)",
                currentVehicle.getVehiculoOriginal().getId(),
                currentVehicle.getVehiculoOriginal().getBrand(),
                currentVehicle.getPorcentajeCompletitud()));
        log("WARN", "PREEMPTION", String.format("   📥 Vehículo entrante: %d (%s, prioridad %d)",
                newVehicle.getVehiculoOriginal().getId(),
                newVehicle.getVehiculoOriginal().getBrand(),
                newVehicle.getVehiculoOriginal().getPriority()));
        log("WARN", "PREEMPTION", String.format("   🎯 Razón: %s", reason));
    }

    /**
     * Log de cambio de cargador
     */
    public void logChargerSwap(VehiculoSimulacion vehicle, Charger oldCharger, Charger newCharger, String reason) {
        chargerSwapCount++;

        log("INFO", "CHARGER_SWAP", String.format("🔀 CAMBIO DE CARGADOR: Vehículo %d",
                vehicle.getVehiculoOriginal().getId()));
        log("INFO", "CHARGER_SWAP", String.format("   📤 Cargador anterior: %d (%d kW)",
                oldCharger.getChargerId(), oldCharger.getPower()));
        log("INFO", "CHARGER_SWAP", String.format("   📥 Cargador nuevo: %d (%d kW)",
                newCharger.getChargerId(), newCharger.getPower()));
        log("INFO", "CHARGER_SWAP", String.format("   🎯 Razón: %s", reason));

        double powerImprovement = newCharger.getPower() - oldCharger.getPower();
        if (powerImprovement > 0) {
            log("INFO", "CHARGER_SWAP", String.format("   📈 Mejora de potencia: +%d kW", (int) powerImprovement));
        } else if (powerImprovement < 0) {
            log("WARN", "CHARGER_SWAP", String.format("   📉 Reducción de potencia: %d kW", (int) powerImprovement));
        }
    }

    /**
     * Log de progreso de carga
     */
    public void logChargingProgress(VehiculoSimulacion vehicle, double energyDelivered,
            double currentPrice, double efficiency, double currentTime) {
        VehicleArrival v = vehicle.getVehiculoOriginal();

        log("DEBUG", "CHARGING", String.format("🔋 Carga Vehículo %d: +%.3f kWh",
                v.getId(), energyDelivered));
        log("DEBUG", "CHARGING", String.format("   📊 Progreso: %.1f%% (%.2f/%.2f kWh)",
                vehicle.getPorcentajeCompletitud(), vehicle.getEnergiaActual(), v.getRequiredEnergy()));
        log("DEBUG", "CHARGING", String.format("   💰 Precio actual: %.4f EUR/kWh", currentPrice));
        log("DEBUG", "CHARGING", String.format("   ⚙️  Eficiencia: %.1f%%", efficiency * 100));

        double timeRemaining = v.getDepartureTime() - currentTime;
        log("DEBUG", "CHARGING", String.format("   ⏰ Tiempo restante: %.3f h", timeRemaining));
    }

    /**
     * Log de finalización de carga
     */
    public void logChargingCompletion(VehiculoSimulacion vehicle, double completionTime, boolean fullyCharged) {
        vehicleCompletionCount++;

        VehicleArrival v = vehicle.getVehiculoOriginal();
        String status = fullyCharged ? "✅ COMPLETADA" : "⚠️ PARCIAL";

        log("SUCCESS", "COMPLETION", String.format("🏁 CARGA %s: Vehículo %d (%s)",
                status, v.getId(), v.getBrand()));
        log("INFO", "COMPLETION", String.format("   ⏰ Tiempo finalización: %.3f h", completionTime));
        log("INFO", "COMPLETION", String.format("   🔋 Energía entregada: %.2f/%.2f kWh (%.1f%%)",
                vehicle.getEnergiaActual(), v.getRequiredEnergy(), vehicle.getPorcentajeCompletitud()));
        log("INFO", "COMPLETION", String.format("   💰 Costo total: %.2f EUR", vehicle.getCostoAcumulado()));

        double chargingTime = completionTime - vehicle.getTiempoInicioCarga();
        log("INFO", "COMPLETION", String.format("   ⏱️  Tiempo de carga: %.3f h", chargingTime));
    }

    /**
     * Log de salida de vehículo
     */
    public void logVehicleDeparture(VehiculoSimulacion vehicle, double departureTime) {
        VehicleArrival v = vehicle.getVehiculoOriginal();

        log("INFO", "DEPARTURE", String.format("🚪 SALIDA: Vehículo %d (%s)",
                v.getId(), v.getBrand()));
        log("INFO", "DEPARTURE", String.format("   ⏰ Tiempo salida: %.3f h (programado: %.3f h)",
                departureTime, v.getDepartureTime()));

        if (vehicle.getCargadorAsignado() != null) {
            log("INFO", "DEPARTURE", String.format("   🔌 Liberando cargador: %d", vehicle.getCargadorAsignado()));
        }

        double waitingTime = vehicle.getTiempoInicioCarga() - vehicle.getTiempoInicioEspera();
        log("INFO", "DEPARTURE", String.format("   ⏳ Tiempo total de espera: %.3f h", waitingTime));
    }

    /**
     * Log de restricciones del transformador
     */
    public void logTransformerConstraint(double currentLoad, int transformerLimit, boolean violated) {
        if (violated) {
            log("ERROR", "CONSTRAINT", String.format("🚨 VIOLACIÓN TRANSFORMADOR: %.1f kW > %d kW",
                    currentLoad, transformerLimit));
        } else {
            log("DEBUG", "CONSTRAINT", String.format("✅ Carga transformador: %.1f/%d kW (%.1f%%)",
                    currentLoad, transformerLimit, (currentLoad / transformerLimit) * 100));
        }
    }

    /**
     * Log de restricciones de la red
     */
    public void logGridConstraints(GridConstraints constraints, double currentPowerPerPhase,
            double voltageDropActual, double powerFactorActual) {
        log("DEBUG", "GRID", "🔌 Verificación restricciones de red:");

        // Potencia por fase
        boolean powerViolation = currentPowerPerPhase > constraints.getMaxPowerPerPhase();
        log(powerViolation ? "WARN" : "DEBUG", "GRID",
                String.format("   ⚡ Potencia por fase: %.1f/%.1f kW %s",
                        currentPowerPerPhase, constraints.getMaxPowerPerPhase(),
                        powerViolation ? "❌" : "✅"));

        // Caída de voltaje
        boolean voltageViolation = voltageDropActual > constraints.getVoltageDropLimit();
        log(voltageViolation ? "WARN" : "DEBUG", "GRID",
                String.format("   📉 Caída de voltaje: %.3f/%.3f %s",
                        voltageDropActual, constraints.getVoltageDropLimit(),
                        voltageViolation ? "❌" : "✅"));

        // Factor de potencia
        boolean pfViolation = powerFactorActual < constraints.getPowerFactorLimit();
        log(pfViolation ? "WARN" : "DEBUG", "GRID",
                String.format("   🔋 Factor de potencia: %.3f/%.3f %s",
                        powerFactorActual, constraints.getPowerFactorLimit(),
                        pfViolation ? "❌" : "✅"));
    }

    /**
     * Log de optimización multiobjetivo
     */
    public void logMultiObjectiveEvaluation(AsignacionCandidata assignment,
            double costComponent, double valueComponent,
            double fairnessComponent, double totalScore) {
        log("DEBUG", "EVALUATION", String.format("📊 Evaluación multiobjetivo: %s",
                assignment.getNombreHeuristica()));
        log("DEBUG", "EVALUATION", String.format("   💰 Componente costo: %.4f", costComponent));
        log("DEBUG", "EVALUATION", String.format("   💎 Componente valor: %.4f", valueComponent));
        log("DEBUG", "EVALUATION", String.format("   ⚖️  Componente equidad: %.4f", fairnessComponent));
        log("DEBUG", "EVALUATION", String.format("   🎯 Puntuación total: %.4f", totalScore));
    }

    /**
     * Log de resumen de iteración
     */
    public void logIterationSummary(double currentTime, int activeVehicles, int chargingVehicles,
            int completedVehicles, double totalEnergyDelivered) {
        log("INFO", "SUMMARY", "📋 Resumen de iteración:");
        log("INFO", "SUMMARY", String.format("   ⏰ Tiempo: %.3f h", currentTime));
        log("INFO", "SUMMARY", String.format("   🚗 Vehículos activos: %d", activeVehicles));
        log("INFO", "SUMMARY", String.format("   🔋 Vehículos cargando: %d", chargingVehicles));
        log("INFO", "SUMMARY", String.format("   ✅ Vehículos completados: %d", completedVehicles));
        log("INFO", "SUMMARY", String.format("   ⚡ Energía total entregada: %.2f kWh", totalEnergyDelivered));
    }

    /**
     * Log de estadísticas finales
     */
    public void logFinalStatistics(double porcentajeCargaEntregado, double energiaTotal, double energiaRequerida) {
        log("SUCCESS", "STATISTICS", "📊 ESTADÍSTICAS FINALES DEL ALGORITMO:");
        log("INFO", "STATISTICS", String.format("   🔄 Total iteraciones: %d", iterationCounter));
        log("INFO", "STATISTICS", String.format("   🔗 Total asignaciones: %d", assignmentCount));
        log("INFO", "STATISTICS", String.format("   🔄 Preempciones realizadas: %d", preemptionCount));
        log("INFO", "STATISTICS", String.format("   🔀 Cambios de cargador: %d", chargerSwapCount));
        log("INFO", "STATISTICS", String.format("   ✅ Verificaciones factibilidad: %d", feasibilityCheckCount));
        log("INFO", "STATISTICS", String.format("   🏁 Finalizaciones de carga: %d", vehicleCompletionCount));
        log("INFO", "STATISTICS", String.format("   ⚡ Energía entregada: %.2f kWh", energiaTotal));
        log("INFO", "STATISTICS", String.format("   🎯 Energía requerida: %.2f kWh", energiaRequerida));
        log("INFO", "STATISTICS",
                String.format("   📊 Porcentaje de carga entregado: %.1f%%", porcentajeCargaEntregado));
    }

    /**
     * Obtener historial completo de logs
     */
    public List<String> getLogHistory() {
        return new ArrayList<>(logHistory);
    }

    /**
     * Filtrar logs por categoría
     */
    public List<String> getLogsByCategory(String category) {
        return logHistory.stream()
                .filter(log -> log.contains("<" + category + ">"))
                .collect(Collectors.toList());
    }

    /**
     * Exportar logs a texto
     */
    public String exportLogs() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(80)).append("\n");
        sb.append("CHARGING SYSTEM EXECUTION LOG\n");
        sb.append("Generated: ").append(LocalDateTime.now()).append("\n");
        sb.append("=".repeat(80)).append("\n\n");

        for (String log : logHistory) {
            sb.append(log).append("\n");
        }

        return sb.toString();
    }

    /**
     * Limpiar historial de logs
     */
    public void clearLogs() {
        logHistory.clear();
        resetCounters();
        iterationCounter = 0;
    }
}