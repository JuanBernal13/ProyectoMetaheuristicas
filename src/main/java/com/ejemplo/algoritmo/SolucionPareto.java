package com.ejemplo.algoritmo;

import java.util.Arrays;

/**
 * Representa una solución en el espacio multiobjetivo para el problema de carga
 * de vehículos eléctricos.
 * Maneja múltiples objetivos en conflicto para análisis de Pareto.
 */
public class SolucionPareto {

    private final SolucionConstructiva solucion;
    private final double[] objetivos;
    private final String[] nombresObjetivos;

    // Índices de objetivos
    public static final int MINIMIZAR_COSTO = 0;
    public static final int MAXIMIZAR_ENERGIA = 1;
    public static final int MAXIMIZAR_VEHICULOS = 2;
    public static final int MINIMIZAR_TIEMPO_ESPERA = 3;
    public static final int MAXIMIZAR_EFICIENCIA = 4;
    public static final int MAXIMIZAR_PORCENTAJE_CARGA = 5;

    public SolucionPareto(SolucionConstructiva solucion) {
        this.solucion = solucion;
        this.objetivos = new double[6];
        this.nombresObjetivos = new String[] {
                "Costo Total (EUR)",
                "Energía Entregada (kWh)",
                "Vehículos Atendidos",
                "Tiempo Espera Promedio (h)",
                "Eficiencia Promedio (%)",
                "Porcentaje Carga Entregado (%)"
        };

        calcularObjetivos();
    }

    /**
     * Calcula todos los valores de objetivos a partir de la solución constructiva
     */
    private void calcularObjetivos() {
        // Objetivo 1: Minimizar costo total (se invierte para tratarlo como
        // maximización)
        objetivos[MINIMIZAR_COSTO] = -solucion.getCostoTotalOperacion();

        // Objetivo 2: Maximizar energía entregada
        objetivos[MAXIMIZAR_ENERGIA] = solucion.getEnergiaTotalEntregada();

        // Objetivo 3: Maximizar número de vehículos atendidos
        objetivos[MAXIMIZAR_VEHICULOS] = solucion.getVehiculosAtendidos();

        // Objetivo 4: Minimizar tiempo de espera promedio (se invierte)
        objetivos[MINIMIZAR_TIEMPO_ESPERA] = -solucion.getTiempoEsperaPromedio();

        // Objetivo 5: Maximizar eficiencia promedio
        objetivos[MAXIMIZAR_EFICIENCIA] = solucion.getEficienciaPromedio() * 100;

        // Objetivo 6: Maximizar porcentaje de carga entregado
        objetivos[MAXIMIZAR_PORCENTAJE_CARGA] = solucion.getPorcentajeCargaEntregado();
    }

    /**
     * Determina si esta solución domina a otra en el sentido de Pareto
     * Una solución A domina a B si A es al menos igual en todos los objetivos
     * y estrictamente mejor en al menos uno
     */
    public boolean domina(SolucionPareto otra) {
        boolean alMenosUnMejor = false;

        for (int i = 0; i < objetivos.length; i++) {
            if (this.objetivos[i] < otra.objetivos[i]) {
                return false; // Esta solución es peor en al menos un objetivo
            }
            if (this.objetivos[i] > otra.objetivos[i]) {
                alMenosUnMejor = true; // Esta solución es mejor en al menos un objetivo
            }
        }

        return alMenosUnMejor;
    }

    /**
     * Verifica si dos soluciones son equivalentes en todos los objetivos
     */
    public boolean esEquivalente(SolucionPareto otra) {
        final double EPSILON = 1e-6;

        for (int i = 0; i < objetivos.length; i++) {
            if (Math.abs(this.objetivos[i] - otra.objetivos[i]) > EPSILON) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calcula la distancia euclidiana en el espacio de objetivos (normalizada)
     */
    public double calcularDistancia(SolucionPareto otra) {
        double suma = 0.0;

        for (int i = 0; i < objetivos.length; i++) {
            double diff = this.objetivos[i] - otra.objetivos[i];
            suma += diff * diff;
        }

        return Math.sqrt(suma);
    }

    /**
     * Genera un resumen de la solución en el espacio multiobjetivo
     */
    public String generarResumenPareto() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SOLUCIÓN MULTIOBJETIVO ===\n");

        for (int i = 0; i < objetivos.length; i++) {
            double valorReal = objetivos[i];

            // Convertir valores invertidos de vuelta a su forma original para mostrar
            if (i == MINIMIZAR_COSTO) {
                valorReal = -valorReal;
                sb.append(String.format("%s: %.2f EUR\n", nombresObjetivos[i], valorReal));
            } else if (i == MINIMIZAR_TIEMPO_ESPERA) {
                valorReal = -valorReal;
                sb.append(String.format("%s: %.3f h\n", nombresObjetivos[i], valorReal));
            } else if (i == MAXIMIZAR_VEHICULOS) {
                sb.append(String.format("%s: %.0f\n", nombresObjetivos[i], valorReal));
            } else {
                sb.append(String.format("%s: %.2f\n", nombresObjetivos[i], valorReal));
            }
        }

        sb.append(String.format("Valor Objetivo Agregado: %.2f\n", solucion.getValorObjetivo()));

        return sb.toString();
    }

    /**
     * Genera una línea compacta para comparación
     */
    public String generarLineaComparacion() {
        return String.format("Costo:%.1f | Energía:%.1f | Vehículos:%.0f | Espera:%.2f | Efic:%.1f | Carga:%.1f%%",
                -objetivos[MINIMIZAR_COSTO],
                objetivos[MAXIMIZAR_ENERGIA],
                objetivos[MAXIMIZAR_VEHICULOS],
                -objetivos[MINIMIZAR_TIEMPO_ESPERA],
                objetivos[MAXIMIZAR_EFICIENCIA],
                objetivos[MAXIMIZAR_PORCENTAJE_CARGA]);
    }

    // Getters
    public SolucionConstructiva getSolucion() {
        return solucion;
    }

    public double[] getObjetivos() {
        return objetivos.clone();
    }

    public String[] getNombresObjetivos() {
        return nombresObjetivos.clone();
    }

    public double getObjetivo(int indice) {
        if (indice >= 0 && indice < objetivos.length) {
            return objetivos[indice];
        }
        throw new IndexOutOfBoundsException("Índice de objetivo inválido: " + indice);
    }

    /**
     * Obtiene el valor real (no invertido) de un objetivo
     */
    public double getValorRealObjetivo(int indice) {
        double valor = getObjetivo(indice);

        // Invertir de vuelta los objetivos de minimización
        if (indice == MINIMIZAR_COSTO || indice == MINIMIZAR_TIEMPO_ESPERA) {
            return -valor;
        }

        return valor;
    }

    @Override
    public String toString() {
        return generarLineaComparacion();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        SolucionPareto otra = (SolucionPareto) obj;
        return Arrays.equals(this.objetivos, otra.objetivos);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(objetivos);
    }
}