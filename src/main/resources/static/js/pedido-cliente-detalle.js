(() => {
    'use strict';

    if (!Auth.requerirRol('CLIENTE')) {
        return;
    }

    const mensaje = document.getElementById('mensaje');
    const detalle = document.getElementById('detalle');
    const parametros = new URLSearchParams(window.location.search);
    const vieneDelHistorial = parametros.get('origen') === 'historial';
    const API = vieneDelHistorial
        ? '/api/cliente/historial-compras'
        : '/api/cliente/pedidos';

    iniciar();

    function iniciar() {
        configurarOrigen();
        const usuario = Auth.obtenerUsuario();
        if (usuario?.nombre) {
            document.getElementById('nombreUsuario').textContent = usuario.nombre;
        }

        document.getElementById('btnCerrarSesion')
            .addEventListener('click', Auth.cerrarSesion);

        const idPedido = obtenerIdPedido();
        if (idPedido === null) {
            mostrarError('El pedido indicado no es válido.');
            return;
        }

        cargarPedido(idPedido);
    }

    function configurarOrigen() {
        if (!vieneDelHistorial) {
            return;
        }

        document.getElementById('rutaDetalle').textContent =
            'Historial de compras / Detalle';
        const volver = document.getElementById('volverDetalle');
        volver.href = '/cliente/historial-compras.html';
        volver.textContent = '← Volver al historial';
    }

    function obtenerIdPedido() {
        const valor = Number(parametros.get('id'));
        return Number.isInteger(valor) && valor > 0 ? valor : null;
    }

    async function cargarPedido(idPedido) {
        try {
            const response = await Auth.fetchAutenticado(
                `${API}/${idPedido}`
            );
            if (!response) {
                return;
            }
            if (!response.ok) {
                throw new Error(await obtenerMensajeError(response));
            }

            mostrarPedido(await response.json());
        } catch (error) {
            mostrarError(
                error.message || 'No se pudo consultar el pedido.'
            );
        }
    }

    function mostrarPedido(pedido) {
        texto('numeroPedido', pedido.numeroPedido || `Pedido #${pedido.idPedido}`);
        texto(
            'fechas',
            `Creado: ${formatearFecha(pedido.fechaCreacion)} · `
                + `Actualizado: ${formatearFecha(pedido.fechaActualizacion)}`
        );
        texto('estadoPedido', formatearTexto(pedido.estadoPedido));
        texto('estadoPago', formatearTexto(pedido.estadoPago));
        texto('medioPago', pedido.medioPago || '-');
        texto('medioEntrega', formatearTexto(pedido.medioEntrega));

        mostrarEntrega(pedido);
        mostrarProductos(pedido.productos);
        mostrarImportes(pedido);
        texto(
            'observaciones',
            pedido.observacionesCliente?.trim() || 'Sin observaciones.'
        );

        mensaje.classList.add('oculto');
        detalle.classList.remove('oculto');
    }

    function mostrarEntrega(pedido) {
        const esEnvio = pedido.medioEntrega === 'ENVIO';
        document.getElementById('filaLocalidad').classList.toggle('oculto', !esEnvio);
        document.getElementById('filaDireccion').classList.toggle('oculto', !esEnvio);

        if (esEnvio) {
            texto('localidad', formatearTexto(pedido.localidadEnvio));
            texto('direccion', pedido.direccionEnvio || '-');
        }
    }

    function mostrarProductos(productos) {
        const tbody = document.getElementById('productos');
        const sinProductos = document.getElementById('sinProductos');
        tbody.innerHTML = '';

        if (!Array.isArray(productos) || productos.length === 0) {
            sinProductos.classList.remove('oculto');
            return;
        }

        sinProductos.classList.add('oculto');
        productos.forEach(producto => {
            const fila = document.createElement('tr');
            agregarCelda(fila, 'Producto', producto.nombre || '-');
            agregarCelda(fila, 'Cantidad', String(producto.cantidad || 0));
            agregarCelda(fila, 'Precio unitario', formatearDinero(producto.precioUnitario));
            agregarCelda(fila, 'Descuento', formatearDinero(producto.descuentoAplicado));
            agregarCelda(fila, 'Subtotal', formatearDinero(producto.subtotal));
            tbody.appendChild(fila);
        });
    }

    function mostrarImportes(pedido) {
        texto('subtotal', formatearDinero(pedido.subtotal));
        texto('descuentoOferta', formatearDescuento(pedido.descuentoOferta));
        texto('descuentoMayorista', formatearDescuento(pedido.descuentoMayorista));
        texto('descuentoCliente', formatearDescuento(pedido.descuentoCliente));
        texto('total', formatearDinero(pedido.total));
    }

    function formatearDescuento(valor) {
        const numero = Number(valor || 0);
        return numero > 0 ? `− ${formatearDinero(numero)}` : formatearDinero(0);
    }

    function agregarCelda(fila, etiqueta, valor) {
        const celda = document.createElement('td');
        celda.dataset.label = etiqueta;
        celda.textContent = valor;
        fila.appendChild(celda);
    }

    function texto(id, valor) {
        document.getElementById(id).textContent = valor;
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
            return body.message || 'El pedido no está disponible.';
        } catch {
            return 'El pedido no está disponible.';
        }
    }

    function mostrarError(textoError) {
        mensaje.textContent = textoError;
        mensaje.className = 'mensaje mensaje-error';
        detalle.classList.add('oculto');
    }
})();
