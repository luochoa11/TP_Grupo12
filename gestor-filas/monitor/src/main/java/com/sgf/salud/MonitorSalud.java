package com.sgf.salud;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.sgf.modelos.HeartbeatDTO;
import com.sgf.modelos.NodoEstadoDTO;

public class MonitorSalud implements Runnable {
    private int puerto;
    private HeartbeatChecker heartbeatChecker;

    public MonitorSalud(int puerto, HeartbeatChecker heartbeatChecker) {
        this.puerto = puerto;
        this.heartbeatChecker = heartbeatChecker;
    }

    @Override
    public void run() {
        try( ServerSocket serverSocket = new ServerSocket(puerto) ){
            System.out.println("Monitor de Salud iniciado en el puerto " + puerto);
            while(true){

                try(
                    Socket socket = serverSocket.accept();
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                    String mensaje = (String) in.readObject();
                    if("HEARTBEAT".equals(mensaje)){
                        // Recibir el DTO del heartbeat
                        HeartbeatDTO hb = (HeartbeatDTO) in.readObject();
                        // Recibir el DTO del estado del nodo
                        NodoEstadoDTO estado = (NodoEstadoDTO) in.readObject();

                        heartbeatChecker.recibirLatido(hb,estado);

                        NodoEstadoDTO pareja= heartbeatChecker.obtenerPareja(estado);
                        out.writeObject(pareja); // le informa quien es el otro server 
                        out.flush();
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando conexión entrante: " + e.getMessage());
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
