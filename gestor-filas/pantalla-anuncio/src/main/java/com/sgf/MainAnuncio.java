package com.sgf;

import javax.swing.SwingUtilities;

import com.sgf.infraestructura.ProxyAnuncio;
import com.sgf.presentacion.ControladorAnuncio;
import com.sgf.presentacion.VentanaAnuncio;

public class MainAnuncio {

    public static void main(String[] args) {

        String directorioIp     = ConfiguracionRed.get("directorio.ip");
        int    directorioPuerto = ConfiguracionRed.getInt("directorio.puerto");

        SwingUtilities.invokeLater(() -> {
            // 1: Iniciamos la ventana
            VentanaAnuncio ventana = new VentanaAnuncio();
            
            // 2: Creamos el Controlador y le damos la ventana
            ControladorAnuncio controlador = new ControladorAnuncio(ventana);
            
            ventana.setVisible(true);

            // 3: El Servidor habla con el Controlador
            ProxyAnuncio cliente = new ProxyAnuncio(directorioIp, directorioPuerto, controlador);
            new Thread(cliente).start();
            
            System.out.println("Anuncio: Sistema ensamblado y listo.");
        });
    }
}