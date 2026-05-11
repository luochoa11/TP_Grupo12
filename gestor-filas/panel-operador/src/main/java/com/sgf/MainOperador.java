package com.sgf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.sgf.infraestructura.ProxyOperador;
import com.sgf.interfaces.IServicioOperador;
import com.sgf.presentacion.ControladorOperador;
import com.sgf.presentacion.VentanaOperador;

public class MainOperador {

    public static void main(String[] args) {
        //por defecto si no se pasa un argumento en puesto id
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

            // 2. Inicializamos la Vista
            VentanaOperador ventana = new VentanaOperador();
            // Título de la ventana para saber cuál es
            ventana.setTitle("Panel de Operador - Puesto #" + idFinal);

            // 3. Inicializamos el Controlador y conectamos
            IServicioOperador servicio = new ProxyOperador(directorioIp /*IP del dir que mapea a server*/, directorioPuerto);
            ControladorOperador controlador = new ControladorOperador(ventana, servicio, idFinal);
            ventana.setControlador(controlador);

            ventana.setVisible(true);
            System.out.println("SGF Operador: Iniciado en Puesto #" + idFinal + " y listo.");

        });
    }
}