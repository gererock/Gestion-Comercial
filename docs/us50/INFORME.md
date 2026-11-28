# US50 — Consultar productos más vendidos

**Cierre de QA: SÍ.** Aceptación: **51/51 PASS**, **0 BLOCKED**. Fecha: 26/11/2026. Las pruebas MVC ejecutaron los controladores, filtros JWT, servicios y repositorios de Spring con PostgreSQL. Chrome ejecutó el HTML, CSS y JavaScript contra Spring Boot local.

## Estado inicial

- Rama feacture/US50-producto-mas-vendido
- HEAD b2797c0f59c2ac871afc87fe0bab1fdfd86ab8af
- Ya estaban modificados DetallePedidoRepository.java y admin/index.html.
- Ya existían sin seguimiento ProductoMasVendidoController.java, ProductoMasVendidoResponse.java, PeriodoProductoVendido.java, ProductoMasVendidoService.java, IProductoMasVendidoService.java y los tres archivos web con nombre singular.
- El repositorio de Descargas requirió una copia de trabajo autorizada para la ejecución. Se conservó la rama y el estado inicial. Los ajustes funcionales se limitaron a los archivos web de US50.

## Cambios y defectos

| ID | Reproducción | Esperado | Observado y causa | Corrección y repetición |
|---|---|---|---|---|
| US50-B01 | GET al enlace de US50 del panel y a sus recursos con servidor estático local | 200 para HTML, CSS y JS | Tres 404. El enlace y los recursos referenciaban nombres plurales y los archivos físicos usaban singular. | Se alinearon los tres nombres. La repetición dio tres 200 y el acceso desde el panel funcionó en Chrome contra Spring Boot. Evidencia en us50-ruta.log. |
| US50-B02 | Abrir el reporte en Chrome con JWT vencido | La API devuelve 401 y el login informa que la sesión expiró | Se recibió 401 y faltó el aviso esperado. La pantalla no activaba avisarSesionExpirada del helper de autenticación. | Se activó la opción tanto en el GET del ranking como en el de detalle. Chrome volvió al login con el aviso tras el 401. Evidencia en us50-sesion-inicial.log y us50-navegador.log. |
| US50-B03 | Abrir el modal de un producto QA con categoría y marca presentes | El modal muestra ambos nombres | El GET de detalle respondió 200, pero el modal no mostró la categoría del producto. ProductoResponse expone idCategoria e idMarca y el modal intentaba leer directamente los nombres de esa respuesta. | El modal toma los nombres del ranking para la fila seleccionada. Chrome comprobó ID, nombre, relaciones, precio, stock y estado en productos ACTIVO, PAUSADO y DISCONTINUADO. Evidencia en us50-modal-inicial.log y us50-navegador.log. |

## Fixtures y contraste SQL

Cada pedido de los tests JVM se creó dentro de una transacción con rollback. Se confirmó current_database() antes de preparar datos. El contraste agrupó por ID de producto, relacionó detalle_pedido, pedidos, productos, categorias y marcas, y comparó la respuesta completa ordenada con SUM(cantidad) y SUM(subtotal). Las 19 invocaciones del test incluyen una regresión combinada de US49 y US52.

| Producto | Escenario | Cantidad esperada | Total histórico |
|---|---|---:|---:|
| A | ONLINE y LOCAL, varios detalles, categoría y marca, 999 unidades canceladas excluidas | 100 | 225,00 |
| B | ONLINE y LOCAL, pago pendiente incluido, 999 unidades pendientes de aprobación excluidas | 80 | 420,00 |
| C | LOCAL y relaciones nulas | 90 | 50,00 |
| D | ONLINE histórico, producto PAUSADO | 3 | 12,50 |
| E | LOCAL histórico, producto DISCONTINUADO | 3 | 9,00 |
| F | Solo una línea cancelada | Fuera del ranking | — |

Orden esperado y comprobado A, C, B, D, E. B genera más dinero que A y queda debajo por unidades. El empate D/E se resuelve por ID. Repetir después de subir el precio actual de A a 950 dejó la respuesta idéntica. Los pedidos de la fixture tienen un total propio distinto de los subtotales comparados.

La venta LOCAL pasó por el POST real de ventas locales. El pedido ONLINE se creó por POST de cliente y se aprobó con PATCH de vendedor; SQL verificó ambos estados y las siete unidades. La salida del ranking se comprobó cambiando a CANCELADO únicamente esa fixture dentro de la transacción. El endpoint de decisión de la aplicación únicamente cancela pedidos pendientes, por lo que la transición desde CONFIRMADO se realizó mediante SQL de QA.

