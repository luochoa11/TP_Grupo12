package com.sgf;

import javax.swing.SwingUtilities;

import com.sgf.infraestructura.ProxyRegistro;
import com.sgf.interfaces.IServicioRegistro;
import com.sgf.presentacion.ControladorRegistro;
import com.sgf.presentacion.VentanaTerminalRegistro;

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

            IServicioRegistro servicio = new ProxyRegistro(directorioIp, directorioPuerto);

            ControladorRegistro controlador = new ControladorRegistro(ventana, servicio);
            ventana.setControlador(controlador);
            ventana.setVisible(true);

            System.out.println("Ventana creada: " + tituloFinal);
        });
    }
}