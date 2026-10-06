(() => {

    'use strict';


    /* =====================================================
       ELEMENTOS GENERALES
       ===================================================== */

    const navToggle =
        document.getElementById('nav-toggle');

    const mainNav =
        document.getElementById('main-nav');


    /* =====================================================
       IMÁGENES
       ===================================================== */

    const portada =
        document.getElementById('imagen-portada');

    const imagenNosotros =
        document.getElementById('imagen-nosotros');

    const imagenHistoria =
        document.getElementById('imagen-historia');


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


            aplicarDatosComercio(comercio);


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

    function aplicarDatosComercio(comercio) {

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
            'descripcion-comercio',
            comercio.descripcion
        );


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
            comercio.whatsapp
        );


        aplicarInstagram(
            comercio.instagram
        );


        /* Imágenes */

        aplicarImagen(
            portada,
            comercio.portada
        );


        aplicarImagen(
            imagenNosotros,
            comercio.imagenNosotros
        );


        aplicarImagen(
            imagenHistoria,
            comercio.imagenHistoria
        );


        /* Equipo */

        renderizarEquipo(
            comercio.equipo
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


                const inicial =
                    obtenerInicial(
                        integrante.nombre
                    );


                fallback.textContent =
                    inicial;


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


    function obtenerInicial(nombre) {

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

        const enlace =
            document.getElementById(
                'telefono-link'
            );


        if (
            !enlace
            || typeof telefono !== 'string'
            || telefono.trim() === ''
        ) {
            return;
        }


        const numero =
            telefono.replace(
                /\D/g,
                ''
            );


        enlace.href =
            `tel:+54${numero}`;

    }


    /* =====================================================
       CORREO
       ===================================================== */

    function aplicarCorreo(
        correo
    ) {

        const enlace =
            document.getElementById(
                'correo-link'
            );


        if (
            !enlace
            || typeof correo !== 'string'
            || correo.trim() === ''
        ) {
            return;
        }


        enlace.href =
            `mailto:${correo.trim()}`;

    }


    /* =====================================================
       WHATSAPP
       ===================================================== */

    function aplicarWhatsapp(
        numero
    ) {

        const enlace =
            document.getElementById(
                'whatsapp-link'
            );


        if (
            !enlace
            || typeof numero !== 'string'
            || numero.trim() === ''
        ) {
            return;
        }


        const numeroLimpio =
            numero.replace(
                /\D/g,
                ''
            );


        enlace.href =
            `https://wa.me/${numeroLimpio}`;

    }


    /* =====================================================
       INSTAGRAM
       ===================================================== */

    function aplicarInstagram(
        ruta
    ) {

        const enlace =
            document.getElementById(
                'instagram-link'
            );


        if (
            !enlace
            || typeof ruta !== 'string'
            || ruta.trim() === ''
        ) {
            return;
        }


        enlace.href =
            ruta.trim();

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


        const abierto =
            mainNav.classList.contains(
                'is-open'
            );


        if (abierto) {

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


    configurarImagen(
        portada
    );


    configurarImagen(
        imagenNosotros
    );


    configurarImagen(
        imagenHistoria
    );


    cargarDatosComercio();


    cargarAnioActual();

})();