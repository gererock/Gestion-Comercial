// ==========================================
// CONFIGURACIÓN
// ==========================================

const API_URL = "/api/categorias";


// ==========================================
// ELEMENTOS DEL HTML
// ==========================================

const tablaCategorias =
    document.getElementById("tablaCategorias");

const contenedorTabla =
    document.getElementById("contenedorTabla");

const sinResultados =
    document.getElementById("sinResultados");

const cargando =
    document.getElementById("cargando");

const cantidadCategorias =
    document.getElementById("cantidadCategorias");


const busqueda =
    document.getElementById("busqueda");

const filtroEstado =
    document.getElementById("filtroEstado");

const btnBuscar =
    document.getElementById("btnBuscar");

const btnLimpiar =
    document.getElementById("btnLimpiar");


const seccionFormulario =
    document.getElementById("seccionFormulario");

const formCategoria =
    document.getElementById("formCategoria");

const categoriaId =
    document.getElementById("categoriaId");

const nombre =
    document.getElementById("nombre");

const descripcion =
    document.getElementById("descripcion");

const tituloFormulario =
    document.getElementById("tituloFormulario");

const textoFormulario =
    document.getElementById("textoFormulario");

const btnGuardar =
    document.getElementById("btnGuardar");

const btnCancelar =
    document.getElementById("btnCancelar");

const btnCerrarFormulario =
    document.getElementById("btnCerrarFormulario");

const btnNuevaCategoria =
    document.getElementById("btnNuevaCategoria");

const contadorDescripcion =
    document.getElementById("contadorDescripcion");


const mensaje =
    document.getElementById("mensaje");


const modalConfirmacion =
    document.getElementById("modalConfirmacion");

const modalTitulo =
    document.getElementById("modalTitulo");

const modalTexto =
    document.getElementById("modalTexto");

const btnModalCancelar =
    document.getElementById("btnModalCancelar");

const btnModalConfirmar =
    document.getElementById("btnModalConfirmar");


const btnCerrarSesion =
    document.getElementById("btnCerrarSesion");

const nombreUsuario =
    document.getElementById("nombreUsuario");


// ==========================================
// VARIABLES
// ==========================================

let categorias = [];

let accionConfirmacion = null;


// ==========================================
// TOKEN
// ==========================================

function obtenerToken() {

    return localStorage.getItem("token");
}


// ==========================================
// HEADERS
// ==========================================

function obtenerHeaders() {

    const token = obtenerToken();

    const headers = {
        "Content-Type": "application/json"
    };

    if (token) {

        headers["Authorization"] =
            `Bearer ${token}`;
    }

    return headers;
}


// ==========================================
// INICIO
// ==========================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        cargarDatosUsuario();

        cargarCategorias();
    }
);


// ==========================================
// DATOS USUARIO
// ==========================================

function cargarDatosUsuario() {

    const nombreGuardado =
        localStorage.getItem("nombre");

    const apellidoGuardado =
        localStorage.getItem("apellido");

    if (nombreGuardado) {

        nombreUsuario.textContent =
            apellidoGuardado
                ? `${nombreGuardado} ${apellidoGuardado}`
                : nombreGuardado;
    }
}


// ==========================================
// CARGAR CATEGORÍAS
// ==========================================

async function cargarCategorias() {

    mostrarCargando(true);

    ocultarMensaje();

    try {

        const parametros =
            new URLSearchParams();


        const textoBusqueda =
            busqueda.value;

        const estadoSeleccionado =
            filtroEstado.value;


        if (textoBusqueda !== "") {

            parametros.append(
                "nombre",
                textoBusqueda
            );
        }


        if (estadoSeleccionado !== "") {

            parametros.append(
                "activa",
                estadoSeleccionado
            );
        }


        let url = API_URL;


        if (parametros.toString()) {

            url +=
                `?${parametros.toString()}`;
        }


        const response =
            await fetch(
                url,
                {
                    method: "GET",
                    headers: obtenerHeaders()
                }
            );


        if (response.status === 401) {

            mostrarMensaje(
                "Tu sesión no es válida o venció. Iniciá sesión nuevamente.",
                "error"
            );

            categorias = [];

            mostrarCategorias();

            return;
        }


        if (response.status === 403) {

            mostrarMensaje(
                "No tenés permisos para consultar las categorías.",
                "error"
            );

            categorias = [];

            mostrarCategorias();

            return;
        }


        if (!response.ok) {

            const mensajeError =
                await obtenerMensajeError(response);

            throw new Error(mensajeError);
        }


        categorias =
            await response.json();


        mostrarCategorias();

    } catch (error) {

        console.error(error);

        mostrarMensaje(
            error.message ||
            "No se pudieron cargar las categorías.",
            "error"
        );

    } finally {

        mostrarCargando(false);
    }
}


