(() => {
'use strict';
if (!Auth.requerirRol('VENDEDOR','ADMINISTRADOR')) return;
const $=id=>document.getElementById(id), api='/api/vendedor/ventas-locales';
$('volver').href=Auth.obtenerRutaPorRol(Auth.obtenerRol());
const lineas=new Map(); let guardando=false, pendiente=null, confirmada=false, versionBusqueda=0;
const moneda=n=>Number(n).toLocaleString('es-AR',{style:'currency',currency:'ARS'});
const entero=v=>/^[0-9]+$/.test(String(v)) && Number.isSafeInteger(Number(v)) && Number(v)>0 && Number(v)<=2147483647;
const nodo=(tag,text,clase)=>{const n=document.createElement(tag);if(text!==undefined)n.textContent=text;if(clase)n.className=clase;return n;};
function calculo(p,cantidad){const bruto=Math.round(Number(p.precioVenta)*cantidad*100)/100;let porcentaje=0,tipo='Sin descuento';
if(p.enOferta){porcentaje=Number(p.porcentajeDescuentoOferta);tipo='Oferta';}
else if(p.cantidadMinimaMayorista!=null && cantidad>=p.cantidadMinimaMayorista){porcentaje=Number(p.porcentajeDescuentoMayorista);tipo='Mayorista';}
const descuento=Math.round(bruto*porcentaje)/100;return {descuento,neto:bruto-descuento,tipo};}
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
 let suma=0,completa=lineas.size>0;
 for(const item of lineas.values()){
  const correcta=entero(item.cantidad)&&Number(item.cantidad)<=item.p.stockActual;
  completa=completa&&correcta;
  if(correcta)suma+=calculo(item.p,Number(item.cantidad)).neto;
 }
 $('total').textContent='Total estimado: '+moneda(suma);
 $('confirmar').disabled=guardando||confirmada||!completa;
});
const quitar=nodo('button','Eliminar');quitar.disabled=bloquear();quitar.onclick=()=>{lineas.delete(id);actualizar();};
card.append(desc,label,input,quitar);$('lineas').append(card);}
$('vacia').hidden=lineas.size>0;$('total').textContent='Total estimado: '+moneda(total);
$('confirmar').disabled=guardando||confirmada||!valida;$('confirmar').textContent=guardando?'Confirmando…':pendiente?'Reintentar confirmación':'Confirmar venta';
$('cancelar').disabled=bloquear();document.querySelectorAll('#resultados button').forEach(b=>b.disabled=bloquear());}
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
$('cancelar').onclick=()=>{if(bloquear())return;lineas.clear();$('mensaje').textContent='Venta cancelada. No se guardaron cambios.';actualizar();};
$('confirmar').onclick=async()=>{if(guardando||confirmada)return;
if(!pendiente){const items=[];for(const [id,l]of lineas){if(!entero(l.cantidad)||Number(l.cantidad)>l.p.stockActual){$('mensaje').textContent='Revisá las cantidades.';return;}items.push({idProducto:id,cantidad:Number(l.cantidad)});}if(!items.length)return;pendiente={items,claveOperacion:crypto.randomUUID()};}
guardando=true;$('mensaje').textContent='';actualizar();
try{const res=await Auth.fetchAutenticado(api,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(pendiente)});if(!res)throw Error('La sesión terminó.');
const data=await res.json();if(!res.ok){if(res.status<500)pendiente=null;throw Error(data.message||data.mensaje||'No se pudo registrar la venta.');}
confirmada=true;pendiente=null;$('comprobante').hidden=false;$('numero').textContent='Número interno: '+data.numeroPedido;
$('detalleFinal').replaceChildren();for(const d of data.items)$('detalleFinal').append(nodo('p',d.nombre+' × '+d.cantidad+' · Unitario: '+moneda(d.precioUnitario)+' · Descuento: '+moneda(d.descuentoAplicado)+' · Subtotal: '+moneda(d.subtotal)));
$('totalFinal').textContent='Total confirmado: '+moneda(data.total);$('mensaje').textContent='Venta registrada correctamente.';
}catch(e){$('mensaje').textContent=e.message+(pendiente?' Reintentá con la misma operación para consultar o completar su resultado.':'');}
finally{guardando=false;actualizar();}};
$('nueva').onclick=()=>{lineas.clear();confirmada=false;$('comprobante').hidden=true;$('mensaje').textContent='';$('resultados').replaceChildren();$('estadoBusqueda').textContent='';actualizar();};
})();