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
        System.out.println("[EmisorHeartbeat] Iniciando, apuntando a " + monitorIp + ":" + monitorPuerto);
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
            estado.setEsPrimario(esPrimario);

            out.writeObject("HEARTBEAT");
            out.writeObject(hb);
            out.writeObject(estado);
            out.flush();

            NodoEstadoDTO pareja = (NodoEstadoDTO) in.readObject();

        if (pareja != null) {
            if (pareja.isEsPrimario() && esPrimario) {
                servidor.degradarEstado();
            } else if (!pareja.isEsPrimario()) {
                if (servidor.esPrimario()) {
                    System.out.println("[Heartbeat] Secundario detectado, sincronizando estado...");
                    servidor.sincronizarEstado();
                }
            }
        }

        } catch (Exception e) {
            System.err.println("[Heartbeat] Error enviando latido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void detener() {
        activo = false;
    }
}