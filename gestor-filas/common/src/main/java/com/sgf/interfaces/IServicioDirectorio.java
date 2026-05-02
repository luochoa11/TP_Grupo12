package com.sgf.interfaces;

/**
 * Interfaz usada por los clientes para preguntar al Directorio la IP del servidor activo.
 */

public interface IServicioDirectorio {
    //o ServerRegister? en este me mareé
    //para consulta de IPs, se implementa en Directorio

    public String getIPPrimario();
    public int getPuertoPrimario();
    public void actualizarPrimario(String ip, int puerto);
    public String getIPSecundario();
    public int getPuertoSecundario();
}
