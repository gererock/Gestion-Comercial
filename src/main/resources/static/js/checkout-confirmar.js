(() => {
    'use strict';

    if (!Auth.requerirRol('CLIENTE')) return;

    const KEY_PAGO = 'checkout_medio_pago_id';
    const KEY_ENTREGA = 'checkout_medio_entrega';
    const KEY_DIRECCION = 'checkout_direccion_envio_id';
    const KEY_CLAVE = 'checkout_confirmacion_clave';
    const KEY_FIRMA = 'checkout_confirmacion_firma';
    const KEY_INCIERTO = 'checkout_confirmacion_incierta';

    const $ = (id) => document.getElementById(id);
    const elementos = {
        nombreUsuario: $('nombreUsuario'),
        cerrarSesion: $('cerrarSesion'),
        mensaje: $('mensaje'),
        cargando: $('cargando'),
        contenido: $('contenido'),
        listaProductos: $('listaProductos'),
        medioEntrega: $('medioEntrega'),
        direccionEntrega: $('direccionEntrega'),
        medioPago: $('medioPago'),
        cantidadProductos: $('cantidadProductos'),
        subtotalEstimado: $('subtotalEstimado'),
        observaciones: $('observaciones'),
        confirmarPedido: $('confirmarPedido'),
        volver: $('volver'),
        pasoConfirmacion: $('pasoConfirmacion'),
        resultadoExitoso: $('resultadoExitoso'),
        numeroConfirmado: $('numeroConfirmado'),
        pagoConfirmado: $('pagoConfirmado'),
        totalConfirmado: $('totalConfirmado')
    };

    let confirmando = false;
    let datos = null;
    let intentoIncierto = sessionStorage.getItem(KEY_INCIERTO) === '1';

    elementos.cerrarSesion.addEventListener('click', Auth.cerrarSesion);
    elementos.confirmarPedido.addEventListener('click', confirmar);
    iniciar();

    function idPositivo(valor) {
        if (valor == null || !/^\d+$/.test(String(valor))) return null;
        const id = Number(valor);
        return Number.isSafeInteger(id) && id > 0 ? id : null;
    }

    function dinero(valor) {
        return new Intl.NumberFormat('es-AR', {
            style: 'currency', currency: 'ARS', minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(Number(valor));
    }

    function mostrarError(texto) {
        elementos.mensaje.textContent = texto;
        elementos.mensaje.className = 'message message-error';
        elementos.mensaje.classList.remove('hidden');
    }

    function ocultarError() {
        elementos.mensaje.textContent = '';
        elementos.mensaje.className = 'message hidden';
    }

    function navegar(url) {
        window.location.replace(url);
    }

    function carritoValido() {
        const carrito = Carrito.obtenerCarrito();
        if (!Array.isArray(carrito) || carrito.length === 0) return null;
        const ids = new Set();
        const productos = [];
        for (const item of carrito) {
            const id = idPositivo(item.idProducto);
            const cantidad = idPositivo(item.cantidad);
            if (!id || !cantidad || ids.has(id)) return null;
            ids.add(id);
            productos.push({ idProducto: id, cantidad });
        }
        return { carrito, productos };
    }

    async function consultar(url) {
        const respuesta = await Auth.fetchAutenticado(url, {
            headers: { Accept: 'application/json' }
        });
        if (!respuesta) throw new Error('Tu sesión ya no está disponible.');
        if (!respuesta.ok) {
            if (respuesta.status === 401) throw new Error('Tu sesión venció.');
            throw new Error('No se pudo validar el checkout. Volvé al paso anterior e intentá nuevamente.');
        }
        return respuesta.json();
    }

    async function iniciar() {
        ocultarError();
        if (intentoIncierto) {
            try {
                const firmaAnterior = JSON.parse(sessionStorage.getItem(KEY_FIRMA) || '{}');
                elementos.observaciones.value = firmaAnterior.observacionesCliente || '';
                elementos.observaciones.readOnly = true;
            } catch (_) { /* conservar datos seguros */ }
        }
        elementos.confirmarPedido.disabled = true;
        elementos.contenido.classList.add('hidden');
        elementos.cargando.classList.remove('hidden');

        const usuario = Auth.obtenerUsuario();
        if (usuario?.nombre) elementos.nombreUsuario.textContent = usuario.nombre;

        const carrito = carritoValido();
        if (!carrito) {
            navegar('/carrito/index.html');
            return;
        }

        const idMedioPago = idPositivo(sessionStorage.getItem(KEY_PAGO));
        if (!idMedioPago) {
            navegar('/cliente/checkout-medio-pago.html');
            return;
        }

        const medioEntrega = sessionStorage.getItem(KEY_ENTREGA);
        if (medioEntrega !== 'RETIRO_LOCAL' && medioEntrega !== 'ENVIO') {
            navegar('/cliente/checkout-entrega.html');
            return;
        }

        const idDireccionEnvio = medioEntrega === 'ENVIO'
            ? idPositivo(sessionStorage.getItem(KEY_DIRECCION)) : null;

        if (medioEntrega === 'ENVIO' && !idDireccionEnvio) {
            navegar('/cliente/checkout-direccion.html');
            return;
        }

        if (medioEntrega === 'RETIRO_LOCAL') {
            sessionStorage.removeItem(KEY_DIRECCION);
        }

        elementos.volver.href = medioEntrega === 'ENVIO'
            ? '/cliente/checkout-direccion.html' : '/cliente/checkout-entrega.html';

        try {
            const medio = await consultar(`/api/cliente/medios-pago/${idMedioPago}`);
            if (Number(medio.idMedioPago) !== idMedioPago || medio.activo !== true) {
                throw new Error('El medio de pago seleccionado ya no está disponible.');
            }

            let direccion = null;
            if (medioEntrega === 'ENVIO') {
                const config = await fetch('/api/public/configuracion-comercio/envio', {
                    headers: { Accept: 'application/json' }
                });
                if (!config.ok) throw new Error('No se pudo consultar la configuración de envío.');
                const condiciones = await config.json();
                if (typeof condiciones.permiteEnvioVillaMaria !== 'boolean' ||
                    typeof condiciones.permiteEnvioVillaNueva !== 'boolean') {
                    throw new Error('No pudimos verificar las localidades habilitadas.');
                }
                direccion = await consultar(`/api/cliente/direcciones-envio/${idDireccionEnvio}`);
                if (Number(direccion.idDireccionEnvio) !== idDireccionEnvio ||
                    direccion.habilitadaParaEnvio !== true) {
                    throw new Error('La dirección elegida ya no está disponible.');
                }
                const minimo = Number(condiciones.montoMinimoEnvio);
                const estimado = Number(Carrito.obtenerSubtotal());
                if (!Number.isFinite(minimo) || minimo < 0 || !Number.isFinite(estimado)) {
                    throw new Error('No se pudo verificar el monto mínimo de envío.');
                }
                // No bloquear por un total local estimado: el servidor recalcula
                // descuentos y monto mínimo sobre el total definitivo.
            }

            datos = {
                ...carrito,
                idMedioPago,
                medioEntrega,
                idDireccionEnvio,
                nombreMedioPago: medio.nombre,
                direccion
            };
            renderizar();
            elementos.contenido.classList.remove('hidden');
            elementos.confirmarPedido.disabled = false;
        } catch (error) {
            mostrarError(error.message || 'No se pudo preparar la confirmación.');
        } finally {
            elementos.cargando.classList.add('hidden');
        }
    }

    function renderizar() {
        elementos.listaProductos.replaceChildren();
        for (const item of datos.carrito) {
            const fila = document.createElement('div');
            fila.className = 'product';
            const info = document.createElement('div');
            const nombre = document.createElement('strong');
            nombre.textContent = String(item.nombre || `Producto #${item.idProducto}`);
            const cantidad = document.createElement('span');
            cantidad.className = 'product-meta';
            cantidad.textContent = `Cantidad: ${item.cantidad} · Precio estimado: ${dinero(item.precioUnitario ?? item.precioBase)}`;
            info.append(nombre, cantidad);
            const importe = document.createElement('span');
            importe.className = 'product-price';
            importe.textContent = dinero(item.subtotal ?? (Number(item.precioUnitario) * Number(item.cantidad)));
            fila.append(info, importe);
            elementos.listaProductos.append(fila);
        }
        elementos.cantidadProductos.textContent = String(Carrito.obtenerCantidadTotal());
        elementos.subtotalEstimado.textContent = dinero(Carrito.obtenerSubtotal());
        elementos.medioPago.textContent = datos.nombreMedioPago;
        elementos.medioEntrega.textContent = datos.medioEntrega === 'ENVIO' ? 'Envío a domicilio' : 'Retiro en local';
        elementos.direccionEntrega.textContent = datos.direccion
            ? `${datos.direccion.calle} ${datos.direccion.numero}${datos.direccion.pisoDepartamento ? ', ' + datos.direccion.pisoDepartamento : ''} — ${String(datos.direccion.localidad).replaceAll('_', ' ')}`
            : 'No requiere dirección';
    }

    function claveParaFirma(firma) {
        const anterior = sessionStorage.getItem(KEY_FIRMA);
        const clave = sessionStorage.getItem(KEY_CLAVE);
        if (anterior === firma && clave && /^[0-9a-f-]{36}$/i.test(clave)) return clave;
        const nuevaClave = crypto.randomUUID();
        sessionStorage.setItem(KEY_FIRMA, firma);
        sessionStorage.setItem(KEY_CLAVE, nuevaClave);
        return nuevaClave;
    }

    async function mensajeApi(respuesta) {
        // Nunca mostrar detalles internos de un error de servidor.
        if (respuesta.status >= 500) {
            return 'Hubo un problema del servidor. No podemos confirmar si el pedido se registró. Reintentá sin cambiar el carrito.';
        }
        try {
            const body = await respuesta.json();
            if (typeof body.message === 'string' && body.message.trim()) return body.message.trim();
            const primerError = Object.values(body.errors || {}).find(v => typeof v === 'string');
            if (primerError) return primerError;
        } catch (_) { /* usar mensaje genérico */ }
        return 'No se pudo confirmar el pedido. Revisá los datos e intentá nuevamente.';
    }

    async function confirmar() {
        if (confirmando || !datos) return;
        ocultarError();
        const actual = carritoValido();
        if (!actual) {
            mostrarError('El carrito cambió o está vacío. Volvé al carrito para revisarlo.');
            return;
        }
        const observacionesCliente = elementos.observaciones.value.trim();
        if (observacionesCliente.length > 500) {
            mostrarError('Las observaciones no pueden superar 500 caracteres.');
            return;
        }
        const firma = JSON.stringify({
            email: Auth.obtenerUsuario()?.email || '',
            productos: actual.productos,
            idMedioPago: datos.idMedioPago,
            medioEntrega: datos.medioEntrega,
            idDireccionEnvio: datos.idDireccionEnvio,
            observacionesCliente
        });
        if (intentoIncierto && sessionStorage.getItem(KEY_FIRMA) !== firma) {
            mostrarError('Después de un error de conexión debés reintentar sin modificar el pedido.');
            return;
        }
        const payload = {
            claveConfirmacion: claveParaFirma(firma),
            productos: actual.productos,
            idMedioPago: datos.idMedioPago,
            medioEntrega: datos.medioEntrega,
            idDireccionEnvio: datos.idDireccionEnvio,
            observacionesCliente: observacionesCliente || null
        };
        confirmando = true;
        elementos.confirmarPedido.disabled = true;
        elementos.observaciones.readOnly = true;
        elementos.confirmarPedido.textContent = 'Confirmando…';
        try {
            const respuesta = await Auth.fetchAutenticado('/api/cliente/pedidos', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!respuesta) return;
            if (!respuesta.ok) {
                const mensaje = await mensajeApi(respuesta);
                if (respuesta.status >= 500) {
                    intentoIncierto = true;
                    sessionStorage.setItem(KEY_INCIERTO, '1');
                }
                throw new Error(mensaje);
            }
            const resultado = await respuesta.json();
            if (!resultado || !idPositivo(resultado.idPedido) || !resultado.numeroPedido) {
                intentoIncierto = true;
                sessionStorage.setItem(KEY_INCIERTO, '1');
                throw new Error('El servidor respondió sin los datos del pedido. Reintentá sin cambiar el carrito.');
            }
            // Solo el servidor confirma la creación. Recién ahora vaciar.
            Carrito.vaciarCarrito();
            [KEY_PAGO, KEY_ENTREGA, KEY_DIRECCION, KEY_CLAVE, KEY_FIRMA, KEY_INCIERTO].forEach(k => sessionStorage.removeItem(k));
            elementos.pasoConfirmacion.classList.add('hidden');
            elementos.resultadoExitoso.classList.remove('hidden');
            elementos.numeroConfirmado.textContent = resultado.numeroPedido;
            elementos.pagoConfirmado.textContent = resultado.estadoPago === 'PENDIENTE' ? 'Pendiente' : String(resultado.estadoPago);
            elementos.totalConfirmado.textContent = dinero(resultado.total);
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } catch (error) {
            // Para fallos que podrían haber ocurrido después de registrar el pedido,
            // mantener clave, carrito y observaciones para reintentar la misma compra.
            if (!(error instanceof Error) || /Failed to fetch|NetworkError|fetch failed|conexión/i.test(error.message)) {
                intentoIncierto = true;
                sessionStorage.setItem(KEY_INCIERTO, '1');
            }
            mostrarError(error.message || 'No se pudo confirmar el pedido. Intentá nuevamente.');
            elementos.confirmarPedido.disabled = false;
            elementos.observaciones.readOnly = intentoIncierto;
        } finally {
            confirmando = false;
            elementos.confirmarPedido.textContent = 'Confirmar pedido';
        }
    }
})();
