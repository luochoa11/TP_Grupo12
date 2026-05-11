package com.sgf.infraestructura;

import com.sgf.interfaces.IServicioDirectorio;

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


    //Los servidores se registran
    public GestorRutas() {
        this.ipPrimario = null;
        this.puertoPrimario = -1;
        this.ipSecundario = null;
        this.puertoSecundario = -1;
    }
    
    public synchronized void registrar(String ip, int puerto) {
        if (ipPrimario == null) {
            this.ipPrimario     = ip;
            this.puertoPrimario = puerto;
            System.out.println("[Directorio] Registrado PRIMARIO → " + ip + ":" + puerto);
        } else {
            this.ipSecundario     = ip;
            this.puertoSecundario = puerto;
            System.out.println("[Directorio] Registrado SECUNDARIO → " + ip + ":" + puerto);
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

 @Override
    public synchronized void actualizarPrimario(String ip, int puerto) {
        // Swap: el secundario pasa a ser primario
        String ipAux    = this.ipPrimario;
        int    puertoAux = this.puertoPrimario;

        this.ipPrimario      = this.ipSecundario;
        this.puertoPrimario  = this.puertoSecundario;
        this.ipSecundario    = ipAux;
        this.puertoSecundario = puertoAux;

        System.out.println("[Directorio] Swap realizado. Nuevo primario → " 
            + this.ipPrimario + ":" + this.puertoPrimario);
    }
    
    //despues??? ver que actualizacion hacer
    /**
    @Override
    public void actualizarPrimario(String ip, int puerto){
        this.ipSecundario = this.ipPrimario;
        this.puertoSecundario = this.puertoPrimario;    
        this.ipPrimario = ip;
        this.puertoPrimario = puerto;
    }
    */

    public String getIPSecundario() {
        return ipSecundario;
    }

    public int getPuertoSecundario() {
        return puertoSecundario;
    }

}
