package com.sgf.disponibilidad;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.infraestructura.ServidorCentral;
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
    private ServidorCentral servidor;

    private String ip;
    private int puerto;

    public EmisorHeartbeat(String hostMonitor, int puertoMonitor, String ip, int puerto) {
        this.hostMonitor = hostMonitor;
        this.puertoMonitor = puertoMonitor;
        this.ip = ip;
        this.puerto = puerto;
    }

    @Override
    public void run() {
        while (activo) {
            try (
                Socket socket = new Socket(hostMonitor, puertoMonitor);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ) {

                HeartbeatDTO hb = new HeartbeatDTO();//Crear el DTO del heartbeat
                hb.setTimestamp(System.currentTimeMillis());

                NodoEstadoDTO estado = new NodoEstadoDTO();   // Crear el DTO del estado del nodo
                estado.setIp(ip);
                estado.setPuerto(puerto);
                estado.setEstado(1);
                estado.setEsPrimario(servidor.esPrimario());

                out.writeObject("HEARTBEAT"); // Envío    
                out.writeObject(hb);
                out.writeObject(estado);
                out.flush();

                NodoEstadoDTO pareja= (NodoEstadoDTO) in.readObject(); // Recibir el DTO del  server compañero
                if(pareja!=null){
                    if(pareja.isEsPrimario()&& servidor.esPrimario()) {
                        // Si el monitor me dice que el otro server es primario, pero yo también me considero primario, debo degradar mi estado a secundario
                        servidor.degradarEstado();
                    } else if (!pareja.isEsPrimario()) {
                        // Si el monitor me dice que el otro server es secundario, lo debo conocer para sincronizarlo
                       servidor.getSincronizador().actualizarSecundario(pareja.getIp(), pareja.getPuerto());

                    }
                }

                
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
