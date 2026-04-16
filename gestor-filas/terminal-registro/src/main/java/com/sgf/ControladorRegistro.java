package com.sgf;

import com.sgf.excepciones.DNIRepetidoException;
import com.sgf.excepciones.DniInvalidoException;

public class ControladorRegistro {
    private VentanaTerminalRegistro vista;
    private ClienteRegistro cliente;

    public ControladorRegistro(VentanaTerminalRegistro vista, ClienteRegistro cliente) {
        this.vista = vista;
        this.cliente = cliente;
    }

    public void escribirNumero(String numero) {  // funcion que escribe el numero en el text field
        String dniActual = vista.getDNI();

        // limitamos a 8 dígitos para evitar ingresos excesivos
        if (dniActual.length() < 8) {
            vista.setDNI(dniActual + numero);
        }
    }

    public void borrarUltimo() {  // funcion boton borrar
        String texto = vista.getDNI().trim();

        if (texto.length() > 0) {
            vista.setDNI(texto.substring(0, texto.length() - 1));
        }
    }

    private void validarDNI(String dni) throws DniInvalidoException,DNIRepetidoException {
        if (dni.length() < 7 || dni.length() > 8) {
            throw new DniInvalidoException(dni);
        }

        if (dni.startsWith("0")){
            //ej el dni 0123456 lo daba como valido
            throw new DniInvalidoException(dni);
        }
    }


    public void ingresarDNI() {
        String dni = vista.getDNI();
            try {
            validarDNI(dni);
            
            Turno t = new Turno(dni); //si validó, creo el turno y lo envío al servidor
            String rta = cliente.registrarTurno(t);
            if (rta.equals("OK")){
                vista.mostrarMensaje("¡Turno Registrado!\nDocumento: " + dni);
                vista.setDNI("");
            }
            else{
                vista.mostrarMensaje("El DNI ingresado ya tiene un lugar en la fila");
            }
        }
        catch (DniInvalidoException e){
            vista.mostrarMensaje(e.getMessage());
        }
        catch(DNIRepetidoException e){
                vista.mostrarMensaje(e.getMessage());
        }
        catch (Exception e) {
            vista.mostrarMensaje("Error de conexión: No se pudo enviar el turno. ");
        }
        
    }

    public void limpiar() {
        vista.setDNI("");
    }
}
