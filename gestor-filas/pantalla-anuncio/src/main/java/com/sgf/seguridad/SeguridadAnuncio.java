package com.sgf.seguridad;

import java.util.List;
import com.sgf.ConfiguracionRed;
import com.sgf.modelos.Turno;

public class SeguridadAnuncio {

    private IEncriptacionStrategy encriptador;

    public SeguridadAnuncio(String algoritmo, String clave) {
        if (clave != null && !clave.isEmpty()) {
            ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
            this.encriptador = proveedor.crear(clave);
            System.out.println("[SeguridadAnuncio] Componente inicializado con config del directorio.");
        } else {
            this.encriptador = null;
            System.err.println("[SeguridadAnuncio] ADVERTENCIA: Sin clave. Cliente arranca desprotegido.");
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
    //  public synchronized void recargarConfiguracion() {
    //     if (!ConfiguracionRed.recargarSiCambio()) {
    //         return;
    //     }
        
    //     String clave = ConfiguracionRed.get("seguridad.clave");
    //     String algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
    //     this.encriptador = ProveedorEstrategiaCifrado.crear(  algoritmo,     clave  );

  
    // }
}
