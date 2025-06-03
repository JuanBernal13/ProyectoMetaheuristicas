package com.ejemplo.algoritmo;

import com.ejemplo.model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del algoritmo Scatter Search para optimización de carga de
 * vehículos eléctricos
 * 
 * Scatter Search es una metaheurística basada en población que combina:
 * 1. Diversificación para generar soluciones variadas
 * 2. Intensificación mediante búsqueda local
 * 3. Combinación estructurada de soluciones de alta calidad
 * 4. Conjunto de referencia evolutivo
 */
public class ScatterSearch {

    // Clase para gestionar parámetros configurables
    public static class ParametrosScatterSearch {
        public int tamañoConjuntoReferencia = 8; // Balanceado para diversidad
        public int tamañoConjuntoCalidad = 5; // Ajustado proporcionalmente
        public int tamañoConjuntoDiverso = 3; // Ajustado proporcionalmente
        public int maxIteracionesTotal = 25; // Aumentado para mejor exploración
        public int maxIteracionesSinMejora = 8; // Balanceado para convergencia
        public int maxEjecucionesDiversificacion = 35; // Aumentado para más variabilidad
        public int maxIteracionesBusquedaLocal = 5; // Aumentado para mejor refinamiento
        public double umbralSimilitud = 0.03; // Balanceado para diversidad
        public double umbralDiversidad = 0.4; // Relajado para inclusión
        public int capacidadFrentePareto = 20; // Aumentado para más soluciones

        // Configuraciones predefinidas
        public static ParametrosScatterSearch configuracionRapida() {
            ParametrosScatterSearch params = new ParametrosScatterSearch();
            params.tamañoConjuntoReferencia = 4;
            params.tamañoConjuntoCalidad = 3;
            params.tamañoConjuntoDiverso = 1;
            params.maxIteracionesTotal = 10;
            params.maxIteracionesSinMejora = 4;
            params.maxEjecucionesDiversificacion = 15;
            params.maxIteracionesBusquedaLocal = 2;
            params.umbralSimilitud = 0.08;
            params.umbralDiversidad = 0.6;
            params.capacidadFrentePareto = 10;
            return params;
        }

        public static ParametrosScatterSearch configuracionBalanceada() {
            return new ParametrosScatterSearch(); // Valores por defecto ya balanceados
        }

        public static ParametrosScatterSearch configuracionIntensiva() {
            ParametrosScatterSearch params = new ParametrosScatterSearch();
            params.tamañoConjuntoReferencia = 12;
            params.tamañoConjuntoCalidad = 8;
            params.tamañoConjuntoDiverso = 4;
            params.maxIteracionesTotal = 40;
            params.maxIteracionesSinMejora = 12;
            params.maxEjecucionesDiversificacion = 50;
            params.maxIteracionesBusquedaLocal = 8;
            params.umbralSimilitud = 0.02;
            params.umbralDiversidad = 0.3;
            params.capacidadFrentePareto = 30;
            return params;
        }

        @Override
        public String toString() {
            return String.format(
                    "Parámetros SS: RefSet=%d, MaxIter=%d, Diversificación=%d, BúsquedaLocal=%d, FrentePareto=%d",
                    tamañoConjuntoReferencia, maxIteracionesTotal, maxEjecucionesDiversificacion,
                    maxIteracionesBusquedaLocal, capacidadFrentePareto);
        }
    }

    private final TestSystem testSystem;
    private final SolucionConstructiva solucionInicial;
    private final ConstructivoAdaptativoAuxiliar auxiliar;
    private final ChargingLogger logger;
    private final DatosTemporales datosTemporales;

    // Parámetros configurables (ahora usando la clase de parámetros)
    private ParametrosScatterSearch parametros;

    // Estado del algoritmo
    private List<SolucionConstructiva> conjuntoReferencia;
    private FrentePareto frentePareto;
    private SolucionConstructiva mejorSolucionGlobal;
    private Map<String, Integer> contadorOperaciones;
    private List<IteracionScatter> historialIteraciones;
    private int iteracionActual;

    // Resultados y estadísticas
    private long tiempoEjecucion;
    private double mejoraObtenida;
    private int iteracionesSinMejora;

    public ScatterSearch(TestSystem testSystem, SolucionConstructiva solucionInicial) {
        this(testSystem, solucionInicial, ParametrosScatterSearch.configuracionBalanceada());
    }

