package com.sgf;

import com.sgf.infraestructura.*;

public class MainServicioDirectorio {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Uso: MainServicioDirectorio <puerto>");
            System.exit(1);
        }

        int puerto = Integer.parseInt(args[0]);

        GestorRutas gestorRutas = new GestorRutas();
        ServidorDirectorio servidorDir = new ServidorDirectorio(puerto, gestorRutas);
        new Thread(servidorDir).start();

        System.out.println("[Directorio] Escuchando en puerto " + puerto);
    }
}