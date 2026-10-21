(() => {

    'use strict';


    const API =
        '/api/vendedor/clientes';


    if (
        !Auth.requerirRol(
            'VENDEDOR',
            'ADMINISTRADOR'
        )
    ) {

        return;
    }


    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );

    const btnCerrarSesion =
        document.getElementById(
            'btnCerrarSesion'
        );

    const btnVolverPanel =
        document.getElementById(
            'btnVolverPanel'
        );

    const formBusqueda =
        document.getElementById(
            'formBusqueda'
        );

    const inputBusqueda =
        document.getElementById(
            'busqueda'
        );

    const btnLimpiar =
        document.getElementById(
            'btnLimpiar'
        );

    const mensaje =
        document.getElementById(
            'mensaje'
        );

    const cargando =
        document.getElementById(
            'cargando'
        );

    const sinClientes =
        document.getElementById(
            'sinClientes'
        );

    const tituloSinClientes =
        document.getElementById(
            'tituloSinClientes'
        );

    const textoSinClientes =
        document.getElementById(
            'textoSinClientes'
        );

    const contenedorClientes =
        document.getElementById(
            'contenedorClientes'
        );


    iniciar();


    function iniciar() {

        mostrarUsuario();

        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        btnVolverPanel.addEventListener(
            'click',
            volverAlPanel
        );


        formBusqueda.addEventListener(
            'submit',
            buscar
        );


        btnLimpiar.addEventListener(
            'click',
            limpiarBusqueda
        );


        cargarClientes(
            null
        );
    }


    function mostrarUsuario() {

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


    function volverAlPanel() {

        window.location.href =
            Auth.obtenerRutaPorRol(
                Auth.obtenerRol()
            );
    }


    function buscar(
            event
    ) {

        event.preventDefault();


        const valor =
            inputBusqueda.value;


        if (
            valor !== ''
            &&
            valor.trim() === ''
        ) {

            mostrarError(
                'La búsqueda no puede contener solamente espacios.'
            );

            return;
        }


        if (
            valor.trim() !== ''
            &&
            !/[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9]/.test(
                valor
            )
        ) {

            mostrarError(
                'La búsqueda ingresada no es válida.'
            );

            return;
        }


        cargarClientes(
            valor
        );
    }


    function limpiarBusqueda() {

        inputBusqueda.value =
            '';


        cargarClientes(
            null
        );
    }


    async function cargarClientes(
            busqueda
    ) {

        ocultarMensaje();

        sinClientes.classList.add(
            'oculto'
        );

        contenedorClientes.classList.add(
            'oculto'
        );

        cargando.classList.remove(
            'oculto'
        );


        try {

            let url =
                API;


            if (
                busqueda !== null
            ) {

                url +=
                    `?busqueda=${encodeURIComponent(
                        busqueda
                    )}`;
            }


            const response =
                await Auth.fetchAutenticado(
                    url
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


            const clientes =
                await response.json();


            renderizarClientes(
                clientes,
                busqueda
            );

        } catch (error) {

            mostrarError(
                error.message
                ||
                'No se pudieron consultar los clientes.'
            );

        } finally {

            cargando.classList.add(
                'oculto'
            );
        }
    }


    function renderizarClientes(
            clientes,
            busqueda
    ) {

        contenedorClientes.innerHTML =
            '';


        if (
            !Array.isArray(clientes)
            ||
            clientes.length === 0
        ) {

            if (
                busqueda !== null
                &&
                busqueda.trim() !== ''
            ) {

                tituloSinClientes.textContent =
                    'No se encontraron clientes';

                textoSinClientes.textContent =
                    'No existen clientes que coincidan con la búsqueda.';

            } else {

                tituloSinClientes.textContent =
                    'No hay clientes registrados';

                textoSinClientes.textContent =
                    'Todavía no existen clientes cargados.';
            }


            sinClientes.classList.remove(
                'oculto'
            );

            return;
        }


        clientes.forEach(
            cliente => {

                contenedorClientes.appendChild(
                    crearClienteCard(
                        cliente
                    )
                );
            }
        );


        contenedorClientes.classList.remove(
            'oculto'
        );
    }


    function crearClienteCard(
            cliente
    ) {

        const article =
            document.createElement(
                'article'
            );

        article.className =
            'cliente-card';


        const header =
            document.createElement(
                'div'
            );

        header.className =
            'cliente-header';


        const titulo =
            document.createElement(
                'h3'
            );

        titulo.textContent =
            obtenerNombreCompleto(
                cliente
            );


        const badge =
            document.createElement(
                'span'
            );

        badge.className =
            cliente.estado
                ? 'badge badge-activo'
                : 'badge badge-inactivo';

        badge.textContent =
            cliente.estado
                ? 'Activo'
                : 'Inactivo';


        header.appendChild(
            titulo
        );

        header.appendChild(
            badge
        );

        article.appendChild(
            header
        );


        article.appendChild(
            crearDato(
                'DNI',
                cliente.dni
                || 'No informado'
            )
        );


        article.appendChild(
            crearDato(
                'Teléfono',
                cliente.telefono
                || 'No informado'
            )
        );


        article.appendChild(
            crearDato(
                'Correo',
                cliente.correo
                || 'No informado'
            )
        );


        article.appendChild(
            crearDato(
                'Razón social',
                cliente.razonSocial
                || 'No informada'
            )
        );


        article.appendChild(
            crearDato(
                'Descuento',
                obtenerDescuento(
                    cliente
                )
            )
        );


        article.appendChild(
            crearDato(
                'Nota interna',
                cliente.notaInterna
                    ? 'Sí'
                    : 'No'
            )
        );


        const acciones =
            document.createElement(
                'div'
            );

        acciones.className =
            'acciones';


        const enlace =
            document.createElement(
                'a'
            );

        enlace.className =
            'btn btn-primary';

        enlace.textContent =
            'Ver detalle';

        enlace.href =
            `/vendedor/cliente-detalle.html?id=${cliente.idCliente}`;


        acciones.appendChild(
            enlace
        );

        article.appendChild(
            acciones
        );


        return article;
    }


    function crearDato(
            etiqueta,
            valor
    ) {

        const contenedor =
            document.createElement(
                'div'
            );

        contenedor.className =
            'dato';


        const span =
            document.createElement(
                'span'
            );

        span.textContent =
            etiqueta;


        const strong =
            document.createElement(
                'strong'
            );

        strong.textContent =
            valor;


        contenedor.appendChild(
            span
        );

        contenedor.appendChild(
            strong
        );


        return contenedor;
    }


    function obtenerNombreCompleto(
            cliente
    ) {

        const nombre =
            cliente.nombre
            || '';


        const apellido =
            cliente.apellido
            || '';


        const completo =
            `${nombre} ${apellido}`
                    .trim();


        return completo
            ||
            `Cliente #${cliente.idCliente}`;
    }


    function obtenerDescuento(
            cliente
    ) {

        const descuento =
            Number(
                cliente.descuentoPorcentaje
            );


        if (
            !Number.isFinite(descuento)
            ||
            descuento <= 0
        ) {

            return 'Sin descuento';
        }


        return `${descuento}%`;
    }


    async function obtenerMensajeError(
            response
    ) {

        try {

            const body =
                await response.json();


            return body.message
                ||
                body.mensaje
                ||
                'Ocurrió un error al consultar los clientes.';

        } catch {

            return 'Ocurrió un error al consultar los clientes.';
        }
    }


    function mostrarError(
            texto
    ) {

        mensaje.textContent =
            texto;

        mensaje.className =
            'mensaje mensaje-error';

        mensaje.classList.remove(
            'oculto'
        );
    }


    function ocultarMensaje() {

        mensaje.textContent =
            '';

        mensaje.classList.add(
            'oculto'
        );
    }

})();