package com.sgf.presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sgf.modelos.Turno;

public class VentanaPanelOperador extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JPanel panelCola;
    private JLabel lblActual;

    private JButton btnLlamar;
    private JButton btnReintentar;

    private Timer timer; //para habilitar reintento (30s)
    private Timer timerPull; //actualizar vista
    private ControladorOperador controlador;

    private final Color COLOR_FONDO = new Color(15, 23, 42);
    private final Color COLOR_TARJETA = new Color(30, 41, 59);
    private final Color COLOR_ACCENTO = new Color(96, 165, 250);


    public VentanaPanelOperador() {

        setTitle("Panel de Operador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

        contentPane = new JPanel(new BorderLayout(0, 20));
        contentPane.setBackground(COLOR_FONDO);
        contentPane.setBorder(new EmptyBorder(30, 50, 30, 50));
        setContentPane(contentPane);

        initUI();
        timerIntentos();
        iniciarPull();
    }

    private void initUI() {
        // Turno actual
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);

        lblActual = new JLabel("No hay clientes pendientes", SwingConstants.CENTER);
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

        //Botones de acción
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 15, 0)); // 1 fila, 2 columnas
        panelBotones.setOpaque(false);

        // Botón llamar
        btnLlamar = new JButton("Llamar siguiente");
        btnLlamar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnLlamar.setBackground(COLOR_ACCENTO);
        btnLlamar.setForeground(COLOR_FONDO);
        btnLlamar.setFocusPainted(false);
        btnLlamar.setPreferredSize(new Dimension(0, 80));
        btnLlamar.addActionListener(e -> {
            if (controlador != null) controlador.accionarLlamado();
            reiniciarTIntento();
            
        });

        btnReintentar = new JButton("Reintentar llamado");
        btnReintentar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnReintentar.setBackground(COLOR_ACCENTO);
        btnReintentar.setForeground(COLOR_FONDO);
        btnReintentar.setFocusPainted(false);
        btnReintentar.setPreferredSize(new Dimension(0, 80));
        btnReintentar.setEnabled(false);
        btnReintentar.addActionListener(e -> {
            if (controlador != null) controlador.accionarReintento();
            reiniciarTIntento();
        });
        panelBotones.add(btnLlamar);
        panelBotones.add(btnReintentar);
        contentPane.add(panelBotones, BorderLayout.SOUTH);

    }

    /**
     * Configura el timer de 30 segundos para el re-intento.
     */
    private void timerIntentos(){
        timer = new Timer(30000, e-> {
            btnReintentar.setEnabled(true);
            btnReintentar.setText("Reintentar llamado");
            timer.stop();
        });
    }

    private void reiniciarTIntento(){
        btnReintentar.setEnabled(false);
        btnReintentar.setText("Esperar 30s...");
        timer.restart();
    }

    public void setControlador(ControladorOperador controlador) {
        this.controlador = controlador;
    }

    private void iniciarPull() {
        this.timerPull = new Timer(2000, e -> { // cada 2 segundos
        if (controlador != null) {
            controlador.actualizarCola();
        }
        });
        timerPull.start();
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
        // Si no hay turno actual y la cola está vacía, mostramos el mensaje solicitado
        if (actual == null && cola.isEmpty()) {
            lblActual.setText("No hay clientes pendientes");
        } else if (actual != null) {
            lblActual.setText("Atendiendo: " + actual.getDniCliente() + " (Intento " + actual.getIntentos() + "/3)");
        } else {
            lblActual.setText("Esperando llamado...");
        }

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