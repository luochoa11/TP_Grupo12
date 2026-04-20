package com.sgf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.sgf.infraestructura.ClienteOperador;
import com.sgf.presentacion.ControladorOperador;
import com.sgf.presentacion.VentanaPanelOperador;

public class MainOperador {

    public static void main(String[] args) {
        //por defecto si no se pasa un argumento en puesto id
        int idPuesto = 1;

        // Si pasamos un argumento (ej: java -jar Operador.jar 3), lo tomamos como ID
        // ej en comandos un operador 2: java -cp "panel-operador/target/classes;common/target/classes" com.sgf.MainOperador 2

        if (args.length > 0) {
            try {
                idPuesto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("ID de puesto inválido. Usando ID por defecto: 1");
            }
        }

        final int idFinal = idPuesto;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {

            // 2. Inicializamos la Vista
            VentanaPanelOperador ventana = new VentanaPanelOperador();
            // Título de la ventana para saber cuál es
            ventana.setTitle("Panel de Operador - Puesto #" + idFinal);

            // 3. Inicializamos el Controlador y conectamos
            ClienteOperador cliente = new ClienteOperador(Constantes.HOST_SERVIDOR_CENTRAL,Constantes.PUERTO_SERVIDOR_CENTRAL);
            ControladorOperador controlador = new ControladorOperador(ventana, cliente,idFinal);
            ventana.setControlador(controlador);

            ventana.setVisible(true);
            System.out.println("SGF Operador: Iniciado en Puesto #" + idFinal + " y listo.");

        });
    }
}