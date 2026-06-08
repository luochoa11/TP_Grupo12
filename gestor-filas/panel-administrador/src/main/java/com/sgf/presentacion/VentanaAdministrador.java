package com.sgf.presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Interfaz gráfica para el Panel del Administrador.
 */
public class VentanaAdministrador extends JFrame {

    private static final long serialVersionUID = 1L;

    // Elementos de Persistencia (Abstract Factory)
    private JComboBox<String> comboPersistencia;
    private JButton btnAplicarPersistencia;
    private JLabel lblPersistenciaEstadoActual;

    // Elementos de Seguridad (Strategy)
    private JComboBox<String> comboAlgoritmo;
    private JTextField txtClaveSecreta;
    private JButton btnAplicarSeguridad;
    private JLabel lblCifradoEstadoActual;

    private ControladorAdministrador controlador;

    // Estado local para control de cambios dinámicos en caliente (UX)
    private String persistenciaActiva = "";
    private String algoritmoActivo = "";
    private String claveActiva = "";

    // Paleta de colores de alto contraste: Midnight Navy, Cyber Sky & Bright White
    private final Color COLOR_FONDO = new Color(11, 15, 26);           
    private final Color COLOR_TARJETA = new Color(22, 28, 45);         
    private final Color COLOR_ACCENTO_PERSISTENCIA = new Color(45, 212, 191); 
    private final Color COLOR_ACCENTO_SEGURIDAD = new Color(56, 189, 248);   
    private final Color COLOR_TEXTO_TITULO = Color.WHITE;               
    private final Color COLOR_TEXTO_DESCRIP = new Color(226, 232, 240);  
    private final Color COLOR_TEXTO_MUTED = new Color(148, 163, 184);   
    private final Color COLOR_BORDE = new Color(51, 65, 85);            

    // Colores para estados de botones
    private final Color COLOR_BTN_DESACTIVADO_BG = new Color(30, 41, 59);    
    private final Color COLOR_BTN_DESACTIVADO_FG = new Color(100, 116, 139);  
    private final Color COLOR_BTN_DESACTIVADO_BORDE = new Color(71, 85, 105); 

