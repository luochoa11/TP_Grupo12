package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import com.sgf.excepciones.FilaVaciaException;
import com.sgf.interfaces.IServicioOperador;
import com.sgf.modelos.Turno;
import com.sgf.seguridad.SeguridadOperador;

public class ProxyOperador implements IServicioOperador{
    private final String directorioIp;
    private final int directorioPuerto;

    //caché local de direccionamiento
    private String ipServidor;
    private int puertoServidor;

    private final int MAX_INTENTOS = 3;
    
    private final SeguridadOperador seguridad;

    public ProxyOperador(String directorioIp, int directorioPuerto, SeguridadOperador seguridad) {
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
        this.seguridad        = seguridad;
        resolverServidor();
    }

    private void resolverServidor() {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_RUTA_PRIMARIA");
            out.flush();

            this.ipServidor     = (String) in.readObject();
            this.puertoServidor = (int)    in.readObject();

            System.out.println("[ProxyOperador] Servidor resuelto -> "+ ipServidor + ":" + puertoServidor);

        } catch (Exception e) {
            throw new RuntimeException(
                "[ProxyOperador] No se pudo resolver el Servidor desde el Directorio: "
                + e.getMessage());
        }
    }

    private Socket conectarServidor() throws Exception {
        return new Socket(ipServidor, puertoServidor);
    }

    private Socket conectarConFallback() throws Exception {
        int intentoActual = 1;

        while (intentoActual <= MAX_INTENTOS) {
            try {
                Socket socket = conectarServidor();
                if (intentoActual > 1) {
                    System.out.println("[ProxyOperador] Conexión recuperada en el intento " + intentoActual);
                }
                return socket;
                
            } catch (Exception e) {
                if(intentoActual < MAX_INTENTOS){
                    try {
                        Thread.sleep(500); 
                    } catch (InterruptedException ignored) {}    
                }
                intentoActual++;
            }
        }
        
        System.out.println("[ProxyOperador] Servidor no responde. Consultando al Directorio para obtener el nuevo Primario...");
        try {
            resolverServidor();
        } catch (Exception ex) {
            System.out.println("[ProxyOperador] Error crítico al intentar conectar con el directorio.");
        }

        try {
            return conectarServidor();
        } catch (Exception e) {
            throw new Exception("Error de conexión definitivo: El panel de operador no se pudo conectar a ningún servidor de filas.");
        }
    }

    @Override
    public Turno llamarSiguiente(int idPuesto) throws FilaVaciaException {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("CLIENTE_OPERADOR");
            out.flush(); 

            out.writeObject("LLAMAR_SIGUIENTE");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();

            if ("ERROR_FILA_VACIA".equals(respuesta)) throw new FilaVaciaException();
            
            Turno turnoLlamado = (Turno) respuesta;
            
            seguridad.desencriptarTurno(turnoLlamado);
            
            return turnoLlamado;

        } catch (FilaVaciaException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[ProxyOperador] Error en llamarSiguiente: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Turno reintentarLlamado(int idPuesto) {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("CLIENTE_OPERADOR");
            out.flush(); 

            out.writeObject("REINTENTAR_LLAMADO");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();

            if (respuesta instanceof Turno) {
                Turno turnoReintento = (Turno) respuesta;
                
                seguridad.desencriptarTurno(turnoReintento);
                
                return turnoReintento;
            }

            System.err.println("[ProxyOperador] Respuesta inesperada: " + respuesta);
            return null;

        } catch (Exception e) {
            System.err.println("[ProxyOperador] Error en reintentarLlamado: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Turno> getCola() {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("CLIENTE_OPERADOR");
            out.flush(); 

            out.writeObject("GET_COLA");
            out.flush();

            Object respuesta = in.readObject();

            if (respuesta instanceof List) {
                List<Turno> cola = (List<Turno>) respuesta;
                
                seguridad.desencriptarLista(cola); 
                
                return cola;
            }
            if (respuesta == null)          return Collections.emptyList();

            System.err.println("[ProxyOperador] Respuesta inesperada: " + respuesta);
            return Collections.emptyList();

        } catch (Exception e) {
            System.err.println("[ProxyOperador] Error en getCola: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Turno getTurnoPuesto(int idPuesto) {
        try (Socket socket = conectarConFallback();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("CLIENTE_OPERADOR");
            out.flush(); 

            out.writeObject("GET_TURNO_PUESTO");
            out.writeObject(idPuesto);
            out.flush();

            Object respuesta = in.readObject();

            if (respuesta instanceof Turno) {
                Turno turnoPuesto = (Turno) respuesta;
                
                seguridad.desencriptarTurno(turnoPuesto); 
                
                return turnoPuesto;
            }
            if (respuesta == null)          return null;

            System.err.println("[ProxyOperador] Respuesta inesperada: " + respuesta);
            return null;

        } catch (Exception e) {
            System.err.println("[ProxyOperador] Error en getTurnoPuesto: " + e.getMessage());
            return null;
        }
    }
}
