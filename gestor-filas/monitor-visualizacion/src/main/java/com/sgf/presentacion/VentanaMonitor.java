package com.sgf.presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.sgf.modelos.Turno;

public class VentanaMonitor extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private JPanel panelTurnos;

    private Timer timerParpadeo;   // timer que alterna el color del DNI en rellamado
    // Lista para trackear todos los labels que deben parpadear en cada actualización
    private List<JLabel> labelsParaTitilar = new ArrayList<>();

    // Colores basados en la identidad visual de la App
     private final Color COLOR_FONDO_OSCURO = new Color(15, 23, 42);      // Azul oscuro fondo
    private final Color COLOR_ACCENTO_CELESTE = new Color(96, 165, 250); // Celeste del operador
    private final Color COLOR_ROJO_ALERTA = new Color(220, 38, 38);      // Rojo para rellamadas
    private final Color COLOR_GRIS_HISTORIAL = new Color(241, 245, 249); // Gris muy suave tarjetas
    private final Color COLOR_TEXTO_ETIQUETA = new Color(100, 116, 139); // Gris suave para labels
    private final Color COLOR_TEXTO_VALOR = new Color(15, 23, 42);       // Texto oscuro principal

    public VentanaMonitor() {
        setTitle("Monitor de Sala");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(COLOR_FONDO_OSCURO);
        contentPane.setBorder(new EmptyBorder(20, 30, 20, 30));
        setContentPane(contentPane);

        // Encabezado
        JLabel lblTitulo = new JLabel("ESTADO DE TURNOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        contentPane.add(lblTitulo, BorderLayout.NORTH);

        // Contenedor principal de tarjetas
        panelTurnos = new JPanel();
        panelTurnos.setLayout(new BoxLayout(panelTurnos, BoxLayout.Y_AXIS));
        panelTurnos.setOpaque(false);
        contentPane.add(panelTurnos, BorderLayout.CENTER);
    }


    private JPanel crearTarjeta(Turno turno, boolean esActual, boolean resaltarDni) {

        JPanel tarjeta = new JPanel(new GridLayout(1, 2));
        tarjeta.setMaximumSize(new Dimension(1000, 80));
        tarjeta.setPreferredSize(new Dimension(740, 80));

        // Estilo según jerarquía
        if (esActual) {
            tarjeta.setBackground(Color.WHITE);
            tarjeta.setBorder(BorderFactory.createLineBorder(COLOR_ROJO_ALERTA, 4, true));
        } else {
            tarjeta.setBackground(COLOR_GRIS_HISTORIAL);
            tarjeta.setBorder(BorderFactory.createLineBorder(COLOR_ACCENTO_CELESTE, 1, true));
        }

        Font fuenteEtiqueta = new Font("Segoe UI", Font.BOLD, 26);
        Font fuenteValor = new Font("Segoe UI", Font.BOLD, 26);

        // --- COLUMNA DNI ---
        JPanel pnlDni = new JPanel(new GridLayout(1, 2));
        pnlDni.setOpaque(false);
        // Línea divisoria en el medio: borde derecho gris suave
        pnlDni.setBorder(new MatteBorder(0, 0, 0, 1, new Color(203, 213, 225)));
        
        JLabel tagDni = new JLabel("DNI", SwingConstants.RIGHT);
        tagDni.setFont(fuenteEtiqueta);
        tagDni.setForeground(COLOR_TEXTO_ETIQUETA);
        tagDni.setBorder(new EmptyBorder(0, 0, 0, 15));
        
        JLabel valDni = new JLabel(turno.getDniCliente(), SwingConstants.LEFT);
        valDni.setFont(fuenteValor);
        
        // Lógica de parpadeo: lo agregamos a la lista si corresponde
        if (resaltarDni) {
            valDni.setForeground(COLOR_ROJO_ALERTA);
            labelsParaTitilar.add(valDni);
        } else {
            valDni.setForeground(COLOR_TEXTO_VALOR);
        }

        pnlDni.add(tagDni);
        pnlDni.add(valDni);

        // --- COLUMNA PUESTO ---
        JPanel pnlPuesto = new JPanel(new GridLayout(1, 2));
        pnlPuesto.setOpaque(false);
        
        JLabel tagPuesto = new JLabel("PUESTO", SwingConstants.RIGHT);
        tagPuesto.setFont(fuenteEtiqueta);
        tagPuesto.setForeground(COLOR_TEXTO_ETIQUETA);
        tagPuesto.setBorder(new EmptyBorder(0, 0, 0, 15));
        
        JLabel valPuesto = new JLabel(String.valueOf(turno.getIdPuesto()), SwingConstants.LEFT);
        valPuesto.setFont(fuenteValor);
        valPuesto.setForeground(COLOR_TEXTO_VALOR);
        
        pnlPuesto.add(tagPuesto);
        pnlPuesto.add(valPuesto);

        tarjeta.add(pnlDni);
        tarjeta.add(pnlPuesto);

        return tarjeta;
    }

    /**
     * Método público para el controlador
     */
    public void actualizarPantalla(Turno actual, List<Turno> historial) {
        SwingUtilities.invokeLater(() -> {
            
           // 1. Limpieza de estado anterior
            if (timerParpadeo != null && timerParpadeo.isRunning()) {
                timerParpadeo.stop();
            }
            labelsParaTitilar.clear();
            panelTurnos.removeAll();

            if (actual != null) {
                boolean esRellamada = actual.getIntentos() > 1;
                panelTurnos.add(crearTarjeta(actual, true, esRellamada));
                panelTurnos.add(Box.createRigidArea(new Dimension(0, 15)));
            }

            if (historial != null && !historial.isEmpty()) {
                for (Turno t : historial) {
                    boolean esRellamadaH = t.getIntentos() > 1;
                    panelTurnos.add(crearTarjeta(t, false, esRellamadaH));
                    panelTurnos.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            // Iniciar parpadeo sincronizado si hay turnos con reintentos
            if (!labelsParaTitilar.isEmpty()) {
                final boolean[] encendido = {true};
                timerParpadeo = new Timer(500, e -> {
                    Color colorActual = encendido[0] ? COLOR_ROJO_ALERTA : COLOR_TEXTO_VALOR;
                    for (JLabel lbl : labelsParaTitilar) {
                        lbl.setForeground(colorActual);
                    }
                    encendido[0] = !encendido[0];
                });
                timerParpadeo.start();
            }

            panelTurnos.revalidate();
            panelTurnos.repaint();
        });
    }
}