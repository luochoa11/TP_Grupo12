package com.sgf.infraestructura;

import com.sgf.Constantes;
import com.sgf.interfaces.IServicioDirectorio;

/**
 * Implementación del Servicio de Directorio.
 * Mantiene en memoria la ubicación (IP y Puerto) del servidor que está operando
 * actualmente como Primario.
 */

public class GestorRutas implements IServicioDirectorio{

    private String ipPrimario;
    private int puertoPrimario;
    private String ipSecundario;
    private int puertoSecundario;

    public GestorRutas() {
    	this.ipPrimario = Constantes.HOST_SERVIDOR_CENTRAL;
        this.puertoPrimario = Constantes.PUERTO_SERVIDOR_CENTRAL;
        
        this.ipSecundario = Constantes.HOST_SERVIDOR_B;
        this.puertoSecundario = Constantes.PUERTO_SERVIDOR_B;
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
    	String ipAUX;
    	int puertoAUX;    	
    	ipAUX = this.ipPrimario;
        puertoAUX = this.puertoPrimario;    
        this.ipPrimario = this.ipSecundario;
        this.puertoPrimario = this.puertoSecundario;
        this.ipSecundario = ipAUX;
        this.puertoSecundario = puertoAUX;
    }    
    
    
    //despues ver que actualizacion hacer
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
