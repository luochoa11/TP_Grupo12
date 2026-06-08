package com.sgf.persistencia;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import com.sgf.modelos.Turno;

public class PersistenciaEscritorXML implements IPersistenciaEscritor{
    private final String PATH_FILA;
    private final String PATH_HISTORIAL;
    private final String PATH_TURNOS_ACTUALES;
    private final String PATH_ULTIMO_LLAMADO;
    private final String PATH_REINTENTOS;

    public PersistenciaEscritorXML(String rutaBase) {
        this.PATH_FILA = rutaBase + "filaEspera.xml";
        this.PATH_HISTORIAL = rutaBase + "historial.xml";
        this.PATH_TURNOS_ACTUALES = rutaBase + "turnosActuales.xml";
        this.PATH_ULTIMO_LLAMADO = rutaBase + "ultimoLlamado.xml";
        this.PATH_REINTENTOS = rutaBase + "historialReintentos.xml";
    }
    @Override
    public void guardarFilaEspera(List<Turno> filaEspera) throws Exception {
        escribirLista(PATH_FILA, "filaEspera", filaEspera);
    }

    @Override
    public void guardarHistorial(List<Turno> historial) throws Exception {
        escribirLista(PATH_HISTORIAL, "historial", historial);
    }

    @Override
    public void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception {
        escribirLista(PATH_TURNOS_ACTUALES, "turnosActuales", turnosActuales);
    }

    @Override
    public void guardarUltimoLlamado(Turno ultimoLlamado) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(PATH_ULTIMO_LLAMADO))) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            if (ultimoLlamado != null) {
                out.println("<ultimoLlamado>");
                out.print(turnoToXmlTags(ultimoLlamado));
                out.println("</ultimoLlamado>");
            } else {
                out.println("<ultimoLlamado />");
            }
        }
    }

    @Override
    public void guardarHistorialReintentos(List<Turno> historialReintentos) throws Exception {
        escribirLista(PATH_REINTENTOS, "historialReintentos", historialReintentos);
    }
    
    private void escribirLista(String path, String rootTag, List<Turno> lista) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<" + rootTag + ">");
            for (Turno t : lista) {
                out.println("  <turno>");
                out.print(turnoToXmlTags(t));
                out.println("  </turno>");
            }
            out.println("</" + rootTag + ">");
        }
    }

    private String turnoToXmlTags(Turno t) {
        StringBuilder sb = new StringBuilder();
        sb.append("    <dniCliente>").append(t.getDniCliente()).append("</dniCliente>\n");
        sb.append("    <idPuesto>").append(t.getIdPuesto()).append("</idPuesto>\n");
        sb.append("    <intentos>").append(t.getIntentos()).append("</intentos>\n");
        // Escribimos marcas estructuradas delegando en FechaUtil
        sb.append("    <tiempoCreacion>").append(FechaUtil.formatearMilis(t.getTiempoCreacion())).append("</tiempoCreacion>\n");
        sb.append("    <tiempoLlamado>").append(FechaUtil.formatearMilis(t.getTiempoLlamado())).append("</tiempoLlamado>\n");
        sb.append("    <tiempoAtendido>").append(FechaUtil.formatearMilis(t.getTiempoAtendido())).append("</tiempoAtendido>\n");
        
        sb.append("    <estado>").append(t.getEstado()).append("</estado>\n");
        return sb.toString();
    }

    
}
