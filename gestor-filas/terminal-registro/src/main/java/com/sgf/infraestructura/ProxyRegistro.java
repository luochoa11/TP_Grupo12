package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.interfaces.IServicioRegistro;
import com.sgf.modelos.Turno;
import com.sgf.seguridad.SeguridadRegistro;

public class ProxyRegistro implements IServicioRegistro {

    private final String directorioIp;
    private final int    directorioPuerto;

    private String ipServidor;
    private int    puertoServidor;

    private final int MAX_INTENTOS = 3;
    
    private final SeguridadRegistro seguridad;

    public ProxyRegistro(String directorioIp, int directorioPuerto, SeguridadRegistro seguridad) {
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
        this.seguridad        = seguridad;
        resolverServidor();
    }

    private Socket conectarServidor() throws Exception {
        return new Socket(ipServidor, puertoServidor);
    }

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
            throw new RuntimeException("[ProxyRegistro] No se pudo resolver el Servidor: " + e.getMessage());
        }
    }

    private Socket conectarConFallback() throws Exception {
        int intentoActual = 1;

        while (intentoActual <= MAX_INTENTOS) {
            try {
                Socket socket = conectarServidor(); 
                if (intentoActual > 1) {
                    System.out.println("[ProxyRegistro] Conexión recuperada en el intento " + intentoActual);
                }
                return socket;
                
            } catch (Exception e) {
                if (intentoActual < MAX_INTENTOS) {
                    try { Thread.sleep(250); } catch (InterruptedException ignored) {}
                }
                intentoActual++;
            }
        }
        
        System.out.println("[ProxyRegistro] Servidor no responde. Consultando al Directorio...");
        try {
            resolverServidor();
        } catch (Exception ex) {}

        try {
            return new Socket(ipServidor, puertoServidor);
        } catch (Exception e) {
            throw new Exception("Error definitivo: Ningún servidor se encuentra activo.");
        }
    }

    @Override
    public void agregarTurno(Turno turno) throws DNIRepetidoException {
        
        String dniOriginal = turno.getDniCliente();

        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject("CLIENTE_REGISTRO");
            out.flush(); 

            turno.setDniCliente(seguridad.encriptarDNI(dniOriginal));

            out.writeObject("NUEVO_TURNO");
            out.writeObject(turno);
            out.flush();

            turno.setDniCliente(dniOriginal);

            Object respuestaObj = in.readObject();
            if (respuestaObj == null) {
                throw new RuntimeException("El servidor rechazó la conexión o está bloqueado.");
            }

            String respuesta = (String) respuestaObj;

            if ("ERROR_DNI_REPETIDO".equals(respuesta)) {
                throw new DNIRepetidoException(dniOriginal);
            }

        } catch (DNIRepetidoException e) {
            turno.setDniCliente(dniOriginal);
            throw e;
        } catch (Exception e) {
            turno.setDniCliente(dniOriginal);
            throw new RuntimeException("Error de conexión con el servidor: " + e.getMessage());
        }
    }
}
