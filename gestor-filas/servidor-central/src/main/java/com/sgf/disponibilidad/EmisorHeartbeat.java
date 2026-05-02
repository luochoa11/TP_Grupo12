package com.sgf.disponibilidad;

import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.modelos.HeartbeatDTO;
import com.sgf.modelos.NodoEstadoDTO;

/**
 * Clase encargada de enviar latidos (heartbeats) al Monitor para reportar el estado del Servidor.
 */
public class EmisorHeartbeat implements Runnable {
    // Hilo que llama a IServicioHeartbeat
   
    private String hostMonitor;
    private int puertoMonitor;
    private boolean activo = true;

    private String nodoId;
    private String ip;
    private int puerto;

    public EmisorHeartbeat(String hostMonitor, int puertoMonitor, String nodoId, String ip, int puerto) {
        this.hostMonitor = hostMonitor;
        this.puertoMonitor = puertoMonitor;
        this.nodoId = nodoId;
        this.ip = ip;
        this.puerto = puerto;
    }

     @Override
    public void run() {
        while (activo) {
            try (
                Socket socket = new Socket(hostMonitor, puertoMonitor);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ) {

                // 1. Crear el DTO del heartbeat
                HeartbeatDTO hb = new HeartbeatDTO();
                hb.setTimestamp(System.currentTimeMillis());
                hb.setNodoId(nodoId);

                // 2. Crear el DTO del estado del nodo
                NodoEstadoDTO estado = new NodoEstadoDTO();
                estado.setIp(ip);
                estado.setPuerto(puerto);
                estado.setEstado(1);

                // 3. Envío
                out.writeObject("HEARTBEAT");
                out.writeObject(hb);
                out.writeObject(estado);
                out.flush();

                Thread.sleep(2000);

            } catch (Exception e) {
                System.err.println("Error enviando heartbeat: " + e.getMessage());
            }
        }
    }

    public void detener() {
        this.activo = false;
    }
    
}
