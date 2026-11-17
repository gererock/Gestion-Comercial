(() => {

    'use strict';


    const API_VENTAS =
        '/api/admin/ventas';


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


    const tablaVentas =
        document.getElementById(
            'tablaVentas'
        );


    const seccionDetalle =
        document.getElementById(
            'seccionDetalle'
        );


    const btnCerrarDetalle =
        document.getElementById(
            'btnCerrarDetalle'
        );


    const cargandoDetalle =
        document.getElementById(
            'cargandoDetalle'
        );


    const contenidoDetalle =
        document.getElementById(
            'contenidoDetalle'
        );


    const detalleNumeroVenta =
        document.getElementById(
            'detalleNumeroVenta'
        );


    const detalleFecha =
        document.getElementById(
            'detalleFecha'
        );


    const detalleCliente =
        document.getElementById(
            'detalleCliente'
        );


    const detalleTipoVenta =
        document.getElementById(
            'detalleTipoVenta'
        );


    const detalleEstadoPedido =
        document.getElementById(
            'detalleEstadoPedido'
        );


    const detalleEstadoPago =
        document.getElementById(
            'detalleEstadoPago'
        );


    const detalleMedioPago =
        document.getElementById(
            'detalleMedioPago'
        );


    const detalleMedioEntrega =
        document.getElementById(
            'detalleMedioEntrega'
        );


    const tablaDetalleProductos =
        document.getElementById(
            'tablaDetalleProductos'
        );


    const detalleSubtotal =
        document.getElementById(
            'detalleSubtotal'
        );


    const detalleDescuentoCliente =
        document.getElementById(
            'detalleDescuentoCliente'
        );


    const detalleDescuentoMayorista =
        document.getElementById(
            'detalleDescuentoMayorista'
        );


    const detalleDescuentoOferta =
        document.getElementById(
            'detalleDescuentoOferta'
        );


    const detalleTotal =
        document.getElementById(
            'detalleTotal'
        );


    /*
     * ELEMENTOS DE LOS FILTROS
     */

    const formFiltros =
        document.getElementById(
            'formFiltros'
        );


    const filtroFecha =
        document.getElementById(
            'filtroFecha'
        );


    const filtroDesde =
        document.getElementById(
            'filtroDesde'
        );


    const filtroHasta =
        document.getElementById(
            'filtroHasta'
        );


    const filtroCliente =
        document.getElementById(
            'filtroCliente'
        );


    const filtroMontoMinimo =
        document.getElementById(
            'filtroMontoMinimo'
        );


    const filtroMontoMaximo =
        document.getElementById(
            'filtroMontoMaximo'
        );


    const filtroTipoVenta =
        document.getElementById(
            'filtroTipoVenta'
        );


    const btnLimpiarFiltros =
        document.getElementById(
            'btnLimpiarFiltros'
        );


    /*
     * VARIABLES DE CONTROL
     */

    let cargandoVentaDetalle =
        false;


    let cargandoHistorial =
        false;


    /*
     * INICIO
     */

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

        await cargarVentas(
            API_VENTAS,
            false
        );
    }


    /*
     * USUARIO
     */

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


    /*
     * EVENTOS
     */

    function registrarEventos() {

        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        btnCerrarDetalle.addEventListener(
            'click',
            cerrarDetalle
        );


        tablaVentas.addEventListener(
            'click',
            manejarAccionTabla
        );


        formFiltros.addEventListener(
            'submit',
            aplicarFiltros
        );


        btnLimpiarFiltros.addEventListener(
            'click',
            limpiarFiltros
        );
    }


    /*
     * APLICAR FILTROS
     */

    async function aplicarFiltros(
        event
    ) {

        event.preventDefault();


        ocultarMensaje();


        const filtros =
            obtenerFiltros();


        const error =
            validarFiltros(
                filtros
            );


        if (
            error
        ) {

            mostrarMensaje(
                error,
                'error'
            );


            return;
        }


        const url =
            construirUrlFiltros(
                filtros
            );


        cerrarDetalle();


        await cargarVentas(
            url,
            hayFiltrosAplicados(
                filtros
            )
        );
    }


    /*
     * OBTENER VALORES DE LOS INPUTS
     */

    function obtenerFiltros() {

        return {

            fecha:
                filtroFecha.value,

            desde:
                filtroDesde.value,

            hasta:
                filtroHasta.value,

            cliente:
                filtroCliente.value,

            montoMinimo:
                filtroMontoMinimo.value,

            montoMaximo:
                filtroMontoMaximo.value,

            tipoVenta:
                filtroTipoVenta.value
        };
    }


    /*
     * VALIDACIONES FRONTEND
     */

    function validarFiltros(
        filtros
    ) {

        if (
            filtros.fecha
            &&
            (
                filtros.desde
                ||
                filtros.hasta
            )
        ) {

            return 'No podés usar una fecha exacta junto con un rango de fechas.';
        }


        if (
            filtros.desde
            &&
            filtros.hasta
            &&
            filtros.desde > filtros.hasta
        ) {

            return 'La fecha desde no puede ser posterior a la fecha hasta.';
        }


        if (
            filtros.cliente
            &&
            filtros.cliente.trim() === ''
        ) {

            return 'El filtro de cliente no puede contener solamente espacios.';
        }


        if (
            filtros.cliente
            &&
            !contieneCaracterValido(
                filtros.cliente
            )
        ) {

            return 'El filtro de cliente contiene un valor inválido.';
        }


        if (
            filtros.montoMinimo !== ''
        ) {

            const montoMinimo =
                Number(
                    filtros.montoMinimo
                );


            if (
                !Number.isFinite(
                    montoMinimo
                )
            ) {

                return 'El monto mínimo no es válido.';
            }


            if (
                montoMinimo < 0
            ) {

                return 'El monto mínimo no puede ser negativo.';
            }
        }


        if (
            filtros.montoMaximo !== ''
        ) {

            const montoMaximo =
                Number(
                    filtros.montoMaximo
                );


            if (
                !Number.isFinite(
                    montoMaximo
                )
            ) {

                return 'El monto máximo no es válido.';
            }


            if (
                montoMaximo < 0
            ) {

                return 'El monto máximo no puede ser negativo.';
            }
        }


        if (
            filtros.montoMinimo !== ''
            &&
            filtros.montoMaximo !== ''
        ) {

            const montoMinimo =
                Number(
                    filtros.montoMinimo
                );


            const montoMaximo =
                Number(
                    filtros.montoMaximo
                );


            if (
                montoMinimo > montoMaximo
            ) {

                return 'El monto mínimo no puede ser mayor al monto máximo.';
            }
        }


        if (
            filtros.tipoVenta
            &&
            filtros.tipoVenta !== 'ONLINE'
            &&
            filtros.tipoVenta !== 'LOCAL'
        ) {

            return 'El tipo de venta seleccionado no es válido.';
        }


        return null;
    }


    /*
     * VALIDAR TEXTO DE CLIENTE
     */

    function contieneCaracterValido(
        texto
    ) {

        return /[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ]/.test(
            texto
        );
    }


    /*
     * CONSTRUIR URL CON QUERY PARAMS
     */

    function construirUrlFiltros(
        filtros
    ) {

        const parametros =
            new URLSearchParams();


        if (
            filtros.fecha
        ) {

            parametros.set(
                'fecha',
                filtros.fecha
            );
        }


        if (
            filtros.desde
        ) {

            parametros.set(
                'desde',
                filtros.desde
            );
        }


        if (
            filtros.hasta
        ) {

            parametros.set(
                'hasta',
                filtros.hasta
            );
        }


        if (
            filtros.cliente.trim()
        ) {

            parametros.set(
                'cliente',
                filtros.cliente.trim()
            );
        }


        if (
            filtros.montoMinimo !== ''
        ) {

            parametros.set(
                'montoMinimo',
                filtros.montoMinimo
            );
        }


        if (
            filtros.montoMaximo !== ''
        ) {

            parametros.set(
                'montoMaximo',
                filtros.montoMaximo
            );
        }


        if (
            filtros.tipoVenta
        ) {

            parametros.set(
                'tipoVenta',
                filtros.tipoVenta
            );
        }


        const query =
            parametros.toString();


        if (
            !query
        ) {

            return API_VENTAS;
        }


        return `${API_VENTAS}?${query}`;
    }


    /*
     * SABER SI HAY FILTROS ACTIVOS
     */

    function hayFiltrosAplicados(
        filtros
    ) {

        return Boolean(

            filtros.fecha

            ||

            filtros.desde

            ||

            filtros.hasta

            ||

            filtros.cliente.trim()

            ||

            filtros.montoMinimo !== ''

            ||

            filtros.montoMaximo !== ''

            ||

            filtros.tipoVenta
        );
    }


    /*
     * LIMPIAR FILTROS
     */

    async function limpiarFiltros() {

        formFiltros.reset();


        ocultarMensaje();


        cerrarDetalle();


        await cargarVentas(
            API_VENTAS,
            false
        );
    }


    /*
     * CARGAR HISTORIAL
     */

    async function cargarVentas(
        url,
        filtrosAplicados
    ) {

        if (
            cargandoHistorial
        ) {

            return;
        }


        cargandoHistorial =
            true;


        mostrarCargando(
            true
        );


        ocultarMensaje();


        try {

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


            const ventas =
                await response.json();


            mostrarVentas(
                ventas,
                filtrosAplicados
            );

        } catch (error) {

            console.error(
                error
            );


            limpiarTablaVentas();


            textoResultados.textContent =
                'No se pudo cargar el historial de ventas.';


            contenedorTabla.classList.add(
                'oculto'
            );


            sinResultados.classList.add(
                'oculto'
            );


            mostrarMensaje(
                error.message ||
                'Ocurrió un error al cargar las ventas.',
                'error'
            );

        } finally {

            cargandoHistorial =
                false;


            mostrarCargando(
                false
            );
        }
    }


    /*
     * MOSTRAR RESULTADOS
     */

    function mostrarVentas(
        ventas,
        filtrosAplicados
    ) {

        limpiarTablaVentas();


        if (
            !Array.isArray(
                ventas
            )
        ) {

            throw new Error(
                'La respuesta del historial de ventas no es válida.'
            );
        }


        if (
            ventas.length === 0
        ) {

            if (
                filtrosAplicados
            ) {

                textoResultados.textContent =
                    'No se encontraron ventas con los filtros aplicados.';


                const titulo =
                    sinResultados.querySelector(
                        'h4'
                    );


                const descripcion =
                    sinResultados.querySelector(
                        'p'
                    );


                if (
                    titulo
                ) {

                    titulo.textContent =
                        'No se encontraron ventas';
                }


                if (
                    descripcion
                ) {

                    descripcion.textContent =
                        'No existen ventas que coincidan con los filtros seleccionados.';
                }

            } else {

                textoResultados.textContent =
                    'No hay ventas registradas.';


                const titulo =
                    sinResultados.querySelector(
                        'h4'
                    );


                const descripcion =
                    sinResultados.querySelector(
                        'p'
                    );


                if (
                    titulo
                ) {

                    titulo.textContent =
                        'No hay ventas registradas';
                }


                if (
                    descripcion
                ) {

                    descripcion.textContent =
                        'Todavía no existen ventas para mostrar en el historial.';
                }
            }


            contenedorTabla.classList.add(
                'oculto'
            );


            sinResultados.classList.remove(
                'oculto'
            );


            return;
        }


        if (
            filtrosAplicados
        ) {

            textoResultados.textContent =
                ventas.length === 1
                    ? 'Se encontró 1 venta con los filtros aplicados.'
                    : `Se encontraron ${ventas.length} ventas con los filtros aplicados.`;

        } else {

            textoResultados.textContent =
                ventas.length === 1
                    ? 'Se encontró 1 venta.'
                    : `Se encontraron ${ventas.length} ventas.`;
        }


        sinResultados.classList.add(
            'oculto'
        );


        ventas.forEach(
            venta => {

                tablaVentas.appendChild(
                    crearFilaVenta(
                        venta
                    )
                );
            }
        );


        contenedorTabla.classList.remove(
            'oculto'
        );
    }


    /*
     * CREAR FILA DEL HISTORIAL
     */

    function crearFilaVenta(
        venta
    ) {

        const fila =
            document.createElement(
                'tr'
            );


        if (
            venta.estadoPedido === 'CANCELADO'
        ) {

            fila.classList.add(
                'fila-cancelada'
            );
        }


        const celdaNumero =
            document.createElement(
                'td'
            );


        celdaNumero.classList.add(
            'numero-venta'
        );


        celdaNumero.textContent =
            venta.numeroPedido || '-';


        const celdaFecha =
            document.createElement(
                'td'
            );


        celdaFecha.textContent =
            formatearFechaHora(
                venta.fecha
            );


        const celdaCliente =
            document.createElement(
                'td'
            );


        celdaCliente.classList.add(
            'cliente-venta'
        );


        celdaCliente.textContent =
            venta.cliente ||
            'Consumidor final';


        const celdaTipo =
            document.createElement(
                'td'
            );


        celdaTipo.appendChild(
            crearBadge(

                formatearTipoVenta(
                    venta.tipoVenta
                ),

                obtenerClaseTipoVenta(
                    venta.tipoVenta
                )
            )
        );


        const celdaEstado =
            document.createElement(
                'td'
            );


        celdaEstado.appendChild(
            crearBadge(

                formatearEstadoPedido(
                    venta.estadoPedido
                ),

                obtenerClaseEstadoPedido(
                    venta.estadoPedido
                )
            )
        );


        const celdaPago =
            document.createElement(
                'td'
            );


        celdaPago.appendChild(
            crearBadge(

                formatearEstadoPago(
                    venta.estadoPago
                ),

                obtenerClaseEstadoPago(
                    venta.estadoPago
                )
            )
        );


        const celdaMedioPago =
            document.createElement(
                'td'
            );


        celdaMedioPago.textContent =
            venta.medioPago ||
            'Sin medio de pago';


        const celdaEntrega =
            document.createElement(
                'td'
            );


        celdaEntrega.textContent =
            formatearMedioEntrega(
                venta.medioEntrega
            );


        const celdaTotal =
            document.createElement(
                'td'
            );


        celdaTotal.classList.add(
            'total-venta'
        );


        celdaTotal.textContent =
            formatearMoneda(
                venta.total
            );


        const celdaAccion =
            document.createElement(
                'td'
            );


        const btnDetalle =
            document.createElement(
                'button'
            );


        btnDetalle.type =
            'button';


        btnDetalle.className =
            'btn btn-detalle';


        btnDetalle.textContent =
            'Ver detalle';


        btnDetalle.dataset.id =
            venta.idPedido;


        btnDetalle.dataset.accion =
            'detalle';


        celdaAccion.appendChild(
            btnDetalle
        );


        fila.appendChild(
            celdaNumero
        );


        fila.appendChild(
            celdaFecha
        );


        fila.appendChild(
            celdaCliente
        );


        fila.appendChild(
            celdaTipo
        );


        fila.appendChild(
            celdaEstado
        );


        fila.appendChild(
            celdaPago
        );


        fila.appendChild(
            celdaMedioPago
        );


        fila.appendChild(
            celdaEntrega
        );


        fila.appendChild(
            celdaTotal
        );


        fila.appendChild(
            celdaAccion
        );


        return fila;
    }


    /*
     * EVENTO VER DETALLE
     */

    function manejarAccionTabla(
        event
    ) {

        const boton =
            event.target.closest(
                'button[data-accion="detalle"]'
            );


        if (
            !boton
        ) {

            return;
        }


        const idPedido =
            Number(
                boton.dataset.id
            );


        if (
            !Number.isInteger(
                idPedido
            )
            ||
            idPedido <= 0
        ) {

            mostrarMensaje(
                'La venta indicada no es válida.',
                'error'
            );


            return;
        }


        cargarDetalle(
            idPedido
        );
    }


    /*
     * CARGAR DETALLE
     */

    async function cargarDetalle(
        idPedido
    ) {

        if (
            cargandoVentaDetalle
        ) {

            return;
        }


        cargandoVentaDetalle =
            true;


        ocultarMensaje();


        mostrarCargandoDetalle(
            true
        );


        seccionDetalle.classList.remove(
            'oculto'
        );


        try {

            const response =
                await Auth.fetchAutenticado(

                    `${API_VENTAS}/${idPedido}`,

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


            const venta =
                await response.json();


            mostrarDetalle(
                venta
            );


            seccionDetalle.scrollIntoView({

                behavior:
                    'smooth',

                block:
                    'start'
            });

        } catch (error) {

            console.error(
                error
            );


            contenidoDetalle.classList.add(
                'oculto'
            );


            mostrarMensaje(
                error.message ||
                'No se pudo cargar el detalle de la venta.',
                'error'
            );

        } finally {

            cargandoVentaDetalle =
                false;


            mostrarCargandoDetalle(
                false
            );
        }
    }


    /*
     * MOSTRAR DETALLE
     */

    function mostrarDetalle(
        venta
    ) {

        detalleNumeroVenta.textContent =
            venta.numeroPedido ||
            'Venta';


        detalleFecha.textContent =
            formatearFechaHora(
                venta.fecha
            );


        detalleCliente.textContent =
            venta.cliente ||
            'Consumidor final';


        detalleTipoVenta.textContent =
            formatearTipoVenta(
                venta.tipoVenta
            );


        detalleEstadoPedido.textContent =
            formatearEstadoPedido(
                venta.estadoPedido
            );


        detalleEstadoPago.textContent =
            formatearEstadoPago(
                venta.estadoPago
            );


        detalleMedioPago.textContent =
            venta.medioPago ||
            'Sin medio de pago';


        detalleMedioEntrega.textContent =
            formatearMedioEntrega(
                venta.medioEntrega
            );


        mostrarProductosDetalle(
            venta.productos
        );


        detalleSubtotal.textContent =
            formatearMoneda(
                venta.subtotal
            );


        detalleDescuentoCliente.textContent =
            formatearMoneda(
                venta.descuentoCliente
            );


        detalleDescuentoMayorista.textContent =
            formatearMoneda(
                venta.descuentoMayorista
            );


        detalleDescuentoOferta.textContent =
            formatearMoneda(
                venta.descuentoOferta
            );


        detalleTotal.textContent =
            formatearMoneda(
                venta.total
            );


        contenidoDetalle.classList.remove(
            'oculto'
        );
    }


    /*
     * PRODUCTOS DEL DETALLE
     */

    function mostrarProductosDetalle(
        productos
    ) {

        tablaDetalleProductos.innerHTML =
            '';


        if (
            !Array.isArray(
                productos
            )
            ||
            productos.length === 0
        ) {

            const fila =
                document.createElement(
                    'tr'
                );


            const celda =
                document.createElement(
                    'td'
                );


            celda.colSpan =
                5;


            celda.textContent =
                'No hay productos registrados en esta venta.';


            fila.appendChild(
                celda
            );


            tablaDetalleProductos.appendChild(
                fila
            );


            return;
        }


        productos.forEach(
            producto => {

                tablaDetalleProductos.appendChild(
                    crearFilaProducto(
                        producto
                    )
                );
            }
        );
    }


    function crearFilaProducto(
        producto
    ) {

        const fila =
            document.createElement(
                'tr'
            );


        const celdaProducto =
            document.createElement(
                'td'
            );


        celdaProducto.textContent =
            producto.producto ||
            '-';


        const celdaCantidad =
            document.createElement(
                'td'
            );


        celdaCantidad.textContent =
            producto.cantidad ?? 0;


        const celdaPrecio =
            document.createElement(
                'td'
            );


        celdaPrecio.textContent =
            formatearMoneda(
                producto.precioUnitario
            );


        const celdaDescuento =
            document.createElement(
                'td'
            );


        celdaDescuento.textContent =
            formatearMoneda(
                producto.descuentoAplicado
            );


        const celdaSubtotal =
            document.createElement(
                'td'
            );


        celdaSubtotal.classList.add(
            'total-venta'
        );


        celdaSubtotal.textContent =
            formatearMoneda(
                producto.subtotal
            );


        fila.appendChild(
            celdaProducto
        );


        fila.appendChild(
            celdaCantidad
        );


        fila.appendChild(
            celdaPrecio
        );


        fila.appendChild(
            celdaDescuento
        );


        fila.appendChild(
            celdaSubtotal
        );


        return fila;
    }


    /*
     * BADGES
     */

    function crearBadge(
        texto,
        clase
    ) {

        const badge =
            document.createElement(
                'span'
            );


        badge.classList.add(
            'badge'
        );


        if (
            clase
        ) {

            badge.classList.add(
                clase
            );
        }


        badge.textContent =
            texto;


        return badge;
    }


    function obtenerClaseTipoVenta(
        tipoVenta
    ) {

        if (
            tipoVenta === 'ONLINE'
        ) {

            return 'badge-online';
        }


        if (
            tipoVenta === 'LOCAL'
        ) {

            return 'badge-local';
        }


        return 'badge-estado';
    }


    function obtenerClaseEstadoPago(
        estadoPago
    ) {

        if (
            estadoPago === 'PAGADO'
        ) {

            return 'badge-pagado';
        }


        if (
            estadoPago === 'PENDIENTE'
        ) {

            return 'badge-pendiente';
        }


        return 'badge-estado';
    }


    function obtenerClaseEstadoPedido(
        estadoPedido
    ) {

        if (
            estadoPedido === 'CANCELADO'
        ) {

            return 'badge-cancelado';
        }


        if (
            estadoPedido === 'ENTREGADO'
        ) {

            return 'badge-entregado';
        }


        return 'badge-estado';
    }


    /*
     * FORMATEADORES
     */

    function formatearTipoVenta(
        tipoVenta
    ) {

        if (
            tipoVenta === 'ONLINE'
        ) {

            return 'Online';
        }


        if (
            tipoVenta === 'LOCAL'
        ) {

            return 'Local';
        }


        return tipoVenta || '-';
    }


    function formatearEstadoPago(
        estadoPago
    ) {

        if (
            estadoPago === 'PAGADO'
        ) {

            return 'Pagado';
        }


        if (
            estadoPago === 'PENDIENTE'
        ) {

            return 'Pendiente';
        }


        return formatearTextoEnum(
            estadoPago
        );
    }


    function formatearEstadoPedido(
        estadoPedido
    ) {

        switch (
        estadoPedido
        ) {

            case 'PENDIENTE_APROBACION':
                return 'Pendiente de aprobación';

            case 'CONFIRMADO':
                return 'Confirmado';

            case 'EN_PREPARACION':
                return 'En preparación';

            case 'LISTO_PARA_RETIRAR':
                return 'Listo para retirar';

            case 'LISTO_PARA_ENVIAR':
                return 'Listo para enviar';

            case 'ENVIADO':
                return 'Enviado';

            case 'ENTREGADO':
                return 'Entregado';

            case 'CANCELADO':
                return 'Cancelado';

            default:
                return formatearTextoEnum(
                    estadoPedido
                );
        }
    }


    function formatearMedioEntrega(
        medioEntrega
    ) {

        if (
            medioEntrega === 'RETIRO_LOCAL'
        ) {

            return 'Retiro en local';
        }


        if (
            medioEntrega === 'ENVIO'
        ) {

            return 'Envío';
        }


        return formatearTextoEnum(
            medioEntrega
        );
    }


    function formatearTextoEnum(
        valor
    ) {

        if (
            !valor
        ) {

            return '-';
        }


        return valor
            .toLowerCase()
            .replaceAll(
                '_',
                ' '
            )
            .replace(
                /^./,
                letra =>
                    letra.toUpperCase()
            );
    }


    function formatearFechaHora(
        fecha
    ) {

        if (
            !fecha
        ) {

            return '-';
        }


        const partes =
            fecha.split(
                'T'
            );


        const fechaParte =
            partes[0];


        const horaParte =
            partes.length > 1
                ? partes[1]
                : null;


        const fechaSeparada =
            fechaParte.split(
                '-'
            );


        if (
            fechaSeparada.length !== 3
        ) {

            return fecha;
        }


        const fechaFormateada =
            `${fechaSeparada[2]}/${fechaSeparada[1]}/${fechaSeparada[0]}`;


        if (
            !horaParte
        ) {

            return fechaFormateada;
        }


        const hora =
            horaParte.substring(
                0,
                5
            );


        return `${fechaFormateada} ${hora}`;
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
                style:
                    'currency',

                currency:
                    'ARS'
            }
        );
    }


    /*
     * SPINNERS
     */

    function mostrarCargando(
        mostrar
    ) {

        if (
            mostrar
        ) {

            cargando.classList.remove(
                'oculto'
            );


            contenedorTabla.classList.add(
                'oculto'
            );


            sinResultados.classList.add(
                'oculto'
            );


            return;
        }


        cargando.classList.add(
            'oculto'
        );
    }


    function mostrarCargandoDetalle(
        mostrar
    ) {

        if (
            mostrar
        ) {

            cargandoDetalle.classList.remove(
                'oculto'
            );


            contenidoDetalle.classList.add(
                'oculto'
            );


            return;
        }


        cargandoDetalle.classList.add(
            'oculto'
        );
    }


    /*
     * CERRAR DETALLE
     */

    function cerrarDetalle() {

        seccionDetalle.classList.add(
            'oculto'
        );


        contenidoDetalle.classList.add(
            'oculto'
        );


        tablaDetalleProductos.innerHTML =
            '';
    }


    /*
     * LIMPIAR TABLA
     */

    function limpiarTablaVentas() {

        tablaVentas.innerHTML =
            '';
    }


    /*
     * MENSAJES
     */

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


    /*
     * ERRORES DEL BACKEND
     */

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

                return 'Los filtros enviados no son válidos.';


            case 401:

                return 'La sesión no es válida o expiró.';


            case 403:

                return 'No tenés permisos para consultar el historial de ventas.';


            case 404:

                return 'La venta indicada no existe.';


            default:

                return 'Ocurrió un error inesperado al consultar las ventas.';
        }
    }

})();