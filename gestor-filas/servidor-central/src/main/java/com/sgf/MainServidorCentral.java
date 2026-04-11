package com.sgf;

public class MainServidorCentral {
    public static void main(String[] args) {
        
        // Inicializamos la lógica centralizada (única instancia en todo el sistema)
        LogicaFila fila = LogicaFila.getInstance(); 

        // Arrancamos el servidor en un hilo
        ServidorCentral servidor = new ServidorCentral(Constantes.PUERTO_SERVIDOR_CENTRAL);
        new Thread(servidor).start();
        
        System.out.println("Servidor Central corriendo en el puerto " + Constantes.PUERTO_SERVIDOR_CENTRAL);
    }
}