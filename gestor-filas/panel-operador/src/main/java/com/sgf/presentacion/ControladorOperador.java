package com.sgf.presentacion;

import java.util.List;

import javax.swing.SwingUtilities;

import com.sgf.interfaces.IServicioOperador;
import com.sgf.modelos.Turno;

public class ControladorOperador {

    private VentanaPanelOperador vista;
    private IServicioOperador servicio;
    private int idPuesto;


    public ControladorOperador(VentanaPanelOperador vista, IServicioOperador servicio,int idPuesto) {
        this.vista = vista;
        this.servicio = servicio;
        this.idPuesto = idPuesto;
    } 

    public void accionarLlamado() {
        try {
            // Intentamos obtener el siguiente (Puede lanzar FilaVaciaException)
            Turno siguiente = servicio.llamarSiguiente(idPuesto);


            // actualizamos la vista 
            SwingUtilities.invokeLater(() -> {
            vista.actualizarVista(siguiente, servicio.getCola()); 
        });

    
        } catch (Exception e) {
            vista.mostrarMensaje("Error al procesar el llamado: " + e.getMessage());
        }
    }


    public void accionarReintento() {
        try {
            Turno reIntento = servicio.reintentarLlamado(idPuesto);
            SwingUtilities.invokeLater(() -> {
                vista.actualizarVista(reIntento, servicio.getCola());
            });
        } catch (Exception e) {
            vista.mostrarMensaje("Error al procesar el reintento: " + e.getMessage());
        }
    }

    public void actualizarCola() {
    try {
        List<Turno> cola = servicio.getCola();
        Turno actual = servicio.getTurnoPuesto(idPuesto); // opcional

        SwingUtilities.invokeLater(() -> {
            vista.actualizarVista(actual, cola);
        });

    } catch (Exception e) {
        System.err.println("Error actualizando cola: " + e.getMessage());
    }
}

}
