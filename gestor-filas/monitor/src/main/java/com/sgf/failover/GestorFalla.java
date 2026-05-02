package com.sgf.failover;

import com.sgf.interfaces.IServicioControl;

/**
 * Clase responsable de ejecutar el protocolo de recuperación. 
 * Se encarga de detectar cuándo un servidor ha dejado de enviar latidos (heartbeats) 
 * y tomar las medidas necesarias para garantizar la continuidad del servicio.
 */
public class GestorFalla {
    // Server Manager

    // Cuando recibe el aviso de falla:
    //  -Llama al IServicioControl del Servidor Secundario.
    //  -Informa al Directorio el cambio de IP.
    public void procesarFalla(){
        // Lógica para procesar la falla
    }
}
