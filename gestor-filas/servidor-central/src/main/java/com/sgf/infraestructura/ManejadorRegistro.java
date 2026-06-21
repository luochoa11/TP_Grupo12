package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.disponibilidad.ActualizacionEstadoDTO;
import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.modelos.Turno;

/**
 * Atiende las conexiones de las Terminales de Registro.
 */
public class ManejadorRegistro extends ManejadorBase {

    public ManejadorRegistro(Socket socket, ObjectInputStream in, ObjectOutputStream out, 
                            ILogicaFila logica, ServidorCentral servidor) {
        super(socket, in, out, logica, servidor);
    }

    @Override
    public void run() {
        try {
            String comando = (String) in.readObject();
            
            if ("NUEVO_TURNO".equals(comando)) {
                Turno t = (Turno) in.readObject();
                
                servidor.desencriptarTurno(t);

                try {
                    synchronized(logica){
                        logica.agregarTurno(t);
                        servidor.persistirEstadoActivo();
                    }
                    
                    out.writeObject("OK");
                    
                    if (servidor.esPrimario() && servidor.getSincronizador() != null) {
                        ActualizacionEstadoDTO delta = new ActualizacionEstadoDTO("REGISTRAR", t, -1);
                        servidor.getSincronizador().sincronizarDelta(delta);
                    }

                } catch (DNIRepetidoException e) {
                    out.writeObject("ERROR_DNI_REPETIDO");
                }
                out.flush();
                
            } else if("GET_CONFIG_SEGURIDAD".equals(comando)) {
                String algoritmoActivo = servidor.getFachada().getAlgoritmoCifradoActivo();
                String claveActiva = servidor.getFachada().getClaveSecretaActiva();
                out.writeObject(algoritmoActivo);
                out.writeObject(claveActiva);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("[ManejadorRegistro] Error de red: " + e.getMessage());
        } finally {
            cerrarConexiones();
        }
    }
}
