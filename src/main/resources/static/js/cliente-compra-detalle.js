(() => {

    'use strict';


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

    const detalle =
        document.getElementById(
            'detalle'
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


        const parametros =
            obtenerParametros();


        if (
            parametros === null
        ) {

            cargando.classList.add(
                'oculto'
            );

            mostrarError(
                'La compra indicada no es válida.'
            );

            return;
        }

        const btnVolverHistorial =
            document.getElementById(
                'btnVolverHistorial'
            );


        btnVolverHistorial.href =
            `/vendedor/cliente-historial.html?idCliente=${parametros.idCliente}`;


        btnVolverHistorial.textContent =
            '← Volver al historial';
            
        cargarCompra(
            parametros.idCliente,
            parametros.idPedido
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


    function obtenerParametros() {

        const parametros =
            new URLSearchParams(
                window.location.search
            );


        const idCliente =
            Number(
                parametros.get(
                    'idCliente'
                )
            );


        const idPedido =
            Number(
                parametros.get(
                    'idPedido'
                )
            );


        if (
            !Number.isInteger(
                idCliente
            )
            ||
            idCliente <= 0
            ||
            !Number.isInteger(
                idPedido
            )
            ||
            idPedido <= 0
        ) {

            return null;
        }


        return {
            idCliente,
            idPedido
        };
    }


    async function cargarCompra(
        idCliente,
        idPedido
    ) {

        try {

            const response =
                await Auth.fetchAutenticado(
                    `/api/vendedor/clientes/${idCliente}/historial/${idPedido}`
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


            const compra =
                await response.json();


            mostrarCompra(
                compra
            );

        } catch (error) {

            mostrarError(
                error.message
                ||
                'No se pudo consultar la compra.'
            );

        } finally {

            cargando.classList.add(
                'oculto'
            );
        }
    }


    function mostrarCompra(
        compra
    ) {

        ponerTexto(
            'numeroPedido',
            compra.numeroPedido
            ||
            `Pedido #${compra.idPedido}`
        );


        ponerTexto(
            'fechaCreacion',
            formatearFecha(
                compra.fechaCreacion
            )
        );


        ponerTexto(
            'tipoVenta',
            formatearTextoEnum(
                compra.tipoVenta
            )
        );


        ponerTexto(
            'estadoPedido',
            formatearTextoEnum(
                compra.estadoPedido
            )
        );


        ponerTexto(
            'estadoPago',
            formatearTextoEnum(
                compra.estadoPago
            )
        );


        ponerTexto(
            'medioPago',
            compra.medioPago
            ||
            'No informado'
        );


        ponerTexto(
            'medioEntrega',
            formatearTextoEnum(
                compra.medioEntrega
            )
        );


        mostrarProductos(
            compra.productos
        );


        mostrarImportes(
            compra
        );


        mensaje.classList.add(
            'oculto'
        );


        detalle.classList.remove(
            'oculto'
        );
    }


    function mostrarProductos(
        productos
    ) {

        const tbody =
            document.getElementById(
                'productos'
            );


        const sinProductos =
            document.getElementById(
                'sinProductos'
            );


        tbody.innerHTML =
            '';


        if (
            !Array.isArray(
                productos
            )
            ||
            productos.length === 0
        ) {

            sinProductos.classList.remove(
                'oculto'
            );

            return;
        }


        sinProductos.classList.add(
            'oculto'
        );


        productos.forEach(
            producto => {

                const fila =
                    document.createElement(
                        'tr'
                    );


                agregarCelda(
                    fila,
                    'Producto',
                    producto.nombre
                    ||
                    '-'
                );


                agregarCelda(
                    fila,
                    'Cantidad',
                    String(
                        producto.cantidad
                        ||
                        0
                    )
                );


                agregarCelda(
                    fila,
                    'Precio unitario',
                    formatearDinero(
                        producto.precioUnitario
                    )
                );


                agregarCelda(
                    fila,
                    'Descuento',
                    formatearDescuento(
                        producto.descuentoAplicado
                    )
                );


                agregarCelda(
                    fila,
                    'Subtotal',
                    formatearDinero(
                        producto.subtotal
                    )
                );


                tbody.appendChild(
                    fila
                );
            }
        );
    }


    function mostrarImportes(
        compra
    ) {

        ponerTexto(
            'subtotal',
            formatearDinero(
                compra.subtotal
            )
        );


        ponerTexto(
            'descuentoOferta',
            formatearDescuento(
                compra.descuentoOferta
            )
        );


        ponerTexto(
            'descuentoMayorista',
            formatearDescuento(
                compra.descuentoMayorista
            )
        );


        ponerTexto(
            'descuentoCliente',
            formatearDescuento(
                compra.descuentoCliente
            )
        );


        ponerTexto(
            'total',
            formatearDinero(
                compra.total
            )
        );
    }


    function agregarCelda(
        fila,
        etiqueta,
        valor
    ) {

        const celda =
            document.createElement(
                'td'
            );


        celda.dataset.label =
            etiqueta;


        celda.textContent =
            valor;


        fila.appendChild(
            celda
        );
    }


    function ponerTexto(
        id,
        valor
    ) {

        document.getElementById(
            id
        ).textContent =
            valor;
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


        return new Intl.NumberFormat(
            'es-AR',
            {
                style:
                    'currency',

                currency:
                    'ARS'
            }
        ).format(
            Number.isFinite(
                numero
            )
                ? numero
                : 0
        );
    }


    function formatearDescuento(
        valor
    ) {

        const numero =
            Number(
                valor
                ||
                0
            );


        if (
            !Number.isFinite(
                numero
            )
            ||
            numero <= 0
        ) {

            return formatearDinero(
                0
            );
        }


        return `− ${formatearDinero(numero)}`;
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
                'La compra indicada no está disponible.';

        } catch {

            return 'La compra indicada no está disponible.';
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