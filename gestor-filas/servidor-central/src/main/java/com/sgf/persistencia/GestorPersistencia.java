package com.sgf.persistencia;

import java.util.List;

import com.sgf.interfaces.IFactoryPersistencia;
import com.sgf.interfaces.IPersistenciaStrategy;
import com.sgf.modelos.Turno;

/**
 * Gestor del Servidor Central encargado de encapsular la fábrica activa.
 */
public class GestorPersistencia {

    private IFactoryPersistencia factoryActiva; 
    private String formatoActivo;

    public GestorPersistencia(String formatoInicial) {
        establecerFormato(formatoInicial);
    }

    /**
     * Intercambia la fábrica concreta en caliente.
     */
    public synchronized void establecerFormato(String formato) {
        this.formatoActivo = formato.toUpperCase();
        switch (this.formatoActivo) {
            case "XML":
                this.factoryActiva = new FactoryPersistenciaXML();
                break;
            case "TXT":
                this.factoryActiva = new FactoryPersistenciaTextoPlano();
                break;
            case "JSON":
            default:
                this.factoryActiva = new FactoryPersistenciaJSON();
                this.formatoActivo = "JSON"; // Sanitizar fallback
                break;
        }
        System.out.println("[GestorPersistencia] Abstract Factory configurada con éxito: " + this.formatoActivo);
    }

    public synchronized String getFormatoActivo() {
        return this.formatoActivo;
    }

    /**
     * Método privado para obtener el producto abstracto creado por la fábrica activa actualmente.
     */
    private IPersistenciaStrategy getStrategy() {
        return factoryActiva.crearPersistencia();
    }

    
    // =========================================================================
    // Métodos de Delegación del Producto Abstracto
    // =========================================================================

    public synchronized void guardarFilaEspera(List<Turno> filaEspera) throws Exception {
        getStrategy().guardarFilaEspera(filaEspera);
    }

    public synchronized List<Turno> recuperarFilaEspera() throws Exception {
        return getStrategy().recuperarFilaEspera();
    }

    public synchronized void guardarHistorial(List<Turno> historial) throws Exception {
        getStrategy().guardarHistorial(historial);
    }

    public synchronized List<Turno> recuperarHistorial() throws Exception {
        return getStrategy().recuperarHistorial();
    }

    public synchronized void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception {
        getStrategy().guardarTurnosActuales(turnosActuales);
    }

    public synchronized List<Turno> recuperarTurnosActuales() throws Exception {
        return getStrategy().recuperarTurnosActuales();
    }

    public synchronized void guardarUltimoLlamado(Turno ultimoLlamado) throws Exception {
        getStrategy().guardarUltimoLlamado(ultimoLlamado);
    }

    public synchronized Turno recuperarUltimoLlamado() throws Exception {
        return getStrategy().recuperarUltimoLlamado();
    }
}