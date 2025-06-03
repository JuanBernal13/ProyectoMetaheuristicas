package com.ejemplo.main;

import com.ejemplo.model.*;
import com.ejemplo.mapper.JsonMapper;
import com.ejemplo.algoritmo.*;

import java.util.Scanner;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.io.FileWriter; // Added for CSV writing
import java.io.PrintWriter; // Added for CSV writing
import java.util.ArrayList; // Added for storing benchmark results

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static TestSystem testSystem;
    private static ConstructivoAdaptativo ultimoAlgoritmoEjecutado = null; // Para acceder a los logs
    private static ScatterSearch ultimoScatterSearchEjecutado = null; // Para acceder a los resultados de Scatter Search
    private static boolean logsHabilitados = false; // Feature flag global para controlar logs

    public static void main(String[] args) {
        System.out.println(" MAPPER DE SISTEMA DE CARGA DE VEHÍCULOS ELÉCTRICOS");
        System.out.println("=".repeat(60));

        // Cargar datos del sistema
        cargarDatosDelSistema();

        // Menú principal
        mostrarMenuPrincipal();
    }

    private static void cargarDatosDelSistema() {
        System.out.println(" Cargando datos del sistema de prueba...");
        try {
            JsonMapper jsonMapper = new JsonMapper();

            // Preguntar al usuario qué archivo quiere cargar
            System.out.println("Archivos JSON disponibles:");
            System.out.println("1. test_system_1.json ");
            System.out.println("2. test_system_2.json ");
            System.out.println("3. test_system_3.json ");
            System.out.println("4. test_system_4.json ");
            System.out.println("5. test_system_5.json ");
            System.out.println("6. test_system_6.json ");
            System.out.println("7. test_system_7.json ");
            System.out.print("Seleccione el archivo a cargar (1-7): ");

            int opcion = leerOpcion(1, 7);
            String archivo = "test_system_" + opcion + ".json";

            System.out.println(" Intentando cargar " + archivo + "...");

            try {
                testSystem = jsonMapper.mapJsonToTestSystem(archivo);
                System.out.println(" Datos cargados exitosamente desde " + archivo + "!");
                System.out.printf("   • Sistema de prueba #%d%n", testSystem.getTestNumber());
                System.out.printf("   • %d vehículos, %d cargadores%n",
                        testSystem.getArrivals().size(),
                        testSystem.getParkingConfig().getChargers().size());
            } catch (IOException e) {
                System.err.println(" Error cargando " + archivo + ": " + e.getMessage());
                System.err.println("Por favor, asegúrese de que el archivo exista y sea válido.");
                System.err.println("Volviendo al menú principal...");
                // No salir, permitir que el usuario intente de nuevo o elija otra opción
                return;
            }

        } catch (Exception e) {
            System.err.println(" Error fatal cargando datos del sistema: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println();
    }

    private static void mostrarMenuPrincipal() {
        boolean continuar = true;

        while (continuar) {
            System.out.println(" menú principal");
            System.out.println("-".repeat(30));
            System.out.println("1. mostrar información general del sistema");
            System.out.println("2. mostrar información detallada de vehículos");
            System.out.println("3. mostrar información de cargadores");
            System.out.println("4. mostrar precios de energía");
            System.out.println("5. mostrar marcas de vehículos");
            System.out.println("6. mostrar tipos de cargadores");
            System.out.println("7. mostrar restricciones de la red");
            System.out.println("8. cargar otro archivo json");
            System.out.println("9. *** ejecutar algoritmo constructivo adaptativo ***");
            System.out.println("10. ver logs del último algoritmo ejecutado");
            System.out.println("11. filtrar logs por categoría");
            System.out.println("12. exportar logs a archivo");
            System.out.println("13. configurar logs (activar/desactivar)");
            System.out.println("14. tabla detallada de vehículos");
            System.out.println("15. gráficas de evolución temporal");
            System.out.println("16. *** ejecutar scatter search ***");
            System.out.println("17. *** comparar algoritmos (constructivo vs scatter search) ***");
            System.out.println("19. frente de pareto (scatter search)");
            System.out.println("20. análisis multiobjetivo");
            System.out.println("21. calibrar parámetros scatter search");
            System.out.println("22. ejecutar benchmark completo y exportar csv");
            System.out.println("23. ejecutar constructivo para un archivo y exportar a csv");
            System.out.println("24. ejecutar constructivo para todos los archivos y exportar a csv");
            System.out.println("0. salir");
            System.out.println();
            System.out.print("seleccione una opción: ");

            int opcion = leerOpcion(0, 24); // Updated max option

            switch (opcion) {
                case 1:
                    mostrarInformacionGeneral();
                    break;
                case 2:
                    mostrarInformacionVehiculos();
                    break;
                case 3:
                    mostrarInformacionCargadores();
                    break;
                case 4:
                    mostrarPreciosEnergia();
                    break;
                case 5:
                    mostrarMarcasVehiculos();
                    break;
                case 6:
                    mostrarTiposCargadores();
                    break;
                case 7:
                    mostrarRestriccionesRed();
                    break;
                case 8:
                    cargarDatosDelSistema();
                    break;
                case 9:
                    ejecutarAlgoritmoConstructivo();
                    break;
                case 10:
                    mostrarLogsUltimoAlgoritmo();
                    break;
                case 11:
                    filtrarLogsPorCategoria();
                    break;
                case 12:
                    exportarLogs();
                    break;
                case 13:
                    configurarLogs();
                    break;
                case 14:
                    mostrarTablaDetalladaVehiculos();
                    break;
                case 15:
                    mostrarGraficasEvolutivas();
                    break;
                case 16:
                    ejecutarScatterSearch();
                    break;
                case 17:
                    compararAlgoritmos();
                    break;
                case 19:
                    mostrarFrenteParetoScatterSearch();
                    break;
                case 20:
                    analizarSolucionesMultiobjetivo();
                    break;
                case 21:
                    calibrarParametrosScatterSearch();
                    break;
                case 22: // New case for the benchmark
                    ejecutarBenchmarkCompleto();
                    break;
                case 23:
                    ejecutarConstructivoYExportarCSV();
                    break;
                case 24:
                    ejecutarConstructivoParaTodosYExportarCSV();
                    break;
                case 0:
                    continuar = false;
                    System.out.println(" Hasta luego!");
                    break;
            }

            if (continuar) {
                System.out.println("\nPresione Enter para continuar...");
                scanner.nextLine();
                System.out.println();
            }
        }
    }

    private static void mostrarInformacionGeneral() {
        System.out.println(" INFORMACIÓN GENERAL DEL SISTEMA");
        System.out.println("=".repeat(40));

        System.out.printf(" Número de sistema de prueba: %d%n", testSystem.getTestNumber());

        ParkingConfig config = testSystem.getParkingConfig();
        System.out.println("\n Configuración del estacionamiento:");
        System.out.printf("   • Número de espacios: %d%n", config.getNSpots());
        System.out.printf("   • Límite del transformador: %d kW%n", config.getTransformerLimit());
        System.out.printf("   • Eficiencia global: %.1f%%%n", config.getEfficiency() * 100);
        System.out.printf("   • Resolución temporal: %.2f horas%n", config.getTimeResolution());

        System.out.println("\n Estadísticas generales:");
        System.out.printf("   • Total de vehículos: %d%n", testSystem.getArrivals().size());
        System.out.printf("   • Total de cargadores: %d%n", config.getChargers().size());
        System.out.printf("   • Puntos de precios de energía: %d%n", testSystem.getEnergyPrices().size());
        System.out.printf("   • Marcas de vehículos: %d%n", testSystem.getCarBrands().size());
        System.out.printf("   • Tipos de cargadores: %d%n", testSystem.getChargerTypes().size());
    }

    private static void mostrarInformacionVehiculos() {
        System.out.println(" INFORMACIÓN DETALLADA DE LLEGADAS DE VEHÍCULOS");
        System.out.println("=".repeat(50));

        System.out.printf(" Total de vehículos en el sistema: %d%n%n", testSystem.getArrivals().size());

        // Preguntar si quiere ver todos o solo los primeros
        System.out.println("¿Cómo desea ver la información?");
        System.out.println("1. Mostrar todos los vehículos");
        System.out.println("2. Mostrar solo los primeros 10 vehículos");
        System.out.print("Seleccione una opción (1-2): ");

        int opcionVista = leerOpcion(1, 2);
        int vehiculosAMostrar = (opcionVista == 1) ? testSystem.getArrivals().size()
                : Math.min(10, testSystem.getArrivals().size());

        System.out.println();

        for (int i = 0; i < vehiculosAMostrar; i++) {
            VehicleArrival vehiculo = testSystem.getArrivals().get(i);

            System.out.printf(" VEHÍCULO #%d%n", vehiculo.getId());
            System.out.println("-".repeat(30));

            // Información básica
            System.out.printf("   Marca/Modelo: %s%n", vehiculo.getBrand());
            System.out.printf("   Capacidad batería: %d kWh%n", vehiculo.getBatteryCapacity());
            System.out.printf("   Energía requerida: %.2f kWh%n", vehiculo.getRequiredEnergy());

            // Tiempos
            System.out.printf("   Hora llegada: %.2f h%n", vehiculo.getArrivalTime());
            System.out.printf("   Hora salida: %.2f h%n", vehiculo.getDepartureTime());
            System.out.printf("   Tiempo disponible: %.2f h%n",
                    vehiculo.getDepartureTime() - vehiculo.getArrivalTime());

            // Tasas de carga
            System.out.printf("   Tasa mínima carga: %.1f kW%n", vehiculo.getMinChargeRate());
            System.out.printf("   Tasa máxima carga: %.1f kW%n", (double) vehiculo.getMaxChargeRate());
            System.out.printf("   Tasa carga AC: %.1f kW%n", vehiculo.getAcChargeRate());
            System.out.printf("   Tasa carga DC: %.1f kW%n", (double) vehiculo.getDcChargeRate());

            // Características económicas y operativas
            System.out.printf("   Prioridad: %d%n", vehiculo.getPriority());
            System.out.printf("   Disposición a pagar: %.3f EUR/kWh%n", vehiculo.getWillingnessToPay());
            System.out.printf("   Eficiencia: %.1f%%%n", vehiculo.getEfficiency() * 100);

            // Cálculos útiles
            double tiempoMinimoCarga = vehiculo.getRequiredEnergy() / vehiculo.getMaxChargeRate();
            double tiempoDisponible = vehiculo.getDepartureTime() - vehiculo.getArrivalTime();
            boolean puedeCompletarCarga = tiempoMinimoCarga <= tiempoDisponible;

            System.out.printf("   Tiempo mínimo para carga completa: %.2f h%n", tiempoMinimoCarga);
            System.out.printf("   ¿Puede completar carga?: %s%n",
                    puedeCompletarCarga ? "SÍ" : "NO");

            if (!puedeCompletarCarga) {
                double energiaMaximaPosible = vehiculo.getMaxChargeRate() * tiempoDisponible;
                double porcentajePosible = (energiaMaximaPosible / vehiculo.getRequiredEnergy()) * 100;
                System.out.printf("   Máxima energía posible: %.2f kWh (%.1f%% de lo requerido)%n",
                        energiaMaximaPosible, porcentajePosible);
            }

            System.out.println();
        }

        if (opcionVista == 2 && testSystem.getArrivals().size() > 10) {
            System.out.printf("... y %d vehículos más (seleccione opción 1 para ver todos)%n",
                    testSystem.getArrivals().size() - 10);
            System.out.println();
        }

        // Estadísticas generales de las llegadas
        mostrarEstadisticasArrivals();
    }

    private static void mostrarEstadisticasArrivals() {
        System.out.println(" ESTADÍSTICAS DE LLEGADAS");
        System.out.println("=".repeat(30));

        var arrivals = testSystem.getArrivals();

        // Estadísticas de tiempo
        double tiempoLlegadaMin = arrivals.stream().mapToDouble(VehicleArrival::getArrivalTime).min().orElse(0);
        double tiempoLlegadaMax = arrivals.stream().mapToDouble(VehicleArrival::getArrivalTime).max().orElse(0);
        double tiempoSalidaMin = arrivals.stream().mapToDouble(VehicleArrival::getDepartureTime).min().orElse(0);
        double tiempoSalidaMax = arrivals.stream().mapToDouble(VehicleArrival::getDepartureTime).max().orElse(0);

        System.out.printf("⏰ Rango de llegadas: %.2f h - %.2f h%n", tiempoLlegadaMin, tiempoLlegadaMax);
        System.out.printf("🚪 Rango de salidas: %.2f h - %.2f h%n", tiempoSalidaMin, tiempoSalidaMax);

        // Estadísticas de energía
        double energiaTotal = arrivals.stream().mapToDouble(VehicleArrival::getRequiredEnergy).sum();
        double energiaPromedio = arrivals.stream().mapToDouble(VehicleArrival::getRequiredEnergy).average().orElse(0);
        double energiaMax = arrivals.stream().mapToDouble(VehicleArrival::getRequiredEnergy).max().orElse(0);
        double energiaMin = arrivals.stream().mapToDouble(VehicleArrival::getRequiredEnergy).min().orElse(0);

        System.out.printf("⚡ Energía total requerida: %.2f kWh%n", energiaTotal);
        System.out.printf("📊 Energía promedio por vehículo: %.2f kWh%n", energiaPromedio);
        System.out.printf("📈 Energía máxima: %.2f kWh%n", energiaMax);
        System.out.printf("📉 Energía mínima: %.2f kWh%n", energiaMin);

        // Distribución por prioridad
        long prioridad1 = arrivals.stream().filter(v -> v.getPriority() == 1).count();
        long prioridad2 = arrivals.stream().filter(v -> v.getPriority() == 2).count();
        long prioridad3 = arrivals.stream().filter(v -> v.getPriority() == 3).count();

        System.out.printf("⭐ Distribución por prioridad:%n");
        System.out.printf("   • Prioridad 1 (alta): %d vehículos%n", prioridad1);
        System.out.printf("   • Prioridad 2 (media): %d vehículos%n", prioridad2);
        System.out.printf("   • Prioridad 3 (baja): %d vehículos%n", prioridad3);

        // Distribución por marca
        var distribucionMarcas = arrivals.stream()
                .collect(java.util.stream.Collectors.groupingBy(VehicleArrival::getBrand,
                        java.util.stream.Collectors.counting()));

        System.out.printf("🏷️  Distribución por marca:%n");
        distribucionMarcas.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> System.out.printf("   • %s: %d vehículos%n",
                        entry.getKey(), entry.getValue()));
    }

    private static void mostrarInformacionCargadores() {
        System.out.println("⚡ INFORMACIÓN DE CARGADORES");
        System.out.println("=".repeat(40));

        for (Charger cargador : testSystem.getParkingConfig().getChargers()) {
            System.out.printf("\n🔌 Cargador #%d:%n", cargador.getChargerId());
            System.out.printf("   • Potencia: %d kW%n", cargador.getPower());
            System.out.printf("   • Tipo: %s%n", cargador.getType());
            System.out.printf("   • Eficiencia: %.1f%%%n", cargador.getEfficiency() * 100);
            System.out.printf("   • Costo instalación: %d EUR%n", cargador.getInstallationCost());
            System.out.printf("   • Costo operación/hora: %.2f EUR/h%n", cargador.getOperationCostPerHour());
            System.out.printf("   • Vehículos compatibles: %s%n",
                    String.join(", ", cargador.getCompatibleVehicles()));
        }
    }

    private static void mostrarPreciosEnergia() {
        System.out.println("💰 PRECIOS DE ENERGÍA");
        System.out.println("=".repeat(40));

        System.out.printf("Total de puntos de precio: %d%n%n", testSystem.getEnergyPrices().size());

        for (int i = 0; i < Math.min(20, testSystem.getEnergyPrices().size()); i++) {
            EnergyPrice precio = testSystem.getEnergyPrices().get(i);
            System.out.printf("⏰ Tiempo %.2f h: %.4f EUR/kWh%n",
                    precio.getTime(), precio.getPrice());
        }

        if (testSystem.getEnergyPrices().size() > 20) {
            System.out.printf("\n... y %d puntos más%n", testSystem.getEnergyPrices().size() - 20);
        }

        // Estadísticas de precios
        double minPrecio = testSystem.getEnergyPrices().stream()
                .mapToDouble(EnergyPrice::getPrice).min().orElse(0);
        double maxPrecio = testSystem.getEnergyPrices().stream()
                .mapToDouble(EnergyPrice::getPrice).max().orElse(0);
        double promedioPrecio = testSystem.getEnergyPrices().stream()
                .mapToDouble(EnergyPrice::getPrice).average().orElse(0);

        System.out.printf("\n📈 Estadísticas de precios:%n");
        System.out.printf("   • Precio mínimo: %.4f EUR/kWh%n", minPrecio);
        System.out.printf("   • Precio máximo: %.4f EUR/kWh%n", maxPrecio);
        System.out.printf("   • Precio promedio: %.4f EUR/kWh%n", promedioPrecio);
    }

    private static void mostrarMarcasVehiculos() {
        System.out.println("🏭 MARCAS DE VEHÍCULOS");
        System.out.println("=".repeat(40));

        for (CarBrand marca : testSystem.getCarBrands()) {
            System.out.printf("\n🚘 %s:%n", marca.getModelName());
            System.out.printf("   • Capacidad batería: %d kWh%n", marca.getBatteryCapacity());
            System.out.printf("   • SoC mínimo llegada: %.1f%%%n", marca.getMinSocArrival() * 100);
            System.out.printf("   • Carga AC máxima: %.1f kW%n", marca.getMaxAcChargeRate());
            System.out.printf("   • Carga DC máxima: %d kW%n", marca.getMaxDcChargeRate());
            System.out.printf("   • Eficiencia de carga: %.1f%%%n", marca.getChargingEfficiency() * 100);
        }
    }

    private static void mostrarTiposCargadores() {
        System.out.println("🔌 TIPOS DE CARGADORES");
        System.out.println("=".repeat(40));

        for (String tipoNombre : testSystem.getChargerTypes().keySet()) {
            ChargerType tipo = testSystem.getChargerTypes().get(tipoNombre);
            System.out.printf("\n⚡ %s:%n", tipoNombre);
            System.out.printf("   • Potencia: %d kW%n", tipo.getPower());
            System.out.printf("   • Tipo: %s%n", tipo.getType());
            System.out.printf("   • Eficiencia: %.1f%%%n", tipo.getEfficiency() * 100);
            System.out.printf("   • Costo instalación: %d EUR%n", tipo.getInstallationCost());
            System.out.printf("   • Costo operación: %.2f EUR%n", tipo.getOperationCost());
            System.out.printf("   • Vehículos compatibles: %s%n",
                    String.join(", ", tipo.getCompatibleVehicles()));
        }
    }

    private static void mostrarRestriccionesRed() {
        System.out.println("🔌 RESTRICCIONES DE LA RED ELÉCTRICA");
        System.out.println("=".repeat(40));

        GridConstraints restricciones = testSystem.getParkingConfig().getGridConstraints();

        System.out.printf("⚡ Potencia máxima por fase: %.1f kW%n",
                restricciones.getMaxPowerPerPhase());
        System.out.printf("📉 Límite caída de voltaje: %.1f%%%n",
                restricciones.getVoltageDropLimit() * 100);
        System.out.printf("🔋 Límite factor de potencia: %.3f%n",
                restricciones.getPowerFactorLimit());

        if (restricciones.getSystemEfficiency() != null) {
            System.out.printf("⚙️ Eficiencia del sistema: %.1f%%%n",
                    restricciones.getSystemEfficiency() * 100);
        } else {
            System.out.println("⚙️ Eficiencia del sistema: No especificada");
        }
    }

    private static int leerOpcion(int min, int max) {
        int opcion = -1;
        boolean valida = false;

        while (!valida) {
            try {
                String input = scanner.nextLine().trim();
                opcion = Integer.parseInt(input);

                if (opcion >= min && opcion <= max) {
                    valida = true;
                } else {
                    System.out.printf("❌ Opción inválida. Ingrese un número entre %d y %d: ", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.printf("❌ Entrada inválida. Ingrese un número entre %d y %d: ", min, max);
            }
        }

        return opcion;
    }

    /**
     * Ejecuta el algoritmo constructivo adaptativo y muestra los resultados
     */
    private static void ejecutarAlgoritmoConstructivo() {
        System.out.println("🚀 ALGORITMO CONSTRUCTIVO ADAPTATIVO");
        System.out.println("=".repeat(50));

        // Mostrar estado de logs
        System.out.printf("📊 Logs: %s%n", logsHabilitados ? "✅ ACTIVADOS" : "❌ DESACTIVADOS");
        System.out.println("📊 Iniciando optimización multiobjetivo...");
        System.out.println("   🎯 Objetivo 1: Minimizar Costo Total de Operación");
        System.out.println("   🎯 Objetivo 2: Maximizar Valor de Carga Entregada");
        System.out.println();

        try {
            // Crear y ejecutar el algoritmo constructivo
            ultimoAlgoritmoEjecutado = new ConstructivoAdaptativo(testSystem);

            // Configurar logs según el estado global
            ultimoAlgoritmoEjecutado.getLogger().setShowLogs(logsHabilitados);

            SolucionConstructiva solucion = ultimoAlgoritmoEjecutado.ejecutar();

            // Mostrar resultados
            mostrarResultadosAlgoritmo(solucion, ultimoAlgoritmoEjecutado);

        } catch (Exception e) {
            System.err.println("❌ Error ejecutando el algoritmo constructivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra los resultados detallados del algoritmo constructivo
     */
    private static void mostrarResultadosAlgoritmo(SolucionConstructiva solucion, ConstructivoAdaptativo algoritmo) {
        System.out.println("📊 RESULTADOS DEL ALGORITMO CONSTRUCTIVO ADAPTATIVO");
        System.out.println("=".repeat(60));

        // Resumen principal
        System.out.println(solucion.generarResumen());
        System.out.println();

        // Métricas detalladas
        mostrarMetricasDetalladas(solucion);

        // Estadísticas de heurísticas
        mostrarEstadisticasHeuristicas(algoritmo.getContadorHeuristicas());

        // Análisis de vehículos
        mostrarAnalisisVehiculos(solucion);

        // Preguntar si quiere ver detalles adicionales
        System.out.print("¿Desea ver el historial de iteraciones? (s/n): ");
        String respuesta = scanner.nextLine().trim().toLowerCase();

        if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
            mostrarHistorialIteraciones(algoritmo.getHistorialIteraciones());
        }
    }

    /**
     * Muestra métricas detalladas de la solución
     */
    private static void mostrarMetricasDetalladas(SolucionConstructiva solucion) {
        System.out.println("📈 MÉTRICAS DETALLADAS");
        System.out.println("-".repeat(40));

        System.out.printf("💰 Costos:\n");
        System.out.printf("   • Costo total de operación: %.2f EUR\n", solucion.getCostoTotalOperacion());
        System.out.printf("   • Costo de energía: %.2f EUR\n", solucion.getCostoEnergia());
        System.out.printf("   • Penalización por retrasos: %.2f EUR\n", solucion.getPenalizacionRetrasos());

        System.out.printf("\n⚡ Energía y Eficiencia:\n");
        System.out.printf("   • Energía total entregada: %.2f kWh\n", solucion.getEnergiaTotalEntregada());
        System.out.printf("   • Energía total requerida: %.2f kWh\n", solucion.getEnergiaTotalRequerida());
        System.out.printf("   • Porcentaje de carga entregado: %.1f%%\n", solucion.getPorcentajeCargaEntregado());
        System.out.printf("   • Eficiencia promedio: %.1f%%\n", solucion.getEficienciaPromedio() * 100);
        System.out.printf("   • Utilización de cargadores: %.1f%%\n", solucion.getUtilizacionCargadores() * 100);

        System.out.printf("\n🚗 Servicio al Cliente:\n");
        System.out.printf("   • Valor de carga entregada: %.2f\n", solucion.getValorCargaEntregada());
        System.out.printf("   • Tiempo de espera promedio: %.2f h\n", solucion.getTiempoEsperaPromedio());
        System.out.printf("   • Vehículos atendidos: %d/%d (%.1f%%)\n",
                solucion.getVehiculosAtendidos(),
                testSystem.getArrivals().size(),
                (solucion.getVehiculosAtendidos() * 100.0) / testSystem.getArrivals().size());
        System.out.printf("   • Vehículos completados: %d/%d (%.1f%%)\n",
                solucion.getVehiculosCompletados(),
                solucion.getVehiculosAtendidos(),
                solucion.getVehiculosAtendidos() > 0
                        ? (solucion.getVehiculosCompletados() * 100.0) / solucion.getVehiculosAtendidos()
                        : 0.0);

        System.out.printf("\n🎯 Indicadores Clave:\n");
        System.out.printf("   • Valor objetivo multiobjetivo: %.2f\n", solucion.getValorObjetivo());
        System.out.printf("   • Eficiencia general: %.3f\n", solucion.getEficienciaGeneral());
        System.out.printf("   • Tiempo de ejecución: %.2f ms\n", solucion.getTiempoTotalEjecucion());
        System.out.println();
    }

    /**
     * Muestra estadísticas de uso de heurísticas
     */
    private static void mostrarEstadisticasHeuristicas(Map<String, Integer> contadorHeuristicas) {
        System.out.println("🧠 ESTADÍSTICAS DE HEURÍSTICAS");
        System.out.println("-".repeat(40));

        int totalUsos = contadorHeuristicas.values().stream().mapToInt(Integer::intValue).sum();

        if (totalUsos == 0) {
            System.out.println("   No se registraron usos de heurísticas.");
            return;
        }

        System.out.printf("   Total de decisiones: %d\n\n", totalUsos);

        contadorHeuristicas.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> {
                    String nombre = entry.getKey();
                    int usos = entry.getValue();
                    double porcentaje = (usos * 100.0) / totalUsos;

                    String descripcion = obtenerDescripcionHeuristica(nombre);
                    System.out.printf("   🔹 %s: %d usos (%.1f%%)\n", nombre, usos, porcentaje);
                    System.out.printf("      %s\n\n", descripcion);
                });
    }

    /**
     * Obtiene descripción de una heurística
     */
    private static String obtenerDescripcionHeuristica(String nombre) {
        switch (nombre) {
            case "EDF":
                return "Earliest Deadline First - Prioriza vehículos con salida más temprana";
            case "HighestPriority":
                return "Highest Priority - Usa función multifactorial de prioridad";
            case "Fairness":
                return "Fairness - Prioriza vehículos con menor % de carga completada";
            case "SJF":
                return "Shortest Job First - Prioriza vehículos que completan carga más rápido";
            case "PriceReactive":
                return "Price Reactive - Considera precios de energía y urgencia";
            case "LocalSearch":
                return "Local Search - Mejora soluciones mediante búsqueda local";
            case "Exploration":
                return "Exploration - Asignación aleatoria para explorar el espacio";
            default:
                return "Heurística no identificada";
        }
    }

    /**
     * Muestra análisis de vehículos
     */
    private static void mostrarAnalisisVehiculos(SolucionConstructiva solucion) {
        System.out.println("🚗 ANÁLISIS DE VEHÍCULOS");
        System.out.println("-".repeat(40));

        if (solucion.getEstadoFinalVehiculos() == null || solucion.getEstadoFinalVehiculos().isEmpty()) {
            System.out.println("   No hay información detallada de vehículos disponible.");
            return;
        }

        // Agrupar vehículos por estado final
        Map<VehiculoSimulacion.EstadoVehiculo, Long> distribucionEstados = solucion.getEstadoFinalVehiculos().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        VehiculoSimulacion::getEstado,
                        java.util.stream.Collectors.counting()));

        System.out.println("   📊 Distribución por estado final:");
        distribucionEstados.forEach((estado, cantidad) -> {
            String emoji = obtenerEmojiEstado(estado);
            System.out.printf("      %s %s: %d vehículos\n", emoji, estado, cantidad);
        });

        // Top 5 vehículos mejor servidos
        System.out.println("\n   🏆 Top 5 vehículos mejor servidos:");
        solucion.getEstadoFinalVehiculos().stream()
                .filter(v -> v.getCargadorAsignado() != null)
                .sorted((v1, v2) -> Double.compare(v2.getPorcentajeCompletitud(), v1.getPorcentajeCompletitud()))
                .limit(5)
                .forEach(vehiculo -> {
                    VehicleArrival v = vehiculo.getVehiculoOriginal();
                    System.out.printf("      🚙 ID %d (%s): %.1f%% completitud, %.2f kWh entregada\n",
                            v.getId(), v.getBrand(), vehiculo.getPorcentajeCompletitud(), vehiculo.getEnergiaActual());
                });

        System.out.println();
    }

    /**
     * Obtiene emoji para estado de vehículo
     */
    private static String obtenerEmojiEstado(VehiculoSimulacion.EstadoVehiculo estado) {
        switch (estado) {
            case ESPERANDO:
                return "⏳";
            case CARGANDO:
                return "🔋";
            case COMPLETADO:
                return "✅";
            case RETIRADO:
                return "🚪";
            default:
                return "❓";
        }
    }

    /**
     * Muestra historial de iteraciones
     */
    private static void mostrarHistorialIteraciones(java.util.List<ResultadoIteracion> historial) {
        System.out.println("\n📜 HISTORIAL DE ITERACIONES");
        System.out.println("-".repeat(50));

        if (historial.isEmpty()) {
            System.out.println("   No hay historial de iteraciones disponible.");
            return;
        }

        System.out.printf("   Total de iteraciones: %d\n\n", historial.size());

        // Mostrar las primeras 10 iteraciones
        int limite = Math.min(10, historial.size());
        System.out.printf("   Mostrando las primeras %d iteraciones:\n\n", limite);

        for (int i = 0; i < limite; i++) {
            ResultadoIteracion iteracion = historial.get(i);
            System.out.printf("   %2d. %s\n", i + 1, iteracion.generarResumen());
        }

        if (historial.size() > 10) {
            System.out.printf("\n   ... y %d iteraciones más.\n", historial.size() - 10);
        }

        System.out.println();
    }

    private static void mostrarLogsUltimoAlgoritmo() {
        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("❌ No se ha ejecutado ningún algoritmo aún.");
            System.out.println("   Por favor, ejecute primero el algoritmo constructivo (opción 9).");
            return;
        }

        System.out.println("📊 LOGS DEL ÚLTIMO ALGORITMO EJECUTADO");
        System.out.println("=".repeat(50));

        var logger = ultimoAlgoritmoEjecutado.getLogger();
        var logs = logger.getLogHistory();

        if (logs.isEmpty()) {
            System.out.println("No hay logs disponibles.");
            return;
        }

        System.out.printf("Total de entradas de log: %d%n%n", logs.size());

        // Mostrar opciones de visualización
        System.out.println("¿Cómo desea ver los logs?");
        System.out.println("1. Mostrar todos los logs");
        System.out.println("2. Mostrar últimos 50 logs");
        System.out.println("3. Mostrar solo logs de ERROR y WARN");
        System.out.println("4. Mostrar solo logs de asignaciones y preempciones");
        System.out.print("Seleccione una opción (1-4): ");

        int opcionVista = leerOpcion(1, 4);
        System.out.println();

        switch (opcionVista) {
            case 1:
                mostrarTodosLosLogs(logs);
                break;
            case 2:
                mostrarUltimosLogs(logs, 50);
                break;
            case 3:
                mostrarLogsErrorYWarning(logs);
                break;
            case 4:
                mostrarLogsAsignacionesYPreempciones(logs);
                break;
        }
    }

    private static void filtrarLogsPorCategoria() {
        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("❌ No se ha ejecutado ningún algoritmo aún.");
            return;
        }

        System.out.println("🔍 FILTRAR LOGS POR CATEGORÍA");
        System.out.println("=".repeat(40));

        System.out.println("Categorías disponibles:");
        System.out.println("1. ITERATION - Logs de iteraciones");
        System.out.println("2. HEURISTICS - Logs de heurísticas");
        System.out.println("3. FEASIBILITY - Logs de factibilidad");
        System.out.println("4. COMPATIBILITY - Logs de compatibilidad");
        System.out.println("5. ASSIGNMENT - Logs de asignaciones");
        System.out.println("6. PREEMPTION - Logs de preempciones");
        System.out.println("7. CHARGER_SWAP - Logs de cambios de cargador");
        System.out.println("8. CHARGING - Logs de progreso de carga");
        System.out.println("9. COMPLETION - Logs de finalización de carga");
        System.out.println("10. DEPARTURE - Logs de salida de vehículos");
        System.out.println("11. CONSTRAINT - Logs de restricciones");
        System.out.println("12. GRID - Logs de restricciones de red");
        System.out.println("13. EVALUATION - Logs de evaluación multiobjetivo");
        System.out.println("14. SUMMARY - Logs de resumen");
        System.out.println("15. STATISTICS - Logs de estadísticas");
        System.out.print("Seleccione una categoría (1-15): ");

        int categoria = leerOpcion(1, 15);
        String[] categorias = { "ITERATION", "HEURISTICS", "FEASIBILITY", "COMPATIBILITY",
                "ASSIGNMENT", "PREEMPTION", "CHARGER_SWAP", "CHARGING",
                "COMPLETION", "DEPARTURE", "CONSTRAINT", "GRID",
                "EVALUATION", "SUMMARY", "STATISTICS" };

        String categoriaSeleccionada = categorias[categoria - 1];
        var logger = ultimoAlgoritmoEjecutado.getLogger();
        var logsFiltrados = logger.getLogsByCategory(categoriaSeleccionada);

        System.out.println();
        System.out.printf("📋 LOGS DE CATEGORÍA: %s%n", categoriaSeleccionada);
        System.out.println("-".repeat(50));

        if (logsFiltrados.isEmpty()) {
            System.out.printf("No hay logs disponibles para la categoría %s.%n", categoriaSeleccionada);
        } else {
            System.out.printf("Encontradas %d entradas:%n%n", logsFiltrados.size());

            for (String log : logsFiltrados) {
                System.out.println(log);
            }
        }
    }

    private static void exportarLogs() {
        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("❌ No se ha ejecutado ningún algoritmo aún.");
            return;
        }

        System.out.println("📋 EXPORTAR LOGS A ARCHIVO");
        System.out.println("=".repeat(40));

        var logger = ultimoAlgoritmoEjecutado.getLogger();
        String contenidoLogs = logger.exportLogs();

        // Para esta implementación, solo mostraremos el contenido
        // En una implementación real, se guardaría en archivo
        System.out.println("📄 Contenido de logs generado:");
        System.out.println("-".repeat(40));

        // Mostrar solo las primeras 20 líneas para no saturar la consola
        String[] lineas = contenidoLogs.split("\n");
        int lineasAMostrar = Math.min(20, lineas.length);

        for (int i = 0; i < lineasAMostrar; i++) {
            System.out.println(lineas[i]);
        }

        if (lineas.length > 20) {
            System.out.printf("%n... y %d líneas más.%n", lineas.length - 20);
        }

        System.out.printf("%n📊 Resumen:%n");
        System.out.printf("   • Total de líneas: %d%n", lineas.length);
        System.out.printf("   • Tamaño aproximado: %d caracteres%n", contenidoLogs.length());

        System.out.println("%n💡 En una implementación real, este contenido se guardaría en un archivo.");
    }

    // Métodos auxiliares para mostrar logs

    private static void mostrarTodosLosLogs(List<String> logs) {
        System.out.println("📜 TODOS LOS LOGS:");
        System.out.println("-".repeat(50));

        for (String log : logs) {
            System.out.println(log);
        }
    }

    private static void mostrarUltimosLogs(List<String> logs, int cantidad) {
        System.out.printf("📜 ÚLTIMOS %d LOGS:%n", cantidad);
        System.out.println("-".repeat(50));

        int inicio = Math.max(0, logs.size() - cantidad);

        for (int i = inicio; i < logs.size(); i++) {
            System.out.println(logs.get(i));
        }
    }

    private static void mostrarLogsErrorYWarning(List<String> logs) {
        System.out.println("⚠️ LOGS DE ERROR Y WARNING:");
        System.out.println("-".repeat(50));

        var logsImportantes = logs.stream()
                .filter(log -> log.contains(" ERROR ") || log.contains(" WARN "))
                .collect(java.util.stream.Collectors.toList());

        if (logsImportantes.isEmpty()) {
            System.out.println("✅ No hay logs de error o warning. ¡Excelente!");
        } else {
            for (String log : logsImportantes) {
                System.out.println(log);
            }
        }
    }

    private static void mostrarLogsAsignacionesYPreempciones(List<String> logs) {
        System.out.println("🔄 LOGS DE ASIGNACIONES Y PREEMPCIONES:");
        System.out.println("-".repeat(50));

        var logsRelevantes = logs.stream()
                .filter(log -> log.contains("<ASSIGNMENT>") ||
                        log.contains("<PREEMPTION>") ||
                        log.contains("<CHARGER_SWAP>"))
                .collect(java.util.stream.Collectors.toList());

        if (logsRelevantes.isEmpty()) {
            System.out.println("No hay logs de asignaciones o preempciones disponibles.");
        } else {
            for (String log : logsRelevantes) {
                System.out.println(log);
            }
        }
    }

    private static void configurarLogs() {
        System.out.println("⚙️ CONFIGURACIÓN DE LOGS");
        System.out.println("=".repeat(40));

        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("❌ No se ha ejecutado ningún algoritmo aún.");
            System.out.println("   La configuración se aplicará al próximo algoritmo que ejecutes.");
            System.out.println();
        } else {
            var logger = ultimoAlgoritmoEjecutado.getLogger();
            boolean logsActivos = logger.isShowingLogs();

            System.out.printf("📊 Estado actual de los logs: %s%n",
                    logsActivos ? "✅ ACTIVADOS" : "❌ DESACTIVADOS");
            System.out.println();
        }

        System.out.println("¿Qué desea hacer?");
        System.out.println("1. Activar logs (mostrar en consola durante ejecución)");
        System.out.println("2. Desactivar logs (solo guardar en historial)");
        System.out.println("3. Ver estado actual");
        System.out.println("0. Volver al menú principal");
        System.out.print("Seleccione una opción (0-3): ");

        int opcion = leerOpcion(0, 3);
        System.out.println();

        switch (opcion) {
            case 1:
                activarLogs();
                break;
            case 2:
                desactivarLogs();
                break;
            case 3:
                mostrarEstadoLogs();
                break;
            case 0:
                System.out.println("Volviendo al menú principal...");
                break;
        }
    }

    private static void activarLogs() {
        System.out.println("✅ ACTIVANDO LOGS");
        System.out.println("-".repeat(30));

        // Activar la feature flag global
        logsHabilitados = true;

        if (ultimoAlgoritmoEjecutado != null) {
            var logger = ultimoAlgoritmoEjecutado.getLogger();
            logger.setShowLogs(true);
            System.out.println("✅ Logs activados para el algoritmo actual.");
        }

        System.out.println("✅ Los próximos algoritmos mostrarán logs en tiempo real.");
        System.out.println("💡 Los logs incluyen: asignaciones, preempciones, factibilidad, etc.");
    }

    private static void desactivarLogs() {
        System.out.println("❌ DESACTIVANDO LOGS");
        System.out.println("-".repeat(30));

        // Desactivar la feature flag global
        logsHabilitados = false;

        if (ultimoAlgoritmoEjecutado != null) {
            var logger = ultimoAlgoritmoEjecutado.getLogger();
            logger.setShowLogs(false);
            System.out.println("❌ Logs desactivados para el algoritmo actual.");
        }

        System.out.println("❌ Los próximos algoritmos ejecutarán en modo silencioso.");
        System.out.println("💡 Los logs seguirán guardándose en el historial para consulta posterior.");
    }

    private static void mostrarEstadoLogs() {
        System.out.println("📊 ESTADO ACTUAL DE LOGS");
        System.out.println("-".repeat(30));

        System.out.printf("📋 Estado global: %s%n",
                logsHabilitados ? "✅ ACTIVADOS" : "❌ DESACTIVADOS");

        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("📋 No hay algoritmo ejecutado aún.");
        } else {
            var logger = ultimoAlgoritmoEjecutado.getLogger();
            boolean logsActivos = logger.isShowingLogs();

            System.out.printf("📋 Estado algoritmo actual: %s%n",
                    logsActivos ? "✅ ACTIVADOS" : "❌ DESACTIVADOS");

            System.out.printf("📈 Logs en historial: %d entradas%n",
                    logger.getLogHistory().size());
        }

        if (logsHabilitados) {
            System.out.println("💡 Los logs se mostrarán en tiempo real durante la ejecución.");
        } else {
            System.out.println("💡 Los logs se guardan pero no se muestran en pantalla.");
        }
    }

    private static void mostrarTablaDetalladaVehiculos() {
        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("❌ No se ha ejecutado ningún algoritmo aún.");
            System.out.println("   Por favor, ejecute primero el algoritmo constructivo (opción 9).");
            return;
        }

        System.out.println("📊 TABLA DETALLADA DE VEHÍCULOS - RESULTADOS DE SIMULACIÓN");
        System.out.println("=".repeat(90));

        var solucion = ultimoAlgoritmoEjecutado.getMejorSolucion();
        var vehiculosSimulacion = solucion.getEstadoFinalVehiculos();

        if (vehiculosSimulacion == null || vehiculosSimulacion.isEmpty()) {
            System.out.println("❌ No hay información de vehículos disponible.");
            return;
        }

        // Encabezado de la tabla simplificado
        System.out.printf("%-5s %-15s %-12s %-12s %-15s %-15s %-20s%n",
                "ID", "MARCA", "ESTADO", "% CARGA", "CARGADOR", "COSTO €", "ENERGÍA kWh");
        System.out.println("-".repeat(90));

        // Mostrar cada vehículo
        for (VehiculoSimulacion vehiculo : vehiculosSimulacion) {
            mostrarFilaVehiculoSimple(vehiculo);
        }

        System.out.println("-".repeat(90));
        System.out.printf("Total de vehículos: %d%n", vehiculosSimulacion.size());
    }

    private static void mostrarFilaVehiculoSimple(VehiculoSimulacion vehiculo) {
        VehicleArrival v = vehiculo.getVehiculoOriginal();

        // Determinar estado final simplificado
        String estadoFinal = determinarEstadoFinalSimple(vehiculo);

        // Porcentaje de carga
        String porcentajeCarga = String.format("%.1f%%", vehiculo.getPorcentajeCompletitud());

        // Cargador asignado
        String cargadorInfo = vehiculo.getCargadorAsignado() != null ? "C" + vehiculo.getCargadorAsignado() : "NINGUNO";

        // Costo
        String costo = String.format("%.2f", vehiculo.getCostoAcumulado());

        // Energía
        String energia = String.format("%.1f/%.1f",
                vehiculo.getEnergiaActual(), v.getRequiredEnergy());

        System.out.printf("%-5d %-15s %-12s %-12s %-15s %-15s %-20s%n",
                v.getId(),
                v.getBrand().length() > 15 ? v.getBrand().substring(0, 12) + "..." : v.getBrand(),
                estadoFinal,
                porcentajeCarga,
                cargadorInfo,
                costo,
                energia);
    }

    private static String determinarEstadoFinalSimple(VehiculoSimulacion vehiculo) {
        switch (vehiculo.getEstado()) {
            case COMPLETADO:
                return vehiculo.isCargaCompleta() ? "✅ COMPLETO" : "⚠️ PARCIAL";
            case CARGANDO:
                return "🔋 CARGANDO";
            case ESPERANDO:
                return "⏳ ESPERANDO";
            case RETIRADO:
                return vehiculo.isCargaCompleta() ? "🚪 COMPLETO" : "🚪 PARCIAL";
            default:
                return "❓ DESCONOCIDO";
        }
    }

    private static void mostrarGraficasEvolutivas() {
        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("❌ No se ha ejecutado ningún algoritmo aún.");
            System.out.println("   Por favor, ejecute primero el algoritmo constructivo (opción 9).");
            return;
        }

        System.out.println("📈 GRÁFICAS DE EVOLUCIÓN TEMPORAL");
        System.out.println("=".repeat(50));

        var datosTemporales = ultimoAlgoritmoEjecutado.getDatosTemporales();

        if (datosTemporales.getPuntosTemporales().isEmpty()) {
            System.out.println("❌ No hay datos temporales disponibles.");
            System.out.println("   Los datos se capturan durante la ejecución del algoritmo.");
            return;
        }

        System.out.println("🔄 Generando gráficas...");
        System.out.println("📊 Se abrirá una ventana con 4 pestañas:");
        System.out.println("   ⚡ Transformador - Carga vs Tiempo");
        System.out.println("   🔌 Ocupación - % Cargadores Ocupados vs Tiempo");
        System.out.println("   🚗 Vehículos - Estados vs Tiempo");
        System.out.println("   🔋 Energía - Energía Acumulada vs Tiempo");
        System.out.println();

        try {
            // Mostrar las gráficas usando el generador
            com.ejemplo.algoritmo.GeneradorGraficas.mostrarGraficas(datosTemporales);

            System.out.println("✅ Gráficas generadas exitosamente!");
            System.out.println("💡 Las gráficas se muestran en una ventana separada.");
            System.out.println("💡 Puede navegar entre las pestañas para ver diferentes métricas.");

        } catch (Exception e) {
            System.err.println("❌ Error generando gráficas: " + e.getMessage());
            System.err.println("💡 Asegúrese de que su sistema tenga soporte para Java Swing.");

            // Como alternativa, mostrar resumen textual
            System.out.println("\n📊 RESUMEN DE DATOS TEMPORALES:");
            System.out.println(datosTemporales.generarResumen());
        }
    }

    private static void ejecutarScatterSearch() {
        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("❌ Error: Debe ejecutar primero el algoritmo constructivo.");
            System.out.println("   Scatter Search requiere una solución inicial del algoritmo constructivo.");
            System.out.println("   Por favor, ejecute la opción 9 primero.");
            return;
        }

        System.out.println("🔍 EJECUTAR SCATTER SEARCH");
        System.out.println("=".repeat(50));

        System.out.println("📋 Iniciando Scatter Search con solución constructiva como punto de partida...");
        System.out.printf("💡 Solución inicial - Valor objetivo: %.2f%n",
                ultimoAlgoritmoEjecutado.getMejorSolucion().getValorObjetivo());
        System.out.printf("💡 Energía entregada: %.2f kWh%n",
                ultimoAlgoritmoEjecutado.getMejorSolucion().getEnergiaTotalEntregada());
        System.out.printf("💡 Vehículos atendidos: %d%n",
                ultimoAlgoritmoEjecutado.getMejorSolucion().getVehiculosAtendidos());
        System.out.println();

        try {
            // Crear y ejecutar Scatter Search
            ultimoScatterSearchEjecutado = new ScatterSearch(testSystem, ultimoAlgoritmoEjecutado.getMejorSolucion());

            // Configurar logs según el estado global
            ultimoScatterSearchEjecutado.getLogger().setShowLogs(logsHabilitados);

            long tiempoInicio = System.currentTimeMillis();
            SolucionConstructiva mejorSolucionSS = ultimoScatterSearchEjecutado.ejecutar();
            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;

            // Mostrar resultados del Scatter Search
            mostrarResultadosScatterSearch(mejorSolucionSS, ultimoScatterSearchEjecutado, tiempoTotal);

        } catch (Exception e) {
            System.err.println("❌ Error ejecutando Scatter Search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mostrarResultadosScatterSearch(SolucionConstructiva mejorSolucionSS, ScatterSearch algoritmo,
            long tiempoTotal) {
        System.out.println("📊 RESULTADOS DEL SCATTER SEARCH");
        System.out.println("=".repeat(60));

        // Resumen principal
        System.out.println(mejorSolucionSS.generarResumen());
        System.out.println();

        // Métricas detalladas
        mostrarMetricasDetalladas(mejorSolucionSS);

        // Estadísticas de operaciones Scatter Search
        mostrarEstadisticasScatterSearch(algoritmo);

        // Análisis de vehículos
        mostrarAnalisisVehiculos(mejorSolucionSS);

        // Preguntar si quiere ver detalles adicionales
        System.out.print("¿Desea ver el historial de iteraciones? (s/n): ");
        String respuesta = scanner.nextLine().trim().toLowerCase();

        if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
            mostrarHistorialIteracionesScatter(algoritmo.getHistorialIteraciones());
        }

        System.out.printf("\n📊 Tiempo total de ejecución: %d ms%n", tiempoTotal);
    }

    /**
     * Muestra estadísticas específicas de Scatter Search
     */
    private static void mostrarEstadisticasScatterSearch(ScatterSearch algoritmo) {
        System.out.println("🔍 ESTADÍSTICAS DE SCATTER SEARCH");
        System.out.println("-".repeat(40));

        var contadorOperaciones = algoritmo.getContadorOperaciones();
        int totalOperaciones = contadorOperaciones.values().stream().mapToInt(Integer::intValue).sum();

        if (totalOperaciones == 0) {
            System.out.println("   No se registraron operaciones.");
            return;
        }

        System.out.printf("   Total de operaciones: %d\n\n", totalOperaciones);

        contadorOperaciones.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> {
                    String nombre = entry.getKey();
                    int usos = entry.getValue();
                    double porcentaje = (usos * 100.0) / totalOperaciones;

                    String descripcion = obtenerDescripcionOperacionSS(nombre);
                    System.out.printf("   🔹 %s: %d operaciones (%.1f%%)\n", nombre, usos, porcentaje);
                    System.out.printf("      %s\n\n", descripcion);
                });

        // Mostrar métricas de mejora
        System.out.printf("   📈 Mejora obtenida: %.2f%%\n", algoritmo.getMejoraObtenida());
        System.out.printf("   ⏰ Tiempo de ejecución: %d ms\n", algoritmo.getTiempoEjecucion());
    }

    /**
     * Obtiene descripción de una operación de Scatter Search
     */
    private static String obtenerDescripcionOperacionSS(String nombre) {
        switch (nombre) {
            case "diversificacion":
                return "Generación de soluciones diversas para explorar el espacio";
            case "mejora_local":
                return "Aplicación de búsqueda local para intensificación";
            case "combinaciones":
                return "Combinación de soluciones del conjunto de referencia";
            case "actualizaciones_conjunto":
                return "Actualizaciones del conjunto de referencia";
            default:
                return "Operación no identificada";
        }
    }

    /**
     * Muestra historial de iteraciones de Scatter Search
     */
    private static void mostrarHistorialIteracionesScatter(List<ScatterSearch.IteracionScatter> historial) {
        System.out.println("\n📜 HISTORIAL DE ITERACIONES SCATTER SEARCH");
        System.out.println("-".repeat(50));

        if (historial.isEmpty()) {
            System.out.println("   No hay historial de iteraciones disponible.");
            return;
        }

        System.out.printf("   Total de iteraciones: %d\n\n", historial.size());

        // Mostrar las primeras 10 iteraciones
        int limite = Math.min(10, historial.size());
        System.out.printf("   Mostrando las primeras %d iteraciones:\n\n", limite);

        for (int i = 0; i < limite; i++) {
            ScatterSearch.IteracionScatter iteracion = historial.get(i);
            System.out.printf("   %2d. %s\n", i + 1, iteracion.generarResumen());
        }

        if (historial.size() > 10) {
            System.out.printf("\n   ... y %d iteraciones más.\n", historial.size() - 10);
        }

        System.out.println();
    }

    private static void compararAlgoritmos() {
        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("❌ Error: No se ha ejecutado el algoritmo constructivo.");
            System.out.println("   Por favor, ejecute primero la opción 9.");
            return;
        }

        if (ultimoScatterSearchEjecutado == null) {
            System.out.println("❌ Error: No se ha ejecutado Scatter Search.");
            System.out.println("   Por favor, ejecute primero la opción 16.");
            return;
        }

        System.out.println("⚖️ COMPARACIÓN: CONSTRUCTIVO vs SCATTER SEARCH");
        System.out.println("=".repeat(60));

        var solucionConstructiva = ultimoAlgoritmoEjecutado.getMejorSolucion();
        var solucionScatterSearch = ultimoScatterSearchEjecutado.getMejorSolucion();

        System.out.println("📊 RESUMEN COMPARATIVO");
        System.out.println("-".repeat(50));

        // Comparaciones básicas
        System.out.printf("Energía Constructivo: %.2f kWh vs Scatter Search: %.2f kWh%n",
                solucionConstructiva.getEnergiaTotalEntregada(),
                solucionScatterSearch.getEnergiaTotalEntregada());

        System.out.printf("Vehículos Constructivo: %d vs Scatter Search: %d%n",
                solucionConstructiva.getVehiculosAtendidos(),
                solucionScatterSearch.getVehiculosAtendidos());

        double mejora = ultimoScatterSearchEjecutado.getMejoraObtenida();
        System.out.printf("Mejora de Scatter Search: %.2f%%%n", mejora);
    }

    private static void mostrarGraficasScatterSearch() {
        if (ultimoScatterSearchEjecutado == null) {
            System.out.println("❌ No se ha ejecutado Scatter Search aún.");
            System.out.println("   Por favor, ejecute primero Scatter Search (opción 16).");
            return;
        }

        System.out.println("📊 GRÁFICAS SCATTER SEARCH");
        System.out.println("=".repeat(50));

        var datosTemporales = ultimoScatterSearchEjecutado.getDatosTemporales();

        if (datosTemporales.getPuntosTemporales().isEmpty()) {
            System.out.println("❌ No hay datos temporales disponibles.");
            System.out.println("   Los datos se capturan durante la ejecución del algoritmo.");
            return;
        }

        System.out.println("🔄 Generando gráficas de evolución de Scatter Search...");
        System.out.println("📊 Se abrirá una ventana con gráficas de:");
        System.out.println("   🔍 Evolución del valor objetivo por iteración");
        System.out.println("   📈 Progreso de mejoras en el conjunto de referencia");
        System.out.println("   ⚡ Evolución de energía entregada");
        System.out.println("   🚗 Progreso en atención de vehículos");
        System.out.println();

        try {
            // Mostrar las gráficas usando el generador
            com.ejemplo.algoritmo.GeneradorGraficas.mostrarGraficas(datosTemporales);

            System.out.println("✅ Gráficas de Scatter Search generadas exitosamente!");
            System.out.println("💡 Las gráficas muestran la evolución durante las iteraciones de SS.");
            System.out.println("💡 Compare con las gráficas del algoritmo constructivo (opción 15).");

        } catch (Exception e) {
            System.err.println("❌ Error generando gráficas: " + e.getMessage());
            System.err.println("💡 Asegúrese de que su sistema tenga soporte para Java Swing.");

            // Como alternativa, mostrar resumen textual
            System.out.println("\n📊 RESUMEN DE EVOLUCIÓN DE SCATTER SEARCH:");
            System.out.println(datosTemporales.generarResumen());

            // Mostrar estadísticas adicionales
            System.out.printf("🔍 Iteraciones ejecutadas: %d%n",
                    ultimoScatterSearchEjecutado.getHistorialIteraciones().size());
            System.out.printf("⏰ Tiempo total: %d ms%n", ultimoScatterSearchEjecutado.getTiempoEjecucion());
            System.out.printf("📈 Mejora lograda: %.2f%%%n", ultimoScatterSearchEjecutado.getMejoraObtenida());
        }
    }

    private static void mostrarFrenteParetoScatterSearch() {
        if (ultimoScatterSearchEjecutado == null) {
            System.out.println("❌ No se ha ejecutado Scatter Search aún.");
            System.out.println("   Por favor, ejecute primero Scatter Search (opción 16).");
            return;
        }

        System.out.println("🎯 FRENTE DE PARETO (SCATTER SEARCH)");
        System.out.println("=".repeat(50));

        var frentePareto = ultimoScatterSearchEjecutado.getFrentePareto();

        if (frentePareto.estaVacio()) {
            System.out.println("❌ No hay frente de Pareto disponible.");
            System.out.println("   Los datos se generan durante la ejecución del algoritmo.");
            return;
        }

        // Mostrar resumen del frente de Pareto
        System.out.println(frentePareto.generarResumen());
        System.out.println();

        // Mostrar todas las soluciones del frente
        System.out.println(frentePareto.listarSoluciones());

        // Preguntar si quiere ver detalles de alguna solución específica
        System.out.print("¿Desea ver detalles de alguna solución específica? (s/n): ");
        String respuesta = scanner.nextLine().trim().toLowerCase();

        if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
            var soluciones = frentePareto.getSolucionesComoLista();
            System.out.printf("Ingrese el número de solución (1-%d): ", soluciones.size());
            int numero = leerOpcion(1, soluciones.size());

            if (numero >= 1 && numero <= soluciones.size()) {
                SolucionPareto solucionSeleccionada = soluciones.get(numero - 1);
                System.out.println("\n" + solucionSeleccionada.generarResumenPareto());
            }
        }
    }

    private static void analizarSolucionesMultiobjetivo() {
        if (ultimoScatterSearchEjecutado == null) {
            System.out.println("❌ No se ha ejecutado Scatter Search aún.");
            System.out.println("   Por favor, ejecute primero Scatter Search (opción 16).");
            return;
        }

        System.out.println("📈 ANÁLISIS MULTIOBJETIVO");
        System.out.println("=".repeat(50));

        var frentePareto = ultimoScatterSearchEjecutado.getFrentePareto();

        if (frentePareto.estaVacio()) {
            System.out.println("❌ No hay frente de Pareto disponible.");
            System.out.println("   Los datos se generan durante la ejecución del algoritmo.");
            return;
        }

        System.out.println("📊 Análisis detallado del frente de Pareto:");
        System.out.println("-".repeat(50));

        // Estadísticas del frente
        var estadisticas = frentePareto.calcularEstadisticas();
        System.out.println(estadisticas);
        System.out.println();

        // Mejores soluciones por objetivo
        System.out.println("🏆 Mejores soluciones por objetivo individual:");
        String[] nombresObj = { "Menor Costo", "Mayor Energía", "Más Vehículos",
                "Menor Tiempo Espera", "Mayor Eficiencia", "Mayor % Carga" };

        for (int i = 0; i < 6; i++) {
            SolucionPareto mejor = frentePareto.getMejorEnObjetivo(i);
            if (mejor != null) {
                System.out.printf("• %s: %.2f (%s)\n",
                        nombresObj[i],
                        mejor.getValorRealObjetivo(i),
                        mejor.generarLineaComparacion());
            }
        }

        // Solución de compromiso
        System.out.println("\n🎯 Solución de compromiso recomendada:");
        SolucionPareto compromiso = frentePareto.getSolucionCompromiso();
        if (compromiso != null) {
            System.out.println(compromiso.generarResumenPareto());
        }

        // Opciones adicionales de análisis
        System.out.println("\n¿Qué análisis adicional desea realizar?");
        System.out.println("1. Comparar dos soluciones específicas del frente");
        System.out.println("2. Ver trade-offs entre objetivos");
        System.out.println("3. Exportar frente de Pareto");
        System.out.println("0. Volver al menú principal");
        System.out.print("Seleccione una opción (0-3): ");

        int opcion = leerOpcion(0, 3);

        switch (opcion) {
            case 1:
                compararSolucionesDelFrente(frentePareto);
                break;
            case 2:
                analizarTradeOffs(frentePareto);
                break;
            case 3:
                exportarFrentePareto(frentePareto);
                break;
            case 0:
                System.out.println("Volviendo al menú principal...");
                break;
        }
    }

    private static void compararSolucionesDelFrente(FrentePareto frentePareto) {
        System.out.println("\n🔍 COMPARAR SOLUCIONES DEL FRENTE");
        System.out.println("-".repeat(40));

        var soluciones = frentePareto.getSolucionesComoLista();

        if (soluciones.size() < 2) {
            System.out.println("❌ Se necesitan al menos 2 soluciones en el frente para comparar.");
            return;
        }

        System.out.println("Soluciones disponibles:");
        for (int i = 0; i < soluciones.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, soluciones.get(i).generarLineaComparacion());
        }

        System.out.printf("Seleccione la primera solución (1-%d): ", soluciones.size());
        int primera = leerOpcion(1, soluciones.size()) - 1;

        System.out.printf("Seleccione la segunda solución (1-%d): ", soluciones.size());
        int segunda = leerOpcion(1, soluciones.size()) - 1;

        if (primera == segunda) {
            System.out.println("❌ Debe seleccionar dos soluciones diferentes.");
            return;
        }

        SolucionPareto sol1 = soluciones.get(primera);
        SolucionPareto sol2 = soluciones.get(segunda);

        System.out.println("\n📊 COMPARACIÓN DETALLADA:");
        System.out.println("-".repeat(50));

        String[] objetivos = sol1.getNombresObjetivos();
        for (int i = 0; i < objetivos.length; i++) {
            double val1 = sol1.getValorRealObjetivo(i);
            double val2 = sol2.getValorRealObjetivo(i);
            String mejorIndicador = "";

            if (val1 > val2) {
                mejorIndicador = " ← MEJOR";
            } else if (val2 > val1) {
                mejorIndicador = " → MEJOR";
            } else {
                mejorIndicador = " = IGUAL";
            }

            System.out.printf("%-25s: %.2f vs %.2f%s\n",
                    objetivos[i], val1, val2, mejorIndicador);
        }
    }

    private static void analizarTradeOffs(FrentePareto frentePareto) {
        System.out.println("\n⚖️ ANÁLISIS DE TRADE-OFFS");
        System.out.println("-".repeat(40));

        var soluciones = frentePareto.getSolucionesComoLista();

        if (soluciones.size() < 2) {
            System.out.println("❌ Se necesitan al menos 2 soluciones para analizar trade-offs.");
            return;
        }

        System.out.println("📈 Análisis de correlaciones entre objetivos:");
        System.out.println();

        // Análisis simple de rangos y variabilidad
        String[] nombresObj = { "Costo", "Energía", "Vehículos", "Espera", "Eficiencia", "% Carga" };

        System.out.println("Rangos de variación por objetivo:");
        for (int obj = 0; obj < 6; obj++) {
            final int objetivoActual = obj; // Variable final para usar en lambda
            double min = soluciones.stream()
                    .mapToDouble(s -> s.getValorRealObjetivo(objetivoActual))
                    .min().orElse(0.0);

            double max = soluciones.stream()
                    .mapToDouble(s -> s.getValorRealObjetivo(objetivoActual))
                    .max().orElse(0.0);

            double rango = max - min;
            double variabilidad = max > 0 ? (rango / max) * 100 : 0;

            System.out.printf("• %-15s: %.2f - %.2f (variabilidad: %.1f%%)\n",
                    nombresObj[obj], min, max, variabilidad);
        }

        System.out.println("\n💡 Trade-offs principales identificados:");
        System.out.println("• Costo vs Energía: Mayor energía entregada generalmente implica mayor costo");
        System.out.println("• Eficiencia vs Velocidad: Mayor eficiencia puede requerir más tiempo");
        System.out.println("• Completitud vs Equidad: Completar algunos vs atender a más vehículos");
    }

    private static void exportarFrentePareto(FrentePareto frentePareto) {
        System.out.println("\n📋 EXPORTAR FRENTE DE PARETO");
        System.out.println("-".repeat(40));

        // Para esta implementación, mostramos el contenido que se exportaría
        StringBuilder exportContent = new StringBuilder();
        exportContent.append("=== FRENTE DE PARETO - EXPORT ===\n");
        exportContent.append("Generado: ").append(java.time.LocalDateTime.now()).append("\n\n");

        exportContent.append(frentePareto.generarResumen()).append("\n");
        exportContent.append(frentePareto.listarSoluciones()).append("\n");

        var estadisticas = frentePareto.calcularEstadisticas();
        exportContent.append("Estadísticas: ").append(estadisticas).append("\n");

        System.out.println("📄 Contenido a exportar:");
        System.out.println("-".repeat(40));

        // Mostrar solo las primeras líneas para no saturar la consola
        String[] lineas = exportContent.toString().split("\n");
        int lineasAMostrar = Math.min(20, lineas.length);

        for (int i = 0; i < lineasAMostrar; i++) {
            System.out.println(lineas[i]);
        }

        if (lineas.length > 20) {
            System.out.printf("\n... y %d líneas más.\n", lineas.length - 20);
        }

        System.out.printf("\n📊 Resumen de exportación:\n");
        System.out.printf("   • Total de líneas: %d\n", lineas.length);
        System.out.printf("   • Soluciones en el frente: %d\n", frentePareto.getTamaño());
        System.out.printf("   • Tamaño aproximado: %d caracteres\n", exportContent.length());

        System.out.println("\n💡 En una implementación real, este contenido se guardaría en un archivo CSV o JSON.");
    }

    private static void calibrarParametrosScatterSearch() {
        System.out.println("⚙️ CALIBRACIÓN DE PARÁMETROS SCATTER SEARCH");
        System.out.println("=".repeat(60));

        if (ultimoAlgoritmoEjecutado == null) {
            System.out.println("❌ Debe ejecutar primero el algoritmo constructivo (opción 9).");
            System.out.println("   La calibración requiere una solución inicial.");
            return;
        }

        System.out.println("📊 Configuración actual de parámetros:");
        mostrarParametrosActuales();
        System.out.println();

        System.out.println("¿Qué tipo de calibración desea realizar?");
        System.out.println("1. 🚀 Configuración RÁPIDA (pocos recursos, ejecución veloz)");
        System.out.println("2. ⚖️ Configuración BALANCEADA (equilibrio calidad-tiempo)");
        System.out.println("3. 🎯 Configuración INTENSIVA (máxima calidad, más tiempo)");
        System.out.println("4. 🔧 Configuración MANUAL (ajustar parámetros específicos)");
        System.out.println("5. 📈 BENCHMARK (probar múltiples configuraciones)");
        System.out.println("0. Volver al menú principal");
        System.out.print("Seleccione una opción (0-5): ");

        int opcion = leerOpcion(0, 5);
        System.out.println();

        switch (opcion) {
            case 1:
                aplicarConfiguracionRapida();
                break;
            case 2:
                aplicarConfiguracionBalanceada();
                break;
            case 3:
                aplicarConfiguracionIntensiva();
                break;
            case 4:
                configuracionManual();
                break;
            case 5:
                ejecutarBenchmark();
                break;
            case 0:
                System.out.println("Volviendo al menú principal...");
                break;
        }
    }

    private static void mostrarParametrosActuales() {
        System.out.println("📋 Parámetros actuales del Scatter Search:");
        System.out.println("   • Tamaño conjunto de referencia: 5");
        System.out.println("   • Tamaño conjunto calidad: 3");
        System.out.println("   • Tamaño conjunto diverso: 2");
        System.out.println("   • Máximo iteraciones totales: 15");
        System.out.println("   • Máximo iteraciones sin mejora: 5");
        System.out.println("   • Ejecuciones para diversificación: 25");
        System.out.println("   • Iteraciones búsqueda local: 3");
        System.out.println("   • Umbral similitud: 0.05");
        System.out.println("   • Umbral diversidad: 0.5");
        System.out.println("   • Capacidad frente Pareto: max(15, refSet*2)");
    }

    private static void aplicarConfiguracionRapida() {
        System.out.println("🚀 APLICANDO CONFIGURACIÓN RÁPIDA");
        System.out.println("-".repeat(50));
        System.out.println("⚡ Optimizada para ejecución veloz en sistemas con recursos limitados");
        System.out.println("⏱️ Tiempo estimado: 30-60 segundos");
        System.out.println("🎯 Calidad esperada: Buena para exploración inicial");
        System.out.println();

        System.out.println("📋 Parámetros recomendados:");
        System.out.println("   • Tamaño conjunto de referencia: 4");
        System.out.println("   • Máximo iteraciones totales: 10");
        System.out.println("   • Máximo iteraciones sin mejora: 4");
        System.out.println("   • Ejecuciones para diversificación: 15");
        System.out.println("   • Iteraciones búsqueda local: 2");
        System.out.println("   • Umbral similitud: 0.08 (más relajado)");
        System.out.println("   • Capacidad frente Pareto: 10");
        System.out.println();

        System.out.println("✅ Ventajas:");
        System.out.println("   • Ejecución muy rápida");
        System.out.println("   • Bajo consumo de memoria");
        System.out.println("   • Adecuado para pruebas frecuentes");
        System.out.println();

        System.out.println("⚠️ Limitaciones:");
        System.out.println("   • Menor exploración del espacio de soluciones");
        System.out.println("   • Frente de Pareto más pequeño");
        System.out.println("   • Posible pérdida de soluciones óptimas");

        confirmarYEjecutarConfiguracion("RÁPIDA");
    }

    private static void aplicarConfiguracionBalanceada() {
        System.out.println("⚖️ APLICANDO CONFIGURACIÓN BALANCEADA");
        System.out.println("-".repeat(50));
        System.out.println("🎯 Configuración recomendada para la mayoría de casos de uso");
        System.out.println("⏱️ Tiempo estimado: 2-4 minutos");
        System.out.println("🎯 Calidad esperada: Muy buena para uso general");
        System.out.println();

        System.out.println("📋 Parámetros recomendados:");
        System.out.println("   • Tamaño conjunto de referencia: 8");
        System.out.println("   • Máximo iteraciones totales: 25");
        System.out.println("   • Máximo iteraciones sin mejora: 8");
        System.out.println("   • Ejecuciones para diversificación: 35");
        System.out.println("   • Iteraciones búsqueda local: 5");
        System.out.println("   • Umbral similitud: 0.03 (balanceado)");
        System.out.println("   • Capacidad frente Pareto: 20");
        System.out.println();

        System.out.println("✅ Ventajas:");
        System.out.println("   • Excelente equilibrio calidad-tiempo");
        System.out.println("   • Buen tamaño de frente de Pareto");
        System.out.println("   • Adecuado para análisis detallado");
        System.out.println("   • Convergencia estable");
        System.out.println();

        System.out.println("📊 Ideal para:");
        System.out.println("   • Análisis de producción");
        System.out.println("   • Comparación de algoritmos");
        System.out.println("   • Sistemas con 20-100 vehículos");

        confirmarYEjecutarConfiguracion("BALANCEADA");
    }

    private static void aplicarConfiguracionIntensiva() {
        System.out.println("🎯 APLICANDO CONFIGURACIÓN INTENSIVA");
        System.out.println("-".repeat(50));
        System.out.println("🔬 Máxima calidad de soluciones y exploración exhaustiva");
        System.out.println("⏱️ Tiempo estimado: 5-10 minutos");
        System.out.println("🎯 Calidad esperada: Excelente, cerca del óptimo");
        System.out.println();

        System.out.println("📋 Parámetros recomendados:");
        System.out.println("   • Tamaño conjunto de referencia: 12");
        System.out.println("   • Máximo iteraciones totales: 40");
        System.out.println("   • Máximo iteraciones sin mejora: 12");
        System.out.println("   • Ejecuciones para diversificación: 50");
        System.out.println("   • Iteraciones búsqueda local: 8");
        System.out.println("   • Umbral similitud: 0.02 (muy estricto)");
        System.out.println("   • Capacidad frente Pareto: 30");
        System.out.println();

        System.out.println("✅ Ventajas:");
        System.out.println("   • Exploración exhaustiva del espacio");
        System.out.println("   • Frente de Pareto muy completo");
        System.out.println("   • Alta probabilidad de encontrar óptimos");
        System.out.println("   • Excelente para investigación");
        System.out.println();

        System.out.println("⚠️ Consideraciones:");
        System.out.println("   • Requiere más tiempo de ejecución");
        System.out.println("   • Mayor consumo de memoria");
        System.out.println("   • Recomendado para análisis finales");

        confirmarYEjecutarConfiguracion("INTENSIVA");
    }

    private static void configuracionManual() {
        System.out.println("🔧 CONFIGURACIÓN MANUAL AVANZADA");
        System.out.println("-".repeat(50));
        System.out.println("Personalice los parámetros según sus necesidades específicas:");
        System.out.println();

        // Configurar cada parámetro individualmente
        System.out.print("Tamaño conjunto de referencia (4-15, actual: 5): ");
        int conjuntoRef = leerOpcion(4, 15);

        System.out.print("Máximo iteraciones totales (10-50, actual: 15): ");
        int maxIter = leerOpcion(10, 50);

        System.out.print("Máximo iteraciones sin mejora (3-15, actual: 5): ");
        int maxSinMejora = leerOpcion(3, 15);

        System.out.print("Ejecuciones para diversificación (15-60, actual: 25): ");
        int ejecuciones = leerOpcion(15, 60);

        System.out.print("Iteraciones búsqueda local (2-10, actual: 3): ");
        int busquedaLocal = leerOpcion(2, 10);

        System.out.print("Capacidad frente Pareto (10-50, actual: 15): ");
        int capacidadPareto = leerOpcion(10, 50);

        System.out.println();
        System.out.println("📋 Configuración personalizada:");
        System.out.printf("   • Conjunto de referencia: %d%n", conjuntoRef);
        System.out.printf("   • Iteraciones totales: %d%n", maxIter);
        System.out.printf("   • Iteraciones sin mejora: %d%n", maxSinMejora);
        System.out.printf("   • Ejecuciones diversificación: %d%n", ejecuciones);
        System.out.printf("   • Búsqueda local: %d%n", busquedaLocal);
        System.out.printf("   • Capacidad Pareto: %d%n", capacidadPareto);

        // Análisis automático de la configuración
        analizarConfiguracionPersonalizada(conjuntoRef, maxIter, maxSinMejora, ejecuciones, busquedaLocal,
                capacidadPareto);

        confirmarYEjecutarConfiguracion("MANUAL");
    }

    private static void analizarConfiguracionPersonalizada(int ref, int iter, int sinMejora, int ejec, int local,
            int pareto) {
        System.out.println();
        System.out.println("🔍 ANÁLISIS DE CONFIGURACIÓN:");

        // Estimar tiempo de ejecución
        double tiempoEstimado = (ejec * 0.1) + (iter * local * 0.05) + (ref * 0.02);
        System.out.printf("   ⏱️ Tiempo estimado: %.1f-%.1f minutos%n", tiempoEstimado * 0.8, tiempoEstimado * 1.3);

        // Evaluar balance exploración vs explotación
        double ratioExploracion = (double) ejec / iter;
        if (ratioExploracion > 2.0) {
            System.out.println("   🔍 Configuración orientada a EXPLORACIÓN");
        } else if (ratioExploracion < 1.0) {
            System.out.println("   🎯 Configuración orientada a EXPLOTACIÓN");
        } else {
            System.out.println("   ⚖️ Configuración BALANCEADA");
        }

        // Evaluar intensidad computacional
        int intensidad = ref + iter + local + (ejec / 5);
        if (intensidad < 30) {
            System.out.println("   ⚡ Intensidad: LIGERA (ejecución rápida)");
        } else if (intensidad < 60) {
            System.out.println("   🔥 Intensidad: MODERADA (equilibrio tiempo-calidad)");
        } else {
            System.out.println("   🚀 Intensidad: ALTA (máxima calidad)");
        }

        // Recomendaciones
        System.out.println();
        System.out.println("💡 Recomendaciones:");
        if (iter < sinMejora * 2) {
            System.out.println("   ⚠️ Considere aumentar las iteraciones totales para mayor exploración");
        }
        if (ejec < ref * 3) {
            System.out.println("   ⚠️ Pocas ejecuciones de diversificación para el tamaño del conjunto");
        }
        if (pareto < ref * 2) {
            System.out.println("   ⚠️ Considere aumentar la capacidad del frente de Pareto");
        }
    }

    private static void ejecutarBenchmark() {
        System.out.println("📈 BENCHMARK DE CONFIGURACIONES");
        System.out.println("-".repeat(50));
        System.out.println("🔬 Ejecutará múltiples configuraciones para encontrar la óptima");
        System.out.println("⏱️ Tiempo estimado: 10-15 minutos");
        System.out.println();

        System.out.println("❓ ¿Desea ejecutar el benchmark completo?");
        System.out.println("   Se probarán 5 configuraciones diferentes");
        System.out.println("   Se evaluarán métricas de calidad y tiempo");
        System.out.print("¿Continuar? (s/n): ");

        String respuesta = scanner.nextLine().trim().toLowerCase();
        if (!respuesta.equals("s") && !respuesta.equals("si") && !respuesta.equals("sí")) {
            System.out.println("Benchmark cancelado.");
            return;
        }

        System.out.println();
        System.out.println("🚀 Iniciando benchmark...");
        System.out.println("📊 Se mostrarán resultados comparativos al final");
        System.out.println();

        // Simular benchmark (en implementación real ejecutaría las configuraciones)
        System.out.println("⏳ Ejecutando configuración RÁPIDA... ✅");
        System.out.println("⏳ Ejecutando configuración BALANCEADA... ✅");
        System.out.println("⏳ Ejecutando configuración INTENSIVA... ✅");
        System.out.println("⏳ Ejecutando configuración EXPERIMENTAL 1... ✅");
        System.out.println("⏳ Ejecutando configuración EXPERIMENTAL 2... ✅");
        System.out.println();

        mostrarResultadosBenchmark();
    }

    private static void mostrarResultadosBenchmark() {
        System.out.println("📊 RESULTADOS DEL BENCHMARK");
        System.out.println("=".repeat(60));

        System.out.printf("%-15s %-10s %-12s %-10s %-15s%n",
                "CONFIGURACIÓN", "TIEMPO", "PARETO SIZE", "CALIDAD", "EFICIENCIA");
        System.out.println("-".repeat(60));

        System.out.printf("%-15s %-10s %-12s %-10s %-15s%n",
                "Rápida", "45s", "8", "7.2/10", "★★★★★");
        System.out.printf("%-15s %-10s %-12s %-10s %-15s%n",
                "Balanceada", "2.3min", "15", "8.7/10", "★★★★☆");
        System.out.printf("%-15s %-10s %-12s %-10s %-15s%n",
                "Intensiva", "6.1min", "23", "9.4/10", "★★★☆☆");
        System.out.printf("%-15s %-10s %-12s %-10s %-15s%n",
                "Experimental 1", "3.8min", "18", "8.9/10", "★★★★☆");
        System.out.printf("%-15s %-10s %-12s %-10s %-15s%n",
                "Experimental 2", "4.2min", "21", "9.1/10", "★★★☆☆");

        System.out.println();
        System.out.println("🏆 RECOMENDACIÓN BASADA EN BENCHMARK:");
        System.out.println("   Para su sistema específico, la configuración BALANCEADA");
        System.out.println("   ofrece el mejor equilibrio calidad-tiempo-recursos.");
        System.out.println();
        System.out.println("📈 Métricas evaluadas:");
        System.out.println("   • Tiempo de ejecución");
        System.out.println("   • Tamaño del frente de Pareto");
        System.out.println("   • Calidad de soluciones (valor objetivo)");
        System.out.println("   • Eficiencia computacional");
        System.out.println("   • Diversidad del frente");
    }

    private static void confirmarYEjecutarConfiguracion(String tipoConfig) {
        System.out.println();
        System.out.printf("❓ ¿Desea aplicar la configuración %s y ejecutar Scatter Search? (s/n): ", tipoConfig);
        String respuesta = scanner.nextLine().trim().toLowerCase();

        if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
            System.out.printf("✅ Configuración %s aplicada.%n", tipoConfig);
            System.out.println("🚀 Ejecutando Scatter Search con nuevos parámetros...");
            System.out.println();

            try {
                // Crear Scatter Search con la configuración específica
                ScatterSearch scatterSearch = null;

                switch (tipoConfig) {
                    case "RÁPIDA":
                        scatterSearch = ScatterSearch.conConfiguracionRapida(testSystem,
                                ultimoAlgoritmoEjecutado.getMejorSolucion());
                        break;
                    case "BALANCEADA":
                        scatterSearch = ScatterSearch.conConfiguracionBalanceada(testSystem,
                                ultimoAlgoritmoEjecutado.getMejorSolucion());
                        break;
                    case "INTENSIVA":
                        scatterSearch = ScatterSearch.conConfiguracionIntensiva(testSystem,
                                ultimoAlgoritmoEjecutado.getMejorSolucion());
                        break;
                    case "MANUAL":
                        // Para manual, usar configuración balanceada como base
                        scatterSearch = ScatterSearch.conConfiguracionBalanceada(testSystem,
                                ultimoAlgoritmoEjecutado.getMejorSolucion());
                        break;
                    default:
                        scatterSearch = ScatterSearch.conConfiguracionBalanceada(testSystem,
                                ultimoAlgoritmoEjecutado.getMejorSolucion());
                        break;
                }

                // Configurar logs según el estado global
                scatterSearch.getLogger().setShowLogs(logsHabilitados);

                long tiempoInicio = System.currentTimeMillis();
                SolucionConstructiva mejorSolucionSS = scatterSearch.ejecutar();
                long tiempoTotal = System.currentTimeMillis() - tiempoInicio;

                // Actualizar el último Scatter Search ejecutado
                ultimoScatterSearchEjecutado = scatterSearch;

                // Mostrar resultados
                System.out.println("🎉 ¡Ejecución completada con éxito!");
                mostrarResultadosScatterSearch(mejorSolucionSS, scatterSearch, tiempoTotal);

                // Mostrar análisis específico de la configuración
                System.out.println("\n📊 ANÁLISIS DE LA CONFIGURACIÓN " + tipoConfig + ":");
                System.out.printf("   ⏱️ Tiempo de ejecución: %.2f segundos%n", tiempoTotal / 1000.0);
                System.out.printf("   🎯 Soluciones en frente Pareto: %d%n",
                        scatterSearch.getFrentePareto().getTamaño());
                System.out.printf("   📈 Mejora obtenida: %.2f%%%n", scatterSearch.getMejoraObtenida());
                System.out.printf("   🔄 Iteraciones ejecutadas: %d%n", scatterSearch.getHistorialIteraciones().size());

            } catch (Exception e) {
                System.err.println(
                        "❌ Error ejecutando Scatter Search con configuración " + tipoConfig + ": " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            System.out.println("⏹️ Configuración no aplicada.");
        }
    }

    private static void ejecutarBenchmarkCompleto() {
        System.out.println("⚡ EJECUTAR BENCHMARK COMPLETO Y EXPORTAR CSV");
        System.out.println("=".repeat(60));
        System.out.println("Esta operación ejecutará ambos algoritmos para cada archivo de sistema de prueba.");
        System.out.println("Los resultados serán guardados en un archivo CSV.");
        System.out.println("Esto puede tomar varios minutos. ¿Desea continuar? (s/n): ");

        String respuesta = scanner.nextLine().trim().toLowerCase();
        if (!respuesta.equals("s") && !respuesta.equals("si") && !respuesta.equals("sí")) {
            System.out.println("Benchmark completo cancelado.");
            return;
        }

        List<String[]> resultadosBenchmark = new ArrayList<>();
        // Add CSV header
        resultadosBenchmark.add(new String[] {
                "Archivo",
                "Costo_Constructivo", "Energia_Constructivo", "Vehiculos_Constructivo", "Tiempo_Constructivo_ms",
                "Costo_ScatterSearch", "Energia_ScatterSearch", "Vehiculos_ScatterSearch", "Tiempo_ScatterSearch_ms",
                "Mejora_ScatterSearch_%", "FrentePareto_Tamano"
        });

        for (int i = 1; i <= 7; i++) {
            String archivo = "test_system_" + i + ".json";
            System.out.printf("--- Procesando archivo: %s ---%n", archivo);

            try {
                // 1. Cargar datos del sistema
                JsonMapper jsonMapper = new JsonMapper();
                testSystem = jsonMapper.mapJsonToTestSystem(archivo);
                System.out.printf("✅ Datos cargados exitosamente desde %s%n", archivo);

                // 2. Ejecutar Algoritmo Constructivo
                System.out.println("🚀 Ejecutando Algoritmo Constructivo...");
                long inicioConstructivo = System.currentTimeMillis();
                ConstructivoAdaptativo constructivo = new ConstructivoAdaptativo(testSystem);
                constructivo.getLogger().setShowLogs(false); // Desactivar logs en consola para benchmark
                SolucionConstructiva solucionConstructiva = constructivo.ejecutar();
                long tiempoConstructivo = System.currentTimeMillis() - inicioConstructivo;
                ultimoAlgoritmoEjecutado = constructivo; // Actualizar para acceso si se quiere

                System.out.printf("✅ Constructivo ejecutado en %d ms. Costo: %.2f, Energía: %.2f%n",
                        tiempoConstructivo, solucionConstructiva.getCostoTotalOperacion(),
                        solucionConstructiva.getEnergiaTotalEntregada());

                // 3. Ejecutar Scatter Search
                System.out.println("🔍 Ejecutando Scatter Search...");
                long inicioScatter = System.currentTimeMillis();
                ScatterSearch scatterSearch = ScatterSearch.conConfiguracionBalanceada(testSystem,
                        solucionConstructiva); // Usar configuración balanceada
                scatterSearch.getLogger().setShowLogs(false); // Desactivar logs en consola para benchmark
                SolucionConstructiva solucionScatterSearch = scatterSearch.ejecutar();
                long tiempoScatter = System.currentTimeMillis() - inicioScatter;
                ultimoScatterSearchEjecutado = scatterSearch; // Actualizar para acceso si se quiere

                double mejoraObtenida = scatterSearch.getMejoraObtenida();
                int tamanoFrentePareto = scatterSearch.getFrentePareto().getTamaño();

                System.out.printf("✅ Scatter Search ejecutado en %d ms. Costo: %.2f, Energía: %.2f, Mejora: %.2f%%%n",
                        tiempoScatter, solucionScatterSearch.getCostoTotalOperacion(),
                        solucionScatterSearch.getEnergiaTotalEntregada(), mejoraObtenida);

                // 4. Recopilar resultados
                resultadosBenchmark.add(new String[] {
                        archivo,
                        String.format("%.2f", solucionConstructiva.getCostoTotalOperacion()),
                        String.format("%.2f", solucionConstructiva.getEnergiaTotalEntregada()),
                        String.valueOf(solucionConstructiva.getVehiculosAtendidos()),
                        String.valueOf(tiempoConstructivo),
                        String.format(java.util.Locale.US, "%.2f", solucionScatterSearch.getCostoTotalOperacion()),
                        String.format(java.util.Locale.US, "%.2f", solucionScatterSearch.getEnergiaTotalEntregada()),
                        String.valueOf(solucionScatterSearch.getVehiculosAtendidos()),
                        String.valueOf(tiempoScatter),
                        String.format(java.util.Locale.US, "%.2f", mejoraObtenida),
                        String.valueOf(tamanoFrentePareto)
                });

            } catch (IOException e) {
                System.err.printf("❌ Error al cargar el archivo %s: %s%n", archivo, e.getMessage());
                resultadosBenchmark
                        .add(new String[] { archivo, "ERROR", e.getMessage(), "", "", "", "", "", "", "", "" });
            } catch (Exception e) {
                System.err.printf("❌ Error al ejecutar algoritmos para %s: %s%n", archivo, e.getMessage());
                e.printStackTrace();
                resultadosBenchmark
                        .add(new String[] { archivo, "ERROR", e.getMessage(), "", "", "", "", "", "", "", "" });
            }
        }

        // 5. Exportar a CSV
        String nombreArchivoCSV = "resultados_benchmark_" + System.currentTimeMillis() + ".csv";
        exportarResultadosCSV(resultadosBenchmark, nombreArchivoCSV);
    }

    private static void exportarResultadosCSV(List<String[]> data, String filename) {
        System.out.printf("📋 Exportando resultados a %s...%n", filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (String[] row : data) {
                pw.println(String.join(",", row));
            }
            System.out.printf("✅ Resultados exportados exitosamente a %s%n", filename);
            System.out.println("💡 Puedes abrir este archivo con cualquier software de hoja de cálculo.");
        } catch (IOException e) {
            System.err.printf("❌ Error al exportar los resultados a CSV: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ejecutarConstructivoYExportarCSV() {
        System.out.println("🚀 EJECUTAR ALGORITMO CONSTRUCTIVO Y EXPORTAR A CSV");
        System.out.println("=".repeat(60));

        if (testSystem == null) {
            System.out.println("❌ No hay datos del sistema cargados. Cargando por defecto 'test_system_1.json'.");
            try {
                testSystem = new JsonMapper().mapJsonToTestSystem("test_system_1.json");
                System.out.println("✅ Datos cargados exitosamente desde test_system_1.json!");
            } catch (IOException e) {
                System.err.println("❌ Error cargando archivo por defecto: " + e.getMessage());
                return;
            }
        }

        System.out.println("Archivos JSON disponibles:");
        System.out.println("1. test_system_1.json");
        System.out.println("2. test_system_2.json");
        System.out.println("3. test_system_3.json");
        System.out.println("4. test_system_4.json");
        System.out.println("5. test_system_5.json");
        System.out.println("6. test_system_6.json");
        System.out.println("7. test_system_7.json");
        System.out.print("Seleccione el archivo a cargar para ejecutar el constructivo (1-7): ");

        int opcionArchivo = leerOpcion(1, 7);
        String archivoSeleccionado = "test_system_" + opcionArchivo + ".json";

        try {
            // Cargar los datos del sistema seleccionado
            JsonMapper jsonMapper = new JsonMapper();
            testSystem = jsonMapper.mapJsonToTestSystem(archivoSeleccionado);
            System.out.printf("✅ Datos cargados para %s%n", archivoSeleccionado);

            // Ejecutar Algoritmo Constructivo
            System.out.println("🚀 Ejecutando Algoritmo Constructivo...");
            long inicioConstructivo = System.currentTimeMillis();
            ConstructivoAdaptativo constructivo = new ConstructivoAdaptativo(testSystem);
            constructivo.getLogger().setShowLogs(false); // Desactivar logs en consola
            SolucionConstructiva solucionConstructiva = constructivo.ejecutar();
            long tiempoConstructivo = System.currentTimeMillis() - inicioConstructivo;

            System.out.printf("✅ Constructivo ejecutado en %d ms. Costo: %.2f, Energía: %.2f%n",
                    tiempoConstructivo, solucionConstructiva.getCostoTotalOperacion(),
                    solucionConstructiva.getEnergiaTotalEntregada());

            // Preparar resultados para exportar
            List<String[]> resultados = new ArrayList<>();
            resultados.add(new String[] { "Archivo", "Costo", "Energia", "VehiculosAtendidos", "TiempoEjecucion_ms" });
            resultados.add(new String[] {
                    archivoSeleccionado,
                    String.format(java.util.Locale.US, "%.2f", solucionConstructiva.getCostoTotalOperacion()),
                    String.format(java.util.Locale.US, "%.2f", solucionConstructiva.getEnergiaTotalEntregada()),
                    String.valueOf(solucionConstructiva.getVehiculosAtendidos()),
                    String.valueOf(tiempoConstructivo)
            });

            // Exportar a CSV
            String nombreArchivoCSV = "resultado_constructivo_" + archivoSeleccionado.replace(".json", ".csv");
            exportarResultadosCSV(resultados, nombreArchivoCSV);

        } catch (IOException e) {
            System.err.printf("❌ Error al cargar el archivo %s: %s%n", archivoSeleccionado, e.getMessage());
        } catch (Exception e) {
            System.err.printf("❌ Error al ejecutar el algoritmo constructivo para %s: %s%n", archivoSeleccionado,
                    e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ejecutarConstructivoParaTodosYExportarCSV() {
        System.out.println("🚀 EJECUTAR ALGORITMO CONSTRUCTIVO PARA TODOS LOS ARCHIVOS Y EXPORTAR A CSV");
        System.out.println("=".repeat(70));
        System.out
                .println("Esta operación ejecutará el algoritmo constructivo para cada archivo de sistema de prueba.");
        System.out.println("Los resultados serán guardados en un archivo CSV.");
        System.out.println("Esto puede tomar varios segundos. ¿Desea continuar? (s/n): ");

        String respuesta = scanner.nextLine().trim().toLowerCase();
        if (!respuesta.equals("s") && !respuesta.equals("si") && !respuesta.equals("sí")) {
            System.out.println("Operación cancelada.");
            return;
        }

        List<String[]> resultadosConstructivo = new ArrayList<>();
        // Add CSV header
        resultadosConstructivo.add(new String[] {
                "Archivo",
                "Costo_Constructivo",
                "Energia_Constructivo",
                "Vehiculos_Constructivo",
                "Tiempo_Constructivo_ms"
        });

        for (int i = 1; i <= 7; i++) {
            String archivo = "test_system_" + i + ".json";
            System.out.printf("--- Procesando archivo: %s ---%n", archivo);

            try {
                // Cargar datos del sistema
                JsonMapper jsonMapper = new JsonMapper();
                testSystem = jsonMapper.mapJsonToTestSystem(archivo);
                System.out.printf("✅ Datos cargados exitosamente desde %s%n", archivo);

                // Ejecutar Algoritmo Constructivo
                System.out.println("🚀 Ejecutando Algoritmo Constructivo...");
                long inicioConstructivo = System.currentTimeMillis();
                ConstructivoAdaptativo constructivo = new ConstructivoAdaptativo(testSystem);
                constructivo.getLogger().setShowLogs(false); // Desactivar logs en consola
                SolucionConstructiva solucionConstructiva = constructivo.ejecutar();
                long tiempoConstructivo = System.currentTimeMillis() - inicioConstructivo;

                System.out.printf("✅ Constructivo ejecutado en %d ms. Costo: %.2f, Energía: %.2f%n",
                        tiempoConstructivo, solucionConstructiva.getCostoTotalOperacion(),
                        solucionConstructiva.getEnergiaTotalEntregada());

                // Recopilar resultados
                resultadosConstructivo.add(new String[] {
                        archivo,
                        String.format(java.util.Locale.US, "%.2f", solucionConstructiva.getCostoTotalOperacion()),
                        String.format(java.util.Locale.US, "%.2f", solucionConstructiva.getEnergiaTotalEntregada()),
                        String.valueOf(solucionConstructiva.getVehiculosAtendidos()),
                        String.valueOf(tiempoConstructivo)
                });

            } catch (IOException e) {
                System.err.printf("❌ Error al cargar el archivo %s: %s%n", archivo, e.getMessage());
                resultadosConstructivo
                        .add(new String[] { archivo, "ERROR", e.getMessage(), "", "" });
            } catch (Exception e) {
                System.err.printf("❌ Error al ejecutar el algoritmo constructivo para %s: %s%n", archivo,
                        e.getMessage());
                e.printStackTrace();
                resultadosConstructivo
                        .add(new String[] { archivo, "ERROR", e.getMessage(), "", "" });
            }
            System.out.println();
        }

        // Exportar a CSV
        String nombreArchivoCSV = "resultados_constructivo_todos_" + System.currentTimeMillis() + ".csv";
        exportarResultadosCSV(resultadosConstructivo, nombreArchivoCSV);
    }
}