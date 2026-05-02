package com.sgf.infraestructura;

import com.sgf.interfaces.IServicioDirectorio;

/**
 * Implementación del Servicio de Directorio.
 * Mantiene en memoria la ubicación (IP y Puerto) del servidor que está operando
 * actualmente como Primario.
 */
public class GestorRutas implements IServicioDirectorio{

    private String ipPrimario;
    private int puertoPrimario;

    public GestorRutas() {

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
    public void actualizarPrimario(String ip, int puerto){
        this.ipPrimario = ip;
        this.puertoPrimario = puerto;
    }
}
