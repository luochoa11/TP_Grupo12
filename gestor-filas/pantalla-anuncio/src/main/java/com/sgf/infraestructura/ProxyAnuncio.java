package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.sgf.interfaces.IServicioAnuncio;
import com.sgf.modelos.Turno;
import com.sgf.presentacion.ControladorAnuncio;
import com.sgf.seguridad.SeguridadAnuncio;

public class ProxyAnuncio implements Runnable, IServicioAnuncio {

    private final String directorioIp;
    private final int    directorioPuerto;
    private final ControladorAnuncio controlador;

    private String ipServidor;
    private int    puertoServidor;

    private Turno       actual;
    private List<Turno> historial;
    private volatile boolean activo = true;

    private final int MAX_INTENTOS = 3;

    private final SeguridadAnuncio seguridad;

    public ProxyAnuncio(String directorioIp, int directorioPuerto, ControladorAnuncio controlador, SeguridadAnuncio seguridad) {
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
        this.controlador      = controlador;
        this.seguridad        = seguridad;
    }

    private void resolverServidor() throws Exception {
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_RUTA_PRIMARIA");
            out.flush();

            this.ipServidor     = (String) in.readObject();
            this.puertoServidor = (int)    in.readObject();

            System.out.println("[ProxyAnuncio] Servidor resuelto -> "+ ipServidor + ":" + puertoServidor);
        } catch (Exception e) {
            System.err.println("[ProxyAnuncio] Error al consultar Directorio: " + e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private void obtenerEstadoInicial() {
        try (Socket socket = new Socket(ipServidor, puertoServidor);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("CLIENTE_ANUNCIO");
            out.flush();
            out.writeObject("GET_ESTADO_MONITOR");
            out.flush();

            this.actual    = (Turno) in.readObject();
            this.historial = (List<Turno>) in.readObject();

            seguridad.desencriptarTurno(this.actual);
            seguridad.desencriptarLista(this.historial);

            controlador.actualizarDesdeServidor(actual, historial);
            System.out.println("[ProxyAnuncio] Estado inicial cargado en pantalla.");

        } catch (Exception e) {
            System.err.println("[ProxyAnuncio] No se pudo cargar el estado inicial.");
        }
    }

    @Override
    public Turno getUltimoLlamado() { return actual; }

    @Override
    public List<Turno> getHistorial() { return historial; }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        while (activo) {
            try {
                if (ipServidor == null) resolverServidor();

                int intentoActual = 1;
                boolean conectado = false;
                Socket socket = null;

                while (intentoActual <= MAX_INTENTOS && !conectado && activo) {
                    try {
                        socket = new Socket(ipServidor, puertoServidor);
                        conectado = true;
                    } catch (Exception e) {
                        System.out.println("[ProxyAnuncio] Intento " + intentoActual + " falló. Reintentando...");
                        if (intentoActual < MAX_INTENTOS) {
                            try { Thread.sleep(1000); } catch (InterruptedException ie) {}
                        }
                        intentoActual++;
                    }
                }

                if (!conectado && activo) {
                    System.out.println("[ProxyAnuncio] Servidor inalcanzable. Buscando failover...");
                    resolverServidor();
                    try {
                        socket = new Socket(ipServidor, puertoServidor);
                    } catch (Exception e) {
                        try { Thread.sleep(3000); } catch (InterruptedException ie) {}
                        continue;
                    }
                }

                if (socket != null && activo) {
                    
                    obtenerEstadoInicial();

                    try (Socket s = socket;
                        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                        ObjectInputStream  in  = new ObjectInputStream(s.getInputStream())) {

                        out.writeObject("CLIENTE_ANUNCIO");
                        out.flush(); 

                        out.writeObject("SUSCRIBIR_MONITOR");
                        out.flush();
                        System.out.println("[ProxyAnuncio] Suscripción establecida en: " + ipServidor + ":" + puertoServidor);

                        while (activo) {
                            this.actual    = (Turno) in.readObject();
                            this.historial = (List<Turno>) in.readObject();
                            
                            seguridad.desencriptarTurno(this.actual);
                            seguridad.desencriptarLista(this.historial);

                            controlador.actualizarDesdeServidor(actual, historial);
                        }
                    }
                }

            } catch (Exception e) {
                if (activo) {
                    System.err.println("[ProxyAnuncio] Canal de eventos cerrado por error de red o de seguridad.");
                    try { Thread.sleep(3000); } catch (InterruptedException ie) { break; }
                }
            }
        }
    }
    
    public void detener() {
        this.activo = false;
    }
}
