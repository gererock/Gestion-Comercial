(() => {

    'use strict';


    const API =
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


    let guardando =
        false;


    let altaCompletada =
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


        /*
         * Después de crear correctamente un cliente,
         * bloqueamos un segundo submit inmediato.
         *
         * Cuando el administrador vuelve a escribir
         * o modificar un campo, permitimos crear
         * otro cliente.
         */

        form.addEventListener(
            'input',
            habilitarNuevaAlta
        );


        form.addEventListener(
            'change',
            habilitarNuevaAlta
        );
    }


    function habilitarNuevaAlta() {

        if (
            altaCompletada
        ) {

            altaCompletada =
                false;
        }
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


    async function guardarCliente(
            event
    ) {

        event.preventDefault();


        /*
         * Evita:
         *
         * - doble click mientras guarda;
         * - segundo submit inmediatamente
         *   después de un alta exitosa.
         */

        if (
            guardando
            ||
            altaCompletada
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


        const datos =
            obtenerDatos();


        guardando =
            true;


        btnGuardar.disabled =
            true;


        btnGuardar.textContent =
            'Guardando...';


        try {

            const response =
                await Auth.fetchAutenticado(
                    API,
                    {
                        method:
                            'POST',

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


            /*
             * Marca que el alta terminó.
             *
             * Esto impide que un segundo click rápido
             * vuelva a validar el formulario recién
             * limpiado y reemplace el mensaje de éxito.
             */

            altaCompletada =
                true;


            mostrarExito(
                cliente.mensaje
                ||
                'Cliente creado correctamente.'
            );


            form.reset();


            /*
             * Después de resetear el formulario,
             * el estado vuelve a ACTIVO.
             */

            document.getElementById(
                'estado'
            ).checked =
                true;


        } catch (error) {

            mostrarError(
                error.message
                ||
                'No se pudo crear el cliente.'
            );

        } finally {

            guardando =
                false;


            btnGuardar.disabled =
                false;


            btnGuardar.textContent =
                'Guardar cliente';
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
                'No se pudo crear el cliente.';


        } catch {

            return 'No se pudo crear el cliente.';
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
