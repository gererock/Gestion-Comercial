(() => {

    'use strict';

    const API_STOCK_BAJO = '/api/admin/stock/productos-bajo-stock';
    const API_CATEGORIAS = '/api/categorias';
    const API_MARCAS = '/api/marcas';

    if (!Auth.requerirRol('ADMINISTRADOR')) {
        return;
    }

    const formFiltros = document.getElementById('formFiltros');
    const idProducto = document.getElementById('idProducto');
    const nombre = document.getElementById('nombre');
    const categoria = document.getElementById('categoria');
    const marca = document.getElementById('marca');
    const estado = document.getElementById('estado');
    const mensaje = document.getElementById('mensaje');
    const productosBody = document.getElementById('productosBody');
    const contenedorTabla = document.getElementById('contenedorTabla');
    const estadoVacio = document.getElementById('estadoVacio');
    const cantidadResultados = document.getElementById('cantidadResultados');
    const detalleProducto = document.getElementById('detalleProducto');

    iniciar();

    async function iniciar() {
        mostrarUsuario();
        configurarEventos();

        try {
            await Promise.all([cargarCategorias(), cargarMarcas()]);
            await consultarProductos();
        } catch (error) {
            mostrarError(error.message || 'No se pudo cargar la consulta de stock bajo.');
        }
    }

    function mostrarUsuario() {
        const usuario = Auth.obtenerUsuario();

        if (usuario) {
            document.getElementById('nombreUsuario').textContent =
                    usuario.nombre || usuario.email || 'Administrador';
        }
    }

    function configurarEventos() {
        document.getElementById('btnCerrarSesion').addEventListener(
                'click', Auth.cerrarSesion
        );
        document.getElementById('btnLimpiar').addEventListener(
                'click', limpiarFiltros
        );
        document.getElementById('btnCerrarDetalle').addEventListener(
                'click', cerrarDetalle
        );
        formFiltros.addEventListener('submit', buscar);
    }

    async function cargarCategorias() {
        const response = await Auth.fetchAutenticado(API_CATEGORIAS);

        if (!response) return;
        if (!response.ok) throw new Error(await obtenerMensajeError(response));

        const categorias = await response.json();
        categorias
                .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'))
                .forEach(item => agregarOpcion(
                        categoria,
                        item.id,
                        item.activa ? item.nombre : `${item.nombre} (inactiva)`
                ));
    }

    async function cargarMarcas() {
        const response = await Auth.fetchAutenticado(API_MARCAS);

        if (!response) return;
        if (!response.ok) throw new Error(await obtenerMensajeError(response));

        const marcas = await response.json();
        marcas
                .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'))
                .forEach(item => agregarOpcion(
                        marca,
                        item.id_marca,
                        item.activa ? item.nombre : `${item.nombre} (inactiva)`
                ));
    }

    function agregarOpcion(select, valor, texto) {
        const opcion = document.createElement('option');
        opcion.value = valor;
        opcion.textContent = texto;
        select.appendChild(opcion);
    }

    async function buscar(event) {
        event.preventDefault();
        ocultarMensaje();
        cerrarDetalle();

        const error = validarFiltros();

        if (error) {
            mostrarError(error);
            return;
        }

        await consultarProductos();
    }

    function validarFiltros() {
        const idOriginal = idProducto.value;
        const id = idOriginal.trim();

        if (idOriginal !== '' && id === '') {
            return 'El ID no puede contener solamente espacios.';
        }

        if (id !== '' && !/^[1-9][0-9]*$/.test(id)) {
            return 'El ID debe ser un número entero mayor a cero.';
        }

        if (id !== '' && (!Number.isSafeInteger(Number(id)) || Number(id) > 2147483647)) {
            return 'El ID ingresado supera el límite permitido.';
        }

        const nombreOriginal = nombre.value;
        const nombreLimpio = nombreOriginal.trim();

        if (nombreOriginal !== '' && nombreLimpio === '') {
            return 'El nombre de búsqueda no puede contener solamente espacios.';
        }

        if (nombreLimpio !== '' && !/[\p{L}\p{N}]/u.test(nombreLimpio)) {
            return 'El nombre de búsqueda debe contener letras o números.';
        }

        return null;
    }

    async function consultarProductos() {
        cantidadResultados.textContent = 'Cargando productos...';
        estadoVacio.classList.add('oculto');
        contenedorTabla.classList.add('oculto');

        try {
            const parametros = construirParametros();
            const url = parametros.size > 0
                    ? `${API_STOCK_BAJO}?${parametros}`
                    : API_STOCK_BAJO;
            const response = await Auth.fetchAutenticado(url);

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            renderizarProductos(await response.json());
        } catch (error) {
            cantidadResultados.textContent = 'No se pudo completar la consulta.';
            mostrarError(error.message || 'No se pudieron consultar los productos.');
        }
    }

    function construirParametros() {
        const parametros = new URLSearchParams();
        const valores = {
            idProducto: idProducto.value.trim(),
            nombre: nombre.value.trim(),
            idCategoria: categoria.value,
            idMarca: marca.value,
            estado: estado.value
        };

        Object.entries(valores).forEach(([clave, valor]) => {
            if (valor !== '') parametros.set(clave, valor);
        });

        return parametros;
    }

    function renderizarProductos(productos) {
        productosBody.innerHTML = '';
        cantidadResultados.textContent = `${productos.length} producto(s) encontrado(s).`;

        if (productos.length === 0) {
            estadoVacio.textContent = hayFiltrosAplicados()
                    ? 'No se encontraron productos con stock bajo para los filtros aplicados.'
                    : 'No existen productos con stock bajo en este momento.';
            estadoVacio.classList.remove('oculto');
            contenedorTabla.classList.add('oculto');
            return;
        }

        productos.forEach(producto => productosBody.appendChild(crearFila(producto)));
        estadoVacio.classList.add('oculto');
        contenedorTabla.classList.remove('oculto');
    }

    function crearFila(producto) {
        const fila = document.createElement('tr');

        if (producto.situacionStock === 'SIN_STOCK') {
            fila.classList.add('fila-sin-stock');
        }

        agregarCelda(fila, 'ID', producto.idProducto);
        agregarCelda(fila, 'Producto', producto.nombre);
        agregarCelda(fila, 'Categoría', producto.categoria || 'Sin categoría');
        agregarCelda(fila, 'Marca', producto.marca || 'Sin marca');
        agregarCelda(fila, 'Stock actual', producto.stockActual);
        agregarCelda(fila, 'Stock mínimo', producto.stockMinimo);
        agregarBadge(
                fila,
                'Estado',
                textoEstado(producto.estado),
                claseEstado(producto.estado)
        );
        agregarCelda(fila, 'Precio de venta', formatearPrecio(producto.precioVenta));
        agregarBadge(
                fila,
                'Nivel',
                textoSituacion(producto.situacionStock),
                producto.situacionStock === 'SIN_STOCK'
                        ? 'badge-sin-stock'
                        : 'badge-bajo-stock'
        );

        const celdaAcciones = document.createElement('td');
        celdaAcciones.dataset.label = 'Acciones';
        const acciones = document.createElement('div');
        acciones.className = 'acciones-producto';

        const ver = document.createElement('button');
        ver.type = 'button';
        ver.className = 'btn btn-light';
        ver.textContent = 'Ver información';
        ver.addEventListener('click', () => mostrarDetalle(producto));

        const reponer = document.createElement('a');
        reponer.className = 'btn btn-primary';
        reponer.textContent = 'Reponer stock';
        reponer.href = crearUrlReposicion(producto.idProducto);

        acciones.append(ver, reponer);
        celdaAcciones.appendChild(acciones);
        fila.appendChild(celdaAcciones);
        return fila;
    }

    function agregarCelda(fila, etiqueta, valor) {
        const celda = document.createElement('td');
        celda.dataset.label = etiqueta;
        celda.textContent = valor;
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

    function mostrarDetalle(producto) {
        document.getElementById('detalleId').textContent = producto.idProducto;
        document.getElementById('detalleNombre').textContent = producto.nombre;
        document.getElementById('detalleCategoria').textContent =
                producto.categoria || 'Sin categoría';
        document.getElementById('detalleMarca').textContent = producto.marca || 'Sin marca';
        document.getElementById('detalleStockActual').textContent = producto.stockActual;
        document.getElementById('detalleStockMinimo').textContent = producto.stockMinimo;
        document.getElementById('detalleEstado').textContent = textoEstado(producto.estado);
        document.getElementById('detallePrecio').textContent =
                formatearPrecio(producto.precioVenta);
        document.getElementById('detalleNivel').textContent =
                textoSituacion(producto.situacionStock);
        document.getElementById('btnReponerDetalle').href =
                crearUrlReposicion(producto.idProducto);

        detalleProducto.classList.remove('oculto');
        detalleProducto.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    function cerrarDetalle() {
        detalleProducto.classList.add('oculto');
    }

    async function limpiarFiltros() {
        formFiltros.reset();
        cerrarDetalle();
        ocultarMensaje();
        await consultarProductos();
    }

    function hayFiltrosAplicados() {
        return idProducto.value.trim() !== ''
                || nombre.value.trim() !== ''
                || categoria.value !== ''
                || marca.value !== ''
                || estado.value !== '';
    }

    function textoEstado(valor) {
        const estados = {
            ACTIVO: 'Activo',
            PAUSADO: 'Pausado',
            DISCONTINUADO: 'Discontinuado'
        };
        return estados[valor] || valor || 'Sin estado';
    }

    function claseEstado(valor) {
        const clases = {
            ACTIVO: 'badge-activo',
            PAUSADO: 'badge-pausado',
            DISCONTINUADO: 'badge-discontinuado'
        };
        return clases[valor] || '';
    }

    function textoSituacion(valor) {
        return valor === 'SIN_STOCK' ? 'Sin stock' : 'Stock bajo';
    }

    function formatearPrecio(valor) {
        return new Intl.NumberFormat('es-AR', {
            style: 'currency',
            currency: 'ARS'
        }).format(Number(valor));
    }

    function crearUrlReposicion(id) {
        return `/admin/reposicion-stock.html?idProducto=${encodeURIComponent(id)}`;
    }

    async function obtenerMensajeError(response) {
        try {
            const data = await response.json();
            return data.message || data.error || 'No se pudo realizar la consulta.';
        } catch {
            return 'No se pudo realizar la consulta.';
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
