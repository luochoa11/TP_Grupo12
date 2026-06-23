package com.sgf.failover;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.modelos.NodoEstadoDTO;

/**
 * Clase responsable de ejecutar el protocolo de recuperación. 
 * Se encarga de detectar cuándo un servidor ha dejado de enviar latidos (heartbeats) 
 * y tomar las medidas necesarias para garantizar la continuidad del servicio.
 */
public class GestorFalla {

    private final String directorioIp;
    private final int    directorioPuerto;

    public GestorFalla(String directorioIp, int directorioPuerto) {
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
    }

    public boolean procesarFalla(NodoEstadoDTO nodoFalla, NodoEstadoDTO primario, NodoEstadoDTO secundario) {
        System.out.println("[Monitor] Procesando falla del nodo: " + nodoFalla.getIp() + ":" + nodoFalla.getPuerto());
        
        if(primario!=null && nodoFalla.getIp().equals(primario.getIp()) && nodoFalla.getPuerto() == primario.getPuerto()){
            System.out.println("[GestorFalla] El nodo caído es el primario. Promoviendo secundario a primario...");
            
            // Verificación estricta: Solo actualizamos el directorio si la promoción fue exitosa
            boolean promovido = enviarPromocion(secundario);
            
            if (promovido) {
                actualizarDirectorio(secundario.getIp(), secundario.getPuerto());
                return true;
            } else {
                System.err.println("[GestorFalla] ALERTA: El secundario también está inalcanzable. Falla Catastrófica (Blackout).");
                return false;
            }
        } else {
            System.out.println("[GestorFalla] El nodo caído no es el primario. No se requiere promoción.");
            return false;
        }
    }

    private boolean enviarPromocion(NodoEstadoDTO secundario){
        try{
            Socket socket = new Socket(secundario.getIp(), secundario.getPuerto());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            out.writeObject("MONITOR_FALLA");
            out.flush(); 

            out.writeObject("PROMOVER");
            out.flush();

            String respuesta = (String) in.readObject(); 
            System.out.println("[GestorFalla] Promoción confirmada: " + respuesta);
            socket.close();
            return true; // Promoción real exitosa
        } catch(Exception e){
            System.err.println("[GestorFalla] Error al enviar promoción (Nodo Muerto): " + e.getMessage());
            return false; // Promoción fallida
        }
    }

    private void actualizarDirectorio(String nuevaIp, int nuevoPuerto) {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("ACTUALIZAR_RUTA");
            out.writeObject(nuevaIp);
            out.writeObject(nuevoPuerto);
            out.flush();

            in.readObject(); // "OK"
            System.out.println("[GestorFalla] Directorio actualizado -> "+ nuevaIp + ":" + nuevoPuerto);

        } catch (Exception e) {
            System.err.println("[GestorFalla] Error al actualizar Directorio: " + e.getMessage());
        }
    }

    public void limpiarPrimarioEnDirectorio() {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("LIMPIAR_PRIMARIO");
            out.flush();
            in.readObject();
            System.out.println("[GestorFalla] Primario eliminado del directorio (Limpieza Blackout).");

        } catch (Exception e) {
            System.err.println("[GestorFalla] Error al limpiar primario: " + e.getMessage());
        }
    }

    public void limpiarSecundarioEnDirectorio(NodoEstadoDTO secundario) {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("LIMPIAR_SECUNDARIO");
            out.flush();

            in.readObject(); // "OK"
            System.out.println("[GestorFalla] Secundario eliminado del directorio.");

        } catch (Exception e) {
            System.err.println("[GestorFalla] Error al limpiar secundario en directorio: " + e.getMessage());
        }
    }
}


