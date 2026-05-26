package com.sgf.persistencia;

import com.sgf.interfaces.IFactoryPersistencia;
import com.sgf.interfaces.IPersistenciaStrategy;

public class FactoryPersistenciaTextoPlano implements IFactoryPersistencia {

    @Override
    public IPersistenciaStrategy crearPersistencia() {
        System.out.println("[FACTORY-PERSISTENCIA] Creando instancia de persistencia en Texto Plano.");
        return new PersistenciaTextoPlano();
    }

}
