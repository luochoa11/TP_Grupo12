package com.sgf.servicios;

import java.util.ArrayList;
import java.util.List;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.infraestructura.ServidorCentral;
import com.sgf.interfaces.IServicioAdministrador;
import com.sgf.modelos.Turno;
import com.sgf.persistencia.GestorPersistencia;
import com.sgf.seguridad.IEncriptacionStrategy;
import com.sgf.seguridad.SeguridadServidorCentral;

/**
 * Fachada Concreta que orquesta de forma simplificada los subsistemas del servidor.
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
        System.out.println("[FACADE-SERVIDOR] Fachada del Servidor Central inicializada.");
    }

    @Override
    public boolean cambiarFormatoPersistencia(String tipoFormato) {
        boolean exito = cambiarFormatoPersistenciaSinReplicar(tipoFormato);
        if (exito) {
            // REPLICACIÓN EN CALIENTE AL SECUNDARIO (Evita bucles infinitos)
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
        System.out.println("[FACADE-SERVIDOR] Aplicando formato de persistencia local -> " + tipoFormato);
        try {
            gestorPersistencia.establecerFormato(tipoFormato);

            gestorPersistencia.guardarFilaEspera(logicaFila.getCola());
            gestorPersistencia.guardarHistorial(logicaFila.getHistorial());

            ArrayList<Turno> turnosActivosPlano = new ArrayList<>(logicaFila.getTurnosActivos().values());
            gestorPersistencia.guardarTurnosActuales(turnosActivosPlano);
            
            gestorPersistencia.guardarUltimoLlamado(logicaFila.getUltimoLlamado());

            System.out.println("[FACADE-SERVIDOR] Almacenamiento local reconfigurado con éxito.");
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

    public SeguridadServidorCentral getSeguridad() {
        return this.seguridad;
    }

    public void encriptarTurno(Turno t) {
        IEncriptacionStrategy enc = seguridad.getEncriptador();
        if (t != null && t.getDniCliente() != null && enc != null)
            t.setDniCliente(enc.encriptar(t.getDniCliente()));
    }

    public void desencriptarTurno(Turno t) {
        IEncriptacionStrategy enc = seguridad.getEncriptador();
        if (t != null && t.getDniCliente() != null && enc != null)
            t.setDniCliente(enc.desencriptar(t.getDniCliente()));
    }

    public Turno copiarYEncriptar(Turno t) {
        if (t == null) return null;
        Turno copia = t.clonar();
        encriptarTurno(copia);
        return copia;
    }

    public List<Turno> copiarYEncriptarLista(List<Turno> lista) {
        if (lista == null) return null;
        List<Turno> copia = new ArrayList<>();
        for (Turno t : lista) copia.add(copiarYEncriptar(t));
        return copia;
    }

    public void desencriptarLista(List<Turno> lista) {
        if (lista != null) for (Turno t : lista) desencriptarTurno(t);
    }

    @Override
    public boolean actualizarConfiguracionSeguridad(String algoritmo, String claveSecreta) {
        System.out.println("[FACADE-SERVIDOR] Configuración de seguridad solicitada -> Algoritmo: " + algoritmo + " | Clave: " + claveSecreta);
        
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
