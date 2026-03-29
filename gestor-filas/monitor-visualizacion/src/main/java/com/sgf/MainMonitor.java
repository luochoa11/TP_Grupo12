package com.sgf;

import javax.swing.SwingUtilities;

public class MainMonitor {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1: Creamos la Vista
            VentanaMonitorVisualizacion ventana = new VentanaMonitorVisualizacion();
            
            // 2: Creamos el Controlador y le damos la vista
            ControladorMonitor controlador = new ControladorMonitor(ventana);
            
            ventana.setVisible(true);

            // 3: El Servidor habla con el Controlador
            new Thread(new ServidorMonitor(5000, controlador)).start();
            
            System.out.println("SGF Monitor: Sistema ensamblado y listo.");
        });
    }
}