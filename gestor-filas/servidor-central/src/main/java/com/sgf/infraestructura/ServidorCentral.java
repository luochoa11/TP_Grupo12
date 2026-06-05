package com.sgf.infraestructura;

import java.io.ObjectInputStream;
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
import com.sgf.disponibilidad.SincronizadorEstado;
import com.sgf.interfaces.IServicioAdministrador;
import com.sgf.modelos.Turno;
import com.sgf.persistencia.GestorPersistencia;
import com.sgf.seguridad.IEncriptacionStrategy;
import com.sgf.servicios.ServidorCentralFacade;

/**
 * Clase que representa el Servidor Central del sistema. 
 */
public class ServidorCentral implements Runnable {
    private int puerto;
    private String ip;
    private ILogicaFila logica;
    private List<ObjectOutputStream> monitores = Collections.synchronizedList(new ArrayList<>());
    Map<Integer, ObjectOutputStream> operadores = new ConcurrentHashMap<>(); 
    private boolean esPrimario;
    private SincronizadorEstado sincronizador;
    
    // Atributos de persistencia y fachada de control administrativo
    private IServicioAdministrador fachadaServidor;
    private GestorPersistencia gestorPersistencia;

    private IEncriptacionStrategy encriptador = null;

    public ServidorCentral(int puerto, String ip, ILogicaFila logica, boolean esPrimario,SincronizadorEstado sincronizador) {
        this.puerto = puerto;
        this.ip = ip;
        this.logica = logica;
        this.esPrimario = esPrimario;
        this.sincronizador = sincronizador;

        this.gestorPersistencia = new GestorPersistencia("JSON"); 
        this.fachadaServidor = new ServidorCentralFacade(this, this.gestorPersistencia, this.logica);
    }

    public IEncriptacionStrategy getEncriptador() {
        return encriptador;
    }

    public void setEncriptador(IEncriptacionStrategy nuevoEncriptador) {
        this.encriptador = nuevoEncriptador;
    }
    
    public IServicioAdministrador getFachada() {
        return this.fachadaServidor;
    }

    @Override
    public void run() {
        try(ServerSocket server = new ServerSocket(puerto)){
            System.out.println("Servidor Central iniciado. Escuchando en el puerto " + puerto);
            
            while (true) {
                Socket socketCliente = server.accept();
                
                new Thread(() -> {
                    try {
                        ObjectOutputStream out = new ObjectOutputStream(socketCliente.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(socketCliente.getInputStream());
                        
                        String saludo = (String) in.readObject();
                        System.out.println("[Servidor] Conexión entrante con saludo: " + saludo);
                        
                        // Secundario rechaza todo excepto sincronización y promoción del monitor
                        if (!esPrimario && !"SYNC_SERVER".equals(saludo) && !"MONITOR_FALLA".equals(saludo)) {
                            out.writeObject("ERROR_SERVIDOR_SECUNDARIO");
                            out.flush();
                            socketCliente.close();
                            return;
                        }

                        switch (saludo) {
                            case "CLIENTE_REGISTRO":
                                new Thread(new ManejadorRegistro(socketCliente, in, out, logica, this)).start();
                                break;
                            case "CLIENTE_OPERADOR":
                                new Thread(new ManejadorOperador(socketCliente, in, out, logica, this)).start();
                                break;
                            case "CLIENTE_ANUNCIO":
                                new Thread(new ManejadorAnuncio(socketCliente, in, out, logica, this)).start();
                                break;
                            case "MONITOR_FALLA":
                                new Thread(new ManejadorMonitor(socketCliente, in, out, logica, this)).start();
                                break;
                            case "SYNC_SERVER":
                                new Thread(new ManejadorSincronizacion(socketCliente, in, out, logica, this)).start();
                                break;
                            case "CLIENTE_ADMINISTRADOR":
                                new Thread(new ManejadorAdministrador(socketCliente, in, out, logica, this, fachadaServidor)).start();
                                break;
                            default:
                                System.err.println("[Servidor] Saludo no reconocido: " + saludo);
                                socketCliente.close();
                        }
                    } catch (Exception e) {
                        System.err.println("[ServidorCentral] Error al clasificar conexión: " + e.getMessage());
                    }
                }).start();
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
            
            if (encriptador == null) throw new RuntimeException("SISTEMA BLOQUEADO: Seguridad no configurada.");

            if (actual != null) actual.setDniCliente(encriptador.encriptar(actual.getDniCliente()));
            if (historial != null) {
                for (Turno t : historial) t.setDniCliente(encriptador.encriptar(t.getDniCliente()));
            }

            Iterator<ObjectOutputStream> it = monitores.iterator();

            while (it.hasNext()) {
                ObjectOutputStream out = it.next();
                try {
                    out.reset();  
                    out.writeObject(actual);
                    out.writeObject(historial);
                    out.flush();
                } catch (Exception e) {
                    it.remove(); 
                }
            }
            
            if (actual != null) actual.setDniCliente(encriptador.desencriptar(actual.getDniCliente()));
            if (historial != null) {
                for (Turno t : historial) t.setDniCliente(encriptador.desencriptar(t.getDniCliente()));
            }
        }
    }
    
    public void notificarOperadores() { 
        for (Map.Entry<Integer, ObjectOutputStream> entry : operadores.entrySet()) {
            int id = entry.getKey();
            ObjectOutputStream out = entry.getValue();

            try {
                Turno actual = logica.getTurnoPuesto(id);
                List<Turno> cola = logica.getCola();
                
                if (encriptador == null) throw new RuntimeException("SISTEMA BLOQUEADO: Seguridad no configurada.");

                if (actual != null) actual.setDniCliente(encriptador.encriptar(actual.getDniCliente()));
                if (cola != null) {
                    for (Turno t : cola) t.setDniCliente(encriptador.encriptar(t.getDniCliente()));
                }

                out.writeObject(actual);
                out.writeObject(cola);
                out.flush();

                if (actual != null) actual.setDniCliente(encriptador.desencriptar(actual.getDniCliente()));
                if (cola != null) {
                    for (Turno t : cola) t.setDniCliente(encriptador.desencriptar(t.getDniCliente()));
                }

            } catch (Exception e) {
                operadores.remove(id);
            }
        }
    }

    public boolean esPrimario() {
        return esPrimario;
    }

    public void promoverEstado() {
        this.esPrimario = true;
        System.out.println("[Servidor] " + this.ip + ":" + this.puerto + " Promovido a PRIMARIO.");
        
        if (this.fachadaServidor instanceof ServidorCentralFacade) {
            ((ServidorCentralFacade) this.fachadaServidor).cargarSeguridadDesdeDisco();
        }
    }

    public void sincronizarEstado() {
        if (esPrimario && sincronizador != null) { 
            sincronizador.sincronizar();
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public SincronizadorEstado getSincronizador() {
        return sincronizador;   
    }

    public void degradarEstado() {
        this.esPrimario = false;
        System.out.println("[Servidor] "+this.ip+":"+this.puerto+" Degradado a SECUNDARIO.");
    }
}
