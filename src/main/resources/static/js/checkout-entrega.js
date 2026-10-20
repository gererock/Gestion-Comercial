(() => {

    'use strict';


    const API_CONFIGURACION_ENVIO =
        '/api/public/configuracion-comercio/envio';


    const CLAVE_MEDIO_PAGO =
        'checkout_medio_pago_id';


    const CLAVE_ENTREGA =
        'checkout_medio_entrega';


    const CLAVE_DIRECCION =
        'checkout_direccion_envio_id';


    // =========================================
    // SEGURIDAD
    // =========================================

    if (
        !Auth.requerirRol(
            'CLIENTE'
        )
    ) {

        return;
    }


    // =========================================
    // ELEMENTOS
    // =========================================

    const userName =
        document.getElementById(
            'checkout-user-name'
        );


    const logoutButton =
        document.getElementById(
            'checkout-logout'
        );


    const form =
        document.getElementById(
            'delivery-form'
        );


    const options =
        document.querySelectorAll(
            'input[name="medioEntrega"]'
        );


    const radioEnvio =
        document.querySelector(
            'input[name="medioEntrega"][value="ENVIO"]'
        );


    const opcionEnvio =
        document.getElementById(
            'delivery-shipping-option'
        );


    const shippingStatus =
        document.getElementById(
            'shipping-status'
        );


    const continueButton =
        document.getElementById(
            'delivery-continue'
        );


    const message =
        document.getElementById(
            'checkout-message'
        );


    const deliverySummary =
        document.getElementById(
            'checkout-delivery-summary'
        );


    const productCount =
        document.getElementById(
            'checkout-product-count'
        );


    const total =
        document.getElementById(
            'checkout-total'
        );


    // =========================================
    // ESTADO
    // =========================================

    let configuracionEnvio =
        null;


    let procesando =
        false;


    // =========================================
    // INICIO
    // =========================================

    iniciar();


    async function iniciar() {

        configurarUsuario();

        configurarResumen();

        configurarEventos();


        const carrito =
            Carrito.obtenerCarrito();


        if (
            carrito.length === 0
        ) {

            window.location.href =
                '/carrito/index.html';


            return;
        }


        const idMedioPago =
            Number(
                sessionStorage.getItem(
                    CLAVE_MEDIO_PAGO
                )
            );


        if (
            !Number.isInteger(
                idMedioPago
            )
            ||
            idMedioPago <= 0
        ) {

            window.location.href =
                '/cliente/checkout-medio-pago.html';


            return;
        }


        bloquearEnvio(
            'Verificando monto mínimo y localidades habilitadas...',
            false
        );


        try {

            await cargarConfiguracionEnvio();


        } catch (error) {

            console.error(
                'Error al cargar configuración de envío:',
                error
            );


            bloquearEnvio(
                'No pudimos verificar las condiciones de envío. Podés elegir retiro en el local.',
                true
            );


            mostrarMensaje(
                'No pudimos consultar la configuración de envío.',
                'error'
            );
        }


        restaurarSeleccion();
    }


    // =========================================
    // USUARIO
    // =========================================

    function configurarUsuario() {

        const usuario =
            Auth.obtenerUsuario();


        if (
            usuario
            &&
            usuario.nombre
        ) {

            userName.textContent =
                usuario.nombre;
        }
    }


    // =========================================
    // RESUMEN
    // =========================================

    function configurarResumen() {

        productCount.textContent =
            String(
                Carrito.obtenerCantidadTotal()
            );


        total.textContent =
            formatearPrecio(
                Carrito.obtenerSubtotal()
            );
    }


    // =========================================
    // EVENTOS
    // =========================================

    function configurarEventos() {

        logoutButton.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        form.addEventListener(
            'submit',
            continuar
        );


        options.forEach(
            option => {

                option.addEventListener(
                    'change',
                    () => {

                        seleccionarEntrega(
                            option.value
                        );
                    }
                );
            }
        );
    }


    // =========================================
    // CONFIGURACIÓN ENVÍO
    // =========================================

    async function cargarConfiguracionEnvio() {

        const response =
            await fetch(
                API_CONFIGURACION_ENVIO,
                {
                    headers: {
                        'Accept':
                            'application/json'
                    }
                }
            );


        if (
            !response.ok
        ) {

            throw new Error(
                'No se pudo cargar la configuración de envío.'
            );
        }


        const data =
            await response.json();


        const montoMinimo =
            Number(
                data
                    ?.montoMinimoEnvio
            );


        if (
            !Number.isFinite(
                montoMinimo
            )
            ||
            montoMinimo < 0
            ||
            typeof data.permiteEnvioVillaMaria
            !== 'boolean'
            ||
            typeof data.permiteEnvioVillaNueva
            !== 'boolean'
        ) {

            throw new Error(
                'La configuración de envío recibida no es válida.'
            );
        }


        configuracionEnvio =
            data;


        actualizarDisponibilidadEnvio();
    }


    // =========================================
    // DISPONIBILIDAD ENVÍO
    // =========================================

    function actualizarDisponibilidadEnvio() {

        if (
            !configuracionEnvio
        ) {

            bloquearEnvio(
                'No pudimos verificar las condiciones de envío.',
                true
            );


            return false;
        }


        const subtotal =
            Number(
                Carrito.obtenerSubtotal()
            );


        const montoMinimo =
            Number(
                configuracionEnvio
                    .montoMinimoEnvio
            );


        /*
         * US61 definió:
         *
         * total >= monto mínimo
         * permite envío.
         */

        if (
            subtotal < montoMinimo
        ) {

            const faltante =
                montoMinimo
                -
                subtotal;


            bloquearEnvio(
                `El envío está disponible desde ${formatearPrecio(montoMinimo)}. Te faltan ${formatearPrecio(faltante)}.`,
                true
            );


            return false;
        }


        const villaMaria =
            configuracionEnvio
                .permiteEnvioVillaMaria
            ===
            true;


        const villaNueva =
            configuracionEnvio
                .permiteEnvioVillaNueva
            ===
            true;


        if (
            !villaMaria
            &&
            !villaNueva
        ) {

            bloquearEnvio(
                'El comercio no tiene localidades habilitadas para envío en este momento.',
                true
            );


            return false;
        }


        radioEnvio.disabled =
            false;


        opcionEnvio.classList.remove(
            'is-disabled'
        );


        if (
            villaMaria
            &&
            villaNueva
        ) {

            shippingStatus.textContent =
                `Disponible para Villa María y Villa Nueva. Monto mínimo: ${formatearPrecio(montoMinimo)}.`;


        } else if (
            villaMaria
        ) {

            shippingStatus.textContent =
                `Disponible para Villa María. Monto mínimo: ${formatearPrecio(montoMinimo)}.`;


        } else {

            shippingStatus.textContent =
                `Disponible para Villa Nueva. Monto mínimo: ${formatearPrecio(montoMinimo)}.`;
        }


        return true;
    }


    function bloquearEnvio(
        texto,
        limpiarSeleccion
    ) {

        radioEnvio.disabled =
            true;


        opcionEnvio.classList.add(
            'is-disabled'
        );


        shippingStatus.textContent =
            texto;


        if (
            limpiarSeleccion
            &&
            sessionStorage.getItem(
                CLAVE_ENTREGA
            )
            ===
            'ENVIO'
        ) {

            sessionStorage.removeItem(
                CLAVE_ENTREGA
            );


            sessionStorage.removeItem(
                CLAVE_DIRECCION
            );


            radioEnvio.checked =
                false;


            actualizarSeleccionVisual();

            actualizarResumenEntrega();
        }
    }


    // =========================================
    // SELECCIÓN
    // =========================================

    function seleccionarEntrega(
        medioEntrega
    ) {

        if (
            medioEntrega !== 'RETIRO_LOCAL'
            &&
            medioEntrega !== 'ENVIO'
        ) {

            return;
        }


        if (
            medioEntrega === 'ENVIO'
            &&
            radioEnvio.disabled
        ) {

            return;
        }


        sessionStorage.setItem(
            CLAVE_ENTREGA,
            medioEntrega
        );


        /*
         * Una dirección de envío deja de corresponder
         * si el cliente cambia a retiro en el local.
         */

        if (
            medioEntrega === 'RETIRO_LOCAL'
        ) {

            sessionStorage.removeItem(
                CLAVE_DIRECCION
            );
        }


        actualizarSeleccionVisual();

        actualizarResumenEntrega();

        ocultarMensaje();
    }


    function restaurarSeleccion() {

        const guardado =
            sessionStorage.getItem(
                CLAVE_ENTREGA
            );


        if (
            guardado !== 'RETIRO_LOCAL'
            &&
            guardado !== 'ENVIO'
        ) {

            sessionStorage.removeItem(
                CLAVE_ENTREGA
            );


            actualizarSeleccionVisual();

            actualizarResumenEntrega();


            return;
        }


        if (
            guardado === 'ENVIO'
            &&
            radioEnvio.disabled
        ) {

            sessionStorage.removeItem(
                CLAVE_ENTREGA
            );


            sessionStorage.removeItem(
                CLAVE_DIRECCION
            );


            actualizarSeleccionVisual();

            actualizarResumenEntrega();


            return;
        }


        const input =
            document.querySelector(
                `input[name="medioEntrega"][value="${guardado}"]`
            );


        if (
            input
        ) {

            input.checked =
                true;
        }


        actualizarSeleccionVisual();

        actualizarResumenEntrega();
    }


    function actualizarSeleccionVisual() {

        document
            .querySelectorAll(
                '.delivery-option'
            )
            .forEach(
                option => {

                    const input =
                        option.querySelector(
                            'input[type="radio"]'
                        );


                    option.classList.toggle(
                        'is-selected',
                        Boolean(
                            input
                            &&
                            input.checked
                        )
                    );
                }
            );
    }


    function actualizarResumenEntrega() {

        const medio =
            sessionStorage.getItem(
                CLAVE_ENTREGA
            );


        if (
            medio === 'RETIRO_LOCAL'
        ) {

            deliverySummary.textContent =
                'Retiro en el local';


            return;
        }


        if (
            medio === 'ENVIO'
        ) {

            deliverySummary.textContent =
                'Envío a domicilio';


            return;
        }


        deliverySummary.textContent =
            'Sin seleccionar';
    }


    // =========================================
    // CONTINUAR
    // =========================================

    async function continuar(
        event
    ) {

        event.preventDefault();


        if (
            procesando
        ) {

            return;
        }


        ocultarMensaje();


        const seleccionado =
            form.querySelector(
                'input[name="medioEntrega"]:checked'
            );


        if (
            !seleccionado
        ) {

            mostrarMensaje(
                'Seleccioná una modalidad de entrega para continuar.',
                'error'
            );


            return;
        }


        const medioEntrega =
            seleccionado.value;


        if (
            medioEntrega !== 'RETIRO_LOCAL'
            &&
            medioEntrega !== 'ENVIO'
        ) {

            sessionStorage.removeItem(
                CLAVE_ENTREGA
            );


            mostrarMensaje(
                'La modalidad de entrega seleccionada no es válida.',
                'error'
            );


            return;
        }


        // =====================================
        // RETIRO EN LOCAL
        // =====================================

        if (
            medioEntrega === 'RETIRO_LOCAL'
        ) {

            sessionStorage.setItem(
                CLAVE_ENTREGA,
                'RETIRO_LOCAL'
            );


            sessionStorage.removeItem(
                CLAVE_DIRECCION
            );


            mostrarMensaje(
                'Retiro en el local quedó seleccionado. No necesitás una dirección de envío.',
                'success'
            );


            /*
             * El próximo paso será US10:
             * confirmación definitiva del pedido.
             */


            return;
        }


        // =====================================
        // ENVÍO
        // =====================================

        procesando =
            true;


        continueButton.disabled =
            true;


        const textoOriginal =
            continueButton.textContent;


        continueButton.textContent =
            'Validando envío...';


        try {

            /*
             * Volvemos a consultar justo antes
             * de continuar por si el administrador
             * cambió monto/localidades.
             */

            await cargarConfiguracionEnvio();


            if (
                !actualizarDisponibilidadEnvio()
            ) {

                mostrarMensaje(
                    'En este momento el pedido no cumple las condiciones para envío.',
                    'error'
                );


                return;
            }


            sessionStorage.setItem(
                CLAVE_ENTREGA,
                'ENVIO'
            );


            window.location.href =
                '/cliente/checkout-direccion.html';


        } catch (error) {

            console.error(
                'Error al validar envío:',
                error
            );


            mostrarMensaje(
                'No pudimos validar las condiciones de envío. Intentá nuevamente.',
                'error'
            );


        } finally {

            procesando =
                false;


            continueButton.disabled =
                false;


            continueButton.textContent =
                textoOriginal;
        }
    }


    // =========================================
    // MENSAJES
    // =========================================

    function mostrarMensaje(
        texto,
        tipo
    ) {

        message.textContent =
            texto;


        message.classList.remove(
            'hidden',
            'checkout-message--error',
            'checkout-message--success'
        );


        message.classList.add(
            tipo === 'success'
                ? 'checkout-message--success'
                : 'checkout-message--error'
        );
    }


    function ocultarMensaje() {

        message.textContent =
            '';


        message.classList.add(
            'hidden'
        );


        message.classList.remove(
            'checkout-message--error',
            'checkout-message--success'
        );
    }


    // =========================================
    // FORMATO
    // =========================================

    function formatearPrecio(
        valor
    ) {

        return new Intl.NumberFormat(
            'es-AR',
            {
                style:
                    'currency',

                currency:
                    'ARS',

                maximumFractionDigits:
                    2
            }
        ).format(
            Number(
                valor
            )
        );
    }

})();