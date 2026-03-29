package com.sgf;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMonitor implements Runnable {

    private int puerto;
    private ControladorMonitor controlador;

    public ServidorMonitor(int puerto, ControladorMonitor controlador) {
        this.puerto = puerto;
        this.controlador = controlador;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor Monitor activo en puerto " + puerto);

            while (true) {
                try (Socket socket = serverSocket.accept();
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                    Turno nuevoTurno = (Turno) in.readObject();

                    // Delegamos la lógica al controlador
                    controlador.recibirNuevoTurno(nuevoTurno);

                } catch (Exception e) {
                    System.err.println("Error recibiendo turno: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fatal en ServidorMonitor: " + e.getMessage());
        }
    }
}