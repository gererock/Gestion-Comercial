(() => {

    'use strict';


    const API_TICKET_PROMEDIO =
        '/api/admin/ticket-promedio';


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


    const botonesTipo =
        document.querySelectorAll(
            '.btn-tipo'
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


    const btnLimpiarFiltros =
        document.getElementById(
            'btnLimpiarFiltros'
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


    const detalleConsulta =
        document.getElementById(
            'detalleConsulta'
        );


    const rangoFechas =
        document.getElementById(
            'rangoFechas'
        );


    const cantidadVentas =
        document.getElementById(
            'cantidadVentas'
        );


    const totalVendido =
        document.getElementById(
            'totalVendido'
        );


    const ticketPromedio =
        document.getElementById(
            'ticketPromedio'
        );


    const sinVentas =
        document.getElementById(
            'sinVentas'
        );


    let periodoActual =
        'DIA';


    let tipoActual =
        'GENERAL';


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

        marcarPeriodoActivo(
            'DIA'
        );

        marcarTipoActivo(
            'GENERAL'
        );


        await consultarTicketPromedio();
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


        botonesTipo.forEach(
            boton => {

                boton.addEventListener(
                    'click',
                    manejarCambioTipo
                );
            }
        );


        formRango.addEventListener(
            'submit',
            manejarConsultaRango
        );


        formRango.addEventListener('input', () => {
            ocultarResultados();
            ocultarMensaje();
        });


        btnLimpiarFiltros.addEventListener(
            'click',
            limpiarFiltros
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


        marcarPeriodoActivo(
            periodoActual
        );


        ocultarMensaje();


        if (
            periodoActual === 'RANGO'
        ) {

            formRango.classList.remove(
                'oculto'
            );


            ocultarResultados();

            return;
        }


        formRango.classList.add(
            'oculto'
        );


        fechaDesde.value =
            '';


        fechaHasta.value =
            '';


        await consultarTicketPromedio();
    }


    async function manejarCambioTipo(
        event
    ) {

        if (
            consultando
        ) {

            return;
        }


        tipoActual =
            event.currentTarget
                .dataset
                .tipo;


        marcarTipoActivo(
            tipoActual
        );


        ocultarMensaje();


        await consultarTicketPromedio();
    }


    async function manejarConsultaRango(event) {

        event.preventDefault();

        await consultarTicketPromedio();
    }


    async function limpiarFiltros() {

        if (
            consultando
        ) {

            return;
        }


        periodoActual =
            'DIA';


        tipoActual =
            'GENERAL';


        fechaDesde.value =
            '';


        fechaHasta.value =
            '';


        formRango.classList.add(
            'oculto'
        );


        marcarPeriodoActivo(
            periodoActual
        );


        marcarTipoActivo(
            tipoActual
        );


        ocultarMensaje();


        await consultarTicketPromedio();
    }


    async function consultarTicketPromedio() {

        if (consultando) {
            return;
        }

        ocultarResultados();
        ocultarMensaje();

        const parametros = new URLSearchParams({
            periodo: periodoActual,
            tipo: tipoActual
        });

        if (periodoActual === 'RANGO') {
            const desde = fechaDesde.value;
            const hasta = fechaHasta.value;

            if (!desde || !hasta) {
                mostrarMensaje('Debés seleccionar la fecha desde y la fecha hasta.', 'error');
                return;
            }

            if (desde > hasta) {
                mostrarMensaje('La fecha desde no puede ser posterior a la fecha hasta.', 'error');
                return;
            }

            parametros.set('desde', desde);
            parametros.set('hasta', hasta);
        }

        consultando =
            true;


        mostrarCargando(true);
        bloquearControles(true);

        try {

            const url =
                `${API_TICKET_PROMEDIO}?${parametros.toString()}`;


            const response =
                await Auth.fetchAutenticado(

                    url,

                    {
                        method: 'GET',
                        avisarSesionExpirada: true
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


            tipoActual =
                datos.tipo;


            marcarPeriodoActivo(
                periodoActual
            );


            marcarTipoActivo(
                tipoActual
            );


            mostrarDatos(
                datos
            );

        } catch (error) {

            mostrarMensaje(

                error.message
                ||
                'No se pudo consultar el ticket promedio.',

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
            ||
            !datos.tipo
        ) {

            throw new Error(
                'La respuesta recibida no es válida.'
            );
        }


        const cantidad =
            Number(
                datos.cantidadVentas
            );


        const total =
            Number(
                datos.totalVendido
            );


        const promedio =
            Number(
                datos.ticketPromedio
            );


        if (
            !Number.isFinite(
                cantidad
            )
            ||
            cantidad < 0
        ) {

            throw new Error(
                'La cantidad de ventas recibida no es válida.'
            );
        }


        if (
            !Number.isFinite(
                total
            )
            ||
            total < 0
        ) {

            throw new Error(
                'El total vendido recibido no es válido.'
            );
        }


        if (
            !Number.isFinite(
                promedio
            )
            ||
            promedio < 0
        ) {

            throw new Error(
                'El ticket promedio recibido no es válido.'
            );
        }
    }


    function mostrarDatos(
        datos
    ) {

        const cantidad =
            Number(
                datos.cantidadVentas
                ??
                0
            );


        const total =
            Number(
                datos.totalVendido
                ??
                0
            );


        const promedio =
            Number(
                datos.ticketPromedio
                ??
                0
            );


        nombrePeriodo.textContent =
            formatearPeriodo(
                datos.periodo
            );


        detalleConsulta.textContent =
            `Tipo: ${formatearTipo(datos.tipo)}`;


        rangoFechas.textContent =
            construirRango(
                datos.fechaDesde,
                datos.fechaHasta
            );


        cantidadVentas.textContent =
            cantidad.toLocaleString(
                'es-AR'
            );


        totalVendido.textContent =
            formatearMoneda(
                total
            );


        ticketPromedio.textContent =
            formatearMoneda(
                promedio
            );


        sinVentas.classList.toggle(
            'oculto',
            cantidad !== 0
        );


        contenidoReporte.classList.remove(
            'oculto'
        );
    }


    function marcarPeriodoActivo(
        periodo
    ) {

        botonesPeriodo.forEach(
            boton => {

                const activo = boton.dataset.periodo === periodo;

                boton.classList.toggle('activo', activo);
                boton.setAttribute('aria-pressed', String(activo));
            }
        );
    }


    function marcarTipoActivo(
        tipo
    ) {

        botonesTipo.forEach(
            boton => {

                const activo = boton.dataset.tipo === tipo;

                boton.classList.toggle('activo', activo);
                boton.setAttribute('aria-pressed', String(activo));
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


    function formatearTipo(
        tipo
    ) {

        switch (
        tipo
        ) {

            case 'GENERAL':

                return 'General';


            case 'ONLINE':

                return 'Online';


            case 'LOCAL':

                return 'Local';


            default:

                return tipo;
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


    function ocultarResultados() {

        contenidoReporte.classList.add('oculto');
        sinVentas.classList.add('oculto');
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


        botonesTipo.forEach(
            boton => {

                boton.disabled =
                    bloquear;
            }
        );


        fechaDesde.disabled =
            bloquear;


        fechaHasta.disabled =
            bloquear;


        btnLimpiarFiltros.disabled =
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

        } catch {

            // Algunas respuestas HTTP no tienen un cuerpo JSON.
        }


        switch (
        response.status
        ) {

            case 400:

                return 'Los datos de la consulta no son válidos.';


            case 401:

                return 'La sesión expiró o no es válida.';


            case 403:

                return 'No tenés permisos para consultar el ticket promedio.';


            case 404:

                return 'No se encontró la información solicitada.';


            default:

                return 'Ocurrió un error al consultar el ticket promedio.';
        }
    }

})();