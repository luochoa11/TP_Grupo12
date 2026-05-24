package com.sgf.servicios;

import com.sgf.interfaces.IServicioAdministrador;

/**
 * Fachada Concreta que orquesta de forma simplificada los subsistemas del servidor.
 */
public class ServidorCentralFacade implements IServicioAdministrador {

    //simulacion el estado de los subsistemas, modificar cuando se implemente
    // los metodos de cifrado y de persistencia
    private String formatoPersistencia = "JSON";
    private String algoritmoCifrado = "AES-128";
    private String claveSecreta = "SeguridadSGF2026";

    public ServidorCentralFacade() {
        //modificar cuando se implementen los subsistemas
        System.out.println("[FACADE-SERVIDOR] Fachada del Servidor Central inicializada.");
    }

    @Override
    public boolean cambiarFormatoPersistencia(String tipoFormato) {
        System.out.println("[FACADE-SERVIDOR] Cambio de formato de persistencia solicitado -> Formato: " + tipoFormato);
        // Aquí es donde se llamará a la Abstract Factory
        this.formatoPersistencia = tipoFormato;
        return true; 
    }

    @Override
    public String getFormatoPersistenciaActivo() {
        return this.formatoPersistencia;
    }

    @Override
    public boolean actualizarConfiguracionSeguridad(String algoritmo, String claveSecreta) {
        System.out.println("[FACADE-SERVIDOR] Configuración de seguridad solicitada -> Algoritmo: " + algoritmo + " | Clave: " + claveSecreta);
        // Aquí es donde se actualizará el Strategy de encriptado
        this.algoritmoCifrado = algoritmo;
        this.claveSecreta = claveSecreta;
        return true;
    }

    @Override
    public String getAlgoritmoCifradoActivo() {
        return this.algoritmoCifrado;
    }

    @Override
    public String getClaveSecretaActiva() {
        return this.claveSecreta;
    }

    @Override
    public String[] obtenerConfiguracionCompleta() {
        // Retorna los tres valores de configuración en un solo viaje
        return new String[] {
            this.formatoPersistencia,
            this.algoritmoCifrado,
            this.claveSecreta
        };
    }
}