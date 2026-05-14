package com.sgf;

import com.sgf.failover.GestorFalla;
import com.sgf.salud.HeartbeatChecker;
import com.sgf.salud.MonitorSalud;

public class MainMonitor {
    public static void main(String[] args) {


        if (args.length < 1) {
            System.err.println("Uso: MainMonitor <puertoMonitor>");
            System.exit(1);
        }

        int    puertoMonitor    = Integer.parseInt(args[0]);
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