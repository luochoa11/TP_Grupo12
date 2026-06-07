package com.sgf.seguridad;

public class ProveedorAES extends ProveedorEstrategiaCifrado {

    @Override
    public IEncriptacionStrategy crear(String clave) {
        return new EstrategiaCifradoAES(clave);
    }

}
