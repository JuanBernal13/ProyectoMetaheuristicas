package com.ejemplo.algoritmo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementa el frente de Pareto para mantener un conjunto de soluciones no
 * dominadas
 * en el problema multiobjetivo de carga de vehículos eléctricos.
 */
public class FrentePareto {

    private final Set<SolucionPareto> solucionesNoDominadas;
    private final int capacidadMaxima;

    public FrentePareto() {
        this(50); // Capacidad por defecto
    }

    public FrentePareto(int capacidadMaxima) {
        this.solucionesNoDominadas = new LinkedHashSet<>();
        this.capacidadMaxima = capacidadMaxima;
    }

    /**
     * Intenta agregar una nueva solución al frente de Pareto
     * 
     * @param nuevaSolucion La solución a evaluar
     * @return true si la solución fue agregada al frente, false si fue dominada
     */
    public boolean agregarSolucion(SolucionPareto nuevaSolucion) {
        if (nuevaSolucion == null)
            return false;

        // Verificar si la nueva solución es dominada por alguna existente
        for (SolucionPareto existente : solucionesNoDominadas) {
            if (existente.domina(nuevaSolucion)) {
                return false; // La nueva solución es dominada, no se agrega
            }
        }

        // Remover soluciones dominadas por la nueva
        solucionesNoDominadas.removeIf(existente -> nuevaSolucion.domina(existente));

        // Agregar la nueva solución al frente
        solucionesNoDominadas.add(nuevaSolucion);

        // Si excede la capacidad, aplicar reducción por diversidad
        if (solucionesNoDominadas.size() > capacidadMaxima) {
            reducirPorDiversidad();
        }

        return true;
    }

    /**
     * Agrega múltiples soluciones al frente de Pareto
     */
    public void agregarSoluciones(Collection<SolucionPareto> soluciones) {
        for (SolucionPareto solucion : soluciones) {
            agregarSolucion(solucion);
        }
    }

    /**
     * Reduce el tamaño del frente manteniendo diversidad
     */
    private void reducirPorDiversidad() {
        if (solucionesNoDominadas.size() <= capacidadMaxima) {
            return;
        }

        List<SolucionPareto> lista = new ArrayList<>(solucionesNoDominadas);
        List<SolucionPareto> mantener = new ArrayList<>();

        // Mantener siempre las soluciones extremas (mejores en cada objetivo)
        for (int obj = 0; obj < 6; obj++) {
            final int objetivoActual = obj;
            SolucionPareto mejor = lista.stream()
                    .max((s1, s2) -> Double.compare(s1.getObjetivo(objetivoActual), s2.getObjetivo(objetivoActual)))
                    .orElse(null);

            if (mejor != null && !mantener.contains(mejor)) {
                mantener.add(mejor);
            }
        }

        // Completar con soluciones más diversas
        while (mantener.size() < capacidadMaxima && mantener.size() < lista.size()) {
            SolucionPareto masDiversa = encontrarMasDiversa(lista, mantener);
            if (masDiversa != null && !mantener.contains(masDiversa)) {
                mantener.add(masDiversa);
            } else {
                break;
            }
        }

        solucionesNoDominadas.clear();
        solucionesNoDominadas.addAll(mantener);
    }

    /**
     * Encuentra la solución más diversa respecto a un conjunto dado
     */
    private SolucionPareto encontrarMasDiversa(List<SolucionPareto> candidatas, List<SolucionPareto> referencia) {
        SolucionPareto masDiversa = null;
        double maxDistanciaMinima = -1;

        for (SolucionPareto candidata : candidatas) {
            if (referencia.contains(candidata))
                continue;

            double distanciaMinima = Double.MAX_VALUE;
            for (SolucionPareto ref : referencia) {
                double distancia = candidata.calcularDistancia(ref);
                distanciaMinima = Math.min(distanciaMinima, distancia);
            }

            if (distanciaMinima > maxDistanciaMinima) {
                maxDistanciaMinima = distanciaMinima;
                masDiversa = candidata;
            }
        }

        return masDiversa;
    }

    /**
     * Obtiene la mejor solución según un objetivo específico
     */
    public SolucionPareto getMejorEnObjetivo(int indiceObjetivo) {
        return solucionesNoDominadas.stream()
                .max((s1, s2) -> Double.compare(s1.getObjetivo(indiceObjetivo), s2.getObjetivo(indiceObjetivo)))
                .orElse(null);
    }

    /**
     * Obtiene una solución representativa (compromise solution)
     * usando la distancia mínima al punto ideal
     */
    public SolucionPareto getSolucionCompromiso() {
        if (solucionesNoDominadas.isEmpty())
            return null;

        // Calcular punto ideal (máximo en cada objetivo)
        double[] puntoIdeal = new double[6];
        for (int i = 0; i < 6; i++) {
            final int obj = i;
            puntoIdeal[i] = solucionesNoDominadas.stream()
                    .mapToDouble(s -> s.getObjetivo(obj))
                    .max()
                    .orElse(0.0);
        }

        // Encontrar la solución más cercana al punto ideal
        return solucionesNoDominadas.stream()
                .min((s1, s2) -> {
                    double dist1 = calcularDistanciaAPunto(s1, puntoIdeal);
                    double dist2 = calcularDistanciaAPunto(s2, puntoIdeal);
                    return Double.compare(dist1, dist2);
                })
                .orElse(null);
    }

