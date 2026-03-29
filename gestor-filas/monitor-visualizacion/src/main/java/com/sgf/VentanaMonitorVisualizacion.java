package com.sgf;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VentanaMonitorVisualizacion extends JFrame {

    private JLabel lblTurnoActual;
    private JPanel panelHistorial;

    public VentanaMonitorVisualizacion() {
        setTitle("Monitor de Sala");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        getContentPane().setBackground(new Color(15, 23, 42));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        lblTurnoActual = new JLabel("---", SwingConstants.CENTER);
        lblTurnoActual.setFont(new Font("SansSerif", Font.BOLD, 100));
        lblTurnoActual.setForeground(Color.WHITE);
        add(lblTurnoActual, BorderLayout.CENTER);

        panelHistorial = new JPanel(new GridLayout(2, 2, 15, 15));
        add(panelHistorial, BorderLayout.SOUTH);
    }

    private JPanel crearTarjetaTurno(int orden, String dni) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(30, 41, 59));
        JLabel lblNum = new JLabel(String.valueOf(orden));
        lblNum.setForeground(new Color(96, 165, 250));
        lblNum.setFont(new Font("SansSerif", Font.BOLD, 26));
        JLabel lblDni = new JLabel(dni);
        lblDni.setForeground(Color.WHITE);
        lblDni.setFont(new Font("SansSerif", Font.BOLD, 30));
        p.add(lblNum, BorderLayout.WEST);
        p.add(lblDni, BorderLayout.CENTER);
        return p;
    }

    public void actualizarPantalla(final Turno actual, final List<Turno> historial) {
        SwingUtilities.invokeLater(() -> {
            lblTurnoActual.setText(actual.getDniCliente());

            panelHistorial.removeAll();
            for (int i = 0; i < historial.size() && i < 4; i++) {
                panelHistorial.add(crearTarjetaTurno(i + 1, historial.get(i).getDniCliente()));
            }
            for (int i = historial.size(); i < 4; i++) {
                panelHistorial.add(crearTarjetaTurno(i + 1, "---"));
            }

            panelHistorial.revalidate();
            panelHistorial.repaint();
        });
    }
}