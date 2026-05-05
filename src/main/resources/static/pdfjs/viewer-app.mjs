const params = new URLSearchParams(window.location.search);
const pdfUrl = params.get('file');

function applyTheme(theme) {
  if (theme === 'light' || theme === 'dark') {
    document.documentElement.setAttribute('data-theme', theme);
  }
}

const themeParam = params.get('theme');
if (themeParam) {
  applyTheme(themeParam);
}

window.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'pdf-theme') {
    applyTheme(event.data.theme);
  }
});

const toolbar        = document.getElementById('toolbar');
const btnPrevPage    = document.getElementById('btnPrevPage');
const btnNextPage    = document.getElementById('btnNextPage');
const pageInput      = document.getElementById('pageInput');
const pageTotal      = document.getElementById('pageTotal');
const btnZoomOut     = document.getElementById('btnZoomOut');
const btnZoomIn      = document.getElementById('btnZoomIn');
const zoomDisplay    = document.getElementById('zoomDisplay');
const scalePopup     = document.getElementById('scalePopup');
const btnPageWidth   = document.getElementById('btnPageWidth');
const btnPageFit     = document.getElementById('btnPageFit');
const btnDownload    = document.getElementById('btnDownload');
const btnFullscreen  = document.getElementById('btnFullscreen');
const loadingOverlay = document.getElementById('loadingOverlay');
const errorOverlay   = document.getElementById('errorOverlay');
const errorMessage   = document.getElementById('errorMessage');
const viewerContainer = document.getElementById('viewerContainer');

function showError(msg) {
  loadingOverlay.classList.add('hidden');
  toolbar.style.display = 'none';
  errorMessage.textContent = msg;
  errorOverlay.classList.remove('hidden');
}

if (!pdfUrl) {
  showError('未指定 PDF 文件');
  throw new Error('No PDF file specified');
}

await import('./build/pdf.min.mjs');
const pdfjsLib = globalThis.pdfjsLib;

pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
  './build/pdf.worker.min.mjs',
  window.location.href
).href;

const { PDFViewer, PDFLinkService, EventBus } = await import('./web/pdf_viewer.mjs');

const container  = document.getElementById('viewerContainer');
const eventBus   = new EventBus();
const linkService = new PDFLinkService({ eventBus });
const viewer = new PDFViewer({ container, eventBus, linkService });
linkService.setViewer(viewer);
linkService.setHistory({ replace: () => {}, push: () => {} });

let totalPages   = 0;
let currentPage  = 1;
let currentScale = 1;

function updatePageUI() {
  currentPage = viewer.currentPageNumber || 1;
  totalPages  = viewer.pagesCount || 0;

  pageInput.value     = currentPage;
  pageInput.max       = totalPages;
  pageTotal.textContent = '/ ' + totalPages;

  const canPrev = currentPage > 1;
  const canNext = currentPage < totalPages;
  btnPrevPage.disabled = !canPrev;
  btnNextPage.disabled = !canNext;
}

function goToPage(page) {
  page = Math.max(1, Math.min(totalPages, page));
  if (viewer.currentPageNumber !== page) {
    viewer.currentPageNumber = page;
  }
}

btnPrevPage.addEventListener('click', () => goToPage(currentPage - 1));
btnNextPage.addEventListener('click', () => goToPage(currentPage + 1));

pageInput.addEventListener('change', () => {
  const v = parseInt(pageInput.value, 10);
  if (!isNaN(v) && v >= 1) goToPage(v);
  else pageInput.value = currentPage;
});

pageInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') { pageInput.blur(); }
  if (e.key === 'ArrowUp')   { e.preventDefault(); goToPage(currentPage + 1); }
  if (e.key === 'ArrowDown') { e.preventDefault(); goToPage(currentPage - 1); }
});

const ZOOM_STEPS = [0.1, 0.25, 0.5, 0.75, 1, 1.25, 1.5, 2, 3, 4];
const ZOOM_NAMES = { 'page-width': '适合宽度', 'page-fit': '适合页面', 'auto': '自动' };

function formatScale(scale) { return Math.round(scale * 100) + '%'; }

function formatScaleDisplay() {
  const sv = viewer.currentScaleValue;
  if (ZOOM_NAMES[sv]) return ZOOM_NAMES[sv];
  return formatScale(viewer.currentScale || 1);
}

function updateZoomUI() {
  currentScale = viewer.currentScale || 1;
  zoomDisplay.textContent = formatScaleDisplay();
}

function zoomIn() {
  const cur = viewer.currentScale || 1;
  const next = ZOOM_STEPS.find(s => s > cur + 0.001) || cur * 1.25;
  viewer.currentScale = next;
}

