package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.sgf.ConfiguracionRed;
import com.sgf.interfaces.IServicioAnuncio;
import com.sgf.modelos.Turno;
import com.sgf.presentacion.ControladorAnuncio;
import com.sgf.seguridad.EstrategiaCifradoAES;
import com.sgf.seguridad.IEncriptacionStrategy;

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

    private String clavePorDefecto = ConfiguracionRed.get("seguridad.clave") != null ? 
                                     ConfiguracionRed.get("seguridad.clave") : "ADMIN123";

    private IEncriptacionStrategy encriptador = new EstrategiaCifradoAES(clavePorDefecto);

    public ProxyAnuncio(String directorioIp, int directorioPuerto, ControladorAnuncio controlador) {
        this.directorioIp     = directorioIp;
        this.directorioPuerto = directorioPuerto;
        this.controlador      = controlador;
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
                // 1. Si no tenemos IP, la resolvemos de entrada
                if (ipServidor == null) {
                    resolverServidor();
                }

                int intentoActual = 1;
                boolean conectado = false;
                Socket socket = null;

                // 2. Intentamos reconectar de forma insistente antes de consultar al directorio de nuevo
                while (intentoActual <= MAX_INTENTOS && !conectado && activo) {
                    try {
                        socket = new Socket(ipServidor, puertoServidor);
                        conectado = true;
                    } catch (Exception e) {
                        System.out.println("[ProxyAnuncio] Intento " + intentoActual + " de suscripción a " 
                            + ipServidor + ":" + puertoServidor + " falló. Reintentando...");
                        if (intentoActual < MAX_INTENTOS) {
                            try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        }
                        intentoActual++;
                    }
                }

                // 3. Si no logramos conectar con el servidor conocido, consultamos al Directorio (Failover)
                if (!conectado && activo) {
                    System.out.println("[ProxyAnuncio] Servidor inalcanzable. Buscando nueva ruta en Directorio...");
                    resolverServidor();
                    try {
                        socket = new Socket(ipServidor, puertoServidor);
                    } catch (Exception e) {
                        System.err.println("[ProxyAnuncio] El nuevo servidor asignado tampoco responde. Re-iniciando ciclo...");
                        try { Thread.sleep(3000); } catch (InterruptedException ie) {}
                        continue;
                    }
                }

                // 4. Establecemos el canal de suscripción persistente
                if (socket != null && activo) {
                    try (Socket s = socket;
                        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                        ObjectInputStream  in  = new ObjectInputStream(s.getInputStream())) {

                        out.writeObject("CLIENTE_ANUNCIO");
                        out.flush(); 

                        out.writeObject("SUSCRIBIR_MONITOR");
                        out.flush();
                        System.out.println("[ProxyAnuncio] Suscripción establecida en: " + ipServidor + ":" + puertoServidor);

                        // Se mantiene escuchando activamente en este bucle
                        while (activo) {
                            this.actual   = (Turno)       in.readObject();
                            this.historial = (List<Turno>) in.readObject();
                            
                            // DESENCRIPTAMOS LO QUE LLEGA DE LA RED
                            desencriptarTurno(this.actual);
                            desencriptarLista(this.historial);

                            controlador.actualizarDesdeServidor(actual, historial);
                        }
                    }
                }

            } catch (Exception e) {
                if (activo) {
                    System.err.println("[ProxyAnuncio] Canal de eventos cerrado: " + e.getMessage());
                    System.out.println("[ProxyAnuncio] Reintentando suscripción en 3 segundos...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
    
    // --- Helpers Privados de Seguridad ---
    private void desencriptarTurno(Turno t) {
        if (t != null && t.getDniCliente() != null) {
            t.setDniCliente(encriptador.desencriptar(t.getDniCliente()));
        }
    }

    private void desencriptarLista(List<Turno> lista) {
        if (lista != null) {
            for (Turno t : lista) desencriptarTurno(t);
        }
    }
    // -------------------------------------
    
    public void detener() {
        this.activo = false;
    }

}
