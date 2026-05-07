package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import com.sgf.Constantes;
import com.sgf.excepciones.FilaVaciaException;
import com.sgf.interfaces.IServicioOperador;
import com.sgf.modelos.Turno;

public class ProxyOperador implements IServicioOperador {
    private String host;
    private int puerto;

    private final int MAX_INTENTOS = 3;
    private final int TIEMPO_ESPERA_MS = 2000;

    public ProxyOperador(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    // MÉTODO AUXILIAR PARA FAILOVER (Consulta al Directorio)
    private void consultarDirectorio() {
        System.out.println("[ProxyOperador] Consultando nueva ruta al Directorio...");
        try (Socket socketDir = new Socket(Constantes.HOST_DIRECTORIO, Constantes.PUERTO_DIRECTORIO);
             ObjectOutputStream outDir = new ObjectOutputStream(socketDir.getOutputStream());
             ObjectInputStream inDir = new ObjectInputStream(socketDir.getInputStream())) {
            
            outDir.writeObject("GET_RUTA_PRIMARIA");
            outDir.flush();
            
            this.host = (String) inDir.readObject();
            this.puerto = (int) inDir.readObject();
            
            System.out.println("[ProxyOperador] Ruta actualizada a: " + this.host + ":" + this.puerto);
            
        } catch (Exception dirEx) {
            System.err.println("[ProxyOperador] Error al contactar al Directorio: " + dirEx.getMessage());
        }
    }

    // MÉTODOS DE LA INTERFAZ CON RESILIENCIA (RETRY)
    @Override
    public Turno llamarSiguiente(int idPuesto) throws FilaVaciaException {
        int intentoActual = 1;

        while (intentoActual <= MAX_INTENTOS) {
            try (Socket socket = new Socket(host, puerto);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("LLAMAR_SIGUIENTE");
                out.writeObject(idPuesto);
                out.flush();

                Object respuesta = in.readObject();
                
                if ("ERROR_FILA_VACIA".equals(respuesta)) {
                    throw new FilaVaciaException();
                }
                
                return (Turno) respuesta;

            } catch (FilaVaciaException e) {
                throw e; // Error de negocio, lo dejamos pasar
            } catch (Exception e) {
                System.err.println("[ProxyOperador - llamarSiguiente] Falla de red en intento " + intentoActual);
                
                if (intentoActual == MAX_INTENTOS) {
                    throw new RuntimeException("Error crítico: Sistema fuera de servicio.");
                }

                esperarYActualizarRuta();
                intentoActual++;
            }
        }
        return null;
    }

    @Override
    public Turno reintentarLlamado(int idPuesto) {
        int intentoActual = 1;

        while (intentoActual <= MAX_INTENTOS) {
            try (Socket socket = new Socket(host, puerto);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("REINTENTAR_LLAMADO");
                out.writeObject(idPuesto);
                out.flush();

                Object respuesta = in.readObject();
                
                if (respuesta instanceof Turno) {
                    return (Turno) respuesta;
                } else {
                    System.err.println("Respuesta inesperada del servidor: " + respuesta);
                    return null;
                }

            } catch (Exception e) {
                System.err.println("[ProxyOperador - reintentarLlamado] Falla de red en intento " + intentoActual);
                
                if (intentoActual == MAX_INTENTOS) {
                    throw new RuntimeException("Error crítico: Sistema fuera de servicio.");
                }

                esperarYActualizarRuta();
                intentoActual++;
            }
        }
        return null;
    }

    @Override
    public List<Turno> getCola() {
        int intentoActual = 1;

        while (intentoActual <= MAX_INTENTOS) {
            try (Socket socket = new Socket(host, puerto);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("GET_COLA");
                out.flush();

                Object respuesta = in.readObject();
                
                if (respuesta instanceof List) {
                    return (List<Turno>) respuesta;
                } else if (respuesta == null) { 
                    return Collections.emptyList();
                } else {
                    System.err.println("Respuesta inesperada del servidor: " + respuesta);
                    return Collections.emptyList();
                }

            } catch (Exception e) {
                System.err.println("[ProxyOperador - getCola] Falla de red en intento " + intentoActual);
                
                if (intentoActual == MAX_INTENTOS) {
                    throw new RuntimeException("Error crítico: Sistema fuera de servicio.");
                }

                esperarYActualizarRuta();
                intentoActual++;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Turno getTurnoPuesto(int idPuesto) {
        int intentoActual = 1;

        while (intentoActual <= MAX_INTENTOS) {
            try (Socket socket = new Socket(host, puerto);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("GET_TURNO_PUESTO");
                out.writeObject(idPuesto);
                out.flush();

                Object respuesta = in.readObject();
                
                if (respuesta instanceof Turno) {
                    return (Turno) respuesta;
                } else if (respuesta == null) {
                    return null; 
                } else {
                    System.err.println("Respuesta inesperada del servidor: " + respuesta);
                    return null;
                }

            } catch (Exception e) {
                System.err.println("[ProxyOperador - getTurnoPuesto] Falla de red en intento " + intentoActual);
                
                if (intentoActual == MAX_INTENTOS) {
                    throw new RuntimeException("Error crítico: Sistema fuera de servicio.");
                }

                esperarYActualizarRuta();
                intentoActual++;
            }
        }
        return null;
    }

    // Método auxiliar para limpiar los try-catch y abstraer la espera
    private void esperarYActualizarRuta() {
        System.out.println("[ProxyOperador] Esperando " + TIEMPO_ESPERA_MS + "ms antes de reconectar...");
        try {
            Thread.sleep(TIEMPO_ESPERA_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        consultarDirectorio();
    }
}