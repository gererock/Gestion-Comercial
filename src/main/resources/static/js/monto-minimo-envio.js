(() => {

    'use strict';


    const API_URL =
        '/api/configuracion-comercio/monto-minimo-envio';


    const MONTO_MAXIMO =
        9999999999.99;


    // =====================================================
    // SEGURIDAD
    // =====================================================

    if (
        !Auth.requerirRol(
            'ADMINISTRADOR'
        )
    ) {

        return;
    }


    // =====================================================
    // ELEMENTOS
    // =====================================================

    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );


    const btnCerrarSesion =
        document.getElementById(
            'btnCerrarSesion'
        );


    const cargando =
        document.getElementById(
            'cargando'
        );


    const errorCarga =
        document.getElementById(
            'errorCarga'
        );


    const btnReintentar =
        document.getElementById(
            'btnReintentar'
        );


    const seccionConfiguracion =
        document.getElementById(
            'seccionConfiguracion'
        );


    const montoActual =
        document.getElementById(
            'montoActual'
        );


    const fechaActualizacion =
        document.getElementById(
            'fechaActualizacion'
        );


    const formMonto =
        document.getElementById(
            'formMonto'
        );


    const montoMinimo =
        document.getElementById(
            'montoMinimo'
        );


    const errorMonto =
        document.getElementById(
            'errorMonto'
        );


    const btnCancelar =
        document.getElementById(
            'btnCancelar'
        );


    const btnGuardar =
        document.getElementById(
            'btnGuardar'
        );


    const mensaje =
        document.getElementById(
            'mensaje'
        );


    // =====================================================
    // ESTADO
    // =====================================================

    let configuracionActual =
        null;


    let guardando =
        false;


    // =====================================================
    // INICIO
    // =====================================================

    iniciar();


    function iniciar() {

        configurarUsuario();

        configurarEventos();

        cargarConfiguracion();
    }


    function configurarUsuario() {

        const usuario =
            Auth.obtenerUsuario();


        if (
            usuario
            &&
            usuario.nombre
        ) {

            nombreUsuario.textContent =
                usuario.nombre;
        }
    }


    function configurarEventos() {

        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        btnReintentar.addEventListener(
            'click',
            cargarConfiguracion
        );


        formMonto.addEventListener(
            'submit',
            guardarConfiguracion
        );


        btnCancelar.addEventListener(
            'click',
            cancelarCambios
        );


        montoMinimo.addEventListener(
            'input',
            () => {

                ocultarErrorMonto();

                ocultarMensaje();
            }
        );
    }


    // =====================================================
    // CARGAR
    // =====================================================

    async function cargarConfiguracion() {

        mostrarCargando();

        ocultarMensaje();


        try {

            const response =
                await Auth.fetchAutenticado(
                    API_URL,
                    {
                        headers: {
                            'Accept':
                                'application/json'
                        }
                    }
                );


            if (
                !response
            ) {

                return;
            }


            if (
                response.status === 403
            ) {

                window.location.href =
                    Auth.obtenerRutaPorRol(
                        Auth.obtenerRol()
                    );


                return;
            }


            if (
                !response.ok
            ) {

                throw new Error(
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            const configuracion =
                await response.json();


            validarRespuesta(
                configuracion
            );


            configuracionActual =
                configuracion;


            renderizarConfiguracion();


        } catch (error) {

            console.error(
                'Error al cargar configuración:',
                error
            );


            mostrarErrorCarga();
        }
    }


    function validarRespuesta(
        configuracion
    ) {

        if (
            !configuracion
            ||
            configuracion.montoMinimoEnvio == null
        ) {

            throw new Error(
                'La configuración recibida no es válida.'
            );
        }


        const monto =
            Number(
                configuracion.montoMinimoEnvio
            );


        if (
            !Number.isFinite(
                monto
            )
        ) {

            throw new Error(
                'El monto mínimo recibido no es válido.'
            );
        }
    }


    function renderizarConfiguracion() {

        const monto =
            Number(
                configuracionActual
                    .montoMinimoEnvio
            );


        montoActual.textContent =
            formatearDinero(
                monto
            );


        montoMinimo.value =
            normalizarParaInput(
                monto
            );


        fechaActualizacion.textContent =
            formatearFecha(
                configuracionActual
                    .fechaActualizacion
            );


        cargando.classList.add(
            'oculto'
        );


        errorCarga.classList.add(
            'oculto'
        );


        seccionConfiguracion.classList.remove(
            'oculto'
        );
    }


    // =====================================================
    // GUARDAR
    // =====================================================

    async function guardarConfiguracion(
        event
    ) {

        event.preventDefault();


        if (
            guardando
        ) {

            return;
        }


        ocultarMensaje();

        ocultarErrorMonto();


        const montoValidado =
            validarMonto();


        if (
            montoValidado === null
        ) {

            return;
        }


        guardando =
            true;


        btnGuardar.disabled =
            true;


        btnCancelar.disabled =
            true;


        const textoOriginal =
            btnGuardar.textContent;


        btnGuardar.textContent =
            'Guardando...';


        try {

            const response =
                await Auth.fetchAutenticado(
                    API_URL,
                    {
                        method:
                            'PUT',

                        headers: {
                            'Content-Type':
                                'application/json',

                            'Accept':
                                'application/json'
                        },

                        body:
                            JSON.stringify({
                                montoMinimoEnvio:
                                    montoValidado
                            })
                    }
                );


            if (
                !response
            ) {

                return;
            }


            if (
                response.status === 403
            ) {

                window.location.href =
                    Auth.obtenerRutaPorRol(
                        Auth.obtenerRol()
                    );


                return;
            }


            if (
                !response.ok
            ) {

                throw new Error(
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            const configuracion =
                await response.json();


            validarRespuesta(
                configuracion
            );


            configuracionActual =
                configuracion;


            renderizarConfiguracion();


            mostrarMensaje(
                'El monto mínimo de envío se actualizó correctamente.',
                'exito'
            );


        } catch (error) {

            console.error(
                'Error al guardar configuración:',
                error
            );


            mostrarMensaje(
                error.message
                ||
                'No se pudo actualizar el monto mínimo de envío.',
                'error'
            );


        } finally {

            guardando =
                false;


            btnGuardar.disabled =
                false;


            btnCancelar.disabled =
                false;


            btnGuardar.textContent =
                textoOriginal;
        }
    }


    // =====================================================
    // VALIDACIÓN
    // =====================================================

    function validarMonto() {

        const valorOriginal =
            montoMinimo.value;


        const valor =
            valorOriginal.trim();


        if (
            valor === ''
        ) {

            mostrarErrorMonto(
                'El monto mínimo de envío es obligatorio.'
            );


            return null;
        }


        if (
            valor.includes(
                ','
            )
        ) {

            mostrarErrorMonto(
                'Usá punto para separar los decimales.'
            );


            return null;
        }


        if (
            valor.startsWith(
                '-'
            )
        ) {

            mostrarErrorMonto(
                'El monto mínimo de envío no puede ser negativo.'
            );


            return null;
        }


        if (
            !/^\d+(\.\d{1,2})?$/.test(
                valor
            )
        ) {

            mostrarErrorMonto(
                'Ingresá un monto numérico válido con hasta 2 decimales.'
            );


            return null;
        }


        const monto =
            Number(
                valor
            );


        if (
            !Number.isFinite(
                monto
            )
        ) {

            mostrarErrorMonto(
                'El monto ingresado no es válido.'
            );


            return null;
        }


        if (
            monto < 0
        ) {

            mostrarErrorMonto(
                'El monto mínimo de envío no puede ser negativo.'
            );


            return null;
        }


        if (
            monto > MONTO_MAXIMO
        ) {

            mostrarErrorMonto(
                'El monto supera el límite permitido de $9.999.999.999,99.'
            );


            return null;
        }


        montoMinimo.value =
            normalizarParaInput(
                monto
            );


        return monto;
    }


    function mostrarErrorMonto(
        texto
    ) {

        errorMonto.textContent =
            texto;


        errorMonto.classList.remove(
            'oculto'
        );


        montoMinimo.focus();
    }


    function ocultarErrorMonto() {

        errorMonto.textContent =
            '';


        errorMonto.classList.add(
            'oculto'
        );
    }


    // =====================================================
    // CANCELAR
    // =====================================================

    function cancelarCambios() {

        ocultarMensaje();

        ocultarErrorMonto();


        if (
            !configuracionActual
        ) {

            return;
        }


        montoMinimo.value =
            normalizarParaInput(
                configuracionActual
                    .montoMinimoEnvio
            );


        mostrarMensaje(
            'Los cambios no guardados fueron descartados.',
            'exito'
        );
    }


    // =====================================================
    // ESTADOS DE CARGA
    // =====================================================

    function mostrarCargando() {

        cargando.classList.remove(
            'oculto'
        );


        errorCarga.classList.add(
            'oculto'
        );


        seccionConfiguracion.classList.add(
            'oculto'
        );
    }


    function mostrarErrorCarga() {

        cargando.classList.add(
            'oculto'
        );


        seccionConfiguracion.classList.add(
            'oculto'
        );


        errorCarga.classList.remove(
            'oculto'
        );
    }


    // =====================================================
    // MENSAJES
    // =====================================================

    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensaje.textContent =
            texto;


        mensaje.classList.remove(
            'oculto',
            'mensaje-exito',
            'mensaje-error'
        );


        mensaje.classList.add(
            tipo === 'exito'
                ? 'mensaje-exito'
                : 'mensaje-error'
        );
    }


    function ocultarMensaje() {

        mensaje.textContent =
            '';


        mensaje.classList.add(
            'oculto'
        );


        mensaje.classList.remove(
            'mensaje-exito',
            'mensaje-error'
        );
    }


    // =====================================================
    // FORMATO
    // =====================================================

    function formatearDinero(
        valor
    ) {

        return new Intl.NumberFormat(
            'es-AR',
            {
                style:
                    'currency',

                currency:
                    'ARS',

                minimumFractionDigits:
                    2,

                maximumFractionDigits:
                    2
            }
        ).format(
            Number(
                valor
            )
        );
    }


    function normalizarParaInput(
        valor
    ) {

        const numero =
            Number(
                valor
            );


        if (
            !Number.isFinite(
                numero
            )
        ) {

            return '';
        }


        return numero.toFixed(
            2
        );
    }


    function formatearFecha(
        valor
    ) {

        if (
            !valor
        ) {

            return 'Sin información';
        }


        const fecha =
            new Date(
                valor
            );


        if (
            Number.isNaN(
                fecha.getTime()
            )
        ) {

            return 'Sin información';
        }


        return new Intl.DateTimeFormat(
            'es-AR',
            {
                dateStyle:
                    'medium',

                timeStyle:
                    'short'
            }
        ).format(
            fecha
        );
    }


    // =====================================================
    // ERROR API
    // =====================================================

    async function obtenerMensajeError(
        response
    ) {

        try {

            const body =
                await response.json();


            if (
                body.errors
                &&
                typeof body.errors === 'object'
            ) {

                const errores =
                    Object.values(
                        body.errors
                    );


                if (
                    errores.length > 0
                ) {

                    return errores[0];
                }
            }


            if (
                typeof body.message === 'string'
                &&
                body.message.trim() !== ''
            ) {

                return body.message.trim();
            }


        } catch (error) {

            // Se utilizará mensaje genérico.
        }


        if (
            response.status === 403
        ) {

            return 'No tenés permisos para modificar esta configuración.';
        }


        return 'No se pudo procesar la solicitud.';
    }

})();