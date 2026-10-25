(() => {

    'use strict';

    const API_PRODUCTOS = '/api/productos';
    const API_AJUSTES = '/api/admin/stock/ajustes';
    const CANTIDAD_MAXIMA = 1000000;

    if (!Auth.requerirRol('ADMINISTRADOR')) {
        return;
    }

    const mensaje = document.getElementById('mensaje');
    const formBuscar = document.getElementById('formBuscar');
    const busqueda = document.getElementById('busqueda');
    const cargandoProductos = document.getElementById('cargandoProductos');
    const sinResultados = document.getElementById('sinResultados');
    const contenedorResultados = document.getElementById('contenedorResultados');
    const productosBody = document.getElementById('productosBody');
    const seccionAjuste = document.getElementById('seccionAjuste');
    const formAjuste = document.getElementById('formAjuste');
    const cantidad = document.getElementById('cantidad');
    const btnConfirmar = document.getElementById('btnConfirmar');
    const resultadoAjuste = document.getElementById('resultadoAjuste');
    const stockPosteriorVista = document.getElementById('stockPosteriorVista');

    let productoSeleccionado = null;
    let claveOperacion = null;
    let ajustando = false;

    iniciar();

    function iniciar() {
        mostrarUsuario();
        document.getElementById('btnCerrarSesion').addEventListener(
            'click',
            Auth.cerrarSesion
        );
        document.getElementById('btnLimpiar').addEventListener('click', limpiarBusqueda);
        document.getElementById('btnCancelar').addEventListener('click', cancelarSeleccion);
        formBuscar.addEventListener('submit', buscarProductos);
        formAjuste.addEventListener('submit', registrarAjuste);
        formAjuste.addEventListener('input', actualizarVistaStock);
        cargarProductos('');
    }

    function mostrarUsuario() {
        const usuario = Auth.obtenerUsuario();

        if (usuario && usuario.nombre) {
            document.getElementById('nombreUsuario').textContent = usuario.nombre;
        }
    }

    function buscarProductos(event) {
        event.preventDefault();
        const error = validarBusqueda(busqueda.value);

        if (error) {
            mostrarError(error);
            return;
        }

        cargarProductos(busqueda.value.trim());
    }

    async function cargarProductos(termino) {
        ocultarMensaje();
        cargandoProductos.classList.remove('oculto');
        sinResultados.classList.add('oculto');
        contenedorResultados.classList.add('oculto');

        try {
            const parametros = new URLSearchParams();

            if (termino) {
                parametros.set(/^\d+$/.test(termino) ? 'idProducto' : 'nombre', termino);
            }

            const url = parametros.size > 0
                    ? `${API_PRODUCTOS}?${parametros}`
                    : API_PRODUCTOS;
            const response = await Auth.fetchAutenticado(url);

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            renderizarProductos(await response.json(), Boolean(termino));
        } catch (error) {
            mostrarError(error.message || 'No se pudieron consultar los productos.');
        } finally {
            cargandoProductos.classList.add('oculto');
        }
    }

    function validarBusqueda(valor) {
        if (valor === '') return null;
        if (valor.trim() === '') return 'La búsqueda no puede contener solamente espacios.';
        if (!/[\p{L}\p{N}]/u.test(valor)) return 'Ingresá un ID o nombre de producto válido.';
        return null;
    }

    function renderizarProductos(productos, teniaFiltro) {
        productosBody.innerHTML = '';

        if (productos.length === 0) {
            sinResultados.textContent = teniaFiltro
                    ? 'No se encontró ningún producto con esa búsqueda.'
                    : 'No existen productos cargados.';
            sinResultados.classList.remove('oculto');
            return;
        }

        productos.forEach(producto => productosBody.appendChild(crearFila(producto)));
        contenedorResultados.classList.remove('oculto');
    }

    function crearFila(producto) {
        const fila = document.createElement('tr');
        agregarCelda(fila, 'ID', producto.idProducto);
        agregarCelda(fila, 'Producto', producto.nombre);

        const celdaEstado = document.createElement('td');
        celdaEstado.dataset.label = 'Estado';
        const estado = document.createElement('span');
        estado.className = `badge ${claseEstado(producto.estado)}`;
        estado.textContent = textoEstado(producto.estado);
        celdaEstado.appendChild(estado);
        fila.appendChild(celdaEstado);

        agregarCelda(fila, 'Stock actual', producto.stockActual);

        const celdaAccion = document.createElement('td');
        celdaAccion.dataset.label = 'Acción';
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'btn btn-primary';
        boton.textContent = 'Seleccionar';
        boton.addEventListener('click', () => seleccionarProducto(producto));
        celdaAccion.appendChild(boton);
        fila.appendChild(celdaAccion);

        return fila;
    }

    function agregarCelda(fila, etiqueta, valor) {
        const celda = document.createElement('td');
        celda.dataset.label = etiqueta;
        celda.textContent = valor;
        fila.appendChild(celda);
    }

    function seleccionarProducto(producto) {
        productoSeleccionado = producto;
        claveOperacion = null;
        cantidad.value = '';
        limpiarTipoAjuste();
        resultadoAjuste.classList.add('oculto');
        stockPosteriorVista.classList.add('oculto');
        ocultarMensaje();

        document.getElementById('productoSeleccionado').textContent = producto.nombre;
        document.getElementById('productoId').textContent = `ID ${producto.idProducto}`;
        document.getElementById('stockActual').textContent = producto.stockActual;

        const estado = document.getElementById('productoEstado');
        estado.textContent = textoEstado(producto.estado);
        estado.className = `badge ${claseEstado(producto.estado)}`;

        seccionAjuste.classList.remove('oculto');
        seccionAjuste.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    async function registrarAjuste(event) {
        event.preventDefault();

        if (ajustando) return;

        if (!productoSeleccionado) {
            mostrarError('Debe seleccionar un producto antes de registrar el ajuste.');
            return;
        }

        const tipoAjuste = obtenerTipoAjuste();

        if (!tipoAjuste) {
            mostrarError('Debe seleccionar un tipo de ajuste.');
            return;
        }

        const error = validarCantidad(cantidad.value);

        if (error) {
            mostrarError(error);
            return;
        }

        const numero = Number(cantidad.value);

        if (tipoAjuste === 'NEGATIVO' && numero > productoSeleccionado.stockActual) {
            mostrarError('El ajuste negativo no puede dejar el stock por debajo de cero.');
            return;
        }

        if (!claveOperacion) {
            claveOperacion = crearClaveOperacion();
        }

        ajustando = true;
        btnConfirmar.disabled = true;
        btnConfirmar.textContent = 'Registrando...';
        ocultarMensaje();

        try {
            const response = await Auth.fetchAutenticado(API_AJUSTES, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    idProducto: String(productoSeleccionado.idProducto),
                    tipoAjuste,
                    cantidad: cantidad.value,
                    claveOperacion
                })
            });

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            const resultado = await response.json();
            productoSeleccionado.stockActual = resultado.stockPosterior;
            document.getElementById('stockActual').textContent = resultado.stockPosterior;
            mostrarResultado(resultado);
            mostrarExito(resultado.mensaje || 'Ajuste manual registrado correctamente.');
            cantidad.value = '';
            limpiarTipoAjuste();
            stockPosteriorVista.classList.add('oculto');
            claveOperacion = null;
            cargarProductos(busqueda.value.trim());
        } catch (errorRegistro) {
            mostrarError(errorRegistro.message || 'No se pudo registrar el ajuste manual.');
        } finally {
            ajustando = false;
            btnConfirmar.disabled = false;
            btnConfirmar.textContent = 'Confirmar ajuste';
        }
    }

    function validarCantidad(valor) {
        if (valor === '') return 'La cantidad del ajuste es obligatoria.';
        if (valor.trim() === '') return 'La cantidad no puede contener solamente espacios.';
        if (!/^[1-9][0-9]*$/.test(valor)) {
            return 'La cantidad debe ser un número entero mayor a cero.';
        }

        const numero = Number(valor);

        if (!Number.isSafeInteger(numero) || numero > CANTIDAD_MAXIMA) {
            return 'La cantidad máxima permitida por ajuste es 1.000.000.';
        }

        return null;
    }

    function actualizarVistaStock() {
        if (!productoSeleccionado) return;

        const tipoAjuste = obtenerTipoAjuste();
        const error = validarCantidad(cantidad.value);

        if (!tipoAjuste || error) {
            stockPosteriorVista.classList.add('oculto');
            return;
        }

        const numero = Number(cantidad.value);
        const posterior = tipoAjuste === 'POSITIVO'
                ? productoSeleccionado.stockActual + numero
                : productoSeleccionado.stockActual - numero;

        if (!Number.isSafeInteger(posterior) || posterior < 0) {
            stockPosteriorVista.classList.add('oculto');
            return;
        }

        document.getElementById('stockPosterior').textContent = posterior;
        stockPosteriorVista.classList.remove('oculto');
    }

    function obtenerTipoAjuste() {
        const seleccionado = formAjuste.querySelector('input[name="tipoAjuste"]:checked');
        return seleccionado ? seleccionado.value : null;
    }

    function limpiarTipoAjuste() {
        formAjuste.querySelectorAll('input[name="tipoAjuste"]').forEach(
                opcion => { opcion.checked = false; }
        );
    }

    function mostrarResultado(resultado) {
        document.getElementById('resultadoProducto').textContent = resultado.producto;
        document.getElementById('resultadoCantidad').textContent = resultado.cantidad;
        document.getElementById('resultadoAnterior').textContent = resultado.stockAnterior;
        document.getElementById('resultadoPosterior').textContent = resultado.stockPosterior;
        document.getElementById('resultadoTipo').textContent = textoTipo(resultado.tipoMovimiento);
        document.getElementById('resultadoOrigen').textContent = 'Ajuste manual';
        resultadoAjuste.classList.remove('oculto');
    }

    function cancelarSeleccion() {
        productoSeleccionado = null;
        claveOperacion = null;
        cantidad.value = '';
        limpiarTipoAjuste();
        seccionAjuste.classList.add('oculto');
        resultadoAjuste.classList.add('oculto');
        stockPosteriorVista.classList.add('oculto');
        ocultarMensaje();
    }

    function limpiarBusqueda() {
        busqueda.value = '';
        cancelarSeleccion();
        cargarProductos('');
    }

    function crearClaveOperacion() {
        if (window.crypto && typeof window.crypto.randomUUID === 'function') {
            return window.crypto.randomUUID();
        }

        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, caracter => {
            const aleatorio = Math.floor(Math.random() * 16);
            const valor = caracter === 'x' ? aleatorio : (aleatorio & 0x3) | 0x8;
            return valor.toString(16);
        });
    }

    function textoEstado(estado) {
        const textos = {
            ACTIVO: 'Activo',
            PAUSADO: 'Pausado',
            DISCONTINUADO: 'Discontinuado'
        };
        return textos[estado] || estado || 'Sin estado';
    }

    function claseEstado(estado) {
        const clases = {
            ACTIVO: 'badge-activo',
            PAUSADO: 'badge-pausado',
            DISCONTINUADO: 'badge-discontinuado'
        };
        return clases[estado] || '';
    }

    function textoTipo(tipo) {
        return tipo === 'AJUSTE_POSITIVO' ? 'Ajuste positivo' : 'Ajuste negativo';
    }

    async function obtenerMensajeError(response) {
        try {
            const body = await response.json();

            if (body.errors && Object.keys(body.errors).length > 0) {
                return Object.values(body.errors).join(' ');
            }

            return body.message || body.mensaje || 'No se pudo completar la operación.';
        } catch {
            return 'No se pudo completar la operación.';
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
