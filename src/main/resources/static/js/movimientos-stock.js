(() => {

    'use strict';

    const API_MOVIMIENTOS = '/api/admin/stock/movimientos';
    const TIPOS_INCREMENTO = new Set([
        'ENTRADA',
        'AJUSTE_POSITIVO',
        'DEVOLUCION',
        'LIBERACION_RESERVA'
    ]);

    if (!Auth.requerirRol('ADMINISTRADOR')) {
        return;
    }

    const mensaje = document.getElementById('mensaje');
    const formFiltros = document.getElementById('formFiltros');
    const idProducto = document.getElementById('idProducto');
    const tipoMovimiento = document.getElementById('tipoMovimiento');
    const origen = document.getElementById('origen');
    const fechaDesde = document.getElementById('fechaDesde');
    const fechaHasta = document.getElementById('fechaHasta');
    const cargando = document.getElementById('cargando');
    const estadoVacio = document.getElementById('estadoVacio');
    const contenedorTabla = document.getElementById('contenedorTabla');
    const movimientosBody = document.getElementById('movimientosBody');
    const cantidadResultados = document.getElementById('cantidadResultados');

    iniciar();

    function iniciar() {
        mostrarUsuario();
        document.getElementById('btnCerrarSesion').addEventListener(
            'click',
            Auth.cerrarSesion
        );
        document.getElementById('btnLimpiar').addEventListener('click', limpiarFiltros);
        formFiltros.addEventListener('submit', aplicarFiltros);
        consultarMovimientos();
    }

    function mostrarUsuario() {
        const usuario = Auth.obtenerUsuario();

        if (usuario && usuario.nombre) {
            document.getElementById('nombreUsuario').textContent = usuario.nombre;
        }
    }

    function aplicarFiltros(event) {
        event.preventDefault();
        const error = validarFiltros();

        if (error) {
            mostrarError(error);
            return;
        }

        consultarMovimientos();
    }

    function validarFiltros() {
        const producto = idProducto.value;

        if (producto !== '' && !/^[1-9][0-9]*$/.test(producto)) {
            return 'El ID del producto debe ser un número entero mayor a cero.';
        }

        if (fechaDesde.value && fechaHasta.value
                && fechaDesde.value > fechaHasta.value) {
            return 'La fecha desde no puede ser posterior a la fecha hasta.';
        }

        return null;
    }

    async function consultarMovimientos() {
        ocultarMensaje();
        cargando.classList.remove('oculto');
        estadoVacio.classList.add('oculto');
        contenedorTabla.classList.add('oculto');
        cantidadResultados.textContent = 'Cargando movimientos...';

        try {
            const parametros = construirParametros();
            const url = parametros.size > 0
                    ? `${API_MOVIMIENTOS}?${parametros}`
                    : API_MOVIMIENTOS;
            const response = await Auth.fetchAutenticado(url);

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            renderizarMovimientos(await response.json());
        } catch (error) {
            cantidadResultados.textContent = 'No se pudo completar la consulta.';
            mostrarError(error.message || 'No se pudieron consultar los movimientos.');
        } finally {
            cargando.classList.add('oculto');
        }
    }

    function construirParametros() {
        const parametros = new URLSearchParams();

        if (idProducto.value) parametros.set('idProducto', idProducto.value);
        if (tipoMovimiento.value) parametros.set('tipoMovimiento', tipoMovimiento.value);
        if (origen.value) parametros.set('origen', origen.value);
        if (fechaDesde.value) parametros.set('fechaDesde', fechaDesde.value);
        if (fechaHasta.value) parametros.set('fechaHasta', fechaHasta.value);

        return parametros;
    }

    function renderizarMovimientos(movimientos) {
        movimientosBody.innerHTML = '';
        cantidadResultados.textContent = `${movimientos.length} movimiento(s) encontrado(s).`;

        if (movimientos.length === 0) {
            estadoVacio.textContent = hayFiltrosAplicados()
                    ? 'No se encontraron movimientos con los filtros aplicados.'
                    : 'No existen movimientos de stock registrados.';
            estadoVacio.classList.remove('oculto');
            return;
        }

        movimientos.forEach(movimiento => {
            movimientosBody.appendChild(crearFila(movimiento));
        });
        contenedorTabla.classList.remove('oculto');
    }

    function crearFila(movimiento) {
        const fila = document.createElement('tr');
        agregarCelda(fila, 'Fecha', formatearFecha(movimiento.fecha));
        agregarProducto(fila, movimiento);
        agregarUsuario(fila, movimiento);
        agregarBadge(
            fila,
            'Tipo',
            textoTipo(movimiento.tipoMovimiento),
            TIPOS_INCREMENTO.has(movimiento.tipoMovimiento)
                    ? 'badge-activo'
                    : 'badge-discontinuado'
        );
        agregarCelda(fila, 'Origen', textoOrigen(movimiento.origen));

        const signo = TIPOS_INCREMENTO.has(movimiento.tipoMovimiento) ? '+' : '-';
        const clase = TIPOS_INCREMENTO.has(movimiento.tipoMovimiento)
                ? 'cantidad-entrada'
                : 'cantidad-salida';
        agregarCelda(fila, 'Cantidad', `${signo}${movimiento.cantidad}`, clase);
        agregarCelda(fila, 'Stock anterior', movimiento.stockAnterior);
        agregarCelda(fila, 'Stock posterior', movimiento.stockPosterior);
        return fila;
    }

    function agregarProducto(fila, movimiento) {
        const celda = document.createElement('td');
        celda.dataset.label = 'Producto';
        celda.className = 'producto-movimiento';
        const nombre = document.createElement('strong');
        nombre.textContent = movimiento.producto;
        const detalle = document.createElement('small');
        detalle.textContent = `ID ${movimiento.idProducto} · ${textoEstado(movimiento.estadoProducto)}`;
        celda.append(nombre, detalle);
        fila.appendChild(celda);
    }

    function agregarUsuario(fila, movimiento) {
        const celda = document.createElement('td');
        celda.dataset.label = 'Usuario';
        celda.className = 'usuario-movimiento';
        const nombre = document.createElement('strong');
        nombre.textContent = movimiento.usuario;
        const email = document.createElement('small');
        email.textContent = movimiento.emailUsuario;
        celda.append(nombre, email);
        fila.appendChild(celda);
    }

    function agregarCelda(fila, etiqueta, valor, clase = '') {
        const celda = document.createElement('td');
        celda.dataset.label = etiqueta;
        celda.textContent = valor;
        if (clase) celda.className = clase;
        fila.appendChild(celda);
    }

    function agregarBadge(fila, etiqueta, texto, clase) {
        const celda = document.createElement('td');
        celda.dataset.label = etiqueta;
        const badge = document.createElement('span');
        badge.className = `badge ${clase}`;
        badge.textContent = texto;
        celda.appendChild(badge);
        fila.appendChild(celda);
    }

    function limpiarFiltros() {
        formFiltros.reset();
        ocultarMensaje();
        consultarMovimientos();
    }

    function hayFiltrosAplicados() {
        return Boolean(
            idProducto.value
            || tipoMovimiento.value
            || origen.value
            || fechaDesde.value
            || fechaHasta.value
        );
    }

    function textoTipo(tipo) {
        const textos = {
            ENTRADA: 'Entrada',
            SALIDA: 'Salida',
            AJUSTE_POSITIVO: 'Ajuste positivo',
            AJUSTE_NEGATIVO: 'Ajuste negativo',
            DEVOLUCION: 'Devolución',
            RESERVA: 'Reserva',
            LIBERACION_RESERVA: 'Liberación de reserva'
        };
        return textos[tipo] || tipo;
    }

    function textoOrigen(valor) {
        const textos = {
            VENTA_LOCAL: 'Venta local',
            PEDIDO_ONLINE: 'Pedido online',
            REPOSICION_STOCK: 'Reposición de stock',
            AJUSTE_MANUAL: 'Ajuste manual',
            DEVOLUCION: 'Devolución',
            RESERVA_PEDIDO: 'Reserva de pedido'
        };
        return textos[valor] || valor;
    }

    function textoEstado(estado) {
        const textos = {
            ACTIVO: 'Activo',
            PAUSADO: 'Pausado',
            DISCONTINUADO: 'Discontinuado'
        };
        return textos[estado] || estado;
    }

    function formatearFecha(valor) {
        if (!valor) return 'No informada';
        const fecha = new Date(valor);
        if (Number.isNaN(fecha.getTime())) return 'No informada';
        return new Intl.DateTimeFormat('es-AR', {
            dateStyle: 'short',
            timeStyle: 'short'
        }).format(fecha);
    }

    async function obtenerMensajeError(response) {
        try {
            const body = await response.json();
            return body.message || body.mensaje || 'No se pudo completar la consulta.';
        } catch {
            return 'No se pudo completar la consulta.';
        }
    }

    function mostrarError(texto) {
        mensaje.textContent = texto;
        mensaje.className = 'mensaje mensaje-error';
    }

    function ocultarMensaje() {
        mensaje.textContent = '';
        mensaje.className = 'mensaje oculto';
    }

})();
