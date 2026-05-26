package com.sgf.interfaces;

/**
 * Interfaz que define las operaciones administrativas del sistema de filas.
 */
public interface IServicioAdministrador {

   // Operaciones de Alta Persistencia
    boolean cambiarFormatoPersistencia(String tipoFormato);
    String getFormatoPersistenciaActivo();

    // Operaciones de Seguridad
    boolean actualizarConfiguracionSeguridad(String algoritmo, String claveSecreta);
    String getAlgoritmoCifradoActivo();
    String getClaveSecretaActiva();
    String[] obtenerConfiguracionCompleta();

}