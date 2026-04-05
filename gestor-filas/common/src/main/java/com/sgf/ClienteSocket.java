package com.sgf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

        try (
            Socket socket = conectarConRetry();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            out.writeObject(turno);
            out.flush();
            System.out.println("Turno enviado: " + turno.getDniCliente());

        } catch (Exception e) {
            System.err.println("Error enviando turno al servidor: " + e.getMessage());
        }
    }

  public String procesarTurnoRemoto(Turno turno) {
    try (
        Socket socket = conectarConRetry();
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        )
    ) {

        out.writeObject(turno);
        out.flush();

        return in.readLine(); // "OK" o "ERROR_DNI_REPETIDO"

    } catch (Exception e) {
        return "ERROR_CONEXION";
    }
}

private Socket conectarConRetry() throws InterruptedException {
    while (true) {
        try {
            return new Socket(host, puerto);
        } catch (IOException e) {
            System.out.println("Esperando servidor en puerto " + puerto + "...");
            Thread.sleep(2000);
        }
    }
}

}