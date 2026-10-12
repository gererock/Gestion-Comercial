(() => {

    "use strict";


    const API_PRODUCTOS =
        "/api/productos";


    const TAMANIO_MAXIMO =
        5 * 1024 * 1024;


    const EXTENSIONES_PERMITIDAS =
        [
            "jpg",
            "jpeg",
            "png"
        ];


    const TIPOS_PERMITIDOS =
        [
            "image/jpeg",
            "image/png"
        ];


    let idProducto = null;

    let operando = false;

    let previewUrl = null;


    document.addEventListener(
        "DOMContentLoaded",
        iniciar
    );


    // =====================================
    // ELEMENTOS
    // =====================================

    const nombreUsuario =
        document.getElementById(
            "nombreUsuario"
        );


    const nombreProducto =
        document.getElementById(
            "nombreProducto"
        );


    const btnCerrarSesion =
        document.getElementById(
            "btnCerrarSesion"
        );


    const btnVolverProducto =
        document.getElementById(
            "btnVolverProducto"
        );


    const mensaje =
        document.getElementById(
            "mensaje"
        );


    const formImagen =
        document.getElementById(
            "formImagen"
        );


    const archivo =
        document.getElementById(
            "archivo"
        );


    const orden =
        document.getElementById(
            "orden"
        );


    const principal =
        document.getElementById(
            "principal"
        );


    const btnCancelarSeleccion =
        document.getElementById(
            "btnCancelarSeleccion"
        );


    const btnAgregarImagen =
        document.getElementById(
            "btnAgregarImagen"
        );


    const contenedorPreview =
        document.getElementById(
            "contenedorPreview"
        );


    const previewImagen =
        document.getElementById(
            "previewImagen"
        );


    const imagenesGrid =
        document.getElementById(
            "imagenesGrid"
        );


    const sinImagenes =
        document.getElementById(
            "sinImagenes"
        );


    // =====================================
    // INICIO
    // =====================================

    async function iniciar() {

        if (
            !Auth.requerirRol(
                "ADMINISTRADOR"
            )
        ) {

            return;
        }


        idProducto =
            obtenerIdProducto();


        cargarUsuario();

        configurarEventos();


        if (
            idProducto === null
        ) {

            mostrarMensaje(
                "No se indicó un producto válido.",
                "error"
            );


            formImagen
                .querySelectorAll(
                    "input, button"
                )
                .forEach(
                    elemento =>
                        elemento.disabled =
                            true
                );


            return;
        }


        btnVolverProducto.href =
            `/editar-producto.html?id=${idProducto}`;


        try {

            operando = true;

            actualizarBloqueo();


            await cargarProducto();

            await cargarImagenes();


        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                "No se pudo cargar la gestión de imágenes.",
                "error"
            );


        } finally {

            operando = false;

            actualizarBloqueo();
        }
    }


    function obtenerIdProducto() {

        const parametros =
            new URLSearchParams(
                window.location.search
            );


        const id =
            Number(
                parametros.get("id")
            );


        if (
            !Number.isInteger(id)
            ||
            id <= 0
        ) {

            return null;
        }


        return id;
    }


    // =====================================
    // USUARIO
    // =====================================

    function cargarUsuario() {

        const usuario =
            Auth.obtenerUsuario();


        if (!usuario) {
            return;
        }


        const nombreCompleto =
            [
                usuario.nombre,
                usuario.apellido
            ]
                .filter(
                    Boolean
                )
                .join(
                    " "
                );


        nombreUsuario.textContent =
            nombreCompleto
            ||
            usuario.email
            ||
            "Administrador";
    }


    // =====================================
    // EVENTOS
    // =====================================

    function configurarEventos() {

        btnCerrarSesion.addEventListener(
            "click",
            Auth.cerrarSesion
        );


        archivo.addEventListener(
            "change",
            actualizarPreview
        );


        btnCancelarSeleccion.addEventListener(
            "click",
            limpiarSeleccion
        );


        formImagen.addEventListener(
            "submit",
            agregarImagen
        );
    }


    // =====================================
    // PRODUCTO
    // =====================================

    async function cargarProducto() {

        const response =
            await Auth.fetchAutenticado(
                `${API_PRODUCTOS}/${idProducto}`
            );


        if (!response) {
            return;
        }


        if (!response.ok) {

            throw new Error(
                await obtenerMensajeError(
                    response
                )
            );
        }


        const producto =
            await response.json();


        nombreProducto.textContent =
            producto.nombre;
    }


    // =====================================
    // LISTAR IMÁGENES
    // =====================================

    async function cargarImagenes() {

        const response =
            await Auth.fetchAutenticado(
                `${API_PRODUCTOS}/${idProducto}/imagenes`
            );


        if (!response) {
            return;
        }


        if (!response.ok) {

            throw new Error(
                await obtenerMensajeError(
                    response
                )
            );
        }


        const imagenes =
            await response.json();


        renderizarImagenes(
            imagenes
        );
    }


    function renderizarImagenes(
        imagenes
    ) {

        imagenesGrid.innerHTML =
            "";


        if (
            imagenes.length === 0
        ) {

            sinImagenes.classList.remove(
                "oculto"
            );


            return;
        }


        sinImagenes.classList.add(
            "oculto"
        );


        imagenes.forEach(
            imagen => {

                imagenesGrid.appendChild(
                    crearTarjetaImagen(
                        imagen
                    )
                );
            }
        );


        actualizarBloqueo();
    }


    function crearTarjetaImagen(
        imagen
    ) {

        const card =
            document.createElement(
                "article"
            );


        card.className =
            "imagen-card";


        const visual =
            document.createElement(
                "div"
            );


        visual.className =
            "imagen-visual";


        const img =
            document.createElement(
                "img"
            );


        img.src =
            imagen.urlImagen;


        img.alt =
            `Imagen del producto ${nombreProducto.textContent}`;


        visual.appendChild(
            img
        );


        if (
            imagen.principal
        ) {

            const badge =
                document.createElement(
                    "span"
                );


            badge.className =
                "badge-principal";


            badge.textContent =
                "Principal";


            visual.appendChild(
                badge
            );
        }


        const info =
            document.createElement(
                "div"
            );


        info.className =
            "imagen-info";


        const identificador =
            document.createElement(
                "p"
            );


        identificador.className =
            "imagen-id";


        identificador.textContent =
            `Imagen #${imagen.idImagen}`;


        const ordenLinea =
            document.createElement(
                "div"
            );


        ordenLinea.className =
            "orden-linea";


        const labelOrden =
            document.createElement(
                "label"
            );


        labelOrden.textContent =
            "Orden";


        const inputOrden =
            document.createElement(
                "input"
            );


        inputOrden.type =
            "number";


        inputOrden.min =
            "0";


        inputOrden.step =
            "1";


        inputOrden.value =
            imagen.orden ?? "";


        inputOrden.className =
            "input-orden-imagen";


        const acciones =
            document.createElement(
                "div"
            );


        acciones.className =
            "acciones-imagen";


        const btnPrincipal =
            document.createElement(
                "button"
            );


        btnPrincipal.type =
            "button";


        btnPrincipal.className =
            "btn btn-light accion-imagen btn-principal";


        btnPrincipal.textContent =
            imagen.principal
                ? "Imagen principal"
                : "Hacer principal";


        btnPrincipal.dataset.esPrincipal =
            String(
                Boolean(
                    imagen.principal
                )
            );


        btnPrincipal.addEventListener(
            "click",
            () =>
                definirPrincipal(
                    imagen.idImagen
                )
        );


        const btnOrden =
            document.createElement(
                "button"
            );


        btnOrden.type =
            "button";


        btnOrden.className =
            "btn btn-light accion-imagen";


        btnOrden.textContent =
            "Guardar orden";


        btnOrden.addEventListener(
            "click",
            () =>
                guardarOrden(
                    imagen.idImagen,
                    inputOrden
                )
        );


        const btnEliminar =
            document.createElement(
                "button"
            );


        btnEliminar.type =
            "button";


        btnEliminar.className =
            "btn btn-danger accion-imagen";


        btnEliminar.textContent =
            "Eliminar";


        btnEliminar.addEventListener(
            "click",
            () =>
                eliminarImagen(
                    imagen.idImagen,
                    imagen.principal
                )
        );


        ordenLinea.appendChild(
            labelOrden
        );


        ordenLinea.appendChild(
            inputOrden
        );


        acciones.appendChild(
            btnPrincipal
        );


        acciones.appendChild(
            btnOrden
        );


        acciones.appendChild(
            btnEliminar
        );


        info.appendChild(
            identificador
        );


        info.appendChild(
            ordenLinea
        );


        info.appendChild(
            acciones
        );


        card.appendChild(
            visual
        );


        card.appendChild(
            info
        );


        return card;
    }


    // =====================================
    // PREVIEW
    // =====================================

    function actualizarPreview() {

        liberarPreview();


        const imagen =
            archivo.files[0];


        if (!imagen) {

            ocultarPreview();

            return;
        }


        const error =
            validarArchivoCliente(
                imagen
            );


        if (error) {

            archivo.value =
                "";


            ocultarPreview();


            mostrarMensaje(
                error,
                "error"
            );


            return;
        }


        previewUrl =
            URL.createObjectURL(
                imagen
            );


        previewImagen.src =
            previewUrl;


        contenedorPreview.classList.remove(
            "oculto"
        );
    }


    function ocultarPreview() {

        previewImagen.removeAttribute(
            "src"
        );


        contenedorPreview.classList.add(
            "oculto"
        );
    }


    function liberarPreview() {

        if (
            previewUrl !== null
        ) {

            URL.revokeObjectURL(
                previewUrl
            );


            previewUrl =
                null;
        }
    }


    // =====================================
    // AGREGAR
    // =====================================

    async function agregarImagen(
        event
    ) {

        event.preventDefault();


        if (operando) {
            return;
        }


        ocultarMensaje();


        const imagen =
            archivo.files[0];


        if (!imagen) {

            mostrarMensaje(
                "Debés seleccionar una imagen.",
                "error"
            );


            return;
        }


        const errorArchivo =
            validarArchivoCliente(
                imagen
            );


        if (errorArchivo) {

            mostrarMensaje(
                errorArchivo,
                "error"
            );


            return;
        }


        if (
            orden.value !== ""
            &&
            (
                !Number.isInteger(
                    Number(
                        orden.value
                    )
                )
                ||
                Number(
                    orden.value
                ) < 0
            )
        ) {

            mostrarMensaje(
                "El orden debe ser un número entero mayor o igual a cero.",
                "error"
            );


            return;
        }


        const datos =
            new FormData();


        datos.append(
            "archivo",
            imagen
        );


        if (
            orden.value !== ""
        ) {

            datos.append(
                "orden",
                orden.value
            );
        }


        datos.append(
            "principal",
            principal.checked
        );


        try {

            iniciarOperacion();


            btnAgregarImagen.textContent =
                "Agregando...";


            const response =
                await Auth.fetchAutenticado(
                    `${API_PRODUCTOS}/${idProducto}/imagenes`,
                    {
                        method: "POST",
                        body: datos
                    }
                );


            if (!response) {
                return;
            }


            if (!response.ok) {

                throw new Error(
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            limpiarSeleccion();


            await cargarImagenes();


            mostrarMensaje(
                "Imagen agregada correctamente.",
                "exito"
            );


        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                "No se pudo agregar la imagen.",
                "error"
            );


        } finally {

            finalizarOperacion();
        }
    }


    // =====================================
    // PRINCIPAL
    // =====================================

    async function definirPrincipal(
        idImagen
    ) {

        if (operando) {
            return;
        }


        try {

            iniciarOperacion();


            const response =
                await Auth.fetchAutenticado(
                    `${API_PRODUCTOS}/${idProducto}/imagenes/${idImagen}/principal`,
                    {
                        method: "PATCH"
                    }
                );


            if (!response) {
                return;
            }


            if (!response.ok) {

                throw new Error(
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            await cargarImagenes();


            mostrarMensaje(
                "Imagen principal actualizada correctamente.",
                "exito"
            );


        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                "No se pudo establecer la imagen principal.",
                "error"
            );


        } finally {

            finalizarOperacion();
        }
    }


    // =====================================
    // ORDEN
    // =====================================

    async function guardarOrden(
        idImagen,
        inputOrden
    ) {

        if (operando) {
            return;
        }


        const valor =
            Number(
                inputOrden.value
            );


        if (
            inputOrden.value === ""
            ||
            !Number.isInteger(
                valor
            )
            ||
            valor < 0
        ) {

            mostrarMensaje(
                "El orden debe ser un número entero mayor o igual a cero.",
                "error"
            );


            return;
        }


        try {

            iniciarOperacion();


            const response =
                await Auth.fetchAutenticado(
                    `${API_PRODUCTOS}/${idProducto}/imagenes/${idImagen}/orden`,
                    {

                        method:
                            "PUT",

                        headers: {

                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                {
                                    orden:
                                        valor
                                }
                            )
                    }
                );


            if (!response) {
                return;
            }


            if (!response.ok) {

                throw new Error(
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            await cargarImagenes();


            mostrarMensaje(
                "Orden actualizado correctamente.",
                "exito"
            );


        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                "No se pudo actualizar el orden.",
                "error"
            );


        } finally {

            finalizarOperacion();
        }
    }


    // =====================================
    // ELIMINAR
    // =====================================

    async function eliminarImagen(
        idImagen,
        esPrincipal
    ) {

        if (operando) {
            return;
        }


        let texto =
            "¿Confirmás que querés eliminar esta imagen?";


        if (esPrincipal) {

            texto =
                "Esta es la imagen principal. ¿Confirmás que querés eliminarla?";
        }


        const confirmado =
            window.confirm(
                texto
            );


        if (!confirmado) {
            return;
        }


        try {

            iniciarOperacion();


            const response =
                await Auth.fetchAutenticado(
                    `${API_PRODUCTOS}/${idProducto}/imagenes/${idImagen}`,
                    {
                        method:
                            "DELETE"
                    }
                );


            if (!response) {
                return;
            }


            if (!response.ok) {

                throw new Error(
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            await cargarImagenes();


            mostrarMensaje(
                "Imagen eliminada correctamente.",
                "exito"
            );


        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                "No se pudo eliminar la imagen.",
                "error"
            );


        } finally {

            finalizarOperacion();
        }
    }


    // =====================================
    // VALIDACIÓN ARCHIVO
    // =====================================

    function validarArchivoCliente(
        imagen
    ) {

        if (
            imagen.size
            >
            TAMANIO_MAXIMO
        ) {

            return "La imagen no puede superar los 5 MB.";
        }


        const partes =
            imagen.name
                .toLowerCase()
                .split(
                    "."
                );


        const extension =
            partes.length > 1
                ? partes.pop()
                : "";


        if (
            !EXTENSIONES_PERMITIDAS.includes(
                extension
            )
        ) {

            return "Solo se permiten imágenes JPG, JPEG o PNG.";
        }


        if (
            !TIPOS_PERMITIDOS.includes(
                imagen.type
            )
        ) {

            return "El archivo seleccionado no tiene un formato permitido.";
        }


        return null;
    }


    // =====================================
    // LIMPIAR SELECCIÓN
    // =====================================

    function limpiarSeleccion() {

        liberarPreview();


        formImagen.reset();


        ocultarPreview();
    }


    // =====================================
    // OPERACIONES
    // =====================================

    function iniciarOperacion() {

        operando =
            true;


        ocultarMensaje();


        actualizarBloqueo();
    }


    function finalizarOperacion() {

        operando =
            false;


        btnAgregarImagen.textContent =
            "Agregar imagen";


        actualizarBloqueo();
    }


    function actualizarBloqueo() {

        archivo.disabled =
            operando;


        orden.disabled =
            operando;


        principal.disabled =
            operando;


        btnAgregarImagen.disabled =
            operando;


        btnCancelarSeleccion.disabled =
            operando;


        document
            .querySelectorAll(
                ".input-orden-imagen"
            )
            .forEach(
                elemento => {

                    elemento.disabled =
                        operando;
                }
            );


        document
            .querySelectorAll(
                ".accion-imagen"
            )
            .forEach(
                boton => {

                    boton.disabled =
                        operando;
                }
            );


        document
            .querySelectorAll(
                ".btn-principal"
            )
            .forEach(
                boton => {

                    const esPrincipal =
                        boton.dataset.esPrincipal
                        ===
                        "true";


                    boton.disabled =
                        operando
                        ||
                        esPrincipal;
                }
            );
    }


    // =====================================
    // MENSAJES
    // =====================================

    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensaje.textContent =
            texto;


        mensaje.className =
            "mensaje";


        if (
            tipo === "exito"
        ) {

            mensaje.classList.add(
                "mensaje-exito"
            );


        } else if (
            tipo === "error"
        ) {

            mensaje.classList.add(
                "mensaje-error"
            );


        } else {

            mensaje.classList.add(
                "mensaje-info"
            );
        }


        mensaje.classList.remove(
            "oculto"
        );


        window.scrollTo(
            {
                top:
                    0,

                behavior:
                    "smooth"
            }
        );
    }


    function ocultarMensaje() {

        mensaje.classList.add(
            "oculto"
        );
    }


    // =====================================
    // ERRORES BACKEND
    // =====================================

    async function obtenerMensajeError(
        response
    ) {

        try {

            const data =
                await response.json();


            if (
                data.errors
                &&
                Object.keys(
                    data.errors
                ).length > 0
            ) {

                return Object.values(
                    data.errors
                ).join(
                    " - "
                );
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
                "No se pudo leer el error:",
                error
            );
        }


        if (
            response.status === 400
        ) {

            return "Los datos de la imagen no son válidos.";
        }


        if (
            response.status === 403
        ) {

            return "No tenés permisos para gestionar imágenes.";
        }


        if (
            response.status === 404
        ) {

            return "No se encontró el producto o la imagen.";
        }


        if (
            response.status === 413
        ) {

            return "La imagen supera el tamaño máximo permitido.";
        }


        return "Ocurrió un error inesperado.";
    }

})();