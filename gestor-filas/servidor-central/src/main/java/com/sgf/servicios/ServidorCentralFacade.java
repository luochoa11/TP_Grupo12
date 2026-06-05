package com.sgf.servicios;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.infraestructura.ServidorCentral;
import com.sgf.interfaces.IServicioAdministrador;
import com.sgf.modelos.Turno;
import com.sgf.persistencia.GestorPersistencia;
import com.sgf.seguridad.EstrategiaCifradoAES;
import com.sgf.seguridad.IEncriptacionStrategy;

/**
 * Fachada Concreta que orquesta de forma simplificada los subsistemas del servidor.
 */
public class ServidorCentralFacade implements IServicioAdministrador {

    private final ServidorCentral servidorCentral;
    private final GestorPersistencia gestorPersistencia;
    private final ILogicaFila logicaFila;

    // El sistema está bloqueado hasta que el admin aplique la config.
    private String algoritmoCifrado = null;
    private String claveSecreta = null;

    public ServidorCentralFacade(ServidorCentral servidorCentral, GestorPersistencia gestorPersistencia, ILogicaFila logicaFila) {
        this.servidorCentral = servidorCentral;
        this.gestorPersistencia = gestorPersistencia;
        this.logicaFila = logicaFila;
        
        // Al arrancar o reiniciar, intentamos recuperar la seguridad guardada.
        cargarSeguridadDesdeDisco();
        
        System.out.println("[FACADE-SERVIDOR] Fachada del Servidor Central inicializada.");
    }

    public void cargarSeguridadDesdeDisco() {
        try (BufferedReader reader = new BufferedReader(new FileReader("seguridad.dat"))) {
            this.algoritmoCifrado = reader.readLine();
            this.claveSecreta = reader.readLine();
            
            if (this.claveSecreta != null && !this.claveSecreta.isEmpty()) {
                this.servidorCentral.setEncriptador(new EstrategiaCifradoAES(this.claveSecreta));
                System.out.println("[FACADE-SERVIDOR] Clave de seguridad recuperada exitosamente desde disco.");
            }
        } catch (Exception e) {
            System.out.println("[FACADE-SERVIDOR] No se encontró clave previa. El sistema arranca bloqueado.");
        }
    }

    private void persistirSeguridadEnDisco(String algoritmo, String clave) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("seguridad.dat"))) {
            writer.println(algoritmo);
            writer.println(clave);
        } catch (Exception e) {
            System.err.println("[FACADE-SERVIDOR] Error persistiendo la seguridad en disco.");
        }
    }

    @Override
    public boolean cambiarFormatoPersistencia(String tipoFormato) {
        System.out.println("[FACADE-SERVIDOR] Cambio de formato de persistencia solicitado -> Formato: " + tipoFormato);
        try {
            gestorPersistencia.establecerFormato(tipoFormato);

            gestorPersistencia.guardarFilaEspera(logicaFila.getCola());
            gestorPersistencia.guardarHistorial(logicaFila.getHistorial());

            ArrayList<Turno> turnosActivosPlano = new ArrayList<>(logicaFila.getTurnosActivos().values());
            gestorPersistencia.guardarTurnosActuales(turnosActivosPlano);
            
            gestorPersistencia.guardarUltimoLlamado(logicaFila.getUltimoLlamado());

            System.out.println("[FACADE-SERVIDOR] Migración y guardado completados con éxito para: " + tipoFormato);
            return true;

        } catch (Exception e) {
            System.err.println("[FACADE-SERVIDOR] Error crítico durante la migración en caliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getFormatoPersistenciaActivo() {
        return gestorPersistencia.getFormatoActivo();
    }

    @Override
    public boolean actualizarConfiguracionSeguridad(String algoritmo, String claveSecreta) {
        System.out.println("[FACADE-SERVIDOR] Configuración de seguridad solicitada -> Algoritmo: " + algoritmo + " | Clave: " + claveSecreta);
        try {
            this.algoritmoCifrado = algoritmo;
            this.claveSecreta = claveSecreta;

            IEncriptacionStrategy nuevaEstrategia;
            
            if ("AES-128".equalsIgnoreCase(algoritmo) || algoritmo.contains("AES")) {
                nuevaEstrategia = new EstrategiaCifradoAES(claveSecreta);
            } else {
                nuevaEstrategia = new EstrategiaCifradoAES(claveSecreta);
            }

            this.servidorCentral.setEncriptador(nuevaEstrategia);
            
            // Guardamos la configuración en disco
            persistirSeguridadEnDisco(algoritmo, claveSecreta);
            
            System.out.println("[FACADE-SERVIDOR] Nueva política de encriptación aplicada en memoria activa y guardada en disco.");
            return true;
        } catch (Exception e) {
            System.err.println("[FACADE-SERVIDOR] Error al cambiar la estrategia de encriptación: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getAlgoritmoCifradoActivo() {
        return this.algoritmoCifrado != null ? this.algoritmoCifrado : "SIN_CONFIGURAR";
    }

    @Override
    public String getClaveSecretaActiva() {
        return this.claveSecreta != null ? this.claveSecreta : "SISTEMA_BLOQUEADO";
    }

    @Override
    public String[] obtenerConfiguracionCompleta() {
        return new String[] {
            gestorPersistencia.getFormatoActivo(),
            this.algoritmoCifrado != null ? this.algoritmoCifrado : "SIN_CONFIGURAR",
            this.claveSecreta != null ? this.claveSecreta : "SISTEMA_BLOQUEADO"
        };
    }
}
