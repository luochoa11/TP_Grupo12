package com.sgf.persistencia;

/**
 * Implementación de la Fábrica Abstracta para la familia XML.
 */

public class FactoryXML implements IFactoryPersistencia{
    private final String rutaBase;

    public FactoryXML(String rutaBase) {
        this.rutaBase = rutaBase;
    }

    @Override
    public IPersistenciaLector crearLector() {
        System.out.println("[FactoryXML] Creando Lector de archivos XML.");
        return new PersistenciaLectorXML(this.rutaBase);
    }

    @Override
    public IPersistenciaEscritor crearEscritor() {
        System.out.println("[FactoryXML] Creando Escritor de archivos XML.");
        return new PersistenciaEscritorXML(this.rutaBase);
    }
}
