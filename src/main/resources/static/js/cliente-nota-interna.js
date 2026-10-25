(() => {

    'use strict';

    const API_CLIENTES = '/api/vendedor/clientes';
    const API_NOTA = '/api/admin/clientes';
    const LONGITUD_MAXIMA = 1000;

    if (!Auth.requerirRol('ADMINISTRADOR')) {
        return;
    }

    const mensaje = document.getElementById('mensaje');
    const cargando = document.getElementById('cargando');
    const contenido = document.getElementById('contenido');
    const form = document.getElementById('formNota');
    const input = document.getElementById('notaInterna');
    const contador = document.getElementById('contador');
    const btnGuardar = document.getElementById('btnGuardar');

    let idCliente = null;
    let guardando = false;

    iniciar();

    function iniciar() {
        mostrarUsuario();
        document.getElementById('btnCerrarSesion').addEventListener(
            'click',
            Auth.cerrarSesion
        );

        form.addEventListener('submit', guardarNota);
        input.addEventListener('input', actualizarContador);
        idCliente = obtenerIdCliente();

        if (idCliente === null) {
            cargando.classList.add('oculto');
            mostrarError('El cliente indicado no es válido.');
            return;
        }

        const detalle = `/vendedor/cliente-detalle.html?id=${idCliente}`;
        document.getElementById('btnVolver').href = detalle;
        document.getElementById('btnCancelar').href = detalle;
        cargarCliente();
    }

    function mostrarUsuario() {
        const usuario = Auth.obtenerUsuario();

        if (usuario && usuario.nombre) {
            document.getElementById('nombreUsuario').textContent = usuario.nombre;
        }
    }

    function obtenerIdCliente() {
        const valor = Number(new URLSearchParams(window.location.search).get('id'));
        return Number.isInteger(valor) && valor > 0 ? valor : null;
    }

    async function cargarCliente() {
        ocultarMensaje();

        try {
            const response = await Auth.fetchAutenticado(`${API_CLIENTES}/${idCliente}`);

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            const cliente = await response.json();
            mostrarCliente(cliente);
        } catch (error) {
            contenido.classList.add('oculto');
            mostrarError(error.message || 'No se pudo consultar el cliente.');
        } finally {
            cargando.classList.add('oculto');
        }
    }

    function mostrarCliente(cliente) {
        document.getElementById('nombreCliente').textContent = nombreCompleto(cliente);
        document.getElementById('identificacionCliente').textContent =
            [cliente.dni ? `DNI ${cliente.dni}` : null, cliente.correo]
                    .filter(Boolean)
                    .join(' · ') || `Cliente #${cliente.idCliente}`;

        const estado = document.getElementById('estadoCliente');
        estado.textContent = cliente.estado === true ? 'Activo' : 'Inactivo';
        estado.className = cliente.estado === true
                ? 'badge badge-activo'
                : 'badge badge-inactivo';

        input.value = cliente.notaInterna || '';
        actualizarNotaVisible(cliente.notaInterna);
        actualizarContador();
        contenido.classList.remove('oculto');
    }

    async function guardarNota(event) {
        event.preventDefault();

        if (guardando) return;

        const validacion = validarNota(input.value);

        if (validacion) {
            mostrarError(validacion);
            return;
        }

        guardando = true;
        btnGuardar.disabled = true;
        btnGuardar.textContent = 'Guardando...';
        ocultarMensaje();

        try {
            const response = await Auth.fetchAutenticado(
                `${API_NOTA}/${idCliente}/nota-interna`,
                {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ notaInterna: input.value })
                }
            );

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            const resultado = await response.json();
            input.value = resultado.notaInterna || '';
            actualizarNotaVisible(resultado.notaInterna);
            actualizarContador();
            mostrarExito(resultado.mensaje || 'Nota interna actualizada correctamente.');
        } catch (error) {
            mostrarError(error.message || 'No se pudo actualizar la nota interna.');
        } finally {
            guardando = false;
            btnGuardar.disabled = false;
            btnGuardar.textContent = 'Guardar nota';
        }
    }

    function validarNota(valor) {
        if (valor === '') {
            return null;
        }

        if (valor.trim() === '') {
            return 'La nota interna no puede contener solamente espacios en blanco.';
        }

        if (valor.length > LONGITUD_MAXIMA) {
            return 'La nota interna no puede superar los 1000 caracteres.';
        }

        if (!/[\p{L}\p{N}]/u.test(valor)) {
            return 'La nota interna debe contener al menos una letra o un número.';
        }

        return null;
    }

    function actualizarNotaVisible(valor) {
        document.getElementById('notaActual').textContent =
            valor || 'Sin nota interna';
    }

    function actualizarContador() {
        contador.textContent = `${input.value.length} / ${LONGITUD_MAXIMA}`;
    }

    function nombreCompleto(cliente) {
        const nombre = `${cliente.nombre || ''} ${cliente.apellido || ''}`.trim();
        return nombre || `Cliente #${cliente.idCliente}`;
    }

    async function obtenerMensajeError(response) {
        try {
            const body = await response.json();

            if (body.errors && Object.keys(body.errors).length > 0) {
                return Object.values(body.errors).join(' ');
            }

            return body.message || body.mensaje || 'No se pudo actualizar la nota interna.';
        } catch {
            return 'No se pudo actualizar la nota interna.';
        }
    }

    function mostrarError(texto) {
        mensaje.textContent = texto;
        mensaje.className = 'mensaje mensaje-error';
    }

    function mostrarExito(texto) {
        mensaje.textContent = texto;
        mensaje.className = 'mensaje mensaje-exito';
    }

    function ocultarMensaje() {
        mensaje.textContent = '';
        mensaje.className = 'mensaje oculto';
    }

})();
