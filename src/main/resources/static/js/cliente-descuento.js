(() => {

    'use strict';

    const API_CLIENTES = '/api/vendedor/clientes';
    const API_DESCUENTO = '/api/admin/clientes';

    if (!Auth.requerirRol('ADMINISTRADOR')) {
        return;
    }

    const mensaje = document.getElementById('mensaje');
    const cargando = document.getElementById('cargando');
    const contenido = document.getElementById('contenido');
    const form = document.getElementById('formDescuento');
    const input = document.getElementById('descuento');
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

        form.addEventListener('submit', guardarDescuento);
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

        actualizarDescuentoVisible(cliente.descuentoPorcentaje);
        input.value = cliente.descuentoPorcentaje === null
                || cliente.descuentoPorcentaje === undefined
                ? ''
                : String(cliente.descuentoPorcentaje);

        contenido.classList.remove('oculto');

        if (cliente.estado !== true) {
            form.classList.add('oculto');
            mostrarError('No se puede modificar el descuento de un cliente inactivo.');
        }
    }

    async function guardarDescuento(event) {
        event.preventDefault();

        if (guardando) return;

        const validacion = validarDescuento(input.value);

        if (validacion) {
            mostrarError(validacion);
            return;
        }

        const raw = input.value;
        const descuento = raw === '' ? null : raw;

        guardando = true;
        btnGuardar.disabled = true;
        btnGuardar.textContent = 'Guardando...';
        ocultarMensaje();

        try {
            const response = await Auth.fetchAutenticado(
                `${API_DESCUENTO}/${idCliente}/descuento`,
                {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ descuentoPorcentaje: descuento })
                }
            );

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            const resultado = await response.json();
            input.value = resultado.descuentoPorcentaje === null
                    || resultado.descuentoPorcentaje === undefined
                    ? ''
                    : String(resultado.descuentoPorcentaje);
            actualizarDescuentoVisible(resultado.descuentoPorcentaje);
            mostrarExito(resultado.mensaje || 'Descuento actualizado correctamente.');
        } catch (error) {
            mostrarError(error.message || 'No se pudo actualizar el descuento.');
        } finally {
            guardando = false;
            btnGuardar.disabled = false;
            btnGuardar.textContent = 'Guardar descuento';
        }
    }

    function validarDescuento(valor) {
        if (valor === '') {
            return null;
        }

        if (valor.trim() === '') {
            return 'El descuento no puede contener solamente espacios.';
        }

        if (valor.length > 6) {
            return 'El descuento ingresado es demasiado largo.';
        }

        if (!/^[0-9]+(?:\.[0-9]{1,2})?$/.test(valor)) {
            return 'El descuento debe ser numérico y puede tener como máximo dos decimales.';
        }

        const numero = Number(valor);

        if (!Number.isFinite(numero) || numero < 0 || numero > 100) {
            return 'El descuento debe estar entre 0 y 100.';
        }

        return null;
    }

    function actualizarDescuentoVisible(valor) {
        const numero = Number(valor);
        document.getElementById('descuentoActual').textContent =
            valor === null
            || valor === undefined
            || !Number.isFinite(numero)
            || numero === 0
                ? 'Sin descuento'
                : `${numero}%`;
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

            return body.message || body.mensaje || 'No se pudo actualizar el descuento.';
        } catch {
            return 'No se pudo actualizar el descuento.';
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
