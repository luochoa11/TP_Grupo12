package com.sgf.persistencia;

/**
 * Implementación de la Fábrica Abstracta para la familia XML.
 */

public class FactoryXML implements IFactoryPersistencia{

    @Override
    public IPersistenciaLector crearLector() {
        System.out.println("[FactoryXML] Creando Lector de archivos XML.");
        return new PersistenciaLectorXML();
    }

    @Override
    public IPersistenciaEscritor crearEscritor() {
        System.out.println("[FactoryXML] Creando Escritor de archivos XML.");
        return new PersistenciaEscritorXML();
    }
}
