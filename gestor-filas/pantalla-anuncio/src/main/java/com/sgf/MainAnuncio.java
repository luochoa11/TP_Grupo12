package com.sgf;

import javax.swing.SwingUtilities;

import com.sgf.ConfiguracionRed;
import com.sgf.infraestructura.ProxyAnuncio;
import com.sgf.presentacion.ControladorAnuncio;
import com.sgf.presentacion.VentanaAnuncio;
import com.sgf.seguridad.SeguridadAnuncio;

public class MainAnuncio {

    public static void main(String[] args) {

        String directorioIp     = ConfiguracionRed.get("directorio.ip");
        int    directorioPuerto = ConfiguracionRed.getInt("directorio.puerto");

        SwingUtilities.invokeLater(() -> {
        VentanaAnuncio ventana = new VentanaAnuncio();
        ControladorAnuncio controlador = new ControladorAnuncio(ventana);
        ventana.setVisible(true);

        SeguridadAnuncio componenteSeguridad = new SeguridadAnuncio();
        ProxyAnuncio cliente = new ProxyAnuncio(directorioIp, directorioPuerto, controlador, componenteSeguridad);

        new Thread(cliente).start();
        System.out.println("Anuncio: Sistema ensamblado y listo.");
    });
    }
}
