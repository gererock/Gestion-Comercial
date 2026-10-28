(() => {

    'use strict';


    const API_CLIENTES =
        '/api/vendedor/clientes';


    if (
        !Auth.requerirRol(
            'VENDEDOR',
            'ADMINISTRADOR'
        )
    ) {

        return;
    }


    const mensaje =
        document.getElementById(
            'mensaje'
        );

    const cargando =
        document.getElementById(
            'cargando'
        );

    const sinCompras =
        document.getElementById(
            'sinCompras'
        );

    const historial =
        document.getElementById(
            'historial'
        );


    iniciar();


    function iniciar() {

        mostrarUsuario();


        document.getElementById(
            'btnCerrarSesion'
        ).addEventListener(
            'click',
            Auth.cerrarSesion
        );


        const idCliente =
            obtenerIdCliente();


        if (
            idCliente === null
        ) {

            cargando.classList.add(
                'oculto'
            );

            mostrarError(
                'El cliente indicado no es válido.'
            );

            return;
        }



        cargarPagina(
            idCliente
        );
    }

    function mostrarUsuario() {

        const usuario =
            Auth.obtenerUsuario();


        if (
            usuario
            &&
            usuario.nombre
        ) {

            document.getElementById(
                'nombreUsuario'
            ).textContent =
                usuario.nombre;
        }
    }

    function obtenerIdCliente() {

        const parametros =
            new URLSearchParams(
                window.location.search
            );


        const valor =
            Number(
                parametros.get(
                    'idCliente'
                )
            );


        if (
            !Number.isInteger(valor)
            ||
            valor <= 0
        ) {

            return null;
        }


        return valor;
    }

    async function cargarPagina(
        idCliente
    ) {

        try {

            await cargarCliente(
                idCliente
            );


            const compras =
                await cargarHistorial(
                    idCliente
                );


            mostrarHistorial(
                compras,
                idCliente
            );

        } catch (error) {

            mostrarError(
                error.message
                ||
                'No se pudo consultar el historial de compras.'
            );

        } finally {

            cargando.classList.add(
                'oculto'
            );
        }
    }

    async function cargarCliente(
        idCliente
    ) {

        const response =
            await Auth.fetchAutenticado(
                `${API_CLIENTES}/${idCliente}`
            );


        if (
            !response
        ) {

            throw new Error(
                'No se pudo consultar el cliente.'
            );
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


        const cliente =
            await response.json();


        document.getElementById(
            'nombreCliente'
        ).textContent =
            obtenerNombreCompleto(
                cliente
            );

        const btnVolverCliente =
            document.getElementById(
                'btnVolverCliente'
            );


        btnVolverCliente.href =
            `/vendedor/cliente-detalle.html?id=${idCliente}`;


        btnVolverCliente.textContent =
            '← Volver al cliente';
    }

    function obtenerNombreCompleto(
        cliente
    ) {

        const nombre =
            `${cliente.nombre || ''} ${cliente.apellido || ''}`
                .trim();


        return nombre
            ||
            `Cliente #${cliente.idCliente}`;
    }

    async function cargarHistorial(
        idCliente
    ) {

        const response =
            await Auth.fetchAutenticado(
                `${API_CLIENTES}/${idCliente}/historial`
            );


        if (
            !response
        ) {

            throw new Error(
                'No se pudo consultar el historial.'
            );
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


        return await response.json();
    }

    function mostrarHistorial(
        compras,
        idCliente
    ) {

        historial.innerHTML =
            '';


        if (
            !Array.isArray(compras)
            ||
            compras.length === 0
        ) {

            sinCompras.classList.remove(
                'oculto'
            );

            historial.classList.add(
                'oculto'
            );

            return;
        }


        sinCompras.classList.add(
            'oculto'
        );


        compras.forEach(
            compra => {

                historial.appendChild(
                    crearTarjetaCompra(
                        compra,
                        idCliente
                    )
                );
            }
        );


        historial.classList.remove(
            'oculto'
        );
    }

    function crearTarjetaCompra(
        compra,
        idCliente
    ) {

        const tarjeta =
            document.createElement(
                'article'
            );


        tarjeta.className =
            'cliente-card';


        const encabezado =
            document.createElement(
                'div'
            );


        encabezado.className =
            'cliente-header';


        const numeroPedido =
            document.createElement(
                'h3'
            );


        numeroPedido.textContent =
            compra.numeroPedido
            ||
            `Pedido #${compra.idPedido}`;


        const tipoVenta =
            document.createElement(
                'span'
            );


        tipoVenta.className =
            'badge badge-activo';


        tipoVenta.textContent =
            formatearTextoEnum(
                compra.tipoVenta
            );


        encabezado.appendChild(
            numeroPedido
        );

        encabezado.appendChild(
            tipoVenta
        );


        tarjeta.appendChild(
            encabezado
        );


        tarjeta.appendChild(
            crearDato(
                'Fecha',
                formatearFecha(
                    compra.fechaCreacion
                )
            )
        );


        tarjeta.appendChild(
            crearDato(
                'Estado del pedido',
                formatearTextoEnum(
                    compra.estadoPedido
                )
            )
        );


        tarjeta.appendChild(
            crearDato(
                'Estado del pago',
                formatearTextoEnum(
                    compra.estadoPago
                )
            )
        );


        tarjeta.appendChild(
            crearDato(
                'Medio de entrega',
                formatearTextoEnum(
                    compra.medioEntrega
                )
            )
        );


        tarjeta.appendChild(
            crearDato(
                'Total',
                formatearDinero(
                    compra.total
                )
            )
        );


        const acciones =
            document.createElement(
                'div'
            );


        acciones.className =
            'acciones';


        const btnDetalle =
            document.createElement(
                'a'
            );


        btnDetalle.className =
            'btn btn-primary';


        btnDetalle.textContent =
            'Ver detalle';


        btnDetalle.href =
            `/vendedor/cliente-compra-detalle.html?idCliente=${idCliente}&idPedido=${compra.idPedido}`;


        acciones.appendChild(
            btnDetalle
        );


        tarjeta.appendChild(
            acciones
        );


        return tarjeta;
    }

    function crearDato(
        etiqueta,
        valor
    ) {

        const dato =
            document.createElement(
                'div'
            );


        dato.className =
            'dato';


        const span =
            document.createElement(
                'span'
            );


        span.textContent =
            etiqueta;


        const strong =
            document.createElement(
                'strong'
            );


        strong.textContent =
            valor;


        dato.appendChild(
            span
        );

        dato.appendChild(
            strong
        );


        return dato;
    }

    function formatearTextoEnum(
        valor
    ) {

        if (
            !valor
        ) {

            return 'No informado';
        }


        return String(
            valor
        )
            .replaceAll(
                '_',
                ' '
            )
            .toLowerCase()
            .replace(
                /^./,
                letra =>
                    letra.toUpperCase()
            );
    }

    function formatearFecha(
        valor
    ) {

        if (
            !valor
        ) {

            return 'No informada';
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

            return 'No informada';
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

    function formatearDinero(
        valor
    ) {

        const numero =
            Number(
                valor
            );


        if (
            !Number.isFinite(numero)
        ) {

            return '$ 0';
        }


        return new Intl.NumberFormat(
            'es-AR',
            {
                style:
                    'currency',

                currency:
                    'ARS'
            }
        ).format(
            numero
        );
    }

    async function obtenerMensajeError(
        response
    ) {

        try {

            const body =
                await response.json();


            return body.message
                ||
                body.mensaje
                ||
                'No se pudo consultar el historial.';

        } catch {

            return 'No se pudo consultar el historial.';
        }
    }

    function mostrarError(
        texto
    ) {

        const nombreCliente =
            document.getElementById(
                'nombreCliente'
            );


        if (
            nombreCliente.textContent.trim() ===
            'Cargando cliente...'
        ) {

            nombreCliente.textContent =
                'Cliente no disponible';
        }
        
        mensaje.textContent =
            texto;


        mensaje.className =
            'mensaje mensaje-error';


        mensaje.classList.remove(
            'oculto'
        );


        sinCompras.classList.add(
            'oculto'
        );


        historial.classList.add(
            'oculto'
        );
    }
})();   
