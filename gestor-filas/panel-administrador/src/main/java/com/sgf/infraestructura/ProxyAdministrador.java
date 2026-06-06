package com.sgf.infraestructura;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.interfaces.IServicioAdministrador;
/**
 * Proxy que implementa IServicioAdministrador para delegar las operaciones administrativas
 * al Servidor Central. Este proxy actúa como intermediario, permitiendo al panel de administrador
 * interactuar con el servidor sin exponer detalles de implementación o comunicación.
 */
public class  ProxyAdministrador implements IServicioAdministrador{
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
            throw new RuntimeException("[ProxyAdministrador] No se pudo resolver el Servidor desde el Directorio: " + e.getMessage());
        }
    }

    private Socket conectarConFallback() throws Exception {
        int intentoActual = 1;
        while (intentoActual <= MAX_INTENTOS) {
            try {
                return conectarServidor();
            } catch (Exception e) {
                System.out.println("[ProxyAdministrador] Fallo de conexión (intento " + intentoActual + " de " + MAX_INTENTOS + ").");
                if (intentoActual < MAX_INTENTOS) {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                }
                intentoActual++;
            }
        }
        
        System.out.println("[ProxyAdministrador] Servidor no responde. Realizando failover mediante el Directorio...");
        try {
            resolverServidor();
        } catch (Exception ex) {
            System.out.println("[ProxyAdministrador] Error crítico al conectar con el directorio.");
        }

        try {
            return new Socket(ipServidor, puertoServidor);
        } catch (Exception e) {
            throw new Exception("Error de conexión definitivo: Ningún servidor de filas activo en la red.");
        }
    }

    @Override
    public boolean cambiarFormatoPersistencia(String tipoFormato) {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
            
            //envio identificacion
            out.writeObject("CLIENTE_ADMINISTRADOR");
            out.flush();

            //envio comando y argumento
            out.writeObject("CAMBIAR_PERSISTENCIA");
            out.writeObject(tipoFormato);
            out.flush();

            return (boolean) in.readObject();

        } catch (Exception e) {
            System.err.println("[ProxyAdministrador] Error al cambiar persistencia: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getFormatoPersistenciaActivo() {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject("CLIENTE_ADMINISTRADOR");
            out.flush();
            out.writeObject("GET_PERSISTENCIA");
            out.flush();

            return (String) in.readObject();
        } catch (Exception e) {
            return "ERROR";
        }
    }

    @Override
    public boolean actualizarConfiguracionSeguridad(String algoritmo, String claveSecreta) {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject("CLIENTE_ADMINISTRADOR");
            out.flush();

            out.writeObject("ACTUALIZAR_SEGURIDAD");
            out.writeObject(algoritmo);
            out.writeObject(claveSecreta);
            out.flush();

            return (boolean) in.readObject();

        } catch (Exception e) {
            System.err.println("[ProxyAdministrador] Error al actualizar seguridad: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getAlgoritmoCifradoActivo() {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject("CLIENTE_ADMINISTRADOR");
            out.flush();
            out.writeObject("GET_ALGORITMO");
            out.flush();

            return (String) in.readObject();
        } catch (Exception e) {
            return "ERROR";
        }
    }

    @Override
    public String getClaveSecretaActiva() {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject("CLIENTE_ADMINISTRADOR");
            out.flush();
            out.writeObject("GET_CLAVE");
            out.flush();

            return (String) in.readObject();
        } catch (Exception e) {
            return "ERROR";
        }
    }

    @Override
    public String[] obtenerConfiguracionCompleta() {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject("CLIENTE_ADMINISTRADOR");
            out.flush();

            out.writeObject("GET_CONFIG_COMPLETA");
            out.flush();

            return (String[]) in.readObject();
        } catch (Exception e) {
            System.err.println("[ProxyAdministrador] Error en consulta: " + e.getMessage());
            return new String[] {"JSON", "AES-128", "SeguridadSGF2026"}; // Fallback seguro
        }
    }

    @Override
    public String[] getAlgoritmosDisponibles() {
    try (Socket socket = conectarConFallback();
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
        
        out.writeObject("CLIENTE_ADMINISTRADOR");
        out.flush();
        out.writeObject("GET_ALGORITMOS");
        out.flush();

        return (String[]) in.readObject();
        } catch (Exception e) {
        return new String[] { "AES", "DES", "XOR" }; // fallback
        }
    }

}
