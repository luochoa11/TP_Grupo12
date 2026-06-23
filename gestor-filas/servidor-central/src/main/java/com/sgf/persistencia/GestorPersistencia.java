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
    private final String rutaBase;

    public GestorPersistencia(int puerto) {
        this.rutaBase = "config" + File.separator + "servidor_" + puerto + File.separator;
        File carpetaNodo = new File(this.rutaBase);
        if(!carpetaNodo.exists()){
            carpetaNodo.mkdirs();
        }
        String formatoDetectado = detectarFormatoExistente();
        System.out.println("[GestorPersistencia] Formato detectado en disco al iniciar en: " + this.rutaBase + ": "+ formatoDetectado);
        establecerFormato(formatoDetectado);
    }

    /**
     * Táctica de Disponibilidad: Resincronización de estado.
     * Busca los archivos de datos en el directorio aislado de este nodo.
     * Prioridad de carga: DAT (binario) -> XML -> JSON.
     */
    public final String detectarFormatoExistente(){
        if (new File(this.rutaBase + "filaEspera.txt").exists()) {
            return "TXT";
        }
        if (new File(this.rutaBase + "filaEspera.xml").exists()) {
            return "XML";
        }
        if (new File(this.rutaBase + "filaEspera.json").exists()) {
            return "JSON";
        }
        return "JSON";
    }

    /**
     * Reconfigura la fábrica concreta activa en tiempo de ejecución.
     */
    public synchronized void establecerFormato(String formato) {
        this.formatoActivo = formato.toUpperCase();
        switch (this.formatoActivo) {
            case "XML":
                this.factoryActiva = new FactoryXML(this.rutaBase);
                break;
            case "TXT":
                this.factoryActiva = new FactoryPlain(this.rutaBase);
                break;
            case "JSON":
            default:
                this.factoryActiva = new FactoryJSON(this.rutaBase);
                this.formatoActivo = "JSON"; 
                break;
        }
        System.out.println("[GestorPersistencia] Fabrica configurada con éxito: " + this.formatoActivo);
    }

    public synchronized String getFormatoActivo() {
        return this.formatoActivo;
    }

    /**
     * Limpia los archivos físicos obsoletos tras una migración exitosa.
     */
    public synchronized void clearOlds(String tipoFormato) {
        if (tipoFormato == null) {
            return;
        }
        String tipo = tipoFormato.toUpperCase();
        String[] bases = new String[] { "filaEspera", "historial", "historialReintentos", "turnosActuales", "ultimoLlamado" };
        String[] formatos = new String[] { "JSON", "XML", "TXT" };

        for (String base : bases) {
            for (String f : formatos) {
                if (!f.equals(tipo)) {
                    String ext = f.toLowerCase();
                    File fichero = new File(this.rutaBase + base + "." + ext);
                    if (fichero.exists()) {
                        boolean eliminado = fichero.delete();
                        System.out.println("[GestorPersistencia] Eliminando " + fichero.getPath() + ": " + (eliminado ? "OK" : "FALLO"));
                    }
                }
            }
        }
        
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

    public synchronized void registrarTurnoFinalizado(Turno turno) throws Exception {
        if (turno != null) {
            factoryActiva.crearEscritor().registrarTurnoFinalizado(turno);
        }
    }
}