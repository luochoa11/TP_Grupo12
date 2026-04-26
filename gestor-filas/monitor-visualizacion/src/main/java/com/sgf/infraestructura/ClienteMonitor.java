package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.sgf.interfaces.IServicioMonitor;
import com.sgf.modelos.Turno;
import com.sgf.presentacion.ControladorMonitor;

public class ClienteMonitor implements Runnable, IServicioMonitor {
    private String host;
    private int puerto;
    private ControladorMonitor controlador;
    private boolean activo=true;

    private Turno actual; //el ultimo actual
    private List<Turno> historial; //el ultimo historial 

    public ClienteMonitor(String host, int puerto, ControladorMonitor controlador) {
        this.host = host;
        this.puerto = puerto;
        this.controlador = controlador;
    }

    // --- MÉTODOS DEL CONTRATO (Individuales) ---
    // Se mantienen para cumplir con la interfaz, aunque el loop principal no los use.
    @Override
    public Turno getUltimoLlamado() {
        return actual;
    }

    @Override
    public List<Turno> getHistorial() {
        return historial;
    }


    @Override
        public void run() {
            try (
                Socket socket = new Socket(host, puerto);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ) {
                out.writeObject("SUSCRIBIR_MONITOR"); 
                out.flush();

                while (true) {
                    Turno actual = (Turno) in.readObject(); // Se duerme el hilo hasta que llegue algo
                    List<Turno> historial = (List<Turno>) in.readObject();

                    controlador.actualizarDesdeServidor(actual, historial);
                }

            } catch (Exception e) {
                System.err.println("Monitor desconectado: " + e.getMessage());
            }
        }
}
