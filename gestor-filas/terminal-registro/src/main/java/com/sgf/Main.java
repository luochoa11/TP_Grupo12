package com.sgf;

public class Main {
    public static void main(String[] args) {

        System.out.println("Arrancando...");

        ClienteSocket cliente = new ClienteSocket("localhost",Constantes.PUERTO_OPERADOR1);
      
        VentanaTerminalRegistro ventana = new VentanaTerminalRegistro(cliente);
        ventana.setVisible(true);

        System.out.println("Ventana creada");

    }
}