package com.sgf.seguridad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import com.sgf.ConfiguracionRed;

/**
 * Gestiona la seguridad del Servidor Central.
 * Se encarga de instanciar el encriptador y modificar el archivo config.properties
 * cuando el administrador cambia la clave.
 */
public class SeguridadServidorCentral {

    private IEncriptacionStrategy encriptador = null;
    private String algoritmoActivo = "AES";// Valor por defecto, se actualizará al cargar la clave desde properties
    private String claveActiva = "";// Valor por defecto, se actualizará al cargar la clave desde properties

    private final String rutaBase;
    private final String rutaArchivoConfig;

    public SeguridadServidorCentral(int puerto) {
        this.rutaBase = "servidor_" + puerto+File.separator;
        this.rutaArchivoConfig = rutaBase + "config.properties";

        File carpetaNodo = new File(this.rutaBase);

        if(!carpetaNodo.exists()){
            carpetaNodo.mkdirs();
        }
        cargarClaveDesdeProperties();
    }

    /**
     * Intenta leer el archivo de seguridad específico de este nodo.
     * Si no existe, arranca con los valores por defecto (AES y vacío).
     */
    public void cargarClaveDesdeProperties() {
        Properties props = new Properties();
        File archivo = new File(this.rutaArchivoConfig);

        if (archivo.exists()) {
            try (FileInputStream in = new FileInputStream(archivo)) {
                props.load(in);
                this.algoritmoActivo = props.getProperty("seguridad.algoritmo", "AES");
                this.claveActiva = props.getProperty("seguridad.clave", "");
                System.out.println("[SeguridadServidor] Configuración recuperada del nodo en: " + this.rutaArchivoConfig);
            } catch (IOException e) {
                System.err.println("[SeguridadServidor] Error al leer configuración local, usando valores base.");
            }
        }

        inicializarEstrategia(this.algoritmoActivo, this.claveActiva);
    }
   /**
     * Cambia la estrategia en caliente y la persiste en el properties del nodo.
     */
    public boolean actualizarSeguridad(String algoritmo, String claveSecreta) {
        try {
            this.algoritmoActivo = algoritmo;
            this.claveActiva = claveSecreta;
            
            inicializarEstrategia(algoritmo, claveSecreta);
            guardarEnArchivoLocal(algoritmo, claveSecreta);
            return true;
        } catch (Exception e) {
            System.err.println("[SeguridadServidor] Error al actualizar la seguridad: " + e.getMessage());
            return false;
        }
    }

   private void inicializarEstrategia(String algoritmo, String clave) {
        if (clave != null && !clave.isEmpty()) {
            ProveedorEstrategiaCifrado proveedor = SelectorProveedores.obtenerProveedor(algoritmo);
            this.encriptador = proveedor.crear(clave);
        } else {
            this.encriptador = null;
        }
    }

    private void guardarEnArchivoLocal(String algoritmo, String clave) {
        Properties props = new Properties();
        props.setProperty("seguridad.algoritmo", algoritmo);
        props.setProperty("seguridad.clave", clave);

        try (FileOutputStream out = new FileOutputStream(this.rutaArchivoConfig)) {
            props.store(out, "Configuración de seguridad del Servidor Central - SGF");
            System.out.println("[SeguridadServidor] Configuración guardada con éxito en: " + this.rutaArchivoConfig);
        } catch (IOException e) {
            System.err.println("[SeguridadServidor] No se pudo persistir la seguridad del nodo: " + e.getMessage());
        }
    }

    public IEncriptacionStrategy getEncriptador() {
        return this.encriptador;
    }

    public String getAlgoritmoActivo() {
        return this.algoritmoActivo != null ? this.algoritmoActivo : "SIN_CONFIGURAR";
    }

    public String getClaveActiva() {
        return this.claveActiva != null ? this.claveActiva : "SISTEMA_BLOQUEADO";
    }
}
