package com.sgf.aplicacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.excepciones.FilaVaciaException;
import com.sgf.modelos.Turno;

/**
 * Implementacion de la lógica de la fila.
 * Es un singleton para garantizar que toda la aplicacion use la misma instancia.
 * Cumple con el requerimiento funcional de lógica FIFO.
 */
public class LogicaFila implements ILogicaFila{
    private static LogicaFila instance = null;
    
    // Cola de espera para los turnos que no fueron llamados aun
    private Queue<Turno> filaEspera =  new LinkedList<>();
    
    // Lista para el historial de los últimos 4 llamados
    private List<Turno> historial = new ArrayList<>();
    
    // Turnos que está siendo llamado actualmente
    private Map<Integer, Turno> turnosActuales = new ConcurrentHashMap<>();

    private Turno ultimoLlamado = null;

    private LogicaFila() {}
    
    public static synchronized LogicaFila getInstance() {
		if (LogicaFila.instance == null)
			LogicaFila.instance = new LogicaFila();
		return instance;
	}

    /**
     * --- Implementacion de IServicioRegistro ---
     * -> No es respo de la fila esta validacion -> Asume validado por la ventana
     */
    @Override
    public synchronized void agregarTurno(Turno t) throws DNIRepetidoException {

        String dni = t.getDniCliente().trim();
        if (hasDni(dni)) throw new DNIRepetidoException(dni);
        else this.filaEspera.add(t);

    }

    /**
     * --- Implementacion de IServicioOperador ---
     *   Lanza excepción si no hay nadie.
     */
    @Override
    public synchronized Turno llamarSiguiente(int idPuesto) throws FilaVaciaException {
        if (filaEspera.isEmpty()) {
            throw new FilaVaciaException();
        }

        // Si el monitor principal estaba ocupado, movemos ese turno al historial
        if (this.ultimoLlamado != null) {
            actualizarHistorial(this.ultimoLlamado);
        }

        //el nuevo turno sale de la cola 
        Turno nuevo = this.filaEspera.poll();
        nuevo.setIdPuesto(idPuesto);
        nuevo.setEstado("LLAMADO");
        nuevo.incrementarIntentos(); //Primer intento

        this.ultimoLlamado = nuevo;
        turnosActuales.put(idPuesto, nuevo);
        return nuevo;
    }

/**
     * Gestiona el reintento de llamado. 
     * Si el reintento desplaza visualmente a otro turno diferente, el anterior va al historial.
     */
    @Override
    public synchronized Turno reintentarLlamado(int idPuesto) {
        Turno t = this.turnosActuales.get(idPuesto);

        if (t != null) { //deberia haber una excepcion?
            if(t.getIntentos() < 3) {
                t.incrementarIntentos();

                //Si el reintento es de un turno no mostrado en pantalla, se actualiza el historial
                //antes desaparecía del monitor
                if (this.ultimoLlamado != null && !this.ultimoLlamado.getDniCliente().equals(t.getDniCliente())) {
                    actualizarHistorial(this.ultimoLlamado);
                }

                // Si el turno que estamos re-llamando estaba en el historial, lo sacamos de ahí
                quitarDelHistorial(t.getDniCliente());

                this.ultimoLlamado = t;
                return t;
            } else {
                // Si falla el 3er intento, el puesto queda libre y el turno va al historial
                actualizarHistorial(t);
                turnosActuales.remove(idPuesto);
                if (this.ultimoLlamado != null && this.ultimoLlamado.getDniCliente().equals(t.getDniCliente())) {
                    this.ultimoLlamado = null;
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public List<Turno> getCola() {
        return new ArrayList<>(filaEspera);
    }

    @Override
    public synchronized Turno getTurnoPuesto(int idPuesto) {
        return turnosActuales.get(idPuesto);
    }

    // --- Implementacion de IServicioMonitor ---
    @Override
    public synchronized Turno getUltimoLlamado() {
        return ultimoLlamado;
    }

    @Override
    public List<Turno> getHistorial() {
        return new ArrayList<>(historial);
    }

    /** --- FUNCIONES AUXILIARES ---
     * Mueve un turno al historial de forma inteligente.
     * Mantiene las últimas 4 posiciones y evita duplicados visuales.
     */

    private void actualizarHistorial(Turno t) {
        // Si el DNI ya existe en el historial, no se hace nada (mantiene su posición vieja)
        for (Turno h : historial) {
            if (h.getDniCliente().equals(t.getDniCliente())) {
                return;
            }
        }

        this.historial.add(0, t);//Si no estaba, lo agregamos al inicio

        if (this.historial.size() > 4) {
            this.historial.remove(4); //mantiene el máximo de 4 en el historial
        }
    }

    /**
     * Elimina un DNI del historial cuando vuelve a ser el llamado principal del monitor.
     */
    private void quitarDelHistorial(String dni) {
        Iterator<Turno> it = historial.iterator();
        while (it.hasNext()) {
            if (it.next().getDniCliente().equals(dni)) {
                it.remove();
            }
        }
    }
    
    /**
     * @return el turno que debe mostrarse destacado en el monitor.
     */
    @Override
    public synchronized Map<Integer, Turno> getTurnosActivos() {
        return new HashMap<>(this.turnosActuales);
    }

    public int getCantidadEnEspera() {
        return filaEspera.size();
    }

    @Override
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