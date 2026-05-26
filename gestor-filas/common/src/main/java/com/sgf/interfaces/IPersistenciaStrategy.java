package com.sgf.interfaces;

import java.util.List;

import com.sgf.modelos.Turno;

public interface IPersistenciaStrategy {
 void guardarFilaEspera(List<Turno> filaEspera) throws Exception;
 List<Turno> recuperarFilaEspera() throws Exception;

 void guardarHistorial(List<Turno> historial) throws Exception;
List<Turno> recuperarHistorial() throws Exception;

//  void guardarNotificaciones(List<Turno> notificaciones) throws Exception; lo guardamos en cada turno, no tenemos lista a parte
//  List<Turno> recuperarNotificaciones() throws Exception;

 //estos no dicen la consigna pero si manejamos nosotros
 void guardarTurnosActuales(List<Turno> turnosActuales) throws Exception;
 List<Turno> recuperarTurnosActuales() throws Exception;

 void guardarUltimoLlamado(Turno ultimoLlamado) throws Exception;
 Turno recuperarUltimoLlamado() throws Exception;
 
}
