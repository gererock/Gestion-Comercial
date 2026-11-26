(() => {

    'use strict';


    const API_CLIENTES_FRECUENTES =
        '/api/admin/clientes-frecuentes';


    if (
        !Auth.requerirRol(
            'ADMINISTRADOR'
        )
    ) {

        return;
    }


    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );


    const btnCerrarSesion =
        document.getElementById(
            'btnCerrarSesion'
        );


    const formFiltros =
        document.getElementById(
            'formFiltros'
        );


    const buscar =
        document.getElementById(
            'buscar'
        );


    const orden =
        document.getElementById(
            'orden'
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


    const btnConsultarRango =
        document.getElementById(
            'btnConsultarRango'
        );


    const btnBuscar =
        document.getElementById(
            'btnBuscar'
        );


    const btnLimpiarFiltros =
        document.getElementById(
            'btnLimpiarFiltros'
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


    const periodoConsultado =
        document.getElementById(
            'periodoConsultado'
        );


    const rangoConsultado =
        document.getElementById(
            'rangoConsultado'
        );


    const cantidadClientes =
        document.getElementById(
            'cantidadClientes'
        );


    const cantidadCompras =
        document.getElementById(
            'cantidadCompras'
        );


    const totalComprado =
        document.getElementById(
            'totalComprado'
        );


    const descripcionOrden =
        document.getElementById(
            'descripcionOrden'
        );


    const sinResultados =
        document.getElementById(
            'sinResultados'
        );


    const textoSinResultados =
        document.getElementById(
            'textoSinResultados'
        );


    const contenedorTabla =
        document.getElementById(
            'contenedorTabla'
        );


    const tablaClientes =
        document.getElementById(
            'tablaClientes'
        );


    let periodoActual =
        'DIA';


    let consultando =
        false;


    let numeroConsulta =
        0;


    iniciar();


    async function iniciar() {

        mostrarUsuario();

        registrarEventos();

        marcarPeriodoActivo(
            periodoActual
        );


        await consultarClientes();
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


        formFiltros.addEventListener(
            'submit',
            manejarBusqueda
        );


        orden.addEventListener(
            'change',
            manejarCambioOrden
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


        fechaDesde.addEventListener(
            'input',
            manejarCambioFechas
        );


        fechaHasta.addEventListener(
            'input',
            manejarCambioFechas
        );


        btnLimpiarFiltros.addEventListener(
            'click',
            limpiarFiltros
        );
    }


    async function manejarBusqueda(
        event
    ) {

        event.preventDefault();


        if (
            consultando
        ) {

            return;
        }


        ocultarMensaje();


        const errorBusqueda =
            validarBusqueda();


        if (
            errorBusqueda
        ) {

            mostrarMensaje(
                errorBusqueda,
                'error'
            );


            return;
        }


        if (
            periodoActual === 'RANGO'
        ) {

            const errorRango =
                validarRango();


            if (
                errorRango
            ) {

                mostrarMensaje(
                    errorRango,
                    'error'
                );


                return;
            }
        }


        await consultarClientes();
    }


    async function manejarCambioOrden() {

        if (
            consultando
        ) {

            return;
        }


        ocultarMensaje();


        if (
            periodoActual === 'RANGO'
            &&
            (
                !fechaDesde.value
                ||
                !fechaHasta.value
            )
        ) {

            ocultarResultados();

            return;
        }


        await consultarClientes();
    }


    async function manejarCambioPeriodo(
        event
    ) {

        if (
            consultando
        ) {

            return;
        }


        periodoActual =
            event.currentTarget
                .dataset
                .periodo;


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


        await consultarClientes();
    }


    function manejarCambioFechas() {

        if (
            periodoActual === 'RANGO'
        ) {

            ocultarResultados();

            ocultarMensaje();
        }
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


        const errorRango =
            validarRango();


        if (
            errorRango
        ) {

            mostrarMensaje(
                errorRango,
                'error'
            );


            return;
        }


        const errorBusqueda =
            validarBusqueda();


        if (
            errorBusqueda
        ) {

            mostrarMensaje(
                errorBusqueda,
                'error'
            );


            return;
        }


        await consultarClientes();
    }


    async function limpiarFiltros() {

        if (
            consultando
        ) {

            return;
        }


        periodoActual =
            'DIA';


        buscar.value =
            '';


        orden.value =
            'CANTIDAD_COMPRAS';


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


        ocultarMensaje();


        await consultarClientes();
    }


    function validarBusqueda() {

        const valor =
            buscar.value;


        if (
            valor === ''
        ) {

            return null;
        }


        if (
            valor.trim() === ''
        ) {

            return 'La búsqueda no puede contener solamente espacios.';
        }


        const tieneCaracterUtil =
            /[\p{L}\p{N}]/u.test(
                valor
            );


        if (
            !tieneCaracterUtil
        ) {

            return 'La búsqueda debe contener letras o números.';
        }


        return null;
    }


    function validarRango() {

        const desde =
            fechaDesde.value;


        const hasta =
            fechaHasta.value;


        if (
            !desde
            ||
            !hasta
        ) {

            return 'Debés seleccionar la fecha desde y la fecha hasta.';
        }


        if (
            desde > hasta
        ) {

            return 'La fecha desde no puede ser posterior a la fecha hasta.';
        }


        return null;
    }


    async function consultarClientes() {

        if (
            consultando
        ) {

            return;
        }


        const consultaActual =
            ++numeroConsulta;


        consultando =
            true;


        ocultarMensaje();

        ocultarResultados();

        mostrarCargando(
            true
        );

        bloquearControles(
            true
        );


        try {

            const url =
                construirUrl();


            const response =
                await Auth.fetchAutenticado(

                    url,

                    {
                        method:
                            'GET',

                        avisarSesionExpirada:
                            true
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


            const clientes =
                await response.json();


            if (
                consultaActual
                !==
                numeroConsulta
            ) {

                return;
            }


            validarRespuesta(
                clientes
            );


            mostrarClientes(
                clientes
            );

        } catch (error) {

            if (
                consultaActual
                !==
                numeroConsulta
            ) {

                return;
            }


            console.error(
                error
            );


            ocultarResultados();


            mostrarMensaje(

                error.message
                ||
                'No se pudieron consultar los clientes frecuentes.',

                'error'
            );

        } finally {

            if (
                consultaActual
                ===
                numeroConsulta
            ) {

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
    }


    function construirUrl() {

        const parametros =
            new URLSearchParams();


        parametros.set(
            'periodo',
            periodoActual
        );


        parametros.set(
            'orden',
            orden.value
        );


        const textoBusqueda =
            buscar.value;


        if (
            textoBusqueda !== ''
        ) {

            parametros.set(
                'buscar',
                textoBusqueda
            );
        }


        if (
            periodoActual === 'RANGO'
        ) {

            parametros.set(
                'desde',
                fechaDesde.value
            );


            parametros.set(
                'hasta',
                fechaHasta.value
            );
        }


        return (
            `${API_CLIENTES_FRECUENTES}?${parametros.toString()}`
        );
    }


    function validarRespuesta(
        clientes
    ) {

        if (
            !Array.isArray(
                clientes
            )
        ) {

            throw new Error(
                'La respuesta recibida no es válida.'
            );
        }


        clientes.forEach(
            cliente => {

                if (
                    cliente.idCliente == null
                ) {

                    throw new Error(
                        'Se recibió un cliente sin identificador.'
                    );
                }


                const compras =
                    Number(
                        cliente.cantidadCompras
                    );


                const total =
                    Number(
                        cliente.totalComprado
                        ??
                        0
                    );


                if (
                    !Number.isFinite(
                        compras
                    )
                    ||
                    compras < 0
                ) {

                    throw new Error(
                        'Se recibió una cantidad de compras inválida.'
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
                        'Se recibió un total comprado inválido.'
                    );
                }
            }
        );
    }


    function mostrarClientes(
        clientes
    ) {

        tablaClientes.replaceChildren();


        actualizarResumen(
            clientes
        );


        actualizarDescripcionOrden();


        if (
            clientes.length === 0
        ) {

            mostrarSinResultados();


            contenidoReporte.classList.remove(
                'oculto'
            );


            return;
        }


        clientes.forEach(
            (
                cliente,
                indice
            ) => {

                tablaClientes.appendChild(
                    crearFilaCliente(
                        cliente,
                        indice + 1
                    )
                );
            }
        );


        sinResultados.classList.add(
            'oculto'
        );


        contenedorTabla.classList.remove(
            'oculto'
        );


        contenidoReporte.classList.remove(
            'oculto'
        );
    }


    function actualizarResumen(
        clientes
    ) {

        const compras =
            clientes.reduce(
                (
                    acumulado,
                    cliente
                ) => {

                    return acumulado
                        +
                        Number(
                            cliente.cantidadCompras
                            ??
                            0
                        );
                },
                0
            );


        const total =
            clientes.reduce(
                (
                    acumulado,
                    cliente
                ) => {

                    return acumulado
                        +
                        Number(
                            cliente.totalComprado
                            ??
                            0
                        );
                },
                0
            );


        cantidadClientes.textContent =
            clientes.length.toLocaleString(
                'es-AR'
            );


        cantidadCompras.textContent =
            compras.toLocaleString(
                'es-AR'
            );


        totalComprado.textContent =
            formatearMoneda(
                total
            );


        periodoConsultado.textContent =
            formatearPeriodo(
                periodoActual
            );


        rangoConsultado.textContent =
            obtenerTextoRango();
    }


    function mostrarSinResultados() {

        contenedorTabla.classList.add(
            'oculto'
        );


        sinResultados.classList.remove(
            'oculto'
        );


        if (
            buscar.value.trim() !== ''
        ) {

            textoSinResultados.textContent =
                'No se encontraron clientes frecuentes que coincidan con la búsqueda y el período seleccionados.';


            return;
        }


        textoSinResultados.textContent =
            'No existen clientes con compras válidas en el período seleccionado.';
    }


    function crearFilaCliente(
        cliente,
        posicion
    ) {

        const fila =
            document.createElement(
                'tr'
            );


        fila.appendChild(
            crearCeldaPosicion(
                posicion
            )
        );


        fila.appendChild(
            crearCeldaTexto(
                'ID',
                cliente.idCliente
            )
        );


        fila.appendChild(
            crearCeldaTexto(
                'Nombre',
                cliente.nombre
                ||
                'No informado',
                'nombre-cliente'
            )
        );


        fila.appendChild(
            crearCeldaTexto(
                'Apellido',
                cliente.apellido
                ||
                'No informado'
            )
        );


        fila.appendChild(
            crearCeldaTexto(
                'Razón social',
                cliente.razonSocial
                ||
                'No informada'
            )
        );


        fila.appendChild(
            crearCeldaContacto(
                cliente
            )
        );


        fila.appendChild(
            crearCeldaTexto(
                'Compras',
                Number(
                    cliente.cantidadCompras
                    ??
                    0
                ).toLocaleString(
                    'es-AR'
                ),
                'cantidad-compras'
            )
        );


        fila.appendChild(
            crearCeldaTexto(
                'Total comprado',
                formatearMoneda(
                    cliente.totalComprado
                ),
                'total-comprado'
            )
        );


        fila.appendChild(
            crearCeldaAcciones(
                cliente
            )
        );


        return fila;
    }


    function crearCeldaPosicion(
        posicion
    ) {

        const celda =
            document.createElement(
                'td'
            );


        celda.dataset.label =
            'Posición';


        const contenido =
            document.createElement(
                'span'
            );


        contenido.className =
            'posicion';


        contenido.textContent =
            posicion;


        celda.appendChild(
            contenido
        );


        return celda;
    }


    function crearCeldaTexto(
        etiqueta,
        valor,
        clase = null
    ) {

        const celda =
            document.createElement(
                'td'
            );


        celda.dataset.label =
            etiqueta;


        if (
            clase
        ) {

            celda.classList.add(
                clase
            );
        }


        celda.textContent =
            valor;


        return celda;
    }


    function crearCeldaContacto(
        cliente
    ) {

        const celda =
            document.createElement(
                'td'
            );


        celda.dataset.label =
            'Contacto';


        const contenedor =
            document.createElement(
                'div'
            );


        contenedor.className =
            'contacto';


        const telefono =
            document.createElement(
                'span'
            );


        telefono.textContent =
            cliente.telefono
                ||
                'Teléfono no informado';


        const correo =
            document.createElement(
                'span'
            );


        correo.className =
            'texto-secundario';


        correo.textContent =
            cliente.correo
                ||
                'Correo no informado';


        contenedor.appendChild(
            telefono
        );


        contenedor.appendChild(
            correo
        );


        celda.appendChild(
            contenedor
        );


        return celda;
    }


    function crearCeldaAcciones(
        cliente
    ) {

        const celda =
            document.createElement(
                'td'
            );


        celda.dataset.label =
            'Información';


        const acciones =
            document.createElement(
                'div'
            );


        acciones.className =
            'acciones-tabla';


        const detalle =
            document.createElement(
                'a'
            );


        detalle.className =
            'btn-accion';


        detalle.href =
            `/vendedor/cliente-detalle.html?id=${cliente.idCliente}`;


        detalle.textContent =
            'Ver detalle';


        const historial =
            document.createElement(
                'a'
            );


        historial.className =
            'btn-accion btn-historial';


        historial.href =
            `/vendedor/cliente-historial.html?idCliente=${cliente.idCliente}`;


        historial.textContent =
            'Historial';


        acciones.appendChild(
            detalle
        );


        acciones.appendChild(
            historial
        );


        celda.appendChild(
            acciones
        );


        return celda;
    }


    function actualizarDescripcionOrden() {

        if (
            orden.value
            ===
            'TOTAL_COMPRADO'
        ) {

            descripcionOrden.textContent =
                'Ordenados de mayor a menor monto total comprado.';


            return;
        }


        descripcionOrden.textContent =
            'Ordenados de mayor a menor cantidad de compras.';
    }


    function marcarPeriodoActivo(
        periodo
    ) {

        botonesPeriodo.forEach(
            boton => {

                const activo =
                    boton.dataset.periodo
                    ===
                    periodo;


                boton.classList.toggle(
                    'activo',
                    activo
                );


                boton.setAttribute(
                    'aria-pressed',
                    activo
                        ? 'true'
                        : 'false'
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


    function obtenerTextoRango() {

        switch (
            periodoActual
        ) {

            case 'DIA':

                return 'Día actual';


            case 'SEMANA':

                return 'Semana actual';


            case 'MES':

                return 'Mes actual';


            case 'RANGO':

                if (
                    fechaDesde.value
                    &&
                    fechaHasta.value
                ) {

                    return (
                        `${formatearFecha(fechaDesde.value)} al ${formatearFecha(fechaHasta.value)}`
                    );
                }


                return '-';


            default:

                return '-';
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


        return (
            `${partes[2]}/${partes[1]}/${partes[0]}`
        );
    }


    function formatearMoneda(
        valor
    ) {

        const monto =
            Number(
                valor
                ??
                0
            );


        if (
            !Number.isFinite(
                monto
            )
        ) {

            return '$ 0,00';
        }


        return monto.toLocaleString(

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

        contenidoReporte.classList.add(
            'oculto'
        );


        sinResultados.classList.add(
            'oculto'
        );


        contenedorTabla.classList.add(
            'oculto'
        );


        tablaClientes.replaceChildren();
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

        buscar.disabled =
            bloquear;


        orden.disabled =
            bloquear;


        btnBuscar.disabled =
            bloquear;


        fechaDesde.disabled =
            bloquear;


        fechaHasta.disabled =
            bloquear;


        btnConsultarRango.disabled =
            bloquear;


        btnLimpiarFiltros.disabled =
            bloquear;


        botonesPeriodo.forEach(
            boton => {

                boton.disabled =
                    bloquear;
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

        mensaje.textContent =
            '';


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
                data.mensaje
            ) {

                return data.mensaje;
            }


            if (
                typeof data.error
                ===
                'string'
            ) {

                return data.error;
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

                return 'Los filtros ingresados no son válidos.';


            case 401:

                return 'La sesión expiró o no es válida.';


            case 403:

                return 'No tenés permisos para consultar clientes frecuentes.';


            case 404:

                return 'No se encontró la información solicitada.';


            case 500:

                return 'Ocurrió un error interno al consultar los clientes frecuentes.';


            default:

                return 'No se pudo completar la consulta.';
        }
    }

})();