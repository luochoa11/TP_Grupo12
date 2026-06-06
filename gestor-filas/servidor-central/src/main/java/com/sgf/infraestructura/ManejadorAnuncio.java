package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.modelos.Turno;

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
                
                while (!socket.isClosed()) {
                    Thread.sleep(10000);
                }
            } else if ("GET_ESTADO_MONITOR".equals(comando)) {
                Turno actualCopia = servidor.copiarYEncriptar(logica.getUltimoLlamado());
                List<Turno> historialCopia = servidor.copiarYEncriptarLista(logica.getHistorial());
                
                out.writeObject(actualCopia);
                out.writeObject(historialCopia);
                out.flush();
                
                servidor.desencriptarTurno(actualCopia);
                servidor.desencriptarLista(historialCopia);
            }
        } catch (Exception e) {
            System.out.println("[ManejadorMonitor] Pantalla de sala desconectada.");
        } finally {
            cerrarConexiones();
        }
    }
}
