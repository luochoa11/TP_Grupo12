package com.sgf.interfaces;

import com.sgf.modelos.HeartbeatDTO;

/**
 * Servicio de Heartbeat para que los servidores puedan reportar su estado al Monitor.
 */

public interface IServicioHeartbeat {
    //Latidos
    public void latidos(HeartbeatDTO latido);

}
