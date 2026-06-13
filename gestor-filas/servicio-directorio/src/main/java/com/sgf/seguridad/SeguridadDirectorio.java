package com.sgf.seguridad;

import com.sgf.ConfiguracionRed;

public class SeguridadDirectorio {

    private String algoritmo;
    private String clave;

    public SeguridadDirectorio() {
        this.algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
        this.clave     = ConfiguracionRed.get("seguridad.clave");
        System.out.println("[SeguridadDirectorio] Config de seguridad cargada: algoritmo=" + algoritmo);
    }

    public String getAlgoritmo() { return algoritmo; }
    public String getClave()     { return clave; }
}
