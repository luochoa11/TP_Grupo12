package com.sgf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;

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
    
    // Turnos que está siendo llamado actualmente
    private Map<Integer, Turno> turnosActuales = new ConcurrentHashMap<>();

    private Turno ultimoLlamado = null;

    private LogicaFila() {

    }
    
    public static synchronized LogicaFila getInstance() {
		if (LogicaFila.instance == null)
			LogicaFila.instance = new LogicaFila();
		return instance;
	}

    /**
     * Registra un nuevo turno proveniente de la terminal, validando repeticion.
     * -> No es respo de la fila esta validacion -> Asume validado por la ventana
     */
    public synchronized void agregarTurno(Turno t) throws DNIRepetidoException {

       
            String dni = t.getDniCliente().trim();
            if (hasDni(dni)) throw new DNIRepetidoException(dni);
            else this.filaEspera.add(t);

         
        }

    /**
     * Lógica de llamado (utiliza Panel de Operador).
     * Lanza excepción si no hay nadie.
     */
    public synchronized Turno llamarSiguiente(int idPuesto) throws FilaVaciaException {
        if (filaEspera.isEmpty()) {
            throw new FilaVaciaException();
        }

        // Si había alguien del puesto en pantalla, pasa al historial antes de llamar al nuevo

        Turno anterior = turnosActuales.get(idPuesto);
        if (anterior != null) {
           actualizarHistorial(anterior);
        }
        

        //el nuevo turno sale de la cola 
        Turno nuevo = this.filaEspera.poll();
        nuevo.setIdPuesto(idPuesto);
        nuevo.setEstado("LLAMADO");
        nuevo.incrementarIntentos(); //son 3 intentos totales?
        this.ultimoLlamado = nuevo;

        turnosActuales.put(idPuesto, nuevo);

        return nuevo;
    }

    private void actualizarHistorial(Turno t) {
        this.historial.add(0, t);
        if (this.historial.size() > 4) {
            this.historial.remove(4); //mantiene el máximo de 4 en el historial
        }
    }

    public synchronized Turno  reIntentarLlamado(int idPuesto) {
       Turno t = this.turnosActuales.get(idPuesto);
       if (t != null) { //deberia haber una excepcion?
            if(t.getIntentos() < 3) {
                t.incrementarIntentos();
                this.ultimoLlamado = t;
                return t;
            } else {
                turnosActuales.remove(idPuesto);
                if (this.ultimoLlamado != null && this.ultimoLlamado.getIdPuesto() == idPuesto) {
                    this.ultimoLlamado = null;
                }
                return null;
            }
        }
        return null;
    }
    /**
     * @return el turno que debe mostrarse destacado en el monitor.
     */
    public synchronized Map<Integer, Turno> getTurnosActivos() {
        return new HashMap<>(this.turnosActuales);
    }

    public synchronized Turno getTurnoPuesto(int idPuesto) {
        return turnosActuales.get(idPuesto);
    }

    public synchronized Turno getUltimoLlamado() {
        return ultimoLlamado;
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
        for (Turno t : turnosActuales.values()) {
            if (t.getDniCliente().equals(dni)) {
                return true;
            }
        }
        return false;
    }
}