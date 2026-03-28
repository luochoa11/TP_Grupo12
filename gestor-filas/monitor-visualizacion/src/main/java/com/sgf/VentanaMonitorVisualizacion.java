package com.sgf;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Ventana del Monitor de Sala.
 * Diseño responsive
 */
public class VentanaMonitorVisualizacion extends JFrame {

    private JLabel lblTurnoActual;
    private JPanel panelHistorial;
    
    // Colores personalizados <--se podría usar la misma para la terminal de registro
    private final Color COLOR_FONDO = new Color(15, 23, 42);
    private final Color COLOR_TARJETA_PRIMARIA = Color.WHITE;
    private final Color COLOR_TARJETA_SECUNDARIA = new Color(30, 41, 59);
    private final Color COLOR_TEXTO_DNI = new Color(15, 23, 42);
    private final Color COLOR_BORDE = new Color(51, 65, 85);

    public VentanaMonitorVisualizacion() {
        setTitle("Monitor de Sala");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setMinimumSize(new Dimension(800, 600));
        getContentPane().setBackground(COLOR_FONDO);
        
        initComponents();
        
        // Inicia el servidor en el puerto 5001
        new Thread(new ServidorMonitor(5001, this)).start();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(20, 40, 20, 40));

        // --- ENCABEZADO SUPERIOR ---
        JLabel lblHeader = new JLabel("SISTEMA DE GESTIÓN DE FILAS", SwingConstants.CENTER);
        lblHeader.setForeground(new Color(148, 163, 184));
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblHeader.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- CONTENIDO CENTRAL: TARJETA DE LLAMADO ---
        JPanel contenedorCentral = new JPanel(new BorderLayout());
        contenedorCentral.setOpaque(false);
        
        // Tarjeta Blanca con BorderLayout para que nada desaparezca
        JPanel cardPrincipal = new JPanel(new BorderLayout());
        cardPrincipal.setBackground(COLOR_TARJETA_PRIMARIA);
        cardPrincipal.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Título Fijo Arriba
        JLabel lblLlamando = new JLabel("LLAMANDO AHORA", SwingConstants.CENTER);
        lblLlamando.setForeground(new Color(51, 65, 85));
        lblLlamando.setFont(new Font("SansSerif", Font.BOLD, 28));
        cardPrincipal.add(lblLlamando, BorderLayout.NORTH);

        // DNI Central (se ajusta al espacio restante)
        lblTurnoActual = new JLabel("---", SwingConstants.CENTER);
        lblTurnoActual.setForeground(COLOR_TEXTO_DNI);
        lblTurnoActual.setFont(new Font("SansSerif", Font.BOLD, 100)); 
        cardPrincipal.add(lblTurnoActual, BorderLayout.CENTER);

        contenedorCentral.add(cardPrincipal, BorderLayout.CENTER);
        add(contenedorCentral, BorderLayout.CENTER);

        // --- SECCIÓN INFERIOR: HISTORIAL ---
        JPanel sectionHistorial = new JPanel(new BorderLayout());
        sectionHistorial.setOpaque(false);
        sectionHistorial.setPreferredSize(new Dimension(0, 180));

        JLabel lblTituloHistorial = new JLabel("Últimos llamados: ", SwingConstants.LEFT);
        lblTituloHistorial.setForeground(Color.WHITE);
        lblTituloHistorial.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTituloHistorial.setBorder(new EmptyBorder(20, 0, 15, 0));
        sectionHistorial.add(lblTituloHistorial, BorderLayout.NORTH);

        panelHistorial = new JPanel(new GridLayout(2, 2, 15, 15));
        panelHistorial.setOpaque(false);
        
        // Inicializamos con 4 vacíos
        for (int i = 0; i < 4; i++) {
            panelHistorial.add(crearTarjetaTurno(i + 1, "---"));
        }
        
        sectionHistorial.add(panelHistorial, BorderLayout.CENTER);
        add(sectionHistorial, BorderLayout.SOUTH);
    }


    private JPanel crearTarjetaTurno(int orden, String dni) {
        JPanel p = new JPanel(new BorderLayout(25, 0));
        p.setBackground(COLOR_TARJETA_SECUNDARIA);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1),
                new EmptyBorder(15, 30, 15, 30)
        ));
        
        JLabel lblNum = new JLabel(String.valueOf(orden));
        lblNum.setForeground(new Color(96, 165, 250)); // Azul brillante para el número
        lblNum.setFont(new Font("SansSerif", Font.BOLD, 26));
        
        JLabel lblDni = new JLabel(dni);
        lblDni.setForeground(Color.WHITE);
        lblDni.setFont(new Font("SansSerif", Font.BOLD, 30));

        p.add(lblNum, BorderLayout.WEST);
        p.add(lblDni, BorderLayout.CENTER);
        return p;
    }

    /**
     * Actualiza la UI usando objetos reales Turno.
     */
    public void actualizarPantalla(final Turno actual, final List<Turno> historial) {
        SwingUtilities.invokeLater(() -> {
            lblTurnoActual.setText(actual.getDniCliente());
            
            panelHistorial.removeAll();
            // Llenamos las 4 tarjetas del historial
            for (int i = 0; i < historial.size() && i < 4; i++) {
                panelHistorial.add(crearTarjetaTurno(i + 1, historial.get(i).getDniCliente()));
            }
            // Si hay menos de 4, rellenamos con guiones
            for (int i = historial.size(); i < 4; i++) {
                panelHistorial.add(crearTarjetaTurno(i + 1, "---"));
            }
            
            panelHistorial.revalidate();
            panelHistorial.repaint();
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new VentanaMonitorVisualizacion().setVisible(true));
    }
}