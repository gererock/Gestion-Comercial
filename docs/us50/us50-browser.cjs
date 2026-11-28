const {spawn}=require('node:child_process');
const fs=require('node:fs');
const os=require('node:os');
const path=require('node:path');
const crypto=require('node:crypto');
const assert=require('node:assert/strict');
const base='http://127.0.0.1:18750';
const report='/admin/productos-mas-vendidos.html';
const root=process.cwd();
const profile=fs.mkdtempSync(path.join(root,'work','chrome-us50-'));
let chrome, socket;
const wait=ms=>new Promise(r=>setTimeout(r,ms));
async function until(fn, attempts=80) {
  for(let i=0;i<attempts;i++) {
    try { const v=await fn(); if(v) return v; } catch {}
    await wait(250);
  }
  throw Error('Tiempo de espera agotado');
}
function jwt(role,expired=false) {
  const [id,sub]=process.env['US50_'+role].split('|');
  const t=Math.floor(Date.now()/1000);
  const encode=o=>Buffer.from(JSON.stringify(o)).toString('base64url');
  const body=encode({alg:'HS256'})+'.'+encode({sub,idUsuario:Number(id),rol:role,iat:t-120,exp:t+(expired?-60:600)});
  return body+'.'+crypto.createHmac('sha256',process.env.APP_JWT_SECRET).update(body).digest('base64url');
}
(async()=>{
  await until(async()=> (await fetch(base+report)).status===200, 280);
  chrome=spawn('C:/Program Files/Google/Chrome/Application/chrome.exe',['--headless=new','--disable-gpu','--remote-debugging-port=0','--user-data-dir='+profile,'about:blank'],{windowsHide:true,stdio:'ignore'});
  const port=await until(()=> fs.readFileSync(path.join(profile,'DevToolsActivePort'),'utf8').split(/\r?\n/)[0]);
  const tabs=await (await fetch('http://127.0.0.1:'+port+'/json')).json();
  socket=new WebSocket(tabs.find(t=>t.type==='page').webSocketDebuggerUrl);
  await new Promise((ok,fail)=>{socket.onopen=ok;socket.onerror=fail;});
  const pending=new Map(), methods=new Map(), calls=[], errors=[];
  let seq=0, simulated=null;
  socket.onmessage=({data})=>{
    const m=JSON.parse(data);
    if(m.id){const p=pending.get(m.id); pending.delete(m.id); m.error?p[1](Error(m.error.message)):p[0](m.result);}
    if(m.method==='Network.requestWillBeSent') methods.set(m.params.requestId,m.params.request.method);
    if(m.method==='Network.responseReceived'){
      const {requestId,response}=m.params;
      if(response.url.startsWith(base+'/api/')) {
        const record=[methods.get(requestId),response.url.slice(base.length),response.status];
        calls.push(record); console.log(...record);
      }
    }
    if(m.method==='Fetch.requestPaused'){
      const {requestId}=m.params;
      const status=simulated;
      simulated=null;
      if(status) {
        const body=Buffer.from(JSON.stringify({message:'Error simulado '+status})).toString('base64');
        void c('Fetch.fulfillRequest',{requestId,responseCode:status,body,responseHeaders:[{name:'Content-Type',value:'application/json'}]});
      } else {
        void c('Fetch.continueRequest',{requestId});
      }
    }
    if(m.method==='Runtime.exceptionThrown') errors.push(m.params.exceptionDetails.text);
  };
  const c=(method,params={})=>new Promise((ok,fail)=>{
    pending.set(++seq,[ok,fail]);socket.send(JSON.stringify({id:seq,method,params}));
  });
  const ev=async(expression)=> (await c('Runtime.evaluate',{expression,returnByValue:true})).result.value;
  const navigate=async(url)=> {await c('Page.navigate',{url:base+url});await until(()=>ev('location.pathname === '+ JSON.stringify(url.split('?')[0])+' && document.readyState === "complete"'));};
  const init=async(role,expired=false)=>{
    if(role) await navigate('/login/index.html');
    if(role) await ev("sessionStorage.setItem('auth_token',"+JSON.stringify(jwt(role,expired))+");sessionStorage.setItem('auth_rol',"+JSON.stringify(role)+");sessionStorage.setItem('auth_usuario',JSON.stringify({nombre:'QA'}));");
  };
  const checkCall=async(start,expected,code=200)=>until(()=>calls.slice(start).find(([m,url,status])=>m==='GET'&&url.includes(expected)&&status===code));
  const click=(selector)=>ev('document.querySelector('+JSON.stringify(selector)+').click()');
  const visible=(id)=>ev('!!document.querySelector('+JSON.stringify(id)+').getClientRects().length');
  await c('Page.enable');await c('Runtime.enable');await c('Network.enable');
  for(const [role,destination] of [[null,'/login/index.html'],['VENDEDOR','/vendedor/index.html'],['CLIENTE','/cliente/index.html']]){
    await init(role);
    const start=calls.length;
    await c('Page.navigate',{url:base+report});
    await until(()=>ev('location.pathname === '+JSON.stringify(destination)+' && document.readyState === "complete"'));
    await wait(200);
    assert(!calls.slice(start).some(([,u])=>u.startsWith('/api/admin/productos-mas-vendidos')));
    console.log('ACCESO',role||'VISITANTE',destination);
  }
  await init('ADMINISTRADOR',true);
  let start=calls.length;
  await c('Page.navigate',{url:base+report});
  await checkCall(start,'periodo=DIA',401);
  await until(()=>ev("location.search === '?sesion=expirada' && document.querySelector('#login-message')?.innerText.toLowerCase().includes('expir')"));
  console.log('SESION VENCIDA redirige con aviso');
  await navigate('/login/index.html');
  const [,email]=process.env.US50_ADMINISTRADOR.split('|');
  await ev(`document.querySelector('#email').value=${JSON.stringify(email)};
            document.querySelector('#password').value=${JSON.stringify(process.env.US50_ADMIN_LOGIN)};
            document.querySelector('#login-form').requestSubmit();`);
  await until(()=>ev("location.pathname === '/admin/index.html'"));
  assert(calls.some(([m,u,status])=>m==='POST'&&u==='/api/auth/login'&&status===200));
  console.log('LOGIN ADMINISTRADOR por formulario confirmado');
  calls.length=0;
  start=calls.length;
  await click('a[href="'+report+'"]');
  await checkCall(start,'periodo=DIA');
  await until(()=>visible('#contenedorTabla'));
  console.log('ACCESO panel -> reporte, ranking visible');
  for(const periodo of ['SEMANA','MES']){
    start=calls.length;
    await click('[data-periodo="'+periodo+'"]');
    await checkCall(start,'periodo='+periodo);
    await until(()=>visible('#contenedorTabla'));
    console.log('PERIODO',periodo,'actualizado');
  }
  await click('[data-periodo="RANGO"]');
  assert(await visible('#fechaDesde'));
  const rango=(desde,hasta)=>ev(`document.querySelector('#fechaDesde').value='${desde}'; document.querySelector('#fechaHasta').value='${hasta}'; document.querySelector('#formRango').requestSubmit();`);
  start=calls.length;
  await rango('2026-11-27','2026-11-26');
  await until(()=>visible('#mensaje'));
  await wait(250);
  assert.equal(calls.length,start);
  console.log('RANGO inverso muestra validacion y no hace GET');
  const cordoba=new Intl.DateTimeFormat('sv-SE',{timeZone:'America/Argentina/Cordoba'}).format(new Date());
  start=calls.length;
  await rango(cordoba,cordoba);
  await checkCall(start,'periodo=RANGO');
  await until(()=>visible('#contenedorTabla'));
  await ev("document.querySelector('#fechaHasta').value='1900-01-01'; document.querySelector('#fechaHasta').dispatchEvent(new Event('input',{bubbles:true}));");
  await until(async()=>!(await visible('#contenedorTabla')));
  console.log('RANGO editado oculta el resultado anterior');
  start=calls.length;
  await rango('1900-01-01','1900-01-01');
  await checkCall(start,'periodo=RANGO');
  await until(()=>visible('#sinResultados'));
  assert(!(await visible('#contenedorTabla')));
  assert(await visible('#fechaDesde'));
  console.log('RANGO vacio, tabla anterior oculta y fechas disponibles');
  start=calls.length;
  await click('#btnLimpiarFiltros');
  await checkCall(start,'periodo=DIA');
  assert.equal(await ev("document.querySelector('#fechaDesde').value + document.querySelector('#fechaHasta').value"),'');
  console.log('LIMPIAR reinicia DIA y fechas');
  start=calls.length;
  await c('Page.reload');
  await checkCall(start,'periodo=DIA');
  await until(()=>visible('#contenedorTabla'));
  console.log('REFRESH repite DIA y muestra ranking');
  const rows=await ev("Array.from(document.querySelectorAll('#tablaProductos tr'),tr=>tr.innerText)");
  assert.equal(rows.filter(x=>x.includes(process.env.US50_TAG)).length,4);
  assert(rows.some(x=>x.includes('Sin categoría')&&x.includes('Sin marca')));
  console.log('TABLA cuatro filas QA distintas, valores nulos visibles');
  const data=await ev("Array.from(document.querySelectorAll('#tablaProductos tr'),tr=>Array.from(tr.cells,td=>td.innerText.trim()))");
  for(const [pos,id,name,category,brand,quantity,total,action] of data.filter(row=>row[2].startsWith(process.env.US50_TAG))){
    assert(/^[1-9]\d*$/.test(pos) && /^[1-9]\d*$/.test(id));
    assert(name.includes(process.env.US50_TAG));
    assert(category.includes(process.env.US50_TAG)||category==='Sin categoría');
    assert(brand.includes(process.env.US50_TAG)||brand==='Sin marca');
    assert.equal(quantity,'4');
    assert(total.includes('40,00'));
    assert.equal(action,'Ver información');
  }
  console.log('CAMPOS RANKING: posicion, ID, nombre, categoria, marca, 4 unidades, 40,00 y accion por fila QA');
  for(const estado of ['ACTIVO','PAUSADO','DISCONTINUADO']){
    start=calls.length;
    const match=process.env.US50_TAG+'-'+estado;
    const clicked=await ev(`(()=>{const row=[...document.querySelectorAll('#tablaProductos tr')].find(tr=>tr.innerText.includes(${JSON.stringify(match)})); const button=row?.querySelector('button'); button?.click(); return !!button;})()`);
    assert(clicked);
    const detail=await checkCall(start,'/api/productos/');
    await until(()=>visible('#contenidoDetalle'));
    const fields=await ev("Object.fromEntries(['Id','Nombre','Categoria','Marca','Precio','StockActual','Estado'].map(k=>[k,document.querySelector('#detalle'+k).innerText]))");
    assert(fields.Nombre.includes(match));
    assert(fields.Id.includes(detail[1].split('/').at(-1)));
    assert(fields.Categoria.includes(process.env.US50_TAG));
    assert(fields.Marca.includes(process.env.US50_TAG));
    assert(fields.Precio.includes('20,75'));
    assert(fields.StockActual.includes('18'));
    assert(fields.Estado.toUpperCase().includes(estado));
    assert(await visible('#btnCerrarModal'));
    await click('#btnCerrarModal');
    await until(async()=>!(await visible('#modalProducto')));
    console.log('DETALLE coincide con fixture',estado);
  }
  for(const [width,height] of [[1366,768],[768,1024],[390,844],[430,932]]){
    await c('Emulation.setDeviceMetricsOverride',{width,height,deviceScaleFactor:1,mobile:false});
    assert(await ev('document.documentElement.scrollWidth <= window.innerWidth && document.querySelector("h1").getBoundingClientRect().width > 0'));
    assert(await visible('#btnLimpiarFiltros'));
    await ev('window.scrollTo(0, 0)');
    const capture=await c('Page.captureScreenshot',{format:'png',captureBeyondViewport:false});
    fs.writeFileSync(path.join(root,'work','Gestion-Comercial','docs','us50',`ranking-${width}x${height}.png`),Buffer.from(capture.data,'base64'));
    const layout=await ev("Array.from(document.querySelector('#tablaProductos tr').cells,el=>({visible:!!el.getClientRects().length,text:el.innerText.trim()}))");
    assert(layout.every(cell=>cell.visible&&cell.text));
    start=calls.length;
    await click('#tablaProductos tr button');
    await checkCall(start,'/api/productos/');
    await until(()=>visible('#contenidoDetalle'));
    assert(await visible('#btnCerrarModal'));
    assert(await ev("(()=>{const b=document.querySelector('#btnCerrarModal'),r=b.getBoundingClientRect();return r.left>=0&&r.right<=innerWidth&&r.top>=0&&r.bottom<=innerHeight&&b.contains(document.elementFromPoint(r.x+r.width/2,r.y+r.height/2));})()"));
    const modal=await c('Page.captureScreenshot',{format:'png',captureBeyondViewport:false});
    fs.writeFileSync(path.join(root,'work','Gestion-Comercial','docs','us50',`modal-${width}x${height}.png`),Buffer.from(modal.data,'base64'));
    await click('#btnCerrarModal');
    await until(async()=>!(await visible('#modalProducto')));
    console.log('LAYOUT',width,height,'tabla, modal y cierre disponibles sin desborde de pagina');
  }
  await c('Emulation.clearDeviceMetricsOverride');
  await c('Fetch.enable',{patterns:[{urlPattern:'*/api/admin/productos-mas-vendidos*'}]});
  for(const status of [400,403,404,500]){
    start=calls.length;
    simulated=status;
    await click('[data-periodo="SEMANA"]');
    await checkCall(start,'periodo=SEMANA',status);
    await until(()=>visible('#mensaje'));
    assert(!(await visible('#contenedorTabla')));
    console.log('SIMULADO HTTP',status,'mensaje visible y tabla anterior oculta');
    start=calls.length;
    await click('[data-periodo="DIA"]');
    await checkCall(start,'periodo=DIA');
    await until(()=>visible('#contenedorTabla'));
    console.log('RECUPERACION backend real DIA 200');
  }
  await c('Fetch.disable');
  const back=await ev("[...document.links].find(a=>a.textContent.includes('Volver al panel'))?.getAttribute('href')");
  assert.equal(back,'/admin/index.html');
  await click('a[href="/admin/index.html"]');
  await until(()=>ev("location.pathname === '/admin/index.html'"));
  assert(calls.every(([m])=>m==='GET'));
  assert.deepEqual(errors,[]);
  console.log('VOLVER AL PANEL confirmado');
  console.log('Consola Runtime.exceptionThrown: 0 en flujos ejecutados');
})().catch(e=>{console.error(e.message);process.exitCode=1;}).finally(()=>{socket?.close();chrome?.kill();});




