package com.sgf.infraestructura;

import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.modelos.Turno;

public class ServidorCentral implements Runnable{
    private int puerto;
    private ILogicaFila logica;
    private List<ObjectOutputStream> monitores = Collections.synchronizedList(new ArrayList<>());
    Map<Integer, ObjectOutputStream> operadores = new ConcurrentHashMap<>();


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
                
                ManejadorCliente manejador = new ManejadorCliente(socketCliente, logica,this);
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
    public void agregarMonitor(ObjectOutputStream out){
        monitores.add(out);
    }


    public void notificarMonitores(Turno actual, List<Turno> historial) {
    synchronized (monitores) {
        Iterator<ObjectOutputStream> it = monitores.iterator();

        while (it.hasNext()) {
            ObjectOutputStream out = it.next();
            try {
                out.reset();  // LIMPIA LA CACHE DEL STREAM PARA ENVIAR DATOS NUEVOS
                out.writeObject(actual);
                out.writeObject(historial);
                out.flush();
            } catch (Exception e) {
                it.remove(); // cliente muerto
            }
        }}
    }
    
    public void notificarOperadores() {
        for (Map.Entry<Integer, ObjectOutputStream> entry : operadores.entrySet()) {
        int id = entry.getKey();
        ObjectOutputStream out = entry.getValue();

        try {
            Turno actual = logica.getTurnoPuesto(id);
            List<Turno> cola = logica.getCola();

            out.writeObject(actual);
            out.writeObject(cola);
            out.flush();

        } catch (Exception e) {
            operadores.remove(id);
        }

    }
}}
