package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//Ex Cliente

import com.sgf.Constantes; 
import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.interfaces.IServicioRegistro;
import com.sgf.modelos.Turno;

public class ProxyRegistro implements IServicioRegistro {
    
    private String host;
    private int puerto;

    private final int MAX_INTENTOS = 3; 
    private final int TIEMPO_ESPERA_MS = 2000;

    public ProxyRegistro(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    @Override
    public void agregarTurno(Turno turno) throws DNIRepetidoException {
        int intentoActual = 1;
        boolean exito = false;

        while (intentoActual <= MAX_INTENTOS && !exito) {
            try (Socket socket = new Socket(host, puerto);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("NUEVO_TURNO");
                out.writeObject(turno);
                out.flush();

                String respuesta = (String) in.readObject();

                if ("ERROR_DNI_REPETIDO".equals(respuesta)) {
                    throw new DNIRepetidoException(turno.getDniCliente());
                }

                exito = true; 

            } catch (DNIRepetidoException e) {
                // Excepción de negocio
                throw e; 
            } catch (Exception e) {
                System.err.println("[ProxyRegistro] Falla de red en el intento " + intentoActual + ". Motivo: " + e.getMessage());

                if (intentoActual == MAX_INTENTOS) {
                    throw new RuntimeException("Error crítico: El sistema se encuentra fuera de servicio tras " + MAX_INTENTOS + " intentos.");
                }

                System.out.println("[ProxyRegistro] Esperando " + TIEMPO_ESPERA_MS + "ms antes del próximo intento...");
                try {
                    Thread.sleep(TIEMPO_ESPERA_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                // CONSULTA AL DIRECTORIO PARA ACTUALIZAR RUTA
                System.out.println("[ProxyRegistro] Consultando nueva ruta al Directorio...");
                try (Socket socketDir = new Socket(Constantes.HOST_DIRECTORIO, Constantes.PUERTO_DIRECTORIO);
                     ObjectOutputStream outDir = new ObjectOutputStream(socketDir.getOutputStream());
                     ObjectInputStream inDir = new ObjectInputStream(socketDir.getInputStream())) {
                    
                    outDir.writeObject("GET_RUTA_PRIMARIA");
                    outDir.flush();
                    
                    this.host = (String) inDir.readObject();
                    this.puerto = (int) inDir.readObject();
                    
                    System.out.println("[ProxyRegistro] Ruta actualizada a: " + this.host + ":" + this.puerto);
                    
                } catch (Exception dirEx) {
                    System.err.println("[ProxyRegistro] Error al contactar al Directorio: " + dirEx.getMessage());
                }

                intentoActual++;
            }
        }
    }
}