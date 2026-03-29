package com.sgf;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del Monitor.
 * Recibe los datos del ServidorMonitor y coordina la actualización de la interfaz.
 */
public class ControladorMonitor {

    private VentanaMonitorVisualizacion vista;
    private List<Turno> historial;
    private Turno turnoActual;

    public ControladorMonitor(VentanaMonitorVisualizacion vista) {
        this.vista = vista;
        this.historial = new ArrayList<>();
        this.turnoActual = null;
    }

    /**
     * Procesa el nuevo turno recibido
     */
    public void recibirNuevoTurno(Turno nuevoTurno) {
        if (nuevoTurno == null) return;

        // Gestiona el historial
        if (turnoActual != null) {
            historial.add(0, turnoActual);
            if (historial.size() > 4) {
                historial.remove(4); 
            }
        }

        this.turnoActual = nuevoTurno;

        vista.actualizarPantalla(turnoActual, historial);
    }
}