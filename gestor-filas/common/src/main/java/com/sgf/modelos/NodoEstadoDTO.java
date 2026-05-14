package com.sgf.modelos;

import java.io.Serializable;

/**
 * DTO que representa el estado de un nodo (servidor) en el sistema.
 */

public class NodoEstadoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    // IP del nodo 
    private String ip;
    // Puerto del nodo 
    private int puerto;
    // Estado del nodo (0: inactivo, 1: activo)
    private int estado;

    private boolean esPrimario;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public boolean isEsPrimario() { 
        return esPrimario; 
    }

    public void setEsPrimario(boolean esPrimario) { 
        this.esPrimario = esPrimario; 
    }   
}
