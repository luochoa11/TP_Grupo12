package com.sgf.persistencia;

/**
 * Implementación de la Fábrica Abstracta para la familia JSON.
 */

public class FactoryJSON implements IFactoryPersistencia {

    @Override
    public IPersistenciaLector crearLector() {
        System.out.println("[FactoryJSON] Creando Lector de archivos JSON.");
        return new PersistenciaLectorJSON();
    }

    @Override
    public IPersistenciaEscritor crearEscritor() {
        System.out.println("[FactoryJSON] Creando Escritor de archivos JSON.");
        return new PersistenciaEscritorJSON();
    }
}