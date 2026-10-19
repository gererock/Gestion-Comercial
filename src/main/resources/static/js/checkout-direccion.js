(() => {

    'use strict';


    const API_DIRECCIONES =
        '/api/cliente/direcciones-envio';


    const API_CONFIGURACION_ENVIO =
        '/api/public/configuracion-comercio/envio';


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

    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );


    const botonCerrarSesion =
        document.getElementById(
            'botonCerrarSesion'
        );


    const mensaje =
        document.getElementById(
            'mensaje'
        );


    const cargando =
        document.getElementById(
            'cargando'
        );


    const errorCarga =
        document.getElementById(
            'errorCarga'
        );


    const botonReintentar =
        document.getElementById(
            'botonReintentar'
        );


    const contenidoDirecciones =
        document.getElementById(
            'contenidoDirecciones'
        );


    const sinDirecciones =
        document.getElementById(
            'sinDirecciones'
        );


    const listaDirecciones =
        document.getElementById(
            'listaDirecciones'
        );


    const botonMostrarFormulario =
        document.getElementById(
            'botonMostrarFormulario'
        );


    const botonCerrarFormulario =
        document.getElementById(
            'botonCerrarFormulario'
        );


    const formularioDireccion =
        document.getElementById(
            'formularioDireccion'
        );


    const calle =
        document.getElementById(
            'calle'
        );


    const numero =
        document.getElementById(
            'numero'
        );


    const pisoDepartamento =
        document.getElementById(
            'pisoDepartamento'
        );


    const localidad =
        document.getElementById(
            'localidad'
        );


    const referencia =
        document.getElementById(
            'referencia'
        );


    const opcionVillaMaria =
        document.getElementById(
            'opcionVillaMaria'
        );


    const opcionVillaNueva =
        document.getElementById(
            'opcionVillaNueva'
        );


    const errorCalle =
        document.getElementById(
            'errorCalle'
        );


    const errorNumero =
        document.getElementById(
            'errorNumero'
        );


    const errorPisoDepartamento =
        document.getElementById(
            'errorPisoDepartamento'
        );


    const errorLocalidad =
        document.getElementById(
            'errorLocalidad'
        );


    const botonCancelarDireccion =
        document.getElementById(
            'botonCancelarDireccion'
        );


    const botonGuardarDireccion =
        document.getElementById(
            'botonGuardarDireccion'
        );


    const botonContinuar =
        document.getElementById(
            'botonContinuar'
        );


    const cantidadProductos =
        document.getElementById(
            'cantidadProductos'
        );


    const totalCompra =
        document.getElementById(
            'totalCompra'
        );


    const resumenDireccion =
        document.getElementById(
            'resumenDireccion'
        );


    // =========================================
    // ESTADO
    // =========================================

    let direcciones =
        [];


    let configuracionEnvio =
        null;


    let guardando =
        false;


    let continuando =
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


        await cargarDatos();
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

            nombreUsuario.textContent =
                usuario.nombre;
        }
    }


    // =========================================
    // RESUMEN
    // =========================================

    function configurarResumen() {

        cantidadProductos.textContent =
            String(
                Carrito.obtenerCantidadTotal()
            );


        totalCompra.textContent =
            formatearPrecio(
                Carrito.obtenerSubtotal()
            );
    }


    // =========================================
    // EVENTOS
    // =========================================

    function configurarEventos() {

        botonCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        botonReintentar.addEventListener(
            'click',
            cargarDatos
        );


        botonMostrarFormulario.addEventListener(
            'click',
            mostrarFormulario
        );


        botonCerrarFormulario.addEventListener(
            'click',
            cerrarFormulario
        );


        botonCancelarDireccion.addEventListener(
            'click',
            cerrarFormulario
        );


        formularioDireccion.addEventListener(
            'submit',
            guardarDireccion
        );


        botonContinuar.addEventListener(
            'click',
            validarYContinuar
        );


        calle.addEventListener(
            'input',
            () => limpiarErrorCampo(
                calle,
                errorCalle
            )
        );


        numero.addEventListener(
            'input',
            () => limpiarErrorCampo(
                numero,
                errorNumero
            )
        );


        pisoDepartamento.addEventListener(
            'input',
            () => limpiarErrorCampo(
                pisoDepartamento,
                errorPisoDepartamento
            )
        );


        localidad.addEventListener(
            'change',
            () => limpiarErrorCampo(
                localidad,
                errorLocalidad
            )
        );
    }


    // =========================================
    // CARGA GENERAL
    // =========================================

    async function cargarDatos() {

        mostrarCargando();

        ocultarMensaje();


        try {

            await cargarConfiguracionEnvio();

            await cargarDirecciones();


            cargando.classList.add(
                'hidden'
            );


            errorCarga.classList.add(
                'hidden'
            );


            contenidoDirecciones.classList.remove(
                'hidden'
            );


        } catch (error) {

            console.error(
                'Error al cargar direcciones:',
                error
            );


            mostrarErrorCarga();
        }
    }


    // =========================================
    // CONFIGURACIÓN US62
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


        if (
            !data
            ||
            typeof data.permiteEnvioVillaMaria
            !== 'boolean'
            ||
            typeof data.permiteEnvioVillaNueva
            !== 'boolean'
        ) {

            throw new Error(
                'La configuración de envío no es válida.'
            );
        }


        configuracionEnvio =
            data;


        actualizarOpcionesLocalidad();
    }


    function actualizarOpcionesLocalidad() {

        opcionVillaMaria.disabled =
            !configuracionEnvio
                .permiteEnvioVillaMaria;


        opcionVillaNueva.disabled =
            !configuracionEnvio
                .permiteEnvioVillaNueva;


        if (
            localidad.value === 'VILLA_MARIA'
            &&
            opcionVillaMaria.disabled
        ) {

            localidad.value =
                '';
        }


        if (
            localidad.value === 'VILLA_NUEVA'
            &&
            opcionVillaNueva.disabled
        ) {

            localidad.value =
                '';
        }


        const ningunaHabilitada =
            !configuracionEnvio
                .permiteEnvioVillaMaria
            &&
            !configuracionEnvio
                .permiteEnvioVillaNueva;


        botonMostrarFormulario.disabled =
            ningunaHabilitada;


        if (
            ningunaHabilitada
        ) {

            botonMostrarFormulario.textContent =
                'No hay localidades habilitadas para envío';
        } else {

            botonMostrarFormulario.textContent =
                '+ Agregar nueva dirección';
        }
    }


    // =========================================
    // CARGAR DIRECCIONES
    // =========================================

    async function cargarDirecciones() {

        const response =
            await Auth.fetchAutenticado(
                API_DIRECCIONES,
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
                'No se pudieron cargar las direcciones.'
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


        direcciones =
            data;


        renderizarDirecciones();
    }


    // =========================================
    // RENDER
    // =========================================

    function renderizarDirecciones() {

        listaDirecciones.innerHTML =
            '';


        if (
            direcciones.length === 0
        ) {

            sinDirecciones.classList.remove(
                'hidden'
            );


            sessionStorage.removeItem(
                CLAVE_DIRECCION
            );


            actualizarResumenDireccion();


            return;
        }


        sinDirecciones.classList.add(
            'hidden'
        );


        direcciones.forEach(
            direccion => {

                listaDirecciones.appendChild(
                    crearOpcionDireccion(
                        direccion
                    )
                );
            }
        );


        restaurarSeleccion();
    }


    function crearOpcionDireccion(
        direccion
    ) {

        const label =
            document.createElement(
                'label'
            );


        label.className =
            'direccion-opcion';


        const habilitada =
            direccion
                .habilitadaParaEnvio === true;


        if (
            !habilitada
        ) {

            label.classList.add(
                'deshabilitada'
            );
        }


        const input =
            document.createElement(
                'input'
            );


        input.type =
            'radio';


        input.name =
            'direccionEnvio';


        input.value =
            String(
                direccion.idDireccionEnvio
            );


        input.disabled =
            !habilitada;


        const contenido =
            document.createElement(
                'div'
            );


        contenido.className =
            'direccion-contenido';


        const principal =
            document.createElement(
                'div'
            );


        principal.className =
            'direccion-principal';


        const titulo =
            document.createElement(
                'strong'
            );


        titulo.textContent =
            `${direccion.calle} ${direccion.numero}`;


        const estado =
            document.createElement(
                'span'
            );


        estado.className =
            habilitada
                ? 'estado-localidad habilitada'
                : 'estado-localidad no-habilitada';


        estado.textContent =
            habilitada
                ? 'Habilitada'
                : 'No disponible';


        principal.appendChild(
            titulo
        );


        principal.appendChild(
            estado
        );


        const localidadTexto =
            document.createElement(
                'p'
            );


        localidadTexto.className =
            'direccion-localidad';


        localidadTexto.textContent =
            formatearLocalidad(
                direccion.localidad
            );


        contenido.appendChild(
            principal
        );


        contenido.appendChild(
            localidadTexto
        );


        const detalles =
            [];


        if (
            direccion.pisoDepartamento
        ) {

            detalles.push(
                `Piso/Depto: ${direccion.pisoDepartamento}`
            );
        }


        if (
            direccion.referencia
        ) {

            detalles.push(
                `Referencia: ${direccion.referencia}`
            );
        }


        if (
            detalles.length > 0
        ) {

            const extra =
                document.createElement(
                    'p'
                );


            extra.className =
                'direccion-extra';


            extra.textContent =
                detalles.join(
                    ' · '
                );


            contenido.appendChild(
                extra
            );
        }


        label.appendChild(
            input
        );


        label.appendChild(
            contenido
        );


        if (
            habilitada
        ) {

            input.addEventListener(
                'change',
                () => {

                    seleccionarDireccion(
                        direccion.idDireccionEnvio
                    );
                }
            );
        }


        return label;
    }


    // =========================================
    // SELECCIÓN
    // =========================================

    function seleccionarDireccion(
        idDireccionEnvio
    ) {

        sessionStorage.setItem(
            CLAVE_DIRECCION,
            String(
                idDireccionEnvio
            )
        );


        actualizarSeleccionVisual();

        actualizarResumenDireccion();

        ocultarMensaje();
    }


    function restaurarSeleccion() {

        const valorGuardado =
            sessionStorage.getItem(
                CLAVE_DIRECCION
            );


        if (
            !valorGuardado
        ) {

            actualizarSeleccionVisual();

            actualizarResumenDireccion();

            return;
        }


        const idGuardado =
            Number(
                valorGuardado
            );


        const direccionGuardada =
            direcciones.find(
                direccion =>
                    Number(
                        direccion.idDireccionEnvio
                    )
                    ===
                    idGuardado
                    &&
                    direccion
                        .habilitadaParaEnvio === true
            );


        if (
            !direccionGuardada
        ) {

            sessionStorage.removeItem(
                CLAVE_DIRECCION
            );


            actualizarSeleccionVisual();

            actualizarResumenDireccion();

            return;
        }


        const input =
            listaDirecciones.querySelector(
                `input[name="direccionEnvio"][value="${idGuardado}"]`
            );


        if (
            input
        ) {

            input.checked =
                true;
        }


        actualizarSeleccionVisual();

        actualizarResumenDireccion();
    }


    function actualizarSeleccionVisual() {

        listaDirecciones
            .querySelectorAll(
                '.direccion-opcion'
            )
            .forEach(
                opcion => {

                    const input =
                        opcion.querySelector(
                            'input[type="radio"]'
                        );


                    opcion.classList.toggle(
                        'seleccionada',
                        Boolean(
                            input
                            &&
                            input.checked
                        )
                    );
                }
            );
    }


    function actualizarResumenDireccion() {

        const idDireccion =
            Number(
                sessionStorage.getItem(
                    CLAVE_DIRECCION
                )
            );


        const direccion =
            direcciones.find(
                item =>
                    Number(
                        item.idDireccionEnvio
                    )
                    ===
                    idDireccion
            );


        if (
            !direccion
        ) {

            resumenDireccion.textContent =
                'Sin seleccionar';


            return;
        }


        resumenDireccion.textContent =
            `${direccion.calle} ${direccion.numero}`;
    }


    // =========================================
    // FORMULARIO
    // =========================================

    function mostrarFormulario() {

        ocultarMensaje();

        limpiarErroresFormulario();


        formularioDireccion.classList.remove(
            'hidden'
        );


        botonMostrarFormulario.classList.add(
            'hidden'
        );


        calle.focus();
    }


    function cerrarFormulario() {

        formularioDireccion.reset();

        limpiarErroresFormulario();

        formularioDireccion.classList.add(
            'hidden'
        );


        botonMostrarFormulario.classList.remove(
            'hidden'
        );


        actualizarOpcionesLocalidad();
    }


    // =========================================
    // GUARDAR
    // =========================================

    async function guardarDireccion(
        event
    ) {

        event.preventDefault();


        if (
            guardando
        ) {

            return;
        }


        ocultarMensaje();

        limpiarErroresFormulario();


        if (
            !validarFormulario()
        ) {

            mostrarMensaje(
                'Revisá los datos de la dirección.',
                'error'
            );


            return;
        }


        const datos = {

            calle:
                calle.value.trim(),

            numero:
                numero.value.trim(),

            pisoDepartamento:
                obtenerValorOpcional(
                    pisoDepartamento.value
                ),

            localidad:
                localidad.value,

            referencia:
                obtenerValorOpcional(
                    referencia.value
                )
        };


        guardando =
            true;


        cambiarEstadoFormulario(
            true
        );


        const textoOriginal =
            botonGuardarDireccion.textContent;


        botonGuardarDireccion.textContent =
            'Guardando...';


        try {

            const response =
                await Auth.fetchAutenticado(
                    API_DIRECCIONES,
                    {
                        method:
                            'POST',

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
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            const nuevaDireccion =
                await response.json();


            direcciones.unshift(
                nuevaDireccion
            );


            sessionStorage.setItem(
                CLAVE_DIRECCION,
                String(
                    nuevaDireccion
                        .idDireccionEnvio
                )
            );


            cerrarFormulario();

            renderizarDirecciones();


            mostrarMensaje(
                'La dirección se guardó y quedó seleccionada.',
                'success'
            );


        } catch (error) {

            console.error(
                'Error al guardar dirección:',
                error
            );


            mostrarMensaje(
                error.message
                ||
                'No se pudo guardar la dirección.',
                'error'
            );


        } finally {

            guardando =
                false;


            cambiarEstadoFormulario(
                false
            );


            botonGuardarDireccion.textContent =
                textoOriginal;
        }
    }


    // =========================================
    // VALIDACIÓN FRONTEND
    // =========================================

    function validarFormulario() {

        let valido =
            true;


        const calleValor =
            calle.value
                .trim()
                .replaceAll(
                    /\s+/g,
                    ' '
                );


        if (
            calleValor === ''
        ) {

            mostrarErrorCampo(
                calle,
                errorCalle,
                'La calle es obligatoria.'
            );


            valido =
                false;


        } else if (
            !/[A-Za-zÁÉÍÓÚáéíóúÑñÜü]/.test(
                calleValor
            )
        ) {

            mostrarErrorCampo(
                calle,
                errorCalle,
                'La calle debe contener al menos una letra.'
            );


            valido =
                false;
        }


        const numeroValor =
            numero.value
                .trim()
                .toUpperCase();


        if (
            numeroValor === ''
        ) {

            mostrarErrorCampo(
                numero,
                errorNumero,
                'El número es obligatorio.'
            );


            valido =
                false;


        } else if (
            !/^(?:[1-9]\d*|S\/N)$/.test(
                numeroValor
            )
        ) {

            mostrarErrorCampo(
                numero,
                errorNumero,
                'Ingresá un número positivo o S/N.'
            );


            valido =
                false;
        }


        const pisoValor =
            pisoDepartamento.value
                .trim();


        if (
            pisoValor !== ''
            &&
            !/[A-Za-zÁÉÍÓÚáéíóúÑñÜü0-9]/.test(
                pisoValor
            )
        ) {

            mostrarErrorCampo(
                pisoDepartamento,
                errorPisoDepartamento,
                'El piso o departamento tiene un formato inválido.'
            );


            valido =
                false;
        }


        if (
            localidad.value === ''
        ) {

            mostrarErrorCampo(
                localidad,
                errorLocalidad,
                'Seleccioná una localidad.'
            );


            valido =
                false;


        } else if (
            localidad.value === 'VILLA_MARIA'
            &&
            !configuracionEnvio
                .permiteEnvioVillaMaria
        ) {

            mostrarErrorCampo(
                localidad,
                errorLocalidad,
                'Villa María no está habilitada para envío.'
            );


            valido =
                false;


        } else if (
            localidad.value === 'VILLA_NUEVA'
            &&
            !configuracionEnvio
                .permiteEnvioVillaNueva
        ) {

            mostrarErrorCampo(
                localidad,
                errorLocalidad,
                'Villa Nueva no está habilitada para envío.'
            );


            valido =
                false;
        }


        return valido;
    }


    // =========================================
    // CONTINUAR
    // =========================================

    async function validarYContinuar() {

        if (
            continuando
        ) {

            return;
        }


        ocultarMensaje();


        const valor =
            sessionStorage.getItem(
                CLAVE_DIRECCION
            );


        const idDireccionEnvio =
            Number(
                valor
            );


        if (
            !valor
            ||
            !Number.isInteger(
                idDireccionEnvio
            )
            ||
            idDireccionEnvio <= 0
        ) {

            mostrarMensaje(
                'Seleccioná o registrá una dirección para continuar.',
                'error'
            );


            return;
        }


        continuando =
            true;


        botonContinuar.disabled =
            true;


        const textoOriginal =
            botonContinuar.textContent;


        botonContinuar.textContent =
            'Validando...';


        try {

            const response =
                await Auth.fetchAutenticado(
                    `${API_DIRECCIONES}/${idDireccionEnvio}`,
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
                !response.ok
            ) {

                sessionStorage.removeItem(
                    CLAVE_DIRECCION
                );


                await cargarConfiguracionEnvio();

                await cargarDirecciones();


                throw new Error(
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            const direccion =
                await response.json();


            if (
                !direccion
                ||
                Number(
                    direccion.idDireccionEnvio
                )
                !==
                idDireccionEnvio
                ||
                direccion
                    .habilitadaParaEnvio
                !== true
            ) {

                sessionStorage.removeItem(
                    CLAVE_DIRECCION
                );


                await cargarDirecciones();


                throw new Error(
                    'La dirección seleccionada ya no está disponible para envío.'
                );
            }


            sessionStorage.setItem(
                CLAVE_DIRECCION,
                String(
                    direccion
                        .idDireccionEnvio
                )
            );


            mostrarMensaje(
                'La dirección quedó seleccionada correctamente.',
                'success'
            );


            /*
             * Cuando US12 / US10 estén integradas,
             * acá se redirigirá al siguiente paso
             * del checkout.
             *
             * Por ahora dejamos la dirección
             * correctamente validada y guardada
             * en sessionStorage:
             *
             * checkout_direccion_envio_id
             */


        } catch (error) {

            console.error(
                'Error al validar dirección:',
                error
            );


            mostrarMensaje(
                error.message
                ||
                'No se pudo validar la dirección.',
                'error'
            );


        } finally {

            continuando =
                false;


            botonContinuar.disabled =
                false;


            botonContinuar.textContent =
                textoOriginal;
        }
    }


    // =========================================
    // ESTADOS
    // =========================================

    function mostrarCargando() {

        cargando.classList.remove(
            'hidden'
        );


        errorCarga.classList.add(
            'hidden'
        );


        contenidoDirecciones.classList.add(
            'hidden'
        );
    }


    function mostrarErrorCarga() {

        cargando.classList.add(
            'hidden'
        );


        contenidoDirecciones.classList.add(
            'hidden'
        );


        errorCarga.classList.remove(
            'hidden'
        );
    }


    function cambiarEstadoFormulario(
        deshabilitado
    ) {

        calle.disabled =
            deshabilitado;

        numero.disabled =
            deshabilitado;

        pisoDepartamento.disabled =
            deshabilitado;

        localidad.disabled =
            deshabilitado;

        referencia.disabled =
            deshabilitado;

        botonCancelarDireccion.disabled =
            deshabilitado;

        botonCerrarFormulario.disabled =
            deshabilitado;

        botonGuardarDireccion.disabled =
            deshabilitado;
    }


    // =========================================
    // ERRORES DE CAMPOS
    // =========================================

    function mostrarErrorCampo(
        elemento,
        elementoError,
        texto
    ) {

        elemento.classList.add(
            'campo-invalido'
        );


        elementoError.textContent =
            texto;
    }


    function limpiarErrorCampo(
        elemento,
        elementoError
    ) {

        elemento.classList.remove(
            'campo-invalido'
        );


        elementoError.textContent =
            '';
    }


    function limpiarErroresFormulario() {

        limpiarErrorCampo(
            calle,
            errorCalle
        );


        limpiarErrorCampo(
            numero,
            errorNumero
        );


        limpiarErrorCampo(
            pisoDepartamento,
            errorPisoDepartamento
        );


        limpiarErrorCampo(
            localidad,
            errorLocalidad
        );
    }


    // =========================================
    // MENSAJES
    // =========================================

    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensaje.textContent =
            texto;


        mensaje.classList.remove(
            'hidden',
            'checkout-message--error',
            'checkout-message--success'
        );


        mensaje.classList.add(
            tipo === 'success'
                ? 'checkout-message--success'
                : 'checkout-message--error'
        );
    }


    function ocultarMensaje() {

        mensaje.textContent =
            '';


        mensaje.classList.add(
            'hidden'
        );


        mensaje.classList.remove(
            'checkout-message--error',
            'checkout-message--success'
        );
    }


    // =========================================
    // ERROR BACKEND
    // =========================================

    async function obtenerMensajeError(
        response
    ) {

        try {

            const data =
                await response.json();


            if (
                data.errors
                &&
                typeof data.errors === 'object'
            ) {

                const errores =
                    Object.values(
                        data.errors
                    );


                if (
                    errores.length > 0
                ) {

                    return String(
                        errores[0]
                    );
                }
            }


            if (
                typeof data.message === 'string'
                &&
                data.message.trim() !== ''
            ) {

                return data.message.trim();
            }


        } catch (error) {

            // No se pudo leer JSON.
        }


        return 'No se pudo procesar la solicitud.';
    }


    // =========================================
    // UTILIDADES
    // =========================================

    function obtenerValorOpcional(
        valor
    ) {

        const normalizado =
            valor
                .trim()
                .replaceAll(
                    /\s+/g,
                    ' '
                );


        return normalizado === ''
            ? null
            : normalizado;
    }


    function formatearLocalidad(
        valor
    ) {

        switch (
        valor
        ) {

            case 'VILLA_MARIA':

                return 'Villa María';


            case 'VILLA_NUEVA':

                return 'Villa Nueva';


            default:

                return valor || '';
        }
    }


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