package com.sgf.seguridad;

public class SelectorProveedores {

    public static ProveedorEstrategiaCifrado obtenerProveedor(String algoritmo){
        if (algoritmo == null) {
            System.err.println("[SelectorProveedores] Algoritmo nulo. Usando AES por defecto.");
            return new ProveedorAES();
        }
        switch(algoritmo.toUpperCase().trim()) {
            case "AES":
            case "AES-128":
                return new ProveedorAES();
            case "DES":
            case "TRIPLEDES":
                return new ProveedorDES();
            case "XOR":
            case "BLOWFISH":
                return new ProveedorXOR();
            default:
                System.err.println("[SelectorProveedores] Algoritmo desconocido en config.properties: '" + algoritmo + "'. Usando AES por defecto.");
                return new ProveedorAES();
        }
    }
}
