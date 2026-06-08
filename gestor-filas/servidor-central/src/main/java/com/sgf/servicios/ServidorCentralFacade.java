package com.sgf.servicios;

import java.util.ArrayList;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.infraestructura.ServidorCentral;
import com.sgf.interfaces.IServicioAdministrador;
import com.sgf.modelos.Turno;
import com.sgf.persistencia.GestorPersistencia;
import com.sgf.seguridad.SeguridadServidorCentral;

/**
 * Fachada Concreta que solo gestiona y configura las políticas de persistencia y seguridad.
 */
public class ServidorCentralFacade implements IServicioAdministrador {

    private final ServidorCentral servidorCentral;
    private final GestorPersistencia gestorPersistencia;
    private final ILogicaFila logicaFila;
    private final SeguridadServidorCentral seguridad;

    public ServidorCentralFacade(ServidorCentral servidorCentral, GestorPersistencia gestorPersistencia, ILogicaFila logicaFila) {
        this.servidorCentral = servidorCentral;
        this.gestorPersistencia = gestorPersistencia;
        this.logicaFila = logicaFila;
        this.seguridad = new SeguridadServidorCentral();
        System.out.println("[FACADE-SERVIDOR] Fachada administrativa inicializada.");
    }


    // =========================================================================
    // ADMINISTRACIÓN DE PERSISTENCIA
    // =========================================================================
    @Override
    public boolean cambiarFormatoPersistencia(String tipoFormato) {
        boolean exito = cambiarFormatoPersistenciaSinReplicar(tipoFormato);
        if (exito) {
            // Replicación en caliente al Servidor Secundario si somos primario activo
            if (servidorCentral.esPrimario() && servidorCentral.getSincronizador() != null) {
                servidorCentral.getSincronizador().sincronizarFormatoPersistencia(tipoFormato);
            }
        }
        return exito;
    }

    /**
     * Ejecuta la reconfiguración local de almacenamiento sin disparar otra sincronización de red.
     */
    public boolean cambiarFormatoPersistenciaSinReplicar(String tipoFormato) {
        System.out.println("[FACADE-SERVIDOR] Cambio de formato local solicitado -> Formato: " + tipoFormato);
        try {
            gestorPersistencia.establecerFormato(tipoFormato);

            gestorPersistencia.guardarFilaEspera(logicaFila.getCola());
            gestorPersistencia.guardarHistorial(logicaFila.getHistorial());

            ArrayList<Turno> turnosActivosPlano = new ArrayList<>(logicaFila.getTurnosActivos().values());
            gestorPersistencia.guardarTurnosActuales(turnosActivosPlano);
            
            gestorPersistencia.guardarUltimoLlamado(logicaFila.getUltimoLlamado());

            System.out.println("[FACADE-SERVIDOR] Migración y guardado local completado.");
            return true;
        } catch (Exception e) {
            System.err.println("[FACADE-SERVIDOR] Error durante la reconfiguración local: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getFormatoPersistenciaActivo() {
        return gestorPersistencia.getFormatoActivo();
    }

    // =========================================================================
    // ADMINISTRACIÓN DE SEGURIDAD
    // =========================================================================
    public SeguridadServidorCentral getSeguridad() {
        return this.seguridad;
    }

    @Override
    public boolean actualizarConfiguracionSeguridad(String algoritmo, String claveSecreta) {
        System.out.println("[FACADE-SERVIDOR] Configuración de seguridad local solicitada -> Algoritmo: " + algoritmo);
        boolean exito = seguridad.actualizarSeguridad(algoritmo, claveSecreta);
        if (exito) {
            System.out.println("[FACADE-SERVIDOR] Nueva política de encriptación aplicada.");
        }
        return exito;
    }

    @Override
    public String getAlgoritmoCifradoActivo() {
        return seguridad.getAlgoritmoActivo();
    }

    @Override
    public String getClaveSecretaActiva() {
        return seguridad.getClaveActiva();
    }

    // =========================================================================
    // CONSULTAS CONSOLIDADAS (Optimización de red)
    // =========================================================================
    @Override
    public String[] obtenerConfiguracionCompleta() {
        return new String[] {
            gestorPersistencia.getFormatoActivo(),
            seguridad.getAlgoritmoActivo(), 
            seguridad.getClaveActiva()      
        };
    }

    @Override
    public String[] getAlgoritmosDisponibles() {
        return new String[] { "AES", "DES", "XOR" };
    }

}
