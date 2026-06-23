package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.disponibilidad.SincronizadorEstado;
import com.sgf.interfaces.IServicioAdministrador;
import com.sgf.modelos.Turno;
import com.sgf.persistencia.GestorPersistencia;
import com.sgf.seguridad.IEncriptacionStrategy;
import com.sgf.servicios.ServidorCentralFacade;

/**
 * Clase que representa el Servidor Central del sistema. 
 */
public class ServidorCentral implements Runnable {
    private int puerto;
    private String ip;
    private ILogicaFila logica;
    private List<ObjectOutputStream> monitores = Collections.synchronizedList(new ArrayList<>());
    Map<Integer, ObjectOutputStream> operadores = new ConcurrentHashMap<>(); 
    private boolean esPrimario;
    private SincronizadorEstado sincronizador;
    
    // Atributos de persistencia y fachada de control administrativo
    private IServicioAdministrador fachadaServidor;
    private GestorPersistencia gestorPersistencia;

    // === guardo turnos terminados pendientes de sincronización para enviarlos al srv secundario cuando vuelva a estar disponible
    private List<Turno> historicoPendienteSync = Collections.synchronizedList(new ArrayList<>());


    public ServidorCentral(int puerto, String ip, ILogicaFila logica, boolean esPrimario,SincronizadorEstado sincronizador) {
        this.puerto = puerto;
        this.ip = ip;
        this.logica = logica;
        this.esPrimario = esPrimario;
        this.sincronizador = sincronizador;
        
        // VINCULACIÓN BIDIRECCIONAL: Permite que el sincronizador de red consulte la configuración en vivo
        if (this.sincronizador != null) {
            this.sincronizador.setServidor(this);
        }
        this.gestorPersistencia = new GestorPersistencia(puerto); 
        this.fachadaServidor = new ServidorCentralFacade(this, this.gestorPersistencia, this.logica);

        if (esPrimario) {
            cargarEstadoPrevioDelDisco();
        }
    }

    public ServidorCentralFacade getFachada() {
        return (ServidorCentralFacade) this.fachadaServidor;
    }

    // =========================================================================
    // Lógica de Red y Notificaciones
    // =========================================================================
    @Override
    public void run() {
        try(ServerSocket server = new ServerSocket(puerto)){
            System.out.println("\n=======================================================");
            System.out.println(" Servidor Central iniciado. Escuchando en el puerto " + puerto);
            System.out.println("=======================================================\n");
            
            while (true) {
                Socket socketCliente = server.accept();
                
                new Thread(() -> {
                    try {
                        ObjectOutputStream out = new ObjectOutputStream(socketCliente.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(socketCliente.getInputStream());
                        
                        String saludo = (String) in.readObject();
                        System.out.println("[Servidor] Conexión entrante con saludo: " + saludo);
                        
                        if (!esPrimario && !"SYNC_SERVER".equals(saludo) && !"MONITOR_FALLA".equals(saludo)) {
                            out.writeObject("ERROR_SERVIDOR_SECUNDARIO");
                            out.flush();
                            socketCliente.close();
                            return;
                        }

                        switch (saludo) {
                            case "CLIENTE_REGISTRO":
                                new Thread(new ManejadorRegistro(socketCliente, in, out, logica, this)).start();
                                break;
                            case "CLIENTE_OPERADOR":
                                new Thread(new ManejadorOperador(socketCliente, in, out, logica, this)).start();
                                break;
                            case "CLIENTE_ANUNCIO":
                                new Thread(new ManejadorAnuncio(socketCliente, in, out, logica, this)).start();
                                break;
                            case "MONITOR_FALLA":
                                new Thread(new ManejadorMonitor(socketCliente, in, out, logica, this)).start();
                                break;
                            case "SYNC_SERVER":
                                new Thread(new ManejadorSincronizacion(socketCliente, in, out, logica, this)).start();
                                break;
                            case "CLIENTE_ADMINISTRADOR":
                                new Thread(new ManejadorAdministrador(socketCliente, in, out, logica, this, fachadaServidor)).start();
                                break;
                            default:
                                System.err.println("[Servidor] Saludo no reconocido: " + saludo);
                                socketCliente.close();
                        }
                    } catch (Exception e) {
                        System.err.println("[ServidorCentral] Error al clasificar conexión: " + e.getMessage());
                    }
                }).start();
            }
        }catch(BindException e){
            System.err.println("Error: El puerto " + puerto + " ya está en uso.");
        }catch(Exception e){
            System.err.println("Error en el Servidor Central: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void agregarMonitor(ObjectOutputStream out){
        monitores.add(out);
    }

    public void notificarMonitores(Turno actual, List<Turno> historial) {
        synchronized (monitores) {
            Turno actualCopia = copiarYEncriptar(actual);
            List<Turno> historialCopia = copiarYEncriptarLista(historial);
            Iterator<ObjectOutputStream> it = monitores.iterator();
            while (it.hasNext()) {
                ObjectOutputStream out = it.next();
                try {
                    out.reset();
                    out.writeObject(actualCopia);
                    out.writeObject(historialCopia);
                    out.flush();
                } catch (Exception e) {
                    it.remove();
                }
            }
        }
    }
    
    public void notificarOperadores() { 
        for (Map.Entry<Integer, ObjectOutputStream> entry : operadores.entrySet()) {
            int id = entry.getKey();
            ObjectOutputStream out = entry.getValue();
            try {
                Turno actualCopia = copiarYEncriptar(logica.getTurnoPuesto(id));
                List<Turno> colaCopia = copiarYEncriptarLista(logica.getCola());
                out.writeObject(actualCopia);
                out.writeObject(colaCopia);
                out.flush();
            } catch (Exception e) {
                operadores.remove(id);
            }
        }
    }

    public boolean esPrimario() {
        return esPrimario;
    }

    public void promoverEstado() {
        this.esPrimario = true;
        System.out.println("[Servidor] " + this.ip + ": " + this.puerto + " Promovido a PRIMARIO.");
        historicoPendienteSync.clear(); // arranca limpio

        //Le pedimos la recarga de llaves simétricas a la fachada
        if (getFachada() != null && getFachada().getSeguridad() != null) {
            getFachada().getSeguridad().cargarClaveDesdeProperties();
        }
    }

    public void sincronizarEstado() {
        if (esPrimario && sincronizador != null) { 
            sincronizador.sincronizar();
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public SincronizadorEstado getSincronizador() {
        return sincronizador;   
    }

    public void degradarEstado() {
        this.esPrimario = false;
        System.out.println("[Servidor] "+this.ip+": "+this.puerto+" Degradado a SECUNDARIO.");
    }

    /**
     * Táctica de Disponibilidad.
     * Permite persistencia en ambos servidores (Primario y Secundario)
     * garantizando copias idénticas y actualizadas en ambos discos duros de forma concurrentes.
     */
    public synchronized void persistirEstadoActivo() {
        if (this.gestorPersistencia != null) {
            try {
                gestorPersistencia.guardarFilaEspera(logica.getCola());
                gestorPersistencia.guardarHistorial(logica.getHistorial());
                
                ArrayList<Turno> activosPlano = new ArrayList<>(logica.getTurnosActivos().values());
                gestorPersistencia.guardarTurnosActuales(activosPlano);
                
                gestorPersistencia.guardarUltimoLlamado(logica.getUltimoLlamado());
                gestorPersistencia.guardarHistorialReintentos(logica.getHistorialReintentos());
                System.out.println("[Servidor-Persistencia] Estado guardado localmente en formato: " + gestorPersistencia.getFormatoActivo());
            } catch (Exception e) {
                System.err.println("[Servidor-Persistencia] Error: No se pudo persistir el estado activo: " + e.getMessage());
            }
        }
    }

    // Para uso normal del primario (marca para réplica futura)
    public void registrarTurnoFinalizado(Turno t) {
        registrarTurnoFinalizado(t, true);
    }

    // Para uso al aplicar deltas recibidos (NO marca para réplica)
    public void registrarTurnoFinalizadoSinReplicar(Turno t) {
        registrarTurnoFinalizado(t, false);
    }
    
    /**
     * Táctica de Disponibilidad y Robustez (Registro Histórico en Frío).
     * Registra un turno cerrado en el disco de forma segura.
     */
    public synchronized void registrarTurnoFinalizado(Turno t, boolean marcarParaReplica) {
        if (this.gestorPersistencia != null && t != null) {
            try {
                this.gestorPersistencia.registrarTurnoFinalizado(t);
                if (marcarParaReplica) {
                    historicoPendienteSync.add(t.clonar());
                }
                System.out.println("[Servidor-Persistencia] Auditoría fría registrada con éxito para el DNI: " + t.getDniCliente());
            } catch (Exception e) {
                System.err.println("[Servidor-Persistencia] ADVERTENCIA: Falló el guardado en el log de auditoría fría: " + e.getMessage());
            }
        }
    }

    public List<Turno> getHistoricoPendienteSync() {
        return historicoPendienteSync;
    }

    public void limpiarHistoricoPendiente() {
        historicoPendienteSync.clear();
    }

    private void cargarEstadoPrevioDelDisco() {
        System.out.println("[Servidor-Recuperación de Estado] Resincronizando estado del servidor...");
        try {
            List<Turno> colaRecuperada = gestorPersistencia.recuperarFilaEspera();
            List<Turno> historialRecuperado = gestorPersistencia.recuperarHistorial();
            List<Turno> activosLista = gestorPersistencia.recuperarTurnosActuales();
            Turno ultimoRecuperado = gestorPersistencia.recuperarUltimoLlamado();
            List<Turno> historialReintentosRecuperado = gestorPersistencia.recuperarHistorialReintentos();

            Map<Integer, Turno> activosMap = new ConcurrentHashMap<>();
            if (activosLista != null) {
                for (Turno t : activosLista) {
                    activosMap.put(t.getIdPuesto(), t);
                }
            }

            logica.reemplazarEstado(colaRecuperada, activosMap, historialRecuperado, ultimoRecuperado, historialReintentosRecuperado);
            
            System.out.println("[Servidor-Recuperación de Estado] ¡ESTADO RESTAURADO CON ÉXITO!");
            System.out.println("  - Formato activo restaurado: " + gestorPersistencia.getFormatoActivo());
            System.out.println("  - Clientes en cola: " + colaRecuperada.size());
            System.out.println("  - Puestos restaurados: " + activosMap.size());

        } catch (Exception e) {
            System.out.println("[Servidor-Recuperación de Estado] No se detectó estado previo o está vacío. Iniciando con base limpia.");
        }
    }

    // =========================================================================
    // Métodos Operativos de Cifrado 
    // =========================================================================
    public IEncriptacionStrategy getEncriptador() {
        ServidorCentralFacade fac = getFachada();
        if (fac != null && fac.getSeguridad() != null) {
            return fac.getSeguridad().getEncriptador();
        }
        return null;
    }

    public void encriptarTurno(Turno t) {
        IEncriptacionStrategy enc = getEncriptador();
        if (t != null && t.getDniCliente() != null && enc != null) {
            t.setDniCliente(enc.encriptar(t.getDniCliente()));
        }
    }

    public void desencriptarTurno(Turno t) {
        IEncriptacionStrategy enc = getEncriptador();
        if (t != null && t.getDniCliente() != null && enc != null) {
            t.setDniCliente(enc.desencriptar(t.getDniCliente()));
        }
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
        for (Turno t : lista) {
            copia.add(copiarYEncriptar(t));
        }
        return copia;
    }

    public void desencriptarLista(List<Turno> lista) {
        if (lista != null) for (Turno t : lista) desencriptarTurno(t);
    }

}
