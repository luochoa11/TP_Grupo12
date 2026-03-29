package com.sgf;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Cliente que se conecta a un servidor mediante sockets
 * y envía objetos serializables (como Turno o List<Turno>).
 */
public class ClienteSocket {

    private String host;
    private int puerto;

    public ClienteSocket(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    /**
     * Envía un objeto Turno al servidor.
     * Crea la conexión, envía y cierra automáticamente.
     */
    public void enviarTurno(Turno turno) {
        if (turno == null) return;

        try (Socket socket = new Socket(host, puerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(turno);
            out.flush();
            System.out.println("Turno enviado: " + turno.getDniCliente());

        } catch (IOException e) {
            System.err.println("Error enviando turno al servidor: " + e.getMessage());
        }
    }
}