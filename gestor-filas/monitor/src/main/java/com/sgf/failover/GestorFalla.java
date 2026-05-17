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
    // Cuando recibe el aviso de falla:
    //  -Llama al IServicioControl del Servidor Secundario.
    //  -Informa al Directorio el cambio de IP.
    
    public void procesarFalla(NodoEstadoDTO nodoFalla, NodoEstadoDTO primario, NodoEstadoDTO secundario) {
        // Lógica para procesar la falla
    
        System.out.println("Procesando falla del nodo: " + nodoFalla.getIp() + ":" + nodoFalla.getPuerto());
        if(primario!=null && nodoFalla.getIp().equals(primario.getIp()) && nodoFalla.getPuerto() == primario.getPuerto()){
            System.out.println("[GF]El nodo caído es el primario. Promoviendo secundario a primario...");
            
            enviarPromocion(secundario);
            actualizarDirectorio(secundario.getIp(), secundario.getPuerto());
        } else {
            System.out.println("[GF]El nodo caído no es el primario. No se requiere acción inmediata.");
        }
    
    }
    private void enviarPromocion(NodoEstadoDTO secundario){
        try{
            Socket socket = new Socket(secundario.getIp(), secundario.getPuerto());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            

            out.writeObject("PROMOVER");
            out.flush();

            String respuesta = (String) in.readObject(); //bloquea
            System.out.println("[GestorFalla] Promoción confirmada: " + respuesta);
            socket.close();
        } catch(Exception e){
            System.err.println("Error al enviar promoción: " + e.getMessage());
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
            System.out.println("[GestorFalla] Directorio actualizado → "+ nuevaIp + ":" + nuevoPuerto);

        } catch (Exception e) {
            System.err.println("[GestorFalla] Error al actualizar Directorio: " + e.getMessage());
        }
    }
}

