(() => {

    'use strict';


    const API_PRODUCTOS =
        '/api/public/catalogo/productos';


    const API_GRUPOS =
        '/api/public/catalogo/grupos';


    const loading =
        document.getElementById(
            'detail-loading'
        );


    const errorState =
        document.getElementById(
            'detail-error'
        );


    const content =
        document.getElementById(
            'detail-content'
        );


    const name =
        document.getElementById(
            'detail-name'
        );


    const description =
        document.getElementById(
            'detail-description'
        );


    const price =
        document.getElementById(
            'detail-price'
        );


    const originalPrice =
        document.getElementById(
            'detail-price-original'
        );


    const offerBadge =
        document.getElementById(
            'detail-offer-badge'
        );


    const availability =
        document.getElementById(
            'detail-availability'
        );


    const wholesale =
        document.getElementById(
            'detail-wholesale'
        );


    const wholesaleText =
        document.getElementById(
            'detail-wholesale-text'
        );


    const group =
        document.getElementById(
            'detail-group'
        );


    const groupName =
        document.getElementById(
            'detail-group-name'
        );


    const optionsSection =
        document.getElementById(
            'detail-options-section'
        );


    const optionsGroupName =
        document.getElementById(
            'detail-options-group-name'
        );


    const optionsContainer =
        document.getElementById(
            'detail-options'
        );


    const meta =
        document.getElementById(
            'detail-meta'
        );


    const mainImage =
        document.getElementById(
            'detail-main-image'
        );


    const thumbnails =
        document.getElementById(
            'detail-thumbnails'
        );


    const stockNote =
        document.getElementById(
            'detail-stock-note'
        );


    const accountLink =
        document.getElementById(
            'detail-account-link'
        );


    iniciar();


    // =========================================
    // INICIO
    // =========================================

    async function iniciar() {

        configurarCuenta();


        const parametros =
            obtenerParametros();


        if (
            parametros.idProducto
        ) {

            const producto =
                await cargarProducto(
                    parametros.idProducto
                );


            if (
                producto
                &&
                producto.idGrupoCatalogo
            ) {

                await cargarOpcionesGrupo(
                    producto.idGrupoCatalogo,
                    producto.idProducto
                );
            }


            return;
        }


        if (
            parametros.idGrupoCatalogo
        ) {

            await cargarGrupoInicial(
                parametros.idGrupoCatalogo
            );


            return;
        }


        mostrarError();

    }


    function obtenerParametros() {

        const parametros =
            new URLSearchParams(
                window.location.search
            );


        return {

            idProducto:
                convertirId(
                    parametros.get(
                        'id'
                    )
                ),

            idGrupoCatalogo:
                convertirId(
                    parametros.get(
                        'grupo'
                    )
                )
        };

    }


    function convertirId(
        valor
    ) {

        const numero =
            Number(
                valor
            );


        if (
            !Number.isInteger(
                numero
            )
            ||
            numero <= 0
        ) {

            return null;
        }


        return numero;

    }


    // =========================================
    // INICIO DESDE GRUPO
    // =========================================

    async function cargarGrupoInicial(
        idGrupoCatalogo
    ) {

        const opciones =
            await obtenerOpcionesGrupo(
                idGrupoCatalogo
            );


        if (
            opciones === null
            ||
            opciones.length === 0
        ) {

            mostrarError();

            return;
        }


        const opcionInicial =
            opciones.find(
                opcion =>
                    opcion.disponible
            )
            ||
            opciones[0];


        renderizarOpciones(
            opciones,
            opcionInicial.idProducto
        );


        const producto =
            await cargarProducto(
                opcionInicial.idProducto
            );


        if (
            !producto
        ) {

            return;
        }


        actualizarUrl(
            producto.idProducto,
            idGrupoCatalogo
        );

    }


    // =========================================
    // PRODUCTO
    // =========================================

    async function cargarProducto(
        idProducto
    ) {

        mostrarCargando();


        try {

            const response =
                await fetch(
                    `${API_PRODUCTOS}/${idProducto}`,
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

                mostrarError();

                return null;
            }


            const producto =
                await response.json();


            renderizar(
                producto
            );


            return producto;


        } catch (error) {

            console.error(
                'Error al cargar detalle:',
                error
            );


            mostrarError();


            return null;
        }

    }


    // =========================================
    // OPCIONES DEL GRUPO
    // =========================================

    async function cargarOpcionesGrupo(
        idGrupoCatalogo,
        idProductoSeleccionado
    ) {

        const opciones =
            await obtenerOpcionesGrupo(
                idGrupoCatalogo
            );


        if (
            opciones === null
            ||
            opciones.length === 0
        ) {

            ocultarOpciones();

            return;
        }


        renderizarOpciones(
            opciones,
            idProductoSeleccionado
        );

    }


    async function obtenerOpcionesGrupo(
        idGrupoCatalogo
    ) {

        try {

            const response =
                await fetch(
                    `${API_GRUPOS}/${idGrupoCatalogo}/opciones`,
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

                return null;
            }


            const opciones =
                await response.json();


            if (
                !Array.isArray(
                    opciones
                )
            ) {

                return null;
            }


            return opciones;


        } catch (error) {

            console.error(
                'Error al cargar opciones del grupo:',
                error
            );


            return null;
        }

    }


    function renderizarOpciones(
        opciones,
        idProductoSeleccionado
    ) {

        optionsContainer.innerHTML =
            '';


        opciones.forEach(
            opcion => {

                const button =
                    crearOpcion(
                        opcion,
                        idProductoSeleccionado
                    );


                optionsContainer.appendChild(
                    button
                );
            }
        );


        optionsSection.classList.remove(
            'hidden'
        );

    }


    function crearOpcion(
        opcion,
        idProductoSeleccionado
    ) {

        const button =
            document.createElement(
                'button'
            );


        button.type =
            'button';


        button.className =
            'product-option';


        button.dataset.idProducto =
            opcion.idProducto;


        if (
            !opcion.disponible
        ) {

            button.classList.add(
                'product-option--out'
            );
        }


        const seleccionada =
            Number(
                opcion.idProducto
            )
            ===
            Number(
                idProductoSeleccionado
            );


        if (
            seleccionada
        ) {

            button.classList.add(
                'is-active'
            );
        }


        button.setAttribute(
            'aria-pressed',
            seleccionada
                ? 'true'
                : 'false'
        );


        const imageContainer =
            document.createElement(
                'span'
            );


        imageContainer.className =
            'product-option__image';


        imageContainer.textContent =
            'TD';


        if (
            typeof opcion.imagenUrl === 'string'
            &&
            opcion.imagenUrl.trim() !== ''
        ) {

            const img =
                document.createElement(
                    'img'
                );


            img.src =
                opcion.imagenUrl.trim();


            img.alt =
                '';


            img.addEventListener(
                'error',
                () => {

                    img.remove();
                }
            );


            imageContainer.appendChild(
                img
            );
        }


        const optionContent =
            document.createElement(
                'span'
            );


        optionContent.className =
            'product-option__content';


        const optionName =
            document.createElement(
                'span'
            );


        optionName.className =
            'product-option__name';


        optionName.textContent =
            opcion.nombre
            ||
            'Producto';


        const optionPrice =
            document.createElement(
                'span'
            );


        optionPrice.className =
            'product-option__price';


        optionPrice.textContent =
            formatearPrecio(
                opcion.precioVenta
            );


        const optionStock =
            document.createElement(
                'span'
            );


        optionStock.className =
            'product-option__stock';


        if (
            opcion.disponible
        ) {

            optionStock.textContent =
                'Disponible';


        } else {

            optionStock.textContent =
                'Sin stock';


            optionStock.classList.add(
                'product-option__stock--out'
            );
        }


        optionContent.appendChild(
            optionName
        );


        optionContent.appendChild(
            optionPrice
        );


        optionContent.appendChild(
            optionStock
        );


        button.appendChild(
            imageContainer
        );


        button.appendChild(
            optionContent
        );


        button.addEventListener(
            'click',
            async () => {

                await seleccionarOpcion(
                    opcion.idProducto
                );
            }
        );


        return button;

    }


    async function seleccionarOpcion(
        idProducto
    ) {

        const producto =
            await cargarProducto(
                idProducto
            );


        if (
            !producto
        ) {

            return;
        }


        marcarOpcionSeleccionada(
            idProducto
        );


        if (
            producto.idGrupoCatalogo
        ) {

            actualizarUrl(
                producto.idProducto,
                producto.idGrupoCatalogo
            );
        }

    }


    function marcarOpcionSeleccionada(
        idProducto
    ) {

        document
                .querySelectorAll(
                    '.product-option'
                )
                .forEach(
                    opcion => {

                        const activa =
                            Number(
                                opcion.dataset.idProducto
                            )
                            ===
                            Number(
                                idProducto
                            );


                        opcion.classList.toggle(
                            'is-active',
                            activa
                        );


                        opcion.setAttribute(
                            'aria-pressed',
                            activa
                                ? 'true'
                                : 'false'
                        );
                    }
                );

    }


    function ocultarOpciones() {

        optionsContainer.innerHTML =
            '';


        optionsSection.classList.add(
            'hidden'
        );

    }


    // =========================================
    // RENDER PRODUCTO
    // =========================================

    function renderizar(
        producto
    ) {

        name.textContent =
            producto.nombre
            ||
            'Producto';


        document.title =
            `${producto.nombre || 'Producto'} | Todo Descartables`;


        description.textContent =
            producto.descripcion
            &&
            producto.descripcion.trim() !== ''
                ? producto.descripcion
                : 'Sin descripción disponible.';


        renderizarMeta(
            producto
        );


        renderizarPrecio(
            producto
        );


        renderizarDisponibilidad(
            producto
        );


        renderizarMayorista(
            producto
        );


        renderizarGrupo(
            producto
        );


        renderizarImagenes(
            producto.imagenes,
            producto.nombre
        );


        loading.classList.add(
            'hidden'
        );


        errorState.classList.add(
            'hidden'
        );


        content.classList.remove(
            'hidden'
        );

    }


    function renderizarMeta(
        producto
    ) {

        const partes =
            [];


        if (
            producto.categoria
        ) {

            partes.push(
                producto.categoria
            );
        }


        if (
            producto.marca
        ) {

            partes.push(
                producto.marca
            );
        }


        meta.textContent =
            partes.join(
                ' · '
            );

    }


    // =========================================
    // PRECIO
    // =========================================

    function renderizarPrecio(
        producto
    ) {

        if (
            producto.enOferta
            &&
            producto.precioOferta != null
        ) {

            offerBadge.textContent =
                `${formatearPorcentaje(
                    producto.porcentajeDescuentoOferta
                )} de descuento`;


            offerBadge.classList.remove(
                'hidden'
            );


            originalPrice.textContent =
                formatearPrecio(
                    producto.precioVenta
                );


            originalPrice.classList.remove(
                'hidden'
            );


            price.textContent =
                formatearPrecio(
                    producto.precioOferta
                );


            return;
        }


        offerBadge.classList.add(
            'hidden'
        );


        originalPrice.classList.add(
            'hidden'
        );


        price.textContent =
            formatearPrecio(
                producto.precioVenta
            );

    }


    // =========================================
    // DISPONIBILIDAD
    // =========================================

    function renderizarDisponibilidad(
        producto
    ) {

        if (
            producto.disponible
        ) {

            availability.textContent =
                'Disponible';


            availability.classList.remove(
                'product-info__availability--out'
            );


            stockNote.textContent =
                'Este producto tiene stock disponible. La incorporación al carrito se habilitará en la funcionalidad de compra.';


        } else {

            availability.textContent =
                'Sin stock';


            availability.classList.add(
                'product-info__availability--out'
            );


            stockNote.textContent =
                'Este producto no tiene stock disponible actualmente.';
        }

    }


    // =========================================
    // MAYORISTA
    // =========================================

    function renderizarMayorista(
        producto
    ) {

        if (
            !producto.tieneDescuentoMayorista
        ) {

            wholesale.classList.add(
                'hidden'
            );


            wholesaleText.textContent =
                '';


            return;
        }


        wholesaleText.textContent =
            `Comprando desde ${
                producto.cantidadMinimaMayorista
            } unidades se aplica un ${
                formatearPorcentaje(
                    producto.porcentajeDescuentoMayorista
                )
            } de descuento.`;


        wholesale.classList.remove(
            'hidden'
        );

    }


    // =========================================
    // GRUPO
    // =========================================

    function renderizarGrupo(
        producto
    ) {

        if (
            !producto.idGrupoCatalogo
            ||
            !producto.grupoCatalogo
        ) {

            group.classList.add(
                'hidden'
            );


            groupName.textContent =
                '';


            optionsGroupName.textContent =
                '';


            ocultarOpciones();


            return;
        }


        groupName.textContent =
            producto.grupoCatalogo;


        optionsGroupName.textContent =
            producto.grupoCatalogo;


        group.classList.remove(
            'hidden'
        );

    }


    // =========================================
    // IMÁGENES
    // =========================================

    function renderizarImagenes(
        imagenes,
        nombreProducto
    ) {

        thumbnails.innerHTML =
            '';


        mainImage.classList.remove(
            'is-loaded'
        );


        mainImage.removeAttribute(
            'src'
        );


        mainImage.alt =
            '';


        if (
            !Array.isArray(
                imagenes
            )
            ||
            imagenes.length === 0
        ) {

            thumbnails.classList.add(
                'hidden'
            );


            return;
        }


        cambiarImagenPrincipal(
            imagenes[0].urlImagen,
            nombreProducto
        );


        if (
            imagenes.length === 1
        ) {

            thumbnails.classList.add(
                'hidden'
            );


            return;
        }


        imagenes.forEach(
            (imagen, indice) => {

                const button =
                    document.createElement(
                        'button'
                    );


                button.type =
                    'button';


                button.className =
                    'product-gallery__thumbnail';


                if (
                    indice === 0
                ) {

                    button.classList.add(
                        'is-active'
                    );
                }


                const img =
                    document.createElement(
                        'img'
                    );


                img.src =
                    imagen.urlImagen;


                img.alt =
                    `Imagen ${indice + 1} de ${nombreProducto || 'producto'}`;


                button.appendChild(
                    img
                );


                button.addEventListener(
                    'click',
                    () => {

                        cambiarImagenPrincipal(
                            imagen.urlImagen,
                            nombreProducto
                        );


                        document
                                .querySelectorAll(
                                    '.product-gallery__thumbnail'
                                )
                                .forEach(
                                    thumbnail =>

                                        thumbnail.classList.remove(
                                            'is-active'
                                        )
                                );


                        button.classList.add(
                            'is-active'
                        );
                    }
                );


                thumbnails.appendChild(
                    button
                );
            }
        );


        thumbnails.classList.remove(
            'hidden'
        );

    }


    function cambiarImagenPrincipal(
        ruta,
        nombreProducto
    ) {

        mainImage.classList.remove(
            'is-loaded'
        );


        mainImage.onload =
            () => {

                mainImage.classList.add(
                    'is-loaded'
                );
            };


        mainImage.onerror =
            () => {

                mainImage.classList.remove(
                    'is-loaded'
                );
            };


        mainImage.alt =
            `Imagen de ${nombreProducto || 'producto'}`;


        mainImage.src =
            ruta;

    }


    // =========================================
    // URL
    // =========================================

    function actualizarUrl(
        idProducto,
        idGrupoCatalogo
    ) {

        const url =
            new URL(
                window.location.href
            );


        url.searchParams.set(
            'id',
            idProducto
        );


        url.searchParams.set(
            'grupo',
            idGrupoCatalogo
        );


        window.history.replaceState(
            {},
            '',
            url
        );

    }


    // =========================================
    // ESTADOS
    // =========================================

    function mostrarCargando() {

        loading.classList.remove(
            'hidden'
        );


        errorState.classList.add(
            'hidden'
        );

    }


    function mostrarError() {

        loading.classList.add(
            'hidden'
        );


        content.classList.add(
            'hidden'
        );


        errorState.classList.remove(
            'hidden'
        );

    }


    // =========================================
    // FORMATO
    // =========================================

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


    function formatearPorcentaje(
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

            return '';
        }


        return Number.isInteger(
            numero
        )
            ? `${numero}%`
            : `${numero.toLocaleString(
                'es-AR'
            )}%`;

    }


    // =========================================
    // CUENTA
    // =========================================

    function configurarCuenta() {

        if (
            typeof Auth === 'undefined'
            ||
            !accountLink
        ) {

            return;
        }


        const token =
            Auth.obtenerToken();


        const rol =
            Auth.obtenerRol();


        if (
            token
            &&
            rol
        ) {

            accountLink.textContent =
                'Mi cuenta';


            accountLink.href =
                Auth.obtenerRutaPorRol(
                    rol
                );
        }

    }

})();