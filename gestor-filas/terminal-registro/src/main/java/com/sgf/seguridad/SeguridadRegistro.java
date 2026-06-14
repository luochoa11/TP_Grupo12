package com.sgf.seguridad;

import com.sgf.ConfiguracionRed;

public class SeguridadRegistro {

    private IEncriptacionStrategy encriptador = null; // En memoria, arranca limpio
    private String algoritmoActual = null;
    private final int puerto;

    public SeguridadRegistro(int puerto) {
       this.puerto = puerto;
       String algoritmo = ConfiguracionRed.getPropLocal("registro", puerto, "seguridad.algoritmo");
       String clave     = ConfiguracionRed.getPropLocal("registro", puerto, "seguridad.clave");

       if (algoritmo != null && clave != null) {
          inicializarEstrategia(algoritmo, clave);
           System.out.println("[SeguridadRegistro] Configuración local cargada. Cifrado activo: " + algoritmo);
       } else {
           System.out.println("[SeguridadRegistro] No se encontró configuración local de seguridad. Modo protegido desactivado.");
       }
    }

    public String encriptarDNI(String dniOriginal) {
        // Si todavía no se sincronizó o el servidor opera sin seguridad, 
        // el DNI pasa en texto plano de forma transparente.
        if (this.encriptador != null && dniOriginal != null) {
            return this.encriptador.encriptar(dniOriginal);
        }
        return dniOriginal;
    }

    /**
     * Este método es el que invoca el ProxyRegistro al hacer la primera conexión.
     */
    public void actualizarConfiguracion(String algoritmo, String clave) {
        ConfiguracionRed.guardarConfigLocal("registro", this.puerto,algoritmo,clave);
        inicializarEstrategia(algoritmo, clave);
       
    }

    private void inicializarEstrategia(String algoritmo, String clave) {
      ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
      this.encriptador = proveedor.crear(clave);
      this.algoritmoActual = algoritmo;
      System.out.println("[SeguridadRegistro] Estrategia de cifrado inicializada: " + algoritmo);
    }
}