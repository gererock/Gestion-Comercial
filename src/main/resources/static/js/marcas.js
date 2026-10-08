const API_URL = "/api/marcas";


let marcas = [];

let accionConfirmacion = null;


// ELEMENTOS

const nombreUsuario =
    document.getElementById("nombreUsuario");

const btnCerrarSesion =
    document.getElementById("btnCerrarSesion");

const btnNuevaMarca =
    document.getElementById("btnNuevaMarca");

const mensaje =
    document.getElementById("mensaje");

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

const tituloFormulario =
    document.getElementById("tituloFormulario");

const textoFormulario =
    document.getElementById("textoFormulario");

const btnCerrarFormulario =
    document.getElementById("btnCerrarFormulario");

const formMarca =
    document.getElementById("formMarca");

const marcaId =
    document.getElementById("marcaId");

const nombre =
    document.getElementById("nombre");

const btnCancelar =
    document.getElementById("btnCancelar");

const btnGuardar =
    document.getElementById("btnGuardar");

const tablaMarcas =
    document.getElementById("tablaMarcas");

const cantidadMarcas =
    document.getElementById("cantidadMarcas");

const cargando =
    document.getElementById("cargando");

const sinResultados =
    document.getElementById("sinResultados");

const contenedorTabla =
    document.getElementById("contenedorTabla");

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


// ==========================================
// INICIO
// ==========================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        if (!Auth.requerirRol("ADMINISTRADOR")) {
            return;
        }

        cargarUsuario();

        cargarMarcas();
    }
);


// ==========================================
// USUARIO
// ==========================================

function cargarUsuario() {

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


// ==========================================
// OBTENER ID
// ==========================================

// Esto acepta tanto "id" como "id_marca"
// por si tu DTO todavía devuelve id_marca.

function obtenerIdMarca(marca) {

    return marca.id ??
        marca.id_marca;
}


// ==========================================
// CARGAR MARCAS
// ==========================================

async function cargarMarcas() {

    ocultarMensaje();

    mostrarCargando(true);


    const parametros =
        new URLSearchParams();


    const texto =
        busqueda.value.trim();


    const estado =
        filtroEstado.value;


    if (texto !== "") {

        parametros.append(
            "nombre",
            texto
        );
    }


    if (estado !== "") {

        parametros.append(
            "activa",
            estado
        );
    }


    let url = API_URL;


    if (parametros.toString()) {

        url +=
            `?${parametros.toString()}`;
    }


    try {

        const response =
            await Auth.fetchAutenticado(
                url
            );


        if (!response) {
            return;
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


        marcas =
            await response.json();


        renderizarMarcas();


    } catch (error) {

        console.error(error);


        mostrarMensaje(
            error.message ||
            "No se pudieron cargar las marcas.",
            "error"
        );


        marcas = [];

        renderizarMarcas();


    } finally {

        mostrarCargando(false);
    }
}


// ==========================================
// MOSTRAR MARCAS
// ==========================================

function renderizarMarcas() {

    tablaMarcas.innerHTML = "";


    cantidadMarcas.textContent =
        marcas.length === 1
            ? "1 marca encontrada"
            : `${marcas.length} marcas encontradas`;


    if (marcas.length === 0) {

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


    marcas.forEach(
        marca => {

            const id =
                obtenerIdMarca(marca);


            const fila =
                document.createElement("tr");


            // NOMBRE

            const tdNombre =
                document.createElement("td");

            const spanNombre =
                document.createElement("span");

            spanNombre.className =
                "nombre-marca";

            spanNombre.textContent =
                marca.nombre;

            tdNombre.appendChild(
                spanNombre
            );


            // ESTADO

            const tdEstado =
                document.createElement("td");

            const spanEstado =
                document.createElement("span");

            spanEstado.className =
                marca.activa
                    ? "estado estado-activa"
                    : "estado estado-inactiva";

            spanEstado.textContent =
                marca.activa
                    ? "Activa"
                    : "Inactiva";

            tdEstado.appendChild(
                spanEstado
            );


            // ACCIONES

            const tdAcciones =
                document.createElement("td");

            tdAcciones.className =
                "columna-acciones";


            const contenedorAcciones =
                document.createElement("div");

            contenedorAcciones.className =
                "acciones-tabla";


            // EDITAR

            const btnEditar =
                document.createElement("button");

            btnEditar.type =
                "button";

            btnEditar.className =
                "btn-tabla btn-editar";

            btnEditar.textContent =
                "Editar";

            btnEditar.addEventListener(
                "click",
                () => editarMarca(id)
            );


            // ACTIVAR / DESACTIVAR

            const btnEstado =
                document.createElement("button");

            btnEstado.type =
                "button";


            if (marca.activa) {

                btnEstado.className =
                    "btn-tabla btn-desactivar";

                btnEstado.textContent =
                    "Desactivar";

                btnEstado.addEventListener(
                    "click",
                    () =>
                        solicitarCambioEstado(
                            id,
                            false,
                            marca.nombre
                        )
                );

            } else {

                btnEstado.className =
                    "btn-tabla btn-activar";

                btnEstado.textContent =
                    "Activar";

                btnEstado.addEventListener(
                    "click",
                    () =>
                        solicitarCambioEstado(
                            id,
                            true,
                            marca.nombre
                        )
                );
            }


            contenedorAcciones.appendChild(
                btnEditar
            );

            contenedorAcciones.appendChild(
                btnEstado
            );


            tdAcciones.appendChild(
                contenedorAcciones
            );


            fila.appendChild(
                tdNombre
            );

            fila.appendChild(
                tdEstado
            );

            fila.appendChild(
                tdAcciones
            );


            tablaMarcas.appendChild(
                fila
            );
        }
    );
}


// ==========================================
// CREAR / ACTUALIZAR
// ==========================================

formMarca.addEventListener(
    "submit",
    async event => {

        event.preventDefault();


        const id =
            marcaId.value;


        const nombreMarca =
            nombre.value.trim();


        if (!nombreMarca) {

            mostrarMensaje(
                "El nombre es obligatorio.",
                "error"
            );

            nombre.focus();

            return;
        }


        if (nombreMarca.length > 100) {

            mostrarMensaje(
                "El nombre no puede superar los 100 caracteres.",
                "error"
            );

            nombre.focus();

            return;
        }


        const contieneLetra =
            /\p{L}/u.test(
                nombreMarca
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
                nombreMarca
        };


        try {

            bloquearFormulario(true);


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
                    "Marca actualizada correctamente.",
                    "exito"
                );

            } else {

                mostrarMensaje(
                    "Marca creada correctamente.",
                    "exito"
                );
            }


            cerrarFormulario();


            await cargarMarcas();


        } catch (error) {

            console.error(error);


            mostrarMensaje(
                error.message ||
                "Ocurrió un error al guardar la marca.",
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

function editarMarca(id) {

    const marca =
        marcas.find(
            marca =>
                obtenerIdMarca(marca) === id
        );


    if (!marca) {

        mostrarMensaje(
            "No se encontró la marca.",
            "error"
        );

        return;
    }


    marcaId.value =
        id;


    nombre.value =
        marca.nombre;


    tituloFormulario.textContent =
        "Editar marca";


    textoFormulario.textContent =
        "Modificá el nombre de la marca.";


    btnGuardar.textContent =
        "Guardar cambios";


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
// NUEVA MARCA
// ==========================================

btnNuevaMarca.addEventListener(
    "click",
    () => {

        limpiarFormulario();


        tituloFormulario.textContent =
            "Nueva marca";


        textoFormulario.textContent =
            "Completá los datos de la nueva marca.";


        btnGuardar.textContent =
            "Guardar marca";


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
    nombreMarca
) {

    modalTitulo.textContent =
        activa
            ? "Activar marca"
            : "Desactivar marca";


    modalTexto.textContent =
        activa
            ? `¿Querés activar la marca "${nombreMarca}"?`
            : `¿Querés desactivar la marca "${nombreMarca}"?`;


    btnModalConfirmar.textContent =
        activa
            ? "Activar"
            : "Desactivar";


    accionConfirmacion =
        () =>
            cambiarEstado(
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

        btnModalConfirmar.disabled =
            true;


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
                ? "Marca activada correctamente."
                : "Marca desactivada correctamente.",

            "exito"
        );


        await cargarMarcas();


    } catch (error) {

        console.error(error);


        mostrarMensaje(
            error.message ||
            "No se pudo cambiar el estado de la marca.",
            "error"
        );


    } finally {

        btnModalConfirmar.disabled =
            false;
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


        const textoLimpio =
            texto.trim();


        if (
            textoLimpio !== "" &&
            !/\p{L}/u.test(textoLimpio)
        ) {

            mostrarMensaje(
                "La búsqueda debe contener al menos una letra.",
                "error"
            );

            return;
        }


        cargarMarcas();
    }
);


// ENTER PARA BUSCAR

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
// FILTRAR
// ==========================================

filtroEstado.addEventListener(
    "change",
    () => {

        cargarMarcas();
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

        cargarMarcas();
    }
);


// ==========================================
// CERRAR / CANCELAR FORMULARIO
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

    marcaId.value = "";

    nombre.value = "";
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

            const accion =
                accionConfirmacion;

            accionConfirmacion = null;

            accion();
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
// ERROR DEL BACKEND
// ==========================================

async function obtenerMensajeError(
    response
) {

    try {

        const data =
            await response.json();


        // Errores de @Valid
        if (
            data.errors &&
            Object.keys(data.errors).length > 0
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

        return "No se encontró la marca.";
    }


    if (response.status === 409) {

        return "Ya existe una marca con ese nombre.";
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


    btnGuardar.disabled =
        bloquear;


    btnCancelar.disabled =
        bloquear;


    btnGuardar.textContent =
        bloquear
            ? "Guardando..."
            : marcaId.value
                ? "Guardar cambios"
                : "Guardar marca";
}


// ==========================================
// CERRAR SESIÓN
// ==========================================

btnCerrarSesion.addEventListener(
    "click",
    () => {

        Auth.cerrarSesion();
    }
);