function zoomOut() {
  const cur = viewer.currentScale || 1;
  const rev  = [...ZOOM_STEPS].reverse();
  const next = rev.find(s => s < cur - 0.001) || cur * 0.8;
  viewer.currentScale = Math.max(0.1, next);
}

btnZoomIn.addEventListener('click', zoomIn);
btnZoomOut.addEventListener('click', zoomOut);

zoomDisplay.addEventListener('click', (e) => {
  e.stopPropagation();
  scalePopup.classList.toggle('show');
  scalePopup.querySelectorAll('.scale-popup-item').forEach(item => {
    const val = item.dataset.scale;
    item.classList.toggle('active',
      viewer.currentScaleValue === val ||
      (val === String(currentScale) && viewer.currentScaleValue === 'auto') ||
      (val === '1' && Math.abs(currentScale - 1) < 0.01 && viewer.currentScaleValue === 'auto')
    );
  });
});

scalePopup.querySelectorAll('.scale-popup-item').forEach(item => {
  item.addEventListener('click', (e) => {
    e.stopPropagation();
    const val = item.dataset.scale;
    if (val === 'page-width' || val === 'page-fit') {
      viewer.currentScaleValue = val;
    } else {
      viewer.currentScale = parseFloat(val);
    }
    scalePopup.classList.remove('show');
  });
});

document.addEventListener('click', () => scalePopup.classList.remove('show'));

btnPageWidth.addEventListener('click', () => { viewer.currentScaleValue = 'page-width'; });
btnPageFit.addEventListener('click',   () => { viewer.currentScaleValue = 'page-fit'; });

btnDownload.addEventListener('click', () => {
  const a = document.createElement('a');
  a.href = pdfUrl;
  a.download = '';
  a.target = '_blank';
  a.rel = 'noopener';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
});

btnFullscreen.addEventListener('click', () => {
  if (!document.fullscreenElement) {
    const el = document.documentElement;
    if (el.requestFullscreen) el.requestFullscreen();
    else if (el.webkitRequestFullscreen) el.webkitRequestFullscreen();
  } else {
    if (document.exitFullscreen) document.exitFullscreen();
    else if (document.webkitExitFullscreen) document.webkitExitFullscreen();
  }
});

eventBus.on('pagesloaded', () => {
  if (!viewer.currentScaleValue || viewer.currentScaleValue === 'auto') {
    viewer.currentScaleValue = 'page-width';
  }
  updatePageUI();
  updateZoomUI();
});

eventBus.on('pagechanging', (evt) => {
  currentPage = evt.pageNumber;
  updatePageUI();
});

eventBus.on('scalechanging', (evt) => {
  currentScale = evt.scale;
  updateZoomUI();
});

eventBus.on('updateviewarea', () => {
  updatePageUI();
  updateZoomUI();
});

document.addEventListener('keydown', (e) => {
  if (e.target.tagName === 'INPUT') return;

  const ctrl = e.ctrlKey || e.metaKey;

  if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
    e.preventDefault();
    goToPage(currentPage + 1);
    return;
  }
  if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
    e.preventDefault();
    goToPage(currentPage - 1);
    return;
  }
  if (e.key === 'Home') { e.preventDefault(); goToPage(1); return; }
  if (e.key === 'End')  { e.preventDefault(); goToPage(totalPages); return; }

  if (ctrl && (e.key === '=' || e.key === '+')) {
    e.preventDefault(); zoomIn(); return;
  }
  if (ctrl && e.key === '-') {
    e.preventDefault(); zoomOut(); return;
  }
  if (ctrl && e.key === '0') {
    e.preventDefault(); viewer.currentScaleValue = 'page-width'; return;
  }

  if (ctrl && e.key === 's') {
    e.preventDefault();
    btnDownload.click();
    return;
  }
});

viewerContainer.addEventListener('wheel', (e) => {
  if (e.ctrlKey || e.metaKey) {
    e.preventDefault();
    if (e.deltaY < 0) zoomIn();
    else zoomOut();
  }
}, { passive: false });

try {
  const loadingTask = pdfjsLib.getDocument({
    url: pdfUrl,
    cMapUrl: new URL('./cmaps/', window.location.href).href,
    cMapPacked: true,
  });
  const pdfDocument = await loadingTask.promise;
  viewer.setDocument(pdfDocument);
  linkService.setDocument(pdfDocument, null);

  updatePageUI();
  updateZoomUI();

  loadingOverlay.classList.add('hidden');
  toolbar.style.display = 'flex';
} catch (err) {
  console.error('PDF loading error:', err);
  showError(err.message || String(err));
}
