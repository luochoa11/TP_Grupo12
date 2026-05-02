package com.sgf.interfaces;
import java.util.List;

import com.sgf.modelos.Turno;

/**
 * Define el contrato de servicios que el Servidor 
 * ofrece para las necesidades de la Pantalla de Anuncio.
 */
public interface IServicioAnuncio {

    /**
     * Recupera el último turno que fue llamado al monitor principal.
     */
    Turno getUltimoLlamado();

    /**
     * Recupera la lista de los últimos turnos llamados (historial).
     */
    List<Turno> getHistorial();
}
