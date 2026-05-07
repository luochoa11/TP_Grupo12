package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.sgf.Constantes;
import com.sgf.interfaces.IServicioAnuncio;
import com.sgf.modelos.Turno;
import com.sgf.presentacion.ControladorAnuncio;

public class ProxyAnuncio implements Runnable, IServicioAnuncio {
    private String host;
    private int puerto;
    private ControladorAnuncio controlador;
    private boolean activo = true;

    private Turno actual; 
    private List<Turno> historial; 

    private final int MAX_INTENTOS = 3;
    private final int TIEMPO_ESPERA_MS = 2000;

    public ProxyAnuncio(String host, int puerto, ControladorAnuncio controlador) {
        this.host = host;
        this.puerto = puerto;
        this.controlador = controlador;
    }

    // --- MÉTODOS DEL CONTRATO (Individuales) ---
    @Override
    public Turno getUltimoLlamado() {
        return actual;
    }

    @Override
    public List<Turno> getHistorial() {
        return historial;
    }

    // MÉTODO AUXILIAR PARA FAILOVER
    private void consultarDirectorio() {
        System.out.println("[ProxyAnuncio] Consultando nueva ruta al Directorio...");
        try (Socket socketDir = new Socket(Constantes.HOST_DIRECTORIO, Constantes.PUERTO_DIRECTORIO);
             ObjectOutputStream outDir = new ObjectOutputStream(socketDir.getOutputStream());
             ObjectInputStream inDir = new ObjectInputStream(socketDir.getInputStream())) {
            
            outDir.writeObject("GET_RUTA_PRIMARIA");
            outDir.flush();
            
            this.host = (String) inDir.readObject();
            this.puerto = (int) inDir.readObject();
            
            System.out.println("[ProxyAnuncio] Ruta actualizada a: " + this.host + ":" + this.puerto);
            
        } catch (Exception dirEx) {
            System.err.println("[ProxyAnuncio] Error al contactar al Directorio: " + dirEx.getMessage());
        }
    }

    @Override
    public void run() {
        int intentoActual = 1;

        // El while principal ahora controla si está activo y si no nos pasamos de intentos
        while (activo && intentoActual <= MAX_INTENTOS) {
            try (
                Socket socket = new Socket(host, puerto);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ) {
                out.writeObject("SUSCRIBIR_MONITOR"); 
                out.flush();
                
                System.out.println("[ProxyAnuncio] Suscrito exitosamente al servidor en " + host + ":" + puerto);

                intentoActual = 1;

                while (activo) {
                    Turno nuevoActual = (Turno) in.readObject(); // Se duerme el hilo hasta que llegue algo
                    List<Turno> nuevoHistorial = (List<Turno>) in.readObject();

                    // Guardamos localmente para los getters
                    this.actual = nuevoActual;
                    this.historial = nuevoHistorial;

                    controlador.actualizarDesdeServidor(actual, historial);
                }

            } catch (Exception e) {
                System.err.println("[ProxyAnuncio] Conexión perdida con el servidor. Motivo: " + e.getMessage());
                
                if (intentoActual == MAX_INTENTOS) {
                    System.err.println("[ProxyAnuncio] Se agotaron los reintentos. La pantalla queda desconectada de la red.");
                    // Acá, si tuvieras un método en el controlador tipo 'mostrarErrorDesconexion()', 
                    // sería ideal llamarlo para que la pantalla del hospital/banco ponga un cartel de "Fuera de servicio".
                    break; 
                }

                System.out.println("[ProxyAnuncio] Intentando reconectar en " + TIEMPO_ESPERA_MS + "ms (Intento " + intentoActual + "/" + MAX_INTENTOS + ")...");
                try {
                    Thread.sleep(TIEMPO_ESPERA_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                consultarDirectorio();
                intentoActual++;
            }
        }
    }

    // Método para apagar el proxy limpiamente si cierran el programa
    public void detener() {
        this.activo = false;
    }
}