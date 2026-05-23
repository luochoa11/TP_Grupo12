package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;

/**
 * Atiende las conexiones del Monitor de Salud (GestorFalla).
 * Procesa comandos críticos de la infraestructura distribuida.
 */
public class ManejadorMonitor extends ManejadorBase {

    public ManejadorMonitor(Socket socket, ObjectInputStream in, ObjectOutputStream out, 
                            ILogicaFila logica, ServidorCentral servidor) {
        super(socket, in, out, logica, servidor);
    }

    @Override
    public void run() {
        try {
            String comando = (String) in.readObject();
            
            if ("PROMOVER".equals(comando)) {
                System.out.println("[Servidor] Comando PROMOVER recibido desde el Monitor de Salud.");
                servidor.promoverEstado();
                out.writeObject("OK");
                out.flush();
            } else {
                System.err.println("[ManejadorMonitor] Comando desconocido: " + comando);
            }
        } catch (Exception e) {
            System.err.println("[ManejadorMonitor] Error al procesar promoción de salud: " + e.getMessage());
        } finally {
            cerrarConexiones();
        }
    }
}