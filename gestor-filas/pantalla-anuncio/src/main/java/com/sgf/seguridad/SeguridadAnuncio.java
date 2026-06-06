package com.sgf.seguridad;

import java.util.List;
import com.sgf.ConfiguracionRed;
import com.sgf.modelos.Turno;

/**
 * Componente dedicado a manejar la seguridad de la Pantalla de Anuncios.
 * Desencripta los datos en tiempo real; si falla, lanza excepciones para
 * forzar el reinicio de la conexión persistente.
 */
public class SeguridadAnuncio {

    private IEncriptacionStrategy encriptador;

    public SeguridadAnuncio() {
        String claveConfigurada = ConfiguracionRed.get("seguridad.clave");
        
        if (claveConfigurada != null && !claveConfigurada.isEmpty()) {
            this.encriptador = new EstrategiaCifradoAES(claveConfigurada);
            System.out.println("[SeguridadAnuncio] Componente inicializado con clave local.");
        } else {
            this.encriptador = null;
            System.err.println("[SeguridadAnuncio] ADVERTENCIA: No se encontró clave local. El cliente arranca desprotegido.");
        }
    }

    /**
     * Desencripta un turno. Lanza Exception si falla (ej. clave incorrecta)
     * para que el Proxy corte la conexión.
     */
    public void desencriptarTurno(Turno t) throws Exception {
        if (t != null && t.getDniCliente() != null && this.encriptador != null) {
            t.setDniCliente(this.encriptador.desencriptar(t.getDniCliente()));
        }
    }

    /**
     * Desencripta una lista completa.
     */
    public void desencriptarLista(List<Turno> lista) throws Exception {
        if (lista != null) {
            for (Turno t : lista) {
                desencriptarTurno(t);
            }
        }
    }
}
