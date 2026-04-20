package com.sgf.interfaces;
import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.modelos.Turno;

public interface IServicioRegistro {

    void agregarTurno(Turno t) throws DNIRepetidoException;
}
