package com.sgf;

import com.sgf.failover.GestorFalla;
import com.sgf.salud.HeartbeatChecker;
import com.sgf.salud.MonitorSalud;

public class MainMonitor {
    public static void main(String args[]){
        

        int    puertoMonitor    = ConfiguracionRed.getInt("monitor.puerto");
        String directorioIp     = ConfiguracionRed.get("directorio.ip");
        int    directorioPuerto = ConfiguracionRed.getInt("directorio.puerto");

        GestorFalla gestorFalla = new GestorFalla(directorioIp, directorioPuerto);
        HeartbeatChecker checker = new HeartbeatChecker(gestorFalla);

        new Thread(checker, "hilo-heartbeat-checker").start();
        MonitorSalud monitorSalud = new MonitorSalud(puertoMonitor, checker);
        new Thread(monitorSalud, "hilo-monitor-salud").start();



        System.out.println("[Monitor] Iniciado en puerto " + puertoMonitor);
    } 
}