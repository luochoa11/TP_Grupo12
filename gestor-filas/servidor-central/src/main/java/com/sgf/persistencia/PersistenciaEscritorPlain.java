package com.sgf.persistencia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.sgf.modelos.Turno;

public class PersistenciaEscritorPlain implements IPersistenciaEscritor {
    private final String PATH_FILA;
    private final String PATH_HISTORIAL;
    private final String PATH_TURNOS_ACTUALES;
    private final String PATH_ULTIMO_LLAMADO;
    private final String PATH_REINTENTOS;
    private final String rutaBase;

    public PersistenciaEscritorPlain(String rutaBase) {
        this.rutaBase = (rutaBase == null || rutaBase.trim().isEmpty()) ? "" : (rutaBase + File.separator);
        this.PATH_FILA = rutaBase + "filaEspera.dat";
        this.PATH_HISTORIAL = rutaBase + "historial.dat";
        this.PATH_TURNOS_ACTUALES = rutaBase + "turnosActuales.dat";
        this.PATH_ULTIMO_LLAMADO = rutaBase + "ultimoLlamado.dat";
        this.PATH_REINTENTOS = rutaBase + "historialReintentos.dat";

        File dirHistorico = new File(this.rutaBase + "historico");
        if (!dirHistorico.exists()) {
            dirHistorico.mkdirs();
        }
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

    //---------método para guardado en frío----------------
    @Override
    public synchronized void registrarTurnoFinalizado(Turno turno) throws Exception {
        if (turno == null) return;
        
        // Rotación de Logs binarios por año y mes (ej: "auditoria_2026_06.dat")
        String mesAnio = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM"));
        String pathHistorico = this.rutaBase + "historico" + File.separator + "auditoria_" + mesAnio + ".dat";
        
        File archivo = new File(pathHistorico);
        boolean existe = archivo.exists() && archivo.length() > 0;
        
        try (FileOutputStream fos = new FileOutputStream(archivo, true)) {
            if (!existe) {
                // Si el archivo es nuevo, escribimos la cabecera estándar de serialización de Java
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(turno);
                }
            } else {
                // Si ya existe, usamos nuestra subclase para omitir escribir una segunda cabecera
                try (AppendingObjectOutputStream aoos = new AppendingObjectOutputStream(fos)) {
                    aoos.writeObject(turno);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al escribir log de auditoría binaria (Plain): " + e.getMessage());
        }
    }


    // serializacion
    private void escribirObjeto(String path, Object objeto) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(objeto);
        }
    }


    /**
     * Clase utilitaria encargada de omitir la escritura de la cabecera de serialización (Stream Header)
     * al anexar objetos en un FileOutputStream de Java abierto en modo append.
     */
    private static class AppendingObjectOutputStream extends ObjectOutputStream {
        public AppendingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }
        @Override
        protected void writeStreamHeader() throws IOException {
            // Se sobreescribe para evitar escribir cabeceras duplicadas y prevenir un StreamCorruptedException
            reset();
        }
    }
}
