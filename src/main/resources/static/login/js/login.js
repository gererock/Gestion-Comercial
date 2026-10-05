(() => {

    'use strict';

    const form = document.getElementById('login-form');

    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');

    const emailError = document.getElementById('email-error');
    const passwordError = document.getElementById('password-error');

    const togglePassword = document.getElementById('toggle-password');

    const loginMessage = document.getElementById('login-message');


    const EMAIL_REGEX =
        /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;


    function mostrarError(input, errorElement, mensaje) {

        errorElement.textContent = mensaje;

        input
            .closest('[data-field]')
            .classList.add('is-error');

        input.setAttribute('aria-invalid', 'true');
    }


    function limpiarError(input, errorElement) {

        errorElement.textContent = '';

        input
            .closest('[data-field]')
            .classList.remove('is-error');

        input.removeAttribute('aria-invalid');
    }


    function validarEmail() {

        const email = emailInput.value.trim();

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

        if (!EMAIL_REGEX.test(email)) {

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

        const password = passwordInput.value;

        if (password.trim() === '') {

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


    emailInput.addEventListener('input', () => {

        if (emailError.textContent !== '') {
            validarEmail();
        }
    });


    passwordInput.addEventListener('input', () => {

        if (passwordError.textContent !== '') {
            validarPassword();
        }
    });


    togglePassword.addEventListener('click', () => {

        const mostrar =
            passwordInput.type === 'password';

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
    });


    form.addEventListener('submit', (event) => {

        event.preventDefault();

        loginMessage.hidden = true;
        loginMessage.textContent = '';

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


        /*
         * En la próxima etapa se realizará
         * la petición al backend.
         */

        loginMessage.textContent =
            'Los datos ingresados son válidos.';

        loginMessage.className =
            'cl-login02__message is-success';

        loginMessage.hidden = false;
    });

})();