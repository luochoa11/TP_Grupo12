package com.sgf.seguridad;

import com.sgf.ConfiguracionRed;

/**
 * Componente dedicado a manejar la seguridad de la Terminal de Registro.
 * Lee la clave desde la configuración estática al inicializarse.
 */
public class SeguridadRegistro {

    private IEncriptacionStrategy encriptador;

    public SeguridadRegistro() {
        String claveConfigurada = ConfiguracionRed.get("seguridad.clave");
        
        if (claveConfigurada != null && !claveConfigurada.isEmpty()) {
            this.encriptador = new EstrategiaCifradoAES(claveConfigurada);
            System.out.println("[Seguridad] Componente inicializado con clave local.");
        } else {
            this.encriptador = null;
            System.err.println("[Seguridad] ADVERTENCIA: No se encontró clave local. El cliente arranca desprotegido.");
        }
    }

    /**
     * Encripta un DNI si hay una estrategia de seguridad configurada.
     * Por ahora usa AES.
     */
    public String encriptarDNI(String dniOriginal) {
        if (this.encriptador != null && dniOriginal != null) {
            return this.encriptador.encriptar(dniOriginal);
        }
        return dniOriginal;
    }

    public boolean estaConfigurado() {
        return this.encriptador != null;
    }
}