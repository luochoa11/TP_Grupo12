package com.sgf.modelos;

/**
 * DTO que representa el estado de un nodo (servidor) en el sistema.
 * 
 */
public class NodoEstadoDTO {
    // IP del nodo 
    private String ip;
    // Puerto del nodo 
    private int puerto;
    // Estado del nodo (0: inactivo, 1: activo)
    private int estado;
}