// ==========================================
// MOSTRAR CATEGORÍAS
// ==========================================

function mostrarCategorias() {

    tablaCategorias.innerHTML = "";


    cantidadCategorias.textContent =
        `${categorias.length} categoría${
            categorias.length !== 1
                ? "s"
                : ""
        }`;


    if (categorias.length === 0) {

        contenedorTabla.classList.add(
            "oculto"
        );

        sinResultados.classList.remove(
            "oculto"
        );

        return;
    }


    sinResultados.classList.add(
        "oculto"
    );

    contenedorTabla.classList.remove(
        "oculto"
    );


    categorias.forEach(
        categoria => {

            const fila =
                document.createElement("tr");


            const descripcionTexto =
                categoria.descripcion
                    ? escaparHtml(
                        categoria.descripcion
                    )
                    : "Sin descripción";


            const estadoHtml =
                categoria.activa
                    ? `
                        <span class="estado estado-activa">
                            Activa
                        </span>
                      `
                    : `
                        <span class="estado estado-inactiva">
                            Inactiva
                        </span>
                      `;


            const botonEstado =
                categoria.activa
                    ? `
                        <button
                            class="btn-tabla btn-desactivar"
                            onclick="solicitarCambioEstado(
                                ${categoria.id},
                                false,
                                '${escaparComillas(
                                    categoria.nombre
                                )}'
                            )"
                        >
                            Desactivar
                        </button>
                      `
                    : `
                        <button
                            class="btn-tabla btn-activar"
                            onclick="solicitarCambioEstado(
                                ${categoria.id},
                                true,
                                '${escaparComillas(
                                    categoria.nombre
                                )}'
                            )"
                        >
                            Activar
                        </button>
                      `;


            fila.innerHTML = `
                <td>
                    <span class="nombre-categoria">
                        ${escaparHtml(
                            categoria.nombre
                        )}
                    </span>
                </td>

                <td>
                    <span class="descripcion-categoria">
                        ${descripcionTexto}
                    </span>
                </td>

                <td>
                    ${estadoHtml}
                </td>

                <td>
                    <div class="acciones-tabla">

                        <button
                            class="btn-tabla btn-editar"
                            onclick="editarCategoria(
                                ${categoria.id}
                            )"
                        >
                            Editar
                        </button>

                        ${botonEstado}

                    </div>
                </td>
            `;


            tablaCategorias.appendChild(
                fila
            );
        }
    );
}


// ==========================================
// CREAR / ACTUALIZAR
// ==========================================

formCategoria.addEventListener(
    "submit",
    async event => {

        event.preventDefault();


        const id =
            categoriaId.value;


        const nombreCategoria =
            nombre.value.trim();


        const descripcionCategoria =
            descripcion.value.trim();


        if (!nombreCategoria) {

            mostrarMensaje(
                "El nombre es obligatorio.",
                "error"
            );

            nombre.focus();

            return;
        }


        const contieneLetra =
            /[\p{L}]/u.test(
                nombreCategoria
            );


        if (!contieneLetra) {

            mostrarMensaje(
                "El nombre debe contener al menos una letra.",
                "error"
            );

            nombre.focus();

            return;
        }


        const datos = {

            nombre:
                nombreCategoria,

            descripcion:
                descripcionCategoria
        };


        try {

            bloquearFormulario(true);


            let response;


            if (id) {

                response =
                    await fetch(
                        `${API_URL}/${id}`,
                        {
                            method: "PUT",
                            headers:
                                obtenerHeaders(),

                            body:
                                JSON.stringify(
                                    datos
                                )
                        }
                    );

            } else {

                response =
                    await fetch(
                        API_URL,
                        {
                            method: "POST",
                            headers:
                                obtenerHeaders(),

                            body:
                                JSON.stringify(
                                    datos
                                )
                        }
                    );
            }


            if (!response.ok) {

                const mensajeError =
                    await obtenerMensajeError(
                        response
                    );

                throw new Error(
                    mensajeError
                );
            }


            if (id) {

                mostrarMensaje(
                    "Categoría actualizada correctamente.",
                    "exito"
                );

            } else {

                mostrarMensaje(
                    "Categoría creada correctamente.",
                    "exito"
                );
            }


            cerrarFormulario();

            await cargarCategorias();

    } catch (error) {

            console.error(error);

            mostrarMensaje(
                error.message ||
                "Ocurrió un error al guardar la categoría.",
                "error"
            );

        } finally {

            bloquearFormulario(false);
        }
    }
);


