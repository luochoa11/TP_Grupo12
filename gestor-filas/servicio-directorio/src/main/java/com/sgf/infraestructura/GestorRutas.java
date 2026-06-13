package com.sgf.infraestructura;

import com.sgf.interfaces.IServicioDirectorio;
import com.sgf.seguridad.SeguridadDirectorio;

/**
 * Implementación del Servicio de Directorio.
 * Realiza la lógica de guardado de IPs 
 * <<Solo guarda, funciona como libreta de direcciones donde quiera que esté>>
 */

public class GestorRutas implements IServicioDirectorio{

    private String ipPrimario;
    private int puertoPrimario;
    private String ipSecundario;
    private int puertoSecundario;
    private final SeguridadDirectorio seguridad;

    //Los servidores se registran
    public GestorRutas(SeguridadDirectorio seguridad) {
        this.ipPrimario = null;
        this.puertoPrimario = -1;
        this.ipSecundario = null;
        this.puertoSecundario = -1;
        this.seguridad = seguridad;
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

// @Override
    public synchronized void actualizarPrimario(String ip, int puerto) {
        this.ipPrimario      = this.ipSecundario;
        this.puertoPrimario  = this.puertoSecundario;
        this.ipSecundario    = null; // lo dejamos null, el server tiene que registrarse de nuevo
        this.puertoSecundario = -1;

        System.out.println("[Directorio] Swap realizado. Nuevo primario -> " 
            + this.ipPrimario + ":" + this.puertoPrimario);
    }

    @Override
    public String getIPSecundario() { return ipSecundario; }

    @Override
    public int getPuertoSecundario() { return puertoSecundario; }

    @Override
    public String getAlgoritmoSeguridad() { return seguridad.getAlgoritmo(); }

    @Override
    public String getClaveSeguridad() { return seguridad.getClave(); }

}
