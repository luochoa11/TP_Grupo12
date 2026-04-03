package com.sgf;

import com.sgf.excepciones.FilaVaciaException;
import com.sgf.excepciones.SGFException;
import javax.swing.SwingUtilities;

public class ControladorOperador {

    private VentanaPanelOperador vista;
    private LogicaFila modelo;
    private ClienteSocket clienteMonitor;

    public ControladorOperador(VentanaPanelOperador vista, LogicaFila modelo) {
        this.vista = vista;
        this.modelo = modelo;
        
        this.clienteMonitor = new ClienteSocket("localhost", Constantes.PUERTO_MONITOR1);
    } 

    public void accionarLlamado() {
        try {
            // Intentamos obtener el siguiente (Puede lanzar FilaVaciaException)
            Turno siguiente = modelo.llamarSiguiente();

            // Si hay turno valido, enviamos al monitor 
            clienteMonitor.enviarTurno(siguiente);

            // actualizamos la vista 
            SwingUtilities.invokeLater(() -> {
            vista.actualizarVista(modelo.getTurnoActual(), modelo.getCola());
        });

        } catch (FilaVaciaException e) {
            vista.mostrarMensaje(e.getMessage());
        } catch (Exception e) {
            vista.mostrarMensaje("Error al procesar el llamado: " + e.getMessage());
        }
    }


    /**
     * Este metodo es llamado por el ServidorOperador cuando llega un DNI de la Terminal.
     */
    public void procesarTurnoDesdeRed(Turno nuevo) {
        try {
            // Agregamos a la lógica (valida duplicados y formato)
            modelo.agregarTurno(nuevo);
            
            // Actualizamos la vista
            SwingUtilities.invokeLater(() -> {
                vista.actualizarVista(modelo.getTurnoActual(), modelo.getCola());
            });
        } catch (Exception e) {
            System.err.println("Error al agregar turno desde red: " + e.getMessage());
        }
            
    }

}
