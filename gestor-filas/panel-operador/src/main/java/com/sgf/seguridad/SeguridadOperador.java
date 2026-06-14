package com.sgf.seguridad;

import java.util.List;
import com.sgf.ConfiguracionRed;
import com.sgf.modelos.Turno;

/**
 * Componente dedicado a manejar la seguridad del Panel de Operador.
 * Se encarga de desencriptar los datos que llegan desde el Servidor Central.
 */
public class SeguridadOperador {

    private IEncriptacionStrategy encriptador;

    public SeguridadOperador() {
        String algoritmo = "AES"; // Algoritmo por defecto
        String clave = ""; // Clave vacía por defecto

        try{
            algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
            clave = ConfiguracionRed.get("seguridad.clave");

            if (clave != null && !clave.isEmpty()) {
                ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
                this.encriptador = proveedor.crear(clave);
                System.out.println("[SeguridadOperador] Inicializado con el config.properties local.");
            } else {
                this.encriptador = null;
                System.err.println("[SeguridadOperador] Sin clave en config.properties local. Usando valores x defecto.");
            }
        } catch (IllegalArgumentException e) {
            this.encriptador = null;
            System.err.println("[SeguridadOperador] No se encontraron llaves locales. Usando valores x defecto.");
        }
    }

    /**
     * Desencripta el DNI de un Turno individual en el mismo objeto.
     */
    public void desencriptarTurno(Turno t) {
        // this.recargarConfiguracion();
        if (t != null && t.getDniCliente() != null && this.encriptador != null) {
            try {
                t.setDniCliente(this.encriptador.desencriptar(t.getDniCliente()));
            } catch (Exception e) {
                System.err.println("[SeguridadOperador] Error al desencriptar DNI con clave incorrecta: " + e.getMessage());
            }
        }
    }

    /**
     * Recorre una lista de Turnos y desencripta todos sus DNIs.
     */
    public void desencriptarLista(List<Turno> lista) {
        if (lista != null) {
            for (Turno t : lista) {
                desencriptarTurno(t);
            }
        }
    }
    
    public boolean estaConfigurado() {
        return this.encriptador != null;
    }

    public void actualizarConfiguracion(String algoritmo, String clave) {
        if (clave != null && !clave.isEmpty()) {
            ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
            this.encriptador = proveedor.crear(clave);
            System.out.println("[SeguridadOperador] Configuración de seguridad actualizada.");
        } else {
            this.encriptador = null;
            System.err.println("[SeguridadOperador] ADVERTENCIA: Clave vacía");
        }

        this.modificarArchivoLocal(algoritmo, clave);
    }

    public void modificarArchivoLocal(String algoritmo, String clave) {
       String[] rutas={
         ".panel-operador/src/main/resources/config.properties",
            ".panel-operador/target/classes/config.properties"
       };
         ConfiguracionRed.guardarConfigLocal(algoritmo, clave, rutas);
    }


    // public synchronized void recargarConfiguracion() {
    //      if (!ConfiguracionRed.recargarSiCambio()) {
    //         return;
    //     }
    //     String clave = ConfiguracionRed.get("seguridad.clave");
    //     String algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
    //     this.encriptador = ProveedorEstrategiaCifrado.crear(  algoritmo,     clave  );

   
    // }
}