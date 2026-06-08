package com.sgf.persistencia;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.sgf.modelos.Turno;

public class PersistenciaEscritorJSON implements IPersistenciaEscritor {
    private final String PATH_FILA;
    private final String PATH_HISTORIAL;
    private final String PATH_TURNOS_ACTUALES;
    private final String PATH_ULTIMO_LLAMADO;
    private final String PATH_REINTENTOS;

    private final String rutaBase;
    
    public PersistenciaEscritorJSON(String rutaBase) {
        this.rutaBase = (rutaBase == null || rutaBase.trim().isEmpty()) ? "" : (rutaBase + File.separator);
        this.PATH_FILA = rutaBase + "filaEspera.json";
        this.PATH_HISTORIAL = rutaBase + "historial.json";
        this.PATH_TURNOS_ACTUALES = rutaBase + "turnosActuales.json";
        this.PATH_ULTIMO_LLAMADO = rutaBase + "ultimoLlamado.json";
        this.PATH_REINTENTOS = rutaBase + "historialReintentos.json";

        File dirHistorico = new File(this.rutaBase+"historico");
        if(!dirHistorico.exists()){
            dirHistorico.mkdirs();
        }
    }
    
    @Override
    public void guardarFilaEspera(List<Turno> filaEspera) throws Exception  {
        escribirLista(PATH_FILA, filaEspera);
    }

    @Override
    public void guardarHistorial(List<Turno> historial) throws Exception {
        escribirLista(PATH_HISTORIAL, historial);
    }

    @Override
    public void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception {
        escribirLista(PATH_TURNOS_ACTUALES, turnosActuales);
    }

    @Override
    public void guardarUltimoLlamado(Turno ultimoLlamado) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(PATH_ULTIMO_LLAMADO))) {
            if (ultimoLlamado != null) {
                out.print(turnoToJson(ultimoLlamado));
            } else {
                out.print("{}");
            }
        }
    }

    @Override
    public void guardarHistorialReintentos(List<Turno> historialReintentos) throws Exception {
        escribirLista(PATH_REINTENTOS, historialReintentos);
    }

    //----------guardado en frío----------------
    @Override
    public synchronized void registrarTurnoFinalizado(Turno turno) throws Exception {
        if (turno == null) return;
        
        // Rotación de Logs por año y mes (ej: "auditoria_2026_06.json")
        String mesAnio = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM"));
        String pathHistorico = this.rutaBase + "historico" + File.separator + "auditoria_" + mesAnio + ".json";
        
        // Abrimos en modo APPEND (segundo parámetro true) para agregar al final de forma eficiente
        try (PrintWriter out = new PrintWriter(new FileWriter(pathHistorico, true))) {
            out.println(turnoToJson(turno));
        } catch (Exception e) {
            throw new Exception("Error al escribir log de auditoría JSON: " + e.getMessage());
        }
    }
    //---------------------------------------

    private void escribirLista(String path, List<Turno> lista) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.println("[");
            for (int i = 0; i < lista.size(); i++) {
                out.print("  " + turnoToJson(lista.get(i)));
                if (i < lista.size() - 1) out.println(",");
                else out.println();
            }
            out.println("]");
        } catch (Exception e) {
            throw new Exception("Error al escribir en " + path + ": " + e.getMessage());
        }
    }

    private String turnoToJson(Turno t) {
        return String.format(
            "{\"dniCliente\":\"%s\", \"idPuesto\":%d, \"intentos\":%d, \"tiempoCreacion\":\"%s\", \"tiempoLlamado\":\"%s\", \"tiempoAtendido\":\"%s\", \"estado\":\"%s\"}",
            t.getDniCliente(), t.getIdPuesto(), t.getIntentos(), 
            FechaUtil.formatearMilis(t.getTiempoCreacion()), 
            FechaUtil.formatearMilis(t.getTiempoLlamado()), 
            FechaUtil.formatearMilis(t.getTiempoAtendido()), 
            t.getEstado()
        );
    }
}