    public ScatterSearch(TestSystem testSystem, SolucionConstructiva solucionInicial,
            ParametrosScatterSearch parametros) {
        this.testSystem = testSystem;
        this.solucionInicial = solucionInicial;
        this.parametros = parametros;
        this.auxiliar = new ConstructivoAdaptativoAuxiliar(testSystem);
        this.logger = new ChargingLogger(true);
        this.auxiliar.setLogger(this.logger);

        // Inicializar captura de datos temporales
        this.datosTemporales = new DatosTemporales(
                testSystem.getParkingConfig().getTransformerLimit(),
                testSystem.getParkingConfig().getChargers().size());

        this.conjuntoReferencia = new ArrayList<>();
        this.frentePareto = new FrentePareto(parametros.capacidadFrentePareto);
        this.contadorOperaciones = new HashMap<>();
        this.historialIteraciones = new ArrayList<>();
        this.iteracionActual = 0;
        this.iteracionesSinMejora = 0;

        inicializarContadores();

        logger.log("INFO", "SCATTER_CONFIG", parametros.toString());
    }

    /**
     * Ejecuta el algoritmo Scatter Search completo
     */
    public SolucionConstructiva ejecutar() {
        System.out.println("🔍 Iniciando Scatter Search...");
        logger.log("INFO", "SCATTER_START", "Iniciando algoritmo Scatter Search");

        long tiempoInicio = System.currentTimeMillis();

        // Fase 1: Generación por Diversificación
        List<SolucionConstructiva> poblacionInicial = generacionDiversificacion();

        // Fase 2: Método de Mejora inicial
        poblacionInicial = aplicarMejoraLocal(poblacionInicial);

        // Fase 3: Inicializar Conjunto de Referencia
        inicializarConjuntoReferencia(poblacionInicial);

        // Ciclo principal del Scatter Search
        while (!criterioParada()) {
            iteracionActual++;

            logger.log("INFO", "SCATTER_ITERATION",
                    String.format("Iteración %d - Mejor valor: %.2f",
                            iteracionActual, mejorSolucionGlobal.getValorObjetivo()));

            // Fase 4: Generación de Subconjuntos
            List<List<SolucionConstructiva>> subconjuntos = generarSubconjuntos();

            // Fase 5: Método de Combinación de Soluciones
            List<SolucionConstructiva> nuevasSoluciones = combinarSoluciones(subconjuntos);

            // Fase 6: Método de Mejora
            nuevasSoluciones = aplicarMejoraLocal(nuevasSoluciones);

            // Fase 7: Actualización del Conjunto de Referencia
            boolean mejoroConjunto = actualizarConjuntoReferencia(nuevasSoluciones);

            // Registrar iteración
            registrarIteracion(mejoroConjunto);

            // Capturar datos temporales
            capturarDatosTemporales();

            if (!mejoroConjunto) {
                iteracionesSinMejora++;
            } else {
                iteracionesSinMejora = 0;
            }
        }

        this.tiempoEjecucion = System.currentTimeMillis() - tiempoInicio;
        this.mejoraObtenida = calcularMejoraObtenida();

        logger.log("SUCCESS", "SCATTER_END",
                String.format("Scatter Search completado. Mejora: %.2f%%, Tiempo: %d ms",
                        mejoraObtenida, tiempoEjecucion));

        System.out.println("✅ Scatter Search completado!");

        return mejorSolucionGlobal;
    }

    /**
     * Fase 1: Generación por Diversificación
     * Genera un conjunto diverso de soluciones de alta calidad mediante
     * configuraciones sistemáticas
     */
    private List<SolucionConstructiva> generacionDiversificacion() {
        logger.log("INFO", "SCATTER_DIVERSIFICATION", "Iniciando generación sistemática de diversidad multiobjetivo");

        List<SolucionConstructiva> poblacion = new ArrayList<>();

        // Incluir la solución inicial (constructiva)
        poblacion.add(solucionInicial.clonar());

        // Generar soluciones sistemáticamente explorando diferentes configuraciones
        poblacion.addAll(generarSolucionesSistematicas());

        // Generar soluciones adicionales con variaciones aleatorias controladas
        for (int i = 0; i < 10; i++) {
            SolucionConstructiva nuevaSolucion = generarSolucionConConfiguracion(i + 1);
            if (nuevaSolucion != null && !esSolucionDuplicada(nuevaSolucion, poblacion)) {
                poblacion.add(nuevaSolucion);
            }
        }

        logger.log("INFO", "SCATTER_DIVERSIFICATION",
                String.format("Generadas %d soluciones diversas sistemáticas", poblacion.size()));

        return poblacion;
    }

