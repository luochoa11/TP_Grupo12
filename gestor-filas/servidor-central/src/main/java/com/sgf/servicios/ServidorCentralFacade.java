package com.sgf.servicios;

import java.util.ArrayList;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.infraestructura.ServidorCentral;
import com.sgf.interfaces.IServicioAdministrador;
import com.sgf.modelos.Turno;
import com.sgf.persistencia.GestorPersistencia;

/**
 * Fachada Concreta que orquesta de forma simplificada los subsistemas del servidor.
 */
public class ServidorCentralFacade implements IServicioAdministrador {

    private final ServidorCentral servidorCentral;
    private final GestorPersistencia gestorPersistencia;
    private final ILogicaFila logicaFila;

    public ServidorCentralFacade(ServidorCentral servidorCentral, GestorPersistencia gestorPersistencia, ILogicaFila logicaFila) {
        this.servidorCentral = servidorCentral;
        this.gestorPersistencia = gestorPersistencia;
        this.logicaFila = logicaFila;
        
        System.out.println("[FACADE-SERVIDOR] Fachada del Servidor Central inicializada.");
    }

    // Ya no es necesario cargar/persistir la seguridad desde aquí, la gestiona el componente dedicado

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
        
        boolean exito = this.servidorCentral.actualizarSeguridad(algoritmo, claveSecreta);
        
        if (exito) {
            System.out.println("[FACADE-SERVIDOR] Nueva política de encriptación aplicada.");
        }
        return exito;
    }

    @Override
    public String getAlgoritmoCifradoActivo() {
        return this.servidorCentral.getAlgoritmoSeguridad();
    }

    @Override
    public String getClaveSecretaActiva() {
        return this.servidorCentral.getClaveSeguridad();
    }

    @Override
    public String[] obtenerConfiguracionCompleta() {
        return new String[] {
            gestorPersistencia.getFormatoActivo(),
            this.servidorCentral.getAlgoritmoSeguridad(),
            this.servidorCentral.getClaveSeguridad()
        };
    }
}
