package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.disponibilidad.ActualizacionEstadoDTO;
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
            
            switch (comando){
                case "SINCRONIZAR_ESTADO":
                    // Sincronización completa al levantar
                    List<Turno> cola = (List<Turno>) in.readObject();
                    Map<Integer, Turno> activos = (Map<Integer, Turno>) in.readObject();
                    List<Turno> historial = (List<Turno>) in.readObject();
                    Turno ultimo = (Turno) in.readObject();
                    List<Turno> historialReintentos = (List<Turno>) in.readObject();
                                
                    logica.reemplazarEstado(cola, activos, historial, ultimo, historialReintentos);
                    System.out.println("[Sync] Estado recibido. Cola: " + cola.size() + " turnos.");
                    break;

                case "NUEVO_DELTA":
                    ActualizacionEstadoDTO delta = (ActualizacionEstadoDTO) in.readObject();
                    
                    switch (delta.getTipo()) {
                        case "REGISTRAR":
                            try {
                                logica.agregarTurno(delta.getTurno());
                            } catch (Exception e) {
                                System.out.println("[Sync] Turno ya registrado localmente, omitiendo.");
                            }
                            break;
                        
                        case "LLAMAR":
                            try {
                                logica.llamarSiguiente(delta.getIdPuesto());
                            } catch (Exception e) {
                                System.out.println("[Sync] Error al sincronizar llamado: Fila vacía en réplica.");
                            }
                            break;
                        
                        case "REINTENTAR":
                            logica.reintentarLlamado(delta.getIdPuesto());
                            logica.getHistorialReintentos().add(delta.getTurno().clonar());
                            break;
                    }
                    System.out.println("[Sync] Delta de tipo [" + delta.getTipo() + "] replicado con éxito.");
                        break;
            }
            out.flush();
        } catch (Exception e) {
            System.err.println("[ManejadorSincronizacion] Error en replicación: " + e.getMessage());
        } finally {
            cerrarConexiones();
        }
    }
}
