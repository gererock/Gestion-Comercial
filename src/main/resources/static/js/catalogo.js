(() => {

    'use strict';


    const API_CATALOGO =
        '/api/public/catalogo';


    const API_FILTROS =
        '/api/public/catalogo/filtros';


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


    const noResultsState =
        document.getElementById(
            'catalog-no-results'
        );


    const errorState =
        document.getElementById(
            'catalog-error'
        );


    const retryButton =
        document.getElementById(
            'catalog-retry'
        );


    const filterForm =
        document.getElementById(
            'catalog-filter-form'
        );


    const searchInput =
        document.getElementById(
            'catalog-search'
        );


    const categorySelect =
        document.getElementById(
            'catalog-category'
        );


    const brandSelect =
        document.getElementById(
            'catalog-brand'
        );


    const categoryWrapper =
        document.getElementById(
            'catalog-category-wrapper'
        );


    const brandWrapper =
        document.getElementById(
            'catalog-brand-wrapper'
        );


    const clearFiltersButton =
        document.getElementById(
            'catalog-clear-filters'
        );


    const clearResultsButton =
        document.getElementById(
            'catalog-clear-results'
        );


    const filterError =
        document.getElementById(
            'catalog-filter-error'
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


        await cargarFiltros();

        await cargarCatalogo();

    }


    // =========================================
    // EVENTOS
    // =========================================

    function configurarEventos() {

        if (
            retryButton
        ) {

            retryButton.addEventListener(
                'click',
                cargarCatalogo
            );
        }


        if (
            filterForm
        ) {

            filterForm.addEventListener(
                'submit',
                event => {

                    event.preventDefault();

                    ejecutarBusqueda();
                }
            );
        }


        if (
            categorySelect
        ) {

            categorySelect.addEventListener(
                'change',
                ejecutarBusqueda
            );
        }


        if (
            brandSelect
        ) {

            brandSelect.addEventListener(
                'change',
                ejecutarBusqueda
            );
        }


        if (
            clearFiltersButton
        ) {

            clearFiltersButton.addEventListener(
                'click',
                limpiarFiltros
            );
        }


        if (
            clearResultsButton
        ) {

            clearResultsButton.addEventListener(
                'click',
                limpiarFiltros
            );
        }


        if (
            searchInput
        ) {

            searchInput.addEventListener(
                'input',
                () => {

                    ocultarErrorFiltro();


                    if (
                        searchInput.value.trim() === ''
                    ) {

                        searchInput.value =
                            '';
                    }
                }
            );
        }

    }


    // =========================================
    // FILTROS
    // =========================================

    async function cargarFiltros() {

        try {

            const response =
                await fetch(
                    API_FILTROS,
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
                    'No se pudieron cargar los filtros.'
                );
            }


            const filtros =
                await response.json();


            cargarOpcionesSelect(
                categorySelect,
                filtros.categorias,
                'Todas las categorías'
            );


            cargarOpcionesSelect(
                brandSelect,
                filtros.marcas,
                'Todas las marcas'
            );


            actualizarVisibilidadFiltro(
                categoryWrapper,
                filtros.categorias
            );


            actualizarVisibilidadFiltro(
                brandWrapper,
                filtros.marcas
            );


        } catch (error) {

            console.error(
                'Error al cargar filtros:',
                error
            );


            if (
                categoryWrapper
            ) {

                categoryWrapper.hidden =
                    true;
            }


            if (
                brandWrapper
            ) {

                brandWrapper.hidden =
                    true;
            }
        }

    }


    function cargarOpcionesSelect(
        select,
        opciones,
        textoInicial
    ) {

        if (
            !select
        ) {

            return;
        }


        select.innerHTML =
            '';


        const optionInicial =
            document.createElement(
                'option'
            );


        optionInicial.value =
            '';


        optionInicial.textContent =
            textoInicial;


        select.appendChild(
            optionInicial
        );


        if (
            !Array.isArray(
                opciones
            )
        ) {

            return;
        }


        opciones.forEach(
            opcion => {

                const option =
                    document.createElement(
                        'option'
                    );


                option.value =
                    opcion.id;


                option.textContent =
                    opcion.nombre;


                select.appendChild(
                    option
                );
            }
        );

    }


    function actualizarVisibilidadFiltro(
        wrapper,
        opciones
    ) {

        if (
            !wrapper
        ) {

            return;
        }


        wrapper.hidden =
            !Array.isArray(
                opciones
            )
            ||
            opciones.length === 0;

    }


    // =========================================
    // BÚSQUEDA
    // =========================================

    async function ejecutarBusqueda() {

        ocultarErrorFiltro();


        const nombre =
            searchInput
                ? searchInput.value.trim()
                : '';


        if (
            !validarBusqueda(
                nombre
            )
        ) {

            return;
        }


        if (
            searchInput
        ) {

            searchInput.value =
                nombre;
        }


        await cargarCatalogo();

    }


    function validarBusqueda(
        nombre
    ) {

        if (
            nombre.length > 120
        ) {

            mostrarErrorFiltro(
                'La búsqueda no puede superar los 120 caracteres.'
            );


            return false;
        }


        if (
            nombre !== ''
            &&
            !/[\p{L}\p{N}]/u.test(
                nombre
            )
        ) {

            mostrarErrorFiltro(
                'La búsqueda debe contener al menos una letra o un número.'
            );


            return false;
        }


        return true;

    }


    // =========================================
    // CATÁLOGO
    // =========================================

    async function cargarCatalogo() {

        ocultarEstados();

        ocultarErrorFiltro();


        if (
            resultCount
        ) {

            resultCount.textContent =
                'Cargando productos...';
        }


        try {

            const response =
                await fetch(
                    construirUrlCatalogo(),
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


    function construirUrlCatalogo() {

        const parametros =
            new URLSearchParams();


        const nombre =
            searchInput
                ? searchInput.value.trim()
                : '';


        const idCategoria =
            categorySelect
                ? categorySelect.value
                : '';


        const idMarca =
            brandSelect
                ? brandSelect.value
                : '';


        if (
            nombre !== ''
        ) {

            parametros.set(
                'nombre',
                nombre
            );
        }


        if (
            idCategoria !== ''
        ) {

            parametros.set(
                'idCategoria',
                idCategoria
            );
        }


        if (
            idMarca !== ''
        ) {

            parametros.set(
                'idMarca',
                idMarca
            );
        }


        const query =
            parametros.toString();


        return query
            ? `${API_CATALOGO}?${query}`
            : API_CATALOGO;

    }


    // =========================================
    // LIMPIAR FILTROS
    // =========================================

    async function limpiarFiltros() {

        if (
            searchInput
        ) {

            searchInput.value =
                '';
        }


        if (
            categorySelect
        ) {

            categorySelect.value =
                '';
        }


        if (
            brandSelect
        ) {

            brandSelect.value =
                '';
        }


        ocultarErrorFiltro();


        await cargarCatalogo();

    }


    // =========================================
    // RENDER CATÁLOGO
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
                '0 productos encontrados';


            if (
                hayFiltrosActivos()
            ) {

                noResultsState.classList.remove(
                    'hidden'
                );


            } else {

                emptyState.classList.remove(
                    'hidden'
                );
            }


            return;
        }


        resultCount.textContent =
            crearTextoCantidad(
                items.length
            );


        items.forEach(
            item => {

                grid.appendChild(
                    crearCard(
                        item
                    )
                );
            }
        );

    }


    function hayFiltrosActivos() {

        const nombre =
            searchInput
                ? searchInput.value.trim()
                : '';


        const categoria =
            categorySelect
                ? categorySelect.value
                : '';


        const marca =
            brandSelect
                ? brandSelect.value
                : '';


        return nombre !== ''
                ||
                categoria !== ''
                ||
                marca !== '';

    }


    function crearTextoCantidad(
        cantidad
    ) {

        if (
            hayFiltrosActivos()
        ) {

            return cantidad === 1
                ? '1 producto encontrado'
                : `${cantidad} productos encontrados`;
        }


        return cantidad === 1
            ? '1 producto disponible'
            : `${cantidad} productos disponibles`;

    }


    // =========================================
    // CARD - US02 / US04 / US05
    // =========================================

    function crearCard(
        item
    ) {

        const esProducto =
            item.tipo === 'PRODUCTO'
            &&
            item.idProducto != null;


        const esGrupo =
            item.tipo === 'GRUPO'
            &&
            item.idGrupoCatalogo != null;


        const esNavegable =
            esProducto
            ||
            esGrupo;


        const card =
            document.createElement(
                esNavegable
                    ? 'a'
                    : 'article'
            );


        card.className =
            'product-card';


        if (
            esNavegable
        ) {

            card.classList.add(
                'product-card--link'
            );
        }


        if (
            esProducto
        ) {

            card.href =
                `/catalogo/producto.html?id=${
                    encodeURIComponent(
                        item.idProducto
                    )
                }`;


            card.setAttribute(
                'aria-label',
                `Ver detalle de ${
                    item.nombre
                    ||
                    'producto'
                }`
            );
        }


        if (
            esGrupo
        ) {

            card.href =
                `/catalogo/producto.html?grupo=${
                    encodeURIComponent(
                        item.idGrupoCatalogo
                    )
                }`;


            card.setAttribute(
                'aria-label',
                `Ver opciones de ${
                    item.nombre
                    ||
                    'grupo'
                }`
            );
        }


        const imageContainer =
            document.createElement(
                'div'
            );


        imageContainer.className =
            'product-card__image';


        imageContainer.appendChild(
            crearFallbackImagen()
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


        type.textContent =
            item.tipo === 'GRUPO'
                ? textoOpciones(
                    item.cantidadOpciones
                )
                : 'Producto';


        const productName =
            document.createElement(
                'h2'
            );


        productName.className =
            'product-card__name';


        productName.textContent =
            item.nombre
            ||
            'Producto';


        const productPrice =
            document.createElement(
                'p'
            );


        productPrice.className =
            'product-card__price';


        productPrice.textContent =
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
            productName
        );


        info.appendChild(
            productPrice
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
    // PRECIOS
    // =========================================

    function crearTextoPrecio(
        item
    ) {

        const precio =
            formatearPrecio(
                item.precio
            );


        return item.precioDesde
            ? `Desde ${precio}`
            : precio;

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

        if (
            errorState
        ) {

            errorState.classList.add(
                'hidden'
            );
        }


        if (
            emptyState
        ) {

            emptyState.classList.add(
                'hidden'
            );
        }


        if (
            noResultsState
        ) {

            noResultsState.classList.add(
                'hidden'
            );
        }

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


    function mostrarErrorFiltro(
        mensaje
    ) {

        if (
            !filterError
        ) {

            return;
        }


        filterError.textContent =
            mensaje;


        filterError.classList.remove(
            'hidden'
        );

    }


    function ocultarErrorFiltro() {

        if (
            !filterError
        ) {

            return;
        }


        filterError.textContent =
            '';


        filterError.classList.add(
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
            catalogLogo
            &&
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