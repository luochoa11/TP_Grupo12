package com.sgf;

public class MainTerminal {
    public static void main(String[] args) {

        System.out.println("Arrancando...");

        ClienteTerminal cliente = new ClienteTerminal(Constantes.HOST_SERVIDOR_CENTRAL,Constantes.PUERTO_SERVIDOR_CENTRAL);

        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        VentanaTerminalRegistro ventana = new VentanaTerminalRegistro(cliente);
        ControladorRegistro controlador = new ControladorRegistro(ventana, cliente);
        ventana.setControlador(controlador);
        ventana.setVisible(true);

        System.out.println("Ventana creada");

    }
}