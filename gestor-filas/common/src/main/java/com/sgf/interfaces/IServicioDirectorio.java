package com.sgf.interfaces;

/**
 * Interfaz usada por los clientes para preguntar al Directorio la IP del servidor activo.
 */

public interface IServicioDirectorio {
    String getIPPrimario();
    int getPuertoPrimario();
    String getIPSecundario();
    int getPuertoSecundario();
    String getAlgoritmoSeguridad();
    String getClaveSeguridad();
}
