package com.sgf;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Servidor de Red para el Monitor.
 * Escucha objetos serializados (Turno y List<Turno>) para respetar la lógica FIFO.
 */
public class ServidorMonitor implements Runnable {

    private int puerto;
    private VentanaMonitorVisualizacion vista;

    public ServidorMonitor(int puerto, VentanaMonitorVisualizacion vista) {
        this.puerto = puerto;
        this.vista = vista;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor Monitor activo en puerto " + puerto);

            while (true) {
                // Aceptamos la conexión del Operador
                try (Socket socket = serverSocket.accept();
                    // Creamos un ObjectInputStream para recibir objetos serializados
                    //esto porque usamos la clase Turno y List<Turno>
                    //Si fuera un string, debemos usar un BufferedReader
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                    
                    // Leemos los objetos en el orden en que el Operador los envía
                    Turno actual = (Turno) in.readObject();
                    List<Turno> historial = (List<Turno>) in.readObject();
                    
                    // Actualizamos la interfaz gráfica con los objetos reales
                    if (actual != null) {
                        vista.actualizarPantalla(actual, historial);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error al recibir objetos: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fatal en ServidorMonitor: " + e.getMessage());
        }
    }
}