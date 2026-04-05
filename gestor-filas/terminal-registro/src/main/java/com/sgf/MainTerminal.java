package com.sgf;

public class MainTerminal {
    public static void main(String[] args) {

        System.out.println("Arrancando...");

        ClienteSocket cliente = new ClienteSocket(Constantes.HOST_OPERADOR1,Constantes.PUERTO_OPERADOR1);

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