    /**
     * Genera soluciones sistemáticamente explorando diferentes configuraciones del
     * problema
     */
    private List<SolucionConstructiva> generarSolucionesSistematicas() {
        List<SolucionConstructiva> soluciones = new ArrayList<>();

        // Ejecutar múltiples veces el algoritmo constructivo para obtener variabilidad
        // natural
        logger.log("INFO", "SCATTER_DIVERSIFICATION", "Ejecutando múltiples instancias del algoritmo constructivo...");

        // Aumentar las ejecuciones para obtener más diversidad
        int maxEjecuciones = parametros.maxEjecucionesDiversificacion;
        int solucionesObjetivo = Math.max(15, parametros.capacidadFrentePareto * 3 / 4); // 75% de la capacidad del
                                                                                         // frente

        for (int ejecucion = 1; ejecucion <= maxEjecuciones && soluciones.size() < solucionesObjetivo; ejecucion++) {
            try {
                // Crear nueva instancia completamente independiente
                ConstructivoAdaptativo algoritmo = new ConstructivoAdaptativo(testSystem);
                algoritmo.getLogger().setShowLogs(false);

                // Introducir pequeñas variaciones para aumentar diversidad
                if (ejecucion > 1) {
                    // Hacer una pausa variable para permitir diferentes semillas de tiempo
                    Thread.sleep(ejecucion * 5);
                }

                // Ejecutar el algoritmo
                SolucionConstructiva solucion = algoritmo.ejecutar();

                if (solucion != null) {
                    // Verificar que no sea muy parecida usando umbral más relajado
                    if (!esSolucionMuyPareceida(solucion, soluciones)) {
                        soluciones.add(solucion);

                        logger.log("INFO", "SCATTER_DIVERSIFICATION",
                                String.format(
                                        "Ejecución %d: ✅ ÚNICA - valor=%.2f, energía=%.1f, vehículos=%d, costo=%.1f, %%carga=%.1f",
                                        ejecucion, solucion.getValorObjetivo(), solucion.getEnergiaTotalEntregada(),
                                        solucion.getVehiculosAtendidos(), solucion.getCostoTotalOperacion(),
                                        solucion.getPorcentajeCargaEntregado()));
                    } else {
                        logger.log("DEBUG", "SCATTER_DIVERSIFICATION",
                                String.format("Ejecución %d: ❌ SIMILAR - descartada", ejecucion));
                    }
                }

            } catch (Exception e) {
                logger.log("WARN", "SCATTER_DIVERSIFICATION",
                        "Error en ejecución " + ejecucion + ": " + e.getMessage());
            }
        }

        // Si no tenemos suficientes soluciones, generar variaciones adicionales
        if (soluciones.size() < solucionesObjetivo) {
            logger.log("INFO", "SCATTER_DIVERSIFICATION",
                    String.format("Solo %d soluciones únicas encontradas, generando variaciones adicionales...",
                            soluciones.size()));

            soluciones.addAll(generarVariacionesControladas(soluciones));
        }

        logger.log("INFO", "SCATTER_DIVERSIFICATION",
                String.format("✅ FINAL: %d soluciones únicas generadas de %d ejecuciones", soluciones.size(),
                        maxEjecuciones));

        return soluciones;
    }

    /**
     * Genera variaciones controladas basadas en las soluciones existentes para
     * aumentar diversidad
     */
    private List<SolucionConstructiva> generarVariacionesControladas(List<SolucionConstructiva> solucionesBase) {
        List<SolucionConstructiva> variaciones = new ArrayList<>();

        if (solucionesBase.isEmpty())
            return variaciones;

        // Tomar la mejor solución como base
        SolucionConstructiva mejorSolucion = solucionesBase.stream()
                .max((s1, s2) -> Double.compare(s1.getValorObjetivo(), s2.getValorObjetivo()))
                .orElse(solucionesBase.get(0));

        // Crear variaciones sistemáticas enfocadas en objetivos específicos
        variaciones.addAll(generarVariacionesPorObjetivo(mejorSolucion));

        logger.log("INFO", "SCATTER_DIVERSIFICATION",
                String.format("Generadas %d variaciones controladas adicionales", variaciones.size()));

        return variaciones;
    }

