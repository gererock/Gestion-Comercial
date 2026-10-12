(() => {

    'use strict';


    const API_CATALOGO =
        '/api/public/catalogo';


    const DATA_COMERCIO =
        '/data/comercio.json';


    const grid =
        document.getElementById(
            'catalog-grid'
        );


    const resultCount =
        document.getElementById(
            'catalog-result-count'
        );


    const emptyState =
        document.getElementById(
            'catalog-empty'
        );


    const errorState =
        document.getElementById(
            'catalog-error'
        );


    const retryButton =
        document.getElementById(
            'catalog-retry'
        );


    const catalogLogo =
        document.getElementById(
            'catalog-logo'
        );


    const brandName =
        document.getElementById(
            'catalog-brand-name'
        );


    const footerName =
        document.getElementById(
            'catalog-footer-name'
        );


    const accountLink =
        document.getElementById(
            'catalog-account-link'
        );


    const footerAccount =
        document.getElementById(
            'catalog-footer-account'
        );


    iniciar();


    async function iniciar() {

        configurarCuenta();

        configurarLogo();

        configurarEventos();


        cargarDatosComercio();


        await cargarCatalogo();

    }


    function configurarEventos() {

        if (
            retryButton
        ) {

            retryButton.addEventListener(
                'click',
                cargarCatalogo
            );
        }

    }


    // =========================================
    // CATÁLOGO
    // =========================================

    async function cargarCatalogo() {

        ocultarEstados();


        if (
            resultCount
        ) {

            resultCount.textContent =
                'Cargando productos...';
        }


        try {

            const response =
                await fetch(
                    API_CATALOGO,
                    {
                        headers: {
                            'Accept':
                                'application/json'
                        }
                    }
                );


            if (
                !response.ok
            ) {

                throw new Error(
                    'No se pudo cargar el catálogo.'
                );
            }


            const items =
                await response.json();


            if (
                !Array.isArray(
                    items
                )
            ) {

                throw new Error(
                    'La respuesta del catálogo no es válida.'
                );
            }


            renderizarCatalogo(
                items
            );


        } catch (error) {

            console.error(
                'Error al cargar catálogo:',
                error
            );


            mostrarError();

        }

    }


    // =========================================
    // RENDER
    // =========================================

    function renderizarCatalogo(
        items
    ) {

        grid.innerHTML =
            '';


        if (
            items.length === 0
        ) {

            resultCount.textContent =
                '0 productos disponibles';


            emptyState.classList.remove(
                'hidden'
            );


            return;
        }


        resultCount.textContent =
            crearTextoCantidad(
                items.length
            );


        items.forEach(
            item => {

                const card =
                    crearCard(
                        item
                    );


                grid.appendChild(
                    card
                );

            }
        );

    }


    function crearTextoCantidad(
        cantidad
    ) {

        if (
            cantidad === 1
        ) {

            return '1 producto disponible';
        }


        return `${cantidad} productos disponibles`;

    }


    // =========================================
    // CARD
    // =========================================

    function crearCard(
        item
    ) {

        const card =
            document.createElement(
                'article'
            );


        card.className =
            'product-card';


        const imageContainer =
            document.createElement(
                'div'
            );


        imageContainer.className =
            'product-card__image';


        const fallback =
            crearFallbackImagen();


        imageContainer.appendChild(
            fallback
        );


        const badges =
            document.createElement(
                'div'
            );


        badges.className =
            'product-card__badges';


        if (
            !item.disponible
        ) {

            badges.appendChild(
                crearBadge(
                    'Sin stock',
                    'product-card__badge--out'
                )
            );
        }


        if (
            item.tipo === 'GRUPO'
        ) {

            badges.appendChild(
                crearBadge(
                    'Opciones',
                    'product-card__badge--group'
                )
            );
        }


        imageContainer.appendChild(
            badges
        );


        if (
            typeof item.imagenUrl === 'string'
            &&
            item.imagenUrl.trim() !== ''
        ) {

            imageContainer.appendChild(
                crearImagenProducto(
                    item.imagenUrl,
                    item.nombre
                )
            );
        }


        const info =
            document.createElement(
                'div'
            );


        info.className =
            'product-card__info';


        const type =
            document.createElement(
                'p'
            );


        type.className =
            'product-card__type';


        if (
            item.tipo === 'GRUPO'
        ) {

            type.textContent =
                textoOpciones(
                    item.cantidadOpciones
                );


        } else {

            type.textContent =
                'Producto';
        }


        const name =
            document.createElement(
                'h2'
            );


        name.className =
            'product-card__name';


        name.textContent =
            item.nombre
            || 'Producto';


        const price =
            document.createElement(
                'p'
            );


        price.className =
            'product-card__price';


        price.textContent =
            crearTextoPrecio(
                item
            );


        const availability =
            document.createElement(
                'p'
            );


        availability.className =
            'product-card__availability';


        if (
            item.disponible
        ) {

            availability.textContent =
                'Disponible';


        } else {

            availability.textContent =
                'Sin stock';


            availability.classList.add(
                'product-card__availability--out'
            );
        }


        info.appendChild(
            type
        );


        info.appendChild(
            name
        );


        info.appendChild(
            price
        );


        info.appendChild(
            availability
        );


        card.appendChild(
            imageContainer
        );


        card.appendChild(
            info
        );


        return card;

    }


    // =========================================
    // IMAGEN
    // =========================================

    function crearFallbackImagen() {

        const fallback =
            document.createElement(
                'div'
            );


        fallback.className =
            'product-card__fallback';


        const mark =
            document.createElement(
                'span'
            );


        mark.className =
            'product-card__fallback-mark';


        mark.textContent =
            'TD';


        const text =
            document.createElement(
                'span'
            );


        text.className =
            'product-card__fallback-text';


        text.textContent =
            'Sin imagen';


        fallback.appendChild(
            mark
        );


        fallback.appendChild(
            text
        );


        return fallback;

    }


    function crearImagenProducto(
        ruta,
        nombre
    ) {

        const img =
            document.createElement(
                'img'
            );


        img.className =
            'product-card__img';


        img.alt =
            nombre
            ? `Imagen de ${nombre}`
            : 'Imagen del producto';


        img.loading =
            'lazy';


        img.addEventListener(
            'load',
            () => {

                img.classList.add(
                    'is-loaded'
                );
            }
        );


        img.addEventListener(
            'error',
            () => {

                img.classList.remove(
                    'is-loaded'
                );
            }
        );


        img.src =
            ruta.trim();


        if (
            img.complete
            &&
            img.naturalWidth > 0
        ) {

            img.classList.add(
                'is-loaded'
            );
        }


        return img;

    }


    // =========================================
    // BADGE
    // =========================================

    function crearBadge(
        texto,
        clase
    ) {

        const badge =
            document.createElement(
                'span'
            );


        badge.className =
            `product-card__badge ${clase}`;


        badge.textContent =
            texto;


        return badge;

    }


    // =========================================
    // PRECIO
    // =========================================

    function crearTextoPrecio(
        item
    ) {

        const precio =
            formatearPrecio(
                item.precio
            );


        if (
            item.precioDesde
        ) {

            return `Desde ${precio}`;
        }


        return precio;

    }


    function formatearPrecio(
        valor
    ) {

        const numero =
            Number(
                valor
            );


        if (
            !Number.isFinite(
                numero
            )
        ) {

            return 'Precio no disponible';
        }


        return new Intl.NumberFormat(
            'es-AR',
            {
                style:
                    'currency',

                currency:
                    'ARS',

                minimumFractionDigits:
                    0,

                maximumFractionDigits:
                    2
            }
        ).format(
            numero
        );

    }


    // =========================================
    // OPCIONES
    // =========================================

    function textoOpciones(
        cantidad
    ) {

        const numero =
            Number(
                cantidad
            );


        if (
            numero === 1
        ) {

            return '1 opción';
        }


        if (
            Number.isFinite(
                numero
            )
            &&
            numero > 1
        ) {

            return `${numero} opciones`;
        }


        return 'Opciones disponibles';

    }


    // =========================================
    // ESTADOS
    // =========================================

    function ocultarEstados() {

        errorState.classList.add(
            'hidden'
        );


        emptyState.classList.add(
            'hidden'
        );

    }


    function mostrarError() {

        grid.innerHTML =
            '';


        resultCount.textContent =
            'No se pudo cargar el catálogo';


        errorState.classList.remove(
            'hidden'
        );

    }


    // =========================================
    // DATOS DEL COMERCIO
    // =========================================

    async function cargarDatosComercio() {

        try {

            const response =
                await fetch(
                    DATA_COMERCIO
                );


            if (
                !response.ok
            ) {

                return;
            }


            const comercio =
                await response.json();


            aplicarDatosComercio(
                comercio
            );


        } catch (error) {

            console.error(
                'No se pudieron cargar los datos del comercio:',
                error
            );
        }

    }


    function aplicarDatosComercio(
        comercio
    ) {

        if (
            typeof comercio.nombre === 'string'
            &&
            comercio.nombre.trim() !== ''
        ) {

            const nombre =
                comercio.nombre.trim();


            brandName.textContent =
                nombre;


            footerName.textContent =
                nombre;


            document.title =
                `Catálogo | ${nombre}`;
        }


        if (
            typeof comercio.logo === 'string'
            &&
            comercio.logo.trim() !== ''
        ) {

            catalogLogo.src =
                comercio.logo.trim();
        }

    }


    // =========================================
    // LOGO
    // =========================================

    function configurarLogo() {

        if (
            !catalogLogo
        ) {

            return;
        }


        catalogLogo.addEventListener(
            'load',
            () => {

                catalogLogo.classList.add(
                    'is-loaded'
                );
            }
        );


        catalogLogo.addEventListener(
            'error',
            () => {

                catalogLogo.classList.remove(
                    'is-loaded'
                );
            }
        );


        if (
            catalogLogo.complete
            &&
            catalogLogo.naturalWidth > 0
        ) {

            catalogLogo.classList.add(
                'is-loaded'
            );
        }

    }


    // =========================================
    // CUENTA
    // =========================================

    function configurarCuenta() {

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
            &&
            rol
        ) {

            texto =
                'Mi cuenta';


            ruta =
                Auth.obtenerRutaPorRol(
                    rol
                );
        }


        [
            accountLink,
            footerAccount
        ]
            .filter(Boolean)
            .forEach(
                enlace => {

                    enlace.textContent =
                        texto;


                    enlace.href =
                        ruta;
                }
            );

    }

})();