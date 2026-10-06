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
       CARGAR DATOS DEL COMERCIO
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
       APLICAR DATOS A LA PÁGINA
       ===================================================== */

    function aplicarDatosComercio(comercio) {

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


        /* Descripción principal */

        establecerTexto(
            'descripcion-comercio',
            comercio.descripcion
        );


        /* Sobre nosotros */

        establecerTexto(
            'texto-nosotros',
            comercio.sobreNosotros
        );


        /* Historia */

        establecerTexto(
            'texto-historia',
            comercio.historia
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


        /* Catálogo */

        aplicarRutaCatalogo(
            comercio.catalogoUrl
        );


        /* Título del navegador */

        if (
            typeof comercio.nombre === 'string'
            && comercio.nombre.trim() !== ''
        ) {

            document.title =
                `${comercio.nombre.trim()} | Inicio`;

        }

    }


    /* =====================================================
       COLOCAR TEXTO
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


        /*
         * Si la imagen ya estaba cargada
         * antes de registrar el evento.
         */

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
       CATÁLOGO
       ===================================================== */

    function aplicarRutaCatalogo(ruta) {

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


        enlaces.forEach(enlace => {

            if (enlace) {

                enlace.href =
                    ruta.trim();

            }

        });

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


        /* Abrir / cerrar menú */

        navToggle.addEventListener(
            'click',
            alternarMenu
        );


        /* Cerrar al seleccionar una opción */

        mainNav
            .querySelectorAll('a')
            .forEach(enlace => {

                enlace.addEventListener(
                    'click',
                    cerrarMenu
                );

            });


        /* Cerrar con Escape */

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


        /* Cerrar si vuelve a escritorio */

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
       AÑO DEL FOOTER
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