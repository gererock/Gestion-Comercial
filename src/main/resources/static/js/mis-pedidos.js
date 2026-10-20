(() => {
    'use strict';

    if (!Auth.requerirRol('CLIENTE')) {
        return;
    }

    const mensaje = document.getElementById('mensaje');
    const listado = document.getElementById('listado');
    const tbody = document.getElementById('pedidos');

    iniciar();

    function iniciar() {
        const usuario = Auth.obtenerUsuario();
        if (usuario?.nombre) {
            document.getElementById('nombreUsuario').textContent = usuario.nombre;
        }

        document.getElementById('btnCerrarSesion')
            .addEventListener('click', Auth.cerrarSesion);
        cargarPedidos();
    }

    async function cargarPedidos() {
        try {
            const response = await Auth.fetchAutenticado('/api/cliente/pedidos');
            if (!response) {
                return;
            }
            if (!response.ok) {
                throw new Error(await obtenerMensajeError(response));
            }

            const pedidos = await response.json();
            mostrarPedidos(pedidos);
        } catch (error) {
            mostrarMensaje(
                error.message || 'No se pudieron consultar tus pedidos.',
                true
            );
        }
    }

    function mostrarPedidos(pedidos) {
        tbody.innerHTML = '';

        if (!Array.isArray(pedidos) || pedidos.length === 0) {
            mostrarMensaje('Todavía no realizaste pedidos online.', false);
            return;
        }

        pedidos.forEach(pedido => tbody.appendChild(crearFila(pedido)));
        mensaje.classList.add('oculto');
        listado.classList.remove('oculto');
    }

    function crearFila(pedido) {
        const fila = document.createElement('tr');
        agregarCelda(fila, 'Pedido', pedido.numeroPedido || `#${pedido.idPedido}`);
        agregarCelda(fila, 'Fecha', formatearFecha(pedido.fechaCreacion));

        const estado = document.createElement('td');
        estado.dataset.label = 'Estado';
        const badge = document.createElement('span');
        badge.className = 'badge';
        badge.textContent = formatearTexto(pedido.estadoPedido);
        estado.appendChild(badge);
        fila.appendChild(estado);

        agregarCelda(fila, 'Pago', formatearTexto(pedido.estadoPago));
        agregarCelda(fila, 'Entrega', formatearTexto(pedido.medioEntrega));
        agregarCelda(fila, 'Destino', formatearDestino(pedido));
        agregarCelda(fila, 'Total', formatearDinero(pedido.total));
        agregarCelda(fila, 'Actualizado', formatearFecha(pedido.fechaActualizacion));

        const acciones = document.createElement('td');
        acciones.dataset.label = 'Detalle';
        const enlace = document.createElement('a');
        enlace.className = 'btn-detalle';
        enlace.href = `/cliente/pedido-detalle.html?id=${pedido.idPedido}`;
        enlace.textContent = 'Ver pedido';
        acciones.appendChild(enlace);
        fila.appendChild(acciones);
        return fila;
    }

    function formatearDestino(pedido) {
        if (pedido.medioEntrega === 'RETIRO_LOCAL') {
            return 'Retiro en local';
        }

        const localidad = formatearTexto(pedido.localidadEnvio);
        return [localidad === '-' ? null : localidad, pedido.direccionEnvio]
            .filter(Boolean)
            .join(' · ') || '-';
    }

    function agregarCelda(fila, etiqueta, valor) {
        const celda = document.createElement('td');
        celda.dataset.label = etiqueta;
        celda.textContent = valor;
        fila.appendChild(celda);
    }

    function formatearTexto(valor) {
        return valor
            ? valor.toLowerCase()
                .replaceAll('_', ' ')
                .replace(/\b\w/g, letra => letra.toUpperCase())
            : '-';
    }

    function formatearFecha(valor) {
        const fecha = valor ? new Date(valor) : null;
        if (!fecha || Number.isNaN(fecha.getTime())) {
            return '-';
        }
        return new Intl.DateTimeFormat('es-AR', {
            dateStyle: 'short',
            timeStyle: 'short'
        }).format(fecha);
    }

    function formatearDinero(valor) {
        const numero = Number(valor || 0);
        return new Intl.NumberFormat('es-AR', {
            style: 'currency',
            currency: 'ARS'
        }).format(Number.isFinite(numero) ? numero : 0);
    }

    async function obtenerMensajeError(response) {
        try {
            const body = await response.json();
            return body.message || 'No se pudieron consultar tus pedidos.';
        } catch {
            return 'No se pudieron consultar tus pedidos.';
        }
    }

    function mostrarMensaje(texto, esError) {
        mensaje.textContent = texto;
        mensaje.className = esError
            ? 'mensaje mensaje-error'
            : 'mensaje';
        listado.classList.add('oculto');
    }
})();
