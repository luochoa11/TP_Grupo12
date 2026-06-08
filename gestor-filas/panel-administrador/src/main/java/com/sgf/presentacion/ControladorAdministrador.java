package com.sgf.presentacion;

import javax.swing.SwingUtilities;

import com.sgf.interfaces.IServicioAdministrador;

/**
 * Controlador de la interfaz de administración.
 */
public class ControladorAdministrador {

    private final VentanaAdministrador vista;
    private final IServicioAdministrador servicio;

    public ControladorAdministrador(VentanaAdministrador vista, IServicioAdministrador servicio) {
        this.vista = vista;
        this.servicio = servicio;
    }

    /**
     * Ordena cambiar el formato de persistencia y refresca la UI inmediatamente tras finalizar.
     */
    public void modificarPersistencia(String nuevoFormato) {
        try {
            boolean exito = servicio.cambiarFormatoPersistencia(nuevoFormato);
            if (exito) {
                vista.mostrarMensaje("Motor de almacenamiento cambiado con éxito a: " + nuevoFormato);
                actualizarEstadoGeneral();
            } else {
                vista.mostrarMensaje("Error: El servidor no pudo realizar la migración al formato " + nuevoFormato);
            }
        } catch (Exception e) {
            vista.mostrarMensaje("Error de comunicación con el servidor central: " + e.getMessage());
        }
    }

    /**
     * Ordena cambiar el protocolo criptográfico en caliente.
     * Al aplicarse de forma exitosa, emite un aviso crítico al Administrador indicando que
     * se requiere un reinicio sistémico en frío para evitar la corrupción o pérdida de DNIs.
     */
    public void modificarSeguridad(String algoritmo, String clave) {
        if (clave == null || clave.trim().isEmpty()) {
            vista.mostrarMensaje("Error: La clave secreta de seguridad no puede estar vacía.");
            return;
        }
        try {
            boolean exito = servicio.actualizarConfiguracionSeguridad(algoritmo, clave);
            if (exito) {
                vista.mostrarMensaje("Configuración de seguridad guardada con éxito en el Servidor:\n"
                        + "- Estrategia: " + algoritmo + "\n"
                        + "- Nueva clave secreta: " + clave);
                
                vista.mostrarAdvertencia(
                    "¡ATENCIÓN: REINICIO DEL SISTEMA REQUERIDO!\n\n"
                    + "Para que la nueva política de encriptación simétrica se aplique de forma consistente\n"
                    + "y sin pérdidas de DNIs en caliente, debe REINICIAR los siguientes componentes:\n"
                    + "  1. Servidores Centrales (Primario y Secundario)\n"
                    + "  2. Terminales de Registro activas\n"
                    + "  3. Puestos de Operadores conectados\n\n"
                    + "El sistema continuará operando bajo las credenciales anteriores hasta su próximo arranque."
                );
                
                // Fuerza un refresco de sincronización INMEDIATAMENTE después de la mutación exitosa
                actualizarEstadoGeneral();
            } else {
                vista.mostrarMensaje("Error: El servidor central rechazó la configuración de seguridad.");
            }
        } catch (Exception e) {
            vista.mostrarMensaje("Error de red al aplicar la política de seguridad: " + e.getMessage());
        }
    }

    /**
     * Realiza la consulta de configuración al Servidor Central de forma síncrona.
     */
    public void actualizarEstadoGeneral() {
        try {
            // Trae formato, algoritmo y clave en un solo viaje de red
            String[] config = servicio.obtenerConfiguracionCompleta();
            
            String formatoActivo = config[0];
            String algoritmoActivo = config[1];
            String claveActiva = config[2];

            // Sincronizamos en el hilo visual de Swing
            SwingUtilities.invokeLater(() -> {
                vista.actualizarMonitoreo(
                    formatoActivo, 
                    algoritmoActivo, 
                    claveActiva
                );
            });

        } catch (Exception e) {
            System.err.println("[CONTROLADOR-ADMINISTRADOR] Error al sincronizar configuración con el servidor: " + e.getMessage());
        }
    }
}