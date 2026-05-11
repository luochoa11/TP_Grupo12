package com.sgf.disponibilidad;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.infraestructura.ServidorCentral;
import com.sgf.modelos.HeartbeatDTO;
import com.sgf.modelos.NodoEstadoDTO;

public class EmisorHeartbeat implements Runnable {

    private final String monitorIp;
    private final int    monitorPuerto;
    private final String miIp;
    private final int    miPuerto;
    private final boolean esPrimario;
    private final ServidorCentral servidor;

    private volatile boolean activo = true;
    private final long intervalo = 2000;

    public EmisorHeartbeat(String monitorIp, int monitorPuerto,
                           String miIp, int miPuerto,
                           boolean esPrimario, ServidorCentral servidor) {
        this.monitorIp     = monitorIp;
        this.monitorPuerto = monitorPuerto;
        this.miIp          = miIp;
        this.miPuerto      = miPuerto;
        this.esPrimario    = esPrimario;
        this.servidor      = servidor;
    }

    @Override
    public void run() {
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

    private void enviarLatido() {
        try (Socket socket = new Socket(monitorIp, monitorPuerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            HeartbeatDTO hb = new HeartbeatDTO();
            hb.setTimestamp(System.currentTimeMillis());

            NodoEstadoDTO estado = new NodoEstadoDTO();
            estado.setIp(miIp);
            estado.setPuerto(miPuerto);
            estado.setEstado(1);
            estado.setEsPrimario(esPrimario);

            out.writeObject("HEARTBEAT");
            out.writeObject(hb);
            out.writeObject(estado);
            out.flush();

            NodoEstadoDTO pareja = (NodoEstadoDTO) in.readObject();

            if (pareja != null) {
                if (pareja.isEsPrimario() && esPrimario) {
                    // Ambos se creen primarios → yo cedo
                    servidor.degradarEstado();
                } else if (!pareja.isEsPrimario()) {
                    // La pareja es secundaria → actualizo sincronizador
                    servidor.getSincronizador().actualizarSecundario(
                        pareja.getIp(), pareja.getPuerto()
                    );
                }
            }

        } catch (Exception e) {
            System.err.println("[Heartbeat] Error enviando latido: " + e.getMessage());
        }
    }

    public void detener() {
        activo = false;
    }
}