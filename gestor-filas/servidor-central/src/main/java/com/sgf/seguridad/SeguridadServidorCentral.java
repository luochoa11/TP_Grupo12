package com.sgf.seguridad;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import com.sgf.ConfiguracionRed;

/**
 * Gestiona la seguridad del Servidor Central.
 * Se encarga de instanciar el encriptador y modificar el archivo config.properties
 * cuando el administrador cambia la clave.
 */
public class SeguridadServidorCentral {

    private IEncriptacionStrategy encriptador = null;
    private String algoritmoActivo = null;
    private String claveActiva = null;

    public SeguridadServidorCentral() {
        cargarClaveDesdeProperties();
    }

    /**
     * Todos leen del mismo lugar: la fuente de la verdad (config.properties)
     */
    public void cargarClaveDesdeProperties() {
        String clave = ConfiguracionRed.get("seguridad.clave");
        String algoritmo = ConfiguracionRed.get("seguridad.algoritmo");
        this.algoritmoActivo = algoritmo != null ? algoritmo : "AES";
        
        if (clave != null && !clave.isEmpty()) {
            this.claveActiva = clave;
            this.encriptador = ProveedorEstrategiaCifrado.crear(this.algoritmoActivo, this.claveActiva);
            System.out.println("[SeguridadServidor] Clave local cargada desde config.properties.");
        } else {
            System.err.println("[SeguridadServidor] No se encontró clave. El servidor arranca bloqueado.");
            this.encriptador = null;
        }
    }

    /**
     * Actualiza la política de seguridad en la RAM y reescribe el archivo properties.
     */
    public boolean actualizarSeguridad(String algoritmo, String claveSecreta) {
        try {
            this.algoritmoActivo = algoritmo;
            this.claveActiva = claveSecreta;
            this.encriptador = ProveedorEstrategiaCifrado.crear(this.algoritmoActivo, this.claveActiva);

            // Magia: Modificamos el archivo físico
            modificarArchivoProperties(claveSecreta, algoritmo);
            
            return true;
        } catch (Exception e) {
            System.err.println("[SeguridadServidor] Error al aplicar la estrategia de encriptación: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca el archivo config.properties, ubica la línea de la clave y la sobreescribe.
     */
    private void modificarArchivoProperties(String nuevaClave, String nuevoAlgoritmo) {
        String[] rutas = {
            "common/src/main/resources/config.properties",
            "common/target/classes/config.properties"
        };

        for (String ruta : rutas) {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                try {
                    List<String> lineas = Files.readAllLines(archivo.toPath());
                    boolean claveModificada = false;
                    boolean algoritmoModificado = false;
                    
                    for (int i = 0; i < lineas.size(); i++) {
                        if (lineas.get(i).trim().startsWith("seguridad.clave")) {
                            lineas.set(i, "seguridad.clave=" + nuevaClave);
                            claveModificada = true;
                        }
                        if (lineas.get(i).trim().startsWith("seguridad.algoritmo")) {
                            lineas.set(i, "seguridad.algoritmo=" + nuevoAlgoritmo);
                            algoritmoModificado = true;
                        }
                    }
                    
                    if (!claveModificada) {
                        lineas.add("seguridad.clave=" + nuevaClave);
                    }
                    
                    if (!algoritmoModificado) {
                        lineas.add("seguridad.algoritmo=" + nuevoAlgoritmo);
                    }

                    Files.write(archivo.toPath(), lineas);
                    System.out.println("[SeguridadServidor] Archivo de configuración actualizado con éxito en: " + ruta);
                       
                    
                    
                } catch (Exception e) {
                    System.err.println("[SeguridadServidor] Advertencia: No se pudo modificar el archivo en " + ruta);
                }
            }
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
