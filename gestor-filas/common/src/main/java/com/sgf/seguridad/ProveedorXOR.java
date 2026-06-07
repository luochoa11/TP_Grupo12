package com.sgf.seguridad;

public class ProveedorXOR extends ProveedorEstrategiaCifrado {

    @Override
    public IEncriptacionStrategy crear(String clave) {
        return new EstrategiaCifradoXOR(clave);
    }

}
