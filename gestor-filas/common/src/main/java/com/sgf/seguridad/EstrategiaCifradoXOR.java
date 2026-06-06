package com.sgf.seguridad;

import java.util.Base64;

public class EstrategiaCifradoXOR implements IEncriptacionStrategy {

    private String clave;

    public EstrategiaCifradoXOR(String clave) {
        this.clave = clave;
    }

    @Override
    public String encriptar(String dato) {
        byte[] datos = dato.getBytes();
        byte[] resultado = new byte[datos.length];
     
        for(int i = 0; i < datos.length; i++) {
            resultado[i] = (byte) (datos[i] ^ clave.charAt(i % clave.length()));
        }
        return Base64.getEncoder().encodeToString(resultado);
    }

    @Override
    public String desencriptar(String datoEncriptado) {
       byte[] datos = Base64.getDecoder().decode(datoEncriptado);
        byte[] resultado = new byte[datos.length];
     
        for(int i = 0; i < datos.length; i++) {
            resultado[i] = (byte) (datos[i] ^ clave.charAt(i % clave.length()));
        }
        return new String(resultado);
    }

}
