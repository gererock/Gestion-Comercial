(() => {

    'use strict';


    const API_URL =
        '/api/medios-pago';


    // =====================================================
    // SEGURIDAD
    // =====================================================

    if (
        !Auth.requerirRol(
            'ADMINISTRADOR'
        )
    ) {

        return;
    }


    // =====================================================
    // ELEMENTOS
    // =====================================================

    const tablaMediosPago =
        document.getElementById(
            'tablaMediosPago'
        );


    const contenedorTabla =
        document.getElementById(
            'contenedorTabla'
        );


    const sinResultados =
        document.getElementById(
            'sinResultados'
        );


    const cargando =
        document.getElementById(
            'cargando'
        );


    const cantidadMediosPago =
        document.getElementById(
            'cantidadMediosPago'
        );


    const busqueda =
        document.getElementById(
            'busqueda'
        );


    const filtroEstado =
        document.getElementById(
            'filtroEstado'
        );


    const btnBuscar =
        document.getElementById(
            'btnBuscar'
        );


    const btnLimpiar =
        document.getElementById(
            'btnLimpiar'
        );


    const btnNuevoMedioPago =
        document.getElementById(
            'btnNuevoMedioPago'
        );


    const seccionFormulario =
        document.getElementById(
            'seccionFormulario'
        );


    const formMedioPago =
        document.getElementById(
            'formMedioPago'
        );


    const medioPagoId =
        document.getElementById(
            'medioPagoId'
        );


    const nombre =
        document.getElementById(
            'nombre'
        );


    const errorNombre =
        document.getElementById(
            'errorNombre'
        );


    const tituloFormulario =
        document.getElementById(
            'tituloFormulario'
        );


    const textoFormulario =
        document.getElementById(
            'textoFormulario'
        );


    const btnGuardar =
        document.getElementById(
            'btnGuardar'
        );


    const btnCancelar =
        document.getElementById(
            'btnCancelar'
        );


    const btnCerrarFormulario =
        document.getElementById(
            'btnCerrarFormulario'
        );


    const mensaje =
        document.getElementById(
            'mensaje'
        );


    const modalConfirmacion =
        document.getElementById(
            'modalConfirmacion'
        );


    const modalTitulo =
        document.getElementById(
            'modalTitulo'
        );


    const modalTexto =
        document.getElementById(
            'modalTexto'
        );


    const btnModalCancelar =
        document.getElementById(
            'btnModalCancelar'
        );


    const btnModalConfirmar =
        document.getElementById(
            'btnModalConfirmar'
        );


    const btnCerrarSesion =
        document.getElementById(
            'btnCerrarSesion'
        );


    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );


    // =====================================================
    // VARIABLES
    // =====================================================

    let mediosPago =
        [];


    let accionConfirmacion =
        null;


    let guardando =
        false;


    // =====================================================
    // INICIO
    // =====================================================

    iniciar();


    function iniciar() {

        configurarUsuario();

        configurarEventos();

        cargarMediosPago();
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


    // =====================================================
    // EVENTOS
    // =====================================================

    function configurarEventos() {

        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        btnNuevoMedioPago.addEventListener(
            'click',
            abrirNuevo
        );


        btnCerrarFormulario.addEventListener(
            'click',
            cerrarFormulario
        );


        btnCancelar.addEventListener(
            'click',
            cerrarFormulario
        );


        formMedioPago.addEventListener(
            'submit',
            guardarMedioPago
        );


        btnBuscar.addEventListener(
            'click',
            buscar
        );


        btnLimpiar.addEventListener(
            'click',
            limpiarFiltros
        );


        busqueda.addEventListener(
            'keydown',
            event => {

                if (
                    event.key === 'Enter'
                ) {

                    event.preventDefault();

                    buscar();
                }
            }
        );


        busqueda.addEventListener(
            'input',
            () => {

                ocultarErrorNombreBusqueda();
            }
        );


        nombre.addEventListener(
            'input',
            () => {

                ocultarErrorNombre();
            }
        );


        btnModalCancelar.addEventListener(
            'click',
            cerrarModal
        );


        btnModalConfirmar.addEventListener(
            'click',
            confirmarAccion
        );


        modalConfirmacion
            .querySelector(
                '.modal-fondo'
            )
            .addEventListener(
                'click',
                cerrarModal
            );
    }


    // =====================================================
    // CARGAR LISTADO
    // =====================================================

    async function cargarMediosPago() {

        mostrarCargando();

        ocultarMensaje();


        try {

            const url =
                construirUrlBusqueda();


            const response =
                await Auth.fetchAutenticado(
                    url,
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

                window.location.href =
                    Auth.obtenerRutaPorRol(
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


            mediosPago =
                await response.json();


            if (
                !Array.isArray(
                    mediosPago
                )
            ) {

                throw new Error(
                    'La respuesta del servidor no es válida.'
                );
            }


            renderizarListado();


        } catch (error) {

            console.error(
                'Error al cargar medios de pago:',
                error
            );


            mediosPago =
                [];


            renderizarListado();


            mostrarMensaje(
                error.message
                ||
                'No se pudieron cargar los medios de pago.',
                'error'
            );
        }

    }


    function construirUrlBusqueda() {

        const parametros =
            new URLSearchParams();


        const texto =
            busqueda.value.trim();


        const estado =
            filtroEstado.value;


        if (
            texto !== ''
        ) {

            parametros.set(
                'nombre',
                texto
            );
        }


        if (
            estado !== ''
        ) {

            parametros.set(
                'activo',
                estado
            );
        }


        const query =
            parametros.toString();


        return query
            ? `${API_URL}?${query}`
            : API_URL;
    }


    // =====================================================
    // BUSCAR
    // =====================================================

    function buscar() {

        ocultarMensaje();


        const valorOriginal =
            busqueda.value;


        const valor =
            valorOriginal.trim();


        if (
            valorOriginal !== ''
            &&
            valor === ''
        ) {

            mostrarMensaje(
                'La búsqueda no puede contener solo espacios.',
                'error'
            );


            return;
        }


        if (
            valor.length > 80
        ) {

            mostrarMensaje(
                'La búsqueda no puede superar los 80 caracteres.',
                'error'
            );


            return;
        }


        if (
            valor !== ''
            &&
            !/\p{L}/u.test(
                valor
            )
        ) {

            mostrarMensaje(
                'La búsqueda debe contener al menos una letra.',
                'error'
            );


            return;
        }


        busqueda.value =
            valor;


        cargarMediosPago();
    }


    function limpiarFiltros() {

        busqueda.value =
            '';


        filtroEstado.value =
            '';


        ocultarMensaje();


        cargarMediosPago();
    }


    function ocultarErrorNombreBusqueda() {

        ocultarMensaje();
    }


    // =====================================================
    // RENDER LISTADO
    // =====================================================

    function renderizarListado() {

        ocultarCargando();


        tablaMediosPago.innerHTML =
            '';


        cantidadMediosPago.textContent =
            crearTextoCantidad(
                mediosPago.length
            );


        if (
            mediosPago.length === 0
        ) {

            contenedorTabla.classList.add(
                'oculto'
            );


            sinResultados.classList.remove(
                'oculto'
            );


            return;
        }


        sinResultados.classList.add(
            'oculto'
        );


        contenedorTabla.classList.remove(
            'oculto'
        );


        mediosPago.forEach(
            medioPago => {

                tablaMediosPago.appendChild(
                    crearFila(
                        medioPago
                    )
                );
            }
        );
    }


    function crearTextoCantidad(
        cantidad
    ) {

        if (
            cantidad === 0
        ) {

            return 'No hay medios de pago para mostrar';
        }


        if (
            cantidad === 1
        ) {

            return '1 medio de pago';
        }


        return `${cantidad} medios de pago`;
    }


    function crearFila(
        medioPago
    ) {

        const fila =
            document.createElement(
                'tr'
            );


        // NOMBRE

        const celdaNombre =
            document.createElement(
                'td'
            );


        celdaNombre.textContent =
            medioPago.nombre;


        // ESTADO

        const celdaEstado =
            document.createElement(
                'td'
            );


        const estado =
            document.createElement(
                'span'
            );


        estado.className =
            medioPago.activo
                ? 'estado estado-activo'
                : 'estado estado-inactivo';


        estado.textContent =
            medioPago.activo
                ? 'Activo'
                : 'Inactivo';


        celdaEstado.appendChild(
            estado
        );


        // ACCIONES

        const celdaAcciones =
            document.createElement(
                'td'
            );


        celdaAcciones.className =
            'columna-acciones';


        const acciones =
            document.createElement(
                'div'
            );


        acciones.className =
            'acciones-tabla';


        const btnEditar =
            document.createElement(
                'button'
            );


        btnEditar.type =
            'button';


        btnEditar.className =
            'btn-tabla';


        btnEditar.textContent =
            'Editar';


        btnEditar.addEventListener(
            'click',
            () => {

                abrirEdicion(
                    medioPago
                );
            }
        );


        const btnEstado =
            document.createElement(
                'button'
            );


        btnEstado.type =
            'button';


        btnEstado.className =
            medioPago.activo
                ? 'btn-tabla estado'
                : 'btn-tabla estado activar';


        btnEstado.textContent =
            medioPago.activo
                ? 'Desactivar'
                : 'Activar';


        btnEstado.addEventListener(
            'click',
            () => {

                solicitarCambioEstado(
                    medioPago
                );
            }
        );


        acciones.appendChild(
            btnEditar
        );


        acciones.appendChild(
            btnEstado
        );


        celdaAcciones.appendChild(
            acciones
        );


        fila.appendChild(
            celdaNombre
        );


        fila.appendChild(
            celdaEstado
        );


        fila.appendChild(
            celdaAcciones
        );


        return fila;
    }


    // =====================================================
    // NUEVO
    // =====================================================

    function abrirNuevo() {

        ocultarMensaje();

        limpiarFormulario();


        tituloFormulario.textContent =
            'Nuevo medio de pago';


        textoFormulario.textContent =
            'Ingresá el nombre del nuevo medio de pago.';


        btnGuardar.textContent =
            'Guardar medio de pago';


        seccionFormulario.classList.remove(
            'oculto'
        );


        nombre.focus();


        seccionFormulario.scrollIntoView({
            behavior:
                'smooth',

            block:
                'start'
        });
    }


    // =====================================================
    // EDITAR
    // =====================================================

    function abrirEdicion(
        medioPago
    ) {

        ocultarMensaje();

        limpiarFormulario();


        medioPagoId.value =
            medioPago.idMedioPago;


        nombre.value =
            medioPago.nombre;


        tituloFormulario.textContent =
            'Modificar medio de pago';


        textoFormulario.textContent =
            'Modificá el nombre del medio de pago seleccionado.';


        btnGuardar.textContent =
            'Guardar cambios';


        seccionFormulario.classList.remove(
            'oculto'
        );


        nombre.focus();


        seccionFormulario.scrollIntoView({
            behavior:
                'smooth',

            block:
                'start'
        });
    }


    // =====================================================
    // GUARDAR
    // =====================================================

    async function guardarMedioPago(
        event
    ) {

        event.preventDefault();


        if (
            guardando
        ) {

            return;
        }


        ocultarMensaje();

        ocultarErrorNombre();


        const nombreValidado =
            validarNombre();


        if (
            nombreValidado === null
        ) {

            return;
        }


        const id =
            medioPagoId.value.trim();


        const esEdicion =
            id !== '';


        guardando =
            true;


        btnGuardar.disabled =
            true;


        const textoBotonOriginal =
            btnGuardar.textContent;


        btnGuardar.textContent =
            'Guardando...';


        try {

            const response =
                await Auth.fetchAutenticado(

                    esEdicion
                        ? `${API_URL}/${id}`
                        : API_URL,

                    {
                        method:
                            esEdicion
                                ? 'PUT'
                                : 'POST',

                        headers: {
                            'Content-Type':
                                'application/json',

                            'Accept':
                                'application/json'
                        },

                        body:
                            JSON.stringify({
                                nombre:
                                    nombreValidado
                            })
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


            cerrarFormulario();


            mostrarMensaje(
                esEdicion
                    ? 'El medio de pago se modificó correctamente.'
                    : 'El medio de pago se creó correctamente.',
                'exito'
            );


            await cargarMediosPago();


        } catch (error) {

            console.error(
                'Error al guardar medio de pago:',
                error
            );


            mostrarMensaje(
                error.message
                ||
                'No se pudo guardar el medio de pago.',
                'error'
            );


        } finally {

            guardando =
                false;


            btnGuardar.disabled =
                false;


            btnGuardar.textContent =
                textoBotonOriginal;
        }

    }


    // =====================================================
    // VALIDACIÓN
    // =====================================================

    function validarNombre() {

        const valor =
            nombre.value
                .trim()
                .replace(
                    /\s+/g,
                    ' '
                );


        if (
            valor === ''
        ) {

            mostrarErrorNombre(
                'El nombre del medio de pago es obligatorio.'
            );


            return null;
        }


        if (
            valor.length > 80
        ) {

            mostrarErrorNombre(
                'El nombre no puede superar los 80 caracteres.'
            );


            return null;
        }


        if (
            !/\p{L}/u.test(
                valor
            )
        ) {

            mostrarErrorNombre(
                'El nombre debe contener al menos una letra.'
            );


            return null;
        }


        if (
            !/^[\p{L}\p{N}\s°ºª.\-/]+$/u.test(
                valor
            )
        ) {

            mostrarErrorNombre(
                'El nombre contiene símbolos inválidos.'
            );


            return null;
        }


        nombre.value =
            valor;


        return valor;
    }


    function mostrarErrorNombre(
        texto
    ) {

        errorNombre.textContent =
            texto;


        errorNombre.classList.remove(
            'oculto'
        );


        nombre.focus();
    }


    function ocultarErrorNombre() {

        errorNombre.textContent =
            '';


        errorNombre.classList.add(
            'oculto'
        );
    }


    // =====================================================
    // CERRAR FORMULARIO
    // =====================================================

    function cerrarFormulario() {

        limpiarFormulario();


        seccionFormulario.classList.add(
            'oculto'
        );
    }


    function limpiarFormulario() {

        formMedioPago.reset();


        medioPagoId.value =
            '';


        ocultarErrorNombre();
    }


    // =====================================================
    // CAMBIO DE ESTADO
    // =====================================================

    function solicitarCambioEstado(
        medioPago
    ) {

        const nuevoEstado =
            !medioPago.activo;


        modalTitulo.textContent =
            nuevoEstado
                ? 'Activar medio de pago'
                : 'Desactivar medio de pago';


        modalTexto.textContent =
            nuevoEstado
                ? `¿Querés activar "${medioPago.nombre}"?`
                : `¿Querés desactivar "${medioPago.nombre}"?`;


        btnModalConfirmar.textContent =
            nuevoEstado
                ? 'Activar'
                : 'Desactivar';


        btnModalConfirmar.className =
            nuevoEstado
                ? 'btn btn-success'
                : 'btn btn-danger';


        accionConfirmacion =
            async () => {

                await cambiarEstado(
                    medioPago.idMedioPago,
                    nuevoEstado
                );
            };


        modalConfirmacion.classList.remove(
            'oculto'
        );
    }


    async function cambiarEstado(
        idMedioPago,
        activar
    ) {

        cerrarModal();


        try {

            const accion =
                activar
                    ? 'activar'
                    : 'desactivar';


            const response =
                await Auth.fetchAutenticado(
                    `${API_URL}/${idMedioPago}/${accion}`,
                    {
                        method:
                            'PATCH',

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

                throw new Error(
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            mostrarMensaje(
                activar
                    ? 'El medio de pago se activó correctamente.'
                    : 'El medio de pago se desactivó correctamente.',
                'exito'
            );


            await cargarMediosPago();


        } catch (error) {

            console.error(
                'Error al cambiar estado:',
                error
            );


            mostrarMensaje(
                error.message
                ||
                'No se pudo modificar el estado.',
                'error'
            );
        }

    }


    // =====================================================
    // MODAL
    // =====================================================

    async function confirmarAccion() {

        if (
            !accionConfirmacion
        ) {

            cerrarModal();

            return;
        }


        const accion =
            accionConfirmacion;


        accionConfirmacion =
            null;


        await accion();
    }


    function cerrarModal() {

        modalConfirmacion.classList.add(
            'oculto'
        );


        accionConfirmacion =
            null;
    }


    // =====================================================
    // CARGANDO
    // =====================================================

    function mostrarCargando() {

        cargando.classList.remove(
            'oculto'
        );


        contenedorTabla.classList.add(
            'oculto'
        );


        sinResultados.classList.add(
            'oculto'
        );


        cantidadMediosPago.textContent =
            'Cargando medios de pago...';
    }


    function ocultarCargando() {

        cargando.classList.add(
            'oculto'
        );
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
                body.fieldErrors
            ) {

                const primerError =
                    Object.values(
                        body.fieldErrors
                    )[0];


                if (
                    primerError
                ) {

                    return primerError;
                }
            }


            if (
                typeof body.message === 'string'
                &&
                body.message.trim() !== ''
            ) {

                return body.message.trim();
            }


        } catch (error) {

            // Se utiliza mensaje genérico.
        }


        if (
            response.status === 403
        ) {

            return 'No tenés permisos para realizar esta acción.';
        }


        if (
            response.status === 404
        ) {

            return 'El medio de pago no existe.';
        }


        if (
            response.status === 409
        ) {

            return 'Ya existe un medio de pago con ese nombre.';
        }


        return 'Ocurrió un error al procesar la solicitud.';
    }

})();