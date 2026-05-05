package com.sgf.salud;

import java.util.Map;

import com.sgf.failover.GestorFalla;

import com.sgf.modelos.HeartbeatDTO;
import com.sgf.modelos.NodoEstadoDTO;

/**
 * Clase responsable de verificar los latidos (heartbeats) de los servidores en el sistema.
 * Si hay silencio, reporta falla.
 */
public class HeartbeatChecker implements Runnable {
    // Debe correr un hilo que verifique el tiempo transcurrido desde el último latido del Servidor Primario.
    private Map<String, NodoEstadoDTO> nodos; // Mapa de nodos registrados por IP:Puerto
    private Map<String, Long> latidos; // Mapa de últimos latidos por IP:Puerto

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
        latidos.put(clave, hb.getTimestamp());

        if (this.primario==null) {
           this.primario = estado;
        } else if (!esMismoNodo(estado, this.primario)) {
            this.secundario = estado;
            this.secundario.setEsPrimario(false);
        }
    }

    @Override
    public void run() {
        while(activo){
            try{
                Thread.sleep(1000);
                
                long ahora = System.currentTimeMillis();

                for(String clave : latidos.keySet()){
                    long ultimoLatido = latidos.get(clave);

                    if(ahora - ultimoLatido > timeout){
                        System.out.println("Falla detectada en nodo: " + clave);
                        NodoEstadoDTO nodoCaido= nodos.get(clave);
                        
                        if(nodoCaido.getIp().equals(primario.getIp()) && nodoCaido.getPuerto() == primario.getPuerto()){
                            gestorFalla.procesarFalla(nodoCaido, primario, secundario);
                            NodoEstadoDTO aux=this.primario;
                            this.primario=this.secundario;
                            this.secundario=aux;
                        } 
                    }
                }



                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public NodoEstadoDTO getSecundario() {
        return secundario;
    }

    private boolean esMismoNodo(NodoEstadoDTO nodo1, NodoEstadoDTO nodo2) {
        return nodo1.getIp().equals(nodo2.getIp()) && nodo1.getPuerto() == nodo2.getPuerto();
    }

    public NodoEstadoDTO obtenerPareja(NodoEstadoDTO emisor){
        if(esMismoNodo(emisor, this.primario))
            return this.secundario; // si es el primario, le devuelvo el secundario para que sepa a quien enviarle la fila
        else 
            return this.primario; // si es secundario, le devuelvo el primario para que actualice su estado
      
    }

}
