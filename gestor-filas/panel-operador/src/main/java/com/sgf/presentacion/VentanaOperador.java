package com.sgf.presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
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

/**
 * Interfaz de usuario para el Panel del Operador.
*/
public class VentanaOperador extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel panelCola;
    private JPanel panelActualContenedor;
    private JLabel lblDniValor;
    private JLabel lblIntentoValor;

    private JButton btnLlamar;
    private JButton btnReintentar;
    private JButton btnFinalizar;

    private Timer timer; // Para habilitar reintento (10s)
    private Timer timerPull; 
    private ControladorOperador controlador;

    private final Color COLOR_FONDO = new Color(15, 23, 42);      
    private final Color COLOR_ACCENTO = new Color(96, 165, 250);  
    private final Color COLOR_TARJETA = new Color(30, 41, 59);    
    private final Color COLOR_TEXTO_SUAVE = new Color(148, 163, 184); 
    private final Color COLOR_ROJO = new Color(220, 38, 38);     
    private final Color COLOR_VERDE_MARINO = new Color(15, 118, 110);

    private boolean ausente = false; 
    private int segundosRestantes = 10;

    public VentanaOperador() {
        setTitle("Panel de Operador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 600));

        JPanel contentPane = new JPanel(new BorderLayout(0, 20));
        contentPane.setBackground(COLOR_FONDO);
        contentPane.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(contentPane);

        initUI(contentPane);
        timerIntentos();
        iniciarPull();
    }

    private void initUI(JPanel container) {
        // --- SECCIÓN SUPERIOR: MONITOR DE TURNOS EN ATENCIÓN ---
        panelActualContenedor = new JPanel(new GridLayout(1, 2));
        panelActualContenedor.setPreferredSize(new Dimension(0, 100));
        panelActualContenedor.setBackground(Color.WHITE);
        panelActualContenedor.setBorder(new LineBorder(COLOR_ACCENTO, 2, false));

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

        // --- SECCIÓN CENTRAL: COLA DE ESPERA ---
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

        // --- SECCIÓN INFERIOR: BOTONERA DE ACCIÓN DE TRES COLUMNAS ---
        JPanel panelBotones = new JPanel(new GridLayout(1, 3, 20, 0));
        panelBotones.setOpaque(false);

        btnLlamar = crearBotonEstilizado("Llamar siguiente", COLOR_ACCENTO);
        btnLlamar.addActionListener(e -> {
            if (controlador != null) {
                Turno siguiente = controlador.accionarLlamado();
                if (siguiente != null) {
                    reiniciarTIntento();
                }
            }
        });

        btnReintentar = crearBotonEstilizado("Reintentar Llamado", COLOR_ACCENTO);
        btnReintentar.setEnabled(false);
        btnReintentar.addActionListener(e -> {
            if (controlador != null) {
                controlador.accionarReintento();
                // Arrancamos el contador solo si no estábamos ya en ausente
                if (!ausente) {
                    reiniciarTIntento();
                }
            }
        });

        btnFinalizar = crearBotonEstilizado("Finalizar Atención", COLOR_VERDE_MARINO);
        btnFinalizar.setEnabled(false);
        btnFinalizar.addActionListener(e -> {
            if (controlador != null) {
                controlador.finalizarAtencion();
            }
        });


        panelBotones.add(btnLlamar);
        panelBotones.add(btnReintentar);
        panelBotones.add(btnFinalizar);
        container.add(panelBotones, BorderLayout.SOUTH);
    }

    private JButton crearBotonEstilizado(String texto, Color bgBorder) {
        JButton b = new JButton(texto) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent(Graphics g) {
                int width = getWidth();
                int height = getHeight();
                
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color currentBg = Color.WHITE; 
                Color currentFg;
                Color currentBorder;
                
                String textoBoton = getText();
                
                if (!isEnabled()) {
                    currentFg = new Color(148, 163, 184);    
                    currentBorder = new Color(203, 213, 225); 
                } else {
                    currentFg = COLOR_FONDO; // Texto oscuro 
                    
                    if (textoBoton.contains("Ausente")) {
                        currentBorder = COLOR_ROJO;
                    } else if (textoBoton.contains("Finalizar")) {
                        currentBorder = COLOR_VERDE_MARINO;
                    } else {
                        currentBorder = COLOR_ACCENTO; // Celeste por defecto
                    }
                }
                
                g2d.setColor(currentBg);
                g2d.fillRect(0, 0, width, height);
                
                g2d.setColor(currentBorder);
                g2d.setStroke(new java.awt.BasicStroke(2.5f));
                g2d.drawRect(1, 1, width - 2, height - 2);
                
                g2d.setFont(getFont());
                g2d.setColor(currentFg);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (width - fm.stringWidth(textoBoton)) / 2;
                int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(textoBoton, x, y);
                
                g2d.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 18));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel crearItemCola(String dni) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_TARJETA);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        p.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, false),
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

    private void timerIntentos(){
        timer = new Timer(1000, e -> {
            segundosRestantes--;

            if (segundosRestantes > 0) {
                if (!ausente) {
                    btnReintentar.setText("Esperar " + segundosRestantes + "s...");
                }
            } else {
                btnReintentar.setEnabled(true);
                if (!ausente) {
                    btnReintentar.setText("Reintentar Llamado");
                }
                timer.stop();
            }
        });
    }

    private void reiniciarTIntento(){
        segundosRestantes = 10;
        btnReintentar.setEnabled(false);
        btnReintentar.setText("Reintentar en 10s...");
        timer.restart();
    }

    public void setControlador(ControladorOperador controlador) {
        this.controlador = controlador;
    }

    private void iniciarPull() {
        this.timerPull = new Timer(2000, e -> {
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
                
                int intentos = actual.getIntentos();
                ausente = (intentos >= 3);

                btnFinalizar.setEnabled(true);

                if (intentos > 1) {
                    lblDniValor.setForeground(COLOR_ROJO);
                    panelActualContenedor.setBorder(new LineBorder(COLOR_ROJO, 3, false));
                } else {
                    lblDniValor.setForeground(COLOR_FONDO);
                    panelActualContenedor.setBorder(new LineBorder(COLOR_ACCENTO, 2, false));
                }

                if (ausente) {
                    if (timer != null && timer.isRunning()) {
                        timer.stop();
                    }
                    btnReintentar.setText("Marcar Ausente");
                    btnReintentar.setEnabled(true);
                } else {
                    if (timer == null || !timer.isRunning()) {
                        btnReintentar.setText("Reintentar Llamado");
                        btnReintentar.setEnabled(true);
                    }
                }

            } else { 
                lblDniValor.setText("---");
                lblIntentoValor.setText("0/3");
                panelActualContenedor.setBorder(new LineBorder(COLOR_TARJETA, 1, false));

                ausente = false;
                if (timer != null && timer.isRunning()) {
                    timer.stop();
                }
                btnReintentar.setText("Reintentar Llamado");
                btnReintentar.setEnabled(false);
                btnFinalizar.setEnabled(false);
            }

            panelCola.removeAll();
            if (cola == null || cola.isEmpty()) {
                JLabel vacio = new JLabel("No hay clientes en espera");
                vacio.setForeground(COLOR_TEXTO_SUAVE);
                vacio.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                vacio.setAlignmentX(CENTER_ALIGNMENT);
                panelCola.add(Box.createVerticalGlue());
                panelCola.add(vacio);
                panelCola.add(Box.createVerticalGlue());
                
                btnLlamar.setEnabled(false);
            } else {
                for (Turno t : cola) {
                    panelCola.add(crearItemCola(t.getDniCliente()));
                    panelCola.add(Box.createVerticalStrut(10));
                }
                btnLlamar.setEnabled(true);
            }
            panelCola.revalidate();
            panelCola.repaint();
        });
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
}