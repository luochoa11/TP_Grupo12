package com.sgf;

import javax.swing.SwingUtilities;

import com.sgf.infraestructura.ClienteRegistro;
import com.sgf.interfaces.IServicioRegistro;
import com.sgf.presentacion.ControladorRegistro;
import com.sgf.presentacion.VentanaTerminalRegistro;

public class MainRegistro {
    public static void main(String[] args) {
        System.out.println("Arrancando...");
        
        // Identifica la terminal si se pasan argumentos
        String idTerminal = (args.length > 0) ? " #" + args[0] : "";
        final String tituloFinal = "Terminal de Registro" + idTerminal;

        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        SwingUtilities.invokeLater(() -> {
            VentanaTerminalRegistro ventana = new VentanaTerminalRegistro();
            ventana.setTitle(tituloFinal); // Seteamos el título con el ID

            IServicioRegistro servicio = new ClienteRegistro(Constantes.HOST_SERVIDOR_CENTRAL,Constantes.PUERTO_SERVIDOR_CENTRAL);
            
            ControladorRegistro controlador = new ControladorRegistro(ventana, servicio);
            ventana.setControlador(controlador);
            ventana.setVisible(true);
            
            System.out.println("Ventana creada: " + tituloFinal);
        });

    }
}