package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;

/**
 * Maneja las suscripciones persistentes de las Pantallas de Anuncios en sala de espera.
 */
public class ManejadorAnuncio extends ManejadorBase {

    public ManejadorAnuncio(Socket socket, ObjectInputStream in, ObjectOutputStream out, 
                            ILogicaFila logica, ServidorCentral servidor) {
        super(socket, in, out, logica, servidor);
    }

    @Override
    public void run() {
        try {
            String comando = (String) in.readObject();
            
            if ("SUSCRIBIR_MONITOR".equals(comando)) {
                servidor.agregarMonitor(this.out);
                
                // Mantiene el hilo activo para que el Socket no se cierre de inmediato
                while (!socket.isClosed()) {
                    Thread.sleep(10000);
                }
            } else if ("GET_ESTADO_MONITOR".equals(comando)) {
                out.writeObject(logica.getUltimoLlamado());
                out.writeObject(logica.getHistorial());
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("[ManejadorMonitor] Pantalla de sala desconectada.");
        } finally {
            cerrarConexiones();
        }
    }
}