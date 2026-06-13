package com.sgf;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.SwingUtilities;

import com.sgf.ConfiguracionRed;
import com.sgf.infraestructura.ProxyRegistro;
import com.sgf.interfaces.IServicioRegistro;
import com.sgf.presentacion.ControladorRegistro;
import com.sgf.presentacion.VentanaTerminalRegistro;
import com.sgf.seguridad.SeguridadRegistro;

public class MainRegistro {
    public static void main(String[] args) {
        System.out.println("Arrancando...");

        String idTerminal = (args.length > 0) ? " #" + args[0] : "";
        final String tituloFinal = "Terminal de Registro" + idTerminal;

        String directorioIp     = ConfiguracionRed.get("directorio.ip");
        int    directorioPuerto = ConfiguracionRed.getInt("directorio.puerto");

        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    SwingUtilities.invokeLater(() -> {
        VentanaTerminalRegistro ventana = new VentanaTerminalRegistro();
        ventana.setTitle(tituloFinal);

        // Pedir config de seguridad al directorio
        String algoritmo = "AES";
        String clave = "";
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_CONFIG_SEGURIDAD");
            out.flush();
            algoritmo = (String) in.readObject();
            clave     = (String) in.readObject();
            System.out.println("[Registro] Config de seguridad recibida: " + algoritmo);
        } catch (Exception e) {
            System.err.println("[Registro] No se pudo obtener config de seguridad: " + e.getMessage());
        }

        SeguridadRegistro componenteSeguridad = new SeguridadRegistro(algoritmo, clave);
        IServicioRegistro servicio = new ProxyRegistro(directorioIp, directorioPuerto, componenteSeguridad);

        ControladorRegistro controlador = new ControladorRegistro(ventana, servicio);
        ventana.setControlador(controlador);
        ventana.setVisible(true);

        System.out.println("Ventana creada: " + tituloFinal);
    });
    }
}
