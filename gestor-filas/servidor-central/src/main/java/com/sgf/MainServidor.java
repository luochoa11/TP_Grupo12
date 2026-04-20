package com.sgf;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.aplicacion.LogicaFila;
import com.sgf.infraestructura.ServidorCentral;

public class MainServidor {
    public static void main(String[] args) {
        ILogicaFila logica = LogicaFila.getInstance();
        int puerto = Constantes.PUERTO_SERVIDOR_CENTRAL;
        
        ServidorCentral servidor = new ServidorCentral(puerto, logica);
        
        Thread hiloServidor = new Thread(servidor);
        hiloServidor.start();
        
        System.out.println("Servidor Central corriendo en el puerto " + Constantes.PUERTO_SERVIDOR_CENTRAL);
    }

}
