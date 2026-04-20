package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import com.sgf.excepciones.FilaVaciaException;
import com.sgf.interfaces.IServicioOperador;
import com.sgf.modelos.Turno;

public class ClienteOperador implements IServicioOperador{
    private String host;
    private int puerto;

    public ClienteOperador(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    @Override
    public Turno llamarSiguiente(int idPuesto) throws FilaVaciaException {
        try (Socket socket = new Socket(host, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Enviamos el ID del puesto al servidor
            out.writeObject("LLAMAR_SIGUIENTE");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();
            
            if ("ERROR_FILA_VACIA".equals(respuesta)) throw new FilaVaciaException();
                return (Turno) respuesta;

        } catch (FilaVaciaException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
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

    @Override
    public List<Turno> getCola() {
        try (Socket socket = new Socket(host, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_COLA");
            out.flush();

            Object respuesta = in.readObject();
            
            if(respuesta instanceof List) {
                return (List<Turno>) respuesta;
            } else if(respuesta==null) { //cola vacia
                return Collections.emptyList();
            }else{
                System.err.println("Respuesta inesperada del servidor: " + respuesta);
                return Collections.emptyList();
            }

        } catch (Exception e) {
            System.err.println("Error al comunicarse con el servidor: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Turno getTurnoPuesto(int idPuesto) {
        try (Socket socket = new Socket(host, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_TURNO_PUESTO");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();
            
            if(respuesta instanceof Turno) {
                return (Turno) respuesta;
            } else if(respuesta==null){
                return null; //no hay turno en ese puesto
            } else{
                System.err.println("Respuesta inesperada del servidor: " + respuesta);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error al comunicarse con el servidor: " + e.getMessage());
            return null;
        }
    }

}
