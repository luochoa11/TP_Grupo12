package com.sgf.aplicacion;

import java.util.Map;

import com.sgf.interfaces.IServicioMonitor;
import com.sgf.interfaces.IServicioOperador;
import com.sgf.interfaces.IServicioRegistro;
import com.sgf.modelos.Turno;

/**
 * Contrato de servicios del Servidor Central para la gestion de filas
 */
public interface ILogicaFila extends IServicioRegistro, IServicioOperador, IServicioMonitor {
    //IServicioRegistro
    //void agregarTurno(Turno t) throws DNIRepetidoException;

    //IServicioOperador
    //Turno llamarSiguiente(int idPuesto) throws FilaVaciaException;
    //Turno reintentarLlamado(int idPuesto);
    //List<Turno> getCola();
    //Turno getTurnoPuesto(int idPuesto);

    //IServicioMonitor
    //Turno getUltimoLlamado();
    //List<Turno> getHistorial();

    //Métodos del servidor
    Map<Integer, Turno> getTurnosActivos();

    boolean hasDni(String dni);
}