Los datos del navegador se etiquetaron con un identificador único de ejecución. Chrome se conectó a Spring con conexiones PostgreSQL en solo lectura y Flyway deshabilitado. Los datos QA se insertaron y eliminaron mediante scripts SQL separados y dirigidos por esa etiqueta. PostgreSQL registró cuatro detalles, un pedido, cuatro productos, una categoría y una marca eliminados en esa ejecución.

## Cobertura por fase

| Fases | Comprobación | Evidencia |
|---|---|---|
| 1 | Suite inicial 347/347, sin fallos, errores ni omitidos | suite-backend.txt |
| 2–3 | Base confirmada, huellas y fixtures de productos, pedidos, modalidades, estados y descuentos | postgresql-antes.txt, fixture-chrome.txt y test JVM |
| 4–5 | JWT de los tres roles, visitante, inválido y vencido. Contrato de seis campos y relaciones nulas | Test JVM y us50-navegador.log |
| 6–12 | Cantidad, subtotal, orden, exclusiones, canales, pago pendiente e históricos | rankingCompletoContraSqlEHistoricos |
| 13–18 | Límites Córdoba por día, semana y mes, rango inclusivo e inferido, fechas imposibles, default DIA y lista vacía | limitesDelPeriodoContraSql, parametrosInvalidos y rangoDeUnDiaYListaVacia |
| 19 | Venta local, alta y aprobación online, y cancelación controlada con SQL | ventaLocalPorEndpointActualizaElRanking y flujoOnlineRealYCancelacionControladaSeReflejanEnElRanking |
| 20 | GET y render en DIA, SEMANA, MES y RANGO. Rango inválido sin GET, edición oculta resultados previos y limpiar restablece DIA | us50-navegador.log |
| 21–25 | Modal e históricos, solo lectura, refresh, tráfico de red y excepciones de JavaScript en Chrome | us50-navegador.log y solo-lectura-antes/despues.txt |
| 26 | 401 real. 400, 403, 404 y 500 simulados con Chrome DevTools. El backend real respondió 200 después de cada simulación | us50-navegador.log |
| 27 | 1366×768, 768×1024, 390×844 y 430×932. Tabla y modal en Chrome con textos largos, sin desborde horizontal de la página | us50-navegador.log y capturas cuando disponibles |
| 28 | Login, enlace del panel y vuelta al panel comprobados en Chrome | us50-navegador.log |
| 29 | 51 casos oficiales | Matriz incluida abajo |
| 30 | US44/US45 con tests de historial y filtros. US47 y US48 con sus suites. US49 y US52 con fixtures compartidas y agregación SQL independiente | suite-backend.txt y regresionComparacionYTicketContraSql |
| 31–32 | Rollbacks, limpieza delimitada, conteos y huellas, informe y evidencia | postgresql-antes/despues.txt y limpieza-chrome.txt |

## Aislamiento y limpieza

- Base consultada y modificada solo para QA: gestion_comercial_test.
- Huellas de filas públicas antes y después coinciden: True. Huellas durante la sesión interactiva, con fixtures presentes: True.
- Se comparó el conteo y un resumen MD5 ordenado de las filas de cada tabla pública. Los contadores de secuencias PostgreSQL pueden avanzar por la creación de fixtures y quedan fuera de la comparación de filas.
- La evidencia de Network excluye los encabezados de autenticación y los cuerpos sensibles. El POST de login es de la pantalla de autenticación. Dentro de US50 se verificaron únicamente GET. El registro de consola contabiliza excepciones JavaScript no capturadas. Los 4xx y 5xx provocados en el ensayo de error están señalados explícitamente.

## Matriz de aceptación

