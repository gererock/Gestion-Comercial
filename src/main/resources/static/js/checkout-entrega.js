(() => {

    'use strict';


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
    // INICIO
    // =========================================

    iniciar();


    function iniciar() {

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


        /*
         * US12 es posterior a US11.
         * No debe llegarse a entrega
         * sin un medio de pago válido
         * previamente seleccionado.
         */

        const medioPago =
            sessionStorage.getItem(
                CLAVE_MEDIO_PAGO
            );


        if (
            !medioPago
        ) {

            window.location.href =
                '/cliente/checkout-medio-pago.html';


            return;
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


        sessionStorage.setItem(
            CLAVE_ENTREGA,
            medioEntrega
        );


        /*
         * Si cambia a RETIRO,
         * una dirección previamente elegida
         * deja de corresponder al checkout.
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


        switch (
            medio
        ) {

            case 'RETIRO_LOCAL':

                deliverySummary.textContent =
                    'Retiro en el local';

                break;


            case 'ENVIO':

                deliverySummary.textContent =
                    'Envío a domicilio';

                break;


            default:

                deliverySummary.textContent =
                    'Sin seleccionar';
        }
    }


    // =========================================
    // CONTINUAR
    // =========================================

    function continuar(
        event
    ) {

        event.preventDefault();

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


        sessionStorage.setItem(
            CLAVE_ENTREGA,
            medioEntrega
        );


        if (
            medioEntrega === 'RETIRO_LOCAL'
        ) {

            sessionStorage.removeItem(
                CLAVE_DIRECCION
            );


            mostrarMensaje(
                'Retiro en el local quedó seleccionado.',
                'success'
            );


            /*
             * US10 continuará desde aquí hacia
             * la confirmación final.
             *
             * No se solicita dirección.
             */


            return;
        }


        /*
         * ENVIO:
         *
         * En Etapa 2 se validará:
         *
         * - monto mínimo US61;
         * - localidades habilitadas US62;
         * - integración con US13.
         */


        mostrarMensaje(
            'Envío a domicilio quedó seleccionado. En el siguiente paso se validarán las condiciones de envío.',
            'success'
        );
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