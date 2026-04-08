package com.sgf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainOperador {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {

            // 2. Inicializamos la Vista
            VentanaPanelOperador ventana = new VentanaPanelOperador();

            // 3. Inicializamos el Controlador y conectamos
            ClienteOperador cliente = new ClienteOperador("localhost",Constantes.PUERTO_SERVIDOR_CENTRAL);
            ControladorOperador controlador = new ControladorOperador(ventana, cliente,Constantes.ID_PUESTO1);
            ventana.setControlador(controlador);

            ventana.setVisible(true);

            
            

            System.out.println("SGF Operador: Iniciado y listo.");
        });
    }
}