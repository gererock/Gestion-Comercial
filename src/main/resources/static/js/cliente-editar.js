(() => {

    'use strict';


    const API_CONSULTA =
        '/api/vendedor/clientes';

    const API_ACTUALIZACION =
        '/api/admin/clientes';


    if (
        !Auth.requerirRol(
            'ADMINISTRADOR'
        )
    ) {

        return;
    }


    const form =
        document.getElementById(
            'formCliente'
        );

    const btnGuardar =
        document.getElementById(
            'btnGuardar'
        );

    const mensaje =
        document.getElementById(
            'mensaje'
        );

    const cargando =
        document.getElementById(
            'cargando'
        );


    let idCliente =
        null;

    let guardando =
        false;


    iniciar();


    function iniciar() {

        mostrarUsuario();


        document.getElementById(
            'btnCerrarSesion'
        ).addEventListener(
            'click',
            Auth.cerrarSesion
        );


        form.addEventListener(
            'submit',
            guardarCliente
        );


        idCliente =
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


        const rutaDetalle =
            '/vendedor/cliente-detalle.html?id='
            + idCliente;


        document.getElementById(
            'btnVolverDetalle'
        ).href =
            rutaDetalle;


        document.getElementById(
            'btnCancelar'
        ).href =
            rutaDetalle;


        cargarCliente();
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
            !Number.isInteger(
                valor
            )
            ||
            valor <= 0
        ) {

            return null;
        }


        return valor;
    }


    async function cargarCliente() {

        ocultarMensaje();


        try {

            const response =
                await Auth.fetchAutenticado(
                    API_CONSULTA
                    + '/'
                    + idCliente
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


            mostrarCliente(
                await response.json()
            );


        } catch (error) {

            form.classList.add(
                'oculto'
            );

            mostrarError(
                error.message
                ||
                'No se pudieron cargar los datos del cliente.'
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

        ponerValor(
            'nombre',
            cliente.nombre
        );

        ponerValor(
            'apellido',
            cliente.apellido
        );

        ponerValor(
            'dni',
            cliente.dni
        );

        ponerValor(
            'telefono',
            cliente.telefono
        );

        ponerValor(
            'correo',
            cliente.correo
        );

        ponerValor(
            'razonSocial',
            cliente.razonSocial
        );

        ponerValor(
            'cuit',
            cliente.cuit
        );

        ponerValor(
            'condicionIva',
            cliente.condicionIva
        );

        ponerValor(
            'direccionFacturacion',
            cliente.direccionFacturacion
        );

        ponerValor(
            'localidadFacturacion',
            cliente.localidadFacturacion
        );

        ponerValor(
            'provinciaFacturacion',
            cliente.provinciaFacturacion
        );

        ponerValor(
            'descuento',
            cliente.descuentoPorcentaje
        );

        ponerValor(
            'notaInterna',
            cliente.notaInterna
        );


        document.getElementById(
            'estado'
        ).checked =
            cliente.estado === true;


        form.classList.remove(
            'oculto'
        );
    }


    function ponerValor(
            id,
            valor
    ) {

        document.getElementById(
            id
        ).value =
            valor ?? '';
    }


    async function guardarCliente(
            event
    ) {

        event.preventDefault();


        if (
            guardando
        ) {

            return;
        }


        ocultarMensaje();


        const validacion =
            validarFormulario();


        if (
            validacion !== null
        ) {

            mostrarError(
                validacion
            );

            return;
        }


        guardando =
            true;

        btnGuardar.disabled =
            true;

        btnGuardar.textContent =
            'Guardando...';


        try {

            const response =
                await Auth.fetchAutenticado(
                    API_ACTUALIZACION
                    + '/'
                    + idCliente,
                    {
                        method:
                            'PUT',

                        headers: {
                            'Content-Type':
                                'application/json'
                        },

                        body:
                            JSON.stringify(
                                obtenerDatos()
                            )
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


            const cliente =
                await response.json();


            mostrarCliente(
                cliente
            );


            mostrarExito(
                cliente.mensaje
                ||
                'Cliente actualizado correctamente.'
            );


        } catch (error) {

            mostrarError(
                error.message
                ||
                'No se pudo actualizar el cliente.'
            );


        } finally {

            guardando =
                false;

            btnGuardar.disabled =
                false;

            btnGuardar.textContent =
                'Guardar cambios';
        }
    }


    function obtenerDatos() {

        const descuentoTexto =
            obtenerValor(
                'descuento'
            );


        return {

            nombre:
                obtenerValor(
                    'nombre'
                ),


            apellido:
                obtenerValorOpcional(
                    'apellido'
                ),


            dni:
                obtenerValorOpcional(
                    'dni'
                ),


            telefono:
                obtenerValorOpcional(
                    'telefono'
                ),


            correo:
                obtenerValorOpcional(
                    'correo'
                ),


            razonSocial:
                obtenerValorOpcional(
                    'razonSocial'
                ),


            cuit:
                obtenerValorOpcional(
                    'cuit'
                ),


            condicionIva:
                obtenerValorOpcional(
                    'condicionIva'
                ),


            direccionFacturacion:
                obtenerValorOpcional(
                    'direccionFacturacion'
                ),


            localidadFacturacion:
                obtenerValorOpcional(
                    'localidadFacturacion'
                ),


            provinciaFacturacion:
                obtenerValorOpcional(
                    'provinciaFacturacion'
                ),


            descuentoPorcentaje:
                descuentoTexto === ''
                    ? null
                    : Number(
                        descuentoTexto
                    ),


            notaInterna:
                obtenerValorOpcional(
                    'notaInterna'
                ),


            estado:
                document.getElementById(
                    'estado'
                ).checked
        };
    }


    function validarFormulario() {

        // =========================================
        // NOMBRE
        // =========================================

        const nombre =
            obtenerValor(
                'nombre'
            );


        if (
            nombre === ''
        ) {

            return 'El nombre es obligatorio.';
        }


        if (
            !/^[\p{L} ]+$/u.test(
                nombre
            )
        ) {

            return 'El nombre solo puede contener letras y espacios.';
        }


        if (
            nombre.length > 100
        ) {

            return 'El nombre no puede superar los 100 caracteres.';
        }


        // =========================================
        // APELLIDO
        // =========================================

        const apellidoInput =
            document.getElementById(
                'apellido'
            ).value;


        if (
            apellidoInput !== ''
            &&
            apellidoInput.trim() === ''
        ) {

            return 'El apellido no puede contener solamente espacios.';
        }


        const apellido =
            apellidoInput.trim();


        if (
            apellido !== ''
            &&
            !/^[\p{L} ]+$/u.test(
                apellido
            )
        ) {

            return 'El apellido solo puede contener letras y espacios.';
        }


        if (
            apellido.length > 100
        ) {

            return 'El apellido no puede superar los 100 caracteres.';
        }


        // =========================================
        // DNI
        // =========================================

        const dni =
            document.getElementById(
                'dni'
            ).value;


        if (
            dni !== ''
            &&
            !/^[0-9]{7,8}$/.test(
                dni
            )
        ) {

            return 'El DNI debe contener entre 7 y 8 números.';
        }


        // =========================================
        // TELÉFONO
        // =========================================

        const telefono =
            document.getElementById(
                'telefono'
            ).value;


        if (
            telefono !== ''
            &&
            !/^[0-9]{8,15}$/.test(
                telefono
            )
        ) {

            return 'El teléfono debe contener entre 8 y 15 números.';
        }


        // =========================================
        // CORREO
        // =========================================

        const correo =
            document.getElementById(
                'correo'
            ).value;


        if (
            correo !== ''
            &&
            !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(
                correo
            )
        ) {

            return 'El correo electrónico no tiene un formato válido.';
        }


        if (
            correo.length > 120
        ) {

            return 'El correo no puede superar los 120 caracteres.';
        }


        // =========================================
        // RAZÓN SOCIAL
        // =========================================

        const razonSocial =
            obtenerValor(
                'razonSocial'
            );


        if (
            razonSocial !== ''
            &&
            !/[\p{L}\p{N}]/u.test(
                razonSocial
            )
        ) {

            return 'La razón social no tiene un formato válido.';
        }


        if (
            razonSocial.length > 150
        ) {

            return 'La razón social no puede superar los 150 caracteres.';
        }


        // =========================================
        // CUIT
        // =========================================

        const cuit =
            document.getElementById(
                'cuit'
            ).value;


        if (
            cuit !== ''
            &&
            !/^[0-9]{11}$/.test(
                cuit
            )
        ) {

            return 'El CUIT debe contener exactamente 11 números.';
        }


        // =========================================
        // CONDICIÓN IVA
        // =========================================

        const condicionIva =
            obtenerValor(
                'condicionIva'
            );


        const condicionesPermitidas = [
            '',
            'Consumidor Final',
            'Responsable Inscripto',
            'Monotributista',
            'Exento'
        ];


        if (
            !condicionesPermitidas.includes(
                condicionIva
            )
        ) {

            return 'La condición frente al IVA no es válida.';
        }


        // =========================================
        // DIRECCIÓN DE FACTURACIÓN
        // =========================================

        const direccion =
            obtenerValor(
                'direccionFacturacion'
            );


        if (
            direccion !== ''
            &&
            !/[\p{L}\p{N}]/u.test(
                direccion
            )
        ) {

            return 'La dirección de facturación no tiene un formato válido.';
        }


        if (
            direccion.length > 200
        ) {

            return 'La dirección de facturación no puede superar los 200 caracteres.';
        }


        // =========================================
        // LOCALIDAD
        // =========================================

        const localidad =
            obtenerValor(
                'localidadFacturacion'
            );


        if (
            localidad !== ''
            &&
            !/^[\p{L} ]+$/u.test(
                localidad
            )
        ) {

            return 'La localidad no tiene un formato válido.';
        }


        if (
            localidad.length > 100
        ) {

            return 'La localidad no puede superar los 100 caracteres.';
        }


        // =========================================
        // PROVINCIA
        // =========================================

        const provincia =
            obtenerValor(
                'provinciaFacturacion'
            );


        if (
            provincia !== ''
            &&
            !/^[\p{L} ]+$/u.test(
                provincia
            )
        ) {

            return 'La provincia no tiene un formato válido.';
        }


        if (
            provincia.length > 100
        ) {

            return 'La provincia no puede superar los 100 caracteres.';
        }


        // =========================================
        // DESCUENTO
        // =========================================

        const descuento =
            obtenerValor(
                'descuento'
            );


        if (
            descuento !== ''
        ) {

            /*
             * Solo permite:
             *
             * 10
             * 10.5
             * 10.55
             *
             * Rechaza:
             *
             * 10.555
             * letras
             * símbolos
             */

            if (
                !/^[0-9]+(\.[0-9]{1,2})?$/.test(
                    descuento
                )
            ) {

                return 'El descuento puede tener como máximo dos decimales.';
            }


            const numero =
                Number(
                    descuento
                );


            if (
                !Number.isFinite(
                    numero
                )
                ||
                numero < 0
                ||
                numero > 100
            ) {

                return 'El descuento debe estar entre 0 y 100.';
            }
        }


        // =========================================
        // NOTA INTERNA
        // =========================================

        const nota =
            document.getElementById(
                'notaInterna'
            ).value;


        if (
            nota.length > 1000
        ) {

            return 'La nota interna no puede superar los 1000 caracteres.';
        }


        return null;
    }


    function obtenerValor(
            id
    ) {

        return document
                .getElementById(
                    id
                )
                .value
                .trim();
    }


    function obtenerValorOpcional(
            id
    ) {

        const valor =
            obtenerValor(
                id
            );


        return valor === ''
            ? null
            : valor;
    }


    async function obtenerMensajeError(
            response
    ) {

        try {

            const body =
                await response.json();


            /*
             * Errores generados por validaciones
             * de Jakarta Validation.
             */

            if (
                body.errors
                &&
                Object.keys(
                    body.errors
                ).length > 0
            ) {

                return Object
                        .values(
                            body.errors
                        )
                        .join(
                            ' '
                        );
            }


            return body.message
                ||
                'No se pudo actualizar el cliente.';


        } catch {

            return 'No se pudo actualizar el cliente.';
        }
    }


    function mostrarError(
            texto
    ) {

        mensaje.textContent =
            texto;


        mensaje.className =
            'mensaje mensaje-error';
    }


    function mostrarExito(
            texto
    ) {

        mensaje.textContent =
            texto;


        mensaje.className =
            'mensaje mensaje-exito';
    }


    function ocultarMensaje() {

        mensaje.textContent =
            '';


        mensaje.className =
            'mensaje oculto';
    }

})();

