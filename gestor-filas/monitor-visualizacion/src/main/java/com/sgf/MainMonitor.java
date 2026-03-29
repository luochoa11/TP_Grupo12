package com.sgf;

import javax.swing.SwingUtilities;

public class MainMonitor {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaMonitorVisualizacion ventana = new VentanaMonitorVisualizacion();
            ventana.setVisible(true);

            // Servidor Monitor escucha los turnos del operador
            new Thread(new ServidorMonitor(5000, ventana)).start();
        });
    }
}