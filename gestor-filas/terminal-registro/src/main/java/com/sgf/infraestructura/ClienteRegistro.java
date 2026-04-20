package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.interfaces.IServicioRegistro;
import com.sgf.modelos.Turno;

public class ClienteRegistro implements IServicioRegistro{
private String host;
private int puerto;

    public ClienteRegistro(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    @Override
    public void agregarTurno(Turno turno) throws DNIRepetidoException {
        try(Socket socket = new Socket(host, puerto)) {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject("NUEVO_TURNO");
        out.writeObject(turno);
        out.flush();

        String respuesta = (String) in.readObject();

        // Si el servidor nos mandó este String, lanzamos la excepción nosotros
        if ("ERROR_DNI_REPETIDO".equals(respuesta)) {
            throw new DNIRepetidoException(turno.getDniCliente());
        }
    
        } catch (DNIRepetidoException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con el servidor");
        }
    }

}