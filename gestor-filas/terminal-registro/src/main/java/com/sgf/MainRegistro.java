package com.sgf;

import javax.swing.SwingUtilities;

public class MainRegistro {
    public static void main(String[] args) {
        //ejemplo 
        // java -cp "terminal-registro/target/classes;common/target/classes" com.sgf.MainTerminal 2

        System.out.println("Arrancando...");
        // Identifica la terminal si se pasan argumentos
        String idTerminal = "";
        if (args.length > 0) {
            idTerminal = " #" + args[0];
        }

        ClienteRegistro cliente = new ClienteRegistro(Constantes.HOST_SERVIDOR_CENTRAL,Constantes.PUERTO_SERVIDOR_CENTRAL);

        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        final String tituloFinal = "Terminal de Registro" + idTerminal;

        SwingUtilities.invokeLater(() -> {
            VentanaTerminalRegistro ventana = new VentanaTerminalRegistro(cliente);
            ventana.setTitle(tituloFinal); // Seteamos el título con el ID
            
            ControladorRegistro controlador = new ControladorRegistro(ventana, cliente);
            ventana.setControlador(controlador);
            ventana.setVisible(true);
            
            System.out.println("Ventana creada: " + tituloFinal);
        });

    }
}