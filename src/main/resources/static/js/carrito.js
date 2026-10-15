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
    // DATOS COMERCIALES DEL ITEM
    // =========================================

    function actualizarDatosComerciales(
        item,
        producto
    ) {

        item.nombre =
            producto.nombre;


        item.precioBase =
            Number(
                producto.precioVenta
            );


        item.stockDisponible =
            Number(
                producto.stockDisponible
            );


        item.enOferta =
            Boolean(
                producto.enOferta
            );


        item.precioOferta =
            producto.precioOferta != null
                ? Number(
                    producto.precioOferta
                )
                : null;


        item.porcentajeDescuentoOferta =
            producto.porcentajeDescuentoOferta != null
                ? Number(
                    producto.porcentajeDescuentoOferta
                )
                : null;


        item.tieneDescuentoMayorista =
            Boolean(
                producto.tieneDescuentoMayorista
            );


        item.cantidadMinimaMayorista =
            producto.cantidadMinimaMayorista != null
                ? Number(
                    producto.cantidadMinimaMayorista
                )
                : null;


        item.porcentajeDescuentoMayorista =
            producto.porcentajeDescuentoMayorista != null
                ? Number(
                    producto.porcentajeDescuentoMayorista
                )
                : null;
    }

    // =========================================
    // RECALCULAR ITEM
    // =========================================

    function recalcularItem(
        item
    ) {

        const productoParaCalculo = {

            precioVenta:
                item.precioBase,

            enOferta:
                item.enOferta,

            precioOferta:
                item.precioOferta,

            porcentajeDescuentoOferta:
                item.porcentajeDescuentoOferta,

            tieneDescuentoMayorista:
                item.tieneDescuentoMayorista,

            cantidadMinimaMayorista:
                item.cantidadMinimaMayorista,

            porcentajeDescuentoMayorista:
                item.porcentajeDescuentoMayorista
        };


        const precioCalculado =
            calcularPrecio(
                productoParaCalculo,
                item.cantidad
            );


        item.precioUnitario =
            precioCalculado.precioUnitario;


        item.tipoDescuento =
            precioCalculado.tipoDescuento;


        item.porcentajeDescuentoAplicado =
            precioCalculado
                .porcentajeDescuentoAplicado;


        item.subtotal =
            redondearDinero(
                item.precioUnitario
                *
                item.cantidad
            );


        return item;
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


        if (
            itemExistente
        ) {

            actualizarDatosComerciales(
                itemExistente,
                producto
            );


            itemExistente.cantidad =
                cantidadFinal;


            recalcularItem(
                itemExistente
            );


        } else {

            const nuevoItem = {

                idProducto:
                    Number(
                        producto.idProducto
                    ),

                cantidad:
                    cantidadFinal
            };


            actualizarDatosComerciales(
                nuevoItem,
                producto
            );


            recalcularItem(
                nuevoItem
            );


            carrito.push(
                nuevoItem
            );
        }


        guardarCarrito(
            carrito
        );


        return buscarProducto(
            producto.idProducto
        );
    }


    // =========================================
    // ACTUALIZAR CANTIDAD
    // =========================================

    function actualizarCantidad(
        idProducto,
        nuevaCantidad
    ) {

        validarCantidad(
            nuevaCantidad
        );


        const carrito =
            obtenerCarrito();


        const item =
            carrito.find(
                producto =>
                    Number(
                        producto.idProducto
                    )
                    ===
                    Number(
                        idProducto
                    )
            );


        if (
            !item
        ) {

            throw new Error(
                'El producto no se encuentra en el carrito.'
            );
        }


        validarStock(
            nuevaCantidad,
            item.stockDisponible
        );


        item.cantidad =
            nuevaCantidad;


        recalcularItem(
            item
        );


        guardarCarrito(
            carrito
        );


        return item;
    }


    // =========================================
    // SINCRONIZAR PRODUCTO
    // =========================================

    function sincronizarProducto(
        producto
    ) {

        validarProductoSinStock(
            producto
        );


        const carrito =
            obtenerCarrito();


        const item =
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


        if (
            !item
        ) {

            return null;
        }


        actualizarDatosComerciales(
            item,
            producto
        );


        recalcularItem(
            item
        );


        guardarCarrito(
            carrito
        );


        return item;
    }

    function eliminarProducto(
        idProducto
    ) {

        const carrito =
            obtenerCarrito();


        const carritoActualizado =
            carrito.filter(
                item =>
                    Number(
                        item.idProducto
                    )
                    !==
                    Number(
                        idProducto
                    )
            );


        if (
            carritoActualizado.length
            ===
            carrito.length
        ) {

            return false;
        }


        guardarCarrito(
            carritoActualizado
        );


        return true;
    }

    // =========================================
    // VACIAR CARRITO
    // =========================================

    function vaciarCarrito() {

        guardarCarrito(
            []
        );
    }

    function validarProductoSinStock(
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
            producto.stockDisponible == null
        ) {

            throw new Error(
                'No se pudo determinar el stock del producto.'
            );
        }
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

    actualizarCantidad,

    sincronizarProducto,

    eliminarProducto,

    vaciarCarrito,

    obtenerCantidadTotal,

    obtenerSubtotal
};

})();