package com.sgf.persistencia;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Fabricación Pura de soporte de infraestructura.
 * Centraliza el formateo y parseo de marcas de tiempo en cadenas de texto
 * para las clases de lectura y escritura estructurada (JSON, XML).
 */
public class FechaUtil {

    private static final DateTimeFormatter FORMATEADOR = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FechaUtil() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Convierte milisegundos epoch en una cadena legible "yyyy-MM-dd HH:mm:ss"
     */
    public static String formatearMilis(long milisegundos) {
        if (milisegundos <= 0) return "";
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(milisegundos), ZoneId.systemDefault())
                                .format(FORMATEADOR);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Convierte una cadena legible "yyyy-MM-dd HH:mm:ss" de vuelta a milisegundos epoch
     */
    public static long parsearCadenaAMilis(String fechaCadena) {
        if (fechaCadena == null || fechaCadena.trim().isEmpty() || fechaCadena.equals("N/A") || fechaCadena.equals("null")) {
            return 0;
        }
        try {
            return LocalDateTime.parse(fechaCadena, FORMATEADOR)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();
        } catch (Exception e) {
            return 0; // Fallback ante inconsistencias de parseo
        }
    }
}