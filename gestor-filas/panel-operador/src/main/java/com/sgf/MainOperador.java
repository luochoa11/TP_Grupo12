package com.sgf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainOperador {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            // 1. Inicializamos la lógica (Modelo)
            LogicaFila modelo = new LogicaFila();

            // 2. Inicializamos la Vista
            VentanaPanelOperador ventana = new VentanaPanelOperador();

            // 3. Inicializamos el Controlador y conectamos
            ControladorOperador controlador = new ControladorOperador(ventana, modelo);
            ventana.setControlador(controlador);

            ventana.setVisible(true);

            // 4. Iniciamos el servidor para recibir DNIs de la Terminal
            // Le pasamos el controlador para que maneje la entrada
            new Thread(new ServidorOperador(Constantes.PUERTO_OPERADOR1, controlador)).start();

            System.out.println("SGF Operador: Iniciado y listo.");
        });
    }
}