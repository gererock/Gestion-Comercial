(() => {

    'use strict';



    const URL_API =
        '/api/configuracion-comercio/localidades-envio';


    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );


    const botonCerrarSesion =
        document.getElementById(
            'botonCerrarSesion'
        );


    const mensaje =
        document.getElementById(
            'mensaje'
        );


    const cargando =
        document.getElementById(
            'cargando'
        );


    const errorCarga =
        document.getElementById(
            'errorCarga'
        );


    const botonReintentar =
        document.getElementById(
            'botonReintentar'
        );


    const seccionConfiguracion =
        document.getElementById(
            'seccionConfiguracion'
        );


    const estadoVillaMaria =
        document.getElementById(
            'estadoVillaMaria'
        );


    const estadoVillaNueva =
        document.getElementById(
            'estadoVillaNueva'
        );


    const fechaActualizacion =
        document.getElementById(
            'fechaActualizacion'
        );


    const formularioLocalidades =
        document.getElementById(
            'formularioLocalidades'
        );


    const casillaVillaMaria =
        document.getElementById(
            'casillaVillaMaria'
        );


    const casillaVillaNueva =
        document.getElementById(
            'casillaVillaNueva'
        );


    const botonCancelar =
        document.getElementById(
            'botonCancelar'
        );


    const botonGuardar =
        document.getElementById(
            'botonGuardar'
        );



    let configuracionActual =
        null;


    let guardando =
        false;


    iniciar();


    function iniciar() {

        if (
            !Auth.requerirRol(
                'ADMINISTRADOR'
            )
        ) {

            return;
        }


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

        botonCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        botonReintentar.addEventListener(
            'click',
            cargarConfiguracion
        );


        formularioLocalidades.addEventListener(
            'submit',
            guardarConfiguracion
        );


        botonCancelar.addEventListener(
            'click',
            cancelarCambios
        );


        casillaVillaMaria.addEventListener(
            'change',
            ocultarMensaje
        );


        casillaVillaNueva.addEventListener(
            'change',
            ocultarMensaje
        );
    }

    async function cargarConfiguracion() {

        mostrarCargando();

        ocultarMensaje();


        try {

            const respuesta =
                await Auth.fetchAutenticado(
                    URL_API,
                    {
                        headers: {
                            'Accept':
                                'application/json'
                        }
                    }
                );


            if (
                !respuesta
            ) {

                return;
            }


            if (
                respuesta.status === 403
            ) {

                Auth.redirigirSegunRol(
                    Auth.obtenerRol()
                );


                return;
            }


            if (
                !respuesta.ok
            ) {

                throw new Error(
                    'No se pudo cargar la configuración de localidades.'
                );
            }


            const configuracion =
                await respuesta.json();


            validarConfiguracion(
                configuracion
            );


            configuracionActual =
                configuracion;


            renderizarConfiguracion();


        } catch (error) {

            console.error(
                'Error al cargar las localidades:',
                error
            );


            mostrarErrorCarga();
        }
    }


    function validarConfiguracion(
        configuracion
    ) {

        if (
            !configuracion
            ||
            typeof configuracion
                .permiteEnvioVillaMaria
                !== 'boolean'
            ||
            typeof configuracion
                .permiteEnvioVillaNueva
                !== 'boolean'
        ) {

            throw new Error(
                'La configuración recibida no es válida.'
            );
        }
    }



    function renderizarConfiguracion() {

        casillaVillaMaria.checked =
            configuracionActual
                .permiteEnvioVillaMaria;


        casillaVillaNueva.checked =
            configuracionActual
                .permiteEnvioVillaNueva;


        actualizarEstadoLocalidad(
            estadoVillaMaria,
            configuracionActual
                .permiteEnvioVillaMaria
        );


        actualizarEstadoLocalidad(
            estadoVillaNueva,
            configuracionActual
                .permiteEnvioVillaNueva
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



    function actualizarEstadoLocalidad(
        elemento,
        habilitada
    ) {

        elemento.classList.remove(
            'estado-habilitado',
            'estado-deshabilitado'
        );


        if (
            habilitada
        ) {

            elemento.textContent =
                'Habilitada';


            elemento.classList.add(
                'estado-habilitado'
            );

        } else {

            elemento.textContent =
                'Deshabilitada';


            elemento.classList.add(
                'estado-deshabilitado'
            );
        }
    }

    async function guardarConfiguracion(
        evento
    ) {

        evento.preventDefault();


        if (
            guardando
        ) {

            return;
        }


        ocultarMensaje();


        const datos = {

            permiteEnvioVillaMaria:
                casillaVillaMaria.checked,

            permiteEnvioVillaNueva:
                casillaVillaNueva.checked
        };


        guardando =
            true;


        botonGuardar.disabled =
            true;


        botonCancelar.disabled =
            true;


        casillaVillaMaria.disabled =
            true;


        casillaVillaNueva.disabled =
            true;


        const textoOriginal =
            botonGuardar.textContent;


        botonGuardar.textContent =
            'Guardando...';


        try {

            const respuesta =
                await Auth.fetchAutenticado(
                    URL_API,
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
                            JSON.stringify(
                                datos
                            )
                    }
                );


            if (
                !respuesta
            ) {

                return;
            }


            if (
                respuesta.status === 403
            ) {

                Auth.redirigirSegunRol(
                    Auth.obtenerRol()
                );


                return;
            }


            if (
                !respuesta.ok
            ) {

                throw new Error(
                    await obtenerMensajeError(
                        respuesta
                    )
                );
            }


            const configuracion =
                await respuesta.json();


            validarConfiguracion(
                configuracion
            );


            configuracionActual =
                configuracion;


            renderizarConfiguracion();


            mostrarMensaje(
                'Las localidades de envío se actualizaron correctamente.',
                'exito'
            );


        } catch (error) {

            console.error(
                'Error al guardar las localidades:',
                error
            );


            mostrarMensaje(
                error.message
                ||
                'No se pudo guardar la configuración.',
                'error'
            );


        } finally {

            guardando =
                false;


            botonGuardar.disabled =
                false;


            botonCancelar.disabled =
                false;


            casillaVillaMaria.disabled =
                false;


            casillaVillaNueva.disabled =
                false;


            botonGuardar.textContent =
                textoOriginal;
        }
    }


    function cancelarCambios() {

        ocultarMensaje();


        if (
            !configuracionActual
        ) {

            return;
        }


        casillaVillaMaria.checked =
            configuracionActual
                .permiteEnvioVillaMaria;


        casillaVillaNueva.checked =
            configuracionActual
                .permiteEnvioVillaNueva;


        mostrarMensaje(
            'Los cambios no guardados fueron descartados.',
            'exito'
        );
    }


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


    async function obtenerMensajeError(
        respuesta
    ) {

        try {

            const cuerpo =
                await respuesta.json();


            if (
                cuerpo.errors
                &&
                typeof cuerpo.errors
                    === 'object'
            ) {

                const errores =
                    Object.values(
                        cuerpo.errors
                    );


                if (
                    errores.length > 0
                ) {

                    return errores[0];
                }
            }


            if (
                typeof cuerpo.message
                    === 'string'
                &&
                cuerpo.message.trim()
                    !== ''
            ) {

                return cuerpo.message.trim();
            }


        } catch (error) {

            // Si el backend no devuelve JSON,
            // utilizamos un mensaje genérico.
        }


        if (
            respuesta.status === 403
        ) {

            return 'No tenés permisos para modificar esta configuración.';
        }


        return 'No se pudo procesar la solicitud.';
    }

})();