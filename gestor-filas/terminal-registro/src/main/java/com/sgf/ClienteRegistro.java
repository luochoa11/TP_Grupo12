package com.sgf;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClienteRegistro {
private String host;
private int puerto;

    public ClienteRegistro(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public String registrarTurno(Turno turno) {
        try(Socket socket = new Socket(host, puerto)) {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject("NUEVO_TURNO");
        out.writeObject(turno);

        return (String) in.readObject();
    
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error"; // Simulación de respuesta de error
    }

}