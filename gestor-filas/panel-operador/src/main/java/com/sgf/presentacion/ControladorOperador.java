package com.sgf.presentacion;

import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import com.sgf.interfaces.IServicioOperador;
import com.sgf.modelos.Turno;

/**
 * Controlador de la GUI del Panel del Operador.
 */
public class ControladorOperador {

    private VentanaOperador vista;
    private IServicioOperador servicio;
    private int idPuesto;

    public ControladorOperador(VentanaOperador vista, IServicioOperador servicio, int idPuesto) {
        this.vista = vista;
        this.servicio = servicio;
        this.idPuesto = idPuesto;
    } 

    public Turno accionarLlamado() {
        try {
            Turno siguiente = servicio.llamarSiguiente(idPuesto);
            List<Turno> cola = servicio.getCola();
            final List<Turno> colaFinal = (cola != null) ? cola : Collections.emptyList();

            SwingUtilities.invokeLater(() -> {
                vista.actualizarVista(siguiente, colaFinal); 
            });

            return siguiente;
        } catch (Exception e) {
            vista.mostrarMensaje("Error al procesar el llamado: " + e.getMessage());
            return null;
        }
    }

    public void accionarReintento() {
        try {
            Turno reIntento = servicio.reintentarLlamado(idPuesto);
            List<Turno> cola = servicio.getCola();
            final List<Turno> colaFinal = (cola != null) ? cola : Collections.emptyList();
            
            SwingUtilities.invokeLater(() -> {
                vista.actualizarVista(reIntento, colaFinal);
            });
        } catch (Exception e) {
            vista.mostrarMensaje("Error al procesar el reintento: " + e.getMessage());
        }
    }

    public void finalizarAtencion() {
        try {
            servicio.finalizarAtencion(idPuesto);
            actualizarCola();
        } catch (Exception e) {
            vista.mostrarMensaje("Error al finalizar la atención: " + e.getMessage());
        }
    }

    public void actualizarCola() {
        try {
            List<Turno> cola = servicio.getCola();
            final List<Turno> colaFinal = (cola != null) ? cola : Collections.emptyList();
            Turno actual = servicio.getTurnoPuesto(idPuesto);

            SwingUtilities.invokeLater(() -> {
                vista.actualizarVista(actual, colaFinal);
            });
        } catch (Exception e) {
            System.err.println("Error actualizando cola: " + e.getMessage());
        }
    }
}