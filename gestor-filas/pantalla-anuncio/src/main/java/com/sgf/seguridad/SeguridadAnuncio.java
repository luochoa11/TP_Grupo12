package com.sgf.seguridad;

import java.util.List;
import com.sgf.ConfiguracionRed;
import com.sgf.modelos.Turno;

public class SeguridadAnuncio {

    private IEncriptacionStrategy encriptador;

    public SeguridadAnuncio() {
        String algoritmo = "AES"; // Algoritmo por defecto
        String clave = ""; // Clave vacía por defecto
        try {
            algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
            clave = ConfiguracionRed.get("seguridad.clave");

            if (clave != null && !clave.isEmpty()) {
                ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
                this.encriptador = proveedor.crear(clave);
                System.out.println("[SeguridadAnuncio] Componente inicializado con config local.");
            } else {
                this.encriptador = null;
                System.err.println("[SeguridadAnuncio] Sin clave en config.properties local. Usando valores x defecto.");
            }
        } catch (IllegalArgumentException e) {
            this.encriptador = null;
            System.err.println("[SeguridadAnuncio] No se encontraron llaves locales en el primer arranque. Usando valores x defecto.");
        }
    }

    public void desencriptarTurno(Turno t) {
        // this.recargarConfiguracion();
        if (t != null && t.getDniCliente() != null && this.encriptador != null) {
            try {
                if (t.getDniCliente().length() > 10) {
                    t.setDniCliente(this.encriptador.desencriptar(t.getDniCliente()));
                }
            } catch (Exception e) {
                // Falla silenciosa para que la pantalla nunca se cuelgue
            }
        }
    }

    public void desencriptarLista(List<Turno> lista) {
    
        if (lista != null) {
            for (Turno t : lista) desencriptarTurno(t);
        }
    }

    public void actualizarConfiguracion(String algoritmo, String clave) {
        if (clave != null && !clave.isEmpty()) {
            ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
            this.encriptador = proveedor.crear(clave);
            System.out.println("[SeguridadAnuncio] Configuración de seguridad actualizada.");
        } else {
            this.encriptador = null;
            System.err.println("[SeguridadAnuncio] ADVERTENCIA: Clave vacía. Cliente ahora sin protección.");
        }
        modificarArchivoLocal(algoritmo, clave);
    }

    private void modificarArchivoLocal(String algoritmo, String clave) {
         String[] rutas = {
            ".pantalla-anuncio/src/main/resources/config.properties",
            ".pantalla-anuncio/target/classes/config.properties"
        };
        ConfiguracionRed.guardarConfigLocal(algoritmo, clave, rutas);
    }
    //  public synchronized void recargarConfiguracion() {
    //     if (!ConfiguracionRed.recargarSiCambio()) {
    //         return;
    //     }
        
    //     String clave = ConfiguracionRed.get("seguridad.clave");
    //     String algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
    //     this.encriptador = ProveedorEstrategiaCifrado.crear(  algoritmo,     clave  );

  
    // }
}
