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


    let cargandoVentaDetalle =
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

        await cargarVentas();
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


        btnCerrarDetalle.addEventListener(
            'click',
            cerrarDetalle
        );


        tablaVentas.addEventListener(
            'click',
            manejarAccionTabla
        );
    }


    async function cargarVentas() {

        mostrarCargando(
            true
        );


        ocultarMensaje();


        try {

            const response =
                await Auth.fetchAutenticado(
                    API_VENTAS,
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
                ventas
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

            mostrarCargando(
                false
            );
        }
    }


    function mostrarVentas(
        ventas
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

            textoResultados.textContent =
                'No hay ventas registradas.';


            contenedorTabla.classList.add(
                'oculto'
            );


            sinResultados.classList.remove(
                'oculto'
            );


            return;
        }


        textoResultados.textContent =
            ventas.length === 1
                ? 'Se encontró 1 venta.'
                : `Se encontraron ${ventas.length} ventas.`;


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
                behavior: 'smooth',
                block: 'start'
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
                style: 'currency',
                currency: 'ARS'
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


    function limpiarTablaVentas() {

        tablaVentas.innerHTML =
            '';
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
                return 'La solicitud realizada no es válida.';

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