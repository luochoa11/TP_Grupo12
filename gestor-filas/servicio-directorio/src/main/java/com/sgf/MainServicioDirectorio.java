package com.sgf;

import com.sgf.infraestructura.*;
import com.sgf.seguridad.SeguridadDirectorio;

public class MainServicioDirectorio {
    public static void main(String args[]) {

        int puerto = ConfiguracionRed.getInt("directorio.puerto");
        SeguridadDirectorio seguridad = new SeguridadDirectorio();
        GestorRutas gestorRutas = new GestorRutas(seguridad);
        ServidorDirectorio servidorDir = new ServidorDirectorio(puerto, gestorRutas);
        new Thread(servidorDir).start();

        System.out.println("[Directorio] Escuchando en puerto " + puerto);
    }
}