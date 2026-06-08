package com.sgf.persistencia;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.sgf.modelos.Turno;

public class PersistenciaEscritorPlain implements IPersistenciaEscritor {
    private final String PATH_FILA;
    private final String PATH_HISTORIAL;
    private final String PATH_TURNOS_ACTUALES;
    private final String PATH_ULTIMO_LLAMADO;
    private final String PATH_REINTENTOS;

    public PersistenciaEscritorPlain(String rutaBase) {
        this.PATH_FILA = rutaBase + "filaEspera.dat";
        this.PATH_HISTORIAL = rutaBase + "historial.dat";
        this.PATH_TURNOS_ACTUALES = rutaBase + "turnosActuales.dat";
        this.PATH_ULTIMO_LLAMADO = rutaBase + "ultimoLlamado.dat";
        this.PATH_REINTENTOS = rutaBase + "historialReintentos.dat";
    }

    @Override
    public void guardarFilaEspera(List<Turno> filaEspera) throws Exception  {
        escribirObjeto(PATH_FILA, filaEspera);
    }

    @Override
    public void guardarHistorial(List<Turno> historial) throws Exception {
        escribirObjeto(PATH_HISTORIAL, historial);
    }

    @Override
    public void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception {
        escribirObjeto(PATH_TURNOS_ACTUALES, turnosActuales);
    }

    @Override
    public void guardarUltimoLlamado(Turno ultimoLlamado) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PATH_ULTIMO_LLAMADO))) {
            oos.writeObject(ultimoLlamado);
        }
    }

    @Override
    public void guardarHistorialReintentos(List<Turno> historialReintentos) throws Exception {
        escribirObjeto(PATH_REINTENTOS, historialReintentos);
    }

    // serializacion
    private void escribirObjeto(String path, Object objeto) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(objeto);
        }
    }

}
