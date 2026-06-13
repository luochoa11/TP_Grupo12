package com.sgf.seguridad;

import com.sgf.ConfiguracionRed;

/**
 * Componente dedicado a manejar la seguridad de la Terminal de Registro.
 * Lee la clave desde la configuración estática al inicializarse.
 */
public class SeguridadRegistro {

    private IEncriptacionStrategy encriptador;

    public SeguridadRegistro(String algoritmo, String clave) {
        if (clave != null && !clave.isEmpty()) {
            ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
            this.encriptador = proveedor.crear(clave);
            System.out.println("[SeguridadRegistro] Componente inicializado con config del directorio.");
        } else {
            this.encriptador = null;
            System.err.println("[SeguridadRegistro] ADVERTENCIA: Sin clave. Cliente arranca desprotegido.");
        }
    }

    /**
     * Encripta un DNI si hay una estrategia de seguridad configurada.
     */
    public String encriptarDNI(String dniOriginal) {
        // this.recargarConfiguracion();
        if (this.encriptador != null && dniOriginal != null) {
            return this.encriptador.encriptar(dniOriginal);
        }
        return dniOriginal;
    }

    public boolean estaConfigurado() {
        return this.encriptador != null;
    }

    //  public synchronized void recargarConfiguracion() {
    //         if (!ConfiguracionRed.recargarSiCambio()) {
    //         return;
    //     }
    //         String clave = ConfiguracionRed.get("seguridad.clave");
    //         String algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
    //         this.encriptador = ProveedorEstrategiaCifrado.crear(  algoritmo,     clave  );
    //  }

}