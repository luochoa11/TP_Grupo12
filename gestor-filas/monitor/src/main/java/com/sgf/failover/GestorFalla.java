package com.sgf.failover;

import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.interfaces.IServicioDirectorio;
import com.sgf.modelos.NodoEstadoDTO;

/**
 * Clase responsable de ejecutar el protocolo de recuperación. 
 * Se encarga de detectar cuándo un servidor ha dejado de enviar latidos (heartbeats) 
 * y tomar las medidas necesarias para garantizar la continuidad del servicio.
 */
public class GestorFalla {
    // Server Manager
    private IServicioDirectorio servicioDirectorio;

    public GestorFalla(IServicioDirectorio servicioDirectorio) {
        this.servicioDirectorio = servicioDirectorio;
    }

    // Cuando recibe el aviso de falla:
    //  -Llama al IServicioControl del Servidor Secundario.
    //  -Informa al Directorio el cambio de IP.
    public void procesarFalla(NodoEstadoDTO nodoFalla, NodoEstadoDTO primario, NodoEstadoDTO secundario) {
        // Lógica para procesar la falla
    
        System.out.println("Procesando falla del nodo: " + nodoFalla.getIp() + ":" + nodoFalla.getPuerto());
        if(primario!=null && nodoFalla.getIp().equals(primario.getIp()) && nodoFalla.getPuerto() == primario.getPuerto()){
            System.out.println("El nodo caído es el primario. Promoviendo secundario a primario...");
            
            enviarPromocion(secundario);
            servicioDirectorio.actualizarPrimario(secundario.getIp(), secundario.getPuerto());
        } else {
            System.out.println("El nodo caído no es el primario. No se requiere acción inmediata.");
        }
    
    }
    private void enviarPromocion(NodoEstadoDTO secundario){
        try{
            Socket socket = new Socket(secundario.getIp(), secundario.getPuerto());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            out.writeObject("PROMOVER");
            out.flush();

            socket.close();
        } catch(Exception e){
            System.err.println("Error al enviar promoción: " + e.getMessage());
        }

    }
}
