package com.sgf.modelos;

/**
 * DTO que representa un heartbeat enviado por un nodo (servidor) para indicar que está activo.
 * 
 */
public class HeartbeatDTO {
    // Timestamp del heartbeat
    private long timestamp;
    // ID del nodo que envía el heartbeat
    private String nodoId;

}
