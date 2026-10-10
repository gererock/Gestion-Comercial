(() => {

    'use strict';

    const TOKEN_KEY = 'auth_token';
    const ROLE_KEY = 'auth_rol';
    const USER_KEY = 'auth_usuario';


    function guardarSesion(loginResponse) {

        sessionStorage.setItem(
            TOKEN_KEY,
            loginResponse.token
        );

        sessionStorage.setItem(
            ROLE_KEY,
            loginResponse.rol
        );

        sessionStorage.setItem(
            USER_KEY,
            JSON.stringify({
                idUsuario: loginResponse.idUsuario,
                nombre: loginResponse.nombre,
                apellido: loginResponse.apellido,
                email: loginResponse.email
            })
        );
    }


    function obtenerToken() {
        return sessionStorage.getItem(TOKEN_KEY);
    }


    function obtenerRol() {
        return sessionStorage.getItem(ROLE_KEY);
    }


    function obtenerUsuario() {

        const usuario =
            sessionStorage.getItem(USER_KEY);

        return usuario
            ? JSON.parse(usuario)
            : null;
    }


    function cerrarSesion() {

        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(ROLE_KEY);
        sessionStorage.removeItem(USER_KEY);

        window.location.href = '/login/index.html';
    }


    function obtenerRutaPorRol(rol) {

        switch (rol) {

            case 'ADMINISTRADOR':
                return '/admin/index.html';

            case 'VENDEDOR':
                return '/vendedor/index.html';

            case 'CLIENTE':
                return '/cliente/index.html';

            default:
                return '/login/index.html';
        }
    }


    function redirigirSegunRol(rol) {

        window.location.href =
            obtenerRutaPorRol(rol);
    }


    function requerirRol(...rolesPermitidos) {

        const token = obtenerToken();
        const rol = obtenerRol();

        if (!token || !rol) {
            window.location.href = '/login/index.html';
            return false;
        }

        if (!rolesPermitidos.includes(rol)) {
            redirigirSegunRol(rol);
            return false;
        }

        return true;
    }


    async function fetchAutenticado(url, options = {}) {

        const token = obtenerToken();

        if (!token) {
            cerrarSesion();
            return;
        }

        const headers =
            new Headers(options.headers || {});

        headers.set(
            'Authorization',
            `Bearer ${token}`
        );

        const response = await fetch(
            url,
            {
                ...options,
                headers
            }
        );

        if (response.status === 401) {
            cerrarSesion();
        }

        return response;
    }


    window.Auth = {
        guardarSesion,
        obtenerToken,
        obtenerRol,
        obtenerUsuario,
        obtenerRutaPorRol,
        cerrarSesion,
        redirigirSegunRol,
        requerirRol,
        fetchAutenticado
    };

})();   