package com.sgf.infraestructura;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.excepciones.FilaVaciaException;
import com.sgf.modelos.Turno;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private ILogicaFila logica;

    public ManejadorCliente(Socket socket, ILogicaFila logica) {
        this.socket = socket;
        this.logica = logica;
    }

    @Override 
    public void run(){
        try(
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ){
            String comando = (String) in.readObject();
            
            switch(comando){
                case "NUEVO_TURNO":
                    Turno t = (Turno) in.readObject();
                    try {
                        logica.agregarTurno(t);
                        out.writeObject("OK");
                    } catch (DNIRepetidoException e) {
                        out.writeObject("ERROR_DNI_REPETIDO");
                    }
                    break;
                case "LLAMAR_SIGUIENTE":
                    int idPuesto = (int) in.readObject();
                    try {
                        Turno llamado = logica.llamarSiguiente(idPuesto);
                        out.writeObject(llamado);
                    } catch (FilaVaciaException e) {
                        out.writeObject("ERROR_FILA_VACIA");
                    }
                    break;
                case "REINTENTAR_LLAMADO":
                    int id=(int)in.readObject();
                    Turno reIntento = logica.reintentarLlamado(id);
                    out.writeObject(reIntento);
                    break;
                case "GET_ESTADO_MONITOR":
                    out.writeObject(logica.getUltimoLlamado());
                    out.writeObject(logica.getHistorial());
                    break;
                case "GET_COLA":
                    out.writeObject(logica.getCola());
                    break;
                case "GET_TURNO_PUESTO":
                    int idPuesto2 = (int) in.readObject();
                    out.writeObject(logica.getTurnoPuesto(idPuesto2));
                    break;

            }
            out.flush();
        }catch(Exception e){
            System.err.println("Error de red " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

}
