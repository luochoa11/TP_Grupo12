package com.sgf.disponibilidad;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;

/**
 * Clase que envía datos del servidor primario al servidor secundario para mantenerlos 
 * sincronizados en caso de falla del primario.
 */


public class SincronizadorEstado {

    private final ILogicaFila logica;
    private final String directorioIp;
    private final int    directorioPuerto;

    public SincronizadorEstado(ILogicaFila logica, String directorioIp, int directorioPuerto) {
        this.logica           = logica;
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
    }

    private String[] resolverSecundario() {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_RUTA_SECUNDARIA");
            out.flush();

            String ip     = (String) in.readObject();
            int    puerto = (int)    in.readObject();


            System.out.println("[Sync] Secundario resuelto → " + ip + ":" + puerto);


            if (ip == null) return null;
            return new String[]{ip, String.valueOf(puerto)};

        } catch (Exception e) {
            System.err.println("[Sync] No se pudo consultar el Directorio: " + e.getMessage());
            return null;
        }
    }

    public void sincronizar() {
        String[] secundario = resolverSecundario();
        if (secundario == null) {
            System.out.println("[Sync] No hay secundario registrado.");
            return;
        }

        String ip     = secundario[0];
        int    puerto = Integer.parseInt(secundario[1]);

        try (Socket socket = new Socket(ip, puerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject("SINCRONIZAR_ESTADO");
            out.writeObject(logica.getCola());
            out.writeObject(logica.getTurnosActivos());
            out.writeObject(logica.getHistorial());
            out.writeObject(logica.getUltimoLlamado());
            out.flush();

            System.out.println("[Sync] Estado sincronizado → " + ip + ":" + puerto);

        } catch (Exception e) {
            System.err.println("[Sync] Error al sincronizar: " + e.getMessage());
        }
    }
}