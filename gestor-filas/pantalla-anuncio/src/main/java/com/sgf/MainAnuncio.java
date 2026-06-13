package com.sgf;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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

        String algoritmo = "AES";
        String clave = "";
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_CONFIG_SEGURIDAD");
            out.flush();
            algoritmo = (String) in.readObject();
            clave     = (String) in.readObject();
            System.out.println("[Anuncio] Config de seguridad recibida: " + algoritmo);
        } catch (Exception e) {
            System.err.println("[Anuncio] No se pudo obtener config de seguridad: " + e.getMessage());
        }

        SeguridadAnuncio componenteSeguridad = new SeguridadAnuncio(algoritmo, clave);
        ProxyAnuncio cliente = new ProxyAnuncio(directorioIp, directorioPuerto, controlador, componenteSeguridad);

        new Thread(cliente).start();
        System.out.println("Anuncio: Sistema ensamblado y listo.");
    });
    }
}
