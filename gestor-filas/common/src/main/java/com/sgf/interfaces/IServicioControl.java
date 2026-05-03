package com.sgf.interfaces;

/**
 * Interfaz que define los métodos que el Servidor Central expone para que el Monitor
 * pueda enviar órdenes (ej: setMode(PRIMARY/SECONDARY))
 */

public interface IServicioControl {
    //o ServerControl
    // Se implemente en ServidorCentral

    //promueve el nodo a primario
    void promoverEstado(String ip, int puerto);


}
