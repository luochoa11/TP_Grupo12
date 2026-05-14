package com.sgf;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.sgf.aplicacion.ILogicaFila;
import com.sgf.aplicacion.LogicaFila;
import com.sgf.disponibilidad.EmisorHeartbeat;
import com.sgf.disponibilidad.SincronizadorEstado;
import com.sgf.infraestructura.ServidorCentral;

public class MainServidor {
    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Uso: MainServidor <puerto>");
            System.exit(1);
        }

        boolean esPrimario;
        int     puerto        = Integer.parseInt(args[1]);
        String  ip            = InetAddress.getLocalHost().getHostAddress();

        String directorioIp     = ConfiguracionRed.get("directorio.ip");
        int    directorioPuerto = ConfiguracionRed.getInt("directorio.puerto");
        String monitorIp        = ConfiguracionRed.get("monitor.ip");
        int    monitorPuerto    = ConfiguracionRed.getInt("monitor.puerto");

        // Registrarse en el Directorio
        try (Socket socket = new Socket(directorioIp, directorioPuerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("REGISTRAR");
            out.writeObject(ip);
            out.writeObject(puerto);
            out.flush();
            String rol = (String) in.readObject(); //devuelve el rol
            esPrimario = "Primario".equalsIgnoreCase(rol);
        }

        // Arrancar servidor
        
        ILogicaFila logica = LogicaFila.getInstance();
        SincronizadorEstado sincronizador = new SincronizadorEstado(logica, directorioIp, directorioPuerto);
        ServidorCentral servidor = new ServidorCentral(puerto, ip, logica, esPrimario, sincronizador);
        new Thread(servidor, "hilo-servidor").start();

        // Arrancar heartbeat hacia el Monitor
        EmisorHeartbeat heartbeat = new EmisorHeartbeat(monitorIp, monitorPuerto, ip, puerto, esPrimario,servidor);
        new Thread(heartbeat, "hilo-heartbeat").start();
        // Desregistrarse al bajar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            heartbeat.detener();
            System.out.println("[Servidor] Apagando...");
        }));

        System.out.println("[Servidor] " + (esPrimario ? "PRIMARIO" : "SECUNDARIO")
            + " corriendo en " + ip + ":" + puerto);
    }
}