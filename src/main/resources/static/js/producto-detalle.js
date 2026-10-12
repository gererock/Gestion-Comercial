(() => {

    'use strict';


    const API =
        '/api/public/catalogo/productos';


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


    async function iniciar() {

        configurarCuenta();


        const idProducto =
            obtenerIdProducto();


        if (
            !idProducto
        ) {

            mostrarError();

            return;
        }


        await cargarProducto(
            idProducto
        );

    }


    function obtenerIdProducto() {

        const parametros =
            new URLSearchParams(
                window.location.search
            );


        const id =
            Number(
                parametros.get(
                    'id'
                )
            );


        if (
            !Number.isInteger(
                id
            )
            ||
            id <= 0
        ) {

            return null;
        }


        return id;

    }


    async function cargarProducto(
        idProducto
    ) {

        try {

            const response =
                await fetch(
                    `${API}/${idProducto}`,
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

                return;
            }


            const producto =
                await response.json();


            renderizar(
                producto
            );


        } catch (error) {

            console.error(
                'Error al cargar detalle:',
                error
            );


            mostrarError();
        }

    }


    function renderizar(
        producto
    ) {

        name.textContent =
            producto.nombre
            || 'Producto';


        document.title =
            `${producto.nombre || 'Producto'} | Todo Descartables`;


        description.textContent =
            producto.descripcion
            && producto.descripcion.trim() !== ''
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
            producto.imagenes
        );


        loading.classList.add(
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


    function renderizarMayorista(
        producto
    ) {

        if (
            !producto.tieneDescuentoMayorista
        ) {

            wholesale.classList.add(
                'hidden'
            );

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

            return;
        }


        groupName.textContent =
            producto.grupoCatalogo;


        group.classList.remove(
            'hidden'
        );

    }


    function renderizarImagenes(
        imagenes
    ) {

        thumbnails.innerHTML =
            '';


        mainImage.classList.remove(
            'is-loaded'
        );


        if (
            !Array.isArray(
                imagenes
            )
            ||
            imagenes.length === 0
        ) {

            mainImage.removeAttribute(
                'src'
            );


            thumbnails.classList.add(
                'hidden'
            );


            return;
        }


        cambiarImagenPrincipal(
            imagenes[0].urlImagen
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
                    `Imagen ${indice + 1} del producto`;


                button.appendChild(
                    img
                );


                button.addEventListener(
                    'click',
                    () => {

                        cambiarImagenPrincipal(
                            imagen.urlImagen
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
        ruta
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


        mainImage.src =
            ruta;

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


        return Number.isInteger(
            numero
        )
            ? `${numero}%`
            : `${numero.toLocaleString(
                'es-AR'
            )}%`;

    }


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