(() => {

    'use strict';


    let cartButton;

    let cartCount;

    let cartOverlay;

    let cartPanel;

    let cartItems;

    let cartEmpty;

    let cartTotal;

    let closeButton;


    document.addEventListener(
        'DOMContentLoaded',
        iniciar
    );


    // =========================================
    // INICIO
    // =========================================

    function iniciar() {

        if (
            typeof Carrito
            ===
            'undefined'
        ) {

            console.error(
                'No se encontró la lógica del carrito.'
            );

            return;
        }


        crearInterfaz();

        configurarEventos();

        actualizar();
    }


    // =========================================
    // CREAR INTERFAZ
    // =========================================

    function crearInterfaz() {

        crearBotonCarrito();

        crearPanelCarrito();
    }


    // =========================================
    // BOTÓN DEL HEADER
    // =========================================

    function crearBotonCarrito() {

        const navegacion =
            document.querySelector(
                '.catalog-nav, .detail-nav'
            );


        if (!navegacion) {

            return;
        }


        cartButton =
            document.createElement(
                'button'
            );


        cartButton.type =
            'button';


        cartButton.className =
            'cart-header-button';


        cartButton.innerHTML = `
            <span>
                Carrito
            </span>

            <span
                class="cart-header-count"
                aria-label="Cantidad de productos en el carrito"
            >
                0
            </span>
        `;


        cartCount =
            cartButton.querySelector(
                '.cart-header-count'
            );


        const accountLink =
            navegacion.querySelector(
                'a[id$="account-link"]'
            );


        if (accountLink) {

            navegacion.insertBefore(
                cartButton,
                accountLink
            );


        } else {

            navegacion.appendChild(
                cartButton
            );
        }
    }


    // =========================================
    // PANEL
    // =========================================

    function crearPanelCarrito() {

        cartOverlay =
            document.createElement(
                'div'
            );


        cartOverlay.className =
            'cart-overlay hidden';


        cartOverlay.innerHTML = `
            <aside
                class="cart-panel"
                role="dialog"
                aria-modal="true"
                aria-label="Carrito de compras"
            >

                <div class="cart-panel__header">

                    <div>

                        <span class="cart-panel__label">
                            Tu compra
                        </span>

                        <h2>
                            Carrito
                        </h2>

                    </div>


                    <button
                        type="button"
                        class="cart-panel__close"
                        aria-label="Cerrar carrito"
                    >
                        ×
                    </button>

                </div>


                <div
                    class="cart-panel__empty hidden"
                >
                    Todavía no agregaste productos.
                </div>


                <div
                    class="cart-panel__items"
                >
                </div>
                <a href="/carrito/index.html" class="cart-panel__manage">Ver carrito completo</a>

                <div
                    class="cart-panel__footer hidden"
                >

                    <span>
                        Subtotal del carrito
                    </span>

                    <strong
                        class="cart-panel__total"
                    >
                        $0
                    </strong>

                </div>

            </aside>
        `;


        document.body.appendChild(
            cartOverlay
        );


        cartPanel =
            cartOverlay.querySelector(
                '.cart-panel'
            );


        cartItems =
            cartOverlay.querySelector(
                '.cart-panel__items'
            );


        cartEmpty =
            cartOverlay.querySelector(
                '.cart-panel__empty'
            );


        cartTotal =
            cartOverlay.querySelector(
                '.cart-panel__total'
            );


        closeButton =
            cartOverlay.querySelector(
                '.cart-panel__close'
            );
    }


    // =========================================
    // EVENTOS
    // =========================================

    function configurarEventos() {

        if (cartButton) {

            cartButton.addEventListener(
                'click',
                abrir
            );
        }


        if (closeButton) {

            closeButton.addEventListener(
                'click',
                cerrar
            );
        }


        if (cartOverlay) {

            cartOverlay.addEventListener(
                'click',
                event => {

                    if (
                        event.target
                        ===
                        cartOverlay
                    ) {

                        cerrar();
                    }
                }
            );
        }


        document.addEventListener(
            'keydown',
            event => {

                if (
                    event.key === 'Escape'
                ) {

                    cerrar();
                }
            }
        );


        window.addEventListener(
            'carrito:actualizado',
            actualizar
        );


        window.addEventListener(
            'storage',
            event => {

                if (
                    event.key === 'carrito'
                ) {

                    actualizar();
                }
            }
        );
    }


    // =========================================
    // ACTUALIZAR TODO
    // =========================================

    function actualizar() {

        const carrito =
            Carrito.obtenerCarrito();


        actualizarContador();

        renderizarItems(
            carrito
        );
    }


    // =========================================
    // CONTADOR
    // =========================================

    function actualizarContador() {

        if (!cartCount) {

            return;
        }


        cartCount.textContent =
            String(
                Carrito.obtenerCantidadTotal()
            );
    }


    // =========================================
    // MOSTRAR ITEMS
    // =========================================

    function renderizarItems(
        carrito
    ) {

        if (
            !cartItems
            ||
            !cartEmpty
        ) {

            return;
        }


        cartItems.innerHTML =
            '';


        if (
            carrito.length === 0
        ) {

            cartEmpty.classList.remove(
                'hidden'
            );


            cartItems.classList.add(
                'hidden'
            );


            ocultarFooter();


            return;
        }


        cartEmpty.classList.add(
            'hidden'
        );


        cartItems.classList.remove(
            'hidden'
        );


        carrito.forEach(
            item => {

                const elemento =
                    crearItem(
                        item
                    );


                cartItems.appendChild(
                    elemento
                );
            }
        );


        mostrarFooter();
    }


    // =========================================
    // CREAR ITEM
    // =========================================

    function crearItem(
        item
    ) {

        const article =
            document.createElement(
                'article'
            );


        article.className =
            'cart-item';


        const info =
            document.createElement(
                'div'
            );


        info.className =
            'cart-item__info';


        const nombre =
            document.createElement(
                'strong'
            );


        nombre.className =
            'cart-item__name';


        nombre.textContent =
            item.nombre;


        const detalle =
            document.createElement(
                'span'
            );


        detalle.className =
            'cart-item__detail';


        detalle.textContent =
            `${item.cantidad} × ${formatearPrecio(
                item.precioUnitario
            )
            }`;


        info.appendChild(
            nombre
        );


        info.appendChild(
            detalle
        );


        if (
            item.tipoDescuento
        ) {

            const descuento =
                document.createElement(
                    'span'
                );


            descuento.className =
                'cart-item__discount';


            descuento.textContent =
                obtenerTextoDescuento(
                    item
                );


            info.appendChild(
                descuento
            );
        }


        const subtotal =
            document.createElement(
                'strong'
            );


        subtotal.className =
            'cart-item__subtotal';


        subtotal.textContent =
            formatearPrecio(
                item.subtotal
            );


        article.appendChild(
            info
        );


        article.appendChild(
            subtotal
        );


        return article;
    }


    // =========================================
    // DESCUENTO
    // =========================================

    function obtenerTextoDescuento(
        item
    ) {

        if (
            item.tipoDescuento
            ===
            'OFERTA'
        ) {

            return 'Oferta aplicada';
        }


        if (
            item.tipoDescuento
            ===
            'MAYORISTA'
        ) {

            return 'Descuento mayorista aplicado';
        }


        return '';
    }


    // =========================================
    // FOOTER
    // =========================================

    function mostrarFooter() {

        const footer =
            cartOverlay.querySelector(
                '.cart-panel__footer'
            );


        footer.classList.remove(
            'hidden'
        );


        cartTotal.textContent =
            formatearPrecio(
                Carrito.obtenerSubtotal()
            );
    }


    function ocultarFooter() {

        const footer =
            cartOverlay.querySelector(
                '.cart-panel__footer'
            );


        footer.classList.add(
            'hidden'
        );
    }


    // =========================================
    // ABRIR / CERRAR
    // =========================================

    function abrir() {

        actualizar();


        cartOverlay.classList.remove(
            'hidden'
        );


        document.body.classList.add(
            'cart-open'
        );
    }


    function cerrar() {

        if (!cartOverlay) {

            return;
        }


        cartOverlay.classList.add(
            'hidden'
        );


        document.body.classList.remove(
            'cart-open'
        );
    }


    // =========================================
    // PRECIO
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

            return '$0';
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

})();