(() => {

    'use strict';


    const API_GASTOS =
        '/api/admin/gastos';

    const API_CATEGORIAS =
        '/api/admin/categorias-gasto?activa=true';

    const API_MEDIOS_PAGO =
        '/api/medios-pago?activo=true';


    const formConsulta =
        document.getElementById(
            'formConsulta'
        );


    const periodo =
        document.getElementById(
            'periodo'
        );


    const desde =
        document.getElementById(
            'desde'
        );


    const hasta =
        document.getElementById(
            'hasta'
        );


    const categoriaGasto =
        document.getElementById(
            'categoriaGasto'
        );


    const medioPago =
        document.getElementById(
            'medioPago'
        );


    const btnLimpiar =
        document.getElementById(
            'btnLimpiar'
        );


    const btnConsultar =
        document.getElementById(
            'btnConsultar'
        );


    const btnCerrarSesion =
        document.getElementById(
            'btnCerrarSesion'
        );


    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );


    const mensaje =
        document.getElementById(
            'mensaje'
        );


    const totalGastos =
        document.getElementById(
            'totalGastos'
        );


    const cantidadGastos =
        document.getElementById(
            'cantidadGastos'
        );


    const periodoConsultado =
        document.getElementById(
            'periodoConsultado'
        );


    const textoResultados =
        document.getElementById(
            'textoResultados'
        );


    const cargando =
        document.getElementById(
            'cargando'
        );


    const sinResultados =
        document.getElementById(
            'sinResultados'
        );


    const contenedorTabla =
        document.getElementById(
            'contenedorTabla'
        );


    const tablaGastos =
        document.getElementById(
            'tablaGastos'
        );


    let consultando = false;


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

        prepararFormulario();

        await cargarDatosIniciales();
    }


    function registrarEventos() {

        formConsulta.addEventListener(
            'submit',
            consultarGastos
        );


        periodo.addEventListener(
            'change',
            actualizarCamposFecha
        );


        btnLimpiar.addEventListener(
            'click',
            limpiarFiltros
        );


        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
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


    function prepararFormulario() {

        periodo.value =
            'MES';


        desde.value =
            '';


        hasta.value =
            '';


        actualizarCamposFecha();

        reiniciarResultados();
    }


    async function cargarDatosIniciales() {

        ocultarMensaje();


        try {

            await Promise.all([
                cargarCategorias(),
                cargarMediosPago()
            ]);

        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                'No se pudieron cargar los filtros de la consulta.',
                'error'
            );
        }
    }


    async function cargarCategorias() {

        const response =
            await Auth.fetchAutenticado(
                API_CATEGORIAS,
                {
                    method: 'GET'
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


        const categorias =
            await response.json();


        categoriaGasto.innerHTML =
            `
                <option value="">
                    Todas las categorías
                </option>
            `;


        categorias.forEach(
            categoria => {

                const option =
                    document.createElement(
                        'option'
                    );


                option.value =
                    categoria.idCategoriaGasto;


                option.textContent =
                    categoria.nombre;


                categoriaGasto.appendChild(
                    option
                );
            }
        );
    }


    async function cargarMediosPago() {

        const response =
            await Auth.fetchAutenticado(
                API_MEDIOS_PAGO,
                {
                    method: 'GET'
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


        const medios =
            await response.json();


        medioPago.innerHTML =
            `
                <option value="">
                    Todos los medios de pago
                </option>
            `;


        medios.forEach(
            medio => {

                const option =
                    document.createElement(
                        'option'
                    );


                option.value =
                    medio.idMedioPago;


                option.textContent =
                    medio.nombre;


                medioPago.appendChild(
                    option
                );
            }
        );
    }


    function actualizarCamposFecha() {

        const esRango =
            periodo.value === 'RANGO';


        desde.disabled =
            !esRango;


        hasta.disabled =
            !esRango;


        if (
            !esRango
        ) {

            desde.value =
                '';


            hasta.value =
                '';
        }
    }


    async function consultarGastos(
        event
    ) {

        event.preventDefault();


        if (
            consultando
        ) {

            return;
        }


        ocultarMensaje();


        if (
            !validarFormulario()
        ) {

            return;
        }


        const parametros =
            construirParametros();


        const url =
            parametros.toString()
                ? `${API_GASTOS}?${parametros.toString()}`
                : API_GASTOS;


        try {

            consultando =
                true;


            bloquearFormulario(
                true
            );


            mostrarCargando(
                true
            );


            const response =
                await Auth.fetchAutenticado(
                    url,
                    {
                        method: 'GET'
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


            const resultado =
                await response.json();


            mostrarResultados(
                resultado
            );

        } catch (error) {

            console.error(
                error
            );


            limpiarTabla();


            reiniciarResumen();


            textoResultados.textContent =
                'No se pudo completar la consulta.';


            mostrarMensaje(
                error.message ||
                'No se pudieron consultar los gastos.',
                'error'
            );

        } finally {

            consultando =
                false;


            mostrarCargando(
                false
            );


            bloquearFormulario(
                false
            );
        }
    }


    function validarFormulario() {

        if (
            !periodo.value
        ) {

            mostrarMensaje(
                'Seleccioná un período para realizar la consulta.',
                'error'
            );


            periodo.focus();


            return false;
        }


        if (
            periodo.value === 'RANGO'
        ) {

            if (
                !desde.value
            ) {

                mostrarMensaje(
                    'Seleccioná la fecha desde.',
                    'error'
                );


                desde.focus();


                return false;
            }


            if (
                !hasta.value
            ) {

                mostrarMensaje(
                    'Seleccioná la fecha hasta.',
                    'error'
                );


                hasta.focus();


                return false;
            }


            const fechaDesde =
                new Date(
                    `${desde.value}T00:00:00`
                );


            const fechaHasta =
                new Date(
                    `${hasta.value}T00:00:00`
                );


            if (
                fechaDesde > fechaHasta
            ) {

                mostrarMensaje(
                    'La fecha desde no puede ser posterior a la fecha hasta.',
                    'error'
                );


                desde.focus();


                return false;
            }
        }


        return true;
    }


    function construirParametros() {

        const parametros =
            new URLSearchParams();


        if (
            periodo.value
        ) {

            parametros.set(
                'periodo',
                periodo.value
            );
        }


        if (
            periodo.value === 'RANGO'
        ) {

            parametros.set(
                'desde',
                desde.value
            );


            parametros.set(
                'hasta',
                hasta.value
            );
        }


        if (
            categoriaGasto.value
        ) {

            parametros.set(
                'idCategoriaGasto',
                categoriaGasto.value
            );
        }


        if (
            medioPago.value
        ) {

            parametros.set(
                'idMedioPago',
                medioPago.value
            );
        }


        return parametros;
    }


    function mostrarResultados(
        resultado
    ) {

        const gastos =
            Array.isArray(
                resultado.gastos
            )
                ? resultado.gastos
                : [];


        totalGastos.textContent =
            formatearMoneda(
                resultado.total
            );


        cantidadGastos.textContent =
            resultado.cantidad ?? gastos.length;


        periodoConsultado.textContent =
            formatearPeriodo(
                resultado.desde,
                resultado.hasta
            );


        textoResultados.textContent =
            gastos.length === 1
                ? 'Se encontró 1 gasto.'
                : `Se encontraron ${gastos.length} gastos.`;


        limpiarTabla();


        if (
            gastos.length === 0
        ) {

            contenedorTabla.classList.add(
                'oculto'
            );


            sinResultados.classList.remove(
                'oculto'
            );


            return;
        }


        sinResultados.classList.add(
            'oculto'
        );


        gastos.forEach(
            gasto => {

                tablaGastos.appendChild(
                    crearFilaGasto(
                        gasto
                    )
                );
            }
        );


        contenedorTabla.classList.remove(
            'oculto'
        );
    }


    function crearFilaGasto(
        gasto
    ) {

        const fila =
            document.createElement(
                'tr'
            );


        const celdaFecha =
            document.createElement(
                'td'
            );


        celdaFecha.textContent =
            formatearFecha(
                gasto.fecha
            );


        const celdaCategoria =
            document.createElement(
                'td'
            );


        celdaCategoria.textContent =
            gasto.categoria || '-';


        const celdaDescripcion =
            document.createElement(
                'td'
            );


        celdaDescripcion.classList.add(
            'descripcion-gasto'
        );


        celdaDescripcion.textContent =
            gasto.descripcion || '-';


        const celdaMonto =
            document.createElement(
                'td'
            );


        celdaMonto.classList.add(
            'monto-gasto'
        );


        celdaMonto.textContent =
            formatearMoneda(
                gasto.monto
            );


        const celdaMedioPago =
            document.createElement(
                'td'
            );


        celdaMedioPago.textContent =
            gasto.medioPago || 'Sin medio de pago';


        const celdaUsuario =
            document.createElement(
                'td'
            );


        celdaUsuario.textContent =
            gasto.usuario || '-';


        const celdaObservaciones =
            document.createElement(
                'td'
            );


        celdaObservaciones.classList.add(
            'observaciones-gasto'
        );


        celdaObservaciones.textContent =
            gasto.observaciones || '-';


        fila.appendChild(
            celdaFecha
        );


        fila.appendChild(
            celdaCategoria
        );


        fila.appendChild(
            celdaDescripcion
        );


        fila.appendChild(
            celdaMonto
        );


        fila.appendChild(
            celdaMedioPago
        );


        fila.appendChild(
            celdaUsuario
        );


        fila.appendChild(
            celdaObservaciones
        );


        return fila;
    }


    function limpiarFiltros() {

        ocultarMensaje();


        periodo.value =
            'MES';


        desde.value =
            '';


        hasta.value =
            '';


        categoriaGasto.value =
            '';


        medioPago.value =
            '';


        actualizarCamposFecha();

        reiniciarResultados();
    }


    function reiniciarResultados() {

        limpiarTabla();

        reiniciarResumen();


        textoResultados.textContent =
            'Realizá una consulta para ver los gastos.';


        cargando.classList.add(
            'oculto'
        );


        sinResultados.classList.add(
            'oculto'
        );


        contenedorTabla.classList.add(
            'oculto'
        );
    }


    function reiniciarResumen() {

        totalGastos.textContent =
            '$ 0,00';


        cantidadGastos.textContent =
            '0';


        periodoConsultado.textContent =
            'Sin consulta';
    }


    function limpiarTabla() {

        tablaGastos.innerHTML =
            '';
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


            sinResultados.classList.add(
                'oculto'
            );


            contenedorTabla.classList.add(
                'oculto'
            );


            return;
        }


        cargando.classList.add(
            'oculto'
        );
    }


    function bloquearFormulario(
        bloquear
    ) {

        periodo.disabled =
            bloquear;


        categoriaGasto.disabled =
            bloquear;


        medioPago.disabled =
            bloquear;


        btnLimpiar.disabled =
            bloquear;


        btnConsultar.disabled =
            bloquear;


        if (
            bloquear
        ) {

            desde.disabled =
                true;


            hasta.disabled =
                true;


            btnConsultar.textContent =
                'Consultando...';

        } else {

            btnConsultar.textContent =
                'Consultar';


            actualizarCamposFecha();
        }
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


    function formatearPeriodo(
        fechaDesde,
        fechaHasta
    ) {

        if (
            !fechaDesde
            &&
            !fechaHasta
        ) {

            return 'Sin período';
        }


        if (
            fechaDesde === fechaHasta
        ) {

            return formatearFecha(
                fechaDesde
            );
        }


        return `${formatearFecha(fechaDesde)} al ${formatearFecha(fechaHasta)}`;
    }


    function formatearMoneda(
        monto
    ) {

        const valor =
            Number(
                monto ?? 0
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
                style: 'currency',
                currency: 'ARS'
            }
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
            tipo === 'exito'
        ) {

            mensaje.classList.add(
                'mensaje-exito'
            );

        } else if (
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


            const erroresCampos =
                data.errors
                ||
                data.fieldErrors;


            if (
                erroresCampos
            ) {

                const errores =
                    Object.values(
                        erroresCampos
                    );


                if (
                    errores.length > 0
                ) {

                    return errores.join(
                        ' - '
                    );
                }
            }


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
                return 'Los parámetros de la consulta no son válidos.';

            case 401:
                return 'La sesión no es válida o expiró.';

            case 403:
                return 'No tenés permisos para consultar los gastos.';

            case 404:
                return 'No se encontró el recurso solicitado.';

            default:
                return 'Ocurrió un error inesperado al consultar los gastos.';
        }
    }

})();