| ID | Acción o condición | Resultado esperado | Resultado observado | Estado | Evidencia |
|---:|---|---|---|---|---|
| 1 | Administrador inicia sesión y entra | Login 200 y ranking accesible | El formulario devolvió 200 y el enlace del panel abrió el ranking. | PASS | Chrome |
| 2 | Vendedor intenta acceder | 403 en API y redirección desde el HTML | API 403 y navegación a /vendedor/index.html. | PASS | Chrome y rolSinAcceso |
| 3 | Cliente intenta acceder | 403 en API y redirección desde el HTML | API 403 y navegación a /cliente/index.html. | PASS | Chrome y rolSinAcceso |
| 4 | Visitante intenta acceder | 401 en API y redirección a login | API 401 y navegación al login. | PASS | Chrome y credencialesAusentesInvalidasOVencidas |
| 5 | Sesión vencida | 401 y aviso de sesión expirada | API 401 y mensaje de expiración visible en el login. | PASS | Chrome y credencialesAusentesInvalidasOVencidas |
| 6 | Listado visible | Tabla con filas QA | Chrome mostró cuatro filas identificadas por la etiqueta QA. | PASS | Chrome |
| 7 | Existen ventas | 200 con filas y cantidades | El GET devolvió 200 e incluyó los cinco productos válidos de la fixture JVM. | PASS | MVC y PostgreSQL |
| 8 | No existen ventas | 200 y lista vacía | El GET a 1900-01-01 devolvió 200 con un arreglo vacío. | PASS | MVC y PostgreSQL |
| 9 | ID visible | Se muestra el identificador del producto | El ID numérico apareció en la fila y coincidió con el detalle. | PASS | Chrome y contrato MVC |
| 10 | Nombre visible | Se muestra el nombre del producto | Las cuatro filas mostraron sus nombres QA completos. | PASS | Chrome |
| 11 | Categoría visible si existe | Nombre de la categoría presente | La fila y el modal del producto activo mostraron el nombre de categoría. | PASS | Chrome y PostgreSQL |
| 12 | Marca visible si existe | Nombre de la marca presente | La fila y el modal del producto activo mostraron el nombre de marca. | PASS | Chrome y PostgreSQL |
| 13 | Cantidad vendida visible | Columna con unidades vendidas | Chrome mostró cuatro unidades por fila QA y SQL registró cuatro unidades por detalle. | PASS | Chrome y PostgreSQL |
| 14 | Total vendido visible | Columna con importe histórico | Chrome mostró 40,00 por fila y PostgreSQL registró 40,00 de subtotal por detalle. | PASS | Chrome y PostgreSQL |
| 15 | Orden descendente correcto | Cantidad decreciente y desempate por ID | A, C, B, D y E. El empate D/E mantuvo ID ascendente. | PASS | MVC y SQL ordenado |
| 16 | Mayor cantidad debajo de menor debe considerarse fallo | La aserción detecta una inversión de posiciones | La comparación posicional de toda la lista comprueba A antes que C, y C antes que B. | PASS | Comparación de lista completa ordenada |
| 17 | Día | Intervalo Córdoba con límite superior exclusivo | Se incluyeron tres líneas del día y quedaron fuera los dos límites externos. | PASS | MVC y SQL |
| 18 | Semana | Lunes a domingo inclusivos | Se incluyeron lunes, día intermedio y domingo. Los límites externos quedaron fuera. | PASS | MVC y SQL |
| 19 | Mes | Primer a último día inclusivos | Se incluyeron el primer y último día del mes, y el día intermedio. | PASS | MVC y SQL |
| 20 | Rango válido | 200 e inclusión de ambas fechas | El rango incluyó ambos límites de fecha. La inferencia con solo fechas produjo el mismo JSON. | PASS | MVC y SQL |
| 21 | Desde mayor que hasta | 400 en API y validación local sin GET | API 400. Chrome mostró la validación y no originó una solicitud de reporte. | PASS | Chrome y parametrosInvalidos |
| 22 | Formato de fecha inválido | 400 para texto, formato local y fecha imposible | Fecha imposible, DD/MM/AAAA y texto devolvieron 400. | PASS | MVC |
| 23 | Período sin ventas | Mensaje y tabla anterior oculta | Chrome mostró la vista vacía, ocultó la tabla anterior y dejó las fechas accesibles. | PASS | Chrome |
| 24 | Período con ventas ONLINE | Las líneas ONLINE están incluidas | Se incluyeron CONFIRMADO, EN_PREPARACION y ENVIADO de origen ONLINE. | PASS | MVC y SQL |
| 25 | Período con ventas LOCAL | Las líneas LOCAL están incluidas | Se incluyeron LISTO_PARA_RETIRAR, LISTO_PARA_ENVIAR y ENTREGADO de origen LOCAL. | PASS | MVC y SQL |
| 26 | Inclusión de ONLINE válidas | Una venta ONLINE válida suma unidades | Las ventas ONLINE de A aportaron 90 unidades a su fila. | PASS | MVC y SQL |
| 27 | Inclusión de LOCAL válidas | Una venta LOCAL válida suma unidades | La venta LOCAL de A aportó diez unidades a esa misma fila. | PASS | MVC y SQL |
| 28 | No incluir pedido CANCELADO | Las 999 unidades canceladas no suman | A conservó 100 unidades válidas frente al intento de sumar 999 canceladas. | PASS | MVC y SQL |
| 29 | No incluir producto de venta cancelada | F, con venta cancelada como única línea, queda fuera | El producto F quedó ausente de la respuesta. | PASS | MVC y SQL |
| 30 | No contar misma línea dos veces | Cada detalle contribuye una vez a la suma | La respuesta y SQL coincidieron fila por fila en las sumas sin multiplicación. | PASS | MVC y SQL |
| 31 | No omitir producto válido | Coinciden todas las filas de la consulta SQL | Las cinco filas válidas de la fixture se encontraron en el JSON. | PASS | MVC y SQL |
| 32 | Cantidad = suma detalle_pedido.cantidad | Cada cantidad coincide con SUM(cantidad) | A 100, C 90, B 80, D 3 y E 3. | PASS | MVC y SQL |
| 33 | Total = suma de subtotales válidos | Cada importe coincide con SUM(subtotal) | A 225,00, C 50,00, B 420,00, D 12,50 y E 9,00. | PASS | MVC y SQL |
| 34 | No mostrar producto sin ventas válidas | F queda fuera | La única línea de F pertenecía a un pedido cancelado y F quedó fuera. | PASS | MVC y SQL |
| 35 | Producto PAUSADO histórico aparece | D figura por su venta válida | D apareció con tres unidades y se pudo abrir un producto pausado en Chrome. | PASS | MVC y SQL |
| 36 | Producto DISCONTINUADO histórico aparece | E figura por su venta válida | E apareció con tres unidades y se pudo abrir un producto discontinuado en Chrome. | PASS | MVC y SQL |
| 37 | Producto eliminado físicamente no debe ser un caso válido | El reporte opera sobre productos persistidos y carece de eliminación | La consulta exige la relación con el producto persistido. Chrome recorrió el reporte sin acciones de eliminación. | PASS | Chrome, SQL y relación detalle/producto |
| 38 | Venta LOCAL nueva actualiza ranking | Venta local real 201 y dos unidades agregadas | POST 201. SQL confirmó dos unidades del producto correcto y se incorporó su fila. | PASS | MVC y SQL |
| 39 | Confirmación ONLINE actualiza ranking si corresponde | Alta pendiente excluida y aprobación real incorpora siete unidades | El POST dejó el pedido pendiente. PATCH aprobar lo confirmó y el ranking sumó siete unidades. | PASS | MVC y SQL |
| 40 | Cancelación quita sus cantidades | Cambio controlado a CANCELADO retira siete unidades | SQL cambió únicamente la fixture confirmada a CANCELADO. Las siete unidades desaparecieron del ranking. | PASS | MVC y cambio SQL QA |
| 41 | DIA a SEMANA actualiza | GET SEMANA y render posterior | Chrome recibió 200 para SEMANA y volvió a renderizar el ranking. | PASS | Chrome |
| 42 | SEMANA a MES actualiza | GET MES y render posterior | Chrome recibió 200 para MES y volvió a renderizar el ranking. | PASS | Chrome |
| 43 | Limpiar período vuelve al default/general | Vuelve a DIA y vacía fechas | Chrome emitió un nuevo GET de DIA y ambos campos de fecha quedaron vacíos. | PASS | Chrome |
| 44 | Seleccionar producto muestra información | Modal del producto correcto con sus datos | Chrome abrió los tres estados y contrastó ID, nombre, categoría, marca, precio, stock y estado. | PASS | Chrome y SQL |
| 45 | No modificar producto | Huellas iguales durante la consulta | Los registros de productos conservaron su huella durante la interacción. | PASS | PostgreSQL y Chrome |
| 46 | No modificar stock | Huellas iguales durante la consulta | Productos y movimientos_stock conservaron sus huellas durante la interacción. | PASS | PostgreSQL y Chrome |
| 47 | No modificar precio | Huellas iguales durante la consulta | El precio persistido se mantuvo durante la consulta del reporte. | PASS | PostgreSQL y Chrome |
| 48 | No eliminar producto | Huellas y conteos iguales durante la consulta | El conteo de productos y su huella se mantuvieron durante la interacción. | PASS | PostgreSQL y Chrome |
| 49 | Refresh conserva datos | GET DIA y cuatro filas QA únicas | Tras recargar, Chrome obtuvo 200 para DIA y mostró cuatro filas QA únicas. | PASS | Chrome |
| 50 | Celular | 390×844 y 430×932 con tabla y modal utilizables | Chrome comprobó tabla, modal y cierre en 390×844 y 430×932, sin desborde horizontal de página. | PASS | Chrome |
| 51 | Computadora | 1366×768 con tabla y modal utilizables | Chrome comprobó tabla, modal y cierre en 1366×768, sin desborde horizontal de página. | PASS | Chrome |
