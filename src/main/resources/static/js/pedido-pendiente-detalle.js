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

    const mensaje =
        document.getElementById(
            'mensaje'
        );

    const cargando =
        document.getElementById(
            'cargando'
        );

    const detalle =
        document.getElementById(
            'detalle'
        );


    iniciar();


    function iniciar() {

        mostrarUsuario();

        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        const idPedido =
            obtenerIdPedido();


        if (
            idPedido === null
        ) {

            cargando.classList.add(
                'oculto'
            );

            mostrarError(
                'El pedido indicado no es válido.'
            );

            return;
        }


        cargarPedido(
            idPedido
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

            nombreUsuario.textContent =
                usuario.nombre;
        }
    }


    function obtenerIdPedido() {

        const parametros =
            new URLSearchParams(
                window.location.search
            );


        const valor =
            Number(
                parametros.get(
                    'id'
                )
            );


        if (
            !Number.isInteger(
                valor
            )
            ||
            valor <= 0
        ) {

            return null;
        }


        return valor;
    }


    async function cargarPedido(
            idPedido
    ) {

        try {

            const response =
                await Auth.fetchAutenticado(
                    `${API}/${idPedido}`
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


            const pedido =
                await response.json();


            mostrarPedido(
                pedido
            );

        } catch (error) {

            mostrarError(
                error.message
                ||
                'No se pudo consultar el pedido.'
            );

        } finally {

            cargando.classList.add(
                'oculto'
            );
        }
    }


    function mostrarPedido(
            pedido
    ) {

        document.getElementById(
            'numeroPedido'
        ).textContent =
            pedido.numeroPedido
            ||
            `Pedido #${pedido.idPedido}`;


        document.getElementById(
            'cliente'
        ).textContent =
            pedido.cliente
            ||
            '-';


        document.getElementById(
            'fechaCreacion'
        ).textContent =
            formatearFecha(
                pedido.fechaCreacion
            );


        document.getElementById(
            'estadoPedido'
        ).textContent =
            formatearTexto(
                pedido.estadoPedido
            );


        document.getElementById(
            'estadoPago'
        ).textContent =
            formatearTexto(
                pedido.estadoPago
            );


        document.getElementById(
            'medioEntrega'
        ).textContent =
            formatearTexto(
                pedido.medioEntrega
            );


        const filaLocalidad =
            document.getElementById(
                'filaLocalidad'
            );

        const filaDireccion =
            document.getElementById(
                'filaDireccion'
            );


        if (
            pedido.medioEntrega
            ===
            'ENVIO'
        ) {

            document.getElementById(
                'localidad'
            ).textContent =
                formatearTexto(
                    pedido.localidadEnvio
                );


            document.getElementById(
                'direccion'
            ).textContent =
                pedido.direccionEnvio
                ||
                '-';


            filaLocalidad.classList.remove(
                'oculto'
            );

            filaDireccion.classList.remove(
                'oculto'
            );

        } else {

            filaLocalidad.classList.add(
                'oculto'
            );

            filaDireccion.classList.add(
                'oculto'
            );
        }


        document.getElementById(
            'total'
        ).textContent =
            formatearDinero(
                pedido.total
            );


        detalle.classList.remove(
            'oculto'
        );
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
                'El pedido pendiente indicado no existe.';

        } catch {

            return 'El pedido pendiente indicado no existe.';
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

        detalle.classList.add(
            'oculto'
        );
    }

})();