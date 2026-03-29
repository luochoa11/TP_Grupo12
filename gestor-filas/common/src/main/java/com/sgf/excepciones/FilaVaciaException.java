package com.sgf.excepciones;

public class FilaVaciaException extends SGFException{
    public FilaVaciaException(){
        super("La fila está vacía. No hay clientes para llamar.");
    }
}
