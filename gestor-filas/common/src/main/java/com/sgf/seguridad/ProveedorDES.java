package com.sgf.seguridad;

public class ProveedorDES extends ProveedorEstrategiaCifrado {

    @Override
    public IEncriptacionStrategy crear(String clave) {
        return new EstrategiaCifradoDES(clave);
    }

}
