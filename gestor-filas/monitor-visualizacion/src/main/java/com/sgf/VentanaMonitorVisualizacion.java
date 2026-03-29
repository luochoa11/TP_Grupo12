package com.sgf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.util.ArrayList;
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
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(30, 60, 30, 60));

        // Encabezado
        JLabel lblHeader = new JLabel("BIENVENIDO", SwingConstants.CENTER);
        lblHeader.setForeground(new Color(148, 163, 184));
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(lblHeader, BorderLayout.NORTH);

        // Tarjeta Blanca Principal
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(40, 40, 40, 40)
        ));


        JLabel lblLlamando = new JLabel("LLAMANDO AHORA", SwingConstants.CENTER);
        lblLlamando.setForeground(new Color(51, 65, 85));
        lblLlamando.setFont(new Font("SansSerif", Font.BOLD, 28));
        card.add(lblLlamando, BorderLayout.NORTH);

        lblTurnoActual = new JLabel("---", SwingConstants.CENTER);
        lblTurnoActual.setFont(new Font("SansSerif", Font.BOLD, 110));
        lblTurnoActual.setForeground(new Color(28, 45, 75));
        card.add(lblTurnoActual, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        // --- Historial ---
        JPanel wrapperInferior = new JPanel(new BorderLayout());
        wrapperInferior.setOpaque(false);
        
        JLabel lblUltimos = new JLabel("Últimos llamados:", SwingConstants.LEFT);
        lblUltimos.setForeground(Color.WHITE);
        lblUltimos.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblUltimos.setBorder(new EmptyBorder(30, 0, 15, 0)); // Margen arriba y abajo del texto
        wrapperInferior.add(lblUltimos, BorderLayout.NORTH);

        // Grid de 4 turnos
        panelHistorial = new JPanel(new GridLayout(2, 2, 20, 20));
        panelHistorial.setOpaque(false);
        panelHistorial.setPreferredSize(new Dimension(0, 180));
        wrapperInferior.add(panelHistorial, BorderLayout.CENTER);

        add(wrapperInferior, BorderLayout.SOUTH);
        
        // Inicializar con guiones
        actualizarPantalla(null, new ArrayList<>());
    }

    private JPanel crearTarjetaHistorial(int orden, String dni) {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setBackground(new Color(30, 41, 59));
        p.setBorder(new EmptyBorder(10, 25, 10, 25));
        
        JLabel lblNum = new JLabel(String.valueOf(orden));
        lblNum.setForeground(new Color(96, 165, 250));
        lblNum.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        JLabel lblDni = new JLabel(dni);
        lblDni.setForeground(Color.WHITE);
        lblDni.setFont(new Font("SansSerif", Font.BOLD, 22));

        p.add(lblNum, BorderLayout.WEST);
        p.add(lblDni, BorderLayout.CENTER);
        return p;
    }

    /**
     * Método público para el controlador
     */
    public void actualizarPantalla(Turno actual, List<Turno> historial) {
        SwingUtilities.invokeLater(() -> {
            if (actual != null) {
                lblTurnoActual.setText(actual.getDniCliente());
            }

            panelHistorial.removeAll();
            for (int i = 0; i < 4; i++) {
                String dni = (i < historial.size()) ? historial.get(i).getDniCliente() : "---";
                panelHistorial.add(crearTarjetaHistorial(i + 1, dni));
            }
            panelHistorial.revalidate();
            panelHistorial.repaint();
        });
    }
}