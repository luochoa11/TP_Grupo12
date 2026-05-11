package com.sgf.disponibilidad;

import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;

/**
 * Clase que envía datos del servidor primario al servidor secundario para mantenerlos 
 * sincronizados en caso de falla del primario.
 */
public class SincronizadorEstado {
    private ILogicaFila logica;
    int puertoSecundario=0;
    String ipSecundario=null;
  

    public SincronizadorEstado(ILogicaFila logica) {
        this.logica = logica;
    }

    public void sincronizar(){
        if (ipSecundario == null)  return;
        try (Socket socket = new Socket(ipSecundario, puertoSecundario);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

        out.writeObject("SINCRONIZAR_ESTADO");
        out.writeObject(logica.getCola());
        out.writeObject(logica.getTurnosActivos());
        out.writeObject(logica.getHistorial());
        out.writeObject(logica.getUltimoLlamado());

        out.flush();
        socket.close();

        System.out.println("[Sync] Estado sincronizado con el servidor secundario en " + ipSecundario + ":" + puertoSecundario);

        }catch(Exception e){
            System.err.println("Error al sincronizar estado: " + e.getMessage());
        }    
    
    }
     public void actualizarSecundario(String ip, int puerto) {
        this.ipSecundario = ip;
        this.puertoSecundario = puerto;
    }
}