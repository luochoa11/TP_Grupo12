package com.sgf.presentacion;

import java.util.List;

import javax.swing.SwingUtilities;

import com.sgf.infraestructura.ClienteOperador;
import com.sgf.modelos.Turno;

public class ControladorOperador {

    private VentanaPanelOperador vista;
    private ClienteOperador cliente;
    private int idPuesto;


    public ControladorOperador(VentanaPanelOperador vista, ClienteOperador cliente,int idPuesto) {
        this.vista = vista;
        this.cliente = cliente;
        this.idPuesto = idPuesto;
    } 

    public void accionarLlamado() {
        try {
            // Intentamos obtener el siguiente (Puede lanzar FilaVaciaException)
            Turno siguiente = cliente.llamarSiguiente(idPuesto);


            // actualizamos la vista 
            SwingUtilities.invokeLater(() -> {
            vista.actualizarVista(siguiente, cliente.getCola()); 
        });

    
        } catch (Exception e) {
            vista.mostrarMensaje("Error al procesar el llamado: " + e.getMessage());
        }
    }


    public void accionarReintento() {
        try {
            Turno reIntento = cliente.reintentarLlamado(idPuesto);
            SwingUtilities.invokeLater(() -> {
                vista.actualizarVista(reIntento, cliente.getCola());
            });
        } catch (Exception e) {
            vista.mostrarMensaje("Error al procesar el reintento: " + e.getMessage());
        }
    }

    public void actualizarCola() {
    try {
        List<Turno> cola = cliente.getCola();
        Turno actual = cliente.getTurnoPuesto(idPuesto); // opcional

        SwingUtilities.invokeLater(() -> {
            vista.actualizarVista(actual, cola);
        });

    } catch (Exception e) {
        System.err.println("Error actualizando cola: " + e.getMessage());
    }
}

}
