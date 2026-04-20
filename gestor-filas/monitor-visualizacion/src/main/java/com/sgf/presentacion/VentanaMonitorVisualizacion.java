package com.sgf.presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sgf.modelos.Turno;

public class VentanaMonitorVisualizacion extends JFrame {

    private JLabel lblTurnoActual;
    private JPanel panelHistorial;
    private JLabel lblPuesto;

    private final Color COLOR_FONDO = new Color(15, 23, 42);
    private final Color COLOR_ACCENTO = new Color(96, 165, 250);
    private final Color COLOR_TEXTO_DARK = new Color(28, 45, 75);
    private final Color COLOR_TEXTO_MUTED = new Color(100, 116, 139);

    public VentanaMonitorVisualizacion() {
        setTitle("Monitor de Sala");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        getContentPane().setBackground(COLOR_FONDO);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(20, 50, 20, 50));

        // Encabezado
        JLabel lblHeader = new JLabel("BIENVENIDO", SwingConstants.CENTER);
        lblHeader.setForeground(new Color(148, 163, 184));
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblHeader, BorderLayout.NORTH);

        JPanel wrapperCentro = new JPanel(new BorderLayout());
        wrapperCentro.setOpaque(false);
        // Tarjeta de llamado actual
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        // Título dentro de la tarjeta
        JLabel lblLlamando = new JLabel("LLAMANDO AHORA", SwingConstants.CENTER);
        lblLlamando.setForeground(new Color(51, 65, 85));
        lblLlamando.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblLlamando.setBorder(new EmptyBorder(0, 0, 20, 0));
        card.add(lblLlamando, BorderLayout.NORTH);

        // Sub-panel con las dos columnas (DNI | PUESTO)
        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // --- COLUMNA DNI ---
        JPanel colDni = new JPanel(new BorderLayout());
        colDni.setOpaque(false);
        
        JLabel titDni = new JLabel("DNI", SwingConstants.CENTER);
        titDni.setFont(new Font("SansSerif", Font.BOLD, 18));
        titDni.setForeground(COLOR_TEXTO_MUTED);
        colDni.add(titDni, BorderLayout.NORTH);

        lblTurnoActual = new JLabel("---", SwingConstants.CENTER);
        lblTurnoActual.setFont(new Font("SansSerif", Font.BOLD, 100));
        lblTurnoActual.setForeground(COLOR_TEXTO_DARK);
        colDni.add(lblTurnoActual, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.60;
        panelContenido.add(colDni, gbc);

        // --- DIVISOR VERTICAL ---
        JPanel divisor = new JPanel();
        divisor.setBackground(new Color(226, 232, 240));
        gbc.gridx = 1; gbc.weightx = 0.01;
        gbc.insets = new Insets(10, 20, 10, 20);
        panelContenido.add(divisor, gbc);

        // --- COLUMNA PUESTO (34% del ancho) ---
        JPanel colPuesto = new JPanel(new BorderLayout());
        colPuesto.setOpaque(false);
        gbc.insets = new Insets(0, 0, 0, 0);

        JLabel titPuesto = new JLabel("PUESTO", SwingConstants.CENTER);
        titPuesto.setFont(new Font("SansSerif", Font.BOLD, 18));
        titPuesto.setForeground(COLOR_TEXTO_MUTED);
        colPuesto.add(titPuesto, BorderLayout.NORTH);

        lblPuesto = new JLabel("-", SwingConstants.CENTER);
        lblPuesto.setFont(new Font("SansSerif", Font.BOLD, 85)); //se podría agrandar a 100 para que se vea como el dni
        lblPuesto.setForeground(COLOR_ACCENTO);
        colPuesto.add(lblPuesto, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.weightx = 0.34;
        panelContenido.add(colPuesto, gbc);

        card.add(panelContenido, BorderLayout.CENTER);
        wrapperCentro.add(card, BorderLayout.CENTER);
        add(wrapperCentro, BorderLayout.CENTER);

        
        // --- Historial ---
        JPanel wrapperInferior = new JPanel(new BorderLayout());
        wrapperInferior.setOpaque(false);
        
        JLabel lblUltimos = new JLabel("Últimos llamados:", SwingConstants.LEFT);
        lblUltimos.setForeground(Color.WHITE);
        lblUltimos.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblUltimos.setBorder(new EmptyBorder(25, 0, 10, 0)); // Margen arriba y abajo del texto
        wrapperInferior.add(lblUltimos, BorderLayout.NORTH);

        // Grid de 4 turnos
        panelHistorial = new JPanel(new GridLayout(2, 2, 20, 20));
        panelHistorial.setOpaque(false);
        panelHistorial.setPreferredSize(new Dimension(0, 160));
        wrapperInferior.add(panelHistorial, BorderLayout.CENTER);

        add(wrapperInferior, BorderLayout.SOUTH);
        
        // Estado inicial con guiones
        actualizarPantalla(null, new ArrayList<>());
    }

    private JPanel crearTarjetaHistorial(int orden, String dni) {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setBackground(new Color(30, 41, 59));
        p.setBorder(new EmptyBorder(10, 25, 10, 25));
        
        JLabel lblNum = new JLabel(String.valueOf(orden));
        lblNum.setForeground(COLOR_ACCENTO);
        lblNum.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        JLabel lblDni = new JLabel(dni);
        lblDni.setForeground(Color.WHITE);
        lblDni.setFont(new Font("SansSerif", Font.BOLD, 20));

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
                lblPuesto.setText("" + actual.getIdPuesto());

                if (actual.getIntentos() > 1) {
                    lblTurnoActual.setForeground(Color.RED); // Cambia a rojo si es reintento
                } else {
                    lblTurnoActual.setForeground(COLOR_TEXTO_DARK); // Color normal
                }
            } else {
                lblTurnoActual.setText("---");
                lblPuesto.setText("-");
                lblTurnoActual.setForeground(COLOR_TEXTO_DARK);
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