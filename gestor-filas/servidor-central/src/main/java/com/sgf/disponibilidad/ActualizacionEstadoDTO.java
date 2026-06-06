package com.sgf.disponibilidad;

import java.io.Serializable;

import com.sgf.modelos.Turno;

public class ActualizacionEstadoDTO implements Serializable{
    private static final long serialVersionUID = 1L;

    private final String tipo; // "AGREGAR", "LLAMAR", "REINTENTAR"
    private final Turno turno;
    private final int idPuesto;

    public ActualizacionEstadoDTO(String tipo, Turno turno, int idPuesto) {
        this.tipo = tipo;
        this.turno = turno;
        this.idPuesto = idPuesto;
    }

    public String getTipo() {
        return tipo;
    }

    public Turno getTurno() {
        return turno;
    }

    public int getIdPuesto() {
        return idPuesto;
    }

    @Override
    public String toString() {
        return "Delta [" + tipo + " | Puesto: " + idPuesto + " | DNI: " + (turno != null ? turno.getDniCliente() : "N/A") + "]";
    }
}
