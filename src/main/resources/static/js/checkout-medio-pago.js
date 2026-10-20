(() => {

    'use strict';


    const API_URL =
        '/api/cliente/medios-pago';


    const STORAGE_KEY =
        'checkout_medio_pago_id';


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


    const loading =
        document.getElementById(
            'payment-loading'
        );


    const errorState =
        document.getElementById(
            'payment-error'
        );


    const emptyState =
        document.getElementById(
            'payment-empty'
        );


    const retryButton =
        document.getElementById(
            'payment-retry'
        );


    const form =
        document.getElementById(
            'payment-form'
        );


    const optionsContainer =
        document.getElementById(
            'payment-options'
        );


    const continueButton =
        document.getElementById(
            'payment-continue'
        );


    const message =
        document.getElementById(
            'checkout-message'
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

    let mediosPago =
        [];


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


        await cargarMediosPago();
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


        retryButton.addEventListener(
            'click',
            cargarMediosPago
        );


        form.addEventListener(
            'submit',
            validarYContinuar
        );
    }


    // =========================================
    // CARGAR MEDIOS
    // =========================================

    async function cargarMediosPago() {

        mostrarCargando();

        ocultarMensaje();


        try {

            const response =
                await Auth.fetchAutenticado(
                    API_URL,
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
                response.status === 403
            ) {

                Auth.redirigirSegunRol(
                    Auth.obtenerRol()
                );


                return;
            }


            if (
                !response.ok
            ) {

                throw new Error(
                    'No se pudieron cargar los medios de pago.'
                );
            }


            const data =
                await response.json();


            if (
                !Array.isArray(
                    data
                )
            ) {

                throw new Error(
                    'La respuesta del servidor no es válida.'
                );
            }


            mediosPago =
                data.filter(
                    medio =>
                        medio
                        &&
                        medio.activo === true
                );


            renderizarMediosPago();


        } catch (error) {

            console.error(
                'Error al cargar medios de pago:',
                error
            );


            mostrarErrorCarga();
        }
    }


    // =========================================
    // RENDER
    // =========================================

    function renderizarMediosPago() {

        optionsContainer.innerHTML =
            '';


        loading.classList.add(
            'hidden'
        );


        errorState.classList.add(
            'hidden'
        );


        if (
            mediosPago.length === 0
        ) {

            form.classList.add(
                'hidden'
            );


            emptyState.classList.remove(
                'hidden'
            );


            sessionStorage.removeItem(
                STORAGE_KEY
            );


            return;
        }


        emptyState.classList.add(
            'hidden'
        );


        form.classList.remove(
            'hidden'
        );


        mediosPago.forEach(
            medioPago => {

                optionsContainer.appendChild(
                    crearOpcion(
                        medioPago
                    )
                );
            }
        );


        restaurarSeleccion();
    }


    function crearOpcion(
        medioPago
    ) {

        const label =
            document.createElement(
                'label'
            );


        label.className =
            'payment-option';


        const input =
            document.createElement(
                'input'
            );


        input.type =
            'radio';


        input.name =
            'medioPago';


        input.value =
            String(
                medioPago.idMedioPago
            );


        const content =
            document.createElement(
                'span'
            );


        content.className =
            'payment-option__content';


        const name =
            document.createElement(
                'strong'
            );


        name.className =
            'payment-option__name';


        name.textContent =
            medioPago.nombre;


        const description =
            document.createElement(
                'span'
            );


        description.className =
            'payment-option__description';


        description.textContent =
            obtenerDescripcion(
                medioPago.nombre
            );


        content.appendChild(
            name
        );


        content.appendChild(
            description
        );


        label.appendChild(
            input
        );


        label.appendChild(
            content
        );


        input.addEventListener(
            'change',
            () => {

                seleccionarMedioPago(
                    medioPago.idMedioPago
                );
            }
        );


        return label;
    }


    function obtenerDescripcion(
        nombre
    ) {

        const normalizado =
            String(
                nombre
            )
                .trim()
                .toLowerCase();


        if (
            normalizado.includes(
                'efectivo'
            )
        ) {

            return 'Abonás el pedido en efectivo.';
        }


        if (
            normalizado.includes(
                'transferencia'
            )
        ) {

            return 'Abonás mediante transferencia bancaria.';
        }


        return 'Medio de pago habilitado por el comercio.';
    }


    // =========================================
    // SELECCIÓN
    // =========================================

    function seleccionarMedioPago(
        idMedioPago
    ) {

        sessionStorage.setItem(
            STORAGE_KEY,
            String(
                idMedioPago
            )
        );


        actualizarSeleccionVisual();

        ocultarMensaje();
    }


    function restaurarSeleccion() {

        const valorGuardado =
            sessionStorage.getItem(
                STORAGE_KEY
            );


        if (
            !valorGuardado
        ) {

            actualizarSeleccionVisual();

            return;
        }


        const idGuardado =
            Number(
                valorGuardado
            );


        const existeActivo =
            mediosPago.some(
                medio =>
                    Number(
                        medio.idMedioPago
                    )
                    ===
                    idGuardado
            );


        if (
            !existeActivo
        ) {

            sessionStorage.removeItem(
                STORAGE_KEY
            );


            actualizarSeleccionVisual();

            return;
        }


        const input =
            optionsContainer.querySelector(
                `input[name="medioPago"][value="${idGuardado}"]`
            );


        if (
            input
        ) {

            input.checked =
                true;
        }


        actualizarSeleccionVisual();
    }


    function actualizarSeleccionVisual() {

        optionsContainer
            .querySelectorAll(
                '.payment-option'
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


    // =========================================
    // CONTINUAR
    // =========================================

    async function validarYContinuar(
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
                'input[name="medioPago"]:checked'
            );


        if (
            !seleccionado
        ) {

            mostrarMensaje(
                'Seleccioná un medio de pago para continuar.',
                'error'
            );


            return;
        }


        const idMedioPago =
            Number(
                seleccionado.value
            );


        if (
            !Number.isInteger(
                idMedioPago
            )
            ||
            idMedioPago <= 0
        ) {

            sessionStorage.removeItem(
                STORAGE_KEY
            );


            mostrarMensaje(
                'El medio de pago seleccionado no es válido.',
                'error'
            );


            return;
        }


        procesando =
            true;


        continueButton.disabled =
            true;


        const textoOriginal =
            continueButton.textContent;


        continueButton.textContent =
            'Validando...';


        try {

            const response =
                await Auth.fetchAutenticado(
                    `${API_URL}/${idMedioPago}`,
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

                sessionStorage.removeItem(
                    STORAGE_KEY
                );


                await cargarMediosPago();


                mostrarMensaje(
                    'El medio de pago seleccionado ya no está disponible. Elegí otro para continuar.',
                    'error'
                );


                return;
            }


            if (
                !response.ok
            ) {

                throw new Error(
                    'No se pudo validar el medio de pago.'
                );
            }


            const medioPago =
                await response.json();


            if (
                !medioPago
                ||
                medioPago.activo !== true
                ||
                Number(
                    medioPago.idMedioPago
                )
                !==
                idMedioPago
            ) {

                sessionStorage.removeItem(
                    STORAGE_KEY
                );


                await cargarMediosPago();


                mostrarMensaje(
                    'El medio de pago seleccionado ya no está disponible.',
                    'error'
                );


                return;
            }


            sessionStorage.setItem(
                STORAGE_KEY,
                String(
                    medioPago.idMedioPago
                )
            );


            window.location.href =
                '/cliente/checkout-entrega.html';



        } catch (error) {

            console.error(
                'Error al validar medio de pago:',
                error
            );


            mostrarMensaje(
                error.message
                ||
                'No se pudo validar el medio de pago.',
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
    // ESTADOS
    // =========================================

    function mostrarCargando() {

        loading.classList.remove(
            'hidden'
        );


        errorState.classList.add(
            'hidden'
        );


        emptyState.classList.add(
            'hidden'
        );


        form.classList.add(
            'hidden'
        );
    }


    function mostrarErrorCarga() {

        loading.classList.add(
            'hidden'
        );


        emptyState.classList.add(
            'hidden'
        );


        form.classList.add(
            'hidden'
        );


        errorState.classList.remove(
            'hidden'
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