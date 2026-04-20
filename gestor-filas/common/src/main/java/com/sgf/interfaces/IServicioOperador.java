package com.sgf.interfaces;
import java.util.List;

import com.sgf.excepciones.FilaVaciaException;
import com.sgf.modelos.Turno;

public interface IServicioOperador {

    Turno llamarSiguiente(int idPuesto) throws FilaVaciaException ;
    Turno reintentarLlamado(int idPuesto);
    List<Turno> getCola();
    Turno getTurnoPuesto(int idPuesto);
}
