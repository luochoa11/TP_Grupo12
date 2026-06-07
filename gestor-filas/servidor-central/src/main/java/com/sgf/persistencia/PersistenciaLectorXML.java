package com.sgf.persistencia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.sgf.modelos.Turno;

public class PersistenciaLectorXML implements IPersistenciaLector{
    private final String PATH_FILA = "filaEspera.xml";
    private final String PATH_HISTORIAL = "historial.xml";
    private final String PATH_TURNOS_ACTUALES = "turnosActuales.xml";
    private final String PATH_ULTIMO_LLAMADO = "ultimoLlamado.xml";
    private final String PATH_REINTENTOS = "historialReintentos.xml";

    @Override
    public List<Turno> recuperarFilaEspera() throws Exception {
        return leerLista(PATH_FILA);
    }

    @Override
    public List<Turno> recuperarHistorial() throws Exception {
        return leerLista(PATH_HISTORIAL);
    }

    @Override
    public List<Turno> recuperarTurnosActuales() throws Exception {
        return leerLista(PATH_TURNOS_ACTUALES);
    }

    @Override
    public Turno recuperarUltimoLlamado() throws Exception {
        File file = new File(PATH_ULTIMO_LLAMADO);
        if (!file.exists()) return null;
        return parsearUnicoTurno(file);
    }

    @Override
    public List<Turno> recuperarHistorialReintentos() throws Exception {
        return leerLista(PATH_REINTENTOS);
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
