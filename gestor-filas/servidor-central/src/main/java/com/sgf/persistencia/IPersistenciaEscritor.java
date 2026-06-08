package com.sgf.persistencia;

import java.util.List;

import com.sgf.modelos.Turno;

/**
 * Contrato dedicado exclusivamente a la escritura y persistencia de datos en frío y en caliente.
 * (Producto abstracto)
 */

public interface IPersistenciaEscritor {
    //---métodos para guardado en caliente----
    void guardarFilaEspera(List<Turno> filaEspera) throws Exception;
    void guardarHistorial(List<Turno> historial) throws Exception;
    void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception;
    void guardarUltimoLlamado(Turno ultimoLlamado) throws Exception;
    void guardarHistorialReintentos(List<Turno> historialReintentos) throws Exception;

    //---método para guardado en frío
    void registrarTurnoFinalizado(Turno turno) throws Exception;
}
