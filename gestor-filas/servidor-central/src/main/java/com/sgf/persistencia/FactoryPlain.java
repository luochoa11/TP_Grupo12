package com.sgf.persistencia;

/**
 * Implementación de la Fábrica Abstracta para la familia TextoPlano.
 */

public class FactoryPlain implements IFactoryPersistencia{
    private final String rutaBase;

    public FactoryPlain(String rutaBase) {
        this.rutaBase = rutaBase;
    }

    @Override
    public IPersistenciaLector crearLector() {
        System.out.println("[FactoryTextoPlano] Creando Lector de archivos de texto plano.");
        return new PersistenciaLectorPlain(this.rutaBase);
    }

    @Override
    public IPersistenciaEscritor crearEscritor() {
        System.out.println("[FactoryTextoPlano] Creando Escritor de archivos de texto plano.");
        return new PersistenciaEscritorPlain(this.rutaBase);
    }
}
