(() => {

    'use strict';


    const API_GASTOS =
        '/api/admin/gastos';

    const API_CATEGORIAS =
        '/api/admin/categorias-gasto?activa=true';

    const API_MEDIOS_PAGO =
        '/api/medios-pago?activo=true';


    const formGasto =
        document.getElementById(
            'formGasto'
        );


    const categoriaGasto =
        document.getElementById(
            'categoriaGasto'
        );


    const medioPago =
        document.getElementById(
            'medioPago'
        );


    const descripcion =
        document.getElementById(
            'descripcion'
        );


    const monto =
        document.getElementById(
            'monto'
        );


    const fecha =
        document.getElementById(
            'fecha'
        );


    const observaciones =
        document.getElementById(
            'observaciones'
        );


    const claveOperacion =
        document.getElementById(
            'claveOperacion'
        );


    const contadorDescripcion =
        document.getElementById(
            'contadorDescripcion'
        );


    const contadorObservaciones =
        document.getElementById(
            'contadorObservaciones'
        );


    const btnGuardar =
        document.getElementById(
            'btnGuardar'
        );


    const btnCancelar =
        document.getElementById(
            'btnCancelar'
        );


    const btnCerrarSesion =
        document.getElementById(
            'btnCerrarSesion'
        );


    const nombreUsuario =
        document.getElementById(
            'nombreUsuario'
        );


    const mensaje =
        document.getElementById(
            'mensaje'
        );


    const cargandoDatos =
        document.getElementById(
            'cargandoDatos'
        );


    let guardando = false;


    document.addEventListener(
        'DOMContentLoaded',
        iniciar
    );


    async function iniciar() {

        if (
            !Auth.requerirRol(
                'ADMINISTRADOR'
            )
        ) {

            return;
        }


        mostrarUsuario();


        registrarEventos();


        prepararFormulario();


        await cargarDatosIniciales();
    }


    function registrarEventos() {

        formGasto.addEventListener(
            'submit',
            registrarGasto
        );


        btnCancelar.addEventListener(
            'click',
            cancelarFormulario
        );


        btnCerrarSesion.addEventListener(
            'click',
            Auth.cerrarSesion
        );


        descripcion.addEventListener(
            'input',
            actualizarContadores
        );


        observaciones.addEventListener(
            'input',
            actualizarContadores
        );
    }


    function mostrarUsuario() {

        const usuario =
            Auth.obtenerUsuario();


        if (
            !usuario
        ) {

            return;
        }


        nombreUsuario.textContent =
            usuario.apellido
                ? `${usuario.nombre} ${usuario.apellido}`
                : usuario.nombre;
    }


    async function cargarDatosIniciales() {

        mostrarCargando(
            true
        );


        ocultarMensaje();


        try {

            await Promise.all([
                cargarCategorias(),
                cargarMediosPago()
            ]);

        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                'No se pudieron cargar los datos necesarios para registrar el gasto.',
                'error'
            );

        } finally {

            mostrarCargando(
                false
            );
        }
    }


    async function cargarCategorias() {

        const response =
            await Auth.fetchAutenticado(
                API_CATEGORIAS,
                {
                    method: 'GET'
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


        const categorias =
            await response.json();


        categoriaGasto.innerHTML =
            `
                <option value="">
                    Seleccioná una categoría
                </option>
            `;


        categorias.forEach(
            categoria => {

                const option =
                    document.createElement(
                        'option'
                    );


                option.value =
                    categoria.idCategoriaGasto;


                option.textContent =
                    categoria.nombre;


                categoriaGasto.appendChild(
                    option
                );
            }
        );
    }


    async function cargarMediosPago() {

        const response =
            await Auth.fetchAutenticado(
                API_MEDIOS_PAGO,
                {
                    method: 'GET'
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


        const medios =
            await response.json();


        medioPago.innerHTML =
            `
                <option value="">
                    Sin medio de pago
                </option>
            `;


        medios.forEach(
            medio => {

                const option =
                    document.createElement(
                        'option'
                    );


                option.value =
                    medio.idMedioPago;


                option.textContent =
                    medio.nombre;


                medioPago.appendChild(
                    option
                );
            }
        );
    }


    async function registrarGasto(
        event
    ) {

        event.preventDefault();


        if (
            guardando
        ) {

            return;
        }


        ocultarMensaje();


        if (
            !validarFormulario()
        ) {

            return;
        }


        const datos = {

            idCategoriaGasto:
                Number(
                    categoriaGasto.value
                ),

            idMedioPago:
                medioPago.value
                    ? Number(
                        medioPago.value
                    )
                    : null,

            descripcion:
                descripcion.value.trim(),

            monto:
                Number(
                    monto.value
                ),

            fecha:
                fecha.value,

            observaciones:
                observaciones.value.trim()
                    ? observaciones.value.trim()
                    : null,

            claveOperacion:
                claveOperacion.value
        };


        try {

            guardando =
                true;


            bloquearFormulario(
                true
            );


            const response =
                await Auth.fetchAutenticado(
                    API_GASTOS,
                    {
                        method: 'POST',

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


            const gasto =
                await response.json();


            limpiarFormulario(
                true
            );


            mostrarMensaje(
                `Gasto registrado correctamente. Número de gasto: ${gasto.idGasto}.`,
                'exito'
            );

        } catch (error) {

            console.error(
                error
            );


            mostrarMensaje(
                error.message ||
                'No se pudo registrar el gasto.',
                'error'
            );

        } finally {

            guardando =
                false;


            bloquearFormulario(
                false
            );
        }
    }


    function validarFormulario() {

        if (
            !categoriaGasto.value
        ) {

            mostrarMensaje(
                'Seleccioná una categoría de gasto.',
                'error'
            );


            categoriaGasto.focus();


            return false;
        }


        const descripcionValor =
            descripcion.value.trim();


        if (
            !descripcionValor
        ) {

            mostrarMensaje(
                'La descripción es obligatoria.',
                'error'
            );


            descripcion.focus();


            return false;
        }


        if (
            descripcionValor.length > 500
        ) {

            mostrarMensaje(
                'La descripción no puede superar los 500 caracteres.',
                'error'
            );


            descripcion.focus();


            return false;
        }


        if (
            !/[\p{L}\p{N}]/u.test(
                descripcionValor
            )
        ) {

            mostrarMensaje(
                'La descripción debe contener al menos una letra o un número.',
                'error'
            );


            descripcion.focus();


            return false;
        }


        if (
            monto.value === ''
        ) {

            mostrarMensaje(
                'El monto es obligatorio.',
                'error'
            );


            monto.focus();


            return false;
        }


        const montoValor =
            Number(
                monto.value
            );


        if (
            !Number.isFinite(
                montoValor
            )
            ||
            montoValor <= 0
        ) {

            mostrarMensaje(
                'El monto debe ser mayor a cero.',
                'error'
            );


            monto.focus();


            return false;
        }


        if (
            !tieneMaximoDosDecimales(
                monto.value
            )
        ) {

            mostrarMensaje(
                'El monto puede tener como máximo 2 decimales.',
                'error'
            );


            monto.focus();


            return false;
        }


        if (
            !fecha.value
        ) {

            mostrarMensaje(
                'La fecha es obligatoria.',
                'error'
            );


            fecha.focus();


            return false;
        }


        const hoy =
            obtenerFechaActual();


        if (
            fecha.value > hoy
        ) {

            mostrarMensaje(
                'La fecha no puede ser futura.',
                'error'
            );


            fecha.focus();


            return false;
        }


        if (
            observaciones.value.length > 1000
        ) {

            mostrarMensaje(
                'Las observaciones no pueden superar los 1000 caracteres.',
                'error'
            );


            observaciones.focus();


            return false;
        }


        return true;
    }


    function tieneMaximoDosDecimales(
        valor
    ) {

        return /^\d+(\.\d{1,2})?$/.test(
            valor
        );
    }


    function cancelarFormulario() {

        limpiarFormulario(
            true
        );


        ocultarMensaje();


        categoriaGasto.focus();
    }


    function prepararFormulario() {

        const hoy =
            obtenerFechaActual();


        fecha.max =
            hoy;


        fecha.value =
            hoy;


        generarClaveOperacion();


        actualizarContadores();
    }


    function limpiarFormulario(
        nuevaOperacion
    ) {

        categoriaGasto.value =
            '';


        medioPago.value =
            '';


        descripcion.value =
            '';


        monto.value =
            '';


        observaciones.value =
            '';


        const hoy =
            obtenerFechaActual();


        fecha.value =
            hoy;


        fecha.max =
            hoy;


        if (
            nuevaOperacion
        ) {

            generarClaveOperacion();
        }


        actualizarContadores();
    }


    function generarClaveOperacion() {

        if (
            window.crypto
            &&
            typeof window.crypto.randomUUID ===
                'function'
        ) {

            claveOperacion.value =
                window.crypto.randomUUID();


            return;
        }


        claveOperacion.value =
            `${Date.now()}-${Math.random()
                .toString(36)
                .slice(2)}`;
    }


    function obtenerFechaActual() {

        const ahora =
            new Date();


        const anio =
            ahora.getFullYear();


        const mes =
            String(
                ahora.getMonth() + 1
            ).padStart(
                2,
                '0'
            );


        const dia =
            String(
                ahora.getDate()
            ).padStart(
                2,
                '0'
            );


        return `${anio}-${mes}-${dia}`;
    }


    function actualizarContadores() {

        contadorDescripcion.textContent =
            descripcion.value.length;


        contadorObservaciones.textContent =
            observaciones.value.length;
    }


    function bloquearFormulario(
        bloquear
    ) {

        categoriaGasto.disabled =
            bloquear;


        medioPago.disabled =
            bloquear;


        descripcion.disabled =
            bloquear;


        monto.disabled =
            bloquear;


        fecha.disabled =
            bloquear;


        observaciones.disabled =
            bloquear;


        btnGuardar.disabled =
            bloquear;


        btnCancelar.disabled =
            bloquear;


        btnGuardar.textContent =
            bloquear
                ? 'Registrando...'
                : 'Registrar gasto';
    }


    function mostrarCargando(
        mostrar
    ) {

        if (
            mostrar
        ) {

            cargandoDatos.classList.remove(
                'oculto'
            );


            formGasto.classList.add(
                'form-deshabilitado'
            );

        } else {

            cargandoDatos.classList.add(
                'oculto'
            );


            formGasto.classList.remove(
                'form-deshabilitado'
            );
        }
    }


    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensaje.textContent =
            texto;


        mensaje.className =
            'mensaje';


        if (
            tipo === 'exito'
        ) {

            mensaje.classList.add(
                'mensaje-exito'
            );

        } else {

            mensaje.classList.add(
                'mensaje-error'
            );
        }


        mensaje.classList.remove(
            'oculto'
        );


        mensaje.scrollIntoView({
            behavior: 'smooth',
            block: 'nearest'
        });
    }


    function ocultarMensaje() {

        mensaje.classList.add(
            'oculto'
        );
    }


    async function obtenerMensajeError(
        response
    ) {

        try {

            const data =
                await response.json();


            const erroresCampos =
                data.errors
                ||
                data.fieldErrors;


            if (
                erroresCampos
            ) {

                const errores =
                    Object.values(
                        erroresCampos
                    );


                if (
                    errores.length > 0
                ) {

                    return errores.join(
                        ' - '
                    );
                }
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
                'No se pudo leer la respuesta de error.',
                error
            );
        }


        switch (
            response.status
        ) {

            case 400:
                return 'Los datos ingresados no son válidos.';

            case 401:
                return 'La sesión no es válida o venció.';

            case 403:
                return 'No tenés permisos para registrar gastos.';

            case 404:
                return 'No se encontró uno de los datos seleccionados.';

            case 409:
                return 'No se pudo registrar el gasto por un conflicto con los datos.';

            default:
                return 'Ocurrió un error inesperado.';
        }
    }

})();