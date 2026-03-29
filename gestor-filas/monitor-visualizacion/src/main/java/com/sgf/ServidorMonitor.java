package com.sgf;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class ServidorMonitor implements Runnable {

    private int puerto;
    private VentanaMonitorVisualizacion ventana;
    private List<Turno> historial;
    private Turno turnoActual;

    public ServidorMonitor(int puerto, VentanaMonitorVisualizacion ventana) {
        this.puerto = puerto;
        this.ventana = ventana;
        this.historial = new ArrayList<>();
        this.turnoActual = null;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor Monitor activo en puerto " + puerto);

            while (true) {
                try (Socket socket = serverSocket.accept();
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                    Turno nuevoTurno = (Turno) in.readObject();

                    // Movemos el turno actual al historial
                    if (turnoActual != null) {
                        historial.add(0, turnoActual);
                        if (historial.size() > 4) {
                            historial.remove(4); // máximo 4 turnos
                        }
                    }

                    turnoActual = nuevoTurno;

                    // Actualizamos la UI en el EDT
                    SwingUtilities.invokeLater(() -> ventana.actualizarPantalla(turnoActual, historial));

                } catch (Exception e) {
                    System.err.println("Error recibiendo turno: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fatal en ServidorMonitor: " + e.getMessage());
        }
    }
}