(() => {
    'use strict';

    if (!Auth.requerirRol('CLIENTE')) {
        return;
    }

    const mensaje = document.getElementById('mensaje');
    const listado = document.getElementById('listado');
    const tbody = document.getElementById('compras');

    iniciar();

    function iniciar() {
        const usuario = Auth.obtenerUsuario();
        if (usuario?.nombre) {
            document.getElementById('nombreUsuario').textContent = usuario.nombre;
        }

        document.getElementById('btnCerrarSesion')
            .addEventListener('click', Auth.cerrarSesion);
        cargarHistorial();
    }

    async function cargarHistorial() {
        try {
            const response = await Auth.fetchAutenticado(
                '/api/cliente/historial-compras'
            );
            if (!response) {
                return;
            }
            if (!response.ok) {
                throw new Error(await obtenerMensajeError(response));
            }

            mostrarCompras(await response.json());
        } catch (error) {
            mostrarMensaje(
                error.message || 'No se pudo consultar el historial de compras.',
                true
            );
        }
    }

    function mostrarCompras(compras) {
        tbody.innerHTML = '';

        if (!Array.isArray(compras) || compras.length === 0) {
            mostrarMensaje('Todavía no tenés compras registradas.', false);
            return;
        }

        compras.forEach(compra => tbody.appendChild(crearFila(compra)));
        mensaje.classList.add('oculto');
        listado.classList.remove('oculto');
    }

    function crearFila(compra) {
        const fila = document.createElement('tr');
        agregarCelda(fila, 'Compra', compra.numeroPedido || `#${compra.idPedido}`);
        agregarCelda(fila, 'Fecha', formatearFecha(compra.fechaCreacion));

        const estado = document.createElement('td');
        estado.dataset.label = 'Estado';
        const badge = document.createElement('span');
        badge.className = 'badge';
        badge.textContent = formatearTexto(compra.estadoPedido);
        estado.appendChild(badge);
        fila.appendChild(estado);

        agregarCelda(fila, 'Pago', formatearTexto(compra.estadoPago));
        agregarCelda(fila, 'Entrega', formatearTexto(compra.medioEntrega));
        agregarCelda(fila, 'Total', formatearDinero(compra.total));

        const acciones = document.createElement('td');
        acciones.dataset.label = 'Detalle';
        const enlace = document.createElement('a');
        enlace.className = 'btn-detalle';
        enlace.href =
            `/cliente/pedido-detalle.html?id=${compra.idPedido}&origen=historial`;
        enlace.textContent = 'Ver compra';
        acciones.appendChild(enlace);
        fila.appendChild(acciones);
        return fila;
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
            return body.message || 'No se pudo consultar el historial de compras.';
        } catch {
            return 'No se pudo consultar el historial de compras.';
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
