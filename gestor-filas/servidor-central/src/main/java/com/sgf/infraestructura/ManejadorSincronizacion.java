package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.modelos.Turno;

/**
 * Atiende únicamente la replicación de datos en caliente inter-servidor.
 */
public class ManejadorSincronizacion extends ManejadorBase {

    public ManejadorSincronizacion(Socket socket, ObjectInputStream in, ObjectOutputStream out, 
                                ILogicaFila logica, ServidorCentral servidor) {
        super(socket, in, out, logica, servidor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            String comando = (String) in.readObject();
            
            if ("SINCRONIZAR_ESTADO".equals(comando)) {
                List<Turno> cola = (List<Turno>) in.readObject();
                Map<Integer, Turno> activos = (Map<Integer, Turno>) in.readObject();
                List<Turno> historial = (List<Turno>) in.readObject();
                Turno ultimo = (Turno) in.readObject();
                
                //desencriptarLista(cola);
                //desencriptarLista(historial);
                //desencriptarTurno(ultimo);
                
                // Como activos es un Map, lo recorremos a mano
                //if (activos != null) {
                //    for (Turno t : activos.values()) {
                //        desencriptarTurno(t);
                //    }
                //}
                
                logica.reemplazarEstado(cola, activos, historial, ultimo);
                System.out.println("[Sync] Estado recibido. Cola: " + cola.size() + " turnos.");
            }
            out.flush();
        } catch (Exception e) {
            System.err.println("[ManejadorSincronizacion] Error en replicación: " + e.getMessage());
        } finally {
            cerrarConexiones();
        }
    }
}
