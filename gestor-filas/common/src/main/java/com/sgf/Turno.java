package com.sgf;

import java.io.Serializable;
//import java.time.LocalDateTime; // Lo comento para usar en otras iteraciones
//import java.time.format.DateTimeFormatter;

/**
 * Representa un Turno en el sistema.
 */
public class Turno implements Serializable {
    private static final long serialVersionUID = 1L; //Para asegurar la compatibilidad durante la serializacion y deserializacion
    private String dniCliente;
    private int puestoAtencion;
    //private LocalDateTime horario;

    public Turno(String dniCliente) {
        this.dniCliente = dniCliente;
        // this.horario = LocalDateTime.now();
    }

    public String getDniCliente() {
        return dniCliente;
    }
    
    public int getPuestoAtencion() {
        return puestoAtencion;
    }

    public void setPuestoAtencion(int puestoAtencion) {
        this.puestoAtencion = puestoAtencion;
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
