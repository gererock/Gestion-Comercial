(() => {

    'use strict';


    const emptyState =
        document.getElementById(
            'cart-page-empty'
        );


    const layout =
        document.getElementById(
            'cart-page-layout'
        );


    const productsList =
        document.getElementById(
            'cart-products-list'
        );


    const summaryQuantity =
        document.getElementById(
            'cart-summary-quantity'
        );


    const summaryTotal =
        document.getElementById(
            'cart-summary-total'
        );


    const clearButton =
        document.getElementById(
            'cart-clear-button'
        );


    const clearModal =
        document.getElementById(
            'cart-clear-modal'
        );


    const clearCancel =
        document.getElementById(
            'cart-clear-cancel'
        );


    const clearConfirm =
        document.getElementById(
            'cart-clear-confirm'
        );


    const checkoutButton =
        document.getElementById(
            'cart-checkout-button'
        );


    const message =
        document.getElementById(
            'cart-page-message'
        );


    const accountLink =
        document.getElementById(
            'cart-page-account-link'
        );


    const productosNoDisponibles =
        new Set();


    iniciar();


    // =========================================
    // INICIO
    // =========================================

    async function iniciar() {

        configurarCuenta();


        await sincronizarCarrito();
        

        configurarEventos();


        renderizar();
    }


    // =========================================
    // EVENTOS GENERALES
    // =========================================

    function configurarEventos() {

        clearButton.addEventListener(
            'click',
            abrirModalVaciar
        );


        clearCancel.addEventListener(
            'click',
            cerrarModalVaciar
        );


        clearConfirm.addEventListener(
            'click',
            confirmarVaciar
        );


        checkoutButton.addEventListener(
            'click',
            continuarPedido
        );


        clearModal.addEventListener(
            'click',
            event => {

                if (
                    event.target
                    ===
                    clearModal
                ) {

                    cerrarModalVaciar();
                }
            }
        );


        document.addEventListener(
            'keydown',
            event => {

                if (
                    event.key
                    ===
                    'Escape'
                ) {

                    cerrarModalVaciar();
                }
            }
        );


        window.addEventListener(
            'carrito:actualizado',
            renderizar
        );
    }


    // =========================================
    // SINCRONIZAR CON BACKEND
    // =========================================

    async function sincronizarCarrito() {

        productosNoDisponibles.clear();


        const carrito =
            Carrito.obtenerCarrito();


        for (
            const item
            of
            carrito
        ) {

            try {

                const response =
                    await fetch(
                        `/api/public/catalogo/productos/${item.idProducto}`
                    );


                if (
                    response.status
                    ===
                    404
                ) {

                    productosNoDisponibles.add(
                        Number(
                            item.idProducto
                        )
                    );


                    continue;
                }


                if (
                    !response.ok
                ) {

                    throw new Error(
                        'No se pudo consultar el producto.'
                    );
                }


                const producto =
                    await response.json();


                Carrito.sincronizarProducto(
                    producto
                );


            } catch (error) {

                console.error(
                    'Error al sincronizar producto:',
                    error
                );


                productosNoDisponibles.add(
                    Number(
                        item.idProducto
                    )
                );
            }
        }
    }


    // =========================================
    // RENDERIZAR
    // =========================================

    function renderizar() {

        ocultarMensaje();


        const carrito =
            Carrito.obtenerCarrito();


        productsList.innerHTML =
            '';


        if (
            carrito.length
            ===
            0
        ) {

            mostrarCarritoVacio();

            return;
        }


        mostrarCarritoConProductos();


        carrito.forEach(
            item => {

                const elemento =
                    crearProductoCarrito(
                        item
                    );


                productsList.appendChild(
                    elemento
                );
            }
        );


        actualizarResumen();
    }


    // =========================================
    // CREAR PRODUCTO
    // =========================================

    function crearProductoCarrito(
        item
    ) {

        const article =
            document.createElement(
                'article'
            );


        article.className =
            'cart-page-item';


        const noDisponible =
            productosNoDisponibles.has(
                Number(
                    item.idProducto
                )
            );


        const stockInsuficiente =
            Number(
                item.cantidad
            )
            >
            Number(
                item.stockDisponible
            );


        article.innerHTML = `

            <div class="cart-page-item__info">

                <h3 class="cart-page-item__name">
                </h3>

                <p class="cart-page-item__price">
                </p>

                <p class="cart-page-item__discount hidden">
                </p>

                <p class="cart-page-item__stock">
                </p>

            </div>


            <div class="cart-page-item__quantity">

                <button
                    type="button"
                    class="cart-quantity-button cart-quantity-minus"
                    aria-label="Disminuir cantidad"
                >
                    −
                </button>


                <input
                    type="number"
                    class="cart-quantity-input"
                    min="1"
                    step="1"
                    inputmode="numeric"
                >


                <button
                    type="button"
                    class="cart-quantity-button cart-quantity-plus"
                    aria-label="Aumentar cantidad"
                >
                    +
                </button>

            </div>


            <div class="cart-page-item__subtotal">

                <span>
                    Subtotal
                </span>

                <strong class="cart-page-item__subtotal-value">
                </strong>

            </div>


            <button
                type="button"
                class="cart-item-remove"
            >
                Eliminar
            </button>

        `;


        const name =
            article.querySelector(
                '.cart-page-item__name'
            );


        const price =
            article.querySelector(
                '.cart-page-item__price'
            );


        const discount =
            article.querySelector(
                '.cart-page-item__discount'
            );


        const stock =
            article.querySelector(
                '.cart-page-item__stock'
            );


        const quantityInput =
            article.querySelector(
                '.cart-quantity-input'
            );


        const minusButton =
            article.querySelector(
                '.cart-quantity-minus'
            );


        const plusButton =
            article.querySelector(
                '.cart-quantity-plus'
            );


        const subtotal =
            article.querySelector(
                '.cart-page-item__subtotal-value'
            );


        const removeButton =
            article.querySelector(
                '.cart-item-remove'
            );


        name.textContent =
            item.nombre;


        price.textContent =
            `Precio unitario: ${formatearPrecio(
                item.precioUnitario
            )
            }`;


        subtotal.textContent =
            formatearPrecio(
                item.subtotal
            );


        quantityInput.value =
            String(
                item.cantidad
            );


        quantityInput.max =
            String(
                item.stockDisponible
            );


        renderizarDescuento(
            discount,
            item
        );


        renderizarStock(
            stock,
            item,
            noDisponible,
            stockInsuficiente
        );


        if (
            noDisponible
        ) {

            article.classList.add(
                'cart-page-item--invalid'
            );


            quantityInput.disabled =
                true;


            minusButton.disabled =
                true;


            plusButton.disabled =
                true;
        }


        minusButton.addEventListener(
            'click',
            () => {

                cambiarCantidad(
                    item.idProducto,
                    item.cantidad - 1
                );
            }
        );


        plusButton.addEventListener(
            'click',
            () => {

                cambiarCantidad(
                    item.idProducto,
                    item.cantidad + 1
                );
            }
        );


        quantityInput.addEventListener(
            'change',
            () => {

                cambiarCantidadDesdeInput(
                    item.idProducto,
                    quantityInput
                );
            }
        );


        removeButton.addEventListener(
            'click',
            () => {

                eliminarProducto(
                    item.idProducto
                );
            }
        );


        return article;
    }


    // =========================================
    // CAMBIAR CANTIDAD
    // =========================================

    function cambiarCantidad(
        idProducto,
        nuevaCantidad
    ) {

        try {

            Carrito.actualizarCantidad(
                idProducto,
                nuevaCantidad
            );


        } catch (error) {

            mostrarMensaje(
                error.message,
                'error'
            );
        }
    }


    function cambiarCantidadDesdeInput(
        idProducto,
        input
    ) {

        const valor =
            input.value.trim();


        if (
            valor === ''
        ) {

            restaurarCantidad(
                idProducto,
                input
            );


            mostrarMensaje(
                'Debe ingresar una cantidad.',
                'error'
            );


            return;
        }


        if (
            !/^\d+$/.test(
                valor
            )
        ) {

            restaurarCantidad(
                idProducto,
                input
            );


            mostrarMensaje(
                'La cantidad debe ser un número entero.',
                'error'
            );


            return;
        }


        const nuevaCantidad =
            Number(
                valor
            );


        try {

            Carrito.actualizarCantidad(
                idProducto,
                nuevaCantidad
            );


        } catch (error) {

            restaurarCantidad(
                idProducto,
                input
            );


            mostrarMensaje(
                error.message,
                'error'
            );
        }
    }


    function restaurarCantidad(
        idProducto,
        input
    ) {

        const item =
            Carrito.buscarProducto(
                idProducto
            );


        if (
            item
        ) {

            input.value =
                String(
                    item.cantidad
                );
        }
    }


    // =========================================
    // ELIMINAR PRODUCTO
    // =========================================

    function eliminarProducto(
        idProducto
    ) {

        const eliminado =
            Carrito.eliminarProducto(
                idProducto
            );


        productosNoDisponibles.delete(
            Number(
                idProducto
            )
        );


        if (
            eliminado
        ) {

            mostrarMensaje(
                'Producto eliminado del carrito.',
                'success'
            );
        }
    }


    // =========================================
    // VACIAR
    // =========================================

    function abrirModalVaciar() {

        clearModal.classList.remove(
            'hidden'
        );
    }


    function cerrarModalVaciar() {

        clearModal.classList.add(
            'hidden'
        );
    }


    function confirmarVaciar() {

        Carrito.vaciarCarrito();


        productosNoDisponibles.clear();


        cerrarModalVaciar();


        mostrarMensaje(
            'El carrito se vació correctamente.',
            'success'
        );
    }


    // =========================================
    // DESCUENTO
    // =========================================

    function renderizarDescuento(
        elemento,
        item
    ) {

        if (
            !item.tipoDescuento
        ) {

            elemento.classList.add(
                'hidden'
            );


            return;
        }


        if (
            item.tipoDescuento
            ===
            'OFERTA'
        ) {

            elemento.textContent =
                `Oferta aplicada: ${formatearPorcentaje(
                    item.porcentajeDescuentoAplicado
                )
                }`;
        }


        if (
            item.tipoDescuento
            ===
            'MAYORISTA'
        ) {

            elemento.textContent =
                `Descuento mayorista: ${formatearPorcentaje(
                    item.porcentajeDescuentoAplicado
                )
                }`;
        }


        elemento.classList.remove(
            'hidden'
        );
    }


    // =========================================
    // STOCK
    // =========================================

    function renderizarStock(
        elemento,
        item,
        noDisponible,
        stockInsuficiente
    ) {

        if (
            noDisponible
        ) {

            elemento.textContent =
                'Este producto ya no está disponible.';


            elemento.classList.add(
                'cart-page-item__stock--error'
            );


            return;
        }


        if (
            stockInsuficiente
        ) {

            elemento.textContent =
                `Stock insuficiente. Disponible: ${item.stockDisponible
                }.`;


            elemento.classList.add(
                'cart-page-item__stock--error'
            );


            return;
        }


        elemento.textContent =
            `Stock disponible: ${item.stockDisponible
            }.`;
    }


    // =========================================
    // RESUMEN
    // =========================================

    function actualizarResumen() {

        summaryQuantity.textContent =
            String(
                Carrito.obtenerCantidadTotal()
            );


        summaryTotal.textContent =
            formatearPrecio(
                Carrito.obtenerSubtotal()
            );


        checkoutButton.disabled =
            !carritoEsValido();
    }


    function carritoEsValido() {

        const carrito =
            Carrito.obtenerCarrito();


        if (
            carrito.length
            ===
            0
        ) {

            return false;
        }


        return carrito.every(
            item => {

                const noDisponible =
                    productosNoDisponibles.has(
                        Number(
                            item.idProducto
                        )
                    );


                const stockInsuficiente =
                    Number(
                        item.cantidad
                    )
                    >
                    Number(
                        item.stockDisponible
                    );


                return (
                    !noDisponible
                    &&
                    !stockInsuficiente
                );
            }
        );
    }


    // =========================================
    // CONTINUAR PEDIDO
    // =========================================

    function continuarPedido() {

        const carrito =
            Carrito.obtenerCarrito();


        if (
            carrito.length
            ===
            0
        ) {

            mostrarMensaje(
                'El carrito está vacío.',
                'error'
            );


            return;
        }


        if (
            !carritoEsValido()
        ) {

            mostrarMensaje(
                'Revisá los productos con problemas de stock antes de continuar.',
                'error'
            );


            return;
        }


        mostrarMensaje(
            'El carrito está listo para continuar con el pedido.',
            'success'
        );
    }


    // =========================================
    // ESTADOS
    // =========================================

    function mostrarCarritoVacio() {

        layout.classList.add(
            'hidden'
        );


        emptyState.classList.remove(
            'hidden'
        );
    }


    function mostrarCarritoConProductos() {

        emptyState.classList.add(
            'hidden'
        );


        layout.classList.remove(
            'hidden'
        );
    }


    // =========================================
    // MENSAJES
    // =========================================

    function mostrarMensaje(
        texto,
        tipo
    ) {

        message.textContent =
            texto;


        message.classList.remove(
            'hidden',
            'cart-page-message--success',
            'cart-page-message--error'
        );


        if (
            tipo === 'success'
        ) {

            message.classList.add(
                'cart-page-message--success'
            );


        } else {

            message.classList.add(
                'cart-page-message--error'
            );
        }
    }


    function ocultarMensaje() {

        message.textContent =
            '';


        message.classList.add(
            'hidden'
        );


        message.classList.remove(
            'cart-page-message--success',
            'cart-page-message--error'
        );
    }


    // =========================================
    // FORMATO
    // =========================================

    function formatearPrecio(
        valor
    ) {

        return new Intl.NumberFormat(
            'es-AR',
            {
                style:
                    'currency',

                currency:
                    'ARS',

                maximumFractionDigits:
                    2
            }
        ).format(
            Number(
                valor
            )
        );
    }


    function formatearPorcentaje(
        valor
    ) {

        return `${Number(
            valor
        ).toLocaleString(
            'es-AR'
        )
            }%`;
    }


    // =========================================
    // CUENTA
    // =========================================

    function configurarCuenta() {

        if (
            typeof Auth
            ===
            'undefined'
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