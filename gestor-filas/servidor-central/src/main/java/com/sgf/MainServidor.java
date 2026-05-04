package com.sgf;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.aplicacion.LogicaFila;
import com.sgf.disponibilidad.EmisorHeartbeat;
import com.sgf.disponibilidad.SincronizadorEstado;
import com.sgf.infraestructura.ServidorCentral;

public class MainServidor {
    public static void main(String[] args) {

        boolean esPrimario = Boolean.parseBoolean(args[0]);
        ILogicaFila logica = LogicaFila.getInstance();
        int puerto = Constantes.PUERTO_SERVIDOR_CENTRAL; //como le pasamos esto? asi siempre crea un central, tendria que ser por arg?
        String ip = Constantes.HOST_SERVIDOR_CENTRAL; //lo mismo que el puerto, lo paso por arg o lo dejo asi?
        
        SincronizadorEstado sincronizador = new SincronizadorEstado(logica,Constantes.PUERTO_SERVIDOR_B,Constantes.HOST_SERVIDOR_B);
        ServidorCentral servidor = new ServidorCentral(puerto,ip,logica,esPrimario,sincronizador);
        
        Thread hiloServidor = new Thread(servidor);
        hiloServidor.start();

        EmisorHeartbeat heartbeat = new EmisorHeartbeat(Constantes.HOST_SERVIDOR_CENTRAL, Constantes.PUERTO_SERVIDOR_CENTRAL,ip,puerto);
        Thread hiloHeartbeat = new Thread(heartbeat);
        hiloHeartbeat.start();

        System.out.println("Servidor Central corriendo en el puerto " + Constantes.PUERTO_SERVIDOR_CENTRAL);
    }

}
