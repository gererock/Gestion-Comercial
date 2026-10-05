(() => {

    'use strict';


    const navToggle =
        document.getElementById('nav-toggle');

    const mainNav =
        document.getElementById('main-nav');

    const portada =
        document.getElementById('imagen-portada');


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


        aplicarPortada(
            comercio.portada
        );


        aplicarRutaCatalogo(
            comercio.catalogoUrl
        );


        if (
            typeof comercio.nombre === 'string'
            && comercio.nombre.trim() !== ''
        ) {

            document.title =
                `${comercio.nombre.trim()} | Inicio`;

        }

    }


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


    function aplicarPortada(ruta) {

        if (!portada) {
            return;
        }


        if (
            typeof ruta !== 'string'
            || ruta.trim() === ''
        ) {

            portada.remove();

            return;
        }


        portada.src =
            ruta.trim();

    }


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


    function configurarPortada() {

        if (!portada) {
            return;
        }


        portada.addEventListener(
            'load',
            () => {

                portada.classList.add(
                    'is-loaded'
                );

            }
        );


        portada.addEventListener(
            'error',
            () => {

                portada.classList.remove(
                    'is-loaded'
                );

            }
        );

    }


    function abrirMenu() {

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
            .forEach(enlace => {

                enlace.addEventListener(
                    'click',
                    cerrarMenu
                );

            });


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


    configurarNavbar();

    configurarPortada();

    cargarDatosComercio();

    cargarAnioActual();

})();