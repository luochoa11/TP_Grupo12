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
    private int puertoSecundario;
    private String ipSecundario;

    public SincronizadorEstado(ILogicaFila logica, int puertoSecundario, String ipSecundario) {
        this.logica = logica;
        this.ipSecundario = ipSecundario;
        this.puertoSecundario = puertoSecundario;
    }

    public void sincronizar(){
        try{
            if (ipSecundario == null) {
                return;
            }
        Socket socket = new Socket(ipSecundario, puertoSecundario);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        out.writeObject("SINCRONIZAR_ESTADO");
        out.writeObject(logica.getCola());
        out.writeObject(logica.getTurnosActivos());
        out.writeObject(logica.getHistorial());
        out.writeObject(logica.getUltimoLlamado());

        out.flush();
        socket.close();

        System.out.println("Estado sincronizado con el servidor secundario en " + ipSecundario + ":" + puertoSecundario);

        }catch(Exception e){
            System.err.println("Error al sincronizar estado: " + e.getMessage());
        }    
    
    }
     public void actualizarSecundario(String ip, int puerto) {
        this.ipSecundario = ip;
        this.puertoSecundario = puerto;
    }
}