package com.sgf.persistencia;

import java.util.List;

import com.sgf.modelos.Turno;

/**
 * Contrato dedicado exclusivamente a la lectura y recuperación de datos.
 * (Producto abstracto)
 */

public interface IPersistenciaLector {
    List<Turno> recuperarFilaEspera() throws Exception;
    List<Turno> recuperarHistorial() throws Exception;
    List<Turno> recuperarTurnosActuales() throws Exception;
    Turno recuperarUltimoLlamado() throws Exception;
}
