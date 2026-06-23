package com.sgf.disponibilidad;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.infraestructura.ServidorCentral;
import com.sgf.modelos.Turno;

/**
 * Clase que envía datos del servidor primario al servidor secundario para mantenerlos 
 * sincronizados en caso de falla del primario.
 */
public class SincronizadorEstado {

    private final ILogicaFila logica;
    private final String directorioIp;
    private final int    directorioPuerto;
    private ServidorCentral servidor;

    public SincronizadorEstado(ILogicaFila logica, String directorioIp, int directorioPuerto) {
        this.logica           = logica;
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
    }

    public void setServidor(ServidorCentral servidor) {
        this.servidor = servidor;
    }

    private String[] resolverSecundario() {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_RUTA_SECUNDARIA");
            out.flush();

            String ip     = (String) in.readObject();
            int    puerto = (int)    in.readObject();

            System.out.println("[Sync] Secundario resuelto -> " + ip + ": " + puerto);

            if (ip == null) return null;
            return new String[]{ip, String.valueOf(puerto)};

        } catch (Exception e) {
            System.err.println("[Sync] No se pudo consultar el Directorio: " + e.getMessage());
            return null;
        }
    }

    public void sincronizar() {
        String[] secundario = resolverSecundario();
        if (secundario == null) {
            System.out.println("[Sync] No hay secundario registrado.");
            return;
        }

        String ip     = secundario[0];
        int    puerto = Integer.parseInt(secundario[1]);

        try (Socket socket = new Socket(ip, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("SYNC_SERVER");
            out.flush(); 

            out.writeObject("SINCRONIZAR_ESTADO");
            out.writeObject(logica.getCola());
            out.writeObject(logica.getTurnosActivos());
            out.writeObject(logica.getHistorial());
            out.writeObject(logica.getUltimoLlamado());
            out.writeObject(logica.getHistorialReintentos());
            
            if (servidor != null && servidor.getFachada() != null) {
                out.writeObject(servidor.getFachada().getFormatoPersistenciaActivo());
                out.writeObject(servidor.getFachada().getAlgoritmoCifradoActivo());
                out.writeObject(servidor.getFachada().getClaveSecretaActiva());
            } else {
                out.writeObject("JSON");
                out.writeObject("AES");
                out.writeObject("");
            }
            out.flush();

            
            System.out.println("[Sync] Estado actual, formato de persistencia y seguridad sincronizado -> " + ip + ":" + puerto);

        } catch (Exception e) {
            System.err.println("[Sync] Error al sincronizar estado completo: " + e.getMessage());
        }
    }

    /**
     * Sincronización Incremental (Delta Sync)
     * Envía únicamente la mutación de estado ocurrida para ahorrar ancho de banda.
     */
    public void sincronizarDelta(ActualizacionEstadoDTO delta) {
        String[] secundario = resolverSecundario();
        if (secundario == null) {
            System.out.println("[Sync] No hay secundario registrado.");
            return;
        }

        String ip     = secundario[0];
        int    puerto = Integer.parseInt(secundario[1]);

        try (Socket socket = new Socket(ip, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("SYNC_SERVER");
            out.flush(); 

            out.writeObject("NUEVO_DELTA");
            out.writeObject(delta);
            out.flush();

            System.out.println("[Sync] Delta enviado con éxito: " + delta);

        } catch (Exception e) {
            System.err.println("[Sync] Error al replicar delta: " + e.getMessage());
        }
    }

    /**
     * Transmite un cambio de formato de persistencia en caliente al secundario.
    */
    public void sincronizarFormatoPersistencia(String nuevoFormato) { 
        String[] secundario = resolverSecundario();
        if (secundario == null) return;

        String ip     = secundario[0];
        int    puerto = Integer.parseInt(secundario[1]);

        try (Socket socket = new Socket(ip, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) { 
            
            out.writeObject("SYNC_SERVER");
            out.flush(); 

            out.writeObject("ACTUALIZAR_PERSISTENCIA");
            out.writeObject(nuevoFormato);
            out.flush();

            System.out.println("[Sync] Cambio de persistencia (" + nuevoFormato + ") replicado al secundario.");

        } catch (Exception e) {
            System.err.println("[Sync] Error al replicar cambio de persistencia: " + e.getMessage());
        }
    }

    public void sincronizarHistoricoDelta() {
        if (servidor == null) return;
        List<Turno> pendientes = servidor.getHistoricoPendienteSync();
        if (pendientes.isEmpty()) return;

        String[] secundario = resolverSecundario();
        if (secundario == null) return;

        String ip     = secundario[0];
        int    puerto = Integer.parseInt(secundario[1]);

        try (Socket socket = new Socket(ip, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject("SYNC_SERVER");
            out.flush();

            out.writeObject("SINCRONIZAR_HISTORICO_DELTA");
            out.writeObject(new ArrayList<>(pendientes));
            out.flush();

            System.out.println("[Sync] " + pendientes.size() + " turnos históricos pendientes replicados al secundario.");
            servidor.limpiarHistoricoPendiente();

        } catch (Exception e) {
            System.err.println("[Sync] Error al replicar histórico pendiente: " + e.getMessage());
        }
    }

    /**
    * Transmite un cambio de configuración de seguridad en caliente al secundario.
    */
    public void sincronizarSeguridad(String algoritmo, String clave) {
        String[] secundario = resolverSecundario();
        if (secundario == null) return;

        String ip     = secundario[0];
        int    puerto = Integer.parseInt(secundario[1]);

        try (Socket socket = new Socket(ip, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("SYNC_SERVER");
            out.flush(); 

            out.writeObject("ACTUALIZAR_SEGURIDAD");
            out.writeObject(algoritmo);
            out.writeObject(clave);
            out.flush();

            System.out.println("[Sync] Cambio de seguridad (" + algoritmo + ") replicado al secundario.");

        } catch (Exception e) {
            System.err.println("[Sync] Error al replicar cambio de seguridad: " + e.getMessage());
        }
    }
}