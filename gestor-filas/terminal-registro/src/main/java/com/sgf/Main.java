package com.sgf;

public class Main {
    public static void main(String[] args) {

        System.out.println("Arrancando...");

        ClienteSocket cliente = new ClienteSocket("localhost",Constantes.PUERTO_OPERADOR1);

          try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
       } catch (Exception e) {
           e.printStackTrace();
        }
      
        VentanaTerminalRegistro ventana = new VentanaTerminalRegistro(cliente);
        ventana.setVisible(true);

        System.out.println("Ventana creada");

    }
}