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

    private String claveConfigurada = ConfiguracionRed.get("seguridad.clave");
    private IEncriptacionStrategy encriptador = claveConfigurada != null ? new EstrategiaCifradoAES(claveConfigurada) : null;

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
            throw e;
        }
    }

    //Sincronizar clave dinámicamente
    private void sincronizarClaveConServidor() {
        try (Socket socket = new Socket(ipServidor, puertoServidor);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("CLIENTE_ADMINISTRADOR");
            out.flush();
            out.writeObject("GET_CLAVE");
            out.flush();

            String claveServidor = (String) in.readObject();

            if (claveServidor != null && !claveServidor.equals("SISTEMA_BLOQUEADO")) {
                this.encriptador = new EstrategiaCifradoAES(claveServidor);
            }
        } catch (Exception e) {
            // Falla silenciosa: si falla, usa la última clave conocida.
        }
    }

    //Obtener la foto de la fila al arrancar
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

            desencriptarTurno(this.actual);
            desencriptarLista(this.historial);

            controlador.actualizarDesdeServidor(actual, historial);
            System.out.println("[ProxyAnuncio] Estado inicial cargado en pantalla.");

        } catch (Exception e) {
            System.err.println("[ProxyAnuncio] No se pudo cargar el estado inicial.");
        }
    }
    // --------------------------------------------------------

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
                    // 1. Actualizamos la clave criptográfica en caliente
                    sincronizarClaveConServidor();
                    
                    // 2. Cargamos los turnos actuales para que la pantalla no esté negra
                    obtenerEstadoInicial();

                    // 3. Establecemos la conexión persistente (Suscripción)
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
                            
                            desencriptarTurno(this.actual);
                            desencriptarLista(this.historial);

                            controlador.actualizarDesdeServidor(actual, historial);
                        }
                    }
                }

            } catch (Exception e) {
                if (activo) {
                    System.err.println("[ProxyAnuncio] Canal de eventos cerrado por error o cambio de clave.");
                    //e.printStackTrace(); // <-- Esto nos va a decir exactamente qué cortó la conexión
                    try { Thread.sleep(3000); } catch (InterruptedException ie) { break; }
                }
            }
        }
    }
    
    // --- Helpers Privados de Seguridad ---
    private void desencriptarTurno(Turno t) throws Exception {
        if (t != null && t.getDniCliente() != null && encriptador != null) {
            t.setDniCliente(encriptador.desencriptar(t.getDniCliente()));
        }
    }

    private void desencriptarLista(List<Turno> lista) throws Exception {
        if (lista != null) {
            for (Turno t : lista) desencriptarTurno(t);
        }
    }
    
    public void detener() {
        this.activo = false;
    }
}
