package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.interfaces.IServicioAdministrador;

/**
 * Hilo del servidor encargado de atender las peticiones de red del Administrador.
 * Traduce el flujo de Sockets a llamadas lógicas sobre el patrón Facade.
 */

public class ManejadorAdministrador extends ManejadorBase implements Runnable {

    private final IServicioAdministrador fachadaServidor; // Fachada inyectada localmente

    public ManejadorAdministrador(Socket socket, ObjectInputStream in, ObjectOutputStream out, 
                                ILogicaFila logica, ServidorCentral servidor, IServicioAdministrador fachada) {
        super(socket, in, out, logica, servidor); 
        this.fachadaServidor = fachada;
    }

    @Override
    public void run() {
        try {
            String comando = (String) in.readObject();

            switch (comando) {
                case "CAMBIAR_PERSISTENCIA":
                    String formato = (String) in.readObject();
                    // Invoca al método de la fachada
                    boolean exitoP = fachadaServidor.cambiarFormatoPersistencia(formato);
                    out.writeObject(exitoP);
                    break;

                case "GET_PERSISTENCIA":
                    out.writeObject(fachadaServidor.getFormatoPersistenciaActivo());
                    break;

                case "ACTUALIZAR_SEGURIDAD":
                    String algoritmo = (String) in.readObject();
                    String clave = (String) in.readObject();
                    
                    boolean exitoS = fachadaServidor.actualizarConfiguracionSeguridad(algoritmo, clave);
                    out.writeObject(exitoS);
                    break;

                case "GET_ALGORITMO":
                    out.writeObject(fachadaServidor.getAlgoritmoCifradoActivo());
                    break;

                case "GET_CLAVE":
                    out.writeObject(fachadaServidor.getClaveSecretaActiva());
                    break;
                
                case "GET_CONFIG_COMPLETA":
                    // Escribe secuencialmente la configuración consolidada para optimizar la red
                    String[] config = fachadaServidor.obtenerConfiguracionCompleta();
                    out.writeObject(config);
                    break;
                
                case "GET_ALGORITMOS":
                out.writeObject(fachadaServidor.getAlgoritmosDisponibles());
                break;

                default:
                    System.err.println("[ManejadorAdmin] Comando desconocido recibido: " + comando);
                    out.writeObject(false);
                    break;
            }
            out.flush();

        } catch (Exception e) {
            System.err.println("[ManejadorAdmin] Error procesando operación de administración: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }
}