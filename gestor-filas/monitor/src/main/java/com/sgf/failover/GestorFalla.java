package com.sgf.failover;

import com.sgf.interfaces.IServicioControl;
import com.sgf.interfaces.IServicioDirectorio;
import com.sgf.modelos.NodoEstadoDTO;

/**
 * Clase responsable de ejecutar el protocolo de recuperación. 
 * Se encarga de detectar cuándo un servidor ha dejado de enviar latidos (heartbeats) 
 * y tomar las medidas necesarias para garantizar la continuidad del servicio.
 */
public class GestorFalla {
    // Server Manager
     private IServicioControl servicioControl;
     private IServicioDirectorio servicioDirectorio;

     public GestorFalla(IServicioControl servicioControl, IServicioDirectorio servicioDirectorio) {
        this.servicioControl = servicioControl;
        this.servicioDirectorio = servicioDirectorio;
    }

    // Cuando recibe el aviso de falla:
    //  -Llama al IServicioControl del Servidor Secundario.
    //  -Informa al Directorio el cambio de IP.
    public void procesarFalla(NodoEstadoDTO nodo) {
        // Lógica para procesar la falla
        try{
        System.out.println("Procesando falla del nodo: " + nodo.getIp() + ":" + nodo.getPuerto());
            
        if(nodo.getIp().equals(servicioDirectorio.getIPPrimario()) && nodo.getPuerto() == servicioDirectorio.getPuertoPrimario()) {
            System.out.println("Falla detectada en el nodo primario: " + nodo.getIp() + ":" + nodo.getPuerto());
        } else {
            System.out.println("Falla detectada en un nodo secundario: " + nodo.getIp() + ":" + nodo.getPuerto());
            return; // Si la falla no es del primario, no hacemos nada
        }
        
        String ipSecundario = servicioDirectorio.getIPSecundario();
        int puertoSecundario = servicioDirectorio.getPuertoSecundario();

       //servicioControl.primoverPrimario(id, puesto)
        servicioDirectorio.actualizarPrimario(ipSecundario, puertoSecundario);
        servicioControl.promoverEstado(ipSecundario, puertoSecundario);

        } catch (Exception e) {
            System.err.println("Error procesando la falla: " + e.getMessage());
        }
       
    }
}
