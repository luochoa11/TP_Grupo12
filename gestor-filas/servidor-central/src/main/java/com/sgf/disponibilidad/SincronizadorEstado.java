package com.sgf.disponibilidad;

import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.interfaces.IServicioDirectorio;

/**
 * Clase que envía datos del servidor primario al servidor secundario para mantenerlos 
 * sincronizados en caso de falla del primario.
 */
public class SincronizadorEstado {
    private ILogicaFila logica;
    private IServicioDirectorio directorio;

    public SincronizadorEstado(ILogicaFila logica, IServicioDirectorio directorio) {
        this.logica = logica;
        this.directorio = directorio;
    }

    public void sincronizar(){
        try{
         String ipSecundario = directorio.getIPSecundario();
         int puertoSecundario = directorio.getPuertoSecundario();

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
}