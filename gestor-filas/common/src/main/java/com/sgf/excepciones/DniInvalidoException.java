package com.sgf.excepciones;

public class DniInvalidoException extends Exception{
    public DniInvalidoException(String dni){
        super("El DNI '"+dni+"' es inválido. Debe contener entre 7 y 8 digitos numéricos.");
    }
}
