package com.sgf.infraestructura;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ServidorDirectorio es el componente central del Servicio de Directorio. 
 * -Escucha en un puerto específico y responde a las consultas de los Proxies de los Clientes.
 * -Mantiene la información actualizada sobre la IP y puerto del Servidor Primario, 
 * y proporcionar esta información a los Proxies cuando se les solicite. 
 * -Permite que el Servidor Primario actualice su ruta en caso de cambios o caídas.    
 */

public class ServidorDirectorio implements Runnable {
    
    private int puerto;
    private GestorRutas gestorRutas;

    public ServidorDirectorio(int puerto, GestorRutas gestorRutas) {
        this.puerto = puerto;
        this.gestorRutas = gestorRutas;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(puerto)) {
            System.out.println("Servidor de Directorio iniciado. Escuchando en el puerto " + puerto);
            
            while (true) {
                Socket socketCliente = server.accept();
                
                ManejadorDirectorio manejador = new ManejadorDirectorio(socketCliente, gestorRutas);
                
                Thread hiloDirectorio = new Thread(manejador);
                hiloDirectorio.setName("Hilo-Dir-" + socketCliente.getInetAddress());
                hiloDirectorio.start();
            }
        } catch (BindException e) {
            System.err.println("Error: El puerto del Directorio " + puerto + " ya está en uso.");
        } catch (Exception e) {
            System.err.println("Error crítico en el Servidor de Directorio: " + e.getMessage());
            e.printStackTrace();
        }
    }
}