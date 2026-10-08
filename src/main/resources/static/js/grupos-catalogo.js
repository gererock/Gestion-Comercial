(() => {

    "use strict";


    const API_URL =
        "/api/grupos-catalogo";


    const tablaGrupos =
        document.getElementById(
            "tablaGrupos"
        );

    const contenedorTabla =
        document.getElementById(
            "contenedorTabla"
        );

    const sinResultados =
        document.getElementById(
            "sinResultados"
        );

    const cargando =
        document.getElementById(
            "cargando"
        );

    const cantidadGrupos =
        document.getElementById(
            "cantidadGrupos"
        );


    const busqueda =
        document.getElementById(
            "busqueda"
        );

    const filtroEstado =
        document.getElementById(
            "filtroEstado"
        );

    const btnBuscar =
        document.getElementById(
            "btnBuscar"
        );

    const btnLimpiar =
        document.getElementById(
            "btnLimpiar"
        );


    const seccionFormulario =
        document.getElementById(
            "seccionFormulario"
        );

    const formGrupo =
        document.getElementById(
            "formGrupo"
        );

    const grupoId =
        document.getElementById(
            "grupoId"
        );

    const nombreVisible =
        document.getElementById(
            "nombreVisible"
        );

    const descripcion =
        document.getElementById(
            "descripcion"
        );

    const imagenUrl =
        document.getElementById(
            "imagenUrl"
        );

    const contadorDescripcion =
        document.getElementById(
            "contadorDescripcion"
        );

    const tituloFormulario =
        document.getElementById(
            "tituloFormulario"
        );

    const textoFormulario =
        document.getElementById(
            "textoFormulario"
        );

    const btnGuardar =
        document.getElementById(
            "btnGuardar"
        );

    const btnCancelar =
        document.getElementById(
            "btnCancelar"
        );

    const btnCerrarFormulario =
        document.getElementById(
            "btnCerrarFormulario"
        );

    const btnNuevoGrupo =
        document.getElementById(
            "btnNuevoGrupo"
        );


    const previewImagen =
        document.getElementById(
            "previewImagen"
        );

    const previewPlaceholder =
        document.getElementById(
            "previewPlaceholder"
        );


    const mensaje =
        document.getElementById(
            "mensaje"
        );


    const modalConfirmacion =
        document.getElementById(
            "modalConfirmacion"
        );

    const modalTitulo =
        document.getElementById(
            "modalTitulo"
        );

    const modalTexto =
        document.getElementById(
            "modalTexto"
        );

    const btnModalCancelar =
        document.getElementById(
            "btnModalCancelar"
        );

    const btnModalConfirmar =
        document.getElementById(
            "btnModalConfirmar"
        );


    const btnCerrarSesion =
        document.getElementById(
            "btnCerrarSesion"
        );

    const nombreUsuario =
        document.getElementById(
            "nombreUsuario"
        );


    let grupos = [];

    let accionConfirmacion = null;

    let guardando = false;


    document.addEventListener(
        "DOMContentLoaded",
        () => {

            if (
                !Auth.requerirRol(
                    "ADMINISTRADOR"
                )
            ) {
                return;
            }


            cargarDatosUsuario();

            cargarGrupos();
        }
    );


    function cargarDatosUsuario() {

        const usuario =
            Auth.obtenerUsuario();


        if (!usuario) {
            return;
        }


        nombreUsuario.textContent =
            usuario.apellido
                ? `${usuario.nombre} ${usuario.apellido}`
                : usuario.nombre;
    }


    async function cargarGrupos() {

        mostrarCargando(true);

        ocultarMensaje();


        try {

            const parametros =
                new URLSearchParams();


            const nombre =
                busqueda.value;

            const activo =
                filtroEstado.value;


            if (nombre !== "") {

                parametros.append(
                    "nombre",
                    nombre
                );
            }


            if (activo !== "") {

                parametros.append(
                    "activo",
                    activo
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
                        method: "GET"
                    }
                );


            if (!response) {
                return;
            }


            if (response.status === 403) {

                mostrarMensaje(
                    "No tenés permisos para consultar los grupos.",
                    "error"
                );

                grupos = [];

                mostrarGrupos();

                return;
            }


            if (!response.ok) {

                throw new Error(
                    await obtenerMensajeError(
                        response
                    )
                );
            }


            grupos =
                await response.json();


            mostrarGrupos();


        } catch (error) {

            console.error(error);


            mostrarMensaje(
                error.message ||
                "No se pudieron cargar los grupos de catálogo.",
                "error"
            );

        } finally {

            mostrarCargando(false);
        }
    }


    function mostrarGrupos() {

        tablaGrupos.innerHTML =
            "";


        cantidadGrupos.textContent =
            `${grupos.length} grupo${grupos.length !== 1
                ? "s"
                : ""
            }`;


        if (
            grupos.length === 0
        ) {

            contenedorTabla
                .classList
                .add("oculto");

            sinResultados
                .classList
                .remove("oculto");

            return;
        }


        sinResultados
            .classList
            .add("oculto");

        contenedorTabla
            .classList
            .remove("oculto");


        grupos.forEach(
            grupo => {

                const fila =
                    document.createElement(
                        "tr"
                    );


                const celdaImagen =
                    document.createElement(
                        "td"
                    );

                celdaImagen.dataset.label =
                    "Imagen";


                if (grupo.imagenUrl) {

                    const img =
                        document.createElement(
                            "img"
                        );

                    img.src =
                        grupo.imagenUrl;

                    img.alt =
                        grupo.nombreVisible;

                    img.className =
                        "imagen-grupo";


                    img.addEventListener(
                        "error",
                        () => {

                            const placeholder =
                                crearPlaceholderImagen();

                            img.replaceWith(
                                placeholder
                            );
                        },
                        {
                            once: true
                        }
                    );


                    celdaImagen.appendChild(
                        img
                    );

                } else {

                    celdaImagen.appendChild(
                        crearPlaceholderImagen()
                    );
                }


                const celdaNombre =
                    document.createElement(
                        "td"
                    );

                celdaNombre.dataset.label =
                    "Nombre";


                const spanNombre =
                    document.createElement(
                        "span"
                    );

                spanNombre.className =
                    "nombre-grupo";

                spanNombre.textContent =
                    grupo.nombreVisible;


                celdaNombre.appendChild(
                    spanNombre
                );


                const celdaDescripcion =
                    document.createElement(
                        "td"
                    );

                celdaDescripcion.dataset.label =
                    "Descripción";


                const spanDescripcion =
                    document.createElement(
                        "span"
                    );

                spanDescripcion.className =
                    "descripcion-grupo";

                spanDescripcion.textContent =
                    grupo.descripcion
                        ? grupo.descripcion
                        : "Sin descripción";


                celdaDescripcion.appendChild(
                    spanDescripcion
                );


                const celdaEstado =
                    document.createElement(
                        "td"
                    );

                celdaEstado.dataset.label =
                    "Estado";


                const spanEstado =
                    document.createElement(
                        "span"
                    );

                spanEstado.className =
                    grupo.activo
                        ? "estado estado-activo"
                        : "estado estado-inactivo";

                spanEstado.textContent =
                    grupo.activo
                        ? "Activo"
                        : "Inactivo";


                celdaEstado.appendChild(
                    spanEstado
                );


                const celdaAcciones =
                    document.createElement(
                        "td"
                    );

                celdaAcciones.dataset.label =
                    "Acciones";


                const acciones =
                    document.createElement(
                        "div"
                    );

                acciones.className =
                    "acciones-tabla";


                const btnEditar =
                    document.createElement(
                        "button"
                    );

                btnEditar.type =
                    "button";

                btnEditar.className =
                    "btn-tabla btn-editar";

                btnEditar.textContent =
                    "Editar";


                btnEditar.addEventListener(
                    "click",
                    () =>
                        editarGrupo(
                            grupo.idGrupoCatalogo
                        )
                );


                const btnEstado =
                    document.createElement(
                        "button"
                    );

                btnEstado.type =
                    "button";


                if (grupo.activo) {

                    btnEstado.className =
                        "btn-tabla btn-desactivar";

                    btnEstado.textContent =
                        "Desactivar";

                } else {

                    btnEstado.className =
                        "btn-tabla btn-activar";

                    btnEstado.textContent =
                        "Activar";
                }


                btnEstado.addEventListener(
                    "click",
                    () =>
                        solicitarCambioEstado(
                            grupo.idGrupoCatalogo,
                            !grupo.activo,
                            grupo.nombreVisible
                        )
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
                    celdaImagen
                );

                fila.appendChild(
                    celdaNombre
                );

                fila.appendChild(
                    celdaDescripcion
                );

                fila.appendChild(
                    celdaEstado
                );

                fila.appendChild(
                    celdaAcciones
                );


                tablaGrupos.appendChild(
                    fila
                );
            }
        );
    }


    function crearPlaceholderImagen() {

        const elemento =
            document.createElement(
                "div"
            );

        elemento.className =
            "imagen-sin-foto";

        elemento.textContent =
            "Sin imagen";

        return elemento;
    }


    formGrupo.addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            if (guardando) {
                return;
            }


            const id =
                grupoId.value;


            const nombre =
                nombreVisible
                    .value
                    .trim();


            const descripcionGrupo =
                descripcion
                    .value
                    .trim();


            const imagen =
                imagenUrl
                    .value
                    .trim();


            if (!nombre) {

                mostrarMensaje(
                    "El nombre visible es obligatorio.",
                    "error"
                );

                nombreVisible.focus();

                return;
            }


            const patronNombre =
                /^(?=.*\p{L})[\p{L}\p{N}\s°ºª.\-]+$/u;


            if (
                !patronNombre.test(
                    nombre
                )
            ) {

                mostrarMensaje(
                    "El nombre visible debe contener al menos una letra y no puede contener símbolos inválidos.",
                    "error"
                );

                nombreVisible.focus();

                return;
            }


            const datos = {

                nombreVisible:
                    nombre,

                descripcion:
                    descripcionGrupo ||
                    null,

                imagenUrl:
                    imagen ||
                    null
            };


            try {

                bloquearFormulario(
                    true
                );


                let response;


                if (id) {

                    response =
                        await Auth.fetchAutenticado(
                            `${API_URL}/${id}`,
                            {
                                method: "PUT",

                                headers: {
                                    "Content-Type":
                                        "application/json"
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
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify(
                                        datos
                                    )
                            }
                        );
                }


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


                mostrarMensaje(
                    id
                        ? "Grupo actualizado correctamente."
                        : "Grupo creado correctamente.",
                    "exito"
                );


                cerrarFormulario();

                await cargarGrupos();


            } catch (error) {

                console.error(error);


                mostrarMensaje(
                    error.message ||
                    "No se pudo guardar el grupo.",
                    "error"
                );

            } finally {

                bloquearFormulario(
                    false
                );
            }
        }
    );


    function editarGrupo(id) {

        const grupo =
            grupos.find(
                item =>
                    item.idGrupoCatalogo
                    === id
            );


        if (!grupo) {

            mostrarMensaje(
                "No se encontró el grupo de catálogo.",
                "error"
            );

            return;
        }


        grupoId.value =
            grupo.idGrupoCatalogo;

        nombreVisible.value =
            grupo.nombreVisible;

        descripcion.value =
            grupo.descripcion || "";

        imagenUrl.value =
            grupo.imagenUrl || "";


        tituloFormulario.textContent =
            "Editar grupo";

        textoFormulario.textContent =
            "Modificá los datos del grupo de catálogo.";

        btnGuardar.textContent =
            "Guardar cambios";


        actualizarContador();

        actualizarPreview();


        seccionFormulario
            .classList
            .remove("oculto");


        nombreVisible.focus();


        seccionFormulario
            .scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
    }


    btnNuevoGrupo.addEventListener(
        "click",
        () => {

            limpiarFormulario();


            tituloFormulario.textContent =
                "Nuevo grupo";

            textoFormulario.textContent =
                "Completá los datos del grupo de catálogo.";

            btnGuardar.textContent =
                "Guardar grupo";


            seccionFormulario
                .classList
                .remove("oculto");


            nombreVisible.focus();


            seccionFormulario
                .scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
        }
    );


    function solicitarCambioEstado(
        id,
        activar,
        nombre
    ) {

        modalTitulo.textContent =
            activar
                ? "Activar grupo"
                : "Desactivar grupo";


        modalTexto.textContent =
            activar
                ? `¿Querés activar el grupo "${nombre}"?`
                : `¿Querés desactivar el grupo "${nombre}"?`;


        btnModalConfirmar.textContent =
            activar
                ? "Activar"
                : "Desactivar";


        accionConfirmacion =
            () =>
                cambiarEstado(
                    id,
                    activar
                );


        modalConfirmacion
            .classList
            .remove("oculto");
    }


    async function cambiarEstado(
        id,
        activar
    ) {

        cerrarModal();


        const accion =
            activar
                ? "activar"
                : "desactivar";


        try {

            const response =
                await Auth.fetchAutenticado(
                    `${API_URL}/${id}/${accion}`,
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


            mostrarMensaje(
                activar
                    ? "Grupo activado correctamente."
                    : "Grupo desactivado correctamente.",
                "exito"
            );


            await cargarGrupos();


        } catch (error) {

            console.error(error);


            mostrarMensaje(
                error.message ||
                "No se pudo cambiar el estado del grupo.",
                "error"
            );
        }
    }


    btnBuscar.addEventListener(
        "click",
        () => {

            const texto =
                busqueda.value;


            if (
                texto !== "" &&
                texto.trim() === ""
            ) {

                mostrarMensaje(
                    "La búsqueda no puede contener solamente espacios.",
                    "error"
                );

                return;
            }


            if (
                texto.trim() !== "" &&
                !/\p{L}/u.test(
                    texto
                )
            ) {

                mostrarMensaje(
                    "La búsqueda debe contener al menos una letra.",
                    "error"
                );

                return;
            }


            cargarGrupos();
        }
    );


    busqueda.addEventListener(
        "keydown",
        event => {

            if (
                event.key === "Enter"
            ) {

                event.preventDefault();

                btnBuscar.click();
            }
        }
    );


    filtroEstado.addEventListener(
        "change",
        cargarGrupos
    );


    btnLimpiar.addEventListener(
        "click",
        () => {

            busqueda.value =
                "";

            filtroEstado.value =
                "";

            cargarGrupos();
        }
    );


    btnCancelar.addEventListener(
        "click",
        cerrarFormulario
    );


    btnCerrarFormulario.addEventListener(
        "click",
        cerrarFormulario
    );


    function cerrarFormulario() {

        limpiarFormulario();


        seccionFormulario
            .classList
            .add("oculto");
    }


    function limpiarFormulario() {

        formGrupo.reset();

        grupoId.value =
            "";

        contadorDescripcion.textContent =
            "0";

        limpiarPreview();
    }


    descripcion.addEventListener(
        "input",
        actualizarContador
    );


    function actualizarContador() {

        contadorDescripcion.textContent =
            descripcion.value.length;
    }


    imagenUrl.addEventListener(
        "input",
        actualizarPreview
    );


    function actualizarPreview() {

        const url =
            imagenUrl.value.trim();


        if (!url) {

            limpiarPreview();

            return;
        }


        previewImagen.onload =
            () => {

                previewPlaceholder
                    .classList
                    .add("oculto");

                previewImagen
                    .classList
                    .remove("oculto");
            };


        previewImagen.onerror =
            () => {

                previewImagen
                    .classList
                    .add("oculto");

                previewPlaceholder
                    .classList
                    .remove("oculto");
            };


        previewImagen.src =
            url;
    }


    function limpiarPreview() {

        previewImagen.removeAttribute(
            "src"
        );

        previewImagen
            .classList
            .add("oculto");

        previewPlaceholder
            .classList
            .remove("oculto");
    }


    btnModalCancelar.addEventListener(
        "click",
        cerrarModal
    );


    btnModalConfirmar.addEventListener(
        "click",
        () => {

            if (
                accionConfirmacion
            ) {

                accionConfirmacion();
            }
        }
    );


    function cerrarModal() {

        modalConfirmacion
            .classList
            .add("oculto");

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


        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });


        if (
            tipo === "exito"
        ) {

            setTimeout(
                ocultarMensaje,
                4000
            );
        }
    }


    function ocultarMensaje() {

        mensaje.classList.add(
            "oculto"
        );
    }


    async function obtenerMensajeError(
        response
    ) {

        try {

            const data =
                await response.json();


            if (
                data.errors &&
                Object.keys(
                    data.errors
                ).length > 0
            ) {

                return Object.values(
                    data.errors
                ).join(" - ");
            }


            if (data.message) {

                return data.message;
            }


            if (data.error) {

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

            return "Los datos ingresados no son válidos.";
        }


        if (
            response.status === 401
        ) {

            return "Debés iniciar sesión.";
        }


        if (
            response.status === 403
        ) {

            return "No tenés permisos para realizar esta acción.";
        }


        if (
            response.status === 404
        ) {

            return "No se encontró el grupo de catálogo.";
        }


        if (
            response.status === 409
        ) {

            return "Ya existe un grupo de catálogo con ese nombre.";
        }


        return "Ocurrió un error inesperado.";
    }


    function mostrarCargando(
        mostrar
    ) {

        if (mostrar) {

            cargando
                .classList
                .remove("oculto");

            contenedorTabla
                .classList
                .add("oculto");

            sinResultados
                .classList
                .add("oculto");

        } else {

            cargando
                .classList
                .add("oculto");
        }
    }


    function bloquearFormulario(
        bloquear
    ) {

        guardando =
            bloquear;


        nombreVisible.disabled =
            bloquear;

        descripcion.disabled =
            bloquear;

        imagenUrl.disabled =
            bloquear;

        btnCancelar.disabled =
            bloquear;

        btnCerrarFormulario.disabled =
            bloquear;

        btnGuardar.disabled =
            bloquear;


        btnGuardar.textContent =
            bloquear
                ? "Guardando..."
                : grupoId.value
                    ? "Guardar cambios"
                    : "Guardar grupo";
    }


    btnCerrarSesion.addEventListener(
        "click",
        () => {

            Auth.cerrarSesion();
        }
    );

})();