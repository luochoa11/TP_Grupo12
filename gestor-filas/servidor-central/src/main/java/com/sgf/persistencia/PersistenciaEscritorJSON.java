package com.sgf.persistencia;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import com.sgf.modelos.Turno;

public class PersistenciaEscritorJSON implements IPersistenciaEscritor {
    private final String PATH_FILA = "filaEspera.json";
    private final String PATH_HISTORIAL = "historial.json";
    private final String PATH_TURNOS_ACTUALES = "turnosActuales.json";
    private final String PATH_ULTIMO_LLAMADO = "ultimoLlamado.json";
    private final String PATH_REINTENTOS = "historialReintentos.json";
    
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
