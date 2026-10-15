(() => {

    'use strict';


    const API_BASE =
        '/api/vendedor/pedidos';


    // =====================================================
    // SEGURIDAD
    // =====================================================

    if (
        !Auth.requerirRol(
            'VENDEDOR',
            'ADMINISTRADOR'
        )
    ) {

        return;
    }


    // =====================================================
    // ELEMENTOS
    // =====================================================

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


    const formBuscar =
        document.getElementById(
            'formBuscar'
        );


    const idPedidoInput =
        document.getElementById(
            'idPedido'
        );


    const errorIdPedido =
        document.getElementById(
            'errorIdPedido'
        );


    const btnBuscar =
        document.getElementById(
            'btnBuscar'
        );


    const cargando =
        document.getElementById(
            'cargando'
        );


    const seccionPedido =
        document.getElementById(
            'seccionPedido'
        );


    const numeroPedido =
        document.getElementById(
            'numeroPedido'
        );


    const pedidoId =
        document.getElementById(
            'pedidoId'
        );


    const estadoPedido =
        document.getElementById(
            'estadoPedido'
        );


    const medioPago =
        document.getElementById(
            'medioPago'
        );


    const estadoPagoActual =
        document.getElementById(
            'estadoPagoActual'
        );


    const fechaActualizacion =
        document.getElementById(
            'fechaActualizacion'
        );


    const descripcionActualContenedor =
        document.getElementById(
            'descripcionActualContenedor'
        );


    const descripcionActual =
        document.getElementById(
            'descripcionActual'
        );


    const formPago =
        document.getElementById(
            'formPago'
        );


    const estadoPago =
        document.getElementById(
            'estadoPago'
        );


    const descripcionPago =
        document.getElementById(
            'descripcionPago'
        );


    const contenedorDescripcion =
        document.getElementById(
            'contenedorDescripcion'
        );


    const errorEstadoPago =
        document.getElementById(
            'errorEstadoPago'
        );


    const errorDescripcion =
        document.getElementById(
            'errorDescripcion'
        );


    const btnCancelar =
        document.getElementById(
            'btnCancelar'
        );


    const btnGuardar =
        document.getElementById(
            'btnGuardar'
        );


    const mensaje =
        document.getElementById(
            'mensaje'
        );


    // =====================================================
    // ESTADO
    // =====================================================

    let pedidoActual =
        null;


    let buscando =
        false;


    let guardando =
        false;


    // =====================================================
    // INICIO
    // =====================================================

    iniciar();


    function iniciar() {

        configurarUsuario();

        configurarEventos();

        configurarVolver();

        cargarPedidoDesdeUrl();
    }


    function configurarUsuario() {

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


    function configurarVolver() {

        btnVolverPanel.href =
            Auth.obtenerRutaPorRol(
                Auth.obtenerRol()
            );
    }


    function configurarEventos() {

        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        formBuscar.addEventListener(
            'submit',
            buscarPedido
        );


        formPago.addEventListener(
            'submit',
            guardarPago
        );


        btnCancelar.addEventListener(
            'click',
            restaurarFormulario
        );


        estadoPago.addEventListener(
            'change',
            actualizarCampoDescripcion
        );


        descripcionPago.addEventListener(
            'input',
            () => {

                ocultarErrorDescripcion();
            }
        );


        idPedidoInput.addEventListener(
            'input',
            () => {

                ocultarErrorId();

                ocultarMensaje();
            }
        );
    }


    // =====================================================
    // SOPORTE FUTURO US16 / US17
    // =====================================================

    function cargarPedidoDesdeUrl() {

        const parametros =
            new URLSearchParams(
                window.location.search
            );


        const id =
            parametros.get(
                'idPedido'
            );


        if (
            !id
        ) {

            return;
        }


        idPedidoInput.value =
            id;


        consultarPedido(
            id
        );
    }


    // =====================================================
    // BUSCAR
    // =====================================================

    async function buscarPedido(
        event
    ) {

        event.preventDefault();


        const id =
            validarIdPedido();


        if (
            id === null
        ) {

            return;
        }


        await consultarPedido(
            id
        );
    }


    function validarIdPedido() {

        ocultarErrorId();

        ocultarMensaje();


        const valor =
            idPedidoInput.value.trim();


        if (
            valor === ''
        ) {

            mostrarErrorId(
                'Ingresá el ID del pedido.'
            );


            return null;
        }


        if (
            !/^\d+$/.test(
                valor
            )
        ) {

            mostrarErrorId(
                'El ID debe ser un número entero válido.'
            );


            return null;
        }


        const id =
            Number(
                valor
            );


        if (
            !Number.isInteger(
                id
            )
            ||
            id <= 0
        ) {

            mostrarErrorId(
                'Ingresá un ID de pedido mayor a cero.'
            );


            return null;
        }


        return id;
    }


    async function consultarPedido(
        idPedido
    ) {

        if (
            buscando
        ) {

            return;
        }


        buscando =
            true;


        btnBuscar.disabled =
            true;


        mostrarCargando();

        ocultarMensaje();


        try {

            const response =
                await Auth.fetchAutenticado(
                    `${API_BASE}/${idPedido}/pago`,
                    {
                        headers: {
                            'Accept':
                                'application/json'
                        }
                    }
                );


            if (
                !response
            ) {

                return;
            }


            if (
                response.status === 404
            ) {

                pedidoActual =
                    null;


                ocultarPedido();


                mostrarMensaje(
                    'No se encontró el pedido indicado.',
                    'error'
                );


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


            validarRespuestaPedido(
                pedido
            );


            pedidoActual =
                pedido;


            renderizarPedido();


            actualizarUrl(
                pedido.idPedido
            );


        } catch (error) {

            console.error(
                'Error al consultar pedido:',
                error
            );


            pedidoActual =
                null;


            ocultarPedido();


            mostrarMensaje(
                error.message
                ||
                'No se pudo consultar el pedido.',
                'error'
            );


        } finally {

            buscando =
                false;


            btnBuscar.disabled =
                false;


            cargando.classList.add(
                'oculto'
            );
        }
    }


    // =====================================================
    // RENDER
    // =====================================================

    function validarRespuestaPedido(
        pedido
    ) {

        if (
            !pedido
            ||
            !Number.isInteger(
                Number(
                    pedido.idPedido
                )
            )
            ||
            !pedido.numeroPedido
            ||
            !pedido.estadoPago
            ||
            !pedido.medioPago
        ) {

            throw new Error(
                'La información recibida del pedido no es válida.'
            );
        }
    }


    function renderizarPedido() {

        cargando.classList.add(
            'oculto'
        );


        seccionPedido.classList.remove(
            'oculto'
        );


        pedidoId.textContent =
            pedidoActual.idPedido;


        numeroPedido.textContent =
            pedidoActual.numeroPedido;


        estadoPedido.textContent =
            formatearEstado(
                pedidoActual.estadoPedido
            );


        medioPago.textContent =
            pedidoActual.medioPago;


        estadoPagoActual.textContent =
            formatearEstado(
                pedidoActual.estadoPago
            );


        estadoPagoActual.classList.remove(
            'estado-pago--pendiente',
            'estado-pago--pagado'
        );


        estadoPagoActual.classList.add(
            pedidoActual.estadoPago
                === 'PAGADO'
                ? 'estado-pago--pagado'
                : 'estado-pago--pendiente'
        );


        fechaActualizacion.textContent =
            formatearFecha(
                pedidoActual.fechaActualizacion
            );


        if (
            pedidoActual.descripcionPago
        ) {

            descripcionActual.textContent =
                pedidoActual.descripcionPago;


            descripcionActualContenedor
                .classList
                .remove(
                    'oculto'
                );

        } else {

            descripcionActual.textContent =
                '';


            descripcionActualContenedor
                .classList
                .add(
                    'oculto'
                );
        }


        restaurarFormulario();
    }


    function ocultarPedido() {

        seccionPedido.classList.add(
            'oculto'
        );


        cargando.classList.add(
            'oculto'
        );
    }


    function mostrarCargando() {

        seccionPedido.classList.add(
            'oculto'
        );


        cargando.classList.remove(
            'oculto'
        );
    }


    // =====================================================
    // FORMULARIO
    // =====================================================

    function restaurarFormulario() {

        ocultarErroresFormulario();


        if (
            !pedidoActual
        ) {

            return;
        }


        estadoPago.value =
            pedidoActual.estadoPago;


        descripcionPago.value =
            pedidoActual.descripcionPago
            ||
            '';


        actualizarCampoDescripcion();
    }


    function actualizarCampoDescripcion() {

        ocultarErrorEstado();

        ocultarErrorDescripcion();


        if (
            estadoPago.value
            ===
            'PENDIENTE'
        ) {

            contenedorDescripcion.classList.remove(
                'oculto'
            );


            return;
        }


        contenedorDescripcion.classList.add(
            'oculto'
        );


        descripcionPago.value =
            '';
    }


    // =====================================================
    // GUARDAR
    // =====================================================

    async function guardarPago(
        event
    ) {

        event.preventDefault();


        if (
            guardando
            ||
            !pedidoActual
        ) {

            return;
        }


        ocultarMensaje();

        ocultarErroresFormulario();


        const datos =
            validarFormulario();


        if (
            datos === null
        ) {

            return;
        }


        guardando =
            true;


        btnGuardar.disabled =
            true;


        btnCancelar.disabled =
            true;


        const textoOriginal =
            btnGuardar.textContent;


        btnGuardar.textContent =
            'Guardando...';


        try {

            const response =
                await Auth.fetchAutenticado(
                    `${API_BASE}/${pedidoActual.idPedido}/pago`,
                    {
                        method:
                            'PATCH',

                        headers: {
                            'Content-Type':
                                'application/json',

                            'Accept':
                                'application/json'
                        },

                        body:
                            JSON.stringify(
                                datos
                            )
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


            const actualizado =
                await response.json();


            validarRespuestaPedido(
                actualizado
            );


            pedidoActual =
                actualizado;


            renderizarPedido();


            mostrarMensaje(
                'El estado de pago se actualizó correctamente.',
                'exito'
            );


        } catch (error) {

            console.error(
                'Error al actualizar pago:',
                error
            );


            mostrarMensaje(
                error.message
                ||
                'No se pudo actualizar el estado de pago.',
                'error'
            );


        } finally {

            guardando =
                false;


            btnGuardar.disabled =
                false;


            btnCancelar.disabled =
                false;


            btnGuardar.textContent =
                textoOriginal;
        }
    }


    function validarFormulario() {

        const nuevoEstado =
            estadoPago.value;


        if (
            nuevoEstado !== 'PENDIENTE'
            &&
            nuevoEstado !== 'PAGADO'
        ) {

            mostrarErrorEstado(
                'Seleccioná un estado de pago válido.'
            );


            return null;
        }


        let descripcion =
            null;


        if (
            nuevoEstado === 'PENDIENTE'
        ) {

            const valor =
                descripcionPago.value
                    .trim()
                    .replace(
                        /\s+/g,
                        ' '
                    );


            if (
                valor.length > 500
            ) {

                mostrarErrorDescripcion(
                    'La descripción no puede superar los 500 caracteres.'
                );


                return null;
            }


            if (
                valor !== ''
                &&
                !/[\p{L}\p{N}]/u.test(
                    valor
                )
            ) {

                mostrarErrorDescripcion(
                    'La descripción debe contener letras o números.'
                );


                return null;
            }


            descripcion =
                valor === ''
                    ? null
                    : valor;
        }


        return {
            estadoPago:
                nuevoEstado,

            descripcionPago:
                descripcion
        };
    }


    // =====================================================
    // URL
    // =====================================================

    function actualizarUrl(
        idPedido
    ) {

        const url =
            new URL(
                window.location.href
            );


        url.searchParams.set(
            'idPedido',
            idPedido
        );


        window.history.replaceState(
            {},
            '',
            url
        );
    }


    // =====================================================
    // ERRORES CAMPOS
    // =====================================================

    function mostrarErrorId(
        texto
    ) {

        errorIdPedido.textContent =
            texto;


        errorIdPedido.classList.remove(
            'oculto'
        );
    }


    function ocultarErrorId() {

        errorIdPedido.textContent =
            '';


        errorIdPedido.classList.add(
            'oculto'
        );
    }


    function mostrarErrorEstado(
        texto
    ) {

        errorEstadoPago.textContent =
            texto;


        errorEstadoPago.classList.remove(
            'oculto'
        );
    }


    function ocultarErrorEstado() {

        errorEstadoPago.textContent =
            '';


        errorEstadoPago.classList.add(
            'oculto'
        );
    }


    function mostrarErrorDescripcion(
        texto
    ) {

        errorDescripcion.textContent =
            texto;


        errorDescripcion.classList.remove(
            'oculto'
        );
    }


    function ocultarErrorDescripcion() {

        errorDescripcion.textContent =
            '';


        errorDescripcion.classList.add(
            'oculto'
        );
    }


    function ocultarErroresFormulario() {

        ocultarErrorEstado();

        ocultarErrorDescripcion();
    }


    // =====================================================
    // MENSAJES
    // =====================================================

    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensaje.textContent =
            texto;


        mensaje.classList.remove(
            'oculto',
            'mensaje-exito',
            'mensaje-error'
        );


        mensaje.classList.add(
            tipo === 'exito'
                ? 'mensaje-exito'
                : 'mensaje-error'
        );
    }


    function ocultarMensaje() {

        mensaje.textContent =
            '';


        mensaje.classList.add(
            'oculto'
        );


        mensaje.classList.remove(
            'mensaje-exito',
            'mensaje-error'
        );
    }


    // =====================================================
    // ERROR API
    // =====================================================

    async function obtenerMensajeError(
        response
    ) {

        try {

            const body =
                await response.json();


            if (
                body.errors
                &&
                typeof body.errors
                ===
                'object'
            ) {

                const errores =
                    Object.values(
                        body.errors
                    );


                if (
                    errores.length > 0
                ) {

                    return errores[0];
                }
            }


            if (
                typeof body.message
                ===
                'string'
                &&
                body.message.trim()
                !==
                ''
            ) {

                return body.message.trim();
            }


        } catch (error) {

            // Se utilizará mensaje genérico.
        }


        if (
            response.status === 404
        ) {

            return 'No se encontró el pedido indicado.';
        }


        if (
            response.status === 409
        ) {

            return 'El cambio solicitado no está permitido.';
        }


        return 'No se pudo procesar la solicitud.';
    }


    // =====================================================
    // FORMATO
    // =====================================================

    function formatearEstado(
        valor
    ) {

        if (
            !valor
        ) {

            return '-';
        }


        const texto =
            String(
                valor
            )
                .toLowerCase()
                .replaceAll(
                    '_',
                    ' '
                );


        return texto
            .charAt(0)
            .toUpperCase()
            +
            texto.slice(1);
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

})();   