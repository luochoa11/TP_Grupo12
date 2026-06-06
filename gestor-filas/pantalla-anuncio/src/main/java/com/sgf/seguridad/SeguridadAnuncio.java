package com.sgf.seguridad;

import java.util.List;
import com.sgf.ConfiguracionRed;
import com.sgf.modelos.Turno;

public class SeguridadAnuncio {

    private IEncriptacionStrategy encriptador;

    public SeguridadAnuncio() {
        String claveConfigurada = ConfiguracionRed.get("seguridad.clave");
        
        if (claveConfigurada != null && !claveConfigurada.isEmpty()) {
            this.encriptador = new EstrategiaCifradoAES(claveConfigurada);
        } else {
            this.encriptador = null;
        }
    }

    public void desencriptarTurno(Turno t) {
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
}
