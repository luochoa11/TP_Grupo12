package com.sgf.salud;

import com.sgf.failover.GestorFalla;

import com.sgf.modelos.HeartbeatDTO;
import com.sgf.modelos.NodoEstadoDTO;

/**
 * Clase responsable de verificar los latidos (heartbeats) de los servidores en el sistema.
 * Si hay silencio, reporta falla.
 */
public class HeartbeatChecker implements Runnable {
    // Debe correr un hilo que verifique el tiempo transcurrido desde el último latido del Servidor Primario.
    private long ultimoLatido;
    private boolean activo = true;
    private final long timeout=5000; // 5 segundos
    private GestorFalla gestorFalla;

    public HeartbeatChecker(GestorFalla gestorFalla) {
        this.gestorFalla = gestorFalla;
        this.ultimoLatido = System.currentTimeMillis();
    }

    public synchronized void recibirLatido(HeartbeatDTO hb, NodoEstadoDTO estado) {
        ultimoLatido = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while(activo){
            try{
                Thread.sleep(1000);
                if(System.currentTimeMillis() - ultimoLatido > timeout){
                    System.out.println("No se recibió latido en el tiempo esperado. Reportando falla...");
                    gestorFalla.procesarFalla();
                    activo = false; // Detener el checker después de reportar la falla
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
