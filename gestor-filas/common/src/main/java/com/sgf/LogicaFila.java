package com.sgf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.excepciones.FilaVaciaException;

/**
 * Gestiona la estructura de datos de la fila y el historial.
 * Cumple con el requerimiento funcional de lógica FIFO.
 */
public class LogicaFila {
    private static LogicaFila instance = null;
    
    // Cola de espera para los turnos que no fueron llamados aun
    private Queue<Turno> filaEspera =  new LinkedList<>();
    
    // Lista para el historial de los últimos 4 llamados
    private List<Turno> historial = new ArrayList<>();
    
    // Turno que está siendo llamado actualmente
    private Turno turnoActual = null;

    private LogicaFila() {

    }
    
    public static LogicaFila getInstance() {
		if (LogicaFila.instance == null)
			LogicaFila.instance = new LogicaFila();
		return instance;
	}

    /**
     * Registra un nuevo turno proveniente de la terminal, validando repeticion.
     * -> No es respo de la fila esta validacion -> Asume validado por la ventana
     */
    public void agregarTurno(Turno t) throws DNIRepetidoException {

       
            String dni = t.getDniCliente().trim();
            if (hasDni(dni)) throw new DNIRepetidoException(dni);
            else this.filaEspera.add(t);
        }

    /**
     * Lógica de llamado (utiliza Panel de Operador).
     * Lanza excepción si no hay nadie.
     */
    public Turno llamarSiguiente() throws FilaVaciaException {
        if (filaEspera.isEmpty()) {
            throw new FilaVaciaException();
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

    public List<Turno> getCola() {
    return new ArrayList<>(filaEspera);
    }

    public boolean hasDni(String dni) {
        for (Turno t : filaEspera) {
            if (t.getDniCliente().equals(dni)) {
                return true;
            }
        }
        return false;
    }
}