// ==========================================
// EDITAR
// ==========================================

function editarCategoria(id) {

    const categoria =
        categorias.find(
            categoria =>
                categoria.id === id
        );


    if (!categoria) {

        mostrarMensaje(
            "No se encontró la categoría.",
            "error"
        );

        return;
    }


    categoriaId.value =
        categoria.id;


    nombre.value =
        categoria.nombre;


    descripcion.value =
        categoria.descripcion || "";


    tituloFormulario.textContent =
        "Editar categoría";


    textoFormulario.textContent =
        "Modificá el nombre o la descripción de la categoría.";


    btnGuardar.textContent =
        "Guardar cambios";


    actualizarContador();


    seccionFormulario.classList.remove(
        "oculto"
    );


    nombre.focus();


    seccionFormulario.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}


// ==========================================
// NUEVA CATEGORÍA
// ==========================================

btnNuevaCategoria.addEventListener(
    "click",
    () => {

        limpiarFormulario();


        tituloFormulario.textContent =
            "Nueva categoría";


        textoFormulario.textContent =
            "Completá los datos de la nueva categoría.";


        btnGuardar.textContent =
            "Guardar categoría";


        seccionFormulario.classList.remove(
            "oculto"
        );


        nombre.focus();


        seccionFormulario.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }
);


// ==========================================
// CAMBIAR ESTADO
// ==========================================

function solicitarCambioEstado(
    id,
    activa,
    nombreCategoria
) {

    modalTitulo.textContent =
        activa
            ? "Activar categoría"
            : "Desactivar categoría";


    modalTexto.textContent =
        activa
            ? `¿Querés activar la categoría "${nombreCategoria}"?`
            : `¿Querés desactivar la categoría "${nombreCategoria}"?`;


    btnModalConfirmar.textContent =
        activa
            ? "Activar"
            : "Desactivar";


    accionConfirmacion =
        () => cambiarEstado(
            id,
            activa
        );


    modalConfirmacion.classList.remove(
        "oculto"
    );
}


async function cambiarEstado(
    id,
    activa
) {

    cerrarModal();


    const accion =
        activa
            ? "activar"
            : "desactivar";


    try {

        const response =
            await fetch(
                `${API_URL}/${id}/${accion}`,
                {
                    method: "PATCH",
                    headers:
                        obtenerHeaders()
                }
            );


        if (!response.ok) {

            const mensajeError =
                await obtenerMensajeError(
                    response
                );

            throw new Error(
                mensajeError
            );
        }


        mostrarMensaje(

            activa
                ? "Categoría activada correctamente."
                : "Categoría desactivada correctamente.",

            "exito"
        );


        await cargarCategorias();

    } catch (error) {

        console.error(error);


        mostrarMensaje(
            error.message ||
            "No se pudo cambiar el estado de la categoría.",
            "error"
        );
    }
}


// ==========================================
// BUSCAR
// ==========================================

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


        cargarCategorias();
    }
);


// Buscar también con Enter

busqueda.addEventListener(
    "keydown",
    event => {

        if (event.key === "Enter") {

            event.preventDefault();

            btnBuscar.click();
        }
    }
);


// ==========================================
// FILTRO
// ==========================================

filtroEstado.addEventListener(
    "change",
    () => {

        cargarCategorias();
    }
);


// ==========================================
// LIMPIAR FILTROS
// ==========================================

