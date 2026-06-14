package com.sgf.seguridad;

public class SeguridadRegistro {

    private IEncriptacionStrategy encriptador = null; // En memoria, arranca limpio
    private String algoritmoActual = null;

    public SeguridadRegistro() {
        // CONSTRUCTOR VACÍO: No lee archivos, no asume rutas. 
        // Es un componente puro que espera órdenes de la red.
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
        if (clave != null && !clave.isEmpty()) {
            ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
            this.encriptador = proveedor.crear(clave);
            this.algoritmoActual = algoritmo;
            System.out.println("[SeguridadCliente] Memoria RAM Sincronizada. Cifrado activo: " + algoritmo);
        } else {
            // Si el servidor responde vacío, el cliente sabe explícitamente que no hay cifrado
            this.encriptador = null;
            this.algoritmoActual = null;
            System.out.println("[SeguridadCliente] El servidor opera sin cifrado. Modo protegido desactivado.");
        }
    }
}