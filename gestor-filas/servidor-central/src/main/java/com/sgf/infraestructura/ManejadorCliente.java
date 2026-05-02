package com.sgf.infraestructura;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.excepciones.FilaVaciaException;
import com.sgf.modelos.Turno;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private ILogicaFila logica;
    private ServidorCentral servidor;

    public ManejadorCliente(Socket socket, ILogicaFila logica, ServidorCentral server) {
        this.socket = socket;
        this.logica = logica;
        this.servidor = server;
    }

    @Override 
    public void run(){
        try(
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ){
            String comando = (String) in.readObject();
            
            if(!servidor.esPrimario()){
                out.writeObject("ERROR_SERVIDOR_SECUNDARIO");
                out.flush();
                return;
            }
    
            switch(comando){
                case "NUEVO_TURNO":
                    Turno t = (Turno) in.readObject();
                    try {
                        logica.agregarTurno(t);
                        servidor.sincronizarEstado();
                        out.writeObject("OK");
                    } catch (DNIRepetidoException e) {
                        out.writeObject("ERROR_DNI_REPETIDO");
                    }
                    break;
                case "LLAMAR_SIGUIENTE":
                    int idPuesto = (int) in.readObject();
                    try {
                        Turno llamado = logica.llamarSiguiente(idPuesto);
                        out.writeObject(llamado); // para el OP
                        servidor.notificarMonitores(logica.getUltimoLlamado(), logica.getHistorial());
                        servidor.sincronizarEstado();
                    } catch (FilaVaciaException e) {
                        out.writeObject("ERROR_FILA_VACIA");
                    }
                    break;
                case "REINTENTAR_LLAMADO":
                    int id=(int)in.readObject();
                    Turno reIntento = logica.reintentarLlamado(id);
                    out.writeObject(reIntento); // para el op
                    servidor.notificarMonitores(logica.getUltimoLlamado(), logica.getHistorial());
                    servidor.sincronizarEstado();
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
                case "SINCRONIZAR_ESTADO":
                List<Turno> cola = (List<Turno>) in.readObject();
                Map<Integer, Turno> activos = (Map<Integer, Turno>) in.readObject();
                List<Turno> historial = (List<Turno>) in.readObject();
                Turno ultimo = (Turno) in.readObject();

                logica.reemplazarEstado(cola, activos, historial, ultimo);
                break;
                case "SUSCRIBIR_MONITOR":
                    servidor.agregarMonitor(out);

                    while (true) {
                        Thread.sleep(10000); // mantener viva la conexión
    } //en el manejador

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
