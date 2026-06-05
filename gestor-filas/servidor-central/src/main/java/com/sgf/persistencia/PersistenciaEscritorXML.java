package com.sgf.persistencia;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import com.sgf.modelos.Turno;

public class PersistenciaEscritorXML implements IPersistenciaEscritor{
    private final String PATH_FILA = "filaEspera.xml";
    private final String PATH_HISTORIAL = "historial.xml";
    private final String PATH_TURNOS_ACTUALES = "turnosActuales.xml";
    private final String PATH_ULTIMO_LLAMADO = "ultimoLlamado.xml";

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
        sb.append("    <estado>").append(t.getEstado()).append("</estado>\n");
        return sb.toString();
    }

    
}
