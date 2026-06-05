package com.sgf.servicios;

import java.util.ArrayList;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.interfaces.IServicioAdministrador;
import com.sgf.modelos.Turno;
import com.sgf.persistencia.GestorPersistencia;

/**
 * Fachada Concreta que orquesta de forma simplificada los subsistemas del servidor.
 */
public class ServidorCentralFacade implements IServicioAdministrador {

    private final GestorPersistencia gestorPersistencia;
    private final ILogicaFila logicaFila;

    //simulacion el estado de los subsistemas, modificar cuando se implemente SEGURIDAD
    private String algoritmoCifrado = "AES-128";
    private String claveSecreta = "SeguridadSGF2026";

    public ServidorCentralFacade(GestorPersistencia gestorPersistencia, ILogicaFila logicaFila) {
        this.gestorPersistencia = gestorPersistencia;
        this.logicaFila = logicaFila;
        System.out.println("[FACADE-SERVIDOR] Fachada del Servidor Central inicializada.");
    }

    @Override
    public boolean cambiarFormatoPersistencia(String tipoFormato) {
        System.out.println("[FACADE-SERVIDOR] Cambio de formato de persistencia solicitado -> Formato: " + tipoFormato);
        try {
            // 1. Mutar la fábrica activa
            gestorPersistencia.establecerFormato(tipoFormato);

            // 2. Ejecutar migración en caliente en un solo paso transaccional
            // Guardamos el estado actual que el servidor tiene en memoria RAM en el nuevo formato físico de archivos
            gestorPersistencia.guardarFilaEspera(logicaFila.getCola());
            gestorPersistencia.guardarHistorial(logicaFila.getHistorial());

            ArrayList<Turno> turnosActivosPlano = new ArrayList<>(logicaFila.getTurnosActivos().values());
            gestorPersistencia.guardarTurnosActuales(turnosActivosPlano);
            
            gestorPersistencia.guardarUltimoLlamado(logicaFila.getUltimoLlamado());

            System.out.println("[FACADE-SERVIDOR] Migración y guardado completados con éxito para: " + tipoFormato);
            return true;

        } catch (Exception e) {
            System.err.println("[FACADE-SERVIDOR] Error crítico durante la migración en caliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getFormatoPersistenciaActivo() {
        return gestorPersistencia.getFormatoActivo();
    }

    @Override
    public boolean actualizarConfiguracionSeguridad(String algoritmo, String claveSecreta) {
        System.out.println("[FACADE-SERVIDOR] Configuración de seguridad solicitada -> Algoritmo: " + algoritmo + " | Clave: " + claveSecreta);
        // Aquí es donde se actualizará el Strategy de encriptado real
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
            gestorPersistencia.getFormatoActivo(),
            this.algoritmoCifrado,
            this.claveSecreta
        };
    }
}