package com.sgf;

import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.excepciones.FilaVaciaException;
import javax.swing.SwingUtilities;

public class ControladorOperador {

    private VentanaPanelOperador vista;
    private LogicaFila modelo;
    private ClienteSocket clienteMonitor;

    public ControladorOperador(VentanaPanelOperador vista, LogicaFila modelo) {
        this.vista = vista;
        this.modelo = modelo;
        
        this.clienteMonitor = new ClienteSocket(Constantes.HOST_MONITOR1, Constantes.PUERTO_MONITOR1);
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
     * Este metodo es llamado por el ServidorOperador cuando llega un DNI de la Terminal. // MOVERLO DESPUES
     */
    public void procesarTurnoDesdeRed(Turno nuevo) throws DNIRepetidoException {
        try {
            modelo.agregarTurno(nuevo); // se valida duplicado

            SwingUtilities.invokeLater(() -> {
                vista.actualizarVista(modelo.getTurnoActual(), modelo.getCola());
            });

        } catch (DNIRepetidoException e) {
            throw e; // la tiro denuevo 
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        }
    }

}
