package com.sgf.persistencia;

/**
 * Implementación de la Fábrica Abstracta para la familia TextoPlano.
 */

public class FactoryPlain implements IFactoryPersistencia{

    @Override
    public IPersistenciaLector crearLector() {
        System.out.println("[FactoryTextoPlano] Creando Lector de archivos de texto plano.");
        return new PersistenciaLectorPlain();
    }

    @Override
    public IPersistenciaEscritor crearEscritor() {
        System.out.println("[FactoryTextoPlano] Creando Escritor de archivos de texto plano.");
        return new PersistenciaEscritorPlain();
    }
}
