package com.sgf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Gestiona la estructura de datos de la fila y el historial.
 * Cumple con el requerimiento funcional de lógica FIFO.
 */
public class LogicaFila {
    
    // Cola de espera para los turnos que no fueron llamados aun
    private Queue<Turno> filaEspera;
    
    // Lista para el historial de los últimos 4 llamados
    private List<Turno> historial;
    
    // Turno que está siendo llamado actualmente
    private Turno turnoActual;

    public LogicaFila() {
        this.filaEspera = new LinkedList<>();
        this.historial = new ArrayList<>();
        this.turnoActual = null;
    }

    /**
     * Registra un nuevo turno proveniente de la terminal.
     */
    public void agregarTurno(Turno t) {
        if (t != null && !t.getDniCliente().trim().isEmpty()) {
            this.filaEspera.add(t);
        }
    }

    /**
     * Lógica de llamado (utiliza Panel de Operador).
     */
    public Turno llamarSiguiente() {
        if (filaEspera.isEmpty()) {
            return null;
        }

        // Si había alguien en pantalla, pasa al historial antes de llamar al nuevo
        if (this.turnoActual != null) {
            this.historial.add(0, this.turnoActual);

            if (this.historial.size() > 4) {
                this.historial.remove(4); 
                // Mantiene el máximo de 4 en el historial
            }
        }

        //el nuevo turno sale de la cola y se conviert en el turno actual
        this.turnoActual = this.filaEspera.poll();
        return this.turnoActual;
    }

    /**
     * @return el turno que debe mostrarse destacado en el monitor.
     */
    public Turno getTurnoActual() {
        return turnoActual;
    }

    public List<Turno> getHistorial() {
        return new ArrayList<>(historial);
    }

    public int getCantidadEnEspera() {
        return filaEspera.size();
    }
}