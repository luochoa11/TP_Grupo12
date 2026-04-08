package com.sgf;

public class MainServidor {
    public static void main(String[] args) {
        int puerto = Constantes.PUERTO_SERVIDOR_CENTRAL;
        ServidorCentral servidor = new ServidorCentral(puerto);
        Thread hiloServidor = new Thread(servidor);
        hiloServidor.start();
    }

}
