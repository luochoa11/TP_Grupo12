package com.sgf.persistencia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.sgf.modelos.Turno;

public class PersistenciaLectorJSON implements IPersistenciaLector {
    private final String PATH_FILA = "filaEspera.json";
    private final String PATH_HISTORIAL = "historial.json";
    private final String PATH_TURNOS_ACTUALES = "turnosActuales.json";
    private final String PATH_ULTIMO_LLAMADO = "ultimoLlamado.json";
    private final String PATH_REINTENTOS = "historialReintentos.json";

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
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea = br.readLine();
            if (linea == null || linea.equals("{}") || linea.trim().isEmpty()) return null;
            return jsonToTurno(linea);
        } catch (Exception e) {
            throw new Exception("Error al leer el archivo " + PATH_ULTIMO_LLAMADO + ": " + e.getMessage());
        }
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
            while ((linea = br.readLine()) != null) {
                if (linea.contains("dniCliente")) {
                    lista.add(jsonToTurno(linea));
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al leer el archivo " + path + ": " + e.getMessage());
        }
        return lista;
    }

    private Turno jsonToTurno(String json) {
        String dni = extraerValorJson(json, "\"dniCliente\":\"");
        int idPuesto = Integer.parseInt(extraerValorJsonNum(json, "\"idPuesto\":"));
        int intentos = Integer.parseInt(extraerValorJsonNum(json, "\"intentos\":"));
        String estado = extraerValorJson(json, "\"estado\":\"");

        Turno t = new Turno(dni);
        t.setIdPuesto(idPuesto);
        t.setIntentos(intentos);
        t.setEstado(estado);
        return t;
    }
    
    private String extraerValorJson(String json, String clave) {
        int inicio = json.indexOf(clave) + clave.length();
        int fin = json.indexOf("\"", inicio);
        return json.substring(inicio, fin);
    }

    private String extraerValorJsonNum(String json, String clave) {
        int inicio = json.indexOf(clave) + clave.length();
        int fin = json.indexOf(",", inicio);
        if (fin == -1) fin = json.indexOf("}", inicio); // Por si es el último elemento
        return json.substring(inicio, fin).trim();
    }
}

