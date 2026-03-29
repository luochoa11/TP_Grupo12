package com.sgf;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;

public class VentanaTerminalRegistro extends JFrame {

    private JPanel contentPane;
    private JTextField textFieldDNI;

    public VentanaTerminalRegistro(ClienteSocket cliente) {
        

        setTitle("Terminal de Registro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 400, 300);
        
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        
        
        contentPane.setLayout(null); 
        
        JTextArea txtrIngresesuDni = new JTextArea();
        txtrIngresesuDni.setText("Ingrese su DNI");
        txtrIngresesuDni.setBounds(141, 24, 117, 22);
        contentPane.add(txtrIngresesuDni);
        
        textFieldDNI = new JTextField();
        textFieldDNI.setBounds(148, 64, 96, 20);
        contentPane.add(textFieldDNI);
        textFieldDNI.setColumns(10);
        
        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> {
            String dni = textFieldDNI.getText().trim();
            if (!dni.isEmpty()) {
                Turno t = new Turno(dni);
                cliente.enviarTurno(t); // Se envía al servidor
                 textFieldDNI.setText("");
              }
        });
        
        btnIngresar.setBounds(154, 102, 88, 22);
        contentPane.add(btnIngresar);
    }
}