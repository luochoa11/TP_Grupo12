package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.interfaces.IServicioRegistro;
import com.sgf.modelos.Turno;

public class ProxyRegistro implements IServicioRegistro {

    private final String directorioIp;
    private final int    directorioPuerto;

    private String ipServidor;
    private int    puertoServidor;

    private final int MAX_INTENTOS = 3;


    public ProxyRegistro(String directorioIp, int directorioPuerto) {
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
        resolverServidor();
    }

    // Abre un socket al Servidor usando el cache actual
    private Socket conectarServidor() throws Exception {
        return new Socket(ipServidor, puertoServidor);
    }

    /**
     * Consulta al Directorio para conocer la IP y Puerto del Servidor Primario activo.
     */
    private void resolverServidor() {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_RUTA_PRIMARIA");
            out.flush();

            this.ipServidor     = (String) in.readObject();
            this.puertoServidor = (int)    in.readObject();

            System.out.println("[ProxyRegistro] Servidor resuelto -> "+ ipServidor + ":" + puertoServidor);

        } catch (Exception e) {
            throw new RuntimeException(
                "[ProxyRegistro] No se pudo resolver el Servidor desde el Directorio: "
                + e.getMessage());
        }
    }

    /**
     * Intenta conectar con el servidor actual. Si se agotan los intentos,
     * realiza el failover consultando la nueva ruta al Directorio y reintenta.
     */
    private Socket conectarConFallback() throws Exception {
        int intentoActual = 1;

        // 1. Intentamos insistir localmente en el servidor conocido
        while (intentoActual <= MAX_INTENTOS) {
            try {
                Socket socket = conectarServidor(); 
                if (intentoActual > 1) {
                    System.out.println("[ProxyRegistro] Conexión recuperada en el intento " + intentoActual);
                }
                return socket;
                
            } catch (Exception e) {
                System.out.println("[ProxyRegistro] Fallo de conexión (intento " + intentoActual + " de " + MAX_INTENTOS + ").");

                if (intentoActual < MAX_INTENTOS) {
                    try {
                        Thread.sleep(500); // Pausa breve entre reintentos de red transitorios
                    } catch (InterruptedException ignored) {}
                }
                intentoActual++;
            }
        }
        
        // 2. Si se agotaron los intentos locales, significa que el servidor cayó. Consultamos al directorio por el failover.
        System.out.println("[ProxyRegistro] Servidor no responde. Consultando al Directorio para obtener el nuevo Primario...");
        try {
            resolverServidor();
        } catch (Exception ex) {
            System.out.println("[ProxyRegistro] Error crítico al intentar conectar con el directorio.");
        }

        // 3. Intentamos conectar una última vez con la IP fresca recuperada del Directorio
        System.out.println("[ProxyRegistro] Intentando conectar al nuevo Servidor Primario -> " + ipServidor + ":" + puertoServidor);
        try {
            return new Socket(ipServidor, puertoServidor);
        } catch (Exception e) {
            throw new Exception("Error de conexión definitivo: Ningún servidor de filas se encuentra activo en la red.");
        }
    }

    @Override
    public void agregarTurno(Turno turno) throws DNIRepetidoException {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject("CLIENTE_REGISTRO");
            out.flush(); 

            out.writeObject("NUEVO_TURNO");
            out.writeObject(turno);
            out.flush();

            String respuesta = (String) in.readObject();

            if ("ERROR_DNI_REPETIDO".equals(respuesta)) {
                throw new DNIRepetidoException(turno.getDniCliente());
            }

        } catch (DNIRepetidoException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con el servidor: " + e.getMessage());
        }
    }
}