package com.sgf.seguridad;

import java.util.List;

import com.sgf.ConfiguracionRed;
import com.sgf.modelos.Turno;

public class SeguridadAnuncio {

    private IEncriptacionStrategy encriptador;
    private final int idTerminal;


    public SeguridadAnuncio(int idTerminal) {
        this.idTerminal = idTerminal;
        
        String algoritmo = ConfiguracionRed.getPropLocal("anuncio", idTerminal, "seguridad.algoritmo");
        String clave     = ConfiguracionRed.getPropLocal("anuncio", idTerminal, "seguridad.clave");

        if (algoritmo != null && clave != null) {
            inicializarEstrategia(algoritmo,clave);
            System.out.println("[SeguridadAnuncio] Encriptador inicializado con algoritmo: " + algoritmo);
        } else {
            this.encriptador = null;
            System.err.println("[SeguridadAnuncio] Configuración de seguridad incompleta. Cliente sin protección.");
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
        ConfiguracionRed.guardarConfigLocal("anuncio", idTerminal, algoritmo, clave);

        this.inicializarEstrategia(algoritmo, clave);
        System.out.println("[SeguridadAnuncio] Configuración actualizada. Nuevo algoritmo: "+algoritmo);
    }

    private void inicializarEstrategia(String algorimo, String clave){
        if(clave!=null && !clave.isEmpty()){
            ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algorimo);
            this.encriptador= proveedor.crear(clave);
            System.out.println("[SeguridadAnuncio] Configuración de seguridad actualizada en RAM.");
        } else {
            this.encriptador=null;
            System.out.println("[SeguridadAnuncio] ADVERTENCIA: Clave vacía. Cliente ahora sin protección.");

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
