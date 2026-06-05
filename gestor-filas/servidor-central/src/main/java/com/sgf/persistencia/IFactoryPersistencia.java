package com.sgf.persistencia;

/**
 * Interfaz encargada de definir la creación de la familia
 * de productos de persistencia (Lector y Escritor).
*/

public interface IFactoryPersistencia {
    IPersistenciaLector crearLector(); //Crea producto concreto de lectura
    IPersistenciaEscritor crearEscritor(); //Crea producto concreto de escritura

}
