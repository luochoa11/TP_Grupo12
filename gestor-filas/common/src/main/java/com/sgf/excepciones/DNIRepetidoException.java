package com.sgf.excepciones;

public class DNIRepetidoException extends Exception{
    private String dni;
    public DNIRepetidoException(String dni){
        super("El DNI '"+dni+"' Ya tiene un turno asignado en la fila.");
        this.dni = dni;
    }

    public String getDni(){
        return this.dni;
    }

}

