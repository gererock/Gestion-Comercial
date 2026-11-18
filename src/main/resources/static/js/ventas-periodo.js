(() => {

    'use strict';


    const API_VENTAS_PERIODO =
        '/api/admin/ventas/periodo';


    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );


    const btnCerrarSesion =
        document.getElementById(
            'btnCerrarSesion'
        );


    const mensaje =
        document.getElementById(
            'mensaje'
        );


    const cargando =
        document.getElementById(
            'cargando'
        );


    const contenidoReporte =
        document.getElementById(
            'contenidoReporte'
        );


    const botonesPeriodo =
        document.querySelectorAll(
            '.btn-periodo'
        );


    const nombrePeriodo =
        document.getElementById(
            'nombrePeriodo'
        );


    const rangoFechas =
        document.getElementById(
            'rangoFechas'
        );


    const cantidadRealizadas =
        document.getElementById(
            'cantidadRealizadas'
        );


    const montoVendido =
        document.getElementById(
            'montoVendido'
        );


    const cantidadCobradas =
        document.getElementById(
            'cantidadCobradas'
        );


    const montoCobrado =
        document.getElementById(
            'montoCobrado'
        );


    const cantidadPendientes =
        document.getElementById(
            'cantidadPendientes'
        );


    const montoPendiente =
        document.getElementById(
            'montoPendiente'
        );


    const sinVentas =
        document.getElementById(
            'sinVentas'
        );


    let periodoCargado =
        null;


    let cargandoReporte =
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


        await cargarVentasPeriodo(
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
    }


    async function manejarCambioPeriodo(
        event
    ) {

        const periodo =
            event.currentTarget
                .dataset
                .periodo;


        if (
            !periodo
            ||
            periodo === periodoCargado
            ||
            cargandoReporte
        ) {

            return;
        }


        marcarPeriodoActivo(
            periodo
        );


        await cargarVentasPeriodo(
            periodo
        );
    }


    async function cargarVentasPeriodo(
        periodo
    ) {

        if (
            cargandoReporte
        ) {

            return;
        }


        cargandoReporte =
            true;

        periodoCargado =
            null;


        ocultarMensaje();

        mostrarCargando(
            true
        );


        bloquearBotones(
            true
        );


        try {

            const parametros =
                new URLSearchParams();


            parametros.set(
                'periodo',
                periodo
            );


            const url =
                `${API_VENTAS_PERIODO}?${parametros.toString()}`;


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


            mostrarReporte(
                datos
            );

            periodoCargado =
                periodo;

        } catch (error) {

            console.error(
                error
            );


            contenidoReporte.classList.add(
                'oculto'
            );


            mostrarMensaje(
                error.message
                ||
                'No se pudo consultar las ventas del período.',
                'error'
            );

        } finally {

            cargandoReporte =
                false;


            mostrarCargando(
                false
            );


            bloquearBotones(
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
                'La respuesta del reporte de ventas no es válida.'
            );
        }


        const camposNumericos =
            [
                datos.cantidadVentasRealizadas,
                datos.montoTotalVendido,
                datos.cantidadVentasCobradas,
                datos.montoTotalCobrado
            ];


        const contieneValorInvalido =
            camposNumericos.some(
                valor =>
                    !Number.isFinite(
                        Number(
                            valor
                        )
                    )
            );


        if (
            contieneValorInvalido
        ) {

            throw new Error(
                'Los valores recibidos del reporte no son válidos.'
            );
        }
    }


    function mostrarReporte(
        datos
    ) {

        const realizadas =
            Number(
                datos.cantidadVentasRealizadas
                ??
                0
            );


        const vendido =
            Number(
                datos.montoTotalVendido
                ??
                0
            );


        const cobradas =
            Number(
                datos.cantidadVentasCobradas
                ??
                0
            );


        const cobrado =
            Number(
                datos.montoTotalCobrado
                ??
                0
            );


        const pendientes =
            Math.max(
                realizadas - cobradas,
                0
            );


        const pendienteCobro =
            Math.max(
                vendido - cobrado,
                0
            );


        nombrePeriodo.textContent =
            formatearNombrePeriodo(
                datos.periodo
            );


        rangoFechas.textContent =
            construirTextoRango(
                datos.fechaDesde,
                datos.fechaHasta
            );


        cantidadRealizadas.textContent =
            realizadas;


        montoVendido.textContent =
            formatearMoneda(
                vendido
            );


        cantidadCobradas.textContent =
            cobradas;


        montoCobrado.textContent =
            formatearMoneda(
                cobrado
            );


        cantidadPendientes.textContent =
            pendientes;


        montoPendiente.textContent =
            formatearMoneda(
                pendienteCobro
            );


        if (
            realizadas === 0
        ) {

            sinVentas.classList.remove(
                'oculto'
            );

        } else {

            sinVentas.classList.add(
                'oculto'
            );
        }


        contenidoReporte.classList.remove(
            'oculto'
        );
    }


    function marcarPeriodoActivo(
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


    function bloquearBotones(
        bloquear
    ) {

        botonesPeriodo.forEach(
            boton => {

                boton.disabled =
                    bloquear;
            }
        );
    }


    function formatearNombrePeriodo(
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


            default:

                return periodo;
        }
    }


    function construirTextoRango(
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


        if (
            !Number.isFinite(
                valor
            )
        ) {

            return '$ 0,00';
        }


        return valor.toLocaleString(
            'es-AR',
            {
                style:
                    'currency',

                currency:
                    'ARS'
            }
        );
    }


    function mostrarCargando(
        mostrar
    ) {

        if (
            mostrar
        ) {

            cargando.classList.remove(
                'oculto'
            );


            return;
        }


        cargando.classList.add(
            'oculto'
        );
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
                'mensaje-info'
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
                data.error
            ) {

                return data.error;
            }

        } catch (error) {

            console.error(
                'No se pudo leer la respuesta de error.',
                error
            );
        }


        switch (
        response.status
        ) {

            case 400:

                return 'El período seleccionado no es válido.';


            case 401:

                return 'La sesión no es válida o expiró.';


            case 403:

                return 'No tenés permisos para consultar este reporte.';


            default:

                return 'Ocurrió un error al consultar las ventas por período.';
        }
    }

})();
