(() => {

    "use strict";


    const API_PRODUCTOS =
        "/api/productos";

    const API_CATEGORIAS =
        "/api/categorias";

    const API_MARCAS =
        "/api/marcas";


    let productosActuales =
        [];


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


    const btnCerrarSesion =
        document.getElementById(
            "btnCerrarSesion"
        );


    const btnVolverPanel =
        document.getElementById(
            "btnVolverPanel"
        );


    const formFiltros =
        document.getElementById(
            "formFiltros"
        );


    const idProducto =
        document.getElementById(
            "idProducto"
        );


    const nombre =
        document.getElementById(
            "nombre"
        );


    const categoria =
        document.getElementById(
            "categoria"
        );


    const marca =
        document.getElementById(
            "marca"
        );


    const estado =
        document.getElementById(
            "estado"
        );


    const stock =
        document.getElementById(
            "stock"
        );


    const btnLimpiar =
        document.getElementById(
            "btnLimpiar"
        );


    const productosBody =
        document.getElementById(
            "productosBody"
        );


    const contenedorTabla =
        document.getElementById(
            "contenedorTabla"
        );


    const estadoVacio =
        document.getElementById(
            "estadoVacio"
        );


    const cantidadResultados =
        document.getElementById(
            "cantidadResultados"
        );


    const mensaje =
        document.getElementById(
            "mensaje"
        );


    const detalleProducto =
        document.getElementById(
            "detalleProducto"
        );


    const btnCerrarDetalle =
        document.getElementById(
            "btnCerrarDetalle"
        );


    // =====================================
    // INICIO
    // =====================================

    async function iniciar() {

        if (
            !Auth.requerirRol(
                "VENDEDOR",
                "ADMINISTRADOR"
            )
        ) {

            return;
        }


        configurarUsuario();

        configurarEventos();


        try {

            await Promise.all(
                [
                    cargarCategorias(),
                    cargarMarcas()
                ]
            );


            await consultarProductos();


        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                "No se pudo cargar la consulta de productos."
            );
        }
    }


    function configurarUsuario() {

        const usuario =
            Auth.obtenerUsuario();

        const rol =
            Auth.obtenerRol();


        if (usuario) {

            nombreUsuario.textContent =
                usuario.nombre
                ||
                usuario.email
                ||
                "Usuario";
        }


        if (
            rol === "ADMINISTRADOR"
        ) {

            btnVolverPanel.href =
                "/admin/index.html";


        } else {

            btnVolverPanel.href =
                "/vendedor/index.html";
        }
    }


    function configurarEventos() {

        btnCerrarSesion.addEventListener(
            "click",
            Auth.cerrarSesion
        );


        formFiltros.addEventListener(
            "submit",
            buscar
        );


        btnLimpiar.addEventListener(
            "click",
            limpiarFiltros
        );


        btnCerrarDetalle.addEventListener(
            "click",
            cerrarDetalle
        );
    }


    // =====================================
    // CATEGORÍAS
    // =====================================

    async function cargarCategorias() {

        const response =
            await Auth.fetchAutenticado(
                API_CATEGORIAS
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


        const categorias =
            await response.json();


        categorias
            .sort(
                (a, b) =>
                    a.nombre.localeCompare(
                        b.nombre,
                        "es"
                    )
            )
            .forEach(
                item => {

                    const option =
                        document.createElement(
                            "option"
                        );


                    option.value =
                        item.id;


                    option.textContent =
                        item.activa
                            ? item.nombre
                            : `${item.nombre} (inactiva)`;


                    categoria.appendChild(
                        option
                    );
                }
            );
    }


    // =====================================
    // MARCAS
    // =====================================

    async function cargarMarcas() {

        const response =
            await Auth.fetchAutenticado(
                API_MARCAS
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


        const marcas =
            await response.json();


        marcas
            .sort(
                (a, b) =>
                    a.nombre.localeCompare(
                        b.nombre,
                        "es"
                    )
            )
            .forEach(
                item => {

                    const option =
                        document.createElement(
                            "option"
                        );


                    option.value =
                        item.id_marca;


                    option.textContent =
                        item.activa
                            ? item.nombre
                            : `${item.nombre} (inactiva)`;


                    marca.appendChild(
                        option
                    );
                }
            );
    }


    // =====================================
    // BUSCAR
    // =====================================

    async function buscar(
        event
    ) {

        event.preventDefault();


        ocultarMensaje();

        cerrarDetalle();


        const error =
            validarFiltros();


        if (error) {

            mostrarMensaje(
                error
            );

            return;
        }


        await consultarProductos();
    }


    function validarFiltros() {

        const id =
            idProducto.value.trim();


        if (
            id !== ""
            &&
            !/^\d+$/.test(
                id
            )
        ) {

            return "El ID debe ser un número entero mayor a cero.";
        }


        if (
            id !== ""
            &&
            Number(id) <= 0
        ) {

            return "El ID debe ser un número entero mayor a cero.";
        }


        if (
            nombre.value !== ""
            &&
            nombre.value.trim() === ""
        ) {

            return "El nombre de búsqueda no puede contener solo espacios.";
        }


        const nombreLimpio =
            nombre.value.trim();


        if (
            nombreLimpio !== ""
            &&
            !/\p{L}/u.test(
                nombreLimpio
            )
        ) {

            return "El nombre de búsqueda debe contener al menos una letra.";
        }


        return null;
    }


    async function consultarProductos() {

        const parametros =
            construirParametros();


        const url =
            parametros.toString()
                ? `${API_PRODUCTOS}?${parametros.toString()}`
                : API_PRODUCTOS;


        const response =
            await Auth.fetchAutenticado(
                url
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


        productosActuales =
            await response.json();


        renderizarProductos(
            productosActuales
        );
    }


    function construirParametros() {

        const parametros =
            new URLSearchParams();


        const id =
            idProducto.value.trim();


        const nombreLimpio =
            nombre.value.trim();


        if (id !== "") {

            parametros.set(
                "idProducto",
                id
            );
        }


        if (nombreLimpio !== "") {

            parametros.set(
                "nombre",
                nombreLimpio
            );
        }


        if (
            categoria.value !== ""
        ) {

            parametros.set(
                "idCategoria",
                categoria.value
            );
        }


        if (
            marca.value !== ""
        ) {

            parametros.set(
                "idMarca",
                marca.value
            );
        }


        if (
            estado.value !== ""
        ) {

            parametros.set(
                "estado",
                estado.value
            );
        }


        if (
            stock.value === "bajo"
        ) {

            parametros.set(
                "bajoStock",
                "true"
            );
        }


        return parametros;
    }


    // =====================================
    // RENDER
    // =====================================

    function renderizarProductos(
        productos
    ) {

        productosBody.innerHTML =
            "";


        cantidadResultados.textContent =
            `${productos.length} producto(s) encontrado(s).`;


        if (
            productos.length === 0
        ) {

            contenedorTabla.classList.add(
                "oculto"
            );


            estadoVacio.textContent =
                hayFiltrosAplicados()
                    ? "No se encontraron productos con los criterios seleccionados."
                    : "No existen productos cargados.";


            estadoVacio.classList.remove(
                "oculto"
            );


            return;
        }


        estadoVacio.classList.add(
            "oculto"
        );


        contenedorTabla.classList.remove(
            "oculto"
        );


        productos.forEach(
            producto => {

                productosBody.appendChild(
                    crearFilaProducto(
                        producto
                    )
                );
            }
        );
    }


    function crearFilaProducto(
        producto
    ) {

        const fila =
            document.createElement(
                "tr"
            );


        agregarCelda(
            fila,
            "ID",
            producto.idProducto
        );


        agregarCelda(
            fila,
            "Producto",
            producto.nombre
        );


        agregarCelda(
            fila,
            "Categoría",
            producto.categoria
                || "Sin categoría"
        );


        agregarCelda(
            fila,
            "Marca",
            producto.marca
                || "Sin marca"
        );


        agregarCelda(
            fila,
            "Precio",
            formatearPrecio(
                producto.precioVenta
            )
        );


        agregarCelda(
            fila,
            "Stock",
            producto.stockActual
        );


        agregarCelda(
            fila,
            "Mínimo",
            producto.stockMinimo
        );


        agregarCeldaBadge(
            fila,
            "Estado",
            obtenerTextoEstado(
                producto.estado
            ),
            obtenerClaseEstado(
                producto.estado
            )
        );


        agregarCeldaBadge(
            fila,
            "Disponibilidad",
            obtenerTextoStock(
                producto.estadoStock
            ),
            obtenerClaseStock(
                producto.estadoStock
            )
        );


        const celdaAccion =
            document.createElement(
                "td"
            );


        celdaAccion.dataset.label =
            "Acción";


        const boton =
            document.createElement(
                "button"
            );


        boton.type =
            "button";


        boton.className =
            "btn btn-light";


        boton.textContent =
            "Ver información";


        boton.addEventListener(
            "click",
            () =>
                mostrarDetalle(
                    producto
                )
        );


        celdaAccion.appendChild(
            boton
        );


        fila.appendChild(
            celdaAccion
        );


        return fila;
    }


    function agregarCelda(
        fila,
        etiqueta,
        valor
    ) {

        const celda =
            document.createElement(
                "td"
            );


        celda.dataset.label =
            etiqueta;


        celda.textContent =
            valor;


        fila.appendChild(
            celda
        );
    }


    function agregarCeldaBadge(
        fila,
        etiqueta,
        texto,
        clase
    ) {

        const celda =
            document.createElement(
                "td"
            );


        celda.dataset.label =
            etiqueta;


        const badge =
            document.createElement(
                "span"
            );


        badge.className =
            `badge ${clase}`;


        badge.textContent =
            texto;


        celda.appendChild(
            badge
        );


        fila.appendChild(
            celda
        );
    }


    // =====================================
    // DETALLE
    // =====================================

    function mostrarDetalle(
        producto
    ) {

        document.getElementById(
            "detalleId"
        ).textContent =
            producto.idProducto;


        document.getElementById(
            "detalleNombre"
        ).textContent =
            producto.nombre;


        document.getElementById(
            "detalleCategoria"
        ).textContent =
            producto.categoria
            || "Sin categoría";


        document.getElementById(
            "detalleMarca"
        ).textContent =
            producto.marca
            || "Sin marca";


        document.getElementById(
            "detallePrecio"
        ).textContent =
            formatearPrecio(
                producto.precioVenta
            );


        document.getElementById(
            "detalleStockActual"
        ).textContent =
            producto.stockActual;


        document.getElementById(
            "detalleStockMinimo"
        ).textContent =
            producto.stockMinimo;


        document.getElementById(
            "detalleEstado"
        ).textContent =
            obtenerTextoEstado(
                producto.estado
            );


        document.getElementById(
            "detalleDisponibilidad"
        ).textContent =
            obtenerTextoStock(
                producto.estadoStock
            );


        detalleProducto.classList.remove(
            "oculto"
        );


        detalleProducto.scrollIntoView(
            {
                behavior:
                    "smooth",

                block:
                    "start"
            }
        );
    }


    function cerrarDetalle() {

        detalleProducto.classList.add(
            "oculto"
        );
    }


    // =====================================
    // LIMPIAR
    // =====================================

    async function limpiarFiltros() {

        formFiltros.reset();

        ocultarMensaje();

        cerrarDetalle();


        await consultarProductos();
    }


    function hayFiltrosAplicados() {

        return (
            idProducto.value.trim() !== ""
            ||
            nombre.value.trim() !== ""
            ||
            categoria.value !== ""
            ||
            marca.value !== ""
            ||
            estado.value !== ""
            ||
            stock.value !== ""
        );
    }


    // =====================================
    // ESTADOS
    // =====================================

    function obtenerTextoEstado(
        valor
    ) {

        if (
            valor === "ACTIVO"
        ) {

            return "Activo";
        }


        if (
            valor === "PAUSADO"
        ) {

            return "Pausado";
        }


        if (
            valor === "DISCONTINUADO"
        ) {

            return "Discontinuado";
        }


        return valor;
    }


    function obtenerClaseEstado(
        valor
    ) {

        if (
            valor === "ACTIVO"
        ) {

            return "badge-activo";
        }


        if (
            valor === "PAUSADO"
        ) {

            return "badge-pausado";
        }


        return "badge-discontinuado";
    }


    function obtenerTextoStock(
        valor
    ) {

        if (
            valor === "SIN_STOCK"
        ) {

            return "Sin stock";
        }


        if (
            valor === "BAJO_STOCK"
        ) {

            return "Bajo stock";
        }


        if (
            valor === "LIMITE_MINIMO"
        ) {

            return "Límite mínimo";
        }


        return "Disponible";
    }


    function obtenerClaseStock(
        valor
    ) {

        if (
            valor === "SIN_STOCK"
        ) {

            return "badge-sin-stock";
        }


        if (
            valor === "BAJO_STOCK"
        ) {

            return "badge-bajo-stock";
        }


        if (
            valor === "LIMITE_MINIMO"
        ) {

            return "badge-limite";
        }


        return "badge-disponible";
    }


    // =====================================
    // PRECIO
    // =====================================

    function formatearPrecio(
        valor
    ) {

        return new Intl.NumberFormat(
            "es-AR",
            {
                style:
                    "currency",

                currency:
                    "ARS"
            }
        ).format(
            Number(
                valor
            )
        );
    }


    // =====================================
    // MENSAJES
    // =====================================

    function mostrarMensaje(
        texto
    ) {

        mensaje.textContent =
            texto;


        mensaje.className =
            "mensaje mensaje-error";


        mensaje.classList.remove(
            "oculto"
        );
    }


    function ocultarMensaje() {

        mensaje.classList.add(
            "oculto"
        );
    }


    // =====================================
    // ERROR BACKEND
    // =====================================

    async function obtenerMensajeError(
        response
    ) {

        try {

            const data =
                await response.json();


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

            return "Los filtros ingresados no son válidos.";
        }


        if (
            response.status === 403
        ) {

            return "No tenés permisos para consultar productos.";
        }


        return "No se pudo realizar la consulta.";
    }

})();