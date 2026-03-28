package com.sgf; // Coincidiendo con tu estructura de carpetas

import java.io.Serializable;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;

/**
 * Representa un Turno en el sistema.
 */
public class Turno implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dniCliente;
    //private LocalDateTime horario;

    public Turno(String dniCliente) {
        this.dniCliente = dniCliente;
        // this.horario = LocalDateTime.now();
    }

    public String getDniCliente() {
        return dniCliente;
    }

    //public LocalDateTime getHorario() {
    //    return horario;
    //}

    //public String getHorarioFormateado() {
    //    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    //    return horario.format(formatter);
    //}

    //@Override
    //public String toString() {
    //    return "Turno - DNI: " + dniCliente + " (Hora: " + getHorarioFormateado() + ")";
    //}
}