package com.sgf.interfaces;


/**
 * Servicio de Heartbeat para que los servidores puedan reportar su estado al Monitor.
 */

public interface IServicioHeartbeat {
    //Latidos
    public void enviarLatido();
}
