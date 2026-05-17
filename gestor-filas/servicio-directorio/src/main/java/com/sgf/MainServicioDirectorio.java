package com.sgf;

import com.sgf.infraestructura.*;

public class MainServicioDirectorio {
    public static void main(String args[]) {

        int puerto = ConfiguracionRed.getInt("directorio.puerto");
        GestorRutas gestorRutas = new GestorRutas();
        ServidorDirectorio servidorDir = new ServidorDirectorio(puerto, gestorRutas);
        new Thread(servidorDir).start();

        System.out.println("[Directorio] Escuchando en puerto " + puerto);
    }
}