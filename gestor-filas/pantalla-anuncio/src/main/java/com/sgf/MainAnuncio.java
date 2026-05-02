package com.sgf;

import javax.swing.SwingUtilities;

import com.sgf.infraestructura.ProxyAnuncio;
import com.sgf.presentacion.ControladorAnuncio;
import com.sgf.presentacion.VentanaMonitor;

public class MainAnuncio {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1: Iniciamos la ventana
            VentanaMonitor ventana = new VentanaMonitor();
            
            // 2: Creamos el Controlador y le damos la ventana
            ControladorAnuncio controlador = new ControladorAnuncio(ventana);
            
            ventana.setVisible(true);

            // 3: El Servidor habla con el Controlador
            ProxyAnuncio cliente = new ProxyAnuncio(Constantes.HOST_SERVIDOR_CENTRAL, Constantes.PUERTO_SERVIDOR_CENTRAL, controlador);
            new Thread(cliente).start();
            
            System.out.println("Monitor: Sistema ensamblado y listo.");
        });
    }
}