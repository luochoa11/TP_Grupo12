package com.sgf;

import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import com.sgf.excepciones.DNIRepetidoException;

import java.io.IOException;

public class ServidorOperador implements Runnable {

    private int puerto;
    private ControladorOperador controlador;

    public ServidorOperador(int puerto, ControladorOperador controlador) {
        this.puerto = puerto;
        this.controlador = controlador;
    }

    @Override
    public void run() {
        // Atrapamos si el puerto está en uso acá
        try (ServerSocket server = new ServerSocket(puerto)) {
            System.out.println("Servidor Operador activo en puerto " + puerto);

            while (true) {
                Socket socketCliente = server.accept();

                // Creamos un hilo para atender a este cliente sin bloquear el servidor
                new Thread(() -> {
                try (
                    ObjectInputStream in = new ObjectInputStream(socketCliente.getInputStream());
                    PrintWriter out = new PrintWriter(socketCliente.getOutputStream(), true)
                ) {

                    Turno turno = (Turno) in.readObject();

                    try {
                        controlador.procesarTurnoDesdeRed(turno);
                        out.println("OK");
                        out.flush();

                    } catch (DNIRepetidoException e) {

                        out.println("ERROR_DNI_REPETIDO");

                    }

                } catch (Exception e) {
                    System.err.println("Error de red: " + e.getMessage());
                } finally {
                    try {
                        socketCliente.close();
                    } catch (IOException ignored) {}
                }
            }).start();
            }

        } catch (BindException e) {
            System.err.println("¡Atención! El puerto " + puerto + " ya está en uso. Cerrá la otra ventana del Operador.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}