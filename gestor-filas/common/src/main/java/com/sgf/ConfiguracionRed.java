package com.sgf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
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

    private static final Properties props = new Properties();
    private static long ultimaModificacion = -1;

    static {
        try (InputStream input = ConfiguracionRed.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new ExceptionInInitializerError(
                        "No se encontró config.properties en el classpath");
            }
            props.load(input);

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

        String valor = props.getProperty(clave);
        if (valor == null) {
            throw new IllegalArgumentException(
                    "Clave no encontrada: " + clave);
        }
        return valor;
    }

    public static int getInt(String clave) {
        return Integer.parseInt(get(clave));
    }

    public static synchronized void guardarConfigLocal(String algoritmo, String clave, String[] rutas) {
        props.setProperty("seguridad.algoritmo", algoritmo);
        props.setProperty("seguridad.clave", clave);
  
        for(String ruta:rutas){
            File archivo = new File(ruta);

            if(archivo.exists()){
                try{
                    List<String> lineas = Files.readAllLines(archivo.toPath());
                    boolean claveModificada = false;
                    boolean algoritmoModificado = false;

                    for (int i = 0; i < lineas.size(); i++) {
                        if (lineas.get(i).trim().startsWith("seguridad.algoritmo=")) {
                            lineas.set(i, "seguridad.algoritmo=" + algoritmo);
                            algoritmoModificado = true;
                        } 
                        
                        if (lineas.get(i).trim().startsWith("seguridad.clave=")) {
                            lineas.set(i, "seguridad.clave=" + clave);
                            claveModificada = true;
                        }
                    }

                    if (!algoritmoModificado) {
                        lineas.add("seguridad.algoritmo=" + algoritmo);
                    }
                    if (!claveModificada) {
                        lineas.add("seguridad.clave=" + clave);
                    }

                    Files.write(archivo.toPath(), lineas);
                    System.out.println("[ConfiguracionRed] Archivo de configuración local actualizado: " + ruta);


                }catch(Exception e){
                    System.err.println("[ConfiguracionRed] Error al guardar configuración local en " + ruta);
                }
            }
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