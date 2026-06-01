package com.sgf.interfaces;

/* Contrato de fábrica de persistencia.
 */

public interface IFactoryPersistencia {
    IPersistenciaStrategy crearPersistencia();

}
