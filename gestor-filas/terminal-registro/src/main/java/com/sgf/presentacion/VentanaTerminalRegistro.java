package com.sgf.presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.sgf.infraestructura.ClienteRegistro;

public class VentanaTerminalRegistro extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JPanel panelCentral;
    private JPanel panelSaludo;
    private JLabel lblBienvenida;
    private JPanel panelIngreso;
    private JPanel panelTexto;
    private JPanel panelTeclado;
    private JTextField textDNI;
    private JButton btnIngresar;
    private JPanel panelDNI;
    private JPanel panelBotonIngreso;
    private JButton btnUno;
    private JButton btnDos;
    private JButton btnTres;
    private JButton btnCuatro;
    private JButton btnCinco;
    private JButton btnSeis;
    private JButton btnSiete;
    private JButton btnOcho;
    private JButton btnNueve;
    private JButton btnCero;
    private JButton btnBorrar;
    private JLabel lblBienvenida2;
    private JPanel panel_1;
    private JPanel panel_2;
    private JButton btnLimpiar;
    
    private ControladorRegistro controlador;

    private final Color COLOR_FONDO = new Color(15, 23, 42);
    private final Color COLOR_BOTON = new Color(30, 41, 59);
    private final Color COLOR_TEXTO_ACCENTO = new Color(96, 165, 250);
    
    public void setControlador(ControladorRegistro controlador) {
        this.controlador = controlador;
        
        configurarBotones();
        btnBorrar.addActionListener(e ->controlador.borrarUltimo());
        btnLimpiar.addActionListener(e-> controlador.limpiar());
        btnIngresar.addActionListener(e -> controlador.ingresarDNI());
    }

    public String getDNI() {
        return textDNI.getText();
    }

    public void setDNI(String dni) {
        textDNI.setText(dni);
    }

    private void configurarBotones() {    // define la funcion de cada boton numerico
        JButton[] botones = {
            btnUno, btnDos, btnTres,
            btnCuatro, btnCinco, btnSeis,
            btnSiete, btnOcho, btnNueve,
            btnCero
        };

        for (JButton btn : botones) {
            btn.addActionListener(e -> {
                controlador.escribirNumero(btn.getText());
            });
        }
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }

    public VentanaTerminalRegistro(ClienteRegistro cliente) {
        setTitle("Terminal de Registro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

        initComponents();
    }

    private void initComponents() {
        this.contentPane = new JPanel(new GridBagLayout());
        this.contentPane.setBackground(COLOR_FONDO);
        this.contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(this.contentPane);

        this.panelCentral = new JPanel();
        this.panelCentral.setLayout(new BorderLayout(30, 30));
        this.panelCentral.setOpaque(false);
        this.panelCentral.setPreferredSize(new Dimension(1000, 600)); // Tamaño base del diseño
        this.contentPane.add(this.panelCentral);

        this.panelIngreso = new JPanel();
        this.panelIngreso.setLayout(new GridLayout(0, 2, 40, 0));
        this.panelIngreso.setOpaque(false);
        this.panelCentral.add(this.panelIngreso, BorderLayout.CENTER);

        this.panelTexto = new JPanel();
        this.panelTexto.setLayout(new BorderLayout(0, 20));
        this.panelTexto.setOpaque(false);
        this.panelIngreso.add(this.panelTexto);

        this.panelDNI = new JPanel();
        this.panelDNI.setLayout(new GridLayout(2, 1, 0, 20));
        this.panelDNI.setOpaque(false);
        this.panelTexto.add(this.panelDNI, BorderLayout.CENTER);

        this.panelSaludo = new JPanel();
        this.panelSaludo.setLayout(new GridLayout(0, 1, 0, 10));
        this.panelSaludo.setOpaque(false);        
        this.panelDNI.add(this.panelSaludo);

        this.lblBienvenida = new JLabel("¡Bienvenido!");
        this.lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 60));
        this.lblBienvenida.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblBienvenida.setForeground(Color.WHITE);
        this.panelSaludo.add(this.lblBienvenida);

        this.lblBienvenida2 = new JLabel("Ingrese su DNI para registrarse");
        this.lblBienvenida2.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblBienvenida2.setForeground(new Color(148, 163, 184));
        this.lblBienvenida2.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        this.panelSaludo.add(this.lblBienvenida2);

        this.panel_1 = new JPanel(new GridBagLayout());
        this.panel_1.setOpaque(false);
        this.panelDNI.add(this.panel_1);

        this.textDNI = new JTextField();
        this.textDNI.setFont(new Font("Segoe UI", Font.BOLD, 48));
        this.textDNI.setPreferredSize(new Dimension(350, 80));
        this.textDNI.setHorizontalAlignment(SwingConstants.CENTER);
        this.textDNI.setEditable(false);
        this.textDNI.setBackground(Color.WHITE);
        this.textDNI.setBorder(BorderFactory.createLineBorder(COLOR_TEXTO_ACCENTO, 2));
        this.panel_1.add(this.textDNI);

        this.panelBotonIngreso = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.panelBotonIngreso.setOpaque(false);
        this.panelTexto.add(this.panelBotonIngreso, BorderLayout.SOUTH);

        this.btnIngresar = new JButton("Ingresar");
        this.btnIngresar.setPreferredSize(new Dimension(300, 70));
        this.panelBotonIngreso.add(this.btnIngresar);

        this.panelTeclado = new JPanel();
        this.panelIngreso.add(this.panelTeclado);
        this.panelTeclado.setLayout(new GridLayout(4, 3, 15, 15));
        this.panelTeclado.setOpaque(false);

        this.btnUno = new JButton("1");
        this.panelTeclado.add(this.btnUno);

        this.btnDos = new JButton("2");
        this.panelTeclado.add(this.btnDos);

        this.btnTres = new JButton("3");
        this.panelTeclado.add(this.btnTres);

        this.btnCuatro = new JButton("4");
        this.panelTeclado.add(this.btnCuatro);

        this.btnCinco = new JButton("5");
        this.panelTeclado.add(this.btnCinco);

        this.btnSeis = new JButton("6");
        this.panelTeclado.add(this.btnSeis);

        this.btnSiete = new JButton("7");
        this.panelTeclado.add(this.btnSiete);

        this.btnOcho = new JButton("8");
        this.panelTeclado.add(this.btnOcho);

        this.btnNueve = new JButton("9");
        this.panelTeclado.add(this.btnNueve);
        
        this.btnLimpiar = new JButton("Limpiar");
        this.panelTeclado.add(this.btnLimpiar);

        this.btnCero = new JButton("0");
        this.panelTeclado.add(this.btnCero);

        this.btnBorrar = new JButton("←");
        this.panelTeclado.add(this.btnBorrar);
        
        this.panel_2 = new JPanel();
        this.panel_2.setPreferredSize(new Dimension(0, 20));
        this.panel_2.setOpaque(false);
        this.panelCentral.add(this.panel_2, BorderLayout.SOUTH);

        JButton[] botones = {
            btnUno, btnDos, btnTres,
            btnCuatro, btnCinco, btnSeis,
            btnSiete, btnOcho, btnNueve,
            btnCero, btnBorrar, btnLimpiar
        };

        Font fuenteNumeros = new Font("Segoe UI", Font.BOLD, 32);

        for (JButton b : botones) {
            b.setFont(fuenteNumeros);
            b.setFocusPainted(false);
            b.setBackground(COLOR_BOTON);
            b.setForeground(COLOR_TEXTO_ACCENTO);
            b.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        }

        this.btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 28));
        this.btnIngresar.setBackground(COLOR_TEXTO_ACCENTO);
        this.btnIngresar.setForeground(COLOR_FONDO);
        this.btnIngresar.setFocusPainted(false);
        this.btnIngresar.setBorderPainted(false);
        
        this.btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 18));

    }
    
}