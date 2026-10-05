(() => {

    'use strict';


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


        if (comercio.nombre) {

            document.title =
                `${comercio.nombre} | Inicio`;

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


    cargarDatosComercio();
    cargarAnioActual();

})();