btnLimpiar.addEventListener(
    "click",
    () => {

        busqueda.value = "";

        filtroEstado.value = "";

        cargarCategorias();
    }
);


// ==========================================
// CANCELAR FORMULARIO
// ==========================================

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


    seccionFormulario.classList.add(
        "oculto"
    );
}


function limpiarFormulario() {

    categoriaId.value = "";

    nombre.value = "";

    descripcion.value = "";

    contadorDescripcion.textContent = "0";
}


// ==========================================
// CONTADOR DESCRIPCIÓN
// ==========================================

descripcion.addEventListener(
    "input",
    actualizarContador
);


function actualizarContador() {

    contadorDescripcion.textContent =
        descripcion.value.length;
}


// ==========================================
// MODAL
// ==========================================

btnModalCancelar.addEventListener(
    "click",
    cerrarModal
);


btnModalConfirmar.addEventListener(
    "click",
    () => {

        if (accionConfirmacion) {

            accionConfirmacion();
        }
    }
);


function cerrarModal() {

    modalConfirmacion.classList.add(
        "oculto"
    );

    accionConfirmacion = null;
}


// ==========================================
// MENSAJES
// ==========================================

function mostrarMensaje(
    texto,
    tipo
) {

    mensaje.textContent =
        texto;


    mensaje.className =
        "mensaje";


    if (tipo === "exito") {

        mensaje.classList.add(
            "mensaje-exito"
        );

    } else if (tipo === "error") {

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


    if (tipo === "exito") {

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


// ==========================================
// MENSAJES DE ERROR DEL BACKEND
// ==========================================

async function obtenerMensajeError(
    response
) {

    try {

        const data =
            await response.json();


        if (data.error) {

            return data.error;
        }


        if (data.message) {

            return data.message;
        }


        /*
         Puede pasar que Spring devuelva
         errores de validación así:

         {
             "nombre": "El nombre es obligatorio"
         }
        */

        const valores =
            Object.values(data);


        if (valores.length > 0) {

            return valores.join(" - ");
        }


    } catch (error) {

        console.error(
            "No se pudo leer el error:",
            error
        );
    }


    if (response.status === 400) {

        return "Los datos ingresados no son válidos.";
    }


    if (response.status === 401) {

        return "Debés iniciar sesión.";
    }


    if (response.status === 403) {

        return "No tenés permisos para realizar esta acción.";
    }


    if (response.status === 404) {

        return "No se encontró la categoría.";
    }


    if (response.status === 409) {

        return "Ya existe una categoría con ese nombre.";
    }


    return "Ocurrió un error inesperado.";
}


// ==========================================
// CARGANDO
// ==========================================

function mostrarCargando(
    mostrar
) {

    if (mostrar) {

        cargando.classList.remove(
            "oculto"
        );

        contenedorTabla.classList.add(
            "oculto"
        );

        sinResultados.classList.add(
            "oculto"
        );

    } else {

        cargando.classList.add(
            "oculto"
        );
    }
}


// ==========================================
// BLOQUEAR FORMULARIO
// ==========================================

function bloquearFormulario(
    bloquear
) {

    nombre.disabled =
        bloquear;

    descripcion.disabled =
        bloquear;

    btnGuardar.disabled =
        bloquear;


    btnGuardar.textContent =
        bloquear
            ? "Guardando..."
            : categoriaId.value
                ? "Guardar cambios"
                : "Guardar categoría";
}


// ==========================================
// CERRAR SESIÓN
// ==========================================

btnCerrarSesion.addEventListener(
    "click",
    () => {

        localStorage.removeItem("token");

        localStorage.removeItem("nombre");

        localStorage.removeItem("apellido");

        localStorage.removeItem("email");

        localStorage.removeItem("rol");


        /*
         Cuando tengamos el login del frontend,
         acá redirigimos a:

         window.location.href = "/login.html";
        */

        mostrarMensaje(
            "Sesión cerrada.",
            "info"
        );
    }
);


// ==========================================
// SEGURIDAD PARA MOSTRAR TEXTO EN HTML
// ==========================================

function escaparHtml(texto) {

    if (texto === null ||
        texto === undefined) {

        return "";
    }


    return String(texto)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}


function escaparComillas(texto) {

    return String(texto)
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "\\'");
}