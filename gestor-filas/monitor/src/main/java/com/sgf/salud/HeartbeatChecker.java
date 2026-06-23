package com.sgf.salud;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sgf.failover.GestorFalla;
import com.sgf.modelos.HeartbeatDTO;
import com.sgf.modelos.NodoEstadoDTO;

/**
 * Clase responsable de verificar los latidos (heartbeats) de los servidores en el sistema.
 * Si hay silencio, reporta falla.
 */
public class HeartbeatChecker implements Runnable {
    // Debe correr un hilo que verifique el tiempo transcurrido desde el último latido del Servidor Primario.
    private Map<String, NodoEstadoDTO> nodos = new ConcurrentHashMap<>();; // Mapa de nodos registrados por IP:Puerto
    private Map<String, Long> latidos = new ConcurrentHashMap<>();; // Mapa de últimos latidos por IP:Puerto

    private NodoEstadoDTO primario;
    private NodoEstadoDTO secundario;

    private boolean activo = true;
    private final long timeout=5000; // 5 segundos
    private GestorFalla gestorFalla;

    public HeartbeatChecker(GestorFalla gestorFalla) {
        this.gestorFalla = gestorFalla;
    }

    public synchronized void recibirLatido(HeartbeatDTO hb, NodoEstadoDTO estado) {
        String clave = estado.getIp() + ":" + estado.getPuerto();
        nodos.put(clave, estado);
       // Registramos el tiempo local de recepción para medir el timeout real de forma segura
        latidos.put(clave, System.currentTimeMillis());

        if (estado.isEsPrimario()) {
            this.primario = estado;
        } else {
            // CORRECCIÓN: Agregado null-check sobre 'this.primario' para evitar NullPointerException en el arranque
            if (this.primario == null || !esMismoNodo(estado, this.primario)) {
                this.secundario = estado;
                this.secundario.setEsPrimario(false);
            }
        }
    }

    @Override
    public void run() {
    while (activo) {
        try {
            Thread.sleep(1000);
            System.out.println("[HeartbeatChecker]  Nodos registrados: " + latidos.size());
            long ahora = System.currentTimeMillis();

            // Si no hay ningún servidor registrado, espera
            if (latidos.isEmpty()) {
                System.out.println("[HeartbeatChecker] Esperando latidos de servidores...");
                continue;
            }

            for (String clave : new ArrayList<>(latidos.keySet())) {
                    Long ultimoLatidoLocal = latidos.get(clave);

                    if (ultimoLatidoLocal != null && (ahora - ultimoLatidoLocal > timeout)) {
                        System.out.println("[HeartbeatChecker] ¡SILENCIO DETECTADO! Falla en nodo: " + clave);

                        synchronized (this) {
                            NodoEstadoDTO nodoCaido = nodos.get(clave);

                            // Si el nodo que se cayó es el que consideramos primario
                            if (nodoCaido != null && primario != null && esMismoNodo(nodoCaido, primario)) {
                                if (secundario != null) {
                                    System.out.println("[HeartbeatChecker] Iniciando conmutación de fallo (Failover)...");
                                    gestorFalla.procesarFalla(nodoCaido, primario, secundario);

                                    // Intercambio seguro de roles lógicos localmente para que el monitor siga operando sano
                                    NodoEstadoDTO temp = primario;
                                    primario = secundario;
                                    primario.setEsPrimario(true);
                                    secundario = temp;
                                    secundario.setEsPrimario(false);
                                } else {
                                    System.err.println("[HeartbeatChecker] El servidor primario cayó y no hay secundario registrado.");
                                    primario = null;
                                    gestorFalla.procesarFalla(nodoCaido, primario, secundario);
                                }
                            }

                            if (nodoCaido != null && secundario != null && esMismoNodo(nodoCaido, secundario)) {
                                System.out.println("[HeartbeatChecker] Secundario caído. Limpiando referencia...");
                                secundario = null;
                            }

                            // Limpieza del mapa
                            latidos.remove(clave);
                            nodos.remove(clave);
                        }
                    }
                }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            activo = false;
        }
    }
}

    public NodoEstadoDTO getSecundario() {
        return secundario;
    }

    private boolean esMismoNodo(NodoEstadoDTO nodo1, NodoEstadoDTO nodo2) {
        if (nodo1 == null || nodo2 == null) return false;
        return nodo1.getIp().equals(nodo2.getIp()) && nodo1.getPuerto() == nodo2.getPuerto();
    }

    public NodoEstadoDTO obtenerPareja(NodoEstadoDTO emisor){
        if(primario != null && esMismoNodo(emisor, this.primario))
            return this.secundario; // si es el primario, le devuelvo el secundario para que sepa a quien enviarle la fila
        else 
            return this.primario; // si es secundario, le devuelvo el primario para que actualice su estado
    }

}