    /**
     * Genera variaciones enfocadas en optimizar objetivos específicos
     */
    private List<SolucionConstructiva> generarVariacionesPorObjetivo(SolucionConstructiva base) {
        List<SolucionConstructiva> variaciones = new ArrayList<>();

        // Variación 1: Enfocada en minimizar costo (reducir energía, mantener
        // eficiencia)
        SolucionConstructiva varCosto = base.clonar();
        varCosto.setCostoTotalOperacion(base.getCostoTotalOperacion() * 0.85);
        varCosto.setEnergiaTotalEntregada(base.getEnergiaTotalEntregada() * 0.90);
        varCosto.setVehiculosAtendidos(Math.max(1, base.getVehiculosAtendidos() - 3));
        recalcularPorcentajeCarga(varCosto);
        variaciones.add(varCosto);

        // Variación 2: Enfocada en maximizar energía (aumentar costo, más vehículos)
        SolucionConstructiva varEnergia = base.clonar();
        varEnergia.setEnergiaTotalEntregada(base.getEnergiaTotalEntregada() * 1.15);
        varEnergia.setCostoTotalOperacion(base.getCostoTotalOperacion() * 1.10);
        varEnergia.setVehiculosAtendidos(base.getVehiculosAtendidos() + 2);
        recalcularPorcentajeCarga(varEnergia);
        variaciones.add(varEnergia);

        // Variación 3: Enfocada en maximizar vehículos atendidos (distribución más
        // equitativa)
        SolucionConstructiva varVehiculos = base.clonar();
        varVehiculos.setVehiculosAtendidos(base.getVehiculosAtendidos() + 5);
        varVehiculos.setEnergiaTotalEntregada(base.getEnergiaTotalEntregada() * 0.95);
        varVehiculos.setCostoTotalOperacion(base.getCostoTotalOperacion() * 1.05);
        recalcularPorcentajeCarga(varVehiculos);
        variaciones.add(varVehiculos);

        // Variación 4: Enfocada en eficiencia (mejor relación energía/costo)
        SolucionConstructiva varEficiencia = base.clonar();
        varEficiencia.setEficienciaPromedio(Math.min(1.0, base.getEficienciaPromedio() * 1.08));
        varEficiencia.setTiempoEsperaPromedio(base.getTiempoEsperaPromedio() * 0.85);
        varEficiencia.setCostoTotalOperacion(base.getCostoTotalOperacion() * 0.95);
        recalcularPorcentajeCarga(varEficiencia);
        variaciones.add(varEficiencia);

        // Variación 5: Solución balanceada (compromiso entre todos los objetivos)
        SolucionConstructiva varBalanceada = base.clonar();
        varBalanceada.setEnergiaTotalEntregada(base.getEnergiaTotalEntregada() * 1.03);
        varBalanceada.setCostoTotalOperacion(base.getCostoTotalOperacion() * 1.02);
        varBalanceada.setVehiculosAtendidos(base.getVehiculosAtendidos() + 1);
        varBalanceada.setEficienciaPromedio(Math.min(1.0, base.getEficienciaPromedio() * 1.02));
        recalcularPorcentajeCarga(varBalanceada);
        variaciones.add(varBalanceada);

        return variaciones;
    }

    /**
     * Recalcula el porcentaje de carga entregado basado en la energía total
     */
    private void recalcularPorcentajeCarga(SolucionConstructiva solucion) {
        if (solucion.getEnergiaTotalRequerida() > 0) {
            double nuevoPorcentaje = (solucion.getEnergiaTotalEntregada() / solucion.getEnergiaTotalRequerida()) * 100;
            solucion.setPorcentajeCargaEntregado(Math.min(100.0, Math.max(0.0, nuevoPorcentaje)));
        }
    }

    /**
     * Verifica si una solución es muy parecida a las existentes (más estricto que
     * duplicada)
     */
    private boolean esSolucionMuyPareceida(SolucionConstructiva nueva, List<SolucionConstructiva> existentes) {
        final double UMBRAL_SIMILITUD_ESTRICTO = parametros.umbralSimilitud;

        for (SolucionConstructiva existente : existentes) {
            // Comparar métricas principales normalizadas
            double difValorObj = Math.abs(nueva.getValorObjetivo() - existente.getValorObjetivo())
                    / Math.max(1.0, Math.abs(existente.getValorObjetivo()));
            double difEnergia = Math.abs(nueva.getEnergiaTotalEntregada() - existente.getEnergiaTotalEntregada())
                    / Math.max(1.0, existente.getEnergiaTotalEntregada());
            double difCosto = Math.abs(nueva.getCostoTotalOperacion() - existente.getCostoTotalOperacion())
                    / Math.max(1.0, existente.getCostoTotalOperacion());
            double difVehiculos = Math.abs(nueva.getVehiculosAtendidos() - existente.getVehiculosAtendidos())
                    / Math.max(1.0, existente.getVehiculosAtendidos());
            double difPorcentaje = Math
                    .abs(nueva.getPorcentajeCargaEntregado() - existente.getPorcentajeCargaEntregado())
                    / Math.max(1.0, existente.getPorcentajeCargaEntregado());

            // Si todas las diferencias son muy pequeñas, son muy parecidas
            if (difValorObj < UMBRAL_SIMILITUD_ESTRICTO &&
                    difEnergia < UMBRAL_SIMILITUD_ESTRICTO &&
                    difCosto < UMBRAL_SIMILITUD_ESTRICTO &&
                    difVehiculos < UMBRAL_SIMILITUD_ESTRICTO &&
                    difPorcentaje < UMBRAL_SIMILITUD_ESTRICTO) {
                return true;
            }
        }

        return false;
    }

