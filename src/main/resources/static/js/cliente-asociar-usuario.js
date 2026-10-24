(() => {

    'use strict';

    const API = '/api/admin/clientes-usuarios';

    if (!Auth.requerirRol('ADMINISTRADOR')) {
        return;
    }

    const mensaje = document.getElementById('mensaje');
    const btnConfirmar = document.getElementById('btnConfirmar');
    const listaClientes = document.getElementById('listaClientes');
    const listaUsuarios = document.getElementById('listaUsuarios');
    const cargandoClientes = document.getElementById('cargandoClientes');
    const cargandoUsuarios = document.getElementById('cargandoUsuarios');
    const sinClientes = document.getElementById('sinClientes');
    const sinUsuarios = document.getElementById('sinUsuarios');
    const buscarCliente = document.getElementById('buscarCliente');
    const buscarUsuario = document.getElementById('buscarUsuario');

    let clienteSeleccionado = null;
    let usuarioSeleccionado = null;
    let asociando = false;
    let asociacionCompletada = false;
    let idClienteInicial = obtenerIdClienteInicial();

    iniciar();

    function iniciar() {
        mostrarUsuario();

        document.getElementById('btnCerrarSesion').addEventListener(
            'click',
            Auth.cerrarSesion
        );

        document.getElementById('formBuscarCliente').addEventListener(
            'submit',
            event => {
                event.preventDefault();
                buscar('cliente');
            }
        );

        document.getElementById('formBuscarUsuario').addEventListener(
            'submit',
            event => {
                event.preventDefault();
                buscar('usuario');
            }
        );

        document.getElementById('btnLimpiarClientes').addEventListener(
            'click',
            () => {
                buscarCliente.value = '';
                cargarClientes(null);
            }
        );

        document.getElementById('btnLimpiarUsuarios').addEventListener(
            'click',
            () => {
                buscarUsuario.value = '';
                cargarUsuarios(null);
            }
        );

        btnConfirmar.addEventListener('click', confirmarAsociacion);

        if (new URLSearchParams(window.location.search).has('idCliente')
                && idClienteInicial === null) {
            mostrarError('El cliente indicado no es válido.');
        }

        cargarClientes(null);
        cargarUsuarios(null);
    }

    function mostrarUsuario() {
        const usuario = Auth.obtenerUsuario();

        if (usuario && usuario.nombre) {
            document.getElementById('nombreUsuario').textContent = usuario.nombre;
        }
    }

    function obtenerIdClienteInicial() {
        const parametros = new URLSearchParams(window.location.search);

        if (!parametros.has('idCliente')) {
            return null;
        }

        const valor = Number(parametros.get('idCliente'));

        return Number.isInteger(valor) && valor > 0
                ? valor
                : null;
    }

    function buscar(tipo) {
        if (asociacionCompletada) {
            return;
        }

        const input = tipo === 'cliente' ? buscarCliente : buscarUsuario;
        const validacion = validarBusqueda(input.value);

        if (validacion) {
            mostrarError(validacion);
            return;
        }

        ocultarMensaje();
        const valor = input.value.trim();

        if (tipo === 'cliente') {
            cargarClientes(valor === '' ? null : valor);
        } else {
            cargarUsuarios(valor === '' ? null : valor);
        }
    }

    function validarBusqueda(valor) {
        if (valor === '') {
            return null;
        }

        if (valor.trim() === '') {
            return 'La búsqueda no puede contener solamente espacios.';
        }

        const normalizada = valor.trim().replace(/\s+/g, ' ');

        if (normalizada.length > 120) {
            return 'La búsqueda no puede superar los 120 caracteres.';
        }

        if (!/[\p{L}\p{N}]/u.test(normalizada)) {
            return 'La búsqueda ingresada no es válida.';
        }

        return null;
    }

    async function cargarClientes(busqueda) {
        prepararCarga(listaClientes, sinClientes, cargandoClientes);

        try {
            const response = await Auth.fetchAutenticado(
                construirUrl('/clientes', busqueda)
            );

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            renderizarClientes(await response.json());
        } catch (error) {
            mostrarError(error.message || 'No se pudieron consultar los clientes.');
            listaClientes.classList.add('oculto');
        } finally {
            cargandoClientes.classList.add('oculto');
        }
    }

    async function cargarUsuarios(busqueda) {
        prepararCarga(listaUsuarios, sinUsuarios, cargandoUsuarios);

        try {
            const response = await Auth.fetchAutenticado(
                construirUrl('/usuarios', busqueda)
            );

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            renderizarUsuarios(await response.json());
        } catch (error) {
            mostrarError(error.message || 'No se pudieron consultar los usuarios.');
            listaUsuarios.classList.add('oculto');
        } finally {
            cargandoUsuarios.classList.add('oculto');
        }
    }

    function construirUrl(ruta, busqueda) {
        if (busqueda === null) {
            return API + ruta;
        }

        return `${API}${ruta}?busqueda=${encodeURIComponent(busqueda)}`;
    }

    function prepararCarga(lista, sinResultados, cargando) {
        lista.classList.add('oculto');
        sinResultados.classList.add('oculto');
        cargando.classList.remove('oculto');
    }

    function renderizarClientes(clientes) {
        listaClientes.innerHTML = '';

        if (!Array.isArray(clientes) || clientes.length === 0) {
            sinClientes.classList.remove('oculto');
            return;
        }

        clientes.forEach(cliente => {
            const disponible = cliente.estado === true && cliente.asociado !== true;
            const motivo = cliente.estado !== true
                    ? 'Cliente inactivo'
                    : cliente.asociado === true
                        ? `Ya asociado${cliente.emailUsuarioAsociado ? ` a ${cliente.emailUsuarioAsociado}` : ''}`
                        : null;

            const boton = crearOpcion({
                tipo: 'cliente',
                id: cliente.idCliente,
                titulo: nombreCompleto(cliente, 'Cliente'),
                etiqueta: cliente.estado === true ? 'Activo' : 'Inactivo',
                detalle: [cliente.dni ? `DNI ${cliente.dni}` : null, cliente.correo].filter(Boolean).join(' · ') || 'Sin DNI ni correo',
                disponible,
                motivo,
                seleccionado: clienteSeleccionado?.idCliente === cliente.idCliente,
                alSeleccionar: () => seleccionarCliente(cliente)
            });

            listaClientes.appendChild(boton);

            if (idClienteInicial === cliente.idCliente) {
                idClienteInicial = null;

                if (disponible) {
                    seleccionarCliente(cliente);
                } else {
                    mostrarError(motivo || 'El cliente no está disponible para asociar.');
                }
            }
        });

        listaClientes.classList.remove('oculto');
    }

    function renderizarUsuarios(usuarios) {
        listaUsuarios.innerHTML = '';

        if (!Array.isArray(usuarios) || usuarios.length === 0) {
            sinUsuarios.classList.remove('oculto');
            return;
        }

        usuarios.forEach(usuario => {
            const esCliente = usuario.rol === 'CLIENTE';
            const estaActivo = usuario.estado === 'ACTIVO';
            const disponible = esCliente && estaActivo && usuario.asociado !== true;
            let motivo = null;

            if (!esCliente) motivo = `Rol ${usuario.rol || 'no informado'} no permitido`;
            else if (!estaActivo) motivo = 'Usuario inactivo';
            else if (usuario.asociado === true) motivo = 'Ya asociado a otro cliente';

            listaUsuarios.appendChild(crearOpcion({
                tipo: 'usuario',
                id: usuario.idUsuario,
                titulo: nombreCompleto(usuario, 'Usuario'),
                etiqueta: usuario.rol || 'Sin rol',
                detalle: usuario.email || 'Sin correo',
                disponible,
                motivo,
                seleccionado: usuarioSeleccionado?.idUsuario === usuario.idUsuario,
                alSeleccionar: () => seleccionarUsuario(usuario)
            }));
        });

        listaUsuarios.classList.remove('oculto');
    }

    function crearOpcion(datos) {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'opcion-asociacion';
        boton.dataset.tipo = datos.tipo;
        boton.dataset.id = String(datos.id);
        boton.disabled = !datos.disponible || asociacionCompletada;

        if (datos.seleccionado) {
            boton.classList.add('seleccionada');
        }

        const cabecera = document.createElement('span');
        cabecera.className = 'opcion-cabecera';

        const titulo = document.createElement('strong');
        titulo.textContent = datos.titulo;

        const badge = document.createElement('span');
        badge.className = 'badge';
        badge.textContent = datos.etiqueta;

        cabecera.append(titulo, badge);

        const detalle = document.createElement('small');
        detalle.textContent = datos.detalle;

        boton.append(cabecera, detalle);

        if (datos.motivo) {
            const motivo = document.createElement('small');
            motivo.className = 'motivo-no-disponible';
            motivo.textContent = datos.motivo;
            boton.appendChild(motivo);
        }

        boton.addEventListener('click', datos.alSeleccionar);
        return boton;
    }

    function seleccionarCliente(cliente) {
        if (asociacionCompletada) return;
        clienteSeleccionado = cliente;
        marcarSeleccion('cliente', cliente.idCliente);
        actualizarResumen();
    }

    function seleccionarUsuario(usuario) {
        if (asociacionCompletada) return;
        usuarioSeleccionado = usuario;
        marcarSeleccion('usuario', usuario.idUsuario);
        actualizarResumen();
    }

    function marcarSeleccion(tipo, id) {
        document.querySelectorAll(`[data-tipo="${tipo}"]`).forEach(elemento => {
            elemento.classList.toggle(
                'seleccionada',
                Number(elemento.dataset.id) === id
            );
        });
    }

    function actualizarResumen() {
        document.getElementById('clienteSeleccionado').textContent = clienteSeleccionado
                ? nombreCompleto(clienteSeleccionado, 'Cliente')
                : 'Ninguno';
        document.getElementById('clienteSeleccionadoDetalle').textContent = clienteSeleccionado
                ? [clienteSeleccionado.dni ? `DNI ${clienteSeleccionado.dni}` : null, clienteSeleccionado.correo].filter(Boolean).join(' · ') || `Cliente #${clienteSeleccionado.idCliente}`
                : 'Seleccioná una ficha de cliente.';

        document.getElementById('usuarioSeleccionado').textContent = usuarioSeleccionado
                ? nombreCompleto(usuarioSeleccionado, 'Usuario')
                : 'Ninguno';
        document.getElementById('usuarioSeleccionadoDetalle').textContent = usuarioSeleccionado
                ? usuarioSeleccionado.email
                : 'Seleccioná una cuenta de usuario.';

        btnConfirmar.disabled = !clienteSeleccionado || !usuarioSeleccionado || asociando || asociacionCompletada;
    }

    async function confirmarAsociacion() {
        if (asociando || asociacionCompletada) return;

        if (!clienteSeleccionado) {
            mostrarError('Debe seleccionar un cliente.');
            return;
        }

        if (!usuarioSeleccionado) {
            mostrarError('Debe seleccionar un usuario.');
            return;
        }

        asociando = true;
        btnConfirmar.disabled = true;
        btnConfirmar.textContent = 'Asociando...';
        ocultarMensaje();

        try {
            const response = await Auth.fetchAutenticado(API, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    idCliente: clienteSeleccionado.idCliente,
                    idUsuario: usuarioSeleccionado.idUsuario
                })
            });

            if (!response) return;
            if (!response.ok) throw new Error(await obtenerMensajeError(response));

            const resultado = await response.json();
            asociacionCompletada = true;
            mostrarExito(resultado.mensaje || 'Cliente y usuario asociados correctamente.');
            btnConfirmar.textContent = 'Asociación realizada';

            const btnCancelar = document.getElementById('btnCancelar');
            btnCancelar.textContent = 'Ver detalle del cliente';
            btnCancelar.href = `/vendedor/cliente-detalle.html?id=${resultado.idCliente}`;

            document.querySelectorAll('.opcion-asociacion').forEach(elemento => {
                elemento.disabled = true;
            });
        } catch (error) {
            mostrarError(error.message || 'No se pudo realizar la asociación.');
        } finally {
            asociando = false;

            if (!asociacionCompletada) {
                btnConfirmar.textContent = 'Confirmar asociación';
                actualizarResumen();
            }
        }
    }

    function nombreCompleto(persona, prefijo) {
        const nombre = `${persona.nombre || ''} ${persona.apellido || ''}`.trim();
        return nombre || `${prefijo} #${persona.idCliente || persona.idUsuario}`;
    }

    async function obtenerMensajeError(response) {
        try {
            const body = await response.json();

            if (body.errors && Object.keys(body.errors).length > 0) {
                return Object.values(body.errors).join(' ');
            }

            return body.message || body.mensaje || 'No se pudo realizar la asociación.';
        } catch {
            return 'No se pudo realizar la asociación.';
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
