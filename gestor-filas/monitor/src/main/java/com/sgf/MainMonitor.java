package com.sgf;

import com.sgf.failover.GestorFalla;
import com.sgf.salud.HeartbeatChecker;
import com.sgf.salud.MonitorSalud;

public class MainMonitor {
    public static void main(String[] args) {
        // IServicioControl control = new ProxyControlServidor();
        // IServicioDirectorio directorio = new ProxyDirectorio();

        // GestorFalla gestorFalla = new GestorFalla(control, directorio);
        
        /*
        HeartbeatChecker checker = new HeartbeatChecker(gestorFalla);
        Thread hiloChecker = new Thread(checker);
        hiloChecker.start();

        MonitorSalud monitorSalud = new MonitorSalud(Constantes.PUERTO_MONITOR1, checker);
        Thread hiloRed = new Thread(monitorSalud);
        hiloRed.start();
        */
    }
}