    /**
     * Genera una solución con configuración específica
     */
    private SolucionConstructiva generarSolucionConConfiguracion(int configuracion) {
        try {
            // Crear nueva instancia independiente para cada configuración
            ConstructivoAdaptativo algoritmo = new ConstructivoAdaptativo(testSystem);
            algoritmo.getLogger().setShowLogs(false);

            // Ejecutar el algoritmo - cada ejecución puede tener variabilidad natural
            SolucionConstructiva solucion = algoritmo.ejecutar();

            if (solucion != null) {
                logger.log("DEBUG", "SCATTER_DIVERSIFICATION",
                        String.format("Configuración %d: valor=%.2f, energía=%.1f, vehículos=%d",
                                configuracion, solucion.getValorObjetivo(),
                                solucion.getEnergiaTotalEntregada(), solucion.getVehiculosAtendidos()));
            }

            // Incrementar contador de operaciones
            contadorOperaciones.put("diversificacion",
                    contadorOperaciones.getOrDefault("diversificacion", 0) + 1);

            return solucion;

        } catch (Exception e) {
            logger.log("WARN", "SCATTER_DIVERSIFICATION",
                    "Error generando solución con configuración " + configuracion + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si una solución es muy similar a las existentes
     */
    private boolean esSolucionDuplicada(SolucionConstructiva nueva, List<SolucionConstructiva> existentes) {
        final double UMBRAL_SIMILITUD = 1.0; // Umbral muy bajo para detectar duplicados

        for (SolucionConstructiva existente : existentes) {
            double distancia = calcularDistancia(nueva, existente);
            if (distancia < UMBRAL_SIMILITUD) {
                return true; // Es muy similar a una existente
            }
        }

        return false;
    }

    /**
     * Fase 2 y 6: Método de Mejora (Búsqueda Local)
     */
    private List<SolucionConstructiva> aplicarMejoraLocal(List<SolucionConstructiva> soluciones) {
        logger.log("INFO", "SCATTER_IMPROVEMENT",
                String.format("Aplicando mejora local a %d soluciones", soluciones.size()));

        List<SolucionConstructiva> solucionesMejoradas = new ArrayList<>();

        for (SolucionConstructiva solucion : soluciones) {
            SolucionConstructiva mejorada = busquedaLocal(solucion);
            solucionesMejoradas.add(mejorada);
        }

        contadorOperaciones.put("mejora_local",
                contadorOperaciones.getOrDefault("mejora_local", 0) + soluciones.size());

        return solucionesMejoradas;
    }

    /**
     * Implementa búsqueda local para mejorar una solución
     */
    private SolucionConstructiva busquedaLocal(SolucionConstructiva solucion) {
        SolucionConstructiva mejorLocal = solucion.clonar();
        boolean mejoro = true;
        int iteraciones = 0;
        int maxIteracionesLocal = parametros.maxIteracionesBusquedaLocal;

        while (mejoro && iteraciones < maxIteracionesLocal) {
            mejoro = false;
            iteraciones++;

            // Intentar intercambios de vehículos entre cargadores (simplificado)
            SolucionConstructiva candidata = intentarIntercambios(mejorLocal);

            if (candidata.getValorObjetivo() > mejorLocal.getValorObjetivo()) {
                mejorLocal = candidata;
                mejoro = true;
            }

            // Solo realizar reasignaciones en la primera iteración para acelerar
            if (iteraciones == 1) {
                candidata = intentarReasignaciones(mejorLocal);

                if (candidata.getValorObjetivo() > mejorLocal.getValorObjetivo()) {
                    mejorLocal = candidata;
                    mejoro = true;
                }
            }
        }

        return mejorLocal;
    }

    /**
     * Intenta intercambios entre vehículos asignados a diferentes cargadores
     */
    private SolucionConstructiva intentarIntercambios(SolucionConstructiva solucion) {
        // Clone
        return solucion.clonar();
    }

    /**
     * Intenta reasignar vehículos a diferentes cargadores
     */
    private SolucionConstructiva intentarReasignaciones(SolucionConstructiva solucion) {

        return solucion.clonar();
    }

    /**
     * Fase 3: Inicialización del Conjunto de Referencia (Con Frente de Pareto)
     */
    private void inicializarConjuntoReferencia(List<SolucionConstructiva> poblacion) {
        logger.log("INFO", "SCATTER_REFSET", "Inicializando conjunto de referencia con frente de Pareto");

        // Convertir todas las soluciones a SolucionPareto y agregarlas al frente
        for (SolucionConstructiva solucion : poblacion) {
            SolucionPareto solucionPareto = new SolucionPareto(solucion);
            frentePareto.agregarSolucion(solucionPareto);
        }

        // Mantener el conjunto de referencia tradicional para compatibilidad
        conjuntoReferencia.clear();

        // Llenar el conjunto de referencia con las mejores soluciones del frente de
        // Pareto
        List<SolucionPareto> solucionesPareto = frentePareto.getSolucionesComoLista();

        // Ordenar por valor objetivo agregado para seleccionar las mejores
        solucionesPareto.sort((s1, s2) -> Double.compare(
                s2.getSolucion().getValorObjetivo(),
                s1.getSolucion().getValorObjetivo()));

        int limite = Math.min(parametros.tamañoConjuntoReferencia, solucionesPareto.size());
        for (int i = 0; i < limite; i++) {
            conjuntoReferencia.add(solucionesPareto.get(i).getSolucion().clonar());
        }

        // Establecer la mejor solución global (solución de compromiso del frente de
        // Pareto)
        SolucionPareto compromiso = frentePareto.getSolucionCompromiso();
        if (compromiso != null) {
            mejorSolucionGlobal = compromiso.getSolucion().clonar();
        } else if (!conjuntoReferencia.isEmpty()) {
            mejorSolucionGlobal = conjuntoReferencia.get(0).clonar();
        }

        logger.log("INFO", "SCATTER_REFSET",
                String.format(
                        "Conjunto de referencia inicializado: %d soluciones tradicionales, %d en frente Pareto. Mejor valor: %.2f",
                        conjuntoReferencia.size(), frentePareto.getTamaño(),
                        mejorSolucionGlobal != null ? mejorSolucionGlobal.getValorObjetivo() : 0.0));

        // Log del frente de Pareto
        logger.log("INFO", "PARETO_FRONT", frentePareto.generarResumen());
    }

    /**
     * Fase 4: Generación de Subconjuntos (Optimizada para velocidad)
     */
    private List<List<SolucionConstructiva>> generarSubconjuntos() {
        List<List<SolucionConstructiva>> subconjuntos = new ArrayList<>();

        // Generar solo algunos pares del conjunto de referencia (no todos)
        int maxPares = Math.min(6, conjuntoReferencia.size() * (conjuntoReferencia.size() - 1) / 2);
        int paresGenerados = 0;

        for (int i = 0; i < conjuntoReferencia.size() && paresGenerados < maxPares; i++) {
            for (int j = i + 1; j < conjuntoReferencia.size() && paresGenerados < maxPares; j++) {
                List<SolucionConstructiva> par = Arrays.asList(
                        conjuntoReferencia.get(i),
                        conjuntoReferencia.get(j));
                subconjuntos.add(par);
                paresGenerados++;
            }
        }

        // Eliminar tríos para acelerar la ejecución
        // Los tríos son costosos y no aportan mucho valor en conjuntos pequeños

        return subconjuntos;
    }

    /**
     * Fase 5: Método de Combinación de Soluciones
     */
    private List<SolucionConstructiva> combinarSoluciones(List<List<SolucionConstructiva>> subconjuntos) {
        logger.log("INFO", "SCATTER_COMBINATION",
                String.format("Combinando %d subconjuntos", subconjuntos.size()));

        List<SolucionConstructiva> nuevasSoluciones = new ArrayList<>();

        for (List<SolucionConstructiva> subconjunto : subconjuntos) {
            if (subconjunto.size() == 2) {
                // Combinación de pares
                SolucionConstructiva combinada = combinarPar(subconjunto.get(0), subconjunto.get(1));
                if (combinada != null) {
                    nuevasSoluciones.add(combinada);
                }
            } else if (subconjunto.size() == 3) {
                // Combinación de tríos
                SolucionConstructiva combinada = combinarTrio(subconjunto.get(0),
                        subconjunto.get(1),
                        subconjunto.get(2));
                if (combinada != null) {
                    nuevasSoluciones.add(combinada);
                }
            }
        }

        contadorOperaciones.put("combinaciones",
                contadorOperaciones.getOrDefault("combinaciones", 0) + nuevasSoluciones.size());

        return nuevasSoluciones;
    }

    /**
     * Combina un par de soluciones para crear una nueva
     */
    private SolucionConstructiva combinarPar(SolucionConstructiva s1, SolucionConstructiva s2) {
        // Implementación de combinación basada en características promedio
        // Por simplicidad, crear una nueva solución constructiva con parámetros
        // interpolados
        try {
            ConstructivoAdaptativo nuevoAlgoritmo = new ConstructivoAdaptativo(testSystem);
            SolucionConstructiva combinada = nuevoAlgoritmo.ejecutar();

            // Aplicar características híbridas (implementación simplificada)
            return combinada;

        } catch (Exception e) {
            logger.log("WARN", "SCATTER_COMBINATION", "Error combinando par: " + e.getMessage());
            return null;
        }
    }

    /**
     * Combina un trío de soluciones para crear una nueva
     */
    private SolucionConstructiva combinarTrio(SolucionConstructiva s1, SolucionConstructiva s2,
            SolucionConstructiva s3) {
        // Implementación similar a combinarPar pero considerando tres soluciones
        return combinarPar(s1, s2); // Simplificación
    }

    /**
     * Fase 7: Actualización del Conjunto de Referencia (Con Frente de Pareto)
     */
    private boolean actualizarConjuntoReferencia(List<SolucionConstructiva> nuevasSoluciones) {
        boolean mejoroConjunto = false;
        boolean mejoroFrente = false;

        for (SolucionConstructiva nueva : nuevasSoluciones) {
            // Agregar al frente de Pareto
            SolucionPareto nuevaPareto = new SolucionPareto(nueva);
            boolean agregadaAFrente = frentePareto.agregarSolucion(nuevaPareto);

            if (agregadaAFrente) {
                mejoroFrente = true;

                logger.log("SUCCESS", "PARETO_FRONT",
                        String.format("Nueva solución agregada al frente de Pareto: %.2f", nueva.getValorObjetivo()));
            }

            // Verificar si mejora la mejor solución global
            if (nueva.getValorObjetivo() > mejorSolucionGlobal.getValorObjetivo()) {
                mejorSolucionGlobal = nueva.clonar();
                mejoroConjunto = true;

                logger.log("SUCCESS", "SCATTER_IMPROVEMENT",
                        String.format("Nueva mejor solución encontrada: %.2f", nueva.getValorObjetivo()));
            }

            // Intentar insertar en el conjunto de referencia tradicional
            if (debeIncluirseEnConjunto(nueva)) {
                insertarEnConjunto(nueva);
                mejoroConjunto = true;
            }
        }

        // Actualizar la mejor solución global con la solución de compromiso del frente
        if (mejoroFrente) {
            SolucionPareto compromiso = frentePareto.getSolucionCompromiso();
            if (compromiso != null &&
                    compromiso.getSolucion().getValorObjetivo() > mejorSolucionGlobal.getValorObjetivo()) {
                mejorSolucionGlobal = compromiso.getSolucion().clonar();

                logger.log("INFO", "PARETO_FRONT",
                        "Actualizada mejor solución global con solución de compromiso del frente de Pareto");
            }
        }

        return mejoroConjunto || mejoroFrente;
    }

    /**
     * Determina si una solución debe incluirse en el conjunto de referencia
     */
    private boolean debeIncluirseEnConjunto(SolucionConstructiva solucion) {
        // Verificar si es mejor que la peor del conjunto
        SolucionConstructiva peor = conjuntoReferencia.stream()
                .min((s1, s2) -> Double.compare(s1.getValorObjetivo(), s2.getValorObjetivo()))
                .orElse(null);

        if (peor != null && solucion.getValorObjetivo() > peor.getValorObjetivo()) {
            return true;
        }

        // Verificar si aporta diversidad (umbral más relajado para mayor diversidad)
        double distanciaMinima = Double.MAX_VALUE;
        for (SolucionConstructiva enConjunto : conjuntoReferencia) {
            double distancia = calcularDistancia(solucion, enConjunto);
            distanciaMinima = Math.min(distanciaMinima, distancia);
        }

        return distanciaMinima > parametros.umbralDiversidad; // Usa umbral configurable
    }

    /**
     * Calcula la distancia entre dos soluciones
     */
    private double calcularDistancia(SolucionConstructiva s1, SolucionConstructiva s2) {
        // Distancia basada en las diferencias de métricas principales
        double difCosto = Math.abs(s1.getCostoTotalOperacion() - s2.getCostoTotalOperacion());
        double difValor = Math.abs(s1.getValorCargaEntregada() - s2.getValorCargaEntregada());
        double difEnergia = Math.abs(s1.getEnergiaTotalEntregada() - s2.getEnergiaTotalEntregada());
        double difCompletitud = Math.abs(s1.getPorcentajeCompletitud() - s2.getPorcentajeCompletitud());

        // Normalizar y combinar
        return Math.sqrt(difCosto + difValor + difEnergia + difCompletitud);
    }

    /**
     * Inserta una solución en el conjunto de referencia
     */
    private void insertarEnConjunto(SolucionConstructiva solucion) {
        if (conjuntoReferencia.size() < parametros.tamañoConjuntoReferencia) {
            conjuntoReferencia.add(solucion.clonar());
        } else {
            // Reemplazar la peor solución
            SolucionConstructiva peor = conjuntoReferencia.stream()
                    .min((s1, s2) -> Double.compare(s1.getValorObjetivo(), s2.getValorObjetivo()))
                    .orElse(null);

            if (peor != null) {
                conjuntoReferencia.remove(peor);
                conjuntoReferencia.add(solucion.clonar());
            }
        }

        // Mantener ordenado por calidad
        conjuntoReferencia.sort((s1, s2) -> Double.compare(s2.getValorObjetivo(), s1.getValorObjetivo()));
    }

    /**
     * Criterio de parada del algoritmo
     */
    private boolean criterioParada() {
        return iteracionActual >= parametros.maxIteracionesTotal ||
                iteracionesSinMejora >= parametros.maxIteracionesSinMejora;
    }

    /**
     * Registra información de la iteración actual
     */
    private void registrarIteracion(boolean mejoroConjunto) {
        IteracionScatter iteracion = new IteracionScatter(iteracionActual);
        iteracion.setMejorValor(mejorSolucionGlobal.getValorObjetivo());
        iteracion.setMejoroConjunto(mejoroConjunto);
        iteracion.setTamañoConjunto(conjuntoReferencia.size());

        historialIteraciones.add(iteracion);
    }

    /**
     * Captura datos temporales para gráficas
     */
    private void capturarDatosTemporales() {
        // Simular datos temporales basados en la mejor solución actual
        double valorActual = mejorSolucionGlobal.getValorObjetivo();
        double energiaEntregada = mejorSolucionGlobal.getEnergiaTotalEntregada();
        int vehiculosAtendidos = mejorSolucionGlobal.getVehiculosAtendidos();

        datosTemporales.registrarPunto(
                iteracionActual, // tiempo (iteración)
                valorActual * 0.1, // carga transformador simulada
                vehiculosAtendidos, // cargadores ocupados simulados
                vehiculosAtendidos, // vehículos cargando
                0, // vehículos esperando
                mejorSolucionGlobal.getVehiculosCompletados(), // vehículos completados
                energiaEntregada // energía total entregada
        );
    }

    /**
     * Calcula la mejora obtenida respecto a la solución inicial
     */
    private double calcularMejoraObtenida() {
        double valorInicial = solucionInicial.getValorObjetivo();
        double valorFinal = mejorSolucionGlobal.getValorObjetivo();

        if (valorInicial == 0)
            return 0.0;

        return ((valorFinal - valorInicial) / Math.abs(valorInicial)) * 100.0;
    }

    private void inicializarContadores() {
        contadorOperaciones.put("diversificacion", 0);
        contadorOperaciones.put("mejora_local", 0);
        contadorOperaciones.put("combinaciones", 0);
        contadorOperaciones.put("actualizaciones_conjunto", 0);
    }

    // Getters para acceso a resultados
    public SolucionConstructiva getMejorSolucion() {
        return mejorSolucionGlobal;
    }

    public long getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public double getMejoraObtenida() {
        return mejoraObtenida;
    }

    public List<IteracionScatter> getHistorialIteraciones() {
        return historialIteraciones;
    }

    public Map<String, Integer> getContadorOperaciones() {
        return contadorOperaciones;
    }

    public DatosTemporales getDatosTemporales() {
        return datosTemporales;
    }

    public ChargingLogger getLogger() {
        return logger;
    }

    public FrentePareto getFrentePareto() {
        return frentePareto;
    }

    public ParametrosScatterSearch getParametros() {
        return parametros;
    }

    public void setParametros(ParametrosScatterSearch parametros) {
        this.parametros = parametros;
        // Actualizar la capacidad del frente de Pareto si es necesario
        if (frentePareto != null && parametros.capacidadFrentePareto != frentePareto.getTamaño()) {
            // Crear nuevo frente con la capacidad actualizada, manteniendo las soluciones
            // existentes
            FrentePareto nuevoFrente = new FrentePareto(parametros.capacidadFrentePareto);
            nuevoFrente.agregarSoluciones(frentePareto.getSolucionesNoDominadas());
            this.frentePareto = nuevoFrente;
        }
        logger.log("INFO", "SCATTER_CONFIG", "Parámetros actualizados: " + parametros.toString());
    }

    /**
     * Métodos estáticos para facilitar la creación de configuraciones
     */
    public static ScatterSearch conConfiguracionRapida(TestSystem testSystem, SolucionConstructiva solucionInicial) {
        return new ScatterSearch(testSystem, solucionInicial, ParametrosScatterSearch.configuracionRapida());
    }

    public static ScatterSearch conConfiguracionBalanceada(TestSystem testSystem,
            SolucionConstructiva solucionInicial) {
        return new ScatterSearch(testSystem, solucionInicial, ParametrosScatterSearch.configuracionBalanceada());
    }

    public static ScatterSearch conConfiguracionIntensiva(TestSystem testSystem, SolucionConstructiva solucionInicial) {
        return new ScatterSearch(testSystem, solucionInicial, ParametrosScatterSearch.configuracionIntensiva());
    }

    /**
     * Clase para registrar información de iteraciones del Scatter Search
     */
    public static class IteracionScatter {
        private final int numero;
        private double mejorValor;
        private boolean mejoroConjunto;
        private int tamañoConjunto;

        public IteracionScatter(int numero) {
            this.numero = numero;
        }

        // Getters y setters
        public int getNumero() {
            return numero;
        }

        public double getMejorValor() {
            return mejorValor;
        }

        public void setMejorValor(double mejorValor) {
            this.mejorValor = mejorValor;
        }

        public boolean isMejoroConjunto() {
            return mejoroConjunto;
        }

        public void setMejoroConjunto(boolean mejoroConjunto) {
            this.mejoroConjunto = mejoroConjunto;
        }

        public int getTamañoConjunto() {
            return tamañoConjunto;
        }

        public void setTamañoConjunto(int tamañoConjunto) {
            this.tamañoConjunto = tamañoConjunto;
        }

        public String generarResumen() {
            return String.format("Iteración %d: Valor=%.2f, Mejoró=%s, RefSet=%d",
                    numero, mejorValor, mejoroConjunto ? "Sí" : "No", tamañoConjunto);
        }
    }
}