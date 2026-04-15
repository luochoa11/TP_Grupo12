package com.sgf;

import java.util.List;
import java.util.Map;

import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.excepciones.FilaVaciaException;

/**
 * Contrato de servicios del Servidor Central para la gestion de filas
 */
public interface ILogicaFila {
    void agregarTurno(Turno t) throws DNIRepetidoException;
    Turno llamarSiguiente(int idPuesto) throws FilaVaciaException;
    Turno reIntentarLlamado(int idPuesto);

    Turno getUltimoLlamado();
    List<Turno> getHistorial();
    List<Turno> getCola();
    Map<Integer, Turno> getTurnosActivos();
    Turno getTurnoPuesto(int idPuesto);
    boolean hasDni(String dni);
}
