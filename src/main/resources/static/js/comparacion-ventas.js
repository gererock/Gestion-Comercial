(() => {

    'use strict';


    const API_COMPARACION =
        '/api/admin/comparacion-ventas';


    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );


    const btnCerrarSesion =
        document.getElementById(
            'btnCerrarSesion'
        );


    const botonesPeriodo =
        document.querySelectorAll(
            '.btn-periodo'
        );


    const formRango =
        document.getElementById(
            'formRango'
        );


    const fechaDesde =
        document.getElementById(
            'fechaDesde'
        );


    const fechaHasta =
        document.getElementById(
            'fechaHasta'
        );


    const cargando =
        document.getElementById(
            'cargando'
        );


    const contenidoReporte =
        document.getElementById(
            'contenidoReporte'
        );


    const mensaje =
        document.getElementById(
            'mensaje'
        );


    const nombrePeriodo =
        document.getElementById(
            'nombrePeriodo'
        );


    const rangoFechas =
        document.getElementById(
            'rangoFechas'
        );


    const cantidadVentasOnline =
        document.getElementById(
            'cantidadVentasOnline'
        );


    const montoTotalOnline =
        document.getElementById(
            'montoTotalOnline'
        );


    const cantidadVentasLocales =
        document.getElementById(
            'cantidadVentasLocales'
        );


    const montoTotalLocal =
        document.getElementById(
            'montoTotalLocal'
        );


    const sinVentas =
        document.getElementById(
            'sinVentas'
        );


    let periodoActual =
        'DIA';


    let consultando =
        false;


    document.addEventListener(
        'DOMContentLoaded',
        iniciar
    );


    async function iniciar() {

        if (
            !Auth.requerirRol(
                'ADMINISTRADOR'
            )
        ) {

            return;
        }


        mostrarUsuario();

        registrarEventos();

        marcarActivo(
            'DIA'
        );


        await consultarComparacion(
            'DIA'
        );
    }


    function mostrarUsuario() {

        const usuario =
            Auth.obtenerUsuario();


        if (
            !usuario
        ) {

            return;
        }


        nombreUsuario.textContent =
            usuario.apellido
                ? `${usuario.nombre} ${usuario.apellido}`
                : usuario.nombre;
    }


    function registrarEventos() {

        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        botonesPeriodo.forEach(
            boton => {

                boton.addEventListener(
                    'click',
                    manejarCambioPeriodo
                );
            }
        );


        formRango.addEventListener(
            'submit',
            manejarConsultaRango
        );
    }


    async function manejarCambioPeriodo(
        event
    ) {

        if (
            consultando
        ) {

            return;
        }


        const periodo =
            event.currentTarget
                .dataset
                .periodo;


        periodoActual =
            periodo;


        marcarActivo(
            periodo
        );


        ocultarMensaje();


        if (
            periodo === 'RANGO'
        ) {

            formRango.classList.remove(
                'oculto'
            );


            contenidoReporte.classList.add(
                'oculto'
            );


            sinVentas.classList.add(
                'oculto'
            );


            return;
        }


        formRango.classList.add(
            'oculto'
        );


        await consultarComparacion(
            periodo
        );
    }


    async function manejarConsultaRango(
        event
    ) {

        event.preventDefault();


        if (
            consultando
        ) {

            return;
        }


        ocultarMensaje();


        const desde =
            fechaDesde.value;


        const hasta =
            fechaHasta.value;


        if (
            !desde
            ||
            !hasta
        ) {

            mostrarMensaje(
                'Debés seleccionar la fecha desde y la fecha hasta.',
                'error'
            );


            return;
        }


        if (
            desde > hasta
        ) {

            mostrarMensaje(
                'La fecha desde no puede ser posterior a la fecha hasta.',
                'error'
            );


            return;
        }


        await consultarComparacion(
            'RANGO',
            desde,
            hasta
        );
    }


    async function consultarComparacion(

        periodo,

        desde = null,

        hasta = null

    ) {

        if (
            consultando
        ) {

            return;
        }


        consultando =
            true;


        ocultarMensaje();


        mostrarCargando(
            true
        );


        bloquearControles(
            true
        );


        try {

            const parametros =
                new URLSearchParams();


            parametros.set(
                'periodo',
                periodo
            );


            if (
                periodo === 'RANGO'
            ) {

                parametros.set(
                    'desde',
                    desde
                );


                parametros.set(
                    'hasta',
                    hasta
                );
            }


            const url =
                `${API_COMPARACION}?${parametros.toString()}`;


            const response =
                await Auth.fetchAutenticado(

                    url,

                    {
                        method:
                            'GET'
                    }
                );


            if (
                !response
            ) {

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


            const datos =
                await response.json();


            validarRespuesta(
                datos
            );


            periodoActual =
                datos.periodo;


            marcarActivo(
                datos.periodo
            );


            mostrarDatos(
                datos
            );

        } catch (error) {

            console.error(
                error
            );


            contenidoReporte.classList.add(
                'oculto'
            );


            sinVentas.classList.add(
                'oculto'
            );


            mostrarMensaje(

                error.message
                ||
                'No se pudo consultar la comparación de ventas.',

                'error'
            );

        } finally {

            consultando =
                false;


            mostrarCargando(
                false
            );


            bloquearControles(
                false
            );
        }
    }


    function validarRespuesta(
        datos
    ) {

        if (
            !datos
            ||
            !datos.periodo
            ||
            !datos.fechaDesde
            ||
            !datos.fechaHasta
        ) {

            throw new Error(
                'La respuesta recibida no es válida.'
            );
        }


        const cantidadOnline =
            Number(
                datos.cantidadVentasOnline
            );


        const montoOnline =
            Number(
                datos.montoTotalOnline
            );


        const cantidadLocal =
            Number(
                datos.cantidadVentasLocales
            );


        const montoLocal =
            Number(
                datos.montoTotalLocal
            );


        if (
            !Number.isFinite(
                cantidadOnline
            )
            ||
            cantidadOnline < 0
        ) {

            throw new Error(
                'La cantidad de ventas online recibida no es válida.'
            );
        }


        if (
            !Number.isFinite(
                montoOnline
            )
            ||
            montoOnline < 0
        ) {

            throw new Error(
                'El monto total online recibido no es válido.'
            );
        }


        if (
            !Number.isFinite(
                cantidadLocal
            )
            ||
            cantidadLocal < 0
        ) {

            throw new Error(
                'La cantidad de ventas locales recibida no es válida.'
            );
        }


        if (
            !Number.isFinite(
                montoLocal
            )
            ||
            montoLocal < 0
        ) {

            throw new Error(
                'El monto total local recibido no es válido.'
            );
        }
    }


    function mostrarDatos(
        datos
    ) {

        const cantidadOnline =
            Number(
                datos.cantidadVentasOnline
                ??
                0
            );


        const montoOnline =
            Number(
                datos.montoTotalOnline
                ??
                0
            );


        const cantidadLocal =
            Number(
                datos.cantidadVentasLocales
                ??
                0
            );


        const montoLocal =
            Number(
                datos.montoTotalLocal
                ??
                0
            );


        nombrePeriodo.textContent =
            formatearPeriodo(
                datos.periodo
            );


        rangoFechas.textContent =
            construirRango(
                datos.fechaDesde,
                datos.fechaHasta
            );


        cantidadVentasOnline.textContent =
            cantidadOnline;


        montoTotalOnline.textContent =
            formatearMoneda(
                montoOnline
            );


        cantidadVentasLocales.textContent =
            cantidadLocal;


        montoTotalLocal.textContent =
            formatearMoneda(
                montoLocal
            );


        const noHayVentas =
            cantidadOnline === 0
            &&
            cantidadLocal === 0;


        sinVentas.classList.toggle(
            'oculto',
            !noHayVentas
        );


        contenidoReporte.classList.remove(
            'oculto'
        );
    }


    function marcarActivo(
        periodo
    ) {

        botonesPeriodo.forEach(
            boton => {

                boton.classList.toggle(

                    'activo',

                    boton.dataset.periodo
                        === periodo
                );
            }
        );
    }


    function formatearPeriodo(
        periodo
    ) {

        switch (
        periodo
        ) {

            case 'DIA':

                return 'Hoy';


            case 'SEMANA':

                return 'Esta semana';


            case 'MES':

                return 'Este mes';


            case 'RANGO':

                return 'Rango personalizado';


            default:

                return periodo;
        }
    }


    function construirRango(
        desde,
        hasta
    ) {

        if (
            desde === hasta
        ) {

            return formatearFecha(
                desde
            );
        }


        return `${formatearFecha(desde)} al ${formatearFecha(hasta)}`;
    }


    function formatearFecha(
        fecha
    ) {

        if (
            !fecha
        ) {

            return '-';
        }


        const partes =
            fecha.split(
                '-'
            );


        if (
            partes.length !== 3
        ) {

            return fecha;
        }


        return `${partes[2]}/${partes[1]}/${partes[0]}`;
    }


    function formatearMoneda(
        monto
    ) {

        const valor =
            Number(
                monto
                ??
                0
            );


        return valor.toLocaleString(
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
        );
    }


    function mostrarCargando(
        mostrar
    ) {

        cargando.classList.toggle(
            'oculto',
            !mostrar
        );
    }


    function bloquearControles(
        bloquear
    ) {

        botonesPeriodo.forEach(
            boton => {

                boton.disabled =
                    bloquear;
            }
        );


        fechaDesde.disabled =
            bloquear;


        fechaHasta.disabled =
            bloquear;


        const botonRango =
            formRango.querySelector(
                'button[type="submit"]'
            );


        if (
            botonRango
        ) {

            botonRango.disabled =
                bloquear;
        }
    }


    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensaje.textContent =
            texto;


        mensaje.className =
            'mensaje';


        if (
            tipo === 'error'
        ) {

            mensaje.classList.add(
                'mensaje-error'
            );

        } else {

            mensaje.classList.add(
                'mensaje-exito'
            );
        }


        mensaje.classList.remove(
            'oculto'
        );
    }


    function ocultarMensaje() {

        mensaje.classList.add(
            'oculto'
        );


        mensaje.textContent =
            '';
    }


    async function obtenerMensajeError(
        response
    ) {

        try {

            const data =
                await response.json();


            if (
                data.message
            ) {

                return data.message;
            }


            if (
                data.mensaje
            ) {

                return data.mensaje;
            }

        } catch (error) {

            console.error(
                error
            );
        }


        switch (
        response.status
        ) {

            case 400:

                return 'Los datos de la consulta no son válidos.';


            case 401:

                return 'La sesión expiró o no es válida.';


            case 403:

                return 'No tenés permisos para consultar la comparación de ventas.';


            case 404:

                return 'No se encontró la información solicitada.';


            default:

                return 'Ocurrió un error al consultar la comparación de ventas.';
        }
    }

})();