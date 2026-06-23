package com.sgf.infraestructura;

import com.sgf.interfaces.IServicioDirectorio;

/**
 * Implementación del Servicio de Directorio.
 * Realiza la lógica de guardado de IPs 
 */

public class GestorRutas implements IServicioDirectorio{
    private String ipPrimario;
    private int puertoPrimario;
    private String ipSecundario;
    private int puertoSecundario;

    //Los servidores se registran
    public GestorRutas() {
        this.ipPrimario = null;
        this.puertoPrimario = -1;
        this.ipSecundario = null;
        this.puertoSecundario = -1;
    }
    
    public synchronized String registrar(String ip, int puerto) {
        if (ipPrimario == null) {
            this.ipPrimario     = ip;
            this.puertoPrimario = puerto;
            System.out.println("[Directorio] Registrado PRIMARIO -> " + ip + ":" + puerto);
            return "PRIMARIO";
        } else {
            this.ipSecundario     = ip;
            this.puertoSecundario = puerto;
            System.out.println("[Directorio] Registrado SECUNDARIO -> " + ip + ":" + puerto);
            return "SECUNDARIO";
        }
    }

    @Override
    public String getIPPrimario(){
        return ipPrimario;
    }

    @Override
    public int getPuertoPrimario(){
        return puertoPrimario;
    }

    public synchronized void actualizarPrimario(String ip, int puerto) {
        this.ipPrimario      = ip;
        this.puertoPrimario  = puerto;
        
        // Si la IP que subió a primario era nuestra IP secundaria actual, vaciamos el secundario
        if (this.ipSecundario != null && this.ipSecundario.equals(ip) && this.puertoSecundario == puerto) {
            this.ipSecundario    = null; 
            this.puertoSecundario = -1;
        }
        System.out.println("[Directorio] Swap realizado. Nuevo primario -> "+ this.ipPrimario + ":" + this.puertoPrimario);
    }

    @Override
    public String getIPSecundario() { return ipSecundario; }

    @Override
    public int getPuertoSecundario() { return puertoSecundario; }

    public synchronized void limpiarSecundario() {
        System.out.println("[Directorio] Secundario eliminado -> " + ipSecundario + ":" + puertoSecundario);
        this.ipSecundario     = null;
        this.puertoSecundario = -1;
    }

    public synchronized void limpiarPrimario() {
        System.out.println("[Directorio] Primario eliminado (Blackout) -> " + ipPrimario + ":" + puertoPrimario);
        this.ipPrimario = null;
        this.puertoPrimario = -1;
    }

}
