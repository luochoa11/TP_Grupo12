package com.sgf.modelos;

import java.io.Serializable;

/**
 * Representa un Turno en el sistema.
 */
public class Turno implements Serializable {
    private static final long serialVersionUID = 1L; //Para asegurar la compatibilidad durante la serializacion y deserializacion
    
    private String dniCliente;
    private int idPuesto;
    private int intentos;
    private String estado; // "ESPERA", "LLAMADO","ATENDIDO", "AUSENTE"
    
    private long tiempoCreacion;
    private long tiempoLlamado;
    private long tiempoAtendido;

    public Turno(String dniCliente) {
        this.dniCliente = dniCliente;
        this.idPuesto = -1; // Indica que aún no se ha asignado un puesto
        this.intentos = 0;
        this.estado = "ESPERA";

        this.tiempoCreacion = System.currentTimeMillis();
        this.tiempoLlamado = 0;
        this.tiempoAtendido = 0;
    }

    public String getDniCliente() {
        return dniCliente;
    }

    public void setDniCliente(String dniCliente) {
        this.dniCliente = dniCliente;
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
    public void setIntentos(int intentos) {
        this.intentos = intentos;
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

    public long getTiempoCreacion() {
        return tiempoCreacion;
    }

    public void setTiempoCreacion(long tiempoCreacion) {
        this.tiempoCreacion = tiempoCreacion;
    }

    public long getTiempoLlamado() {
        return tiempoLlamado;
    }

    public void setTiempoLlamado(long tiempoLlamado) {
        this.tiempoLlamado = tiempoLlamado;
    }

    public long getTiempoAtendido() {
        return tiempoAtendido;
    }

    public void setTiempoAtendido(long tiempoAtendido) {
        this.tiempoAtendido = tiempoAtendido;
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

    /**
     * Retorna el tiempo total que esperó el cliente en la fila antes de ser llamado.
     */
    public long obtenerTiempoEspera() {
        if (tiempoLlamado <= 0) return 0;
        return tiempoLlamado - tiempoCreacion;
    }

    /**
     * Retorna el tiempo total que tomó la atención del cliente en el puesto.
     */
    public long obtenerTiempoAtencion() {
        if (tiempoAtendido <= 0 || tiempoLlamado <= 0) return 0;
        return tiempoAtendido - tiempoLlamado;
    }

    public Turno clonar() {
        Turno copia = new Turno(this.dniCliente);
        copia.setIdPuesto(this.idPuesto);
        copia.setIntentos(this.intentos);
        copia.setEstado(this.estado);
        copia.setTiempoCreacion(this.tiempoCreacion);
        copia.setTiempoLlamado(this.tiempoLlamado);
        copia.setTiempoAtendido(this.tiempoAtendido);
        return copia;
    }
}
