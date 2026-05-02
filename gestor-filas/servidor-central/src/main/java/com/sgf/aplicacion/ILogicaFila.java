package com.sgf.aplicacion;

import java.util.List;
import java.util.Map;

import com.sgf.interfaces.IServicioAnuncio;
import com.sgf.interfaces.IServicioOperador;
import com.sgf.interfaces.IServicioRegistro;
import com.sgf.modelos.Turno;

/**
 * Contrato de servicios del Servidor Central para la gestion de filas
 */
public interface ILogicaFila extends IServicioRegistro, IServicioOperador, IServicioAnuncio {
    //IServicioRegistro
    //void agregarTurno(Turno t) throws DNIRepetidoException;

    //IServicioOperador
    //Turno llamarSiguiente(int idPuesto) throws FilaVaciaException;
    //Turno reintentarLlamado(int idPuesto);
    //List<Turno> getCola();
    //Turno getTurnoPuesto(int idPuesto);

    //IServicioAnuncio
    //Turno getUltimoLlamado();
    //List<Turno> getHistorial();

    //Métodos del servidor
    Map<Integer, Turno> getTurnosActivos();

    boolean hasDni(String dni);

    void reemplazarEstado(List<Turno> nuevaCola, Map<Integer, Turno> nuevosActivos, List<Turno> nuevoHistorial, Turno nuevoUltimo);
}
