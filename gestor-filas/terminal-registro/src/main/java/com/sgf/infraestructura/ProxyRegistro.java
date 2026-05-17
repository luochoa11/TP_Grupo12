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

    private void resolverServidor() {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_RUTA_PRIMARIA");
            out.flush();

            this.ipServidor     = (String) in.readObject();
            this.puertoServidor = (int)    in.readObject();

            System.out.println("[ProxyRegistro] Servidor resuelto → "
                + ipServidor + ":" + puertoServidor);

        } catch (Exception e) {
            throw new RuntimeException(
                "[ProxyRegistro] No se pudo resolver el Servidor desde el Directorio: "
                + e.getMessage());
        }
    }

private Socket conectarConFallback() throws Exception {
        int intentoActual = 1;

        while (intentoActual <= MAX_INTENTOS) {
            try {
                Socket socket = new Socket(ipServidor, puertoServidor);
                
                if (intentoActual > 1) {
                    System.out.println("[ProxyRegistro] Conexión recuperada en el intento " + intentoActual);
                }
                
                return socket;
                
            } catch (Exception e) {
                System.out.println("[ProxyRegistro] Fallo de conexión (intento " + intentoActual + " de " + MAX_INTENTOS + ").");

                if (intentoActual == MAX_INTENTOS) {
                    throw new Exception("No se pudo conectar con el servidor tras " + MAX_INTENTOS + " intentos.");
                }

                System.out.println("[ProxyRegistro] Re-consultando Directorio y reintentando...");
                try {
                    resolverServidor();
                    Thread.sleep(500); 
                } catch (Exception ex) {
                    System.out.println("[ProxyRegistro] Error al consultar el directorio en el reintento.");
                }

                intentoActual++;
            }
        }
        throw new Exception("Error de conexión inesperado en ProxyRegistro.");
    }

    @Override
    public void agregarTurno(Turno turno) throws DNIRepetidoException {
        try (Socket socket = conectarConFallback();
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

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