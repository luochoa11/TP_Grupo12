package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.interfaces.IServicioAdministrador;

/**
 * Proxy que implementa IServicioAdministrador para delegar las operaciones administrativas.
 * Implementa Failover Dinámico interceptando rechazos del Servidor Secundario.
 */
public class ProxyAdministrador implements IServicioAdministrador {
    
    private final String directorioIp;
    private final int directorioPuerto;

    private String ipServidor;
    private int puertoServidor;
    
    private final int MAX_INTENTOS = 3;

    public ProxyAdministrador(String directorioIp, int directorioPuerto) {
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
        resolverServidor();
    }

    private Socket conectarServidor() throws Exception {
        return new Socket(ipServidor, puertoServidor);
    }

    /**
     * Consulta al Directorio para conocer la IP y Puerto del Servidor Primario activo
     */
    private void resolverServidor() {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_RUTA_PRIMARIA");
            out.flush();

            this.ipServidor     = (String) in.readObject();
            this.puertoServidor = (int)    in.readObject();

            System.out.println("[ProxyAdministrador] Servidor resuelto -> " + ipServidor + ":" + puertoServidor);

        } catch (Exception e) {
            System.err.println("[ProxyAdministrador] No se pudo resolver el Servidor desde el Directorio: " + e.getMessage());
        }
    }

    /**
     * Motor central de peticiones. 
     * Encapsula la reconexión y el manejo de rutas obsoletas (Stale Routes).
     */
    private Object enviarComandoTransaccional(String comando, Object... args) throws Exception {
        int intentoActual = 1;
        
        while (intentoActual <= MAX_INTENTOS) {
            try (Socket socket = conectarServidor();
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
                
                out.writeObject("CLIENTE_ADMINISTRADOR");
                out.flush();

                out.writeObject(comando);
                for (Object arg : args) {
                    out.writeObject(arg);
                }
                out.flush();

                Object respuesta = in.readObject();

                if (respuesta instanceof String && "ERROR_SERVIDOR_SECUNDARIO".equals(respuesta)) {
                    System.out.println("[ProxyAdministrador] Conectado a Secundario obsoleto. Refrescando IP...");
                    resolverServidor(); 
                    intentoActual++;
                    continue; // Repetimos el ciclo while sin lanzar error a la pantalla
                }

                return respuesta;

            } catch (Exception e) {
                System.out.println("[ProxyAdministrador] Fallo de red (Intento " + intentoActual + "). Reconectando...");
                resolverServidor();
                intentoActual++;
                if (intentoActual <= MAX_INTENTOS) {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                }
            }
        }
        throw new Exception("No se pudo completar la operación tras múltiples intentos.");
    }


    // ===================================================================================
    // MÉTODOS DE LA FACHADA
    // ===================================================================================

    @Override
    public boolean cambiarFormatoPersistencia(String tipoFormato) {
        try {
            return (boolean) enviarComandoTransaccional("CAMBIAR_PERSISTENCIA", tipoFormato);
        } catch (Exception e) {
            System.err.println("[ProxyAdministrador] Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getFormatoPersistenciaActivo() {
        try {
            return (String) enviarComandoTransaccional("GET_PERSISTENCIA");
        } catch (Exception e) {
            return "ERROR";
        }
    }

    @Override
    public boolean actualizarConfiguracionSeguridad(String algoritmo, String claveSecreta) {
        try {
            return (boolean) enviarComandoTransaccional("ACTUALIZAR_SEGURIDAD", algoritmo, claveSecreta);
        } catch (Exception e) {
            System.err.println("[ProxyAdministrador] Error de seguridad: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getAlgoritmoCifradoActivo() {
        try {
            return (String) enviarComandoTransaccional("GET_ALGORITMO");
        } catch (Exception e) {
            return "ERROR";
        }
    }

    @Override
    public String getClaveSecretaActiva() {
        try {
            return (String) enviarComandoTransaccional("GET_CLAVE");
        } catch (Exception e) {
            return "ERROR";
        }
    }

    @Override
    public String[] obtenerConfiguracionCompleta() {
        try {
            return (String[]) enviarComandoTransaccional("GET_CONFIG_COMPLETA");
        } catch (Exception e) {
            System.err.println("[ProxyAdministrador] Error en consulta consolidada: " + e.getMessage());
            return new String[] {"JSON", "AES", ""}; 
        }
    }

    @Override
    public String[] getAlgoritmosDisponibles() {
        try {
            return (String[]) enviarComandoTransaccional("GET_ALGORITMOS");
        } catch (Exception e) {
            return new String[] { "AES", "DES", "XOR" }; 
        }
    }
}