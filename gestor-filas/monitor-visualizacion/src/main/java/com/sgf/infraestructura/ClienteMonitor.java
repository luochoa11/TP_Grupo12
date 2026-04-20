package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.sgf.interfaces.IServicioMonitor;
import com.sgf.modelos.Turno;
import com.sgf.presentacion.ControladorMonitor;

public class ClienteMonitor implements Runnable, IServicioMonitor {
    private String host;
    private int puerto;
    private ControladorMonitor controlador;
    private boolean activo=true;

    public ClienteMonitor(String host, int puerto, ControladorMonitor controlador) {
        this.host = host;
        this.puerto = puerto;
        this.controlador = controlador;
    }

    // --- MÉTODOS DEL CONTRATO (Individuales) ---
    // Se mantienen para cumplir con la interfaz, aunque el loop principal no los use.
    @Override
    public Turno getUltimoLlamado() {
        try (Socket socket = new Socket(host, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject("GET_ULTIMO_LLAMADO_UNICO");// Comando específico si fuera necesario
            out.flush();
            return (Turno) in.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Turno> getHistorial() {
        try (Socket socket = new Socket(host, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject("GET_HISTORIAL");
            out.flush();
            return (List<Turno>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    @Override
    public void run() {
        System.out.println("Cliente Monitor iniciado. Conectando a " + host + ":" + puerto);

        while(activo){
            try(Socket socket = new Socket(host,puerto);
            
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            ){
                out.writeObject("GET_ESTADO_MONITOR");
                out.flush();

                Turno turnoActual = (Turno) in.readObject();
                List<Turno> historial = (List<Turno>) in.readObject();
                controlador.actualizarDesdeServidor(turnoActual, historial);   
            
            }catch (Exception e){
                System.err.println("Error de conexión: " + e.getMessage());
            }
            try {
                Thread.sleep(1000); // Espera 1 segundo antes de intentar reconectar
            } catch (InterruptedException e) {
                System.err.println("Cliente Monitor interrumpido: " + e.getMessage());
            }
        }
    }

}
