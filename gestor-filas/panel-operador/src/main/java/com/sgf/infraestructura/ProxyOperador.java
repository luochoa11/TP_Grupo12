package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import com.sgf.excepciones.FilaVaciaException;
import com.sgf.interfaces.IServicioOperador;
import com.sgf.modelos.Turno;

public class ProxyOperador implements IServicioOperador{
    private final String directorioIp;
    private final int directorioPuerto;

    //pseudocache
    private String ipServidor;
    private int    puertoServidor;


    public ProxyOperador(String directorioIp, int directorioPuerto) {
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
        resolverServidor();
    }


    private void resolverServidor() {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_RUTA_PRIMARIA");
            out.flush();

            this.ipServidor     = (String) in.readObject();
            this.puertoServidor = (int)    in.readObject();

            System.out.println("[ProxyOperador] Servidor resuelto → "
                + ipServidor + ":" + puertoServidor);

        } catch (Exception e) {
            throw new RuntimeException(
                "[ProxyOperador] No se pudo resolver el Servidor desde el Directorio: "
                + e.getMessage());
        }
    }

    // Abre un socket al Servidor usando el cache actual
    private Socket conectarServidor() throws Exception {
        return new Socket(ipServidor, puertoServidor);
    }

    // Si falla la conexión al Servidor, re-consulta al Directorio y reintenta una vez
    private Socket conectarConFallback() throws Exception {
        try {
            return conectarServidor();
        } catch (Exception e) {
            System.out.println("[ProxyOperador] Fallo de conexión, re-consultando Directorio...");
            resolverServidor();
            return conectarServidor();
        }
    }


    @Override
    public Turno llamarSiguiente(int idPuesto) throws FilaVaciaException {
        try (Socket socket = conectarConFallback();
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("LLAMAR_SIGUIENTE");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();

            if ("ERROR_FILA_VACIA".equals(respuesta)) throw new FilaVaciaException();
            return (Turno) respuesta;

        } catch (FilaVaciaException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[ProxyOperador] Error en llamarSiguiente: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Turno reintentarLlamado(int idPuesto) {
        try (Socket socket = conectarConFallback();
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("REINTENTAR_LLAMADO");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();

            if (respuesta instanceof Turno) return (Turno) respuesta;

            System.err.println("[ProxyOperador] Respuesta inesperada: " + respuesta);
            return null;

        } catch (Exception e) {
            System.err.println("[ProxyOperador] Error en reintentarLlamado: " + e.getMessage());
            return null;
        }
    }

     @SuppressWarnings("unchecked")
    @Override
    public List<Turno> getCola() {
        try (Socket socket = conectarConFallback();
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_COLA");
            out.flush();

            Object respuesta = in.readObject();

            if (respuesta instanceof List)  return (List<Turno>) respuesta;
            if (respuesta == null)          return Collections.emptyList();

            System.err.println("[ProxyOperador] Respuesta inesperada: " + respuesta);
            return Collections.emptyList();

        } catch (Exception e) {
            System.err.println("[ProxyOperador] Error en getCola: " + e.getMessage());
            return Collections.emptyList();
        }
    }

@Override
    public Turno getTurnoPuesto(int idPuesto) {
        try (Socket socket = conectarConFallback();
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_TURNO_PUESTO");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();

            if (respuesta instanceof Turno) return (Turno) respuesta;
            if (respuesta == null)          return null;

            System.err.println("[ProxyOperador] Respuesta inesperada: " + respuesta);
            return null;

        } catch (Exception e) {
            System.err.println("[ProxyOperador] Error en getTurnoPuesto: " + e.getMessage());
            return null;
        }
    }
}