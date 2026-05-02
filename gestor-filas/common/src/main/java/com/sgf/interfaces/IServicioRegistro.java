package com.sgf.interfaces;
import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.modelos.Turno;

/**
 * Interfaz con servicios que el cliente Registro puede solicitar al Servidor.
 */

public interface IServicioRegistro {

    void agregarTurno(Turno t) throws DNIRepetidoException;
}
