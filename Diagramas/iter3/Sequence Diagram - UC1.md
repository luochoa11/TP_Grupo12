```mermaid

sequenceDiagram
    actor C as Cliente

    box rgb(200, 220, 240) Terminal Registro
        participant V as VentanaRegistro
        participant Ctrl as ControladorRegistro
        participant ProxyR as ProxyRegistro
    end

    box rgb(255, 245, 150) Directorio
        participant D as Directorio
    end

    box rgb(240, 200, 200) Servidor Central
        participant M as ManejadorRegistro
        participant S_Primario as Servidor Primario
        participant L as LogicaFila
        participant S_Secundario as Servidor Secundario
    end
    
    note over V,S_Secundario: UC1 - Cliente solicita nuevo turno

    C->>V: Ingresa DNI y presiona Ingresar
    activate V
    V->>+Ctrl: ingresarDNI()

    Ctrl->>+Ctrl: validarDNI(dni)
    deactivate Ctrl

    alt Formato Invalido
        Ctrl->>V: mostrarMensaje(DNI invalido)

    else Formato Correcto
        Ctrl->>+ProxyR: IServicioRegistro.agregarTurno(turno)

        ProxyR->>+D: GET_RUTA_PRIMARIA
        D-->>-ProxyR: IP/puerto primario

        ProxyR->>+M: solicitarNuevoTurno()
        ProxyR->>M: enviarDatosTurno(turno)

        alt Servidor primario caído
            loop Reintentar conexión (3 intentos)
                M->>S_Primario: intentarConectar()
                S_Primario-->>M: No responde
            end
            alt Se reintenta sin éxito
                M-->>ProxyR: respuesta(ERROR_SERVIDOR_CAIDO)
                ProxyR-->>Ctrl: Error de servidor
                Ctrl->>V: mostrarMensaje(Servidor no disponible, intente más tarde)
            end
        else Servidor primario disponible
            M->>+S_Primario: verificarDisponibilidad()
            S_Primario-->>-M: Servidor Activo

            M->>L: ILogicaFila.agregarTurno(turno)

            alt DNI Duplicado
                L-->>M: DNI REPETIDO
                M-->>ProxyR: respuesta(ERROR_DNI_REPETIDO)
                ProxyR-->>Ctrl: DNI REPETIDO
                Ctrl->>V: mostrarMensaje(DNI ya registrado)
            else Exito
                L-->>M: Turno agregado

                par Sincronizacion Servidor Secundario
                    M->>+S_Secundario: sincronizarEstado(turno)
                    S_Secundario-->>-M: Confirmacion
                end

                M-->>ProxyR: respuesta(OK)
                ProxyR-->>Ctrl: Registro exitoso
                Ctrl->>V: mostrarMensaje(Turno registrado con exito)
                V->>C: Muestra comprobante
            end
        end
    end

    deactivate V
