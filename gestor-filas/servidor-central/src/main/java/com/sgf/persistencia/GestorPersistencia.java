package com.sgf.persistencia;

import java.io.File;
import java.util.List;

import com.sgf.modelos.Turno;

/**
 * Gestor del Servidor Central encargado de administrar qué familia de 
 * persistencia se encuentra activa en caliente.
 * Recibe turnos, historiales y filas desde el servidor y delega la persistencia
 */
public class GestorPersistencia {

    private IFactoryPersistencia factoryActiva; 
    private String formatoActivo;

    // Archivos testigos para verificar la última configuración de guardado física
    private static final String FILE_JSON = "filaEspera.json";
    private static final String FILE_XML  = "filaEspera.xml";
    private static final String FILE_DAT  = "filaEspera.dat";

    public GestorPersistencia() {
        String formatoDetectado = detectarFormatoExistente();
        System.out.println("[GestorPersistencia] Formato detectado en disco al iniciar: " + formatoDetectado);
        establecerFormato(formatoDetectado);
    }

    /**
     * Táctica de Disponibilidad: Resincronización de estado.
     * Barre la carpeta raíz buscando archivos de datos guardados en ejecuciones previas.
     * Prioridad de carga: DAT (binario) -> XML -> JSON.
     */
    public final String detectarFormatoExistente() {
        if (new File(FILE_DAT).exists()) {
            return "TXT";
        }
        if (new File(FILE_XML).exists()) {
            return "XML";
        }
        if (new File(FILE_JSON).exists()) {
            return "JSON";
        }
        // Fallback: Si el sistema se ejecuta por primera vez en limpio, inicia con JSON
        return "JSON";
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
                this.formatoActivo = "JSON"; 
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

    public synchronized void guardarHistorialReintentos(List<Turno> historialReintentos) throws Exception {
        factoryActiva.crearEscritor().guardarHistorialReintentos(historialReintentos);
    }

    public synchronized List<Turno> recuperarHistorialReintentos() throws Exception {
        return factoryActiva.crearLector().recuperarHistorialReintentos();
    }
}