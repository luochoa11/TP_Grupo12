package com.sgf.disponibilidad;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.infraestructura.ServidorCentral;
import com.sgf.interfaces.IServicioHeartbeat;
import com.sgf.modelos.HeartbeatDTO;
import com.sgf.modelos.NodoEstadoDTO;

public class EmisorHeartbeat implements Runnable,IServicioHeartbeat {

    private final String monitorIp;
    private final int    monitorPuerto;
    private final String miIp;
    private final int    miPuerto;
    private final ServidorCentral servidor;

    private volatile boolean activo = true;
    private final long intervalo = 2000;

    private String ultimaParejaSincronizada = null;

    public EmisorHeartbeat(String monitorIp, int monitorPuerto,
            String miIp, int miPuerto,
            boolean esPrimario, ServidorCentral servidor) {
        this.monitorIp     = monitorIp;
        this.monitorPuerto = monitorPuerto;
        this.miIp          = miIp;
        this.miPuerto      = miPuerto;
        this.servidor      = servidor;
    }

    @Override
    public void run() {
        System.out.println("[EmisorHeartbeat] Iniciando emisor para " + monitorIp + ":" + monitorPuerto);
        while (activo) {
            try {
                Thread.sleep(intervalo);
                enviarLatido();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                activo = false;
            }
        }
    }

    @Override
    public void enviarLatido() {
        System.out.println("[EmisorHeartbeat] Intentando conectar a " + monitorIp + ":" + monitorPuerto);
        try (Socket socket = new Socket(monitorIp, monitorPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            HeartbeatDTO hb = new HeartbeatDTO();
            hb.setTimestamp(System.currentTimeMillis());

            NodoEstadoDTO estado = new NodoEstadoDTO();
            estado.setIp(miIp);
            estado.setPuerto(miPuerto);
            estado.setEstado(1);
            // Lee dinámicamente si el nodo ha sido promovido o degradado
            estado.setEsPrimario(servidor.esPrimario());

            out.writeObject("HEARTBEAT");
            out.writeObject(hb);
            out.writeObject(estado);
            out.flush();

            NodoEstadoDTO pareja = (NodoEstadoDTO) in.readObject();
            System.out.println("[EmisorHeartbeat] Latido confirmado por monitor.");

            if (pareja != null) {
                if (pareja.isEsPrimario() && servidor.esPrimario()) {
                    // Conflicto: dos primarios → este se degrada
                    servidor.degradarEstado();
                } else if (!pareja.isEsPrimario() && servidor.esPrimario()) {
                    // Soy primario y detecto un secundario
                    String clavePareja = pareja.getIp() + ":" + pareja.getPuerto();
                    if (!clavePareja.equals(ultimaParejaSincronizada)) {
                        System.out.println("[Heartbeat] Nuevo secundario detectado -> " + clavePareja + ". Sincronizando...");
                        servidor.sincronizarEstado();
                        ultimaParejaSincronizada = clavePareja;
                    }
                }
            } else {
                ultimaParejaSincronizada = null; // ← cuando no hay pareja, reseteamos
            }
        } catch (Exception e) {
            System.err.println("[Heartbeat] Error al reportar latido al monitor: " + e.getMessage());
        }
    }

    public void detener() {
        activo = false;
    }
}