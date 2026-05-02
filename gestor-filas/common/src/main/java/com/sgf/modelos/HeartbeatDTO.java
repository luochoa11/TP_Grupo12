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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNodoId() {
        return nodoId;
    }

    public void setNodoId(String nodoId) {
        this.nodoId = nodoId;
    }

}
