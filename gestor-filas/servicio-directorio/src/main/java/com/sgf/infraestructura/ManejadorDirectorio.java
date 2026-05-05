package com.sgf.infraestructura;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ManejadorDirectorio es el encargado de procesar cada conexión entrante al Servidor de Directorio.
 */

public class ManejadorDirectorio implements Runnable {

    private Socket socket;
    private GestorRutas gestorRutas;

    public ManejadorDirectorio(Socket socket, GestorRutas gestorRutas) {
        this.socket = socket;
        this.gestorRutas = gestorRutas;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            String comando = (String) in.readObject();

            switch (comando) {
                case "GET_RUTA_PRIMARIA":
                    out.writeObject(gestorRutas.getIPPrimario());
                    out.writeObject(gestorRutas.getPuertoPrimario());
                    break;

                case "ACTUALIZAR_RUTA":
                    String nuevaIp = (String) in.readObject();
                    int nuevoPuerto = (int) in.readObject();
                    
                    gestorRutas.actualizarPrimario(nuevaIp, nuevoPuerto);
                    
                    out.writeObject("OK");
                    break;
                    
                default:
                    System.out.println("Comando desconocido recibido en el Directorio: " + comando);
                    break;
            }
            out.flush();

        } catch (Exception e) {
            System.err.println("Error procesando conexión en el Directorio: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}