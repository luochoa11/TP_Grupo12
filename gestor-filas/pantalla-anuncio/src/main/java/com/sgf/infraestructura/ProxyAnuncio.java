package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.sgf.interfaces.IServicioAnuncio;
import com.sgf.modelos.Turno;
import com.sgf.presentacion.ControladorAnuncio;

public class ProxyAnuncio implements Runnable, IServicioAnuncio {

    private final String directorioIp;
    private final int    directorioPuerto;
    private final ControladorAnuncio controlador;

    private String ipServidor;
    private int    puertoServidor;

    private Turno      actual;
    private List<Turno> historial;

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

            System.out.println("[ProxyAnuncio] Servidor resuelto → "
                + ipServidor + ":" + puertoServidor);
        }
    }

    @Override
    public Turno getUltimoLlamado() { return actual; }

    @Override
    public List<Turno> getHistorial() { return historial; }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        while (true) {
            try {
                resolverServidor();

                try (Socket socket = new Socket(ipServidor, puertoServidor);
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

                    out.writeObject("SUSCRIBIR_MONITOR");
                    out.flush();

                    while (true) {
                        this.actual    = (Turno)       in.readObject();
                        this.historial = (List<Turno>) in.readObject();
                        controlador.actualizarDesdeServidor(actual, historial);
                    }
                }

            } catch (Exception e) {
                System.err.println("[ProxyAnuncio] Conexión perdida: " + e.getMessage());
                System.out.println("[ProxyAnuncio] Reintentando en 500ms...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break; // salida limpia si el hilo es interrumpido
                }
            }
        }
    }
}