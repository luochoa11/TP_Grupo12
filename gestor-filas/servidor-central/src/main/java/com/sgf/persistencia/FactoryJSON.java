package com.sgf.persistencia;

/**
 * Implementación de la Fábrica Abstracta para la familia JSON.
 */

public class FactoryJSON implements IFactoryPersistencia {
    private final String rutaBase;

    public FactoryJSON(String rutaBase) {
        this.rutaBase = rutaBase;
    }

    @Override
    public IPersistenciaLector crearLector() {
        System.out.println("[FactoryJSON] Creando Lector de archivos JSON.");
        return new PersistenciaLectorJSON(this.rutaBase);
    }

    @Override
    public IPersistenciaEscritor crearEscritor() {
        System.out.println("[FactoryJSON] Creando Escritor de archivos JSON.");
        return new PersistenciaEscritorJSON(this.rutaBase);
    }
}