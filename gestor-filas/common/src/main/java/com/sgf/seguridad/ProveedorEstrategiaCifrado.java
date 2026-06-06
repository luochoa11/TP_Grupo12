package com.sgf.seguridad;

public class ProveedorEstrategiaCifrado {

    public static IEncriptacionStrategy crear(String algoritmo,String clave){
        switch (algoritmo.toUpperCase()) {
            case "AES":
                return new EstrategiaCifradoAES(clave);
            case "DES":
                return new EstrategiaCifradoDES(clave);
            case "XOR":
                return new EstrategiaCifradoXOR(clave);
           
            default:
                throw new IllegalArgumentException("Algoritmo de cifrado no soportado: " + algoritmo);
        }
    }

}
