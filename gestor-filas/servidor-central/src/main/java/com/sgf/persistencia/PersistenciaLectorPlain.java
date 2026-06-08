package com.sgf.persistencia;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.sgf.modelos.Turno;

@SuppressWarnings("unchecked")
public class PersistenciaLectorPlain implements IPersistenciaLector{
    private final String PATH_FILA;
    private final String PATH_HISTORIAL;
    private final String PATH_TURNOS_ACTUALES;
    private final String PATH_ULTIMO_LLAMADO;
    private final String PATH_REINTENTOS;

    public PersistenciaLectorPlain(String rutaBase) {
        this.PATH_FILA = rutaBase + "filaEspera.dat";
        this.PATH_HISTORIAL = rutaBase + "historial.dat";
        this.PATH_TURNOS_ACTUALES = rutaBase + "turnosActuales.dat";
        this.PATH_ULTIMO_LLAMADO = rutaBase + "ultimoLlamado.dat";
        this.PATH_REINTENTOS = rutaBase + "historialReintentos.dat";
    }

    @Override
    public List<Turno> recuperarFilaEspera() throws Exception {
        return (List<Turno>) leerObjeto(PATH_FILA);
    } 

    @Override
    public List<Turno> recuperarHistorial() throws Exception {
        return (List<Turno>) leerObjeto(PATH_HISTORIAL);
    }

    @Override
    public List<Turno> recuperarTurnosActuales() throws Exception {
        return (List<Turno>) leerObjeto(PATH_TURNOS_ACTUALES);
    }

    @Override
    public Turno recuperarUltimoLlamado() throws Exception {
        File file = new File(PATH_ULTIMO_LLAMADO);
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Turno) ois.readObject();
        } catch (EOFException e){
            return null;
        }
    }

    @Override
    public List<Turno> recuperarHistorialReintentos() throws Exception {
        return (List<Turno>) leerObjeto(PATH_REINTENTOS);
     }

    // deserializacion
    private Object leerObjeto(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) return new ArrayList<Turno>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return ois.readObject();
        } catch (EOFException e) {
            return new ArrayList<Turno>();
        }
    }
}
