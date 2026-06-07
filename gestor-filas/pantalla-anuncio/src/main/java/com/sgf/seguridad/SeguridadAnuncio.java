package com.sgf.seguridad;

import java.util.List;
import com.sgf.ConfiguracionRed;
import com.sgf.modelos.Turno;

public class SeguridadAnuncio {

    private IEncriptacionStrategy encriptador;

    public SeguridadAnuncio() {
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
                    System.err.println("[SeguridadAnuncio] Algoritmo desconocido en config.properties. Usando AES por defecto.");
                    proveedor = new ProveedorAES();
            }
            this.encriptador = proveedor.crear(claveConfigurada);

        } else {
            this.encriptador = null;
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
