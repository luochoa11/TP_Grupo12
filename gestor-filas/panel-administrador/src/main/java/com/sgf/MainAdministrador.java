package com.sgf;

import javax.swing.SwingUtilities;

import com.sgf.infraestructura.ProxyAdministrador;
import com.sgf.interfaces.IServicioAdministrador;
import com.sgf.presentacion.ControladorAdministrador;
import com.sgf.presentacion.VentanaAdministrador;

public class MainAdministrador {

    public static void main(String[] args) {
        System.out.println("Arrancando Panel de Administración...");

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
            VentanaAdministrador ventana = new VentanaAdministrador();
            ventana.setTitle("Panel de Administración");

            IServicioAdministrador servicio = new ProxyAdministrador(directorioIp, directorioPuerto);

            ControladorAdministrador controlador = new ControladorAdministrador(ventana, servicio);
            
            ventana.setControlador(controlador);
            ventana.setVisible(true);

            controlador.actualizarEstadoGeneral();

            System.out.println("Ventana de Administración creada y conectada al Directorio.");
        });
    }
}