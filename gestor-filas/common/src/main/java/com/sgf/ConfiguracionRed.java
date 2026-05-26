package com.sgf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfiguracionRed {

    /**
     * Utilidad para leer configuraciones de red desde un archivo
     * de propiedades (config.properties) o desde variables de entorno.
     * <p>
     * Comportamiento:
     * - Primero intenta leer la clave desde una variable de entorno
     *   cuyo nombre corresponde a la clave con puntos reemplazados por
     *   guiones bajos y en mayúsculas (ej. directorio.ip -> DIRECTORIO_IP).
     * - Si no existe la variable de entorno, lee la clave desde
     *   el archivo config.properties ubicado en el classpath.
     * <p>
     * Si no se encuentra el archivo de propiedades durante la inicialización
     * se lanzará ExceptionInInitializerError. Si una clave solicitada no
     * existe se lanzará IllegalArgumentException.
     */

    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConfiguracionRed.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new ExceptionInInitializerError(
                    "No se encontró config.properties en el classpath"
                );
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
            clave.replace(".", "_").toUpperCase()
        );
        if (valorEnv != null) return valorEnv;

        String valor = props.getProperty(clave);
        if (valor == null) {
            throw new IllegalArgumentException(
                "Clave no encontrada: " + clave
            );
        }
        return valor;
    }

    public static int getInt(String clave) {
        return Integer.parseInt(get(clave));
    }
}