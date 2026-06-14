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

        int idTerminal = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
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

    

        SeguridadRegistro componenteSeguridad = new SeguridadRegistro(idTerminal);
        IServicioRegistro servicio = new ProxyRegistro(directorioIp, directorioPuerto, componenteSeguridad);

        ControladorRegistro controlador = new ControladorRegistro(ventana, servicio);
        ventana.setControlador(controlador);
        ventana.setVisible(true);

        System.out.println("Ventana creada: " + tituloFinal);
    });
    }
}
