package com.sgf.persistencia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.sgf.interfaces.IPersistenciaStrategy;
import com.sgf.modelos.Turno;

public class PersistenciaXML implements IPersistenciaStrategy {
    private final String PATH_FILA = "filaEspera.xml";
    private final String PATH_HISTORIAL = "historial.xml";
    private final String PATH_TURNOS_ACTUALES = "turnosActuales.xml";
    private final String PATH_ULTIMO_LLAMADO = "ultimoLlamado.xml";

    @Override
    public void guardarFilaEspera(List<Turno> filaEspera) throws Exception {
        escribirLista(PATH_FILA, "filaEspera", filaEspera);
    }

    @Override
    public List<Turno> recuperarFilaEspera() throws Exception {
       return leerLista(PATH_FILA);
    }

    @Override
    public void guardarHistorial(List<Turno> historial) throws Exception {
       escribirLista(PATH_HISTORIAL, "historial", historial);
    }

    @Override
    public List<Turno> recuperarHistorial() throws Exception {
       return leerLista(PATH_HISTORIAL);
    }

    @Override
    public void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception {
        escribirLista(PATH_TURNOS_ACTUALES, "turnosActuales", turnosActuales);
    }

    @Override
    public List<Turno> recuperarTurnosActuales() throws Exception {
        return leerLista(PATH_TURNOS_ACTUALES);
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
    public Turno recuperarUltimoLlamado() throws Exception {
        File file = new File(PATH_ULTIMO_LLAMADO);
        if (!file.exists()) return null;
        return parsearUnicoTurno(file);
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

    private List<Turno> leerLista(String path) throws Exception {
        List<Turno> lista = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            String dni = null, estado = null;
            int idPuesto = -1, intentos = 0;

            while ((linea = br.readLine()) != null) {
                if (linea.contains("<dniCliente>")) dni = extraerTag(linea, "dniCliente");
                if (linea.contains("<idPuesto>")) idPuesto = Integer.parseInt(extraerTag(linea, "idPuesto"));
                if (linea.contains("<intentos>")) intentos = Integer.parseInt(extraerTag(linea, "intentos"));
                if (linea.contains("<estado>")) {
                    estado = extraerTag(linea, "estado");
                    
                    Turno t = new Turno(dni);
                    t.setIdPuesto(idPuesto);
                    t.setIntentos(intentos);
                    t.setEstado(estado);
                    lista.add(t);
                }
            }
        }
        return lista;
    }

    private Turno parsearUnicoTurno(File file) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            String dni = null, estado = null;
            int idPuesto = -1, intentos = 0;
            boolean tieneDatos = false;
            while ((linea = br.readLine()) != null) {
                if (linea.contains("<dniCliente>")) { dni = extraerTag(linea, "dniCliente"); tieneDatos = true; }
                if (linea.contains("<idPuesto>")) idPuesto = Integer.parseInt(extraerTag(linea, "idPuesto"));
                if (linea.contains("<intentos>")) intentos = Integer.parseInt(extraerTag(linea, "intentos"));
                if (linea.contains("<estado>")) estado = extraerTag(linea, "estado");
            }
            if (!tieneDatos) return null;
            Turno t = new Turno(dni);
            t.setIdPuesto(idPuesto);
            t.setIntentos(intentos);
            t.setEstado(estado);
            return t;
        }
    }

    private String extraerTag(String linea, String tag) {
        int inicio = linea.indexOf("<" + tag + ">") + ("<" + tag + ">").length();
        int fin = linea.indexOf("</" + tag + ">");
        return linea.substring(inicio, fin).trim();
    }
}

