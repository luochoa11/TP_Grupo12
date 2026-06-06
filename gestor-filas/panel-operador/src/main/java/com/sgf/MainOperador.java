package com.sgf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.sgf.ConfiguracionRed;
import com.sgf.infraestructura.ProxyOperador;
import com.sgf.interfaces.IServicioOperador;
import com.sgf.presentacion.ControladorOperador;
import com.sgf.presentacion.VentanaOperador;
import com.sgf.seguridad.SeguridadOperador;

public class MainOperador {

    public static void main(String[] args) {
        int idPuesto = 1;
        if (args.length > 0) {
            try {
                idPuesto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("ID de puesto inválido. Usando ID por defecto: 1");
            }
        }
        
        String directorioIp     = ConfiguracionRed.get("directorio.ip");
        int    directorioPuerto = ConfiguracionRed.getInt("directorio.puerto");

        final int idFinal = idPuesto;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {

            VentanaOperador ventana = new VentanaOperador();
            ventana.setTitle("Panel de Operador - Puesto #" + idFinal);

            SeguridadOperador componenteSeguridad = new SeguridadOperador();
            IServicioOperador servicio = new ProxyOperador(directorioIp, directorioPuerto, componenteSeguridad);
            
            ControladorOperador controlador = new ControladorOperador(ventana, servicio, idFinal);
            ventana.setControlador(controlador);

            ventana.setVisible(true);
            System.out.println("SGF Operador: Iniciado en Puesto #" + idFinal + " y listo.");

        });
    }
}
