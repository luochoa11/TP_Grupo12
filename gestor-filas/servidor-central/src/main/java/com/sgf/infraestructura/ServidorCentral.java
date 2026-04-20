package com.sgf.infraestructura;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;

public class ServidorCentral implements Runnable{
    private int puerto;
    private ILogicaFila logica;

    public ServidorCentral(int puerto, ILogicaFila logica) {
        this.puerto = puerto;
        this.logica = logica;
    }

    @Override
    public void run() {
        try(ServerSocket server = new ServerSocket(puerto)){
            System.out.println("Servidor Central iniciado. Escuchando en el puerto " + puerto);
            while(true){
                Socket socketCliente = server.accept();
                
                ManejadorCliente manejador = new ManejadorCliente(socketCliente, logica);
                Thread hiloCliente = new Thread(manejador);
                hiloCliente.setName("Hilo-Manejador-" + socketCliente.getInetAddress());
                hiloCliente.start();
            }
        }catch(BindException e){
            System.err.println("Error: El puerto " + puerto + " ya está en uso.");
    }catch(Exception e){
            System.err.println("Error en el Servidor Central: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
