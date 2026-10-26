(() => {
'use strict';
if (!Auth.requerirRol('VENDEDOR','ADMINISTRADOR')) return;
const $=id=>document.getElementById(id), api='/api/vendedor/ventas-locales';
$('volver').href=Auth.obtenerRutaPorRol(Auth.obtenerRol());
let clienteSeleccionado=null, versionClientes=0, mediosDisponibles=false;
const lineas=new Map(); let guardando=false, pendiente=null, confirmada=false, versionBusqueda=0;
const moneda=n=>Number(n).toLocaleString('es-AR',{style:'currency',currency:'ARS'});
const entero=v=>/^[0-9]+$/.test(String(v)) && Number.isSafeInteger(Number(v)) && Number(v)>0 && Number(v)<=2147483647;
const nodo=(tag,text,clase)=>{const n=document.createElement(tag);if(text!==undefined)n.textContent=text;if(clase)n.className=clase;return n;};
function calculo(p,cantidad){const bruto=Math.round(Number(p.precioVenta)*cantidad*100)/100;let porcentaje=0,tipo='Sin descuento';
if(p.enOferta){porcentaje=Number(p.porcentajeDescuentoOferta);tipo='Oferta';}
else if(p.cantidadMinimaMayorista!=null && cantidad>=p.cantidadMinimaMayorista){porcentaje=Number(p.porcentajeDescuentoMayorista);tipo='Mayorista';}
const descuento=Math.round(bruto*porcentaje)/100;return {bruto,descuento,neto:bruto-descuento,tipo};}
function bloquear(){return guardando || !!pendiente || confirmada;}
function actualizar(){let total=0,valida=lineas.size>0;$('lineas').replaceChildren();
for(const [id,l] of lineas){const card=nodo('div',undefined,'producto'),desc=nodo('div',undefined,'descripcion');
desc.append(nodo('strong',l.p.nombre),nodo('p','ID '+id+' · Stock: '+l.p.stockActual+' · Unitario: '+moneda(l.p.precioVenta)));
const valido=entero(l.cantidad)&&Number(l.cantidad)<=l.p.stockActual;valida=valida&&valido;
if(valido){const c=calculo(l.p,Number(l.cantidad));total+=c.neto;desc.append(nodo('p',c.tipo+': '+moneda(c.descuento)+' · Subtotal: '+moneda(c.neto)));}
else desc.append(nodo('p','Ingresá una cantidad entera entre 1 y '+l.p.stockActual));
const label=nodo('label','Cantidad');const input=nodo('input');input.id='cantidad-'+id;label.htmlFor=input.id;input.type='text';input.inputMode='numeric';input.value=l.cantidad;input.disabled=bloquear();input.setAttribute('aria-invalid',String(!valido));
input.addEventListener('input',()=>{
 l.cantidad=input.value;
 const valido=entero(l.cantidad)&&Number(l.cantidad)<=l.p.stockActual;
 input.setAttribute('aria-invalid',String(!valido));
 const c=valido?calculo(l.p,Number(l.cantidad)):null;
 desc.lastElementChild.textContent=c?c.tipo+': '+moneda(c.descuento)+' · Subtotal: '+moneda(c.neto):'Ingresá una cantidad entera entre 1 y '+l.p.stockActual;
 actualizarResumen();
});
const quitar=nodo('button','Eliminar');quitar.disabled=bloquear();quitar.onclick=()=>{lineas.delete(id);actualizar();};
card.append(desc,label,input,quitar);$('lineas').append(card);}
$('vacia').hidden=lineas.size>0;$('total').textContent='Total estimado: '+moneda(total);
$('confirmar').disabled=guardando||confirmada||!valida||(!pendiente&&!pagoValido());$('confirmar').textContent=guardando?'Confirmando…':pendiente?'Reintentar confirmación':'Confirmar venta';
$('cancelar').disabled=bloquear();document.querySelectorAll('#resultados button').forEach(b=>b.disabled=bloquear());actualizarResumen();actualizarCliente();actualizarPago();}
$('busqueda').onsubmit=async event=>{event.preventDefault();const version=++versionBusqueda;$('resultados').replaceChildren();
const id=$('idProducto').value.trim(),nombre=$('nombre').value.trim();if(id&&!entero(id)){$('estadoBusqueda').textContent='Ingresá un ID entero positivo.';return;}
$('estadoBusqueda').textContent='Buscando…';const params=new URLSearchParams();if(id)params.set('idProducto',id);if(nombre)params.set('nombre',nombre);
try{const res=await Auth.fetchAutenticado(api+'/productos?'+params);if(!res) return;const data=await res.json();if(version!==versionBusqueda)return;if(!res.ok)throw Error(data.message||data.mensaje||'No se pudo buscar.');
$('estadoBusqueda').textContent=data.length?'Productos disponibles: '+data.length:'Sin resultados.';
for(const p of data){const card=nodo('div',undefined,'producto'),desc=nodo('div',undefined,'descripcion');desc.append(nodo('strong',p.nombre),nodo('p','ID '+p.idProducto+' · Stock: '+p.stockActual+' · Precio: '+moneda(p.precioVenta)));
const boton=nodo('button','Agregar');boton.disabled=bloquear();boton.onclick=()=>{const anterior=lineas.get(p.idProducto),cantidad=anterior?Number(anterior.cantidad)+1:1;
if(!entero(cantidad)||cantidad>p.stockActual){$('mensaje').textContent='La cantidad acumulada supera el stock disponible.';return;}
lineas.set(p.idProducto,{p,cantidad:String(cantidad)});$('mensaje').textContent='';actualizar();};card.append(desc,boton);$('resultados').append(card);}
}catch(e){if(version===versionBusqueda){$('resultados').replaceChildren();$('estadoBusqueda').textContent=e.message;}}};
$('cancelar').onclick=()=>{if(bloquear())return;lineas.clear();reiniciarCliente();reiniciarPago();$('mensaje').textContent='Venta cancelada. No se guardaron cambios.';actualizar();};
$('confirmar').onclick=async()=>{if(guardando||confirmada)return;
if(!pendiente){if(!pagoValido()){$('mensaje').textContent='Seleccioná medio y estado. Para pendiente, ingresá una descripción válida.';return;}const items=[];for(const [id,l]of lineas){if(!entero(l.cantidad)||Number(l.cantidad)>l.p.stockActual){$('mensaje').textContent='Revisá las cantidades.';return;}items.push({idProducto:id,cantidad:Number(l.cantidad)});}if(!items.length)return;pendiente={items,claveOperacion:crypto.randomUUID(),idCliente:clienteSeleccionado?.idCliente??null,idMedioPago:Number($('medioPago').value),estadoPago:$('estadoPago').value,descripcionPago:$('descripcionPago').value.trim()||null};}
guardando=true;$('mensaje').textContent='';actualizar();
try{const res=await Auth.fetchAutenticado(api,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(pendiente)});if(!res)throw Error('La sesión terminó.');
const data=await res.json();if(!res.ok){if(res.status<500)pendiente=null;throw Error(data.message||data.mensaje||'No se pudo registrar la venta.');}
confirmada=true;pendiente=null;$('comprobante').hidden=false;$('numero').textContent='Número interno: '+data.numeroPedido;
$('detalleFinal').replaceChildren();for(const d of data.items)$('detalleFinal').append(nodo('p',d.nombre+' × '+d.cantidad+' · Unitario: '+moneda(d.precioUnitario)+' · Descuento: '+moneda(d.descuentoAplicado)+' · Subtotal: '+moneda(d.subtotal)));
$('clienteFinal').textContent=data.cliente?'Cliente: '+[data.cliente.nombre,data.cliente.apellido].filter(Boolean).join(' '):'Consumidor final';
$('pagoFinal').textContent='Medio: '+data.medioPago.nombre+' · Estado: '+data.estadoPago;
$('descripcionPagoFinal').textContent=data.descripcionPago||'';
$('descuentoClienteFinal').textContent='Descuento cliente confirmado: '+moneda(data.descuentoCliente);
$('totalFinal').textContent='Total confirmado: '+moneda(data.total);$('mensaje').textContent='Venta registrada correctamente.';
}catch(e){$('mensaje').textContent=e.message+(pendiente?' Reintentá con la misma operación para consultar o completar su resultado.':'');}
finally{guardando=false;actualizar();}};
$('nueva').onclick=()=>{lineas.clear();reiniciarCliente();reiniciarPago();confirmada=false;$('comprobante').hidden=true;$('mensaje').textContent='';$('resultados').replaceChildren();$('estadoBusqueda').textContent='';actualizar();};

function actualizarResumen(){
 let bruto=0,oferta=0,mayorista=0,neto=0,valida=lineas.size>0;
 for(const l of lineas.values()){
  const correcta=entero(l.cantidad)&&Number(l.cantidad)<=l.p.stockActual;
  valida=valida&&correcta;
  if(!correcta)continue;
  const c=calculo(l.p,Number(l.cantidad));bruto+=c.bruto;neto+=c.neto;
  if(c.tipo==='Oferta')oferta+=c.descuento;
  if(c.tipo==='Mayorista')mayorista+=c.descuento;
 }
 const porcentaje=Number(clienteSeleccionado?.descuentoPorcentaje??0);
 const descuento=Math.round(neto*porcentaje)/100;
 $('subtotalBruto').textContent=moneda(bruto);
 $('descuentoOferta').textContent=moneda(oferta);
 $('descuentoMayorista').textContent=moneda(mayorista);
 $('descuentoCliente').textContent=moneda(descuento)+' ('+porcentaje+'%)';
 $('total').textContent='Total estimado: '+moneda(neto-descuento);
 $('confirmar').disabled=guardando||confirmada||!valida||(!pendiente&&!pagoValido());
}
function actualizarCliente(){
 const contenedor=$('clienteSeleccionado');contenedor.replaceChildren();
 if(!clienteSeleccionado)contenedor.textContent='Consumidor final';
 else{
  const c=clienteSeleccionado;
  contenedor.append(nodo('strong',[c.nombre,c.apellido].filter(Boolean).join(' ')));
  for(const [etiqueta,valor]of [['DNI',c.dni],['Teléfono',c.telefono],['Correo',c.correo]]){
   if(valor)contenedor.append(nodo('p',etiqueta+': '+valor));
  }
  contenedor.append(nodo('p','Descuento cliente: '+Number(c.descuentoPorcentaje??0)+'%'));
 }
 $('buscarCliente').textContent=clienteSeleccionado?'Cambiar cliente':'Buscar cliente';
 $('buscarCliente').disabled=bloquear();$('quitarCliente').hidden=!clienteSeleccionado;$('quitarCliente').disabled=bloquear();
 document.querySelectorAll('#busquedaClientes input,#busquedaClientes button').forEach(n=>n.disabled=bloquear());
 document.querySelectorAll('#resultadosClientes button').forEach(n=>n.disabled=bloquear()||n.dataset.activo!=='true');
}
function cerrarBuscadorCliente(){
 versionClientes++;$('buscadorCliente').hidden=true;$('buscarCliente').setAttribute('aria-expanded','false');
 $('resultadosClientes').replaceChildren();$('estadoClientes').textContent='';
}
function reiniciarCliente(){
 clienteSeleccionado=null;cerrarBuscadorCliente();$('consultaCliente').value='';
}
$('buscarCliente').onclick=()=>{
 if(bloquear())return;
 $('buscadorCliente').hidden=false;$('buscarCliente').setAttribute('aria-expanded','true');$('consultaCliente').focus();
};
$('quitarCliente').onclick=()=>{if(bloquear())return;reiniciarCliente();actualizar();};
$('busquedaClientes').onsubmit=async event=>{
 event.preventDefault();if(bloquear())return;
 const version=++versionClientes,valor=$('consultaCliente').value;
 $('resultadosClientes').replaceChildren();
 if(valor.length>0&&(!valor.trim()||!/[\p{L}\p{N}]/u.test(valor))){
  $('estadoClientes').textContent='Ingresá una búsqueda con letras o números.';return;
 }
 $('estadoClientes').textContent='Buscando clientes…';
 const params=new URLSearchParams();if(valor)params.set('busqueda',valor);
 try{
  const respuesta=await Auth.fetchAutenticado('/api/vendedor/clientes?'+params);
  if(!respuesta)return;
  const clientes=await respuesta.json();if(version!==versionClientes)return;
  if(!respuesta.ok)throw Error(clientes.message||'No se pudieron buscar los clientes.');
  $('estadoClientes').textContent=clientes.length?'Clientes encontrados: '+clientes.length:'No se encontraron clientes';
  for(const c of clientes){
   const card=nodo('div',undefined,'producto'),descripcion=nodo('div',undefined,'descripcion');
   descripcion.append(nodo('strong',[c.nombre,c.apellido].filter(Boolean).join(' ')));
   for(const [etiqueta,valor]of [['DNI',c.dni],['Teléfono',c.telefono],['Correo',c.correo]]){
    if(valor)descripcion.append(nodo('p',etiqueta+': '+valor));
   }
   descripcion.append(nodo('p','Descuento: '+Number(c.descuentoPorcentaje??0)+'%'));
   if(!c.estado)descripcion.append(nodo('p','Cliente inactivo','inactivo'));
   const seleccionar=nodo('button',c.estado?'Seleccionar cliente':'Cliente inactivo');seleccionar.type='button';
   seleccionar.dataset.activo=String(c.estado===true);seleccionar.disabled=c.estado!==true||bloquear();
   seleccionar.onclick=()=>{if(bloquear()||c.estado!==true)return;clienteSeleccionado=c;cerrarBuscadorCliente();actualizar();};
   card.append(descripcion,seleccionar);$('resultadosClientes').append(card);
  }
 }catch(error){if(version===versionClientes){$('resultadosClientes').replaceChildren();$('estadoClientes').textContent=error.message;}}
};
function pagoValido(){return mediosDisponibles&&entero($('medioPago').value)&&['PAGADO','PENDIENTE'].includes($('estadoPago').value)&&($('estadoPago').value!=='PENDIENTE'||/[\p{L}\p{N}]/u.test($('descripcionPago').value.trim()));}
function actualizarPago(){
 for(const id of ['medioPago','estadoPago','descripcionPago'])$(id).disabled=bloquear()||(id==='medioPago'&&!mediosDisponibles);
 const obligatorio=$('estadoPago').value==='PENDIENTE';$('descripcionPago').required=obligatorio;
 $('labelDescripcionPago').textContent='Descripción de pago ('+(obligatorio?'obligatoria':'opcional')+')';
 $('recargarMedios').disabled=bloquear();
}
function reiniciarPago(){$('medioPago').value='';$('estadoPago').value='';$('descripcionPago').value='';}
async function cargarMedios(){
 mediosDisponibles=false;$('estadoMedios').textContent='Cargando medios de pago…';$('recargarMedios').hidden=true;
 $('medioPago').replaceChildren(nodo('option','Seleccioná un medio'));$('medioPago').firstChild.value='';actualizar();
 try{const res=await Auth.fetchAutenticado(api+'/medios-pago');if(!res)throw Error('La sesión terminó.');const data=await res.json();if(!res.ok)throw Error(data.message||'No se pudieron cargar los medios.');
 for(const medio of data){const opcion=nodo('option',medio.nombre);opcion.value=medio.idMedioPago;$('medioPago').append(opcion);}
 mediosDisponibles=data.length>0;$('estadoMedios').textContent=mediosDisponibles?'Elegí un medio de pago activo.':'No hay medios de pago activos. No se puede confirmar la venta.';
 $('recargarMedios').hidden=mediosDisponibles;
 }catch(e){$('estadoMedios').textContent=e.message;$('recargarMedios').hidden=false;}finally{actualizar();}
}
for(const id of ['medioPago','estadoPago','descripcionPago'])$(id).addEventListener('input',()=>{actualizarPago();actualizarResumen();});
$('recargarMedios').onclick=cargarMedios;
actualizar();cargarMedios();
})();
