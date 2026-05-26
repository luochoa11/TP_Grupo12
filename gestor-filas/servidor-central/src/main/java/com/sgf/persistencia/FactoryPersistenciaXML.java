package com.sgf.persistencia;

import com.sgf.interfaces.IFactoryPersistencia;
import com.sgf.interfaces.IPersistenciaStrategy;

public class FactoryPersistenciaXML implements IFactoryPersistencia {

    @Override
    public IPersistenciaStrategy crearPersistencia() {
        System.out.println("[FACTORY-PERSISTENCIA] Creando instancia de persistencia XML.");
        return new PersistenciaXML();
    }

}
