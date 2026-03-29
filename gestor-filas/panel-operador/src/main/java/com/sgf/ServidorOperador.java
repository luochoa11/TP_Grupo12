package com.sgf;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorOperador implements Runnable {

    private int puerto;
    private LogicaFila logica; 
    private VentanaPanelOperador ventana;

    public ServidorOperador(int puerto, LogicaFila logica, VentanaPanelOperador ventana) {
        this.puerto = puerto;
        this.logica = logica;
        this.ventana = ventana;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(puerto)) {
            System.out.println("Servidor Operador activo en puerto " + puerto);

            while (true) {
                Socket socket = server.accept(); // escucha

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                Turno turno = (Turno) in.readObject();

                logica.agregarTurno(turno);
                ventana.actualizarVista();

                System.out.println("En cola: " + logica.getCantidadEnEspera());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}