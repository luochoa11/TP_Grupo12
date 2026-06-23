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

                case "REGISTRAR":
                    String ipReg=(String) in.readObject();
                    int    puertoReg=(int)    in.readObject();
                    out.writeObject(gestorRutas.registrar(ipReg, puertoReg));
                    break;
                    
                case "GET_RUTA_SECUNDARIA":
                    out.writeObject(gestorRutas.getIPSecundario());
                    out.writeObject(gestorRutas.getPuertoSecundario());
                    break;

                case "LIMPIAR_SECUNDARIO":
                    gestorRutas.limpiarSecundario();
                    out.writeObject("OK");
                    break;

                case "LIMPIAR_PRIMARIO":
                    gestorRutas.limpiarPrimario();
                    out.writeObject("OK");
                    break;

                default:
                    System.out.println("[Directorio] Comando desconocido recibido: " + comando);
                    break;
            }
            out.flush();

        } catch (Exception e) {
            System.err.println("[Directorio] Error procesando conexión: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}