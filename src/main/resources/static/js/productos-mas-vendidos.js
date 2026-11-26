(() => {

    'use strict';


    const API_PRODUCTOS_MAS_VENDIDOS =
        '/api/admin/productos-mas-vendidos';


    const API_PRODUCTOS =
        '/api/productos';


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


    const cantidadProductos =
        document.getElementById(
            'cantidadProductos'
        );


    const cantidadUnidades =
        document.getElementById(
            'cantidadUnidades'
        );


    const sinResultados =
        document.getElementById(
            'sinResultados'
        );


    const contenedorTabla =
        document.getElementById(
            'contenedorTabla'
        );


    const tablaProductos =
        document.getElementById(
            'tablaProductos'
        );


    const modalProducto =
        document.getElementById(
            'modalProducto'
        );


    const btnCerrarModal =
        document.getElementById(
            'btnCerrarModal'
        );


    const btnCerrarModalFooter =
        document.getElementById(
            'btnCerrarModalFooter'
        );


    const cargandoDetalle =
        document.getElementById(
            'cargandoDetalle'
        );


    const contenidoDetalle =
        document.getElementById(
            'contenidoDetalle'
        );


    const tituloModalProducto =
        document.getElementById(
            'tituloModalProducto'
        );


    const detalleId =
        document.getElementById(
            'detalleId'
        );


    const detalleNombre =
        document.getElementById(
            'detalleNombre'
        );


    const detalleCategoria =
        document.getElementById(
            'detalleCategoria'
        );


    const detalleMarca =
        document.getElementById(
            'detalleMarca'
        );


    const detallePrecio =
        document.getElementById(
            'detallePrecio'
        );


    const detalleStockActual =
        document.getElementById(
            'detalleStockActual'
        );


    const detalleStockMinimo =
        document.getElementById(
            'detalleStockMinimo'
        );


    const detalleEstado =
        document.getElementById(
            'detalleEstado'
        );


    let periodoActual =
        'DIA';


    let consultando =
        false;


    let consultandoDetalle =
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
            periodoActual
        );


        await consultarProductos();
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


        fechaDesde.addEventListener(
            'input',
            manejarEdicionRango
        );


        fechaHasta.addEventListener(
            'input',
            manejarEdicionRango
        );


        btnLimpiarFiltros.addEventListener(
            'click',
            limpiarFiltros
        );


        tablaProductos.addEventListener(
            'click',
            manejarAccionTabla
        );


        btnCerrarModal.addEventListener(
            'click',
            cerrarModal
        );


        btnCerrarModalFooter.addEventListener(
            'click',
            cerrarModal
        );


        modalProducto.addEventListener(
            'click',
            event => {

                if (
                    event.target
                    ===
                    modalProducto
                ) {

                    cerrarModal();
                }
            }
        );


        document.addEventListener(
            'keydown',
            event => {

                if (
                    event.key === 'Escape'
                    &&
                    !modalProducto.classList.contains(
                        'oculto'
                    )
                ) {

                    cerrarModal();
                }
            }
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


        await consultarProductos();
    }


    function manejarEdicionRango() {

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


        await consultarProductos(
            desde,
            hasta
        );
    }


    async function limpiarFiltros() {

        if (
            consultando
        ) {

            return;
        }


        periodoActual =
            'DIA';


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


        await consultarProductos();
    }


    async function consultarProductos(
        desde = null,
        hasta = null
    ) {

        if (
            consultando
        ) {

            return;
        }


        if (
            periodoActual === 'RANGO'
        ) {

            if (
                !desde
                ||
                !hasta
            ) {

                mostrarMensaje(
                    'Debés seleccionar ambas fechas para consultar un rango.',
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
        }


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

            const parametros =
                new URLSearchParams();


            parametros.set(
                'periodo',
                periodoActual
            );


            if (
                periodoActual === 'RANGO'
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
                `${API_PRODUCTOS_MAS_VENDIDOS}?${parametros.toString()}`;


            const response =
                await Auth.fetchAutenticado(

                    url,

                    {
                        method:
                            'GET',
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


            const productos =
                await response.json();


            validarRespuesta(
                productos
            );


            mostrarProductos(
                productos,
                desde,
                hasta
            );

        } catch (error) {

            console.error(
                error
            );


            ocultarResultados();


            mostrarMensaje(

                error.message
                ||
                'No se pudieron consultar los productos más vendidos.',

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
        productos
    ) {

        if (
            !Array.isArray(
                productos
            )
        ) {

            throw new Error(
                'La respuesta recibida no es válida.'
            );
        }


        productos.forEach(
            producto => {

                if (
                    producto.idProducto == null
                    ||
                    !producto.nombre
                ) {

                    throw new Error(
                        'Se recibió un producto con datos incompletos.'
                    );
                }


                const cantidad =
                    Number(
                        producto.cantidadVendida
                    );


                const total =
                    Number(
                        producto.totalVendido
                        ??
                        0
                    );


                if (
                    !Number.isFinite(
                        cantidad
                    )
                    ||
                    cantidad < 0
                ) {

                    throw new Error(
                        'Se recibió una cantidad vendida inválida.'
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
                        'Se recibió un total vendido inválido.'
                    );
                }
            }
        );
    }


    function mostrarProductos(
        productos,
        desde,
        hasta
    ) {

        tablaProductos.replaceChildren();


        const totalUnidades =
            productos.reduce(
                (
                    acumulado,
                    producto
                ) => {

                    return acumulado
                        +
                        Number(
                            producto.cantidadVendida
                            ??
                            0
                        );
                },
                0
            );


        cantidadProductos.textContent =
            productos.length.toLocaleString(
                'es-AR'
            );


        cantidadUnidades.textContent =
            totalUnidades.toLocaleString(
                'es-AR'
            );


        periodoConsultado.textContent =
            formatearPeriodo(
                periodoActual
            );


        rangoConsultado.textContent =
            obtenerTextoRango(
                desde,
                hasta
            );


        if (
            productos.length === 0
        ) {

            sinResultados.classList.remove(
                'oculto'
            );


            contenedorTabla.classList.add(
                'oculto'
            );


            contenidoReporte.classList.remove(
                'oculto'
            );


            return;
        }


        productos.forEach(
            (
                producto,
                indice
            ) => {

                tablaProductos.appendChild(
                    crearFilaProducto(
                        producto,
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


    function crearFilaProducto(
        producto,
        posicion
    ) {

        const fila =
            document.createElement(
                'tr'
            );


        fila.appendChild(
            crearCelda(
                'Posición',
                posicion,
                'posicion'
            )
        );


        fila.appendChild(
            crearCelda(
                'ID',
                producto.idProducto
            )
        );


        fila.appendChild(
            crearCelda(
                'Producto',
                producto.nombre,
                'producto-nombre'
            )
        );


        fila.appendChild(
            crearCelda(
                'Categoría',
                producto.categoria
                ||
                'Sin categoría'
            )
        );


        fila.appendChild(
            crearCelda(
                'Marca',
                producto.marca
                ||
                'Sin marca'
            )
        );


        fila.appendChild(
            crearCelda(
                'Cantidad vendida',
                Number(
                    producto.cantidadVendida
                    ??
                    0
                ).toLocaleString(
                    'es-AR'
                ),
                'cantidad-vendida'
            )
        );


        fila.appendChild(
            crearCelda(
                'Total vendido',
                formatearMoneda(
                    producto.totalVendido
                ),
                'total-vendido'
            )
        );


        const celdaAccion =
            document.createElement(
                'td'
            );


        celdaAccion.dataset.label =
            'Información';


        const boton =
            document.createElement(
                'button'
            );


        boton.type =
            'button';


        boton.className =
            'btn-ver';


        boton.dataset.accion =
            'ver-producto';


        boton.dataset.idProducto =
            producto.idProducto;

        boton.dataset.categoria = producto.categoria ?? '';
        boton.dataset.marca = producto.marca ?? '';


        boton.textContent =
            'Ver información';


        celdaAccion.appendChild(
            boton
        );


        fila.appendChild(
            celdaAccion
        );


        return fila;
    }


    function crearCelda(
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
            clase === 'posicion'
        ) {

            const contenido =
                document.createElement(
                    'span'
                );


            contenido.className =
                clase;


            contenido.textContent =
                valor;


            celda.appendChild(
                contenido
            );


            return celda;
        }


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


    async function manejarAccionTabla(
        event
    ) {

        const boton =
            event.target.closest(
                '[data-accion="ver-producto"]'
            );


        if (
            !boton
        ) {

            return;
        }


        const idProducto =
            Number(
                boton.dataset.idProducto
            );


        if (
            !Number.isInteger(
                idProducto
            )
            ||
            idProducto <= 0
        ) {

            mostrarMensaje(
                'El ID del producto no es válido.',
                'error'
            );


            return;
        }


        await abrirDetalleProducto(
            idProducto,
            boton.dataset.categoria,
            boton.dataset.marca
        );
    }


    async function abrirDetalleProducto(
        idProducto,
        categoria,
        marca
    ) {

        if (
            consultandoDetalle
        ) {

            return;
        }


        consultandoDetalle =
            true;


        limpiarDetalle();


        modalProducto.classList.remove(
            'oculto'
        );


        document.body.classList.add(
            'modal-abierto'
        );


        cargandoDetalle.classList.remove(
            'oculto'
        );


        contenidoDetalle.classList.add(
            'oculto'
        );


        try {

            const response =
                await Auth.fetchAutenticado(

                    `${API_PRODUCTOS}/${idProducto}`,

                    {
                        method:
                            'GET',
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


            const producto =
                await response.json();


            mostrarDetalleProducto(
                producto,
                categoria,
                marca
            );

        } catch (error) {

            console.error(
                error
            );


            cerrarModal();


            mostrarMensaje(

                error.message
                ||
                'No se pudo consultar la información del producto.',

                'error'
            );

        } finally {

            consultandoDetalle =
                false;


            cargandoDetalle.classList.add(
                'oculto'
            );
        }
    }


    function mostrarDetalleProducto(
        producto,
        categoria,
        marca
    ) {

        const id =
            producto.idProducto
            ??
            producto.id
            ??
            '-';


        const nombre =
            producto.nombre
            ||
            'Sin nombre';


        tituloModalProducto.textContent =
            nombre;


        detalleId.textContent =
            id;


        detalleNombre.textContent =
            nombre;


        detalleCategoria.textContent =
            obtenerNombreRelacion(
                producto.categoria ?? (categoria || null),
                'Sin categoría'
            );


        detalleMarca.textContent =
            obtenerNombreRelacion(
                producto.marca ?? (marca || null),
                'Sin marca'
            );


        detallePrecio.textContent =
            producto.precioVenta != null

                ? formatearMoneda(
                    producto.precioVenta
                )

                : 'No informado';


        detalleStockActual.textContent =
            producto.stockActual != null

                ? Number(
                    producto.stockActual
                ).toLocaleString(
                    'es-AR'
                )

                : 'No informado';


        detalleStockMinimo.textContent =
            producto.stockMinimo != null

                ? Number(
                    producto.stockMinimo
                ).toLocaleString(
                    'es-AR'
                )

                : 'No informado';


        detalleEstado.textContent =
            formatearEstado(
                producto.estado
            );


        contenidoDetalle.classList.remove(
            'oculto'
        );
    }


    function obtenerNombreRelacion(
        valor,
        valorVacio
    ) {

        if (
            valor == null
        ) {

            return valorVacio;
        }


        if (
            typeof valor === 'string'
        ) {

            return valor;
        }


        if (
            typeof valor === 'object'
            &&
            valor.nombre
        ) {

            return valor.nombre;
        }


        return valorVacio;
    }


    function limpiarDetalle() {

        tituloModalProducto.textContent =
            'Detalle';


        detalleId.textContent =
            '-';


        detalleNombre.textContent =
            '-';


        detalleCategoria.textContent =
            '-';


        detalleMarca.textContent =
            '-';


        detallePrecio.textContent =
            '-';


        detalleStockActual.textContent =
            '-';


        detalleStockMinimo.textContent =
            '-';


        detalleEstado.textContent =
            '-';
    }


    function cerrarModal() {

        modalProducto.classList.add(
            'oculto'
        );


        document.body.classList.remove(
            'modal-abierto'
        );


        contenidoDetalle.classList.add(
            'oculto'
        );


        cargandoDetalle.classList.add(
            'oculto'
        );
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


    function obtenerTextoRango(
        desde,
        hasta
    ) {

        if (
            periodoActual === 'RANGO'
        ) {

            if (
                desde
                &&
                hasta
            ) {

                return `${formatearFecha(desde)} al ${formatearFecha(hasta)}`;
            }


            return '-';
        }


        if (
            periodoActual === 'DIA'
        ) {

            return 'Día actual';
        }


        if (
            periodoActual === 'SEMANA'
        ) {

            return 'Semana actual';
        }


        if (
            periodoActual === 'MES'
        ) {

            return 'Mes actual';
        }


        return '-';
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


    function formatearEstado(
        estado
    ) {

        if (
            !estado
        ) {

            return 'No informado';
        }


        switch (
        estado
        ) {

            case 'ACTIVO':

                return 'Activo';


            case 'PAUSADO':

                return 'Pausado';


            case 'DISCONTINUADO':

                return 'Discontinuado';


            default:

                return estado;
        }
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


        tablaProductos.replaceChildren();
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


        btnLimpiarFiltros.disabled =
            bloquear;


        const botonConsultarRango =
            formRango.querySelector(
                'button[type="submit"]'
            );


        if (
            botonConsultarRango
        ) {

            botonConsultarRango.disabled =
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
                data.error
                &&
                typeof data.error === 'string'
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

                return 'Los datos ingresados para la consulta no son válidos.';


            case 401:

                return 'La sesión expiró o no es válida.';


            case 403:

                return 'No tenés permisos para consultar este reporte.';


            case 404:

                return 'No se encontró la información solicitada.';


            case 500:

                return 'Ocurrió un error interno al procesar la consulta.';


            default:

                return 'No se pudo completar la consulta.';
        }
    }

})();