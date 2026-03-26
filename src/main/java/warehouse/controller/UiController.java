package warehouse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UiController {

    @GetMapping("/ui")
    @ResponseBody
    public String ui() {
        return """
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Warehouse DOM</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: system-ui, sans-serif; background: #0f1117; color: #e2e8f0; min-height: 100vh; }
  header { background: #1a1d2e; border-bottom: 1px solid #2d3748; padding: 1rem 2rem; display: flex; align-items: center; gap: 1rem; }
  header h1 { font-size: 1.25rem; font-weight: 600; color: #63b3ed; }
  header span { font-size: 0.75rem; background: #2d3748; padding: 2px 8px; border-radius: 999px; color: #a0aec0; }
  .container { max-width: 1200px; margin: 0 auto; padding: 2rem; }
  .stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem; }
  .stat { background: #1a1d2e; border: 1px solid #2d3748; border-radius: 12px; padding: 1.25rem; }
  .stat .label { font-size: 0.75rem; color: #718096; text-transform: uppercase; letter-spacing: 0.05em; }
  .stat .value { font-size: 2rem; font-weight: 700; color: #63b3ed; margin-top: 0.25rem; }
  .tabs { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; }
  .tab { padding: 0.5rem 1.25rem; border-radius: 8px; border: 1px solid #2d3748; background: #1a1d2e; color: #a0aec0; cursor: pointer; font-size: 0.875rem; transition: all 0.15s; }
  .tab.active, .tab:hover { background: #2b6cb0; border-color: #2b6cb0; color: #fff; }
  .panel { display: none; }
  .panel.active { display: block; }
  .warehouse-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 1rem; }
  .warehouse-card { background: #1a1d2e; border: 1px solid #2d3748; border-radius: 12px; overflow: hidden; }
  .warehouse-card .wh-header { background: #2b6cb0; padding: 1rem 1.25rem; display: flex; justify-content: space-between; align-items: center; }
  .warehouse-card .wh-name { font-weight: 600; font-size: 1rem; }
  .warehouse-card .wh-id { font-size: 0.75rem; opacity: 0.8; background: rgba(0,0,0,0.2); padding: 2px 8px; border-radius: 999px; }
  .warehouse-card .wh-meta { padding: 0.75rem 1.25rem; font-size: 0.8rem; color: #718096; border-bottom: 1px solid #2d3748; }
  .warehouse-card .wh-products { padding: 0.75rem 1.25rem; }
  .warehouse-card .prod-count { font-size: 0.8rem; color: #a0aec0; margin-bottom: 0.5rem; }
  .cat-badge { display: inline-block; font-size: 0.7rem; padding: 2px 8px; border-radius: 999px; margin: 2px; }
  .cat-0 { background: #2c5282; color: #90cdf4; }
  .cat-1 { background: #276749; color: #9ae6b4; }
  .cat-2 { background: #744210; color: #fbd38d; }
  .cat-3 { background: #702459; color: #fbb6ce; }
  .cat-4 { background: #553c9a; color: #d6bcfa; }
  .cat-5 { background: #2c3e50; color: #a0aec0; }
  table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
  thead tr { background: #1a1d2e; border-bottom: 2px solid #2d3748; }
  th { padding: 0.75rem 1rem; text-align: left; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: #718096; }
  tbody tr { border-bottom: 1px solid #1a1d2e; transition: background 0.1s; }
  tbody tr:hover { background: #1a1d2e; }
  td { padding: 0.6rem 1rem; color: #cbd5e0; }
  .qty { font-weight: 600; color: #68d391; }
  .qty.low { color: #fc8181; }
  .qty.medium { color: #f6ad55; }
  .search { width: 100%; padding: 0.6rem 1rem; background: #1a1d2e; border: 1px solid #2d3748; border-radius: 8px; color: #e2e8f0; font-size: 0.875rem; margin-bottom: 1rem; outline: none; }
  .search:focus { border-color: #2b6cb0; }
  .report-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 1rem; }
  .report-card { background: #1a1d2e; border: 1px solid #2d3748; border-radius: 12px; padding: 1.25rem; }
  .report-card h3 { color: #63b3ed; font-size: 0.9rem; margin-bottom: 0.75rem; }
  .report-card p { color: #a0aec0; font-size: 0.85rem; line-height: 1.6; white-space: pre-wrap; }
  .btn { padding: 0.5rem 1.25rem; background: #2b6cb0; border: none; border-radius: 8px; color: #fff; cursor: pointer; font-size: 0.875rem; transition: background 0.15s; }
  .btn:hover { background: #2c5282; }
  .btn:disabled { opacity: 0.5; cursor: not-allowed; }
  .loading { color: #718096; font-size: 0.875rem; padding: 2rem; text-align: center; }
  .error { color: #fc8181; font-size: 0.875rem; padding: 1rem; }
  .filter-row { display: flex; gap: 0.5rem; margin-bottom: 1rem; flex-wrap: wrap; }
  .filter-btn { padding: 0.3rem 0.75rem; border-radius: 999px; border: 1px solid #2d3748; background: #1a1d2e; color: #a0aec0; cursor: pointer; font-size: 0.75rem; }
  .filter-btn.active { background: #2b6cb0; border-color: #2b6cb0; color: #fff; }
  #product-table-wrap { background: #1a1d2e; border: 1px solid #2d3748; border-radius: 12px; overflow: hidden; }
</style>
</head>
<body>
<header>
  <h1>Warehouse DOM</h1>
  <span id="status">Laden...</span>
</header>
<div class="container">
  <div class="stats">
    <div class="stat"><div class="label">Lagerstandorte</div><div class="value" id="s-wh">-</div></div>
    <div class="stat"><div class="label">Produkte gesamt</div><div class="value" id="s-prod">-</div></div>
    <div class="stat"><div class="label">Kategorien</div><div class="value" id="s-cat">-</div></div>
    <div class="stat"><div class="label">Kritisch (&lt;50)</div><div class="value" id="s-low" style="color:#fc8181">-</div></div>
  </div>
  <div class="tabs">
    <button class="tab active" onclick="showTab('warehouses')">Lagerstandorte</button>
    <button class="tab" onclick="showTab('products')">Alle Produkte</button>
    <button class="tab" onclick="showTab('reports')">KI Berichte</button>
  </div>

  <div id="tab-warehouses" class="panel active">
    <div class="warehouse-grid" id="wh-grid"><div class="loading">Lade Lagerstandorte...</div></div>
  </div>

  <div id="tab-products" class="panel">
    <input class="search" id="prod-search" placeholder="Produkt suchen..." oninput="filterProducts()">
    <div class="filter-row" id="cat-filters"></div>
    <div id="product-table-wrap">
      <table>
        <thead><tr><th>Produkt</th><th>Kategorie</th><th>Lagerstandort</th><th>Menge</th><th>Einheit</th><th>Preis</th></tr></thead>
        <tbody id="prod-tbody"></tbody>
      </table>
    </div>
  </div>

  <div id="tab-reports" class="panel">
    <div style="display:flex;gap:0.75rem;margin-bottom:1.5rem;flex-wrap:wrap">
      <button class="btn" onclick="loadReport('stock','report-stock')">Lagerbestand analysieren</button>
      <button class="btn" onclick="loadReport('lowstock','report-low')">Kritische Bestände</button>
      <button class="btn" onclick="loadReport('value','report-value')">Lagerwert</button>
      <button class="btn" onclick="loadAllReports()">Alle laden</button>
    </div>
    <div class="report-grid">
      <div class="report-card"><h3>Lagerbestand Top 15</h3><p id="report-stock" style="color:#718096">Klicke 'Lagerbestand analysieren'</p></div>
      <div class="report-card"><h3>Kritische Bestände</h3><p id="report-low" style="color:#718096">Klicke 'Kritische Bestände'</p></div>
      <div class="report-card"><h3>Lagerwert pro Standort</h3><p id="report-value" style="color:#718096">Klicke 'Lagerwert'</p></div>
    </div>
  </div>
</div>
<script>
const CATS = ['Getränke','Waschmittel','Tierfutter','Reinigung','Lebensmittel','Hygiene'];
let allProducts = [], activeCategory = null;

function showTab(name) {
  document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.getElementById('tab-' + name).classList.add('active');
  event.target.classList.add('active');
}

async function init() {
  try {
    const [whs, prods] = await Promise.all([
      fetch('/warehouse').then(r => r.json()),
      fetch('/product').then(r => r.json())
    ]);
    allProducts = prods.map(p => ({...p, warehouseName: ''}));

    document.getElementById('s-wh').textContent = whs.length;
    document.getElementById('s-prod').textContent = prods.length;
    const cats = new Set(prods.map(p => p.productCategory));
    document.getElementById('s-cat').textContent = cats.size;
    document.getElementById('s-low').textContent = prods.filter(p => p.productQuantity < 50).length;
    document.getElementById('status').textContent = whs.length + ' Standorte aktiv';

    renderWarehouses(whs);
    buildProductTable(whs, prods);
    buildCatFilters();
  } catch(e) {
    document.getElementById('status').textContent = 'Fehler beim Laden';
  }
}

function renderWarehouses(whs) {
  const grid = document.getElementById('wh-grid');
  grid.innerHTML = whs.map(wh => {
    const cats = {};
    (wh.productData || []).forEach(p => { cats[p.productCategory] = (cats[p.productCategory]||0)+1; });
    const badges = Object.entries(cats).map(([cat,n], i) =>
      `<span class="cat-badge cat-${i%6}">${cat} (${n})</span>`).join('');
    return `<div class="warehouse-card">
      <div class="wh-header">
        <span class="wh-name">${wh.warehouseName}</span>
        <span class="wh-id">${wh.warehouseID}</span>
      </div>
      <div class="wh-meta">${wh.warehouseCity} ${wh.warehousePostalCode} &bull; ${wh.warehouseCountry}</div>
      <div class="wh-products">
        <div class="prod-count">${(wh.productData||[]).length} Produkte</div>
        ${badges}
      </div>
    </div>`;
  }).join('');
}

function buildProductTable(whs, prods) {
  const whMap = {};
  whs.forEach(wh => (wh.productData||[]).forEach(p => { whMap[p.productID] = wh.warehouseName; }));
  allProducts = prods.map(p => ({...p, warehouseName: whMap[p.productID] || '-'}));
  renderProductTable(allProducts);
}

function renderProductTable(prods) {
  const tbody = document.getElementById('prod-tbody');
  tbody.innerHTML = prods.slice(0,500).map(p => {
    const q = p.productQuantity;
    const cls = q < 50 ? 'low' : q < 100 ? 'medium' : '';
    return `<tr>
      <td>${p.productName}</td>
      <td>${p.productCategory}</td>
      <td>${p.warehouseName}</td>
      <td class="qty ${cls}">${q}</td>
      <td>${p.productUnit||'-'}</td>
      <td>€${(p.productPrice||0).toFixed(2)}</td>
    </tr>`;
  }).join('');
}

function buildCatFilters() {
  const row = document.getElementById('cat-filters');
  row.innerHTML = '<button class="filter-btn active" onclick="setCategory(null,this)">Alle</button>' +
    CATS.map(c => `<button class="filter-btn" onclick="setCategory('${c}',this)">${c}</button>`).join('');
}

function setCategory(cat, el) {
  activeCategory = cat;
  document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
  el.classList.add('active');
  filterProducts();
}

function filterProducts() {
  const q = document.getElementById('prod-search').value.toLowerCase();
  let res = allProducts;
  if (activeCategory) res = res.filter(p => p.productCategory === activeCategory);
  if (q) res = res.filter(p => p.productName.toLowerCase().includes(q) || p.productCategory.toLowerCase().includes(q));
  renderProductTable(res);
}

async function loadReport(endpoint, elId) {
  const el = document.getElementById(elId);
  el.textContent = 'Lade KI-Bericht...';
  try {
    const data = await fetch('/report/' + endpoint).then(r => r.json());
    el.textContent = data.report;
  } catch(e) { el.textContent = 'Fehler: ' + e.message; }
}

async function loadAllReports() {
  loadReport('stock','report-stock');
  loadReport('lowstock','report-low');
  loadReport('value','report-value');
}

init();
</script>
</body>
</html>
""";
    }
}
