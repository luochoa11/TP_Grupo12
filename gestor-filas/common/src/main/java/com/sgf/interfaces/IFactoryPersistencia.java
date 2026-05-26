package com.sgf.interfaces;

import com.sgf.seguridad.IEncriptacionStrategy;

public interface IFactoryPersistencia {
    IPersistenciaStrategy crearPersistencia();

}
