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
        String claveConfigurada = ConfiguracionRed.get("seguridad.clave");
        String algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
        
        if (claveConfigurada != null && !claveConfigurada.isEmpty()) {
            ProveedorEstrategiaCifrado proveedor;

            switch(algoritmo.toUpperCase().trim()) {
                case "AES":
                case "AES-128":
                    proveedor = new ProveedorAES();
                    break;
                case "DES":
                case "TRIPLEDES":
                    proveedor = new ProveedorDES();
                    break;
                case "XOR":
                case "BLOWFISH":
                    proveedor = new ProveedorXOR();
                    break;
                default:
                    System.err.println("[SeguridadOperador] Algoritmo desconocido en config.properties. Usando AES por defecto.");
                    proveedor = new ProveedorAES();
            }
            this.encriptador = proveedor.crear(claveConfigurada);
            System.out.println("[SeguridadOperador] Componente inicializado con clave local.");
        } else {
            this.encriptador = null;
            System.err.println("[SeguridadOperador] ADVERTENCIA: No se encontró clave local. El cliente arranca desprotegido.");
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

    // public synchronized void recargarConfiguracion() {
    //      if (!ConfiguracionRed.recargarSiCambio()) {
    //         return;
    //     }
    //     String clave = ConfiguracionRed.get("seguridad.clave");
    //     String algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
    //     this.encriptador = ProveedorEstrategiaCifrado.crear(  algoritmo,     clave  );

   
    // }
}