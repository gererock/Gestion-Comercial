(() => {
    'use strict';

    const API = '/api/vendedor/pedidos';

    if (!Auth.requerirRol('VENDEDOR', 'ADMINISTRADOR')) {
        return;
    }

    const mensaje = document.getElementById('mensaje');
    const cargando = document.getElementById('cargando');
    const detalle = document.getElementById('detalle');

    iniciar();

    function iniciar() {
        mostrarUsuario();
        document.getElementById('btnCerrarSesion')
            .addEventListener('click', Auth.cerrarSesion);

        const idPedido = obtenerIdPedido();
        if (idPedido === null) {
            cargando.classList.add('oculto');
            mostrarError('El pedido indicado no es válido.');
            return;
        }

        cargarPedido(idPedido);
    }

    function mostrarUsuario() {
        const usuario = Auth.obtenerUsuario();
        if (usuario?.nombre) {
            document.getElementById('nombreUsuario').textContent = usuario.nombre;
        }
    }

    function obtenerIdPedido() {
        const valor = Number(new URLSearchParams(window.location.search).get('id'));
        return Number.isInteger(valor) && valor > 0 ? valor : null;
    }

    async function cargarPedido(idPedido) {
        try {
            const response = await Auth.fetchAutenticado(`${API}/${idPedido}`);
            if (!response) {
                return;
            }
            if (!response.ok) {
                throw new Error(await obtenerMensajeError(response));
            }

            mostrarPedido(await response.json());
        } catch (error) {
            mostrarError(error.message || 'No se pudo consultar el pedido.');
        } finally {
            cargando.classList.add('oculto');
        }
    }

    function mostrarPedido(pedido) {
        texto('numeroPedido', pedido.numeroPedido || `Pedido #${pedido.idPedido}`);
        texto('fechaCreacion', formatearFecha(pedido.fechaCreacion));
        texto('estadoPedido', formatearTexto(pedido.estadoPedido));
        texto('estadoPago', formatearTexto(pedido.estadoPago));
        texto('medioPago', pedido.medioPago || '-');
        texto('medioEntrega', formatearTexto(pedido.medioEntrega));

        mostrarCliente(pedido.cliente || {});
        mostrarEntrega(pedido);
        mostrarProductos(pedido.productos);
        mostrarImportes(pedido);

        texto(
            'observacionesCliente',
            textoOpcional(pedido.observacionesCliente, 'Sin observaciones.')
        );
        texto(
            'observacionesInternas',
            textoOpcional(pedido.observacionesInternas, 'Sin observaciones.')
        );

        detalle.classList.remove('oculto');
    }

    function mostrarCliente(cliente) {
        const nombreCompleto = [cliente.nombre, cliente.apellido]
            .filter(Boolean)
            .join(' ')
            .trim();

        texto(
            'clienteNombre',
            textoOpcional(cliente.razonSocial, nombreCompleto || '-')
        );
        texto(
            'clienteDocumento',
            cliente.cuit
                ? `CUIT ${cliente.cuit}`
                : cliente.dni
                    ? `DNI ${cliente.dni}`
                    : '-'
        );
        texto('clienteTelefono', textoOpcional(cliente.telefono, '-'));
        texto('clienteCorreo', textoOpcional(cliente.correo, '-'));
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
        productos.forEach(producto => tbody.appendChild(crearFilaProducto(producto)));
    }

    function crearFilaProducto(producto) {
        const fila = document.createElement('tr');
        agregarCelda(fila, 'Producto', producto.nombre || `Producto #${producto.idProducto}`);
        agregarCelda(fila, 'Cantidad', numero(producto.cantidad));
        agregarCelda(fila, 'Precio unitario', formatearDinero(producto.precioUnitario));
        agregarCelda(fila, 'Descuento', formatearDinero(producto.descuentoAplicado));
        agregarCelda(fila, 'Subtotal', formatearDinero(producto.subtotal));

        const celdaStock = document.createElement('td');
        celdaStock.dataset.label = 'Stock disponible';

        const stock = document.createElement('span');
        stock.className = producto.stockSuficiente
            ? 'stock'
            : 'stock stock-insuficiente';
        stock.textContent = numero(producto.stockDisponible);
        celdaStock.appendChild(stock);

        if (!producto.stockSuficiente) {
            const aviso = document.createElement('span');
            aviso.className = 'aviso-stock';
            aviso.textContent = 'Stock insuficiente para la cantidad solicitada';
            celdaStock.appendChild(aviso);
        }

        fila.appendChild(celdaStock);
        return fila;
    }

    function agregarCelda(fila, etiqueta, valor) {
        const celda = document.createElement('td');
        celda.dataset.label = etiqueta;
        celda.textContent = valor;
        fila.appendChild(celda);
    }

    function mostrarImportes(pedido) {
        texto('subtotal', formatearDinero(pedido.subtotal));
        texto('descuentoOferta', formatearDescuento(pedido.descuentoOferta));
        texto('descuentoMayorista', formatearDescuento(pedido.descuentoMayorista));
        texto('descuentoCliente', formatearDescuento(pedido.descuentoCliente));
        texto('total', formatearDinero(pedido.total));
    }

    function formatearDescuento(valor) {
        const importe = Number(valor || 0);
        return importe > 0 ? `− ${formatearDinero(importe)}` : formatearDinero(0);
    }

    function texto(id, valor) {
        document.getElementById(id).textContent = valor;
    }

    function textoOpcional(valor, reemplazo) {
        return typeof valor === 'string' && valor.trim()
            ? valor.trim()
            : reemplazo;
    }

    function numero(valor) {
        const resultado = Number(valor);
        return Number.isFinite(resultado) ? String(resultado) : '0';
    }

    function formatearTexto(valor) {
        if (!valor) {
            return '-';
        }
        return valor.toLowerCase()
            .replaceAll('_', ' ')
            .replace(/\b\w/g, letra => letra.toUpperCase());
    }

    function formatearDinero(valor) {
        const resultado = Number(valor || 0);
        return new Intl.NumberFormat('es-AR', {
            style: 'currency',
            currency: 'ARS'
        }).format(Number.isFinite(resultado) ? resultado : 0);
    }

    function formatearFecha(valor) {
        const fecha = valor ? new Date(valor) : null;
        if (!fecha || Number.isNaN(fecha.getTime())) {
            return '-';
        }
        return new Intl.DateTimeFormat('es-AR', {
            dateStyle: 'medium',
            timeStyle: 'short'
        }).format(fecha);
    }

    async function obtenerMensajeError(response) {
        try {
            const body = await response.json();
            return body.message || body.mensaje || 'El pedido online indicado no existe.';
        } catch {
            return 'El pedido online indicado no existe.';
        }
    }

    function mostrarError(textoError) {
        mensaje.textContent = textoError;
        mensaje.className = 'mensaje mensaje-error';
        detalle.classList.add('oculto');
    }
})();
