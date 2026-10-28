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


    const mensaje =
        document.getElementById(
            'mensaje'
        );

    const cargando =
        document.getElementById(
            'cargando'
        );

    const detalle =
        document.getElementById(
            'detalle'
        );


    iniciar();


    function iniciar() {

        mostrarUsuario();


        document.getElementById(
            'btnCerrarSesion'
        ).addEventListener(
            'click',
            Auth.cerrarSesion
        );


        const idCliente =
            obtenerIdCliente();


        if (
            idCliente === null
        ) {

            cargando.classList.add(
                'oculto'
            );

            mostrarError(
                'El cliente indicado no es válido.'
            );

            return;
        }
        
        const btnHistorialCompras =
            document.getElementById(
                'btnHistorialCompras'
            );


        btnHistorialCompras.href =
            `/vendedor/cliente-historial.html?idCliente=${idCliente}`;


        if (
            Auth.obtenerRol() ===
            'ADMINISTRADOR'
        ) {

            const btnEditarCliente =
                document.getElementById(
                    'btnEditarCliente'
                );


            btnEditarCliente.href =
                `/admin/cliente-editar.html?id=${idCliente}`;


            btnEditarCliente.classList.remove(
                'oculto'
            );
        }


        cargarCliente(
            idCliente
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

            document.getElementById(
                'nombreUsuario'
            ).textContent =
                usuario.nombre;
        }
    }


    function obtenerIdCliente() {

        const parametros =
            new URLSearchParams(
                window.location.search
            );


        const valor =
            Number(
                parametros.get(
                    'id'
                )
            );


        if (
            !Number.isInteger(valor)
            ||
            valor <= 0
        ) {

            return null;
        }


        return valor;
    }


    async function cargarCliente(
        idCliente
    ) {

        try {

            const response =
                await Auth.fetchAutenticado(
                    `${API}/${idCliente}`
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


            const cliente =
                await response.json();


            mostrarCliente(
                cliente
            );

        } catch (error) {

            mostrarError(
                error.message
                ||
                'No se pudo consultar el cliente.'
            );

        } finally {

            cargando.classList.add(
                'oculto'
            );
        }
    }


    function mostrarCliente(
        cliente
    ) {

        document.getElementById(
            'nombreCliente'
        ).textContent =
            obtenerNombreCompleto(
                cliente
            );


        const estado =
            document.getElementById(
                'estadoCliente'
            );


        estado.textContent =
            cliente.estado
                ? 'Activo'
                : 'Inactivo';


        estado.className =
            cliente.estado
                ? 'badge badge-activo'
                : 'badge badge-inactivo';


        ponerTexto(
            'dni',
            cliente.dni
        );

        ponerTexto(
            'telefono',
            cliente.telefono
        );

        ponerTexto(
            'correo',
            cliente.correo
        );

        ponerTexto(
            'razonSocial',
            cliente.razonSocial
        );

        ponerTexto(
            'cuit',
            cliente.cuit
        );

        ponerTexto(
            'condicionIva',
            cliente.condicionIva
        );

        ponerTexto(
            'direccionFacturacion',
            cliente.direccionFacturacion
        );

        ponerTexto(
            'localidadFacturacion',
            cliente.localidadFacturacion
        );

        ponerTexto(
            'provinciaFacturacion',
            cliente.provinciaFacturacion
        );


        document.getElementById(
            'descuento'
        ).textContent =
            obtenerDescuento(
                cliente.descuentoPorcentaje
            );


        if (
            Auth.obtenerRol() === 'ADMINISTRADOR'
        ) {

            const btnNota =
                document.getElementById(
                    'btnGestionarNota'
                );


            btnNota.href =
                `/admin/cliente-nota-interna.html?id=${cliente.idCliente}`;


            btnNota.classList.remove(
                'oculto'
            );
        }


        if (
            Auth.obtenerRol() === 'ADMINISTRADOR'
            &&
            cliente.estado === true
        ) {

            const btnDescuento =
                document.getElementById(
                    'btnGestionarDescuento'
                );


            btnDescuento.href =
                `/admin/cliente-descuento.html?id=${cliente.idCliente}`;


            btnDescuento.classList.remove(
                'oculto'
            );
        }


        document.getElementById(
            'notaInterna'
        ).textContent =
            cliente.notaInterna
            ||
            'Sin nota interna';


        document.getElementById(
            'usuarioAsociado'
        ).textContent =
            obtenerNombreUsuario(
                cliente
            );


        document.getElementById(
            'emailUsuarioAsociado'
        ).textContent =
            cliente.emailUsuario
            ||
            'No informado';


        if (
            Auth.obtenerRol() === 'ADMINISTRADOR'
            &&
            cliente.estado === true
            &&
            !cliente.idUsuario
        ) {

            const btnAsociar =
                document.getElementById(
                    'btnAsociarUsuario'
                );


            btnAsociar.href =
                `/admin/cliente-asociar-usuario.html?idCliente=${cliente.idCliente}`;


            btnAsociar.classList.remove(
                'oculto'
            );
        }


        document.getElementById(
            'fechaAlta'
        ).textContent =
            formatearFecha(
                cliente.fechaAlta
            );


        detalle.classList.remove(
            'oculto'
        );
    }


    function ponerTexto(
        id,
        valor
    ) {

        document.getElementById(
            id
        ).textContent =
            valor
            ||
            'No informado';
    }


    function obtenerNombreCompleto(
        cliente
    ) {

        const completo =
            `${cliente.nombre || ''} ${cliente.apellido || ''}`
                .trim();


        return completo
            ||
            `Cliente #${cliente.idCliente}`;
    }


    function obtenerNombreUsuario(
        cliente
    ) {

        if (
            !cliente.idUsuario
        ) {

            return 'Sin usuario asociado';
        }


        const nombre =
            `${cliente.nombreUsuario || ''} ${cliente.apellidoUsuario || ''}`
                .trim();


        return nombre
            ||
            `Usuario #${cliente.idUsuario}`;
    }


    function obtenerDescuento(
        valor
    ) {

        const numero =
            Number(
                valor
            );


        if (
            !Number.isFinite(numero)
            ||
            numero <= 0
        ) {

            return 'Sin descuento';
        }


        return `${numero}%`;
    }


    function formatearFecha(
        valor
    ) {

        if (
            !valor
        ) {

            return 'No informada';
        }


        const fecha =
            new Date(
                valor
            );


        if (
            Number.isNaN(
                fecha.getTime()
            )
        ) {

            return 'No informada';
        }


        return new Intl.DateTimeFormat(
            'es-AR',
            {
                dateStyle:
                    'medium',

                timeStyle:
                    'short'
            }
        ).format(
            fecha
        );
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
                'El cliente indicado no existe.';

        } catch {

            return 'El cliente indicado no existe.';
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

        detalle.classList.add(
            'oculto'
        );
    }

})();
