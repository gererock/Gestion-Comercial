(() => {

    'use strict';


    const API =
        '/api/vendedor/pedidos/pendientes';


    if (
        !Auth.requerirRol(
            'VENDEDOR',
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

    const btnVolverPanel =
        document.getElementById(
            'btnVolverPanel'
        );

    const mensaje =
        document.getElementById(
            'mensaje'
        );

    const cargando =
        document.getElementById(
            'cargando'
        );

    const sinPedidos =
        document.getElementById(
            'sinPedidos'
        );

    const contenedorPedidos =
        document.getElementById(
            'contenedorPedidos'
        );


    iniciar();


    function iniciar() {

        mostrarUsuario();

        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );

        btnVolverPanel.addEventListener(
            'click',
            volverAlPanel
        );

        cargarPedidos();
    }


    function mostrarUsuario() {

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


    function volverAlPanel() {

        const rol =
            Auth.obtenerRol();


        window.location.href =
            Auth.obtenerRutaPorRol(
                rol
            );
    }


    async function cargarPedidos() {

        ocultarMensaje();

        cargando.classList.remove(
            'oculto'
        );

        sinPedidos.classList.add(
            'oculto'
        );

        contenedorPedidos.classList.add(
            'oculto'
        );


        try {

            const response =
                await Auth.fetchAutenticado(
                    API
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


            const pedidos =
                await response.json();


            renderizarPedidos(
                pedidos
            );

        } catch (error) {

            mostrarError(
                error.message
                ||
                'No se pudieron cargar los pedidos pendientes.'
            );

        } finally {

            cargando.classList.add(
                'oculto'
            );
        }
    }


    function renderizarPedidos(
            pedidos
    ) {

        contenedorPedidos.innerHTML =
            '';


        if (
            !Array.isArray(pedidos)
            ||
            pedidos.length === 0
        ) {

            sinPedidos.classList.remove(
                'oculto'
            );

            return;
        }


        pedidos.forEach(
            pedido => {

                contenedorPedidos.appendChild(
                    crearPedidoCard(
                        pedido
                    )
                );
            }
        );


        contenedorPedidos.classList.remove(
            'oculto'
        );
    }


    function crearPedidoCard(
            pedido
    ) {

        const article =
            document.createElement(
                'article'
            );

        article.className =
            'pedido-card';


        const header =
            document.createElement(
                'div'
            );

        header.className =
            'pedido-header';


        const tituloContenedor =
            document.createElement(
                'div'
            );


        const titulo =
            document.createElement(
                'h3'
            );

        titulo.textContent =
            pedido.numeroPedido
            ||
            `Pedido #${pedido.idPedido}`;


        const fecha =
            document.createElement(
                'p'
            );

        fecha.className =
            'fecha';

        fecha.textContent =
            formatearFecha(
                pedido.fechaCreacion
            );


        tituloContenedor.appendChild(
            titulo
        );

        tituloContenedor.appendChild(
            fecha
        );


        const badge =
            document.createElement(
                'span'
            );

        badge.className =
            'badge';

        badge.textContent =
            formatearTexto(
                pedido.estadoPedido
            );


        header.appendChild(
            tituloContenedor
        );

        header.appendChild(
            badge
        );


        article.appendChild(
            header
        );


        article.appendChild(
            crearDato(
                'Cliente',
                pedido.cliente
                ||
                '-'
            )
        );


        article.appendChild(
            crearDato(
                'Estado de pago',
                formatearTexto(
                    pedido.estadoPago
                ),
                pedido.estadoPago
                    === 'PAGADO'
                    ? 'estado-pago-pagado'
                    : 'estado-pago-pendiente'
            )
        );


        article.appendChild(
            crearDato(
                'Entrega',
                obtenerEntrega(
                    pedido
                )
            )
        );


        article.appendChild(
            crearDato(
                'Total',
                formatearDinero(
                    pedido.total
                ),
                'total'
            )
        );


        const acciones =
            document.createElement(
                'div'
            );

        acciones.className =
            'acciones';


        const enlace =
            document.createElement(
                'a'
            );

        enlace.className =
            'btn btn-primary';

        enlace.textContent =
            'Ver detalle';

        enlace.href =
            `/vendedor/pedido-detalle.html?id=${pedido.idPedido}`;


        acciones.appendChild(
            enlace
        );

        article.appendChild(
            acciones
        );


        return article;
    }


    function crearDato(
            etiqueta,
            valor,
            claseValor = ''
    ) {

        const contenedor =
            document.createElement(
                'div'
            );

        contenedor.className =
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


        if (
            claseValor
        ) {

            strong.classList.add(
                claseValor
            );
        }


        contenedor.appendChild(
            span
        );

        contenedor.appendChild(
            strong
        );


        return contenedor;
    }


    function obtenerEntrega(
            pedido
    ) {

        if (
            pedido.medioEntrega
            ===
            'RETIRO_LOCAL'
        ) {

            return 'Retiro en local';
        }


        if (
            pedido.medioEntrega
            ===
            'ENVIO'
        ) {

            const localidad =
                formatearTexto(
                    pedido.localidadEnvio
                );


            const direccion =
                pedido.direccionEnvio
                ||
                'Dirección no informada';


            return `${localidad} - ${direccion}`;
        }


        return '-';
    }


    function formatearTexto(
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
                    /\b\w/g,
                    letra =>
                            letra.toUpperCase()
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
            Number.isNaN(
                numero
            )
        ) {

            return '$0';
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


    function formatearFecha(
            valor
    ) {

        if (
            !valor
        ) {

            return '-';
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

            return '-';
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
            response
    ) {

        try {

            const body =
                await response.json();


            return body.message
                ||
                body.mensaje
                ||
                'Ocurrió un error al consultar los pedidos.';

        } catch {

            return 'Ocurrió un error al consultar los pedidos.';
        }
    }


    function mostrarError(
            texto
    ) {

        mensaje.textContent =
            texto;

        mensaje.className =
            'mensaje mensaje-error';

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

})();
