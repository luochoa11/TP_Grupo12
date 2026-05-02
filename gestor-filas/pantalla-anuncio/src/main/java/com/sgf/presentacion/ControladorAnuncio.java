package com.sgf.presentacion;

import java.util.List;

import com.sgf.modelos.Turno;

/**
 * Controlador del Monitor.
 * Recibe los datos del ServidorMonitor y coordina la actualización de la interfaz.
 */
public class ControladorAnuncio {

    private VentanaMonitor vista;

    public ControladorAnuncio(VentanaMonitor vista) {
        this.vista = vista;
    }

    /**
     * Procesa el nuevo turno recibido
     */
    public void actualizarDesdeServidor(Turno turnoActual, List<Turno> historial) {
        vista.actualizarPantalla(turnoActual, historial);
        
    }
}