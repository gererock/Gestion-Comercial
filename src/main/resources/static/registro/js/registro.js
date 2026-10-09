(() => {

    'use strict';


    const form =
        document.getElementById('registro-form');


    const nombreInput =
        document.getElementById('nombre');

    const apellidoInput =
        document.getElementById('apellido');

    const emailInput =
        document.getElementById('email');

    const passwordInput =
        document.getElementById('password');

    const confirmacionPasswordInput =
        document.getElementById(
            'confirmacionPassword'
        );


    const nombreError =
        document.getElementById(
            'nombre-error'
        );

    const apellidoError =
        document.getElementById(
            'apellido-error'
        );

    const emailError =
        document.getElementById(
            'email-error'
        );

    const passwordError =
        document.getElementById(
            'password-error'
        );

    const confirmacionPasswordError =
        document.getElementById(
            'confirmacion-password-error'
        );


    const registroMessage =
        document.getElementById(
            'registro-message'
        );


    const togglePassword =
        document.getElementById(
            'toggle-password'
        );

    const toggleConfirmacionPassword =
        document.getElementById(
            'toggle-confirmacion-password'
        );


    const submitButton =
        form.querySelector(
            'button[type="submit"]'
        );


    const NOMBRE_REGEX =
        /^[\p{L} ]+$/u;

    const EMAIL_REGEX =
        /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;



    function mostrarError(
        input,
        errorElement,
        mensaje
    ) {

        errorElement.textContent =
            mensaje;


        input
            .closest('[data-field]')
            .classList
            .add('is-error');


        input.setAttribute(
            'aria-invalid',
            'true'
        );
    }



    function limpiarError(
        input,
        errorElement
    ) {

        errorElement.textContent = '';


        input
            .closest('[data-field]')
            .classList
            .remove('is-error');


        input.removeAttribute(
            'aria-invalid'
        );
    }



    function mostrarMensaje(
        mensaje,
        tipo = 'error'
    ) {

        registroMessage.textContent =
            mensaje;


        registroMessage.className =
            `cl-login02__message is-${tipo}`;


        registroMessage.hidden =
            false;
    }



    function ocultarMensaje() {

        registroMessage.hidden =
            true;

        registroMessage.textContent =
            '';
    }



    function validarNombre() {

        const nombre =
            nombreInput.value.trim();


        if (nombre === '') {

            mostrarError(
                nombreInput,
                nombreError,
                'El nombre es obligatorio.'
            );

            return false;
        }


        if (
            !NOMBRE_REGEX.test(nombre)
        ) {

            mostrarError(
                nombreInput,
                nombreError,
                'El nombre solo puede contener letras.'
            );

            return false;
        }


        limpiarError(
            nombreInput,
            nombreError
        );

        return true;
    }



    function validarApellido() {

        const apellido =
            apellidoInput.value.trim();


        if (apellido === '') {

            mostrarError(
                apellidoInput,
                apellidoError,
                'El apellido es obligatorio.'
            );

            return false;
        }


        if (
            !NOMBRE_REGEX.test(apellido)
        ) {

            mostrarError(
                apellidoInput,
                apellidoError,
                'El apellido solo puede contener letras.'
            );

            return false;
        }


        limpiarError(
            apellidoInput,
            apellidoError
        );

        return true;
    }



    function validarEmail() {

        const email =
            emailInput.value.trim();


        if (email === '') {

            mostrarError(
                emailInput,
                emailError,
                'El correo electrónico es obligatorio.'
            );

            return false;
        }


        if (/\s/.test(email)) {

            mostrarError(
                emailInput,
                emailError,
                'El correo electrónico no puede contener espacios.'
            );

            return false;
        }


        if (
            !EMAIL_REGEX.test(email)
        ) {

            mostrarError(
                emailInput,
                emailError,
                'Ingresá un correo electrónico válido.'
            );

            return false;
        }


        limpiarError(
            emailInput,
            emailError
        );

        return true;
    }



    function validarPassword() {

        const password =
            passwordInput.value;


        if (
            password.trim() === ''
        ) {

            mostrarError(
                passwordInput,
                passwordError,
                'La contraseña es obligatoria.'
            );

            return false;
        }


        if (
            password.length < 8
        ) {

            mostrarError(
                passwordInput,
                passwordError,
                'La contraseña debe tener al menos 8 caracteres.'
            );

            return false;
        }


        limpiarError(
            passwordInput,
            passwordError
        );

        return true;
    }



    function validarConfirmacionPassword() {

        const confirmacion =
            confirmacionPasswordInput.value;


        if (
            confirmacion.trim() === ''
        ) {

            mostrarError(
                confirmacionPasswordInput,
                confirmacionPasswordError,
                'La confirmación de contraseña es obligatoria.'
            );

            return false;
        }


        if (
            confirmacion !==
            passwordInput.value
        ) {

            mostrarError(
                confirmacionPasswordInput,
                confirmacionPasswordError,
                'Las contraseñas no coinciden.'
            );

            return false;
        }


        limpiarError(
            confirmacionPasswordInput,
            confirmacionPasswordError
        );

        return true;
    }



    function configurarTogglePassword(
        boton,
        input
    ) {

        boton.addEventListener(
            'click',
            () => {

                const mostrar =
                    input.type === 'password';


                input.type =
                    mostrar
                        ? 'text'
                        : 'password';


                boton.setAttribute(
                    'aria-pressed',
                    String(mostrar)
                );


                boton.setAttribute(
                    'aria-label',
                    mostrar
                        ? 'Ocultar contraseña'
                        : 'Mostrar contraseña'
                );
            }
        );
    }



    configurarTogglePassword(
        togglePassword,
        passwordInput
    );


    configurarTogglePassword(
        toggleConfirmacionPassword,
        confirmacionPasswordInput
    );



    nombreInput.addEventListener(
        'blur',
        validarNombre
    );


    apellidoInput.addEventListener(
        'blur',
        validarApellido
    );


    emailInput.addEventListener(
        'blur',
        validarEmail
    );


    passwordInput.addEventListener(
        'blur',
        validarPassword
    );


    confirmacionPasswordInput
        .addEventListener(
            'blur',
            validarConfirmacionPassword
        );



    nombreInput.addEventListener(
        'input',
        () => {

            if (
                nombreError.textContent !== ''
            ) {

                validarNombre();
            }
        }
    );



    apellidoInput.addEventListener(
        'input',
        () => {

            if (
                apellidoError.textContent !== ''
            ) {

                validarApellido();
            }
        }
    );



    emailInput.addEventListener(
        'input',
        () => {

            if (
                emailError.textContent !== ''
            ) {

                validarEmail();
            }
        }
    );



    passwordInput.addEventListener(
        'input',
        () => {

            if (
                passwordError.textContent !== ''
            ) {

                validarPassword();
            }


            if (
                confirmacionPasswordInput
                    .value !== ''
            ) {

                validarConfirmacionPassword();
            }
        }
    );



    confirmacionPasswordInput
        .addEventListener(
            'input',
            () => {

                if (
                    confirmacionPasswordError
                        .textContent !== ''
                ) {

                    validarConfirmacionPassword();
                }
            }
        );



    form.addEventListener(
        'submit',
        async (event) => {

            event.preventDefault();

            ocultarMensaje();


            const nombreValido =
                validarNombre();

            const apellidoValido =
                validarApellido();

            const emailValido =
                validarEmail();

            const passwordValida =
                validarPassword();

            const confirmacionValida =
                validarConfirmacionPassword();


            if (
                !nombreValido ||
                !apellidoValido ||
                !emailValido ||
                !passwordValida ||
                !confirmacionValida
            ) {

                return;
            }


            submitButton.disabled =
                true;

            submitButton.textContent =
                'Registrando...';


            let registroExitoso =
                false;


            try {

                const response =
                    await fetch(
                        '/api/auth/registro',
                        {
                            method: 'POST',

                            headers: {
                                'Content-Type':
                                    'application/json'
                            },

                            body:
                                JSON.stringify({

                                    nombre:
                                        nombreInput
                                            .value
                                            .trim(),

                                    apellido:
                                        apellidoInput
                                            .value
                                            .trim(),

                                    email:
                                        emailInput
                                            .value
                                            .trim(),

                                    password:
                                        passwordInput
                                            .value,

                                    confirmacionPassword:
                                        confirmacionPasswordInput
                                            .value
                                })
                        }
                    );


                const data =
                    await response.json();


                if (!response.ok) {

                    if (
                        data.errors?.nombre
                    ) {

                        mostrarError(
                            nombreInput,
                            nombreError,
                            data.errors.nombre
                        );
                    }


                    if (
                        data.errors?.apellido
                    ) {

                        mostrarError(
                            apellidoInput,
                            apellidoError,
                            data.errors.apellido
                        );
                    }


                    if (
                        data.errors?.email
                    ) {

                        mostrarError(
                            emailInput,
                            emailError,
                            data.errors.email
                        );
                    }


                    if (
                        data.errors?.password
                    ) {

                        mostrarError(
                            passwordInput,
                            passwordError,
                            data.errors.password
                        );
                    }


                    if (
                        data.errors
                            ?.confirmacionPassword
                    ) {

                        mostrarError(
                            confirmacionPasswordInput,
                            confirmacionPasswordError,
                            data.errors
                                .confirmacionPassword
                        );
                    }


                    if (
                        response.status === 409
                    ) {

                        mostrarError(
                            emailInput,
                            emailError,
                            data.message ||
                            'Este correo ya está registrado.'
                        );
                    }


                    mostrarMensaje(
                        data.message ||
                        'No se pudo realizar el registro.'
                    );

                    return;
                }


                registroExitoso =
                    true;


                mostrarMensaje(
                    'Registro realizado correctamente. Ahora podés iniciar sesión.',
                    'success'
                );


                form.reset();


                setTimeout(
                    () => {

                        window.location.href =
                            '/login/index.html?registro=exitoso';
                    },
                    3000
                );


            } catch (error) {

                mostrarMensaje(
                    'No se pudo conectar con el servidor.'
                );


            } finally {

                if (!registroExitoso) {

                    submitButton.disabled =
                        false;

                    submitButton.textContent =
                        'Registrarse';
                }
            }
        }
    );

})();