package com.ejemplo.algoritmo;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

/**
 * Generador de gráficas para visualizar la evolución temporal del sistema
 * Usa Java Swing nativo sin dependencias externas
 */
public class GeneradorGraficas {

    /**
     * Muestra las gráficas de evolución temporal
     */
    public static void mostrarGraficas(DatosTemporales datos) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("📊 Evolución Temporal del Sistema de Carga");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);

            // Crear panel principal con pestañas
            JTabbedPane tabbedPane = new JTabbedPane();

            // Pestaña 1: Carga del Transformador
            tabbedPane.addTab("⚡ Transformador", crearGraficaTransformador(datos));

            // Pestaña 2: Ocupación de Cargadores
            tabbedPane.addTab("🔌 Ocupación", crearGraficaOcupacion(datos));

            // Pestaña 3: Estado de Vehículos
            tabbedPane.addTab("🚗 Vehículos", crearGraficaVehiculos(datos));

            // Pestaña 4: Energía Acumulada
            tabbedPane.addTab("🔋 Energía", crearGraficaEnergia(datos));

            frame.add(tabbedPane);
            frame.setVisible(true);

            // Mostrar resumen en consola
            System.out.println("\n" + datos.generarResumen());
            System.out.println("📊 Gráficas mostradas en ventana separada.");
        });
    }

    /**
     * Crea gráfica de carga del transformador vs tiempo
     */
    private static JPanel crearGraficaTransformador(DatosTemporales datos) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                dibujarGraficaTransformador(g2d, datos, getWidth(), getHeight());

                g2d.dispose();
            }
        };
    }

    /**
     * Crea gráfica de ocupación de cargadores vs tiempo
     */
    private static JPanel crearGraficaOcupacion(DatosTemporales datos) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                dibujarGraficaOcupacion(g2d, datos, getWidth(), getHeight());

                g2d.dispose();
            }
        };
    }

    /**
     * Crea gráfica de estado de vehículos vs tiempo
     */
    private static JPanel crearGraficaVehiculos(DatosTemporales datos) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                dibujarGraficaVehiculos(g2d, datos, getWidth(), getHeight());

                g2d.dispose();
            }
        };
    }

    /**
     * Crea gráfica de energía acumulada vs tiempo
     */
    private static JPanel crearGraficaEnergia(DatosTemporales datos) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                dibujarGraficaEnergia(g2d, datos, getWidth(), getHeight());

                g2d.dispose();
            }
        };
    }

    /**
     * Dibuja la gráfica de carga del transformador
     */
    private static void dibujarGraficaTransformador(Graphics2D g2d, DatosTemporales datos, int width, int height) {
        List<DatosTemporales.PuntoTemporal> puntos = datos.getPuntosTemporales();
        if (puntos.isEmpty())
            return;

        // Configurar área de dibujo
        int margen = 60;
        int areaWidth = width - 2 * margen;
        int areaHeight = height - 2 * margen;

        // Obtener rangos de datos
        double tiempoMax = puntos.get(puntos.size() - 1).getTiempo();
        double cargaMax = Math.max(datos.getLimiteTransformador() * 1.1,
                puntos.stream().mapToDouble(DatosTemporales.PuntoTemporal::getCargaTransformador).max().orElse(0));

        // Dibujar ejes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(margen, height - margen, width - margen, height - margen); // Eje X
        g2d.drawLine(margen, margen, margen, height - margen); // Eje Y

        // Dibujar línea límite del transformador
        double yLimite = height - margen - (datos.getLimiteTransformador() / cargaMax) * areaHeight;
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[] { 5, 5 }, 0));
        g2d.drawLine(margen, (int) yLimite, width - margen, (int) yLimite);

        // Etiqueta del límite
        g2d.setColor(Color.RED);
        g2d.drawString("Límite: " + datos.getLimiteTransformador() + " kW",
                width - margen - 100, (int) yLimite - 5);

        // Dibujar línea de datos
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(3));

        for (int i = 1; i < puntos.size(); i++) {
            DatosTemporales.PuntoTemporal p1 = puntos.get(i - 1);
            DatosTemporales.PuntoTemporal p2 = puntos.get(i);

            double x1 = margen + (p1.getTiempo() / tiempoMax) * areaWidth;
            double y1 = height - margen - (p1.getCargaTransformador() / cargaMax) * areaHeight;
            double x2 = margen + (p2.getTiempo() / tiempoMax) * areaWidth;
            double y2 = height - margen - (p2.getCargaTransformador() / cargaMax) * areaHeight;

            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        }

        // Títulos y etiquetas
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Carga del Transformador vs Tiempo", width / 2 - 120, 30);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Tiempo (h)", width / 2 - 30, height - 10);

        // Rotar texto para eje Y
        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.rotate(-Math.PI / 2, 20, height / 2);
        g2dRotated.drawString("Carga (kW)", 20, height / 2);
        g2dRotated.dispose();

        // Agregar marcas en los ejes
        dibujarMarcasEjes(g2d, margen, width, height, areaWidth, areaHeight, tiempoMax, cargaMax);
    }

    /**
     * Dibuja la gráfica de ocupación de cargadores
     */
    private static void dibujarGraficaOcupacion(Graphics2D g2d, DatosTemporales datos, int width, int height) {
        List<DatosTemporales.PuntoTemporal> puntos = datos.getPuntosTemporales();
        if (puntos.isEmpty())
            return;

        int margen = 60;
        int areaWidth = width - 2 * margen;
        int areaHeight = height - 2 * margen;

        double tiempoMax = puntos.get(puntos.size() - 1).getTiempo();
        double ocupacionMax = 100.0;

        // Dibujar ejes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(margen, height - margen, width - margen, height - margen);
        g2d.drawLine(margen, margen, margen, height - margen);

        // Dibujar línea de datos
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(3));

        for (int i = 1; i < puntos.size(); i++) {
            DatosTemporales.PuntoTemporal p1 = puntos.get(i - 1);
            DatosTemporales.PuntoTemporal p2 = puntos.get(i);

            double x1 = margen + (p1.getTiempo() / tiempoMax) * areaWidth;
            double y1 = height - margen - (p1.getPorcentajeOcupacion() / ocupacionMax) * areaHeight;
            double x2 = margen + (p2.getTiempo() / tiempoMax) * areaWidth;
            double y2 = height - margen - (p2.getPorcentajeOcupacion() / ocupacionMax) * areaHeight;

            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        }

        // Títulos
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Ocupación de Cargadores vs Tiempo", width / 2 - 130, 30);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Tiempo (h)", width / 2 - 30, height - 10);

        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.rotate(-Math.PI / 2, 20, height / 2);
        g2dRotated.drawString("Ocupación (%)", 20, height / 2);
        g2dRotated.dispose();

        dibujarMarcasEjes(g2d, margen, width, height, areaWidth, areaHeight, tiempoMax, ocupacionMax);
    }

    /**
     * Dibuja la gráfica de vehículos por estado
     */
    private static void dibujarGraficaVehiculos(Graphics2D g2d, DatosTemporales datos, int width, int height) {
        List<DatosTemporales.PuntoTemporal> puntos = datos.getPuntosTemporales();
        if (puntos.isEmpty())
            return;

        int margen = 60;
        int areaWidth = width - 2 * margen;
        int areaHeight = height - 2 * margen;

        double tiempoMax = puntos.get(puntos.size() - 1).getTiempo();
        int vehiculosMax = puntos.stream()
                .mapToInt(p -> p.getVehiculosCargando() + p.getVehiculosEsperando() + p.getVehiculosCompletados())
                .max().orElse(0);

        // Dibujar ejes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(margen, height - margen, width - margen, height - margen);
        g2d.drawLine(margen, margen, margen, height - margen);

        // Dibujar líneas para cada estado
        String[] estados = { "Cargando", "Esperando", "Completados" };
        Color[] colores = { Color.BLUE, Color.ORANGE, Color.GREEN };

        for (int estado = 0; estado < 3; estado++) {
            g2d.setColor(colores[estado]);
            g2d.setStroke(new BasicStroke(2));

            for (int i = 1; i < puntos.size(); i++) {
                DatosTemporales.PuntoTemporal p1 = puntos.get(i - 1);
                DatosTemporales.PuntoTemporal p2 = puntos.get(i);

                int valor1 = obtenerValorEstado(p1, estado);
                int valor2 = obtenerValorEstado(p2, estado);

                double x1 = margen + (p1.getTiempo() / tiempoMax) * areaWidth;
                double y1 = height - margen - (valor1 / (double) vehiculosMax) * areaHeight;
                double x2 = margen + (p2.getTiempo() / tiempoMax) * areaWidth;
                double y2 = height - margen - (valor2 / (double) vehiculosMax) * areaHeight;

                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }

        // Leyenda
        int yLeyenda = 50;
        for (int i = 0; i < 3; i++) {
            g2d.setColor(colores[i]);
            g2d.fillRect(width - 150, yLeyenda + i * 20, 15, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString(estados[i], width - 130, yLeyenda + i * 20 + 10);
        }

        // Títulos
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Estado de Vehículos vs Tiempo", width / 2 - 120, 30);
    }

    /**
     * Dibuja la gráfica de energía acumulada
     */
    private static void dibujarGraficaEnergia(Graphics2D g2d, DatosTemporales datos, int width, int height) {
        List<DatosTemporales.PuntoTemporal> puntos = datos.getPuntosTemporales();
        if (puntos.isEmpty())
            return;

        int margen = 60;
        int areaWidth = width - 2 * margen;
        int areaHeight = height - 2 * margen;

        double tiempoMax = puntos.get(puntos.size() - 1).getTiempo();
        double energiaMax = puntos.stream()
                .mapToDouble(DatosTemporales.PuntoTemporal::getEnergiaTotalEntregada)
                .max().orElse(0);

        // Dibujar ejes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(margen, height - margen, width - margen, height - margen);
        g2d.drawLine(margen, margen, margen, height - margen);

        // Dibujar curva de energía
        g2d.setColor(Color.MAGENTA);
        g2d.setStroke(new BasicStroke(3));

        for (int i = 1; i < puntos.size(); i++) {
            DatosTemporales.PuntoTemporal p1 = puntos.get(i - 1);
            DatosTemporales.PuntoTemporal p2 = puntos.get(i);

            double x1 = margen + (p1.getTiempo() / tiempoMax) * areaWidth;
            double y1 = height - margen - (p1.getEnergiaTotalEntregada() / energiaMax) * areaHeight;
            double x2 = margen + (p2.getTiempo() / tiempoMax) * areaWidth;
            double y2 = height - margen - (p2.getEnergiaTotalEntregada() / energiaMax) * areaHeight;

            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        }

        // Títulos
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Energía Acumulada vs Tiempo", width / 2 - 120, 30);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Tiempo (h)", width / 2 - 30, height - 10);

        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.rotate(-Math.PI / 2, 20, height / 2);
        g2dRotated.drawString("Energía (kWh)", 20, height / 2);
        g2dRotated.dispose();

        dibujarMarcasEjes(g2d, margen, width, height, areaWidth, areaHeight, tiempoMax, energiaMax);
    }

    private static int obtenerValorEstado(DatosTemporales.PuntoTemporal punto, int estado) {
        switch (estado) {
            case 0:
                return punto.getVehiculosCargando();
            case 1:
                return punto.getVehiculosEsperando();
            case 2:
                return punto.getVehiculosCompletados();
            default:
                return 0;
        }
    }

    private static void dibujarMarcasEjes(Graphics2D g2d, int margen, int width, int height,
            int areaWidth, int areaHeight, double maxX, double maxY) {
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        // Marcas eje X (tiempo)
        for (int i = 0; i <= 10; i++) {
            double valor = (maxX / 10) * i;
            int x = margen + (areaWidth / 10) * i;
            g2d.drawLine(x, height - margen, x, height - margen + 5);
            g2d.drawString(String.format("%.1f", valor), x - 10, height - margen + 20);
        }

        // Marcas eje Y
        for (int i = 0; i <= 10; i++) {
            double valor = (maxY / 10) * i;
            int y = height - margen - (areaHeight / 10) * i;
            g2d.drawLine(margen - 5, y, margen, y);
            g2d.drawString(String.format("%.0f", valor), margen - 35, y + 5);
        }
    }
}