    /**
     * Calcula la distancia euclidiana de una solución a un punto específico
     */
    private double calcularDistanciaAPunto(SolucionPareto solucion, double[] punto) {
        double suma = 0.0;
        double[] objetivos = solucion.getObjetivos();

        for (int i = 0; i < objetivos.length; i++) {
            double diff = objetivos[i] - punto[i];
            suma += diff * diff;
        }

        return Math.sqrt(suma);
    }

    /**
     * Calcula métricas de calidad del frente de Pareto
     */
    public EstadisticasFrente calcularEstadisticas() {
        if (solucionesNoDominadas.isEmpty()) {
            return new EstadisticasFrente();
        }

        EstadisticasFrente stats = new EstadisticasFrente();
        stats.tamaño = solucionesNoDominadas.size();

        // Calcular diversidad (distancia promedio entre soluciones)
        double sumaDistancias = 0.0;
        int contador = 0;

        List<SolucionPareto> lista = new ArrayList<>(solucionesNoDominadas);
        for (int i = 0; i < lista.size(); i++) {
            for (int j = i + 1; j < lista.size(); j++) {
                sumaDistancias += lista.get(i).calcularDistancia(lista.get(j));
                contador++;
            }
        }

        stats.diversidadPromedio = contador > 0 ? sumaDistancias / contador : 0.0;

        // Calcular rangos en cada objetivo
        stats.rangosObjetivos = new double[6];
        for (int obj = 0; obj < 6; obj++) {
            final int objetivoActual = obj;
            OptionalDouble min = solucionesNoDominadas.stream()
                    .mapToDouble(s -> s.getObjetivo(objetivoActual))
                    .min();
            OptionalDouble max = solucionesNoDominadas.stream()
                    .mapToDouble(s -> s.getObjetivo(objetivoActual))
                    .max();

            if (min.isPresent() && max.isPresent()) {
                stats.rangosObjetivos[obj] = max.getAsDouble() - min.getAsDouble();
            }
        }

        return stats;
    }

    /**
     * Genera un resumen del frente de Pareto
     */
    public String generarResumen() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FRENTE DE PARETO ===\n");
        sb.append(String.format("Número de soluciones no dominadas: %d\n", solucionesNoDominadas.size()));

        if (solucionesNoDominadas.isEmpty()) {
            sb.append("No hay soluciones en el frente.\n");
            return sb.toString();
        }

        EstadisticasFrente stats = calcularEstadisticas();
        sb.append(String.format("Diversidad promedio: %.3f\n", stats.diversidadPromedio));

        sb.append("\nMejores soluciones por objetivo:\n");
        String[] nombresObj = { "Menor Costo", "Mayor Energía", "Más Vehículos",
                "Menor Espera", "Mayor Eficiencia", "Mayor % Carga" };

        for (int i = 0; i < 6; i++) {
            SolucionPareto mejor = getMejorEnObjetivo(i);
            if (mejor != null) {
                sb.append(String.format("• %s: %.2f\n", nombresObj[i], mejor.getValorRealObjetivo(i)));
            }
        }

        SolucionPareto compromiso = getSolucionCompromiso();
        if (compromiso != null) {
            sb.append("\nSolución de compromiso:\n");
            sb.append(compromiso.generarLineaComparacion()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Lista todas las soluciones del frente
     */
    public String listarSoluciones() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TODAS LAS SOLUCIONES DEL FRENTE DE PARETO ===\n");

        if (solucionesNoDominadas.isEmpty()) {
            sb.append("No hay soluciones en el frente.\n");
            return sb.toString();
        }

        List<SolucionPareto> lista = new ArrayList<>(solucionesNoDominadas);

        // Ordenar por valor objetivo agregado para mostrar
        lista.sort((s1, s2) -> Double.compare(
                s2.getSolucion().getValorObjetivo(),
                s1.getSolucion().getValorObjetivo()));

        for (int i = 0; i < lista.size(); i++) {
            sb.append(String.format("%2d. %s\n", i + 1, lista.get(i).generarLineaComparacion()));
        }

        return sb.toString();
    }

    // Getters
    public Set<SolucionPareto> getSolucionesNoDominadas() {
        return new LinkedHashSet<>(solucionesNoDominadas);
    }

    public List<SolucionPareto> getSolucionesComoLista() {
        return new ArrayList<>(solucionesNoDominadas);
    }

    public int getTamaño() {
        return solucionesNoDominadas.size();
    }

    public boolean estaVacio() {
        return solucionesNoDominadas.isEmpty();
    }

    public void limpiar() {
        solucionesNoDominadas.clear();
    }

    /**
     * Clase para almacenar estadísticas del frente de Pareto
     */
    public static class EstadisticasFrente {
        public int tamaño = 0;
        public double diversidadPromedio = 0.0;
        public double[] rangosObjetivos = new double[6];

        @Override
        public String toString() {
            return String.format("Tamaño: %d, Diversidad: %.3f", tamaño, diversidadPromedio);
        }
    }
}