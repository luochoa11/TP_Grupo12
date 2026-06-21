package com.sgf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class ConfiguracionRed {

    /**
     * Utilidad para leer configuraciones de red desde un archivo
     * de propiedades (config.properties) o desde variables de entorno.
     * <p>
     * Comportamiento:
     * - Primero intenta leer la clave desde una variable de entorno
     * cuyo nombre corresponde a la clave con puntos reemplazados por
     * guiones bajos y en mayúsculas (ej. directorio.ip -> DIRECTORIO_IP).
     * - Si no existe la variable de entorno, lee la clave desde
     * el archivo config.properties ubicado en el classpath.
     * <p>
     * Si no se encuentra el archivo de propiedades durante la inicialización
     * se lanzará ExceptionInInitializerError. Si una clave solicitada no
     * existe se lanzará IllegalArgumentException.
     */

    private static final Properties propsGlobales = new Properties();

    static {
        try (InputStream input = ConfiguracionRed.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new ExceptionInInitializerError(
                        "No se encontró config.properties en el classpath");
            }
            propsGlobales.load(input);

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String get(String clave) {
        // Primero variables de entorno (útil para Docker)
        // directorio.ip → DIRECTORIO_IP
        String valorEnv = System.getenv(
                clave.replace(".", "_").toUpperCase());
        if (valorEnv != null)
            return valorEnv;

        String valor = propsGlobales.getProperty(clave);
        if (valor == null) {
            throw new IllegalArgumentException(
                    "Clave no encontrada: " + clave);
        }
        return valor;
    }

    public static int getInt(String clave) {
        return Integer.parseInt(get(clave));
    }

    // ======================== LOGICA DE CONFIGURACION LOCAL=========================

    private static final String CARPETA_BASE = "config" + File.separator;

    public static synchronized void guardarConfigLocal(String nodo, int id, String algoritmo, String clave) {
        String rutaCarpeta = CARPETA_BASE + nodo + "_" + id + File.separator;
        File carpeta = new File(rutaCarpeta);

            if (!carpeta.exists()) {
                carpeta.mkdirs();
        }

        File archivoConfig = new File(carpeta, "config.properties");
        Properties propsNodo = new Properties();

        if(archivoConfig.exists()) {
            try (InputStream input = Files.newInputStream(archivoConfig.toPath())) {
                propsNodo.load(input);
            } catch (IOException e) {
                System.err.println("[ConfiguracionRed] Error al cargar configuración local: " + e.getMessage());
            } 
        }

        propsNodo.setProperty("seguridad.algoritmo", algoritmo != null ? algoritmo : "AES");
        propsNodo.setProperty("seguridad.clave", clave!= null ? clave:"");

        try(FileOutputStream out = new FileOutputStream(archivoConfig)) {
            propsNodo.store(out, "Configuración de seguridad para " + nodo + ":" + id);
            System.out.println("[ConfiguracionRed] Configuración local guardada en " + archivoConfig.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[ConfiguracionRed] Error al guardar configuración local: " + e.getMessage());
        }
    }

    public static String getPropLocal(String nodo, int id, String clave) {
        String rutaCarpeta = CARPETA_BASE + nodo + "_" + id + File.separator;
        File archivoConfig = new File(rutaCarpeta, "config.properties");

        if (!archivoConfig.exists()) {
            return null;// agregar valor por defecto?
        }

        Properties propsNodo = new Properties();
    
        try(FileInputStream in = new FileInputStream(archivoConfig)) {
            propsNodo.load(in);
            return propsNodo.getProperty(clave);
        } catch (IOException e) {
            System.err.println("[ConfiguracionRed] Error al leer configuración local: " + e.getMessage());
            return null;

        }
    }

    // public static synchronized void recargar() {
    // props.clear();

    // try (InputStream input = ConfiguracionRed.class
    // .getClassLoader()
    // .getResourceAsStream("config.properties")) {

    // if (input == null) {
    // throw new RuntimeException(
    // "No se encontró config.properties en el classpath");
    // }

    // props.load(input);

    // System.out.println("[ConfiguracionRed] Configuración recargada.");

    // } catch (IOException e) {
    // throw new RuntimeException(
    // "Error al recargar config.properties",
    // e);
    // }
    // }

    // public static synchronized boolean recargarSiCambio() {

    //     File archivo = new File(
    //             "../common/target/classes/config.properties");

    //     if (!archivo.exists()) {
    //         return false;
    //     }

    //     long modificacionActual = archivo.lastModified();

    //     if (modificacionActual == ultimaModificacion) {
    //         return false;
    //     }

    //     ultimaModificacion = modificacionActual;

    //     try (InputStream input = ConfiguracionRed.class
    //             .getClassLoader()
    //             .getResourceAsStream("config.properties")) {

    //         props.clear();
    //         props.load(input);

    //         System.out.println(
    //                 "[ConfiguracionRed] Configuración recargada.");

    //         return true;

    //     } catch (IOException e) {
    //         System.err.println(
    //                 "[ConfiguracionRed] Error al recargar configuración.");
    //         return false;
    //     }
    // }
}