    public VentanaAdministrador() {
        setTitle("Panel de Administrador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(880, 480));

        JPanel contentPane = new JPanel(new BorderLayout(0, 20));
        contentPane.setBackground(COLOR_FONDO);
        contentPane.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(contentPane);

        initUI(contentPane);

        // Inicialización preventiva local: evita que el panel aparezca en blanco mientras conecta la red
        actualizarMonitoreo("JSON", "AES", "SeguridadSGF2026");
    }

    private void initUI(JPanel container) {
        // --- CABECERA ---
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
        panelHeader.setOpaque(false);
        panelHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblTitulo = new JLabel("CONFIGURACIÓN CENTRAL DEL SERVIDOR", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(COLOR_TEXTO_TITULO);

        JLabel lblSubtitulo = new JLabel("Establecer el formato de almacenamiento del sistema y los protocolos para la protección de los datos.", SwingConstants.LEFT);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(COLOR_TEXTO_DESCRIP);

        panelHeader.add(lblTitulo);
        panelHeader.add(Box.createVerticalStrut(6)); // Corrección del bug de compilación de createVerticalStrStrut
        panelHeader.add(lblSubtitulo);
        container.add(panelHeader, BorderLayout.NORTH);

        // --- PANEL CENTRAL ---
        JPanel panelCuerpo = new JPanel(new GridLayout(1, 2, 35, 0));
        panelCuerpo.setOpaque(false);

        panelCuerpo.add(crearPanelPersistencia());
        panelCuerpo.add(crearPanelSeguridad());

        container.add(panelCuerpo, BorderLayout.CENTER);
    }

    private JPanel crearPanelPersistencia() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(COLOR_TARJETA);
        p.setBorder(crearBordeSeccion("ALMACENAMIENTO DE DATOS", COLOR_ACCENTO_PERSISTENCIA));

        JPanel central = new JPanel();
        central.setLayout(new BoxLayout(central, BoxLayout.Y_AXIS));
        central.setOpaque(false);

        JLabel desc = new JLabel("<html>Seleccione el formato de almacenamiento persistente del servidor central. Al aplicar, se migrarán los datos en caliente.</html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(COLOR_TEXTO_DESCRIP);
        desc.setAlignmentX(LEFT_ALIGNMENT);

        String[] formatos = {"JSON (Archivo estructurado)", "XML (Esquema definido)", "TXT (Texto Plano / Log)"};
        comboPersistencia = crearComboboxEstilizado(formatos, COLOR_ACCENTO_PERSISTENCIA);
        comboPersistencia.addActionListener(e -> evaluarCambiosPersistencia());

        lblPersistenciaEstadoActual = new JLabel("Formato activo en servidor: ---");
        lblPersistenciaEstadoActual.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPersistenciaEstadoActual.setForeground(COLOR_ACCENTO_PERSISTENCIA);
        lblPersistenciaEstadoActual.setAlignmentX(LEFT_ALIGNMENT);

        central.add(desc);
        central.add(Box.createVerticalStrut(18));
        central.add(comboPersistencia);
        central.add(Box.createVerticalStrut(18));
        central.add(lblPersistenciaEstadoActual);

        btnAplicarPersistencia = crearBotonEstilizado("Aplicar Formato de Almacenamiento");
        actualizarEstadoVisualBoton(btnAplicarPersistencia, false, COLOR_ACCENTO_PERSISTENCIA);
        
        btnAplicarPersistencia.addActionListener(e -> {
            if (controlador != null) {
                String select = (String) comboPersistencia.getSelectedItem();
                String formatoAbreviado = extraerFormatoAbrev(select);
                controlador.modificarPersistencia(formatoAbreviado);
            }
        });

        p.add(central, BorderLayout.CENTER);
        p.add(btnAplicarPersistencia, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearPanelSeguridad() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(COLOR_TARJETA);
        p.setBorder(crearBordeSeccion("POLÍTICA DE CONFIDENCIALIDAD", COLOR_ACCENTO_SEGURIDAD));

        JPanel central = new JPanel();
        central.setLayout(new BoxLayout(central, BoxLayout.Y_AXIS));
        central.setOpaque(false);

        JLabel lblAlgo = new JLabel("Estrategia de Cifrado Simétrico:");
        lblAlgo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAlgo.setForeground(COLOR_TEXTO_TITULO);
        lblAlgo.setAlignmentX(LEFT_ALIGNMENT);

        String[] algoritmos = {"AES-128 (Estándar recomendado)", "XOR (Sencillo / Rápido)", "TripleDES (Compatibilidad)"};
        comboAlgoritmo = crearComboboxEstilizado(algoritmos, COLOR_ACCENTO_SEGURIDAD);
        comboAlgoritmo.addActionListener(e -> evaluarCambiosSeguridad());

        JLabel lblClave = new JLabel("Clave Secreta Compartida:");
        lblClave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblClave.setForeground(COLOR_TEXTO_TITULO);
        lblClave.setAlignmentX(LEFT_ALIGNMENT);

        txtClaveSecreta = new JTextField();
        txtClaveSecreta.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtClaveSecreta.setBackground(COLOR_FONDO);
        txtClaveSecreta.setForeground(COLOR_TEXTO_TITULO);
        txtClaveSecreta.setCaretColor(COLOR_ACCENTO_SEGURIDAD);
        txtClaveSecreta.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDE, 1), new EmptyBorder(6, 12, 6, 12)));
        txtClaveSecreta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtClaveSecreta.setAlignmentX(LEFT_ALIGNMENT);

        txtClaveSecreta.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { evaluarCambiosSeguridad(); }
            @Override public void removeUpdate(DocumentEvent e) { evaluarCambiosSeguridad(); }
            @Override public void changedUpdate(DocumentEvent e) { evaluarCambiosSeguridad(); }
        });

        lblCifradoEstadoActual = new JLabel("Algoritmo activo: ---");
        lblCifradoEstadoActual.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCifradoEstadoActual.setForeground(COLOR_ACCENTO_SEGURIDAD);
        lblCifradoEstadoActual.setAlignmentX(LEFT_ALIGNMENT);

        central.add(lblAlgo);
        central.add(Box.createVerticalStrut(6));
        central.add(comboAlgoritmo);
        central.add(Box.createVerticalStrut(15));
        central.add(lblClave);
        central.add(Box.createVerticalStrut(6));
        central.add(txtClaveSecreta);
        central.add(Box.createVerticalStrut(15));
        central.add(lblCifradoEstadoActual);

        btnAplicarSeguridad = crearBotonEstilizado("Establecer Clave & Algoritmo");
        actualizarEstadoVisualBoton(btnAplicarSeguridad, false, COLOR_ACCENTO_SEGURIDAD);

        btnAplicarSeguridad.addActionListener(e -> {
            if (controlador != null) {
                String select = (String) comboAlgoritmo.getSelectedItem();
                String algoAbrev = extraerAlgoAbrev(select);
                controlador.modificarSeguridad(algoAbrev, txtClaveSecreta.getText());
            }
        });

        p.add(central, BorderLayout.CENTER);
        p.add(btnAplicarSeguridad, BorderLayout.SOUTH);
        return p;
    }

    private JComboBox<String> crearComboboxEstilizado(String[] items, Color accentColor) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(COLOR_TARJETA);
        combo.setForeground(COLOR_TEXTO_TITULO);
        combo.setBorder(new LineBorder(COLOR_BORDE, 1));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        combo.setAlignmentX(LEFT_ALIGNMENT);
        combo.setFocusable(false); // Quita el molesto reborde de foco azul nativo de la caja

        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("▼") {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(COLOR_TARJETA);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(accentColor);
                        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        FontMetrics fm = g.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth("▼")) / 2;
                        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                        g.drawString("▼", x, y);
                    }
                };
                button.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDE));
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(COLOR_TARJETA);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        combo.setRenderer(new ListCellRenderer<String>() {
            private final JLabel label = new JLabel();
            {
                label.setOpaque(true);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }

            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                label.setText(value);
                label.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

                if (index == -1) {
                    label.setBackground(COLOR_TARJETA);
                    label.setForeground(COLOR_TEXTO_TITULO);
                } else {
                    if (isSelected) {
                        // Sombreado translúcido sumamente sutil (15% de opacidad) que actúa como un hover refinado
                        label.setBackground(new Color(
                            accentColor.getRed(),
                            accentColor.getGreen(),
                            accentColor.getBlue(),
                            40 // Alpha de 40 sobre 255 (Aproximadamente 15% opacidad)
                        ));
                        label.setForeground(accentColor); // Texto del color de acento para excelente legibilidad y estética
                    } else {
                        label.setBackground(COLOR_TARJETA);
                        label.setForeground(COLOR_TEXTO_DESCRIP);
                    }
                }
                return label;
            }
        });

        return combo;
    }

    private JButton crearBotonEstilizado(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(true);
        b.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    private void actualizarEstadoVisualBoton(JButton btn, boolean habilitado, Color colorActivo) {
        btn.setEnabled(habilitado);
        if (habilitado) {
            btn.setBackground(colorActivo);
            btn.setForeground(COLOR_FONDO);
            btn.setBorder(new LineBorder(colorActivo, 1));
        } else {
            btn.setBackground(COLOR_BTN_DESACTIVADO_BG);
            btn.setForeground(COLOR_BTN_DESACTIVADO_FG);
            btn.setBorder(new LineBorder(COLOR_BTN_DESACTIVADO_BORDE, 1));
        }
    }

    private void evaluarCambiosPersistencia() {
        String seleccionado = (String) comboPersistencia.getSelectedItem();
        if (seleccionado == null) return;
        String seleccionadoAbrev = extraerFormatoAbrev(seleccionado);

        boolean cambioDetectado = !seleccionadoAbrev.equalsIgnoreCase(persistenciaActiva);
        actualizarEstadoVisualBoton(btnAplicarPersistencia, cambioDetectado, COLOR_ACCENTO_PERSISTENCIA);
    }

    private void evaluarCambiosSeguridad() {
        String seleccionadoAlgo = (String) comboAlgoritmo.getSelectedItem();
        if (seleccionadoAlgo == null) return;
        String seleccionadoAlgoAbrev = extraerAlgoAbrev(seleccionadoAlgo);
        String claveEscrita = txtClaveSecreta.getText();

        boolean cambioAlgo = !seleccionadoAlgoAbrev.equalsIgnoreCase(algoritmoActivo);
        boolean cambioClave = !claveEscrita.equals(claveActiva);

        actualizarEstadoVisualBoton(btnAplicarSeguridad, (cambioAlgo || cambioClave), COLOR_ACCENTO_SEGURIDAD);
    }

    private String extraerFormatoAbrev(String item) {
        if (item.contains("XML")) return "XML";
        if (item.contains("TXT")) return "TXT";
        return "JSON";
    }

    private String extraerAlgoAbrev(String item) {
        if (item.contains("XOR")) return "XOR";
        if (item.contains("TripleDES")) return "DES";
        return "AES";
    }

    /**
     * Sincroniza la vista estrictamente con los parámetros de configuración.
     * @param formato Tipo de persistencia activo ("JSON", "XML", "TXT")
     * @param algoritmo Estrategia criptográfica activa
     * @param clave Clave simétrica compartida activa
     */
    public void actualizarMonitoreo(String formato, String algoritmo, String clave) {
        this.persistenciaActiva = formato;
        this.algoritmoActivo = algoritmo;
        this.claveActiva = clave;

        SwingUtilities.invokeLater(() -> {
            lblPersistenciaEstadoActual.setText("Formato activo en servidor: " + formato);
            lblCifradoEstadoActual.setText("Algoritmo activo: " + algoritmo);

            seleccionarComboPersistencia(formato);
            seleccionarComboAlgoritmo(algoritmo);

            if (!txtClaveSecreta.hasFocus() && !btnAplicarSeguridad.isEnabled()) {
                txtClaveSecreta.setText(clave);
            }

            evaluarChangesAdicionales();
        });
    }

    private void evaluarChangesAdicionales() {
        evaluarCambiosPersistencia();
        evaluarCambiosSeguridad();
    }

    private void seleccionarComboPersistencia(String formato) {
        if (comboPersistencia == null) return;
        for (int i = 0; i < comboPersistencia.getItemCount(); i++) {
            String item = comboPersistencia.getItemAt(i);
            if (item.toUpperCase().contains(formato.toUpperCase())) {
                comboPersistencia.setSelectedIndex(i);
                break;
            }
        }
    }

    private void seleccionarComboAlgoritmo(String algoritmo) {
        if (comboAlgoritmo == null) return;
        for (int i = 0; i < comboAlgoritmo.getItemCount(); i++) {
            String item = comboAlgoritmo.getItemAt(i);
            if (item.toUpperCase().contains(algoritmo.toUpperCase())) {
                comboAlgoritmo.setSelectedIndex(i);
                break;
            }
        }
    }

    private CompoundBorder crearBordeSeccion(String titulo, Color colorAcce) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                " " + titulo + " ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                colorAcce
        );
        return new CompoundBorder(tb, new EmptyBorder(20, 20, 20, 20));
    }

    public void setControlador(ControladorAdministrador controlador) {
        this.controlador = controlador;
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Administración de Servidor", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra una ventana de advertencia de seguridad con un icono de peligro.
     * Utilizado para alertar la necesidad de un reinicio sistémico.
     * @param mensaje Explicación técnica de la acción requerida.
     */
    public void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia de Reinicio de Sesión", JOptionPane.WARNING_MESSAGE);
    }
}