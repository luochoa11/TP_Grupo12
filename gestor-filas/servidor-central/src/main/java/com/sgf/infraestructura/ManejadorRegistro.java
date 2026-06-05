package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.modelos.Turno;

/**
 * Atiende las conexiones de las Terminales de Registro.
 */
public class ManejadorRegistro extends ManejadorBase {

    public ManejadorRegistro(Socket socket, ObjectInputStream in, ObjectOutputStream out, 
                             ILogicaFila logica, ServidorCentral servidor) {
        super(socket, in, out, logica, servidor);
    }

    @Override
    public void run() {
        try {
            String comando = (String) in.readObject();
            
            if ("NUEVO_TURNO".equals(comando)) {
                Turno t = (Turno) in.readObject();
                
                desencriptarTurno(t);
                
                try {
                    logica.agregarTurno(t);
                    servidor.sincronizarEstado();
                    out.writeObject("OK");
                } catch (DNIRepetidoException e) {
                    out.writeObject("ERROR_DNI_REPETIDO");
                }
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("[ManejadorRegistro] Error de red: " + e.getMessage());
        } finally {
            cerrarConexiones();
        }
    }
}
