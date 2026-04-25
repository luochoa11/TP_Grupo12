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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.sgf.modelos.Turno;

public class VentanaPanelOperador extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel panelCola;
    private JPanel panelActualContenedor;
    private JLabel lblDniValor;
    private JLabel lblIntentoValor;

    private JButton btnLlamar;
    private JButton btnReintentar;

    private Timer timer; //para habilitar reintento (30s)
    private Timer timerPull; //actualizar vista
    private ControladorOperador controlador;

    private final Color COLOR_FONDO = new Color(15, 23, 42);      // Azul oscuro
    private final Color COLOR_ACCENTO = new Color(96, 165, 250);    // Celeste identidad
    private final Color COLOR_TARJETA = new Color(30, 41, 59);    // Azul grisáceo
    private final Color COLOR_TEXTO_SUAVE = new Color(148, 163, 184); // Gris slate
    private final Color COLOR_ROJO = new Color(220, 38, 38);      // Rojo alerta


    public VentanaPanelOperador() {

        setTitle("Panel de Operador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

        JPanel contentPane = new JPanel(new BorderLayout(0, 20));
        contentPane.setBackground(COLOR_FONDO);
        contentPane.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(contentPane);

        initUI(contentPane);
        timerIntentos();
        iniciarPull();
    }

    private void initUI(JPanel container) {
        // Turno actual
        panelActualContenedor = new JPanel(new GridLayout(1, 2));
        panelActualContenedor.setPreferredSize(new Dimension(0, 100));
        panelActualContenedor.setBackground(Color.WHITE);
        panelActualContenedor.setBorder(new LineBorder(COLOR_ACCENTO, 2, true));

        // Columna DNI del operador
        JPanel pnlDni = new JPanel(new GridLayout(1, 2));
        pnlDni.setOpaque(false);
        pnlDni.setBorder(new MatteBorder(0, 0, 0, 1, new Color(203, 213, 225)));

        JLabel tagDni = new JLabel("ATENDIENDO", SwingConstants.RIGHT);
        tagDni.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tagDni.setForeground(COLOR_TEXTO_SUAVE);
        tagDni.setBorder(new EmptyBorder(0, 0, 0, 15));

        lblDniValor = new JLabel("---", SwingConstants.LEFT);
        lblDniValor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblDniValor.setForeground(COLOR_FONDO);
        
        pnlDni.add(tagDni); pnlDni.add(lblDniValor);


        // Columna Intentos
        JPanel pnlIntentos = new JPanel(new GridLayout(1, 2));
        pnlIntentos.setOpaque(false);
        
        JLabel tagIntento = new JLabel("INTENTO", SwingConstants.RIGHT);
        tagIntento.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tagIntento.setForeground(COLOR_TEXTO_SUAVE);
        tagIntento.setBorder(new EmptyBorder(0, 0, 0, 15));
        
        lblIntentoValor = new JLabel("0/3", SwingConstants.LEFT);
        lblIntentoValor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblIntentoValor.setForeground(COLOR_FONDO);
        
        pnlIntentos.add(tagIntento); pnlIntentos.add(lblIntentoValor);

        panelActualContenedor.add(pnlDni);
        panelActualContenedor.add(pnlIntentos);
        container.add(panelActualContenedor, BorderLayout.NORTH);

        // Cola
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setOpaque(false);

        JLabel lblTituloCola = new JLabel("Clientes en espera:", SwingConstants.LEFT);
        lblTituloCola.setForeground(COLOR_ACCENTO);
        lblTituloCola.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTituloCola.setBorder(new EmptyBorder(10, 0, 10, 0));
        panelCentro.add(lblTituloCola, BorderLayout.NORTH);

        panelCola = new JPanel();
        panelCola.setLayout(new BoxLayout(panelCola, BoxLayout.Y_AXIS));
        panelCola.setBackground(COLOR_FONDO);

        JScrollPane scroll = new JScrollPane(panelCola);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panelCentro.add(scroll, BorderLayout.CENTER);
        container.add(panelCentro, BorderLayout.CENTER);

        //Botones de acción
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 20, 0)); // 1 fila, 2 columnas
        panelBotones.setOpaque(false);

        btnLlamar = crearBotonEstilizado("Llamar siguiente", COLOR_ACCENTO);
        btnLlamar.addActionListener(e -> {
            if (controlador != null) controlador.accionarLlamado();
            reiniciarTIntento();
        });

        btnReintentar = crearBotonEstilizado("Esperar 30s...", new Color(51, 65, 85));
        btnReintentar.setEnabled(false);
        btnReintentar.addActionListener(e -> {
            if (controlador != null) controlador.accionarReintento();
            reiniciarTIntento();
        });

        panelBotones.add(btnLlamar);
        panelBotones.add(btnReintentar);
        container.add(panelBotones, BorderLayout.SOUTH);

    }

    private JButton crearBotonEstilizado(String texto, Color bg) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Segoe UI", Font.BOLD, 22));
        b.setBackground(bg);
        b.setForeground(bg.equals(COLOR_ACCENTO) ? COLOR_FONDO : Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }


    private JPanel crearItemCola(String dni) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_TARJETA);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        p.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JLabel lblDni = new JLabel("DNI: " + dni);
        lblDni.setForeground(Color.WHITE);
        lblDni.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(lblDni, BorderLayout.WEST);

        JLabel lblEstado = new JLabel("EN ESPERA", SwingConstants.RIGHT);
        lblEstado.setForeground(COLOR_TEXTO_SUAVE);
        lblEstado.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        p.add(lblEstado, BorderLayout.EAST);

        return p;
    }

    /**
     * Configura el timer de 30 segundos para el re-intento.
     */
    private void timerIntentos(){
        timer = new Timer(30000, e-> {
            btnReintentar.setEnabled(true);
            btnReintentar.setText("Reintentar llamado");
            btnReintentar.setBackground(COLOR_ACCENTO);
            btnReintentar.setForeground(COLOR_FONDO);
            timer.stop();
        });
    }

    private void reiniciarTIntento(){
        btnReintentar.setEnabled(false);
        btnReintentar.setText("Esperar 30s...");
        btnReintentar.setBackground(new Color(51, 65, 85));
        btnReintentar.setForeground(Color.WHITE);
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


    public void actualizarVista(Turno actual, List<Turno> cola) {
        SwingUtilities.invokeLater(() -> {
            if (actual != null) {
                lblDniValor.setText(actual.getDniCliente());
                lblIntentoValor.setText(actual.getIntentos() + "/3");
                
                // Si es reintento, destacamos en rojo como en el monitor
                if (actual.getIntentos() > 1) {
                    lblDniValor.setForeground(COLOR_ROJO);
                    panelActualContenedor.setBorder(new LineBorder(COLOR_ROJO, 3, true));
                } else {
                    lblDniValor.setForeground(COLOR_FONDO);
                    panelActualContenedor.setBorder(new LineBorder(COLOR_ACCENTO, 2, true));
                }
            } else {
                lblDniValor.setText("---");
                lblIntentoValor.setText("0/3");
                panelActualContenedor.setBorder(new LineBorder(COLOR_TARJETA, 1, true));
            }

            panelCola.removeAll();
            if (cola.isEmpty()) {
                JLabel vacio = new JLabel("No hay clientes en espera");
                vacio.setForeground(COLOR_TEXTO_SUAVE);
                vacio.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                vacio.setAlignmentX(CENTER_ALIGNMENT);
                panelCola.add(Box.createVerticalGlue());
                panelCola.add(vacio);
                panelCola.add(Box.createVerticalGlue());
            } else {
                for (Turno t : cola) {
                    panelCola.add(crearItemCola(t.getDniCliente()));
                    panelCola.add(Box.createVerticalStrut(10));
                }
            }
            panelCola.revalidate();
            panelCola.repaint();
        });
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }

}