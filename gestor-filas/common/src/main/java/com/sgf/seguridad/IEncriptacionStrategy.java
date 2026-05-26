package com.sgf.seguridad;

public interface IEncriptacionStrategy {
    String encriptar(String dato);
    String desencriptar(String datoEncriptado);
}
