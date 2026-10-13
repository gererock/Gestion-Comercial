(() => {

    'use strict';


    // =========================================
    // CONFIGURACIÓN
    // =========================================

    const STORAGE_KEY =
        'carrito';


    // =========================================
    // OBTENER CARRITO
    // =========================================

    function obtenerCarrito() {

        const carritoGuardado =
            localStorage.getItem(
                STORAGE_KEY
            );


        if (!carritoGuardado) {

            return [];
        }


        try {

            const carrito =
                JSON.parse(
                    carritoGuardado
                );


            return Array.isArray(carrito)
                ? carrito
                : [];


        } catch (error) {

            console.error(
                'No se pudo leer el carrito:',
                error
            );


            localStorage.removeItem(
                STORAGE_KEY
            );


            return [];
        }
    }


    // =========================================
    // GUARDAR CARRITO
    // =========================================

    function guardarCarrito(
        carrito
    ) {

        localStorage.setItem(

            STORAGE_KEY,

            JSON.stringify(
                carrito
            )
        );


        window.dispatchEvent(
            new CustomEvent(
                'carrito:actualizado'
            )
        );
    }


    // =========================================
    // BUSCAR PRODUCTO
    // =========================================

    function buscarProducto(
        idProducto
    ) {

        const carrito =
            obtenerCarrito();


        return carrito.find(
            item =>
                Number(
                    item.idProducto
                )
                ===
                Number(
                    idProducto
                )
        ) || null;
    }


    // =========================================
    // AGREGAR PRODUCTO
    // =========================================

    function agregarProducto(
        producto,
        cantidad
    ) {

        validarProducto(
            producto
        );


        validarCantidad(
            cantidad
        );


        const carrito =
            obtenerCarrito();


        const itemExistente =
            carrito.find(
                item =>
                    Number(
                        item.idProducto
                    )
                    ===
                    Number(
                        producto.idProducto
                    )
            );


        const cantidadAnterior =
            itemExistente
                ? itemExistente.cantidad
                : 0;


        const cantidadFinal =
            cantidadAnterior
            +
            cantidad;


        validarStock(
            cantidadFinal,
            producto.stockDisponible
        );


        const precioCalculado =
            calcularPrecio(
                producto,
                cantidadFinal
            );


        const subtotal =
            redondearDinero(
                precioCalculado.precioUnitario
                *
                cantidadFinal
            );


        if (itemExistente) {

            itemExistente.nombre =
                producto.nombre;


            itemExistente.precioBase =
                Number(
                    producto.precioVenta
                );


            itemExistente.stockDisponible =
                Number(
                    producto.stockDisponible
                );


            itemExistente.cantidad =
                cantidadFinal;


            itemExistente.precioUnitario =
                precioCalculado.precioUnitario;


            itemExistente.tipoDescuento =
                precioCalculado.tipoDescuento;


            itemExistente.porcentajeDescuentoAplicado =
                precioCalculado
                    .porcentajeDescuentoAplicado;


            itemExistente.subtotal =
                subtotal;


        } else {

            carrito.push({

                idProducto:
                    Number(
                        producto.idProducto
                    ),

                nombre:
                    producto.nombre,

                precioBase:
                    Number(
                        producto.precioVenta
                    ),

                stockDisponible:
                    Number(
                        producto.stockDisponible
                    ),

                cantidad:
                    cantidadFinal,

                precioUnitario:
                    precioCalculado.precioUnitario,

                tipoDescuento:
                    precioCalculado.tipoDescuento,

                porcentajeDescuentoAplicado:
                    precioCalculado
                        .porcentajeDescuentoAplicado,

                subtotal:
                    subtotal
            });
        }


        guardarCarrito(
            carrito
        );


        return buscarProducto(
            producto.idProducto
        );
    }


    // =========================================
    // VALIDAR PRODUCTO
    // =========================================

    function validarProducto(
        producto
    ) {

        if (
            !producto
            ||
            !producto.idProducto
        ) {

            throw new Error(
                'El producto no es válido.'
            );
        }


        if (
            !producto.disponible
        ) {

            throw new Error(
                'El producto no tiene stock disponible.'
            );
        }


        if (
            producto.stockDisponible == null
            ||
            Number(
                producto.stockDisponible
            ) <= 0
        ) {

            throw new Error(
                'El producto no tiene stock disponible.'
            );
        }
    }


    // =========================================
    // VALIDAR CANTIDAD
    // =========================================

    function validarCantidad(
        cantidad
    ) {

        if (
            !Number.isInteger(
                cantidad
            )
        ) {

            throw new Error(
                'La cantidad debe ser un número entero.'
            );
        }


        if (
            cantidad <= 0
        ) {

            throw new Error(
                'La cantidad debe ser mayor a cero.'
            );
        }
    }


    // =========================================
    // VALIDAR STOCK
    // =========================================

    function validarStock(
        cantidadFinal,
        stockDisponible
    ) {

        const stock =
            Number(
                stockDisponible
            );


        if (
            !Number.isInteger(stock)
            ||
            stock < 0
        ) {

            throw new Error(
                'El stock disponible no es válido.'
            );
        }


        if (
            cantidadFinal > stock
        ) {

            throw new Error(
                `La cantidad solicitada supera el stock disponible (${stock}).`
            );
        }
    }


    // =========================================
    // PRECIO
    // =========================================

    function calcularPrecio(
        producto,
        cantidadFinal
    ) {

        const precioBase =
            Number(
                producto.precioVenta
            );


        if (
            !Number.isFinite(
                precioBase
            )
            ||
            precioBase <= 0
        ) {

            throw new Error(
                'El precio del producto no es válido.'
            );
        }


        const aplicaOferta =
            Boolean(
                producto.enOferta
            )
            &&
            producto.precioOferta != null;


        // =====================================
        // OFERTA
        // =====================================

        if (
            aplicaOferta
        ) {

            const precioOferta =
                Number(
                    producto.precioOferta
                );


            return {

                precioUnitario:
                    redondearDinero(
                        precioOferta
                    ),

                tipoDescuento:
                    'OFERTA',

                porcentajeDescuentoAplicado:
                    producto
                        .porcentajeDescuentoOferta
                    ?? null
            };
        }


        const aplicaMayorista =
            Boolean(
                producto.tieneDescuentoMayorista
            )
            &&
            producto.cantidadMinimaMayorista != null
            &&
            producto.porcentajeDescuentoMayorista != null
            &&
            cantidadFinal >=
            Number(
                producto.cantidadMinimaMayorista
            );


        // =====================================
        // MAYORISTA
        // =====================================

        if (
            aplicaMayorista
        ) {

            const porcentaje =
                Number(
                    producto
                        .porcentajeDescuentoMayorista
                );


            const precioMayorista =
                precioBase
                *
                (
                    1
                    -
                    porcentaje / 100
                );


            return {

                precioUnitario:
                    redondearDinero(
                        precioMayorista
                    ),

                tipoDescuento:
                    'MAYORISTA',

                porcentajeDescuentoAplicado:
                    porcentaje
            };
        }


        // =====================================
        // SIN DESCUENTO
        // =====================================

        return {

            precioUnitario:
                redondearDinero(
                    precioBase
                ),

            tipoDescuento:
                null,

            porcentajeDescuentoAplicado:
                null
        };
    }


    // =========================================
    // CANTIDAD TOTAL
    // =========================================

    function obtenerCantidadTotal() {

        return obtenerCarrito()
            .reduce(
                (
                    total,
                    item
                ) =>
                    total
                    +
                    item.cantidad,
                0
            );
    }


    // =========================================
    // SUBTOTAL GENERAL
    // =========================================

    function obtenerSubtotal() {

        return redondearDinero(

            obtenerCarrito()
                .reduce(
                    (
                        total,
                        item
                    ) =>
                        total
                        +
                        item.subtotal,
                    0
                )
        );
    }


    // =========================================
    // REDONDEAR DINERO
    // =========================================

    function redondearDinero(
        valor
    ) {

        return Math.round(
            (
                Number(valor)
                +
                Number.EPSILON
            )
            *
            100
        )
            /
            100;
    }


    // =========================================
    // API PÚBLICA
    // =========================================

    window.Carrito = {

        obtenerCarrito,

        buscarProducto,

        agregarProducto,

        obtenerCantidadTotal,

        obtenerSubtotal
    };

})();