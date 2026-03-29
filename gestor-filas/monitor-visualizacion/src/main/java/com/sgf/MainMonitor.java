package com.sgf;

import javax.swing.SwingUtilities;

public class MainMonitor {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1: Iniciamos la ventana
            VentanaMonitorVisualizacion ventana = new VentanaMonitorVisualizacion();
            
            // 2: Creamos el Controlador y le damos la ventana
            ControladorMonitor controlador = new ControladorMonitor(ventana);
            
            ventana.setVisible(true);

            // 3: El Servidor habla con el Controlador
            new Thread(new ServidorMonitor(Constantes.PUERTO_MONITOR1, controlador)).start();
            
            System.out.println("Monitor: Sistema ensamblado y listo.");
        });
    }
}