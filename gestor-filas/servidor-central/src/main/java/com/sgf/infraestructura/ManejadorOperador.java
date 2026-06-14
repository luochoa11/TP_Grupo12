package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.disponibilidad.ActualizacionEstadoDTO;
import com.sgf.excepciones.FilaVaciaException;
import com.sgf.modelos.Turno;

/**
 * Atiende las peticiones de los Puestos de Atención de los Operadores.
 */
public class ManejadorOperador extends ManejadorBase {

    public ManejadorOperador(Socket socket, ObjectInputStream in, ObjectOutputStream out, 
                            ILogicaFila logica, ServidorCentral servidor) {
        super(socket, in, out, logica, servidor);
    }

    @Override
    public void run() {
        try {
            String comando = (String) in.readObject();
            
            switch (comando) {
                case "LLAMAR_SIGUIENTE":
                    int idPuesto = (int) in.readObject();
                    
                    try {
                        Turno turnoPrevio = logica.getTurnoPuesto(idPuesto);
                        Turno llamado;
                        synchronized(logica){ 
                            llamado = logica.llamarSiguiente(idPuesto);
                            servidor.persistirEstadoActivo();
                        }
                        
                        //PERSISTENCIA EN FRÍO: Si el operador ya tenía a alguien en atención,
                        // ese turno previo pasó a "ATENDIDO". Lo guardamos incrementalmente en el histórico frío.
                        if (turnoPrevio != null) {
                            servidor.registrarTurnoFinalizado(turnoPrevio);
                        }

                        out.writeObject(servidor.copiarYEncriptar(llamado));
                        
                        servidor.notificarMonitores(logica.getUltimoLlamado(), logica.getHistorial());
                        
                        //Envío del cambio a serv secundario
                        if (servidor.esPrimario() && servidor.getSincronizador() != null) {
                            ActualizacionEstadoDTO delta = new ActualizacionEstadoDTO("LLAMAR", llamado, idPuesto);
                            servidor.getSincronizador().sincronizarDelta(delta);
                        }
                    } catch (FilaVaciaException e) {
                        out.writeObject("ERROR_FILA_VACIA");
                    }
                    break;
                    
                case "REINTENTAR_LLAMADO":
                    int id = (int) in.readObject();
                    Turno turnoParaReintentar = logica.getTurnoPuesto(id);
                    Turno reIntento;
                    synchronized(logica){
                        reIntento = logica.reintentarLlamado(id);
                        servidor.persistirEstadoActivo();
                    }
                    
                    //PERSISTENCIA EN FRÍO: Si 'reIntento' es null pero el turno previo existía,
                    // significa que superó el límite de llamados y pasó a "AUSENTE". Lo enviamos al histórico frío.
                    if (reIntento == null && turnoParaReintentar != null) {
                        servidor.registrarTurnoFinalizado(turnoParaReintentar);
                    }

                    out.writeObject(servidor.copiarYEncriptar(reIntento)); // null si se eliminó, el op ya lo maneja
                    
                    servidor.notificarMonitores(logica.getUltimoLlamado(), logica.getHistorial());
                    
                    if (servidor.esPrimario() && servidor.getSincronizador() != null) {
                        ActualizacionEstadoDTO delta = new ActualizacionEstadoDTO("REINTENTAR", reIntento, id);
                        servidor.getSincronizador().sincronizarDelta(delta);
                    }
                    
                    break;
                    
                case "GET_COLA":
                    out.writeObject(servidor.copiarYEncriptarLista(logica.getCola()));
                    break;
                    
                case "GET_TURNO_PUESTO":
                    int idPuesto2 = (int) in.readObject();
                    out.writeObject(servidor.copiarYEncriptar(logica.getTurnoPuesto(idPuesto2)));
                    break;
                case "GET_CONFIG_SEGURIDAD":
                    String algotimoActivo = servidor.getFachada().getAlgoritmoCifradoActivo();
                    String claveActiva = servidor.getFachada().getClaveSecretaActiva();
                    out.writeObject(algotimoActivo);
                    out.writeObject(claveActiva);
            }
            out.flush();
        } catch (Exception e) {
            System.err.println("[ManejadorOperador] Error en la comunicación con el operador: " + e.getMessage());
        } finally {
            cerrarConexiones();
        }
    }
}
