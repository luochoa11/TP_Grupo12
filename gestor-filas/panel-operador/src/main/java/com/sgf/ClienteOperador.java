package com.sgf;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class ClienteOperador {
    private String host;
    private int puerto;

    public ClienteOperador(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public Turno llamarSiguiente(int idPuesto){
        try (Socket socket = new Socket(host, puerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Enviamos el ID del puesto al servidor
            out.writeObject("LLAMAR_SIGUIENTE");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();
            
            if(respuesta instanceof Turno) {
                return (Turno) respuesta;
            } else {
                System.err.println("Respuesta inesperada del servidor: " + respuesta);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error al comunicarse con el servidor: " + e.getMessage());
            return null;
        }
    }

    public Turno reintentarLlamado(int idPuesto){
        try (Socket socket = new Socket(host, puerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Enviamos el ID del puesto al servidor
        out.writeObject("REINTENTAR_LLAMADO");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();
            
            if(respuesta instanceof Turno) {
                return (Turno) respuesta;
            } else {
                System.err.println("Respuesta inesperada del servidor: " + respuesta);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error al comunicarse con el servidor: " + e.getMessage());
            return null;
        }
    }

    public List<Turno> getCola() {
        try (Socket socket = new Socket(host, puerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_COLA");
            out.flush();

            Object respuesta = in.readObject();
            
            if(respuesta instanceof List) {
                return (List<Turno>) respuesta;
            } else {
                System.err.println("Respuesta inesperada del servidor: " + respuesta);
                return Collections.emptyList();
            }

        } catch (Exception e) {
            System.err.println("Error al comunicarse con el servidor: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Turno getTurnoActual(int idPuesto) {
        try (Socket socket = new Socket(host, puerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_TURNO_PUESTO");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();
            
            if(respuesta instanceof Turno) {
                return (Turno) respuesta;
            } else {
                System.err.println("Respuesta inesperada del servidor: " + respuesta);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error al comunicarse con el servidor: " + e.getMessage());
            return null;
        }
    }

    

   
}
