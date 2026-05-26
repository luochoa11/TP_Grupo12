package com.sgf.persistencia;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.sgf.interfaces.IPersistenciaStrategy;
import com.sgf.modelos.Turno;

public class PersistenciaTextoPlano implements IPersistenciaStrategy {
    private final String PATH_FILA = "filaEspera.dat";
    private final String PATH_HISTORIAL = "historial.dat";
    private final String PATH_TURNOS_ACTUALES = "turnosActuales.dat";
    private final String PATH_ULTIMO_LLAMADO = "ultimoLlamado.dat";

    @Override
    public void guardarFilaEspera(List<Turno> filaEspera) throws Exception {
        escribirObjeto(PATH_FILA, filaEspera);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Turno> recuperarFilaEspera() throws Exception {
        return (List<Turno>) leerObjeto(PATH_FILA);
    }

    @Override
    public void guardarHistorial(List<Turno> historial) throws Exception {
        escribirObjeto(PATH_HISTORIAL, historial);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Turno> recuperarHistorial() throws Exception {
        return (List<Turno>) leerObjeto(PATH_HISTORIAL);
       
    }

    @Override
    public void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception {
        escribirObjeto(PATH_TURNOS_ACTUALES, turnosActuales);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Turno> recuperarTurnosActuales() throws Exception {
        return (List<Turno>) leerObjeto(PATH_TURNOS_ACTUALES);
    }

    @Override
    public void guardarUltimoLlamado(Turno ultimoLlamado) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PATH_ULTIMO_LLAMADO))) {
            oos.writeObject(ultimoLlamado);
        }
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
    // serializacion
    private void escribirObjeto(String path, Object objeto) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(objeto);
        }
    }

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
