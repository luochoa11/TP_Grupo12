package com.sgf.seguridad;

public class ProveedorEstrategiaCifrado {

    public static IEncriptacionStrategy crear(String algoritmo, String clave) {
    switch (algoritmo.toUpperCase().trim()) {
        case "AES":
        case "AES-128":
            return new EstrategiaCifradoAES(clave);
        case "DES":
        case "TRIPLEDES":
        case "3DES":
            return new EstrategiaCifradoDES(clave);
        case "XOR":
        case "BLOWFISH":
            return new EstrategiaCifradoXOR(clave);
        default:
            throw new IllegalArgumentException("Algoritmo no soportado: " + algoritmo);
        }
    }

}
