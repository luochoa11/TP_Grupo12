package com.sgf;

import com.sgf.infraestructura.*;

public class MainServicioDirectorio {
    
    public static void main(String[] args) {
        int puerto = Constantes.PUERTO_DIRECTORIO; 
        
        GestorRutas gestorRutas = new GestorRutas();
        
        ServidorDirectorio servidorDir = new ServidorDirectorio(puerto, gestorRutas);
        
        Thread hiloServidorDir = new Thread(servidorDir);
        hiloServidorDir.start();
        
        System.out.println("Directorio escuchando en el puerto " + puerto);
    }
}