package com.sgf;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClienteMonitor implements Runnable {
    private String host;
    private int puerto;
    private ControladorMonitor controlador;
    private boolean activo=true;

    public ClienteMonitor(String host, int puerto, ControladorMonitor controlador) {
        this.host = host;
        this.puerto = puerto;
        this.controlador = controlador;
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
