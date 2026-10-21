(() => {

    'use strict';


    const checkoutButton =
        document.getElementById(
            'cart-checkout-button'
        );


    if (
        !checkoutButton
        ||
        typeof Auth === 'undefined'
    ) {

        return;
    }


    /*
     * Se utiliza captura para interceptar el botón
     * antes del listener anterior de US07.
     */

    checkoutButton.addEventListener(
        'click',
        event => {

            if (
                checkoutButton.disabled
            ) {

                return;
            }


            event.preventDefault();

            event.stopImmediatePropagation();


            const token =
                Auth.obtenerToken();


            const rol =
                Auth.obtenerRol();


            /*
             * El carrito puede usarse como visitante,
             * pero para comenzar el checkout debe
             * iniciar sesión.
             */

            if (
                !token
                ||
                !rol
            ) {

                window.location.href =
                    '/login/index.html';


                return;
            }


            if (
                rol !== 'CLIENTE'
            ) {

                Auth.redirigirSegunRol(
                    rol
                );


                return;
            }


            window.location.href =
                '/cliente/checkout-medio-pago.html';
        },
        true
    );

})();