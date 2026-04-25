package com.sgf;

import javax.swing.SwingUtilities;

import com.sgf.infraestructura.ClienteMonitor;
import com.sgf.presentacion.ControladorMonitor;
import com.sgf.presentacion.VentanaMonitor;

public class MainMonitor {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1: Iniciamos la ventana
            VentanaMonitor ventana = new VentanaMonitor();
            
            // 2: Creamos el Controlador y le damos la ventana
            ControladorMonitor controlador = new ControladorMonitor(ventana);
            
            ventana.setVisible(true);

            // 3: El Servidor habla con el Controlador
            ClienteMonitor cliente = new ClienteMonitor(Constantes.HOST_SERVIDOR_CENTRAL, Constantes.PUERTO_SERVIDOR_CENTRAL, controlador);
            new Thread(cliente).start();
            
            System.out.println("Monitor: Sistema ensamblado y listo.");
        });
    }
}