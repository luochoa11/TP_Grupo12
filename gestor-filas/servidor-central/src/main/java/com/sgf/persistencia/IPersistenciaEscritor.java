package com.sgf.persistencia;

import java.util.List;

import com.sgf.modelos.Turno;

/**
 * Contrato dedicado exclusivamente a la escritura y persistencia.
 * (Producto abstracto)
 */

public interface IPersistenciaEscritor {
    void guardarFilaEspera(List<Turno> filaEspera) throws Exception;
    void guardarHistorial(List<Turno> historial) throws Exception;
    void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception;
    void guardarUltimoLlamado(Turno ultimoLlamado) throws Exception;
    void guardarHistorialReintentos(List<Turno> historialReintentos) throws Exception;
}
