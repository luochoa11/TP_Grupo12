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
    private final int idTerminal;

    public SeguridadOperador(int idTerminal) {
       this.idTerminal=idTerminal;

       String algoritmo = ConfiguracionRed.getPropLocal("operador", idTerminal, "seguridad.algoritmo");
       String clave = ConfiguracionRed.getPropLocal("operador", idTerminal, "seguridad.clave");

       if (algoritmo != null && clave != null && !clave.isEmpty()) {
            inicializarEstrategia(algoritmo, clave);
            System.out.println("[SeguridadOperador] Inicializado desde archivo local (" + algoritmo + ") para ID " + idTerminal);
        } else {
            this.encriptador = null;
            System.out.println("[SeguridadOperador] Sin configuración local previa para ID " + idTerminal + ". Esperando sincronización.");
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
        ConfiguracionRed.guardarConfigLocal("operador", this.idTerminal, algoritmo, clave);
        inicializarEstrategia(algoritmo, clave);
    }

    private void inicializarEstrategia(String algoritmo, String clave){
        if (clave != null && !clave.isEmpty()) {
            ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
            this.encriptador = proveedor.crear(clave);
            System.out.println("[SeguridadOperador] Estrategia de cifrado actualizada en RAM.");
        } else {
            this.encriptador = null;
            System.err.println("[SeguridadOperador] ADVERTENCIA: Clave vacía. Operador sin protección.");
        }
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