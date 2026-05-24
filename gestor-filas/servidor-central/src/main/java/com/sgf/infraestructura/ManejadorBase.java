package com.sgf.infraestructura;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;

/**
 * Clase base para todos los manejadores del sistema.
 * Guarda las referencias a los Sockets y Streams abiertos en el Servidor Central.
 */

public abstract class ManejadorBase implements Runnable {
    protected final Socket socket;
    protected ObjectInputStream in;
    protected ObjectOutputStream out;
    protected final ILogicaFila logica;
    protected final ServidorCentral servidor;

    public ManejadorBase(Socket socket, ObjectInputStream in, ObjectOutputStream out, 
                        ILogicaFila logica, ServidorCentral servidor) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.logica = logica;
        this.servidor = servidor;
    }

    /**
     * Cierra de forma segura la conexión y los flujos al terminar la ejecución.
     */
    protected void cerrarConexiones() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[Manejador] Error al cerrar socket: " + e.getMessage());
        }
    }
}