package com.sgf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class VentanaPanelOperador extends JFrame {

    private JPanel contentPane;
    private JPanel panelCola;
    private JLabel lblActual;

    private LogicaFila logica;
    private ObjectOutputStream outMonitor;
    private ClienteSocket cliente;

    public VentanaPanelOperador(LogicaFila logica) {


        this.cliente = new ClienteSocket("localhost", Constantes.PUERTO_MONITOR1);
        this.logica = logica;

        setTitle("Panel Operador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 500));

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(new Color(15, 23, 42));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        initUI();
    }

    private void initUI() {

        // Turno actual
        lblActual = new JLabel("Actual: ---", SwingConstants.CENTER);
        lblActual.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblActual.setForeground(Color.WHITE);
        contentPane.add(lblActual, BorderLayout.NORTH);

        // Cola
        panelCola = new JPanel(new GridLayout(0, 1, 10, 10));
        panelCola.setBackground(new Color(15, 23, 42));
        contentPane.add(panelCola, BorderLayout.CENTER);

        // Botón llamar
        JButton btnLlamar = new JButton("Llamar siguiente");
        btnLlamar.addActionListener(e -> llamarSiguiente());
        contentPane.add(btnLlamar, BorderLayout.SOUTH);
    }

    private JPanel crearItemTurno(String dni) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(30, 41, 59));
        p.setBorder(new CompoundBorder(
                new LineBorder(Color.GRAY),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lbl = new JLabel(dni);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));

        p.add(lbl, BorderLayout.CENTER);
        return p;
    }


    public void actualizarVista() {
        SwingUtilities.invokeLater(() -> {

            // turno actual
            Turno actual = logica.getTurnoActual();
            lblActual.setText("Actual: " + (actual != null ? actual.getDniCliente() : "---"));

            // cola
            panelCola.removeAll();

            List<Turno> cola = logica.getCola(); 

            for (Turno t : cola) {
                panelCola.add(crearItemTurno(t.getDniCliente()));
            }

            panelCola.revalidate();
            panelCola.repaint();
        });
    }

    private void llamarSiguiente() {
    try {
        Turno t = logica.llamarSiguiente(); 
        cliente.enviarTurno(t); // Se envía al servidor

        if (t != null) {
            // actualizar UI
            actualizarVista();

            // enviar al monitor
            outMonitor.writeObject(t);
            outMonitor.writeObject(logica.getHistorial());
            outMonitor.flush();

            System.out.println("Enviado al monitor: " + t);
        } else {
            System.out.println("No hay turnos");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}