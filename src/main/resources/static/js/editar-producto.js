(() => {

    "use strict";


    const API_PRODUCTOS =
        "/api/productos";

    const API_CATEGORIAS =
        "/api/categorias?activa=true";

    const API_MARCAS =
        "/api/marcas?activa=true";

    const API_GRUPOS =
        "/api/grupos-catalogo?activo=true";


    let idProducto = null;

    let guardando = false;


    document.addEventListener(
        "DOMContentLoaded",
        iniciar
    );


    // =====================================
    // INICIAR
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


        if (idProducto === null) {

            mostrarMensaje(
                "No se indicó qué producto se desea modificar.",
                "error"
            );

            bloquearFormulario(true);

            return;
        }


        cargarUsuario();

        configurarEventos();


        try {

            bloquearFormulario(true);

            btnGuardar.textContent =
                "Cargando...";


            await cargarOpciones();

            await cargarProducto();


        } catch (error) {

            console.error(error);


            mostrarMensaje(
                error.message ||
                "No se pudo cargar el producto.",
                "error"
            );


        } finally {

            bloquearFormulario(false);
        }
    }


    // =====================================
    // OBTENER ID DESDE URL
    // =====================================

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
                .filter(Boolean)
                .join(" ");


        document.getElementById(
            "nombreUsuario"
        ).textContent =
            nombreCompleto ||
            usuario.email ||
            "Administrador";
    }


    // =====================================
    // ELEMENTOS
    // =====================================

    const formProducto =
        document.getElementById(
            "formProducto"
        );

    const mensaje =
        document.getElementById(
            "mensaje"
        );

    const nombre =
        document.getElementById(
            "nombre"
        );

    const descripcion =
        document.getElementById(
            "descripcion"
        );

    const contadorDescripcion =
        document.getElementById(
            "contadorDescripcion"
        );

    const categoria =
        document.getElementById(
            "categoria"
        );

    const marca =
        document.getElementById(
            "marca"
        );

    const grupoCatalogo =
        document.getElementById(
            "grupoCatalogo"
        );

    const precioCosto =
        document.getElementById(
            "precioCosto"
        );

    const precioVenta =
        document.getElementById(
            "precioVenta"
        );

    const stockActual =
        document.getElementById(
            "stockActual"
        );

    const stockMinimo =
        document.getElementById(
            "stockMinimo"
        );

    const cantidadMinimaMayorista =
        document.getElementById(
            "cantidadMinimaMayorista"
        );

    const porcentajeMayorista =
        document.getElementById(
            "porcentajeMayorista"
        );

    const enOferta =
        document.getElementById(
            "enOferta"
        );

    const contenedorPorcentajeOferta =
        document.getElementById(
            "contenedorPorcentajeOferta"
        );

    const porcentajeOferta =
        document.getElementById(
            "porcentajeOferta"
        );

    const estado =
        document.getElementById(
            "estado"
        );

    const publicadoOnline =
        document.getElementById(
            "publicadoOnline"
        );

    const btnGuardar =
        document.getElementById(
            "btnGuardar"
        );

    const btnCancelar =
        document.getElementById(
            "btnCancelar"
        );

    const btnCerrarSesion =
        document.getElementById(
            "btnCerrarSesion"
        );


    // =====================================
    // EVENTOS
    // =====================================

    function configurarEventos() {

        formProducto.addEventListener(
            "submit",
            guardarCambios
        );


        descripcion.addEventListener(
            "input",
            actualizarContador
        );


        enOferta.addEventListener(
            "change",
            actualizarOferta
        );


        btnCancelar.addEventListener(
            "click",
            cancelar
        );


        btnCerrarSesion.addEventListener(
            "click",
            Auth.cerrarSesion
        );
    }


    // =====================================
    // CARGAR OPCIONES
    // =====================================

    async function cargarOpciones() {

        const [
            categorias,
            marcas,
            grupos
        ] =
            await Promise.all([

                obtenerOpciones(
                    API_CATEGORIAS
                ),

                obtenerOpciones(
                    API_MARCAS
                ),

                obtenerOpciones(
                    API_GRUPOS
                )

            ]);


        cargarSelect(
            categoria,
            categorias,
            "id",
            "nombre",
            "Sin categoría"
        );


        cargarSelect(
            marca,
            marcas,
            "id_marca",
            "nombre",
            "Sin marca"
        );


        cargarSelect(
            grupoCatalogo,
            grupos,
            "idGrupoCatalogo",
            "nombreVisible",
            "Sin grupo"
        );
    }


    async function obtenerOpciones(
        url
    ) {

        const response =
            await Auth.fetchAutenticado(
                url
            );


        if (!response) {

            throw new Error(
                "No se pudo completar la solicitud."
            );
        }


        if (!response.ok) {

            throw new Error(
                await obtenerMensajeError(
                    response
                )
            );
        }


        return await response.json();
    }


    function cargarSelect(
        select,
        datos,
        propiedadId,
        propiedadNombre,
        textoVacio
    ) {

        select.innerHTML = "";


        const opcionVacia =
            document.createElement(
                "option"
            );


        opcionVacia.value = "";

        opcionVacia.textContent =
            textoVacio;


        select.appendChild(
            opcionVacia
        );


        datos.forEach(
            item => {

                const option =
                    document.createElement(
                        "option"
                    );


                option.value =
                    item[propiedadId];


                option.textContent =
                    item[propiedadNombre];


                select.appendChild(
                    option
                );
            }
        );
    }


    // =====================================
    // CARGAR PRODUCTO ACTUAL
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


        cargarDatosProducto(
            producto
        );
    }


    function cargarDatosProducto(
        producto
    ) {

        nombre.value =
            producto.nombre ?? "";


        descripcion.value =
            producto.descripcion ?? "";


        seleccionarValor(
            categoria,
            producto.idCategoria
        );


        seleccionarValor(
            marca,
            producto.idMarca
        );


        seleccionarValor(
            grupoCatalogo,
            producto.idGrupoCatalogo
        );


        precioCosto.value =
            producto.precioCosto ?? "";


        precioVenta.value =
            producto.precioVenta ?? "";


        stockActual.value =
            producto.stockActual ?? 0;


        stockMinimo.value =
            producto.stockMinimo ?? "";


        cantidadMinimaMayorista.value =
            producto.cantidadMinimaMayorista
            ?? "";


        porcentajeMayorista.value =
            producto.porcentajeDescuentoMayorista
            ?? "";


        enOferta.checked =
            Boolean(
                producto.enOferta
            );


        porcentajeOferta.value =
            producto.porcentajeDescuentoOferta
            ?? "";


        estado.value =
            producto.estado;


        publicadoOnline.checked =
            Boolean(
                producto.publicadoOnline
            );


        actualizarOferta();

        actualizarContador();
    }


    function seleccionarValor(
        select,
        valor
    ) {

        if (valor === null || valor === undefined) {

            select.value = "";

            return;
        }


        const existeOpcion =
            Array.from(
                select.options
            )
                .some(
                    option =>
                        Number(
                            option.value
                        ) === Number(valor)
                );


        if (existeOpcion) {

            select.value =
                String(valor);

            return;
        }


        const option =
            document.createElement(
                "option"
            );


        option.value =
            valor;

        option.textContent =
            `ID ${valor} - actualmente no disponible`;

        option.selected =
            true;


        select.appendChild(
            option
        );
    }


    // =====================================
    // GUARDAR CAMBIOS
    // =====================================

    async function guardarCambios(
        event
    ) {

        event.preventDefault();


        if (guardando) {
            return;
        }


        ocultarMensaje();

        limpiarErrores();


        if (!validarFormulario()) {
            return;
        }


        const producto =
            construirProducto();


        try {

            guardando = true;

            bloquearFormulario(true);

            btnGuardar.textContent =
                "Guardando...";


            const response =
                await Auth.fetchAutenticado(

                    `${API_PRODUCTOS}/${idProducto}`,

                    {

                        method: "PUT",

                        headers: {

                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                producto
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


            const productoActualizado =
                await response.json();


            cargarDatosProducto(
                productoActualizado
            );


            mostrarMensaje(
                `Producto "${productoActualizado.nombre}" actualizado correctamente.`,
                "exito"
            );


        } catch (error) {

            console.error(error);


            mostrarMensaje(
                error.message ||
                "No se pudo modificar el producto.",
                "error"
            );


        } finally {

            guardando = false;

            bloquearFormulario(false);
        }
    }


    // =====================================
    // CONSTRUIR REQUEST
    // =====================================

    function construirProducto() {

        return {

            nombre:
                nombre.value.trim(),


            descripcion:
                valorTextoONull(
                    descripcion.value
                ),


            idCategoria:
                valorEnteroONull(
                    categoria.value
                ),


            idMarca:
                valorEnteroONull(
                    marca.value
                ),


            idGrupoCatalogo:
                valorEnteroONull(
                    grupoCatalogo.value
                ),


            precioCosto:
                valorDecimalONull(
                    precioCosto.value
                ),


            precioVenta:
                Number(
                    precioVenta.value
                ),


            stockMinimo:
                valorEnteroONull(
                    stockMinimo.value
                ),


            cantidadMinimaMayorista:
                valorEnteroONull(
                    cantidadMinimaMayorista.value
                ),


            porcentajeDescuentoMayorista:
                valorDecimalONull(
                    porcentajeMayorista.value
                ),


            enOferta:
                enOferta.checked,


            porcentajeDescuentoOferta:
                enOferta.checked
                    ? valorDecimalONull(
                        porcentajeOferta.value
                    )
                    : null,


            estado:
                estado.value,


            publicadoOnline:
                publicadoOnline.checked
        };
    }


    // =====================================
    // VALIDACIONES
    // =====================================

    function validarFormulario() {

        let valido = true;


        const nombreLimpio =
            nombre.value.trim();


        if (nombreLimpio === "") {

            marcarError(
                nombre,
                "errorNombre",
                "El nombre del producto es obligatorio."
            );

            valido = false;

        } else if (
            !/\p{L}/u.test(
                nombreLimpio
            )
        ) {

            marcarError(
                nombre,
                "errorNombre",
                "El nombre debe contener al menos una letra."
            );

            valido = false;
        }


        if (
            descripcion.value.length > 500
        ) {

            marcarError(
                descripcion,
                "errorDescripcion",
                "La descripción no puede superar los 500 caracteres."
            );

            valido = false;
        }


        if (
            precioVenta.value === ""
        ) {

            marcarError(
                precioVenta,
                "errorPrecioVenta",
                "El precio de venta es obligatorio."
            );

            valido = false;


        } else if (
            Number(
                precioVenta.value
            ) <= 0
        ) {

            marcarError(
                precioVenta,
                "errorPrecioVenta",
                "El precio de venta debe ser mayor a cero."
            );

            valido = false;
        }


        if (
            precioCosto.value !== ""
            &&
            Number(
                precioCosto.value
            ) < 0
        ) {

            marcarError(
                precioCosto,
                "errorPrecioCosto",
                "El precio de costo no puede ser negativo."
            );

            valido = false;
        }


        if (
            !validarEnteroNoNegativo(
                stockMinimo,
                "errorStockMinimo",
                "El stock mínimo"
            )
        ) {

            valido = false;
        }


        const tieneCantidadMayorista =
            cantidadMinimaMayorista.value
            !== "";


        const tienePorcentajeMayorista =
            porcentajeMayorista.value
            !== "";


        if (
            tieneCantidadMayorista
            !==
            tienePorcentajeMayorista
        ) {

            if (
                !tieneCantidadMayorista
            ) {

                marcarError(
                    cantidadMinimaMayorista,
                    "errorCantidadMayorista",
                    "Debés indicar la cantidad mínima."
                );
            }


            if (
                !tienePorcentajeMayorista
            ) {

                marcarError(
                    porcentajeMayorista,
                    "errorPorcentajeMayorista",
                    "Debés indicar el porcentaje."
                );
            }


            valido = false;
        }


        if (
            tieneCantidadMayorista
            &&
            (
                !Number.isInteger(
                    Number(
                        cantidadMinimaMayorista.value
                    )
                )
                ||
                Number(
                    cantidadMinimaMayorista.value
                ) <= 0
            )
        ) {

            marcarError(
                cantidadMinimaMayorista,
                "errorCantidadMayorista",
                "La cantidad mínima debe ser un entero mayor a cero."
            );


            valido = false;
        }


        if (
            tienePorcentajeMayorista
            &&
            !porcentajeValido(
                porcentajeMayorista.value
            )
        ) {

            marcarError(
                porcentajeMayorista,
                "errorPorcentajeMayorista",
                "El porcentaje debe ser mayor a 0 y menor o igual a 100."
            );


            valido = false;
        }


        if (
            enOferta.checked
        ) {

            if (
                porcentajeOferta.value
                === ""
            ) {

                marcarError(
                    porcentajeOferta,
                    "errorPorcentajeOferta",
                    "Debés indicar el porcentaje de oferta."
                );


                valido = false;


            } else if (
                !porcentajeValido(
                    porcentajeOferta.value
                )
            ) {

                marcarError(
                    porcentajeOferta,
                    "errorPorcentajeOferta",
                    "El porcentaje debe ser mayor a 0 y menor o igual a 100."
                );


                valido = false;
            }
        }


        if (
            estado.value === ""
        ) {

            marcarError(
                estado,
                "errorEstado",
                "Debés seleccionar un estado."
            );


            valido = false;
        }


        if (!valido) {

            mostrarMensaje(
                "Revisá los campos marcados antes de guardar.",
                "error"
            );
        }


        return valido;
    }


    function validarEnteroNoNegativo(
        input,
        errorId,
        nombreCampo
    ) {

        if (
            input.value === ""
        ) {

            return true;
        }


        const valor =
            Number(
                input.value
            );


        if (
            !Number.isInteger(
                valor
            )
            ||
            valor < 0
        ) {

            marcarError(
                input,
                errorId,
                `${nombreCampo} debe ser un número entero mayor o igual a cero.`
            );


            return false;
        }


        return true;
    }


    function porcentajeValido(
        valor
    ) {

        const numero =
            Number(
                valor
            );


        return (
            Number.isFinite(
                numero
            )
            &&
            numero > 0
            &&
            numero <= 100
        );
    }


    // =====================================
    // OFERTA
    // =====================================

    function actualizarOferta() {

        if (
            enOferta.checked
        ) {

            contenedorPorcentajeOferta
                .classList.remove(
                    "oculto"
                );


            porcentajeOferta.disabled =
                false;


        } else {

            contenedorPorcentajeOferta
                .classList.add(
                    "oculto"
                );


            porcentajeOferta.value =
                "";


            porcentajeOferta.disabled =
                true;


            document.getElementById(
                "errorPorcentajeOferta"
            ).textContent =
                "";
        }
    }


    // =====================================
    // ERRORES DE CAMPOS
    // =====================================

    function marcarError(
        input,
        errorId,
        texto
    ) {

        input.classList.add(
            "campo-invalido"
        );


        document.getElementById(
            errorId
        ).textContent =
            texto;
    }


    function limpiarErrores() {

        document
            .querySelectorAll(
                ".campo-invalido"
            )
            .forEach(
                elemento =>
                    elemento
                        .classList
                        .remove(
                            "campo-invalido"
                        )
            );


        document
            .querySelectorAll(
                ".error-campo"
            )
            .forEach(
                elemento =>
                    elemento.textContent =
                        ""
            );
    }


    // =====================================
    // HELPERS
    // =====================================

    function valorTextoONull(
        valor
    ) {

        const limpio =
            valor.trim();


        return limpio === ""
            ? null
            : limpio;
    }


    function valorEnteroONull(
        valor
    ) {

        return valor === ""
            ? null
            : Number(
                valor
            );
    }


    function valorDecimalONull(
        valor
    ) {

        return valor === ""
            ? null
            : Number(
                valor
            );
    }


    function actualizarContador() {

        contadorDescripcion.textContent =
            descripcion.value.length;
    }


    // =====================================
    // CANCELAR
    // =====================================

    function cancelar() {

        if (guardando) {
            return;
        }


        window.location.href =
            "/admin/index.html";
    }


    // =====================================
    // BLOQUEAR FORMULARIO
    // =====================================

    function bloquearFormulario(
        bloquear
    ) {

        formProducto
            .querySelectorAll(
                "input, textarea, select, button"
            )
            .forEach(
                elemento => {

                    if (
                        elemento.id
                        ===
                        "stockActual"
                    ) {

                        elemento.disabled =
                            true;

                        return;
                    }


                    elemento.disabled =
                        bloquear;
                }
            );


        if (!bloquear) {

            btnGuardar.disabled =
                false;

            btnCancelar.disabled =
                false;

            btnCerrarSesion.disabled =
                false;


            btnGuardar.textContent =
                "Guardar cambios";


            actualizarOferta();


            stockActual.disabled =
                true;
        }
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


        window.scrollTo({

            top: 0,

            behavior: "smooth"
        });
    }


    function ocultarMensaje() {

        mensaje.classList.add(
            "oculto"
        );
    }


    // =====================================
    // ERRORES DEL BACKEND
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

            return "No tenés permisos para modificar productos.";
        }


        if (
            response.status === 404
        ) {

            return "No se encontró el producto o una de las opciones seleccionadas.";
        }


        return "Ocurrió un error inesperado.";
    }

})();