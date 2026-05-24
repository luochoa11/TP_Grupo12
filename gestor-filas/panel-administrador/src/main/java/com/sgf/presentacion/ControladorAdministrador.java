package com.sgf.presentacion;

import javax.swing.SwingUtilities;

import com.sgf.interfaces.IServicioAdministrador;

/**
 * Controlador para la interfaz de administración.
 */
public class ControladorAdministrador {

    private final VentanaAdministrador vista;
    private final IServicioAdministrador servicio;

    public ControladorAdministrador(VentanaAdministrador vista, IServicioAdministrador servicio) {
        this.vista = vista;
        this.servicio = servicio;
    }

    /**
     * Cambia de forma activa el formato de persistencia del servidor.
     * Tras realizar el cambio de persistencia en caliente, fuerza un refresco visual
     * inmediato para actualizar las etiquetas de estado y resetear el botón "Aplicar".
     * @param nuevoFormato Abreviatura del formato ("JSON", "XML", "TXT")
     */
    public void modificarPersistencia(String nuevoFormato) {
        try {
            boolean exito = servicio.cambiarFormatoPersistencia(nuevoFormato);
            if (exito) {
                vista.mostrarMensaje("Forma de almacenamiento cambiado con éxito a: " + nuevoFormato + ".");
                actualizarEstadoGeneral();
            } else {
                vista.mostrarMensaje("Error: El servidor no pudo realizar la migración al formato " + nuevoFormato);
            }
        } catch (Exception e) {
            vista.mostrarMensaje("Error de comunicación con el servidor central: " + e.getMessage());
        }
    }

    /**
     * Cambia la estrategia de encriptación y la clave simétrica del servidor en caliente.
     * Una vez guardada, fuerza un refresco visual inmediato para restablecer el estado del botón.
     * @param algoritmo Nombre de la estrategia criptográfica ("AES-128", "Blowfish", "TripleDES")
     * @param clave Clave secreta compartida editada por el administrador.
     */
    public void modificarSeguridad(String algoritmo, String clave) {
        if (clave == null || clave.trim().isEmpty()) {
            vista.mostrarMensaje("Error: La clave secreta de seguridad no puede estar vacía.");
            return;
        }
        try {
            boolean exito = servicio.actualizarConfiguracionSeguridad(algoritmo, clave);
            if (exito) {
                vista.mostrarMensaje("Política de seguridad actualizada con éxito:\n"
                        + "- Estrategia: " + algoritmo + "\n"
                        + "- Clave secreta: " + clave);
                actualizarEstadoGeneral();
            } else {
                vista.mostrarMensaje("Error: El servidor central rechazó la configuración criptográfica.");
            }
        } catch (Exception e) {
            vista.mostrarMensaje("Error de red al aplicar la política de seguridad: " + e.getMessage());
        }
    }

    public void actualizarEstadoGeneral() {
        try {
            String[] config = servicio.obtenerConfiguracionCompleta();
            String formatoActivo = config[0];
            String algoritmoActivo = config[1];
            String claveActiva = config[2];

            SwingUtilities.invokeLater(() -> {
                vista.actualizarMonitoreo(
                    formatoActivo, 
                    algoritmoActivo, 
                    claveActiva
                );
            });

        } catch (Exception e) {
            System.err.println("[ControladorAdministrador] Error en refresco de configuración: " + e.getMessage());
        }
    }
}