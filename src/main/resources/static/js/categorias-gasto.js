(() => {

    'use strict';

    const API_URL =
        '/api/admin/categorias-gasto';


    const tablaCategorias =
        document.getElementById('tablaCategorias');

    const contenedorTabla =
        document.getElementById('contenedorTabla');

    const sinResultados =
        document.getElementById('sinResultados');

    const cargando =
        document.getElementById('cargando');

    const cantidadCategorias =
        document.getElementById('cantidadCategorias');


    const busqueda =
        document.getElementById('busqueda');

    const filtroEstado =
        document.getElementById('filtroEstado');

    const btnBuscar =
        document.getElementById('btnBuscar');

    const btnLimpiar =
        document.getElementById('btnLimpiar');


    const seccionFormulario =
        document.getElementById('seccionFormulario');

    const formCategoria =
        document.getElementById('formCategoria');

    const categoriaId =
        document.getElementById('categoriaId');

    const nombre =
        document.getElementById('nombre');

    const descripcion =
        document.getElementById('descripcion');

    const tituloFormulario =
        document.getElementById('tituloFormulario');

    const textoFormulario =
        document.getElementById('textoFormulario');

    const btnGuardar =
        document.getElementById('btnGuardar');

    const btnCancelar =
        document.getElementById('btnCancelar');

    const btnCerrarFormulario =
        document.getElementById('btnCerrarFormulario');

    const btnNuevaCategoria =
        document.getElementById('btnNuevaCategoria');

    const contadorDescripcion =
        document.getElementById('contadorDescripcion');


    const mensaje =
        document.getElementById('mensaje');


    const modalConfirmacion =
        document.getElementById('modalConfirmacion');

    const modalTitulo =
        document.getElementById('modalTitulo');

    const modalTexto =
        document.getElementById('modalTexto');

    const btnModalCancelar =
        document.getElementById('btnModalCancelar');

    const btnModalConfirmar =
        document.getElementById('btnModalConfirmar');


    const btnCerrarSesion =
        document.getElementById('btnCerrarSesion');

    const nombreUsuario =
        document.getElementById('nombreUsuario');


    let categorias = [];

    let accionConfirmacion = null;

    let guardando = false;


    document.addEventListener(
        'DOMContentLoaded',
        iniciar
    );


    async function iniciar() {

        if (
            !Auth.requerirRol(
                'ADMINISTRADOR'
            )
        ) {
            return;
        }

        cargarDatosUsuario();

        registrarEventos();

        await cargarCategorias();
    }


    function registrarEventos() {

        btnNuevaCategoria.addEventListener(
            'click',
            abrirFormularioCreacion
        );


        btnCancelar.addEventListener(
            'click',
            cerrarFormulario
        );


        btnCerrarFormulario.addEventListener(
            'click',
            cerrarFormulario
        );


        formCategoria.addEventListener(
            'submit',
            guardarCategoria
        );


        descripcion.addEventListener(
            'input',
            actualizarContador
        );


        btnBuscar.addEventListener(
            'click',
            buscarCategorias
        );


        busqueda.addEventListener(
            'keydown',
            event => {

                if (
                    event.key === 'Enter'
                ) {

                    event.preventDefault();

                    buscarCategorias();
                }
            }
        );


        filtroEstado.addEventListener(
            'change',
            cargarCategorias
        );


        btnLimpiar.addEventListener(
            'click',
            limpiarFiltros
        );


        tablaCategorias.addEventListener(
            'click',
            manejarAccionTabla
        );


        btnModalCancelar.addEventListener(
            'click',
            cerrarModal
        );


        btnModalConfirmar.addEventListener(
            'click',
            confirmarAccion
        );


        modalConfirmacion.addEventListener(
            'click',
            event => {

                if (
                    event.target.classList.contains(
                        'modal-fondo'
                    )
                ) {

                    cerrarModal();
                }
            }
        );


        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );
    }


    function cargarDatosUsuario() {

        const usuario =
            Auth.obtenerUsuario();


        if (
            !usuario
        ) {
            return;
        }


        nombreUsuario.textContent =
            usuario.apellido
                ? `${usuario.nombre} ${usuario.apellido}`
                : usuario.nombre;
    }


    async function cargarCategorias(
        preservarMensaje = false
    ) {

        mostrarCargando(
            true
        );


        if (
            !preservarMensaje
        ) {

            ocultarMensaje();
        }


        try {

            const parametros =
                new URLSearchParams();


            const textoBusqueda =
                busqueda.value;


            const estadoSeleccionado =
                filtroEstado.value;


            if (
                textoBusqueda !== ''
            ) {

                parametros.append(
                    'nombre',
                    textoBusqueda
                );
            }


            if (
                estadoSeleccionado !== ''
            ) {

                parametros.append(
                    'activa',
                    estadoSeleccionado
                );
            }


            let url =
                API_URL;


            if (
                parametros.toString()
            ) {

                url +=
                    `?${parametros.toString()}`;
            }


            const response =
                await Auth.fetchAutenticado(
                    url,
                    {
                        method: 'GET'
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

                mostrarMensaje(
                    'No tenés permisos para consultar las categorías de gasto.',
                    'error'
                );

                categorias = [];

                mostrarCategorias();

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


            categorias =
                await response.json();


            mostrarCategorias();

        } catch (error) {

            console.error(
                error
            );


            categorias = [];

            mostrarCategorias();


            mostrarMensaje(
                error.message ||
                'No se pudieron cargar las categorías de gasto.',
                'error'
            );

        } finally {

            mostrarCargando(
                false
            );
        }
    }


    function mostrarCategorias() {

        tablaCategorias.innerHTML =
            '';


        cantidadCategorias.textContent =
            `${categorias.length} categoría${categorias.length !== 1
                ? 's'
                : ''
            }`;


        if (
            categorias.length === 0
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


        categorias.forEach(
            categoria => {

                const fila =
                    document.createElement(
                        'tr'
                    );


                const descripcionTexto =
                    categoria.descripcion
                        ? escaparHtml(
                            categoria.descripcion
                        )
                        : 'Sin descripción';


                fila.innerHTML = `
                    <td>
                        <span class="nombre-categoria">
                            ${escaparHtml(categoria.nombre)}
                        </span>
                    </td>

                    <td>
                        <span class="descripcion-categoria">
                            ${descripcionTexto}
                        </span>
                    </td>

                    <td>
                        ${categoria.activa
                        ? `
                                    <span class="estado estado-activa">
                                        Activa
                                    </span>
                                  `
                        : `
                                    <span class="estado estado-inactiva">
                                        Inactiva
                                    </span>
                                  `
                    }
                    </td>

                    <td>
                        <div class="acciones-tabla">

                            <button
                                type="button"
                                class="btn-tabla btn-editar"
                                data-accion="editar"
                                data-id="${categoria.idCategoriaGasto}"
                            >
                                Editar
                            </button>

                            ${categoria.activa
                        ? `
                                        <button
                                            type="button"
                                            class="btn-tabla btn-desactivar"
                                            data-accion="desactivar"
                                            data-id="${categoria.idCategoriaGasto}"
                                        >
                                            Desactivar
                                        </button>
                                      `
                        : `
                                        <button
                                            type="button"
                                            class="btn-tabla btn-activar"
                                            data-accion="activar"
                                            data-id="${categoria.idCategoriaGasto}"
                                        >
                                            Activar
                                        </button>
                                      `
                    }

                        </div>
                    </td>
                `;


                tablaCategorias.appendChild(
                    fila
                );
            }
        );
    }


    function manejarAccionTabla(
        event
    ) {

        const boton =
            event.target.closest(
                'button[data-accion]'
            );


        if (
            !boton
        ) {
            return;
        }


        const id =
            Number(
                boton.dataset.id
            );


        const accion =
            boton.dataset.accion;


        if (
            !Number.isInteger(id)
            ||
            id <= 0
        ) {

            mostrarMensaje(
                'La categoría indicada no es válida.',
                'error'
            );

            return;
        }


        if (
            accion === 'editar'
        ) {

            editarCategoria(
                id
            );

            return;
        }


        if (
            accion === 'activar'
        ) {

            solicitarCambioEstado(
                id,
                true
            );

            return;
        }


        if (
            accion === 'desactivar'
        ) {

            solicitarCambioEstado(
                id,
                false
            );
        }
    }


    function abrirFormularioCreacion() {

        limpiarFormulario();


        tituloFormulario.textContent =
            'Nueva categoría de gasto';


        textoFormulario.textContent =
            'Completá los datos de la nueva categoría.';


        btnGuardar.textContent =
            'Guardar categoría';


        seccionFormulario.classList.remove(
            'oculto'
        );


        nombre.focus();


        seccionFormulario.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }


    function editarCategoria(
        id
    ) {

        const categoria =
            categorias.find(
                item =>
                    item.idCategoriaGasto === id
            );


        if (
            !categoria
        ) {

            mostrarMensaje(
                'No se encontró la categoría de gasto.',
                'error'
            );

            return;
        }


        categoriaId.value =
            categoria.idCategoriaGasto;


        nombre.value =
            categoria.nombre;


        descripcion.value =
            categoria.descripcion || '';


        tituloFormulario.textContent =
            'Editar categoría de gasto';


        textoFormulario.textContent =
            'Modificá el nombre o la descripción de la categoría.';


        btnGuardar.textContent =
            'Guardar cambios';


        actualizarContador();


        seccionFormulario.classList.remove(
            'oculto'
        );


        nombre.focus();


        seccionFormulario.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }


    async function guardarCategoria(
        event
    ) {

        event.preventDefault();


        if (
            guardando
        ) {
            return;
        }


        ocultarMensaje();


        const nombreCategoria =
            nombre.value.trim();


        const descripcionCategoria =
            descripcion.value.trim();


        if (
            !nombreCategoria
        ) {

            mostrarMensaje(
                'El nombre es obligatorio.',
                'error'
            );

            nombre.focus();

            return;
        }


        if (
            nombreCategoria.length > 100
        ) {

            mostrarMensaje(
                'El nombre no puede superar los 100 caracteres.',
                'error'
            );

            nombre.focus();

            return;
        }


        const contieneLetra =
            /\p{L}/u.test(
                nombreCategoria
            );


        if (
            !contieneLetra
        ) {

            mostrarMensaje(
                'El nombre debe contener al menos una letra.',
                'error'
            );

            nombre.focus();

            return;
        }


        if (
            descripcionCategoria.length > 500
        ) {

            mostrarMensaje(
                'La descripción no puede superar los 500 caracteres.',
                'error'
            );

            descripcion.focus();

            return;
        }


        const datos = {

            nombre:
                nombreCategoria,

            descripcion:
                descripcionCategoria
                    ? descripcionCategoria
                    : null
        };


        const id =
            categoriaId.value;


        try {

            guardando =
                true;


            bloquearFormulario(
                true
            );


            let response;


            if (
                id
            ) {

                response =
                    await Auth.fetchAutenticado(
                        `${API_URL}/${id}`,
                        {
                            method: 'PUT',
                            headers: {
                                'Content-Type':
                                    'application/json'
                            },
                            body:
                                JSON.stringify(
                                    datos
                                )
                        }
                    );

            } else {

                response =
                    await Auth.fetchAutenticado(
                        API_URL,
                        {
                            method: 'POST',
                            headers: {
                                'Content-Type':
                                    'application/json'
                            },
                            body:
                                JSON.stringify(
                                    datos
                                )
                        }
                    );
            }


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
                id
                    ? 'Categoría de gasto actualizada correctamente.'
                    : 'Categoría de gasto creada correctamente.',
                'exito'
            );


            cerrarFormulario();


            await cargarCategorias(
                true
            );

        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                'No se pudo guardar la categoría de gasto.',
                'error'
            );

        } finally {

            guardando =
                false;


            bloquearFormulario(
                false
            );
        }
    }


    function solicitarCambioEstado(
        id,
        activa
    ) {

        const categoria =
            categorias.find(
                item =>
                    item.idCategoriaGasto === id
            );


        if (
            !categoria
        ) {

            mostrarMensaje(
                'No se encontró la categoría de gasto.',
                'error'
            );

            return;
        }


        modalTitulo.textContent =
            activa
                ? 'Activar categoría'
                : 'Desactivar categoría';


        modalTexto.textContent =
            activa
                ? `¿Querés activar la categoría "${categoria.nombre}"?`
                : `¿Querés desactivar la categoría "${categoria.nombre}"?`;


        btnModalConfirmar.textContent =
            activa
                ? 'Activar'
                : 'Desactivar';


        accionConfirmacion =
            () =>
                cambiarEstado(
                    id,
                    activa
                );


        modalConfirmacion.classList.remove(
            'oculto'
        );
    }


    function confirmarAccion() {

        if (
            !accionConfirmacion
        ) {
            return;
        }


        const accion =
            accionConfirmacion;


        accionConfirmacion =
            null;


        accion();
    }


    async function cambiarEstado(
        id,
        activa
    ) {

        cerrarModal();


        const accion =
            activa
                ? 'activar'
                : 'desactivar';


        try {

            const response =
                await Auth.fetchAutenticado(
                    `${API_URL}/${id}/${accion}`,
                    {
                        method: 'PATCH'
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
                activa
                    ? 'Categoría de gasto activada correctamente.'
                    : 'Categoría de gasto desactivada correctamente.',
                'exito'
            );


            await cargarCategorias(
                true
            );

        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                'No se pudo cambiar el estado de la categoría.',
                'error'
            );
        }
    }


    function buscarCategorias() {

        const texto =
            busqueda.value;


        if (
            texto !== ''
            &&
            texto.trim() === ''
        ) {

            mostrarMensaje(
                'La búsqueda no puede contener solamente espacios.',
                'error'
            );

            return;
        }


        cargarCategorias();
    }


    function limpiarFiltros() {

        busqueda.value =
            '';


        filtroEstado.value =
            '';


        cargarCategorias();
    }


    function cerrarFormulario() {

        limpiarFormulario();


        seccionFormulario.classList.add(
            'oculto'
        );
    }


    function limpiarFormulario() {

        categoriaId.value =
            '';


        nombre.value =
            '';


        descripcion.value =
            '';


        contadorDescripcion.textContent =
            '0';


        btnGuardar.textContent =
            'Guardar categoría';
    }


    function actualizarContador() {

        contadorDescripcion.textContent =
            descripcion.value.length;
    }


    function cerrarModal() {

        modalConfirmacion.classList.add(
            'oculto'
        );


        accionConfirmacion =
            null;
    }


    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensaje.textContent =
            texto;


        mensaje.className =
            'mensaje';


        if (
            tipo === 'exito'
        ) {

            mensaje.classList.add(
                'mensaje-exito'
            );

        } else if (
            tipo === 'error'
        ) {

            mensaje.classList.add(
                'mensaje-error'
            );

        } else {

            mensaje.classList.add(
                'mensaje-info'
            );
        }


        mensaje.classList.remove(
            'oculto'
        );
    }


    function ocultarMensaje() {

        mensaje.classList.add(
            'oculto'
        );
    }


    function mostrarCargando(
        mostrar
    ) {

        if (
            mostrar
        ) {

            cargando.classList.remove(
                'oculto'
            );


            contenedorTabla.classList.add(
                'oculto'
            );


            sinResultados.classList.add(
                'oculto'
            );

        } else {

            cargando.classList.add(
                'oculto'
            );
        }
    }


    function bloquearFormulario(
        bloquear
    ) {

        nombre.disabled =
            bloquear;


        descripcion.disabled =
            bloquear;


        btnGuardar.disabled =
            bloquear;


        btnCancelar.disabled =
            bloquear;


        btnCerrarFormulario.disabled =
            bloquear;


        btnGuardar.textContent =
            bloquear
                ? 'Guardando...'
                : categoriaId.value
                    ? 'Guardar cambios'
                    : 'Guardar categoría';
    }

    async function obtenerMensajeError(
        response
    ) {

        try {

            const data =
                await response.json();


            const erroresCampos =
                data.errors
                ||
                data.fieldErrors;


            if (
                erroresCampos
            ) {

                const errores =
                    Object.values(
                        erroresCampos
                    );


                if (
                    errores.length > 0
                ) {

                    return errores.join(
                        ' - '
                    );
                }
            }


            if (
                data.message
            ) {

                return data.message;
            }


            if (
                data.error
            ) {

                return data.error;
            }

        } catch (error) {

            console.error(
                'No se pudo leer la respuesta de error.',
                error
            );
        }


        switch (
        response.status
        ) {

            case 400:
                return 'Los datos ingresados no son válidos.';

            case 403:
                return 'No tenés permisos para realizar esta acción.';

            case 404:
                return 'No se encontró la categoría de gasto.';

            case 409:
                return 'Ya existe una categoría de gasto con ese nombre.';

            default:
                return 'Ocurrió un error inesperado.';
        }
    }

    function escaparHtml(
        texto
    ) {

        if (
            texto === null
            ||
            texto === undefined
        ) {

            return '';
        }


        return String(
            texto
        )
            .replaceAll(
                '&',
                '&amp;'
            )
            .replaceAll(
                '<',
                '&lt;'
            )
            .replaceAll(
                '>',
                '&gt;'
            )
            .replaceAll(
                '"',
                '&quot;'
            )
            .replaceAll(
                "'",
                '&#039;'
            );
    }

})();