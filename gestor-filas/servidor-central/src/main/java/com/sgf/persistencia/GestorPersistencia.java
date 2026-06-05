package com.sgf.persistencia;

import java.util.List;

import com.sgf.modelos.Turno;

/**
 * Gestor del Servidor Central encargado de administrar qué familia de 
 * persistencia se encuentra activa en caliente.
 */
public class GestorPersistencia {

    private IFactoryPersistencia factoryActiva; 
    private String formatoActivo;

    public GestorPersistencia(String formatoInicial) {
        establecerFormato(formatoInicial);
    }

    /**
     * Reconfigura la fábrica concreta activa en tiempo de ejecución.
     */
    public synchronized void establecerFormato(String formato) {
        this.formatoActivo = formato.toUpperCase();
        switch (this.formatoActivo) {
            case "XML":
                this.factoryActiva = new FactoryXML();
                break;
            case "TXT":
                this.factoryActiva = new FactoryPlain();
                break;
            case "JSON":
            default:
                this.factoryActiva = new FactoryJSON();
                this.formatoActivo = "JSON"; // Sanitizar fallback
                break;
        }
        System.out.println("[GestorPersistencia] Fabrica configurada con éxito: " + this.formatoActivo);
    }

    public synchronized String getFormatoActivo() {
        return this.formatoActivo;
    }

    
    // =========================================================================
    // Métodos de Delegación de la Familia Activa
    // =========================================================================

    public synchronized void guardarFilaEspera(List<Turno> fila) throws Exception {
        //Fabricamos el escritor de la familia activa
        IPersistenciaEscritor escritor = factoryActiva.crearEscritor();
        //Ejecutamos la persistencia sin saber si es JSON, XML o TXT
        escritor.guardarFilaEspera(fila);
    }

    public synchronized List<Turno> recuperarFilaEspera() throws Exception {
        IPersistenciaLector lector = factoryActiva.crearLector();
        return lector.recuperarFilaEspera();
    }

    public synchronized void guardarHistorial(List<Turno> historial) throws Exception {
        factoryActiva.crearEscritor().guardarHistorial(historial);
    }

    public synchronized List<Turno> recuperarHistorial() throws Exception {
        return factoryActiva.crearLector().recuperarHistorial();
    }

    public synchronized void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception {
        factoryActiva.crearEscritor().guardarTurnosActuales(turnosActuales);
    }

    public synchronized List<Turno> recuperarTurnosActuales() throws Exception {
        return factoryActiva.crearLector().recuperarTurnosActuales();
    }

    public synchronized void guardarUltimoLlamado(Turno ultimoLlamado) throws Exception {
        factoryActiva.crearEscritor().guardarUltimoLlamado(ultimoLlamado);
    }

    public synchronized Turno recuperarUltimoLlamado() throws Exception {
        return factoryActiva.crearLector().recuperarUltimoLlamado();
    }
}