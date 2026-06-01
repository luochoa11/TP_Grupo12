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

public class PersistenciaJSON implements IPersistenciaStrategy {
    private final String PATH_FILA = "filaEspera.json";
    private final String PATH_HISTORIAL = "historial.json";
     //private final String PATH_NOTIFICACIONES = "notificaciones.json";
    private final String PATH_TURNOS_ACTUALES = "turnosActuales.json";
    private final String PATH_ULTIMO_LLAMADO = "ultimoLlamado.json";

    @Override
    public void guardarFilaEspera(List<Turno> filaEspera) throws Exception  {
        escribirLista(PATH_FILA, filaEspera);
    }

    @Override
    public List<Turno> recuperarFilaEspera() throws Exception {
        return leerLista(PATH_FILA);
    }

    @Override
    public void guardarHistorial(List<Turno> historial) throws Exception {
        escribirLista(PATH_HISTORIAL, historial);
    }

    @Override
    public List<Turno> recuperarHistorial() throws Exception {
        return leerLista(PATH_HISTORIAL);
    }

//     @Override
//    public void guardarNotificaciones(List<Turno> notificaciones) throws Exception {
        
//    }

//     @Override
//     public List<Turno> recuperarNotificaciones() throws Exception {
//        
//     }

    @Override
    public void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception {
        escribirLista(PATH_TURNOS_ACTUALES, turnosActuales);
    }

    @Override
    public List<Turno> recuperarTurnosActuales() throws Exception {
        return leerLista(PATH_TURNOS_ACTUALES);
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

    private String turnoToJson(Turno t) {
        return String.format(
            "{\"dniCliente\":\"%s\", \"idPuesto\":%d, \"intentos\":%d, \"estado\":\"%s\"}",
            t.getDniCliente(), t.getIdPuesto(), t.getIntentos(), t.getEstado()
        );
    
    }
    private Turno jsonToTurno(String json) {
        String dni = extraherValorJson(json, "\"dniCliente\":\"");
        int idPuesto = Integer.parseInt(extraherValorJsonNum(json, "\"idPuesto\":"));
        int intentos = Integer.parseInt(extraherValorJsonNum(json, "\"intentos\":"));
        String estado = extraherValorJson(json, "\"estado\":\"");

        Turno t = new Turno(dni);
        t.setIdPuesto(idPuesto);
        t.setIntentos(intentos);
        t.setEstado(estado);
        return t;
    }
    private String extraherValorJson(String json, String clave) {
        int inicio = json.indexOf(clave) + clave.length();
        int fin = json.indexOf("\"", inicio);
        return json.substring(inicio, fin);
    }

    private String extraherValorJsonNum(String json, String clave) {
        int inicio = json.indexOf(clave) + clave.length();
        int fin = json.indexOf(",", inicio);
        if (fin == -1) fin = json.indexOf("}", inicio); // Por si es el último elemento
        return json.substring(inicio, fin).trim();
    }
}

