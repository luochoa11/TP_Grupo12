package com.sgf;

import java.awt.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class VentanaPanelOperador extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JPanel panelCola;
    private JLabel lblActual;

    private JButton btnLlamar;
    private ControladorOperador controlador;

    private final Color COLOR_FONDO = new Color(15, 23, 42);
    private final Color COLOR_TARJETA = new Color(30, 41, 59);
    private final Color COLOR_ACCENTO = new Color(96, 165, 250);


    public VentanaPanelOperador() {

        setTitle("Panel deOperador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

        contentPane = new JPanel(new BorderLayout(0, 20));
        contentPane.setBackground(COLOR_FONDO);
        contentPane.setBorder(new EmptyBorder(30, 50, 30, 50));
        setContentPane(contentPane);

        initUI();
    }

    private void initUI() {

        // Turno actual
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);

        lblActual = new JLabel("Actual: ---", SwingConstants.CENTER);
        lblActual.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblActual.setForeground(Color.WHITE);
        lblActual.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_ACCENTO, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        panelNorte.add(lblActual, BorderLayout.CENTER);
        contentPane.add(lblActual, BorderLayout.NORTH);

        // Cola
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setOpaque(false);

        JLabel lblTituloCola = new JLabel("Clientes en espera:", SwingConstants.LEFT);
        lblTituloCola.setForeground(new Color(148, 163, 184));
        lblTituloCola.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTituloCola.setBorder(new EmptyBorder(10, 0, 10, 0));
        panelCentro.add(lblTituloCola, BorderLayout.NORTH);

        panelCola = new JPanel();
        panelCola.setLayout(new BoxLayout(panelCola, BoxLayout.Y_AXIS));
        panelCola.setBackground(COLOR_FONDO);

        JScrollPane scroll = new JScrollPane(panelCola);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        panelCentro.add(scroll, BorderLayout.CENTER);

        contentPane.add(panelCentro, BorderLayout.CENTER);

        // Botón llamar
        btnLlamar = new JButton("Llamar siguiente");
        btnLlamar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnLlamar.setBackground(COLOR_ACCENTO);
        btnLlamar.setForeground(COLOR_FONDO);
        btnLlamar.setFocusPainted(false);
        btnLlamar.setPreferredSize(new Dimension(0, 80));
        btnLlamar.addActionListener(e -> {
            if (controlador != null) controlador.accionarLlamado();
        });
        contentPane.add(btnLlamar, BorderLayout.SOUTH);

    }

    public void setControlador(ControladorOperador controlador) {
        this.controlador = controlador;
    }

    private JPanel crearItemTurno(String dni) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_TARJETA);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85)),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lbl = new JLabel(dni);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 20));

        p.add(lbl, BorderLayout.CENTER);
        return p;
    }


    public void actualizarVista(Turno actual, List<Turno> cola) {
        lblActual.setText("Actual: " + (actual != null ? actual.getDniCliente() : "---"));

        panelCola.removeAll();
        for (Turno t : cola) {
            panelCola.add(crearItemTurno(t.getDniCliente()));
            panelCola.add(Box.createVerticalStrut(10)); // Espaciado entre items
        }
        panelCola.revalidate();
        panelCola.repaint();
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
}