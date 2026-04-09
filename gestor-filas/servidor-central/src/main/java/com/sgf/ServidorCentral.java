package com.sgf;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.excepciones.FilaVaciaException;

public class ServidorCentral implements Runnable {

    private int puerto;
    private LogicaFila modelo;

    public ServidorCentral(int puerto, LogicaFila modelo) {
        this.puerto = puerto;
        this.modelo = modelo;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(puerto)) {
            System.out.println("Servidor Central activo en el puerto " + puerto);

            while (true) {
                Socket socketCliente = server.accept();

                new Thread(() -> {
                    try (
                        ObjectOutputStream out = new ObjectOutputStream(socketCliente.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(socketCliente.getInputStream())
                    ) {
                        String comando = (String) in.readObject();

                        switch (comando) {
                            case "REGISTRAR":
                                Turno nuevoTurno = (Turno) in.readObject();
                                try {
                                    modelo.agregarTurno(nuevoTurno);
                                    out.writeObject("OK");
                                } catch (DNIRepetidoException e) {
                                    out.writeObject("ERROR_DNI_REPETIDO");
                                }
                                break;

                            case "LLAMAR_SIGUIENTE":
                                try {
                                    // 1. Leemos qué puesto nos está pidiendo el turno
                                    int idPuesto = (int) in.readObject(); 
                                    
                                    // 2. Sacamos el turno de la fila centralizada
                                    Turno siguiente = modelo.llamarSiguiente();
                                    
                                    // 3. Le asignamos el puesto al turno (Requisito 2 del PDF)
                                    siguiente.setPuestoAtencion(idPuesto); 
                                    
                                    // 4. Le respondemos "OK" y el turno al Operador
                                    out.writeObject("OK");
                                    out.writeObject(siguiente);
                                    out.flush();
                                    
                                    // 5. ¡LA MAGIA! El Servidor Central le avisa al Monitor
                                    notificarMonitor(siguiente);
                                    
                                } catch (FilaVaciaException e) {
                                    out.writeObject("ERROR_FILA_VACIA");
                                }
                                break;
                                
                            default:
                                out.writeObject("ERROR_COMANDO");
                                break;
                        }
                        out.flush();

                    } catch (Exception e) {
                        System.err.println("Error en Servidor Central: " + e.getMessage());
                    } finally {
                        try {
                            if (!socketCliente.isClosed()) socketCliente.close();
                        } catch (IOException ignored) {}
                    }
                }).start();
            }

        } catch (BindException e) {
            System.err.println("Error: Puerto " + puerto + " en uso.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método privado que usa el Servidor Central para conectarse al Monitor
     * y enviarle el turno recién llamado.
     */
    private void notificarMonitor(Turno turno) {
        // Acá usamos el puerto y host donde está escuchando tu ServidorMonitor
        try (Socket socketMonitor = new Socket(Constantes.HOST_MONITOR1, Constantes.PUERTO_MONITOR1);
             ObjectOutputStream outMonitor = new ObjectOutputStream(socketMonitor.getOutputStream())) {
             
            outMonitor.writeObject(turno);
            outMonitor.flush();
            System.out.println("Monitor notificado: Turno " + turno.getDniCliente() + " al puesto " + turno.getPuestoAtencion());
            
        } catch (IOException e) {
            System.err.println("No se pudo contactar al Monitor de Visualización: " + e.getMessage());
        }
    }
}