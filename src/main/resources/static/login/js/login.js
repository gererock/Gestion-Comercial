(() => {

    'use strict';


    const form =
        document.getElementById(
            'login-form'
        );


    const emailInput =
        document.getElementById(
            'email'
        );


    const passwordInput =
        document.getElementById(
            'password'
        );


    const emailError =
        document.getElementById(
            'email-error'
        );


    const passwordError =
        document.getElementById(
            'password-error'
        );


    const togglePassword =
        document.getElementById(
            'toggle-password'
        );


    const loginMessage =
        document.getElementById(
            'login-message'
        );


    const submitButton =
        form.querySelector(
            'button[type="submit"]'
        );


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

        errorElement.textContent =
            '';


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

        loginMessage.textContent =
            mensaje;


        loginMessage.className =
            `cl-login02__message is-${tipo}`;


        loginMessage.hidden =
            false;
    }



    function ocultarMensaje() {

        loginMessage.hidden =
            true;

        loginMessage.textContent =
            '';
    }



    /*
     * Si el usuario viene desde el registro,
     * mostramos un mensaje indicando que
     * la cuenta fue creada correctamente.
     */

    const parametros =
        new URLSearchParams(
            window.location.search
        );


    if (
        parametros.get('registro') ===
        'exitoso'
    ) {

        mostrarMensaje(
            'Tu cuenta fue creada correctamente. Ya podés iniciar sesión.',
            'success'
        );


        /*
         * Quitamos ?registro=exitoso de la URL
         * para que al actualizar la página
         * no vuelva a aparecer el mensaje.
         */

        window.history.replaceState(
            {},
            document.title,
            '/login/index.html'
        );
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


        limpiarError(
            passwordInput,
            passwordError
        );

        return true;
    }



    emailInput.addEventListener(
        'blur',
        validarEmail
    );


    passwordInput.addEventListener(
        'blur',
        validarPassword
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
        }
    );



    togglePassword.addEventListener(
        'click',
        () => {

            const mostrar =
                passwordInput.type ===
                'password';


            passwordInput.type =
                mostrar
                    ? 'text'
                    : 'password';


            togglePassword.setAttribute(
                'aria-pressed',
                String(mostrar)
            );


            togglePassword.setAttribute(
                'aria-label',
                mostrar
                    ? 'Ocultar contraseña'
                    : 'Mostrar contraseña'
            );
        }
    );



    form.addEventListener(
        'submit',
        async (event) => {

            event.preventDefault();

            ocultarMensaje();


            const emailValido =
                validarEmail();


            const passwordValida =
                validarPassword();


            if (!emailValido) {

                emailInput.focus();

                return;
            }


            if (!passwordValida) {

                passwordInput.focus();

                return;
            }


            submitButton.disabled =
                true;


            submitButton.textContent =
                'Iniciando sesión...';


            try {

                const response =
                    await fetch(
                        '/api/auth/login',
                        {
                            method: 'POST',

                            headers: {
                                'Content-Type':
                                    'application/json'
                            },

                            body:
                                JSON.stringify({

                                    email:
                                        emailInput
                                            .value
                                            .trim(),

                                    password:
                                        passwordInput
                                            .value
                                })
                        }
                    );


                const data =
                    await response.json();


                if (!response.ok) {

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


                    mostrarMensaje(
                        data.message ||
                        'No se pudo iniciar sesión.'
                    );

                    return;
                }


                Auth.guardarSesion(
                    data
                );


                mostrarMensaje(
                    'Inicio de sesión correcto.',
                    'success'
                );


                Auth.redirigirSegunRol(
                    data.rol
                );


            } catch (error) {

                mostrarMensaje(
                    'No se pudo conectar con el servidor.'
                );


            } finally {

                submitButton.disabled =
                    false;


                submitButton.textContent =
                    'Iniciar sesión';
            }
        }
    );

})();