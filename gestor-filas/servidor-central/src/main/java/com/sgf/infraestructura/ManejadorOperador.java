package com.sgf.infraestructura;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;
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
                        Turno llamado = logica.llamarSiguiente(idPuesto);
                        out.writeObject(llamado);
                        servidor.notificarMonitores(logica.getUltimoLlamado(), logica.getHistorial());
                        servidor.sincronizarEstado();
                    } catch (FilaVaciaException e) {
                        out.writeObject("ERROR_FILA_VACIA");
                    }
                    break;
                    
                case "REINTENTAR_LLAMADO":
                    int id = (int) in.readObject();
                    Turno reIntento = logica.reintentarLlamado(id);
                    out.writeObject(reIntento); // null si se eliminó , el op ya lo maneja
                    
                    servidor.notificarMonitores(logica.getUltimoLlamado(), logica.getHistorial());
                    servidor.sincronizarEstado();
                    break;
                    
                case "GET_COLA":
                    out.writeObject(logica.getCola());
                    break;
                    
                case "GET_TURNO_PUESTO":
                    int idPuesto2 = (int) in.readObject();
                    out.writeObject(logica.getTurnoPuesto(idPuesto2));
                    break;
            }
            out.flush();
        } catch (Exception e) {
            System.err.println("[ManejadorOperador] Error en la comunicación con el operador: " + e.getMessage());
        } finally {
            cerrarConexiones();
        }
    }
}
