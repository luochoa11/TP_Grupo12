package com.sgf;

public class Main {
    public static void main(String[] args) {

        int PUERTO = Constantes.PUERTO_OPERADOR1;

        LogicaFila logica = new LogicaFila();

        // UI operador
        VentanaPanelOperador ventana = new VentanaPanelOperador(logica);
        
        
        // servidor que recibe turnos
        ServidorOperador servidor = new ServidorOperador(PUERTO, logica, ventana);
        new Thread(servidor).start();
        
        ventana.setVisible(true); 
    }
}