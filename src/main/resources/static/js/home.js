(() => {

    'use strict';


    /* =====================================================
       ELEMENTOS GENERALES
       ===================================================== */

    const navToggle =
        document.getElementById('nav-toggle');

    const mainNav =
        document.getElementById('main-nav');


    const accountLink =
        document.getElementById('account-link');

    const footerAccountLink =
        document.getElementById('footer-account-link');


    /* =====================================================
       IMÁGENES
       ===================================================== */

    const logoHeader =
        document.getElementById('logo-header');

    const logoFooter =
        document.getElementById('logo-footer');

    const portada =
        document.getElementById('imagen-portada');

    const imagenNosotros =
        document.getElementById('imagen-nosotros');


    /* =====================================================
       MUESTRARIO - NUESTRA HISTORIA
       ===================================================== */

    const historiaImagenPrincipal =
        document.getElementById(
            'historia-imagen-principal'
        );

    const historiaMiniaturas =
        document.getElementById(
            'historia-miniaturas'
        );

    const historiaMuestrario =
        document.getElementById(
            'historia-muestrario'
        );


    let historiaImagenes = [];

    let historiaIndiceActual = 0;

    let historiaIntervalo = null;


    const HISTORIA_TIEMPO_ROTACION =
        5000;


    /* =====================================================
       CARGAR DATOS
       ===================================================== */

    async function cargarDatosComercio() {

        try {

            const response =
                await fetch('/data/comercio.json');


            if (!response.ok) {

                throw new Error(
                    'No se pudieron cargar los datos del comercio.'
                );

            }


            const comercio =
                await response.json();


            aplicarDatosComercio(
                comercio
            );


        } catch (error) {

            console.error(
                'Error al cargar los datos del comercio:',
                error
            );

        }

    }


    /* =====================================================
       APLICAR DATOS
       ===================================================== */

    function aplicarDatosComercio(
        comercio
    ) {

        /* Nombre */

        establecerTexto(
            'nombre-comercio',
            comercio.nombre
        );

        establecerTexto(
            'nombre-comercio-header',
            comercio.nombre
        );

        establecerTexto(
            'nombre-comercio-footer',
            comercio.nombre
        );

        establecerTexto(
            'footer-copyright-nombre',
            comercio.nombre
        );


        /* Descripción */

        establecerTexto(
            'descripcion-comercio',
            comercio.descripcion
        );

        establecerTexto(
            'descripcion-comercio-footer',
            comercio.descripcion
        );


        /* Institucional */

        establecerTexto(
            'texto-nosotros',
            comercio.sobreNosotros
        );

        establecerTexto(
            'texto-historia',
            comercio.historia
        );


        /* Ubicación */

        establecerTexto(
            'direccion-comercio',
            comercio.direccion
        );

        establecerTexto(
            'localidad-comercio',
            comercio.localidad
        );

        establecerTexto(
            'provincia-comercio',
            comercio.provincia
        );


        establecerTexto(
            'footer-direccion',
            comercio.direccion
        );

        establecerTexto(
            'footer-localidad',
            comercio.localidad
        );

        establecerTexto(
            'footer-provincia',
            comercio.provincia
        );


        aplicarHorarios(
            comercio.horarios
        );


        aplicarMapa(
            comercio.mapaUrl
        );


        /* Contacto */

        establecerTexto(
            'telefono-comercio',
            comercio.telefono
        );

        establecerTexto(
            'correo-comercio',
            comercio.correo
        );


        aplicarTelefono(
            comercio.telefono
        );


        aplicarCorreo(
            comercio.correo
        );


        aplicarWhatsapp(
            comercio.whatsapp,
            comercio.whatsappMensaje
        );


        aplicarInstagram(
            comercio.instagram
        );


        /* Imágenes */

        aplicarImagen(
            logoHeader,
            comercio.logo
        );


        aplicarImagen(
            logoFooter,
            comercio.logo
        );


        aplicarImagen(
            portada,
            comercio.portada
        );


        aplicarImagen(
            imagenNosotros,
            comercio.imagenNosotros
        );


        /* Muestrario Historia */

        configurarMuestrarioHistoria(
            comercio.imagenesHistoria
        );


        /* Equipo */

        renderizarEquipo(
            comercio.equipo
        );


        /* Galería */

        renderizarGaleria(
            comercio.imagenesSecundarias
        );


        /* Catálogo */

        aplicarRutaCatalogo(
            comercio.catalogoUrl
        );


        /* Título */

        if (
            typeof comercio.nombre === 'string'
            && comercio.nombre.trim() !== ''
        ) {

            document.title =
                `${comercio.nombre.trim()} | Inicio`;

        }

    }


    /* =====================================================
       TEXTO
       ===================================================== */

    function establecerTexto(
        id,
        valor
    ) {

        const elemento =
            document.getElementById(id);


        if (!elemento) {
            return;
        }


        if (
            typeof valor !== 'string'
            || valor.trim() === ''
        ) {
            return;
        }


        elemento.textContent =
            valor.trim();

    }


    /* =====================================================
       IMÁGENES
       ===================================================== */

    function aplicarImagen(
        elemento,
        ruta
    ) {

        if (!elemento) {
            return;
        }


        if (
            typeof ruta !== 'string'
            || ruta.trim() === ''
        ) {

            elemento.remove();

            return;
        }


        elemento.src =
            ruta.trim();

    }


    function configurarImagen(
        elemento
    ) {

        if (!elemento) {
            return;
        }


        elemento.addEventListener(
            'load',
            () => {

                elemento.classList.add(
                    'is-loaded'
                );

            }
        );


        elemento.addEventListener(
            'error',
            () => {

                elemento.classList.remove(
                    'is-loaded'
                );

            }
        );


        if (
            elemento.complete
            && elemento.naturalWidth > 0
        ) {

            elemento.classList.add(
                'is-loaded'
            );

        }

    }


    /* =====================================================
       MUESTRARIO - NUESTRA HISTORIA
       ===================================================== */

    function configurarMuestrarioHistoria(
        imagenes
    ) {

        if (
            !historiaImagenPrincipal
            || !historiaMiniaturas
        ) {
            return;
        }


        detenerRotacionHistoria();


        historiaImagenes =
            Array.isArray(imagenes)
                ? imagenes.filter(
                    item => {

                        return (
                            item
                            && typeof item.imagen === 'string'
                            && item.imagen.trim() !== ''
                        );

                    }
                )
                : [];


        historiaIndiceActual =
            0;


        historiaMiniaturas.innerHTML =
            '';


        /* Sin imágenes */

        if (
            historiaImagenes.length === 0
        ) {

            historiaImagenPrincipal.classList.remove(
                'is-loaded'
            );


            historiaImagenPrincipal.removeAttribute(
                'src'
            );


            historiaImagenPrincipal.alt =
                'Nuestra historia';


            return;

        }


        /* Crear miniaturas */

        historiaImagenes.forEach(
            (item, indice) => {

                const boton =
                    document.createElement(
                        'button'
                    );


                boton.type =
                    'button';


                boton.className =
                    'history-thumbnail';


                boton.setAttribute(
                    'aria-label',
                    `Mostrar imagen ${indice + 1} de nuestra historia`
                );


                boton.setAttribute(
                    'aria-pressed',
                    'false'
                );


                /* Fallback de miniatura */

                const fallback =
                    document.createElement(
                        'span'
                    );


                fallback.className =
                    'history-thumbnail__fallback';


                fallback.textContent =
                    indice + 1;


                boton.appendChild(
                    fallback
                );


                /* Imagen de miniatura */

                const imagen =
                    document.createElement(
                        'img'
                    );


                imagen.className =
                    'history-thumbnail__image';


                imagen.alt =
                    item.descripcion
                    || `Nuestra historia - Foto ${indice + 1}`;


                imagen.loading =
                    'lazy';


                configurarImagen(
                    imagen
                );


                imagen.src =
                    item.imagen.trim();


                boton.appendChild(
                    imagen
                );


                /* Click manual */

                boton.addEventListener(
                    'click',
                    () => {

                        mostrarImagenHistoria(
                            indice
                        );


                        reiniciarRotacionHistoria();

                    }
                );


                historiaMiniaturas.appendChild(
                    boton
                );

            }
        );


        /* Primera imagen */

        mostrarImagenHistoria(
            0
        );


        /* Iniciar rotación automática */

        iniciarRotacionHistoria();

    }


    function mostrarImagenHistoria(
        indice
    ) {

        if (
            historiaImagenes.length === 0
            || !historiaImagenPrincipal
        ) {
            return;
        }


        if (
            indice < 0
            || indice >= historiaImagenes.length
        ) {
            return;
        }


        historiaIndiceActual =
            indice;


        const item =
            historiaImagenes[
                historiaIndiceActual
            ];


        historiaImagenPrincipal.classList.remove(
            'is-loaded'
        );


        historiaImagenPrincipal.alt =
            item.descripcion
            || `Nuestra historia - Foto ${historiaIndiceActual + 1}`;


        /*
         * Primero configuramos los eventos
         * y después asignamos la imagen.
         */

        historiaImagenPrincipal.onload =
            () => {

                historiaImagenPrincipal.classList.add(
                    'is-loaded'
                );

            };


        historiaImagenPrincipal.onerror =
            () => {

                historiaImagenPrincipal.classList.remove(
                    'is-loaded'
                );

            };


        historiaImagenPrincipal.src =
            item.imagen.trim();


        /*
         * Por si el navegador ya tenía
         * la imagen guardada en caché.
         */

        if (
            historiaImagenPrincipal.complete
            && historiaImagenPrincipal.naturalWidth > 0
        ) {

            historiaImagenPrincipal.classList.add(
                'is-loaded'
            );

        }


        actualizarMiniaturasHistoria();

    }


    function actualizarMiniaturasHistoria() {

        if (!historiaMiniaturas) {
            return;
        }


        const botones =
            historiaMiniaturas.querySelectorAll(
                '.history-thumbnail'
            );


        botones.forEach(
            (boton, indice) => {

                const activo =
                    indice === historiaIndiceActual;


                boton.classList.toggle(
                    'is-active',
                    activo
                );


                boton.setAttribute(
                    'aria-pressed',
                    activo
                        ? 'true'
                        : 'false'
                );

            }
        );

    }


    function iniciarRotacionHistoria() {

        detenerRotacionHistoria();


        if (
            historiaImagenes.length <= 1
        ) {
            return;
        }


        historiaIntervalo =
            window.setInterval(
                () => {

                    const siguiente =
                        (
                            historiaIndiceActual
                            + 1
                        )
                        % historiaImagenes.length;


                    mostrarImagenHistoria(
                        siguiente
                    );

                },
                HISTORIA_TIEMPO_ROTACION
            );

    }


    function detenerRotacionHistoria() {

        if (
            historiaIntervalo === null
        ) {
            return;
        }


        window.clearInterval(
            historiaIntervalo
        );


        historiaIntervalo =
            null;

    }


    function reiniciarRotacionHistoria() {

        detenerRotacionHistoria();


        iniciarRotacionHistoria();

    }


    function configurarInteraccionHistoria() {

        if (!historiaMuestrario) {
            return;
        }


        /*
         * Si el usuario pasa el mouse por
         * las imágenes, pausamos el cambio.
         */

        historiaMuestrario.addEventListener(
            'mouseenter',
            detenerRotacionHistoria
        );


        historiaMuestrario.addEventListener(
            'mouseleave',
            iniciarRotacionHistoria
        );


        /*
         * También pausamos si el usuario
         * navega por las miniaturas usando teclado.
         */

        historiaMuestrario.addEventListener(
            'focusin',
            detenerRotacionHistoria
        );


        historiaMuestrario.addEventListener(
            'focusout',
            () => {

                window.setTimeout(
                    () => {

                        if (
                            !historiaMuestrario.contains(
                                document.activeElement
                            )
                        ) {

                            iniciarRotacionHistoria();

                        }

                    },
                    0
                );

            }
        );


        /*
         * Si el usuario cambia de pestaña,
         * detenemos temporalmente la rotación.
         */

        document.addEventListener(
            'visibilitychange',
            () => {

                if (
                    document.hidden
                ) {

                    detenerRotacionHistoria();

                } else {

                    iniciarRotacionHistoria();

                }

            }
        );

    }


    /* =====================================================
       EQUIPO
       ===================================================== */

    function renderizarEquipo(
        integrantes
    ) {

        const contenedor =
            document.getElementById(
                'equipo-lista'
            );


        if (!contenedor) {
            return;
        }


        contenedor.innerHTML = '';


        if (
            !Array.isArray(integrantes)
            || integrantes.length === 0
        ) {
            return;
        }


        integrantes.forEach(
            integrante => {

                const tarjeta =
                    document.createElement(
                        'article'
                    );


                tarjeta.className =
                    'team-card';


                const imagenContenedor =
                    document.createElement(
                        'div'
                    );


                imagenContenedor.className =
                    'team-card__image';


                const fallback =
                    document.createElement(
                        'div'
                    );


                fallback.className =
                    'team-card__fallback';


                fallback.textContent =
                    obtenerInicial(
                        integrante.nombre
                    );


                imagenContenedor.appendChild(
                    fallback
                );


                if (
                    typeof integrante.imagen === 'string'
                    && integrante.imagen.trim() !== ''
                ) {

                    const imagen =
                        document.createElement(
                            'img'
                        );


                    imagen.className =
                        'team-card__img';


                    imagen.src =
                        integrante.imagen.trim();


                    imagen.alt =
                        integrante.nombre
                            ? `Foto de ${integrante.nombre}`
                            : 'Integrante del equipo';


                    imagen.loading =
                        'lazy';


                    configurarImagen(
                        imagen
                    );


                    imagenContenedor.appendChild(
                        imagen
                    );

                }


                const contenido =
                    document.createElement(
                        'div'
                    );


                contenido.className =
                    'team-card__content';


                const nombre =
                    document.createElement(
                        'h3'
                    );


                nombre.className =
                    'team-card__name';


                nombre.textContent =
                    integrante.nombre
                    || 'Integrante';


                const rol =
                    document.createElement(
                        'p'
                    );


                rol.className =
                    'team-card__role';


                rol.textContent =
                    integrante.rol
                    || 'Equipo';


                contenido.appendChild(
                    nombre
                );


                contenido.appendChild(
                    rol
                );


                tarjeta.appendChild(
                    imagenContenedor
                );


                tarjeta.appendChild(
                    contenido
                );


                contenedor.appendChild(
                    tarjeta
                );

            }
        );

    }


    function obtenerInicial(
        nombre
    ) {

        if (
            typeof nombre !== 'string'
            || nombre.trim() === ''
        ) {
            return '?';
        }


        return nombre
            .trim()
            .charAt(0)
            .toUpperCase();

    }


    /* =====================================================
       GALERÍA
       ===================================================== */

    function renderizarGaleria(
        imagenes
    ) {

        const contenedor =
            document.getElementById(
                'galeria-comercio'
            );


        if (!contenedor) {
            return;
        }


        contenedor.innerHTML = '';


        if (
            !Array.isArray(imagenes)
            || imagenes.length === 0
        ) {

            const seccion =
                document.getElementById(
                    'galeria'
                );


            if (seccion) {
                seccion.hidden = true;
            }


            return;

        }


        imagenes.forEach(
            item => {

                const figura =
                    document.createElement(
                        'figure'
                    );


                figura.className =
                    'gallery-item';


                const fallback =
                    document.createElement(
                        'div'
                    );


                fallback.className =
                    'gallery-item__fallback';


                fallback.textContent =
                    item.descripcion
                    || 'Todo Descartables';


                figura.appendChild(
                    fallback
                );


                if (
                    typeof item.imagen === 'string'
                    && item.imagen.trim() !== ''
                ) {

                    const imagen =
                        document.createElement(
                            'img'
                        );


                    imagen.className =
                        'gallery-item__image';


                    imagen.src =
                        item.imagen.trim();


                    imagen.alt =
                        item.descripcion
                        || 'Imagen del comercio';


                    imagen.loading =
                        'lazy';


                    configurarImagen(
                        imagen
                    );


                    figura.appendChild(
                        imagen
                    );

                }


                contenedor.appendChild(
                    figura
                );

            }
        );

    }


    /* =====================================================
       HORARIOS
       ===================================================== */

    function aplicarHorarios(
        horarios
    ) {

        const contenedor =
            document.getElementById(
                'horarios-comercio'
            );


        if (
            !contenedor
            || !Array.isArray(horarios)
        ) {
            return;
        }


        contenedor.innerHTML = '';


        horarios.forEach(
            horario => {

                if (
                    typeof horario !== 'string'
                    || horario.trim() === ''
                ) {
                    return;
                }


                const parrafo =
                    document.createElement(
                        'p'
                    );


                parrafo.textContent =
                    horario.trim();


                contenedor.appendChild(
                    parrafo
                );

            }
        );

    }


    /* =====================================================
       MAPA
       ===================================================== */

    function aplicarMapa(
        ruta
    ) {

        const mapa =
            document.getElementById(
                'mapa-comercio'
            );


        if (
            !mapa
            || typeof ruta !== 'string'
            || ruta.trim() === ''
        ) {
            return;
        }


        mapa.src =
            ruta.trim();

    }


    /* =====================================================
       TELÉFONO
       ===================================================== */

    function aplicarTelefono(
        telefono
    ) {

        if (
            typeof telefono !== 'string'
            || telefono.trim() === ''
        ) {
            return;
        }


        const numero =
            telefono.replace(
                /\D/g,
                ''
            );


        const enlaces = [

            document.getElementById(
                'telefono-link'
            ),

            document.getElementById(
                'footer-telefono-link'
            )

        ];


        enlaces.forEach(
            enlace => {

                if (!enlace) {
                    return;
                }


                enlace.href =
                    `tel:+54${numero}`;


                if (
                    enlace.id ===
                    'footer-telefono-link'
                ) {

                    enlace.textContent =
                        telefono.trim();

                }

            }
        );

    }


    /* =====================================================
       CORREO
       ===================================================== */

    function aplicarCorreo(
        correo
    ) {

        if (
            typeof correo !== 'string'
            || correo.trim() === ''
        ) {
            return;
        }


        const enlaces = [

            document.getElementById(
                'correo-link'
            ),

            document.getElementById(
                'footer-correo-link'
            )

        ];


        enlaces.forEach(
            enlace => {

                if (!enlace) {
                    return;
                }


                enlace.href =
                    `mailto:${correo.trim()}`;


                if (
                    enlace.id ===
                    'footer-correo-link'
                ) {

                    enlace.textContent =
                        correo.trim();

                }

            }
        );

    }


    /* =====================================================
       WHATSAPP
       ===================================================== */

    function aplicarWhatsapp(
        numero,
        mensaje
    ) {

        if (
            typeof numero !== 'string'
            || numero.trim() === ''
        ) {
            return;
        }


        const numeroLimpio =
            numero.replace(
                /\D/g,
                ''
            );


        let ruta =
            `https://wa.me/${numeroLimpio}`;


        if (
            typeof mensaje === 'string'
            && mensaje.trim() !== ''
        ) {

            ruta +=
                `?text=${encodeURIComponent(
                    mensaje.trim()
                )}`;

        }


        const enlaces = [

            document.getElementById(
                'whatsapp-link'
            ),

            document.getElementById(
                'footer-whatsapp-link'
            )

        ];


        enlaces.forEach(
            enlace => {

                if (enlace) {

                    enlace.href =
                        ruta;

                }

            }
        );

    }


    /* =====================================================
       INSTAGRAM
       ===================================================== */

    function aplicarInstagram(
        ruta
    ) {

        if (
            typeof ruta !== 'string'
            || ruta.trim() === ''
        ) {
            return;
        }


        const enlaces = [

            document.getElementById(
                'instagram-link'
            ),

            document.getElementById(
                'footer-instagram-link'
            )

        ];


        enlaces.forEach(
            enlace => {

                if (enlace) {

                    enlace.href =
                        ruta.trim();

                }

            }
        );

    }


    /* =====================================================
       CATÁLOGO
       ===================================================== */

    function aplicarRutaCatalogo(
        ruta
    ) {

        if (
            typeof ruta !== 'string'
            || ruta.trim() === ''
        ) {
            return;
        }


        const enlaces = [

            document.getElementById(
                'shop-link-header'
            ),

            document.getElementById(
                'shop-link-hero'
            ),

            document.getElementById(
                'shop-link-footer'
            )

        ];


        enlaces.forEach(
            enlace => {

                if (enlace) {

                    enlace.href =
                        ruta.trim();

                }

            }
        );

    }


    /* =====================================================
       SESIÓN / MI CUENTA
       ===================================================== */

    function configurarAccesoCuenta() {

        const enlaces = [
            accountLink,
            footerAccountLink
        ];


        if (
            typeof Auth === 'undefined'
        ) {
            return;
        }


        const token =
            Auth.obtenerToken();

        const rol =
            Auth.obtenerRol();


        let texto =
            'Ingresar';


        let ruta =
            '/login/index.html';


        if (
            token
            && rol
        ) {

            texto =
                'Mi cuenta';


            ruta =
                Auth.obtenerRutaPorRol(
                    rol
                );

        }


        enlaces.forEach(
            enlace => {

                if (!enlace) {
                    return;
                }


                enlace.textContent =
                    texto;


                enlace.href =
                    ruta;

            }
        );

    }


    /* =====================================================
       SECCIÓN ACTIVA
       ===================================================== */

    function configurarSeccionActiva() {

        const enlaces =
            Array.from(
                document.querySelectorAll(
                    '.main-nav__link[data-section]'
                )
            );


        if (
            enlaces.length === 0
        ) {
            return;
        }


        function actualizar() {

            const referencia =
                150;


            let enlaceActivo =
                enlaces[0];


            let menorDistancia =
                Number.POSITIVE_INFINITY;


            enlaces.forEach(
                enlace => {

                    const id =
                        enlace.dataset.section;


                    const seccion =
                        document.getElementById(
                            id
                        );


                    if (!seccion) {
                        return;
                    }


                    const rect =
                        seccion.getBoundingClientRect();


                    if (
                        rect.bottom <= 0
                        || rect.top >= window.innerHeight
                    ) {
                        return;
                    }


                    const distancia =
                        Math.abs(
                            rect.top - referencia
                        );


                    if (
                        distancia < menorDistancia
                    ) {

                        menorDistancia =
                            distancia;


                        enlaceActivo =
                            enlace;

                    }

                }
            );


            enlaces.forEach(
                enlace => {

                    const activo =
                        enlace === enlaceActivo;


                    enlace.classList.toggle(
                        'is-active',
                        activo
                    );


                    if (activo) {

                        enlace.setAttribute(
                            'aria-current',
                            'location'
                        );

                    } else {

                        enlace.removeAttribute(
                            'aria-current'
                        );

                    }

                }
            );

        }


        window.addEventListener(
            'scroll',
            actualizar,
            {
                passive: true
            }
        );


        window.addEventListener(
            'resize',
            actualizar
        );


        actualizar();

    }


    /* =====================================================
       NAVBAR MOBILE
       ===================================================== */

    function abrirMenu() {

        if (
            !mainNav
            || !navToggle
        ) {
            return;
        }


        mainNav.classList.add(
            'is-open'
        );


        navToggle.classList.add(
            'is-active'
        );


        navToggle.setAttribute(
            'aria-expanded',
            'true'
        );


        navToggle.setAttribute(
            'aria-label',
            'Cerrar menú'
        );


        document.body.classList.add(
            'nav-open'
        );

    }


    function cerrarMenu() {

        if (
            !mainNav
            || !navToggle
        ) {
            return;
        }


        mainNav.classList.remove(
            'is-open'
        );


        navToggle.classList.remove(
            'is-active'
        );


        navToggle.setAttribute(
            'aria-expanded',
            'false'
        );


        navToggle.setAttribute(
            'aria-label',
            'Abrir menú'
        );


        document.body.classList.remove(
            'nav-open'
        );

    }


    function alternarMenu() {

        if (!mainNav) {
            return;
        }


        if (
            mainNav.classList.contains(
                'is-open'
            )
        ) {

            cerrarMenu();

        } else {

            abrirMenu();

        }

    }


    function configurarNavbar() {

        if (
            !navToggle
            || !mainNav
        ) {
            return;
        }


        navToggle.addEventListener(
            'click',
            alternarMenu
        );


        mainNav
            .querySelectorAll('a')
            .forEach(
                enlace => {

                    enlace.addEventListener(
                        'click',
                        cerrarMenu
                    );

                }
            );


        document.addEventListener(
            'keydown',
            event => {

                if (
                    event.key === 'Escape'
                ) {

                    cerrarMenu();

                }

            }
        );


        window.addEventListener(
            'resize',
            () => {

                if (
                    window.innerWidth > 900
                ) {

                    cerrarMenu();

                }

            }
        );

    }


    /* =====================================================
       FOOTER
       ===================================================== */

    function cargarAnioActual() {

        const elemento =
            document.getElementById(
                'anio-actual'
            );


        if (!elemento) {
            return;
        }


        elemento.textContent =
            new Date().getFullYear();

    }


    /* =====================================================
       INICIALIZACIÓN
       ===================================================== */

    configurarNavbar();


    configurarAccesoCuenta();


    configurarSeccionActiva();


    configurarInteraccionHistoria();


    [
        logoHeader,
        logoFooter,
        portada,
        imagenNosotros
    ].forEach(
        configurarImagen
    );


    cargarDatosComercio();


    cargarAnioActual();

})();