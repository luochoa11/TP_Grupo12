package com.sgf.modelos;

import java.io.Serializable;
//import java.time.LocalDateTime; // Lo comento para usar en otras iteraciones
//import java.time.format.DateTimeFormatter;

/**
 * Representa un Turno en el sistema.
 */
public class Turno implements Serializable {
    private static final long serialVersionUID = 1L; //Para asegurar la compatibilidad durante la serializacion y deserializacion
    private String dniCliente;
    private int idPuesto;
    private int intentos;
    private String estado; // "ESPERA", "LLAMADO","ATENDIDO"
    //private LocalDateTime horario;

    public Turno(String dniCliente) {
        this.dniCliente = dniCliente;
        this.idPuesto = -1; // Indica que aún no se ha asignado un puesto
        this.intentos = 0;
        this.estado = "ESPERA";

        // this.horario = LocalDateTime.now();
    }

    public String getDniCliente() {
        return dniCliente;
    }
    
    public int getIdPuesto() {
        return idPuesto;
    }
    public void setIdPuesto(int idPuesto) {
        this.idPuesto = idPuesto;
    }
    public int getIntentos() {
        return intentos;
    }
    public void incrementarIntentos() {
        this.intentos++;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
            Turno other = (Turno) obj;
    
        return dniCliente != null && dniCliente.equals(other.dniCliente);
    }

    @Override
    public int hashCode() {
        return dniCliente != null ? dniCliente.hashCode() : 0;
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
