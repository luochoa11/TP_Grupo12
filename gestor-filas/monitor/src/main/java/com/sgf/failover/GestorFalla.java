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
     private IServicioDirectorio servicioDirectorio;
     private IServicioControl controlPrimario;
     private IServicioControl controlSecundario;

     public GestorFalla(IServicioDirectorio servicioDirectorio, IServicioControl controlPrimario, IServicioControl controlSecundario) {
         this.servicioDirectorio = servicioDirectorio;
         this.controlPrimario = controlPrimario;
         this.controlSecundario = controlSecundario;
    }

    // Cuando recibe el aviso de falla:
    //  -Llama al IServicioControl del Servidor Secundario.
    //  -Informa al Directorio el cambio de IP.
    public void procesarFalla(NodoEstadoDTO nodoFalla) {
        // Lógica para procesar la falla
        try{
        System.out.println("Procesando falla del nodo: " + nodoFalla.getIp() + ":" + nodoFalla.getPuerto());
         
        if(nodoFalla.getIp().equals(controlPrimario.getIp()) && nodoFalla.getPuerto() == controlPrimario.getPuerto()) {
            // El nodo primario ha fallado, promover el secundario a primario

            controlSecundario.promoverEstado(controlPrimario.getIp(), controlPrimario.getPuerto()); //le pasa la info del primario para guardarla con nuevo secundario
            servicioDirectorio.actualizarPrimario(controlSecundario.getIp(), controlSecundario.getPuerto());

            IServicioControl temp = controlPrimario;
        
           this.controlPrimario = controlSecundario; // El secundario ahora es el primario
           this.controlSecundario = temp; // El primario ahora es el secundario (aunque esté caído, se mantiene la referencia para cuando vuelva a levantarse)
        } 

        } catch (Exception e) {
            System.err.println("Error procesando la falla: " + e.getMessage());
        }
       
    }
}
