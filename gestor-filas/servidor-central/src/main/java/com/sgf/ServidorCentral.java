package com.sgf;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

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
            System.out.println("Servidor Central escuchando en puerto " + puerto);
            while(true){
                Socket socketCliente = server.accept();
                
                ManejadorCliente manejador = new ManejadorCliente(socketCliente, logica);
                Thread hiloCliente = new Thread(manejador);
                hiloCliente.start();
            }
        }catch(BindException e){
            System.err.println("Error: El puerto " + puerto + " ya está en uso.");
    }catch(Exception e){
            e.printStackTrace();
        }
    }


}
