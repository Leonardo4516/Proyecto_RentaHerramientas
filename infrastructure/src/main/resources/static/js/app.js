const TOOL_FALLBACK_DATA_URI = "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='600' height='400' viewBox='0 0 600 400'%3E%3Cdefs%3E%3ClinearGradient id='bg' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' stop-color='%231e293b'/%3E%3Cstop offset='100%25' stop-color='%230f172a'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='600' height='400' fill='url(%23bg)'/%3E%3Crect x='20' y='20' width='560' height='360' rx='12' fill='none' stroke='%23334155' stroke-width='2' stroke-dasharray='8 8'/%3E%3Ccircle cx='300' cy='160' r='42' fill='%23334155' opacity='0.5'/%3E%3Cpath d='M280 148 h40 l10 12 h15 a5 5 0 0 1 5 5 v35 a5 5 0 0 1 -5 5 h-70 a5 5 0 0 1 -5 -5 v-35 a5 5 0 0 1 5 -5 h10 z M300 162 a15 15 0 1 0 0 30 a15 15 0 1 0 0 -30' fill='%23f59e0b'/%3E%3Ctext x='300' y='240' text-anchor='middle' fill='%23f8fafc' font-family='system-ui, -apple-system, sans-serif' font-weight='600' font-size='18'%3EEspacio Disponible para Foto%3C/text%3E%3Ctext x='300' y='268' text-anchor='middle' fill='%2394a3b8' font-family='system-ui, -apple-system, sans-serif' font-size='13'%3EAgrega la imagen en assets/img/machinery/%3C/text%3E%3C/svg%3E";
window.TOOL_FALLBACK_IMG = TOOL_FALLBACK_DATA_URI;

function handleToolImageError(img) {
  if (!img) return;
  const currentSrc = img.src || '';
  if (!img.dataset.extAttempt) {
    if (currentSrc.endsWith('.jpg')) {
      img.dataset.extAttempt = 'jpeg';
      img.src = currentSrc.slice(0, -4) + '.jpeg';
      return;
    } else if (currentSrc.endsWith('.jpeg')) {
      img.dataset.extAttempt = 'png';
      img.src = currentSrc.slice(0, -5) + '.png';
      return;
    }
  } else if (img.dataset.extAttempt === 'jpeg') {
    img.dataset.extAttempt = 'png';
    img.src = currentSrc.slice(0, -5) + '.png';
    return;
  }
  img.onerror = null;
  img.src = window.TOOL_FALLBACK_IMG;
}
window.handleToolImageError = handleToolImageError;

function getToolImageUrl(tool) {
  if (!tool) return window.TOOL_FALLBACK_IMG;
  const url = tool.imageUrl || (tool.images && tool.images[0]) || tool.coverImageUrl;
  if (url && typeof url === 'string' && url.trim().length > 0) {
    if (url.endsWith('.svg')) {
      return window.TOOL_FALLBACK_IMG;
    }
    return url.startsWith('/') || url.startsWith('http') ? url : '/' + url;
  }
  return window.TOOL_FALLBACK_IMG;
}
window.getToolImageUrl = getToolImageUrl;

const App = (() => {
  let allTools = [];
  let filteredTools = [];
  let categories = [];
  let selectedCategory = 'TODAS';
  let activeToolForBooking = null;
  let cachedRentals = [];

  async function init() {
    setupEventListeners();
    checkAuthStatus();
    RealtimeManager.init();

    // Fechas por defecto para el widget Hero (mínimo mañana y +6 días)
    initHeroWidgetDates();

    try {
      await Promise.all([loadCategories(), loadTools()]);
    } catch (e) {
      console.warn('Error al cargar catálogo inicial:', e);
    }
  }

  function setupEventListeners() {
    // Buscador en vivo y botón de limpiar
    const searchInput = document.getElementById('search-input');
    const searchClearBtn = document.getElementById('search-clear-btn');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        if (searchClearBtn) {
          searchClearBtn.style.display = e.target.value.trim() ? 'inline-block' : 'none';
        }
        filterTools(e.target.value);
      });
    }
    if (searchClearBtn) {
      searchClearBtn.addEventListener('click', () => {
        if (searchInput) {
          searchInput.value = '';
          searchClearBtn.style.display = 'none';
          filterTools('');
        }
      });
    }

    // Slider de radio de proximidad
    document.getElementById('radius-slider')?.addEventListener('input', (e) => {
      const radius = e.target.value;
      document.getElementById('radius-value').textContent = `${radius} km`;
      const toolsInRadius = MapManager.filterByRadius(radius, allTools);
      renderTools(toolsInRadius);
    });

    // Píldora inicial 'Todas'
    document.querySelectorAll('.category-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('.category-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        selectedCategory = pill.dataset.category;
        filterTools(document.getElementById('search-input')?.value || '');
      });
    });

    // Control de colapso/despliegue del mapa interactivo
    const mapCollapseEl = document.getElementById('map-collapse-container');
    const mapToggleStatus = document.getElementById('map-toggle-status');
    const mapToggleChevron = document.getElementById('map-toggle-chevron');
    if (mapCollapseEl) {
      mapCollapseEl.addEventListener('shown.bs.collapse', () => {
        if (mapToggleStatus) mapToggleStatus.textContent = 'Ocultar mapa';
        if (mapToggleChevron) mapToggleChevron.classList.add('rotate-180');
        if (typeof MapManager !== 'undefined' && MapManager) {
          MapManager.init();
          MapManager.updateToolsOnMap(filteredTools);
          setTimeout(() => {
            if (MapManager.invalidateSize) MapManager.invalidateSize();
          }, 150);
        }
      });
      mapCollapseEl.addEventListener('hidden.bs.collapse', () => {
        if (mapToggleStatus) mapToggleStatus.textContent = 'Ver mapa';
        if (mapToggleChevron) mapToggleChevron.classList.remove('rotate-180');
      });
    }

    // Cambios en fechas de reserva para cotización en vivo
    const bookStartEl = document.getElementById('book-start-date');
    const bookEndEl = document.getElementById('book-end-date');
    if (bookStartEl) {
      bookStartEl.addEventListener('change', () => {
        if (bookEndEl && bookStartEl.value) {
          bookEndEl.min = bookStartEl.value;
          if (bookEndEl.value && bookEndEl.value <= bookStartEl.value) {
            const nextDay = new Date(bookStartEl.value + 'T00:00:00');
            nextDay.setDate(nextDay.getDate() + 1);
            bookEndEl.value = nextDay.toISOString().split('T')[0];
          }
        }
        recalculateBookingQuote();
      });
      bookStartEl.addEventListener('input', recalculateBookingQuote);
    }
    if (bookEndEl) {
      bookEndEl.addEventListener('change', recalculateBookingQuote);
      bookEndEl.addEventListener('input', recalculateBookingQuote);
    }

    // Limpiar estado inválido de dirección de entrega
    document.getElementById('book-delivery-address')?.addEventListener('input', (e) => {
      e.target.classList.remove('is-invalid');
    });

    // Selector de método de pago
    document.querySelectorAll('input[name="paymentMethod"]').forEach(radio => {
      radio.addEventListener('change', (e) => {
        togglePaymentFields(e.target.value);
      });
    });

    // Formateo dinámico y detección de marca de tarjeta de crédito/débito
    const cardNumberInput = document.getElementById('card-number');
    const cardExpiryInput = document.getElementById('card-expiry');
    const cardCvcInput = document.getElementById('card-cvc');
    const cardHolderInput = document.getElementById('card-holder-name');
    const cardBrandIcon = document.getElementById('card-brand-icon');

    if (cardNumberInput) {
      cardNumberInput.addEventListener('input', (e) => {
        let val = e.target.value.replace(/\D/g, '').substring(0, 16);
        let formatted = val.match(/.{1,4}/g)?.join(' ') || val;
        e.target.value = formatted;
        e.target.classList.remove('is-invalid');

        if (cardBrandIcon) {
          cardBrandIcon.className = 'fa sb-card-brand-icon';
          if (/^4/.test(val)) {
            cardBrandIcon.classList.add('fa-cc-visa', 'text-primary');
          } else if (/^(5[1-5]|2[2-7])/.test(val)) {
            cardBrandIcon.classList.add('fa-cc-mastercard', 'text-warning');
          } else if (/^3[47]/.test(val)) {
            cardBrandIcon.classList.add('fa-cc-amex', 'text-info');
          } else {
            cardBrandIcon.classList.add('fa-credit-card', 'text-muted');
          }
        }
      });
    }

    if (cardExpiryInput) {
      cardExpiryInput.addEventListener('input', (e) => {
        let val = e.target.value.replace(/\D/g, '').substring(0, 4);
        if (val.length >= 3) {
          e.target.value = `${val.substring(0, 2)}/${val.substring(2)}`;
        } else {
          e.target.value = val;
        }
        e.target.classList.remove('is-invalid');
      });
    }

    if (cardCvcInput) {
      cardCvcInput.addEventListener('input', (e) => {
        e.target.value = e.target.value.replace(/\D/g, '').substring(0, 4);
        e.target.classList.remove('is-invalid');
      });
    }

    if (cardHolderInput) {
      cardHolderInput.addEventListener('input', (e) => {
        e.target.classList.remove('is-invalid');
      });
    }

    const pseBankSelect = document.getElementById('pse-bank');
    const pseEmailInput = document.getElementById('pse-email');
    if (pseBankSelect) {
      pseBankSelect.addEventListener('change', () => pseBankSelect.classList.remove('is-invalid'));
    }
    if (pseEmailInput) {
      pseEmailInput.addEventListener('input', () => pseEmailInput.classList.remove('is-invalid'));
    }

    // Campos de inspección de devolución
    document.getElementById('return-horometro')?.addEventListener('input', (e) => {
      e.target.classList.remove('is-invalid');
    });
    document.getElementById('damage-cost')?.addEventListener('input', (e) => {
      e.target.classList.remove('is-invalid');
    });

    // Suscripción a eventos WebSocket para refresco en tiempo real
    RealtimeManager.onNotification((notif) => {
      if (notif.channel === 'Catálogo') {
        loadTools();
      }
      const user = API.getUser();
      if (user && (user.role === 'ROLE_CLIENTE' || user.role === 'ROLE_ADMIN')) {
        loadClientRentals();
      }
      if (user && (user.role === 'ROLE_PROVEEDOR' || user.role === 'ROLE_ADMIN')) {
        loadProviderRequests();
      }
    });
  }

  async function loadCategories() {
    try {
      const data = await API.tools.getCategories();
      if (Array.isArray(data) && data.length > 0) {
        categories = data;
        renderCategoryPills(categories);
        renderCategoryDirectory(categories);
        populateAddToolCategories(categories);
        enrichToolsWithCategories();
      }
    } catch (e) {
      console.warn('Error cargando categorías desde la base de datos:', e);
    }
  }

  function enrichToolsWithCategories() {
    if (!categories || categories.length === 0 || !allTools || allTools.length === 0) return;
    allTools.forEach(t => {
      const cat = categories.find(c => c.id === t.categoryId);
      if (cat) {
        t.categoryName = cat.name;
      }
    });
  }

  function populateAddToolCategories(cats) {
    const select = document.getElementById('add-tool-category');
    if (!select || !cats || cats.length === 0) return;
    select.innerHTML = cats.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  }

  function renderCategoryDirectory(cats) {
    const grid = document.getElementById('sb-category-grid');
    if (!grid) return;

    const catImageMap = {
      'Excavación y Movimiento de Tierras': 'assets/img/machinery/Retroexcavadora-Oruga-CAT-320D.png',
      'Concreto y Mezclado': 'assets/img/machinery/mezcladora-diesel-380l.jpg',
      'Trabajo en Altura y Andamios': 'assets/img/machinery/plataforma-tijera.jpg',
      'Demolición y Rotura': 'assets/img/machinery/taladro-demoledor-bosch.jpeg',
      'Generación de Energía y Plantas Eléctricas': 'assets/img/machinery/planta-electrica-cummins.jpg',
      'Corte y Perforación de Suelos': 'assets/img/machinery/cortadora-pavimento.jpg',
      'Compactación de Terreno': 'assets/img/machinery/rodillo-compactador.jpg',
      'Bombeo y Evacuación de Agua': 'assets/img/machinery/motobomba-lodos.jpg',
      'Compresores y Aire Comprimido': 'assets/img/machinery/compresor-aire.jpg',
      'Elevación y Montacargas': 'assets/img/machinery/malacate-electrico.jpg',
      'Soldadura Industrial': 'assets/img/machinery/motosoldadora-miller.jpg',
      'Iluminación de Obras Nocturnas': 'assets/img/machinery/torre-iluminacion-generac.jpg',
      'Topografía y Medición Láser': 'assets/img/machinery/estacion-total.jpg',
      'Pintura y Recubrimientos Airless': 'assets/img/machinery/equipo-pintura-airless.jpg',
      'Limpieza e Hidrolavado Industrial': 'assets/img/machinery/hidrolavadora-karcher.jpg',
      'Jardinería y Descapote de Terreno': 'assets/img/machinery/compactadora-canguro.jpg',
      'Formaletas y Encofrados Metálicos': 'assets/img/machinery/andamio-multidireccional.jpg',
      'Herramienta Eléctrica de Taller': 'assets/img/machinery/martillo-rompedor-hilti.jpg',
      'Carga y Transporte Ligero': 'assets/img/machinery/minicargador-bobcat.jpeg',
      'Seguridad y Señalización Vial': 'assets/img/machinery/retroexcavadora-cat-420f2.jpg'
    };

    grid.innerHTML = cats.map(cat => {
      const imgUrl = catImageMap[cat.name] || 'assets/img/machinery/Retroexcavadora-Oruga-CAT-320D.png';
      return `
        <div class="sb-category-card" onclick="App.filterByCategory('${cat.name}')">
          <img src="${imgUrl}" alt="${cat.name}" class="sb-category-thumb" onerror="handleToolImageError(this)">
          <div class="sb-category-name">${cat.name}</div>
        </div>
      `;
    }).join('');
  }

  function filterByCategory(catName) {
    selectedCategory = catName;
    const container = document.getElementById('category-pills-container');
    if (container) {
      container.querySelectorAll('.category-pill').forEach(p => {
        if (p.dataset.category && p.dataset.category.toLowerCase() === catName.toLowerCase()) {
          p.classList.add('active');
        } else {
          p.classList.remove('active');
        }
      });
    }
    filterTools(document.getElementById('search-input')?.value || '');
    const catalogSec = document.getElementById('section-catalog');
    if (catalogSec) {
      catalogSec.scrollIntoView({ behavior: 'smooth' });
    }
  }

  function renderCategoryPills(cats) {
    const container = document.getElementById('category-pills-container');
    if (!container) return;

    const iconMap = {
      'Excavación y Movimiento de Tierras': 'fa-truck-pickup',
      'Concreto y Mezclado': 'fa-cube',
      'Trabajo en Altura y Andamios': 'fa-layer-group',
      'Demolición y Rotura': 'fa-hammer',
      'Generación de Energía y Plantas Eléctricas': 'fa-bolt',
      'Corte y Perforación de Suelos': 'fa-circle-notch',
      'Compactación de Terreno': 'fa-compress-arrows-alt',
      'Bombeo y Evacuación de Agua': 'fa-water',
      'Compresores y Aire Comprimido': 'fa-wind',
      'Elevación y Montacargas': 'fa-arrow-up',
      'Soldadura Industrial': 'fa-fire',
      'Iluminación de Obras Nocturnas': 'fa-lightbulb',
      'Topografía y Medición Láser': 'fa-crosshairs',
      'Pintura y Recubrimientos Airless': 'fa-paint-roller',
      'Limpieza e Hidrolavado Industrial': 'fa-shield-alt',
      'Jardinería y Descapote de Terreno': 'fa-cut',
      'Formaletas y Encofrados Metálicos': 'fa-th-large',
      'Herramienta Eléctrica de Taller': 'fa-tools',
      'Carga y Transporte Ligero': 'fa-truck',
      'Seguridad y Señalización Vial': 'fa-exclamation-triangle'
    };

    let html = `<div class="category-pill ${selectedCategory === 'TODAS' ? 'active' : ''}" data-category="TODAS">
      <i class="fa fa-layer-group me-1"></i> Todas
    </div>`;

    cats.forEach(cat => {
      const icon = iconMap[cat.name] || 'fa-tag';
      const isActive = selectedCategory.toLowerCase() === cat.name.toLowerCase();
      html += `
        <div class="category-pill ${isActive ? 'active' : ''}" data-category="${cat.name}" data-id="${cat.id}">
          <i class="fa ${icon} me-1"></i> ${cat.name}
        </div>`;
    });

    container.innerHTML = html;

    container.querySelectorAll('.category-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        container.querySelectorAll('.category-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        selectedCategory = pill.dataset.category;
        filterTools(document.getElementById('search-input')?.value || '');
      });
    });
  }

  async function loadTools() {
    try {
      allTools = await API.tools.getAll();
      enrichToolsWithCategories();
      filteredTools = [...allTools];
      renderTools(filteredTools);
      if (typeof MapManager !== 'undefined' && MapManager.updateToolsOnMap) {
        MapManager.updateToolsOnMap(filteredTools);
      }
    } catch (err) {
      console.error('Error cargando maquinaria:', err);
    }
  }

  function filterTools(query = '') {
    const q = query.trim().toLowerCase();
    filteredTools = allTools.filter(tool => {
      const toolCat = (tool.categoryName || tool.category || '').toLowerCase();
      const matchCat = (selectedCategory === 'TODAS' || 
                        toolCat === selectedCategory.toLowerCase() || 
                        toolCat.includes(selectedCategory.toLowerCase()));
      const matchQuery = !q || 
        (tool.name && tool.name.toLowerCase().includes(q)) || 
        (tool.description && tool.description.toLowerCase().includes(q)) || 
        (tool.brand && tool.brand.toLowerCase().includes(q)) || 
        (tool.model && tool.model.toLowerCase().includes(q)) || 
        (tool.locationName && tool.locationName.toLowerCase().includes(q));
      return matchCat && matchQuery;
    });
    renderTools(filteredTools);
    if (typeof MapManager !== 'undefined' && MapManager.updateToolsOnMap) {
      MapManager.updateToolsOnMap(filteredTools);
    }
  }

  let heroDispatchMode = 'delivery';

  function initHeroWidgetDates() {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = tomorrow.toISOString().split('T')[0];

    const heroEnd = new Date(tomorrow);
    heroEnd.setDate(heroEnd.getDate() + 6);
    const defaultEndStr = heroEnd.toISOString().split('T')[0];

    const heroStartEl = document.getElementById('hero-start-date');
    const heroEndEl = document.getElementById('hero-end-date');

    if (heroStartEl) {
      heroStartEl.min = tomorrowStr;
      if (!heroStartEl.value || heroStartEl.value < tomorrowStr) {
        heroStartEl.value = tomorrowStr;
      }
      heroStartEl.addEventListener('change', () => validateHeroDates('start'));
    }

    if (heroEndEl) {
      const nextDay = new Date(heroStartEl ? heroStartEl.value : tomorrowStr);
      nextDay.setDate(nextDay.getDate() + 1);
      const nextDayStr = nextDay.toISOString().split('T')[0];
      heroEndEl.min = nextDayStr;
      if (!heroEndEl.value || heroEndEl.value <= (heroStartEl ? heroStartEl.value : tomorrowStr)) {
        heroEndEl.value = defaultEndStr > nextDayStr ? defaultEndStr : nextDayStr;
      }
      heroEndEl.addEventListener('change', () => validateHeroDates('end'));
    }
  }

  function validateHeroDates(changedInput) {
    const startEl = document.getElementById('hero-start-date');
    const endEl = document.getElementById('hero-end-date');
    if (!startEl || !endEl) return;

    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = tomorrow.toISOString().split('T')[0];

    if (startEl.value && startEl.value < tomorrowStr) {
      Swal.fire({
        icon: 'warning',
        title: 'Fecha de inicio no válida',
        text: 'Las reservas de maquinaria pesada deben coordinarse como mínimo a partir de mañana.',
        confirmButtonColor: '#00843D',
        timer: 3000
      });
      startEl.value = tomorrowStr;
    }

    if (startEl.value) {
      const startDate = new Date(startEl.value + 'T00:00:00');
      const minEndDate = new Date(startDate);
      minEndDate.setDate(minEndDate.getDate() + 1);
      const minEndDateStr = minEndDate.toISOString().split('T')[0];
      endEl.min = minEndDateStr;

      if (endEl.value && endEl.value <= startEl.value) {
        if (changedInput === 'end') {
          Swal.fire({
            icon: 'warning',
            title: 'Fecha de devolución inválida',
            text: 'La fecha de devolución debe ser posterior a la fecha de inicio del alquiler.',
            confirmButtonColor: '#00843D',
            timer: 3000
          });
        }
        endEl.value = minEndDateStr;
      }
    }
  }

  function selectHeroLocation(locName) {
    const input = document.getElementById('hero-location-input');
    if (input) {
      input.value = `${locName} (Bogotá & Sabana)`;
      input.focus();
    }
  }

  function setHeroDispatchMode(mode) {
    heroDispatchMode = mode;
    const btnDelivery = document.getElementById('btn-hero-delivery');
    const btnPickup = document.getElementById('btn-hero-pickup');
    if (btnDelivery && btnPickup) {
      const delIcon = btnDelivery.querySelector('i');
      const pickIcon = btnPickup.querySelector('i');
      if (mode === 'delivery') {
        btnDelivery.classList.add('active');
        btnPickup.classList.remove('active');
        if (delIcon) delIcon.className = 'fa fa-dot-circle me-1';
        if (pickIcon) pickIcon.className = 'fa fa-circle me-1 text-muted';
      } else {
        btnPickup.classList.add('active');
        btnDelivery.classList.remove('active');
        if (delIcon) delIcon.className = 'fa fa-circle me-1 text-muted';
        if (pickIcon) pickIcon.className = 'fa fa-dot-circle me-1';
      }
    }
  }

  function applyHeroBookingFilter() {
    const locInput = document.getElementById('hero-location-input');
    const startInput = document.getElementById('hero-start-date');
    const endInput = document.getElementById('hero-end-date');

    const locationVal = locInput ? locInput.value.trim() : '';
    const startVal = startInput ? startInput.value : '';
    const endVal = endInput ? endInput.value : '';

    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = tomorrow.toISOString().split('T')[0];

    if (!startVal || startVal < tomorrowStr) {
      Swal.fire({
        icon: 'warning',
        title: 'Fecha de inicio no válida',
        text: 'La fecha de inicio de la reserva debe ser como mínimo a partir de mañana.',
        confirmButtonColor: '#00843D',
        confirmButtonText: 'Ajustar fecha'
      });
      if (startInput) startInput.value = tomorrowStr;
      return;
    }

    if (!endVal || new Date(startVal + 'T00:00:00') >= new Date(endVal + 'T00:00:00')) {
      Swal.fire({
        icon: 'warning',
        title: 'Fechas Inválidas',
        text: 'La fecha de devolución debe ser posterior a la fecha de inicio del alquiler.',
        confirmButtonColor: '#00843D',
        background: '#ffffff',
        color: '#101820'
      });
      const nextDay = new Date(startVal + 'T00:00:00');
      nextDay.setDate(nextDay.getDate() + 1);
      if (endInput) endInput.value = nextDay.toISOString().split('T')[0];
      return;
    }

    // Scroll suave al catálogo
    const catalogSec = document.getElementById('section-catalog');
    if (catalogSec) {
      catalogSec.scrollIntoView({ behavior: 'smooth' });
    }

    const modeText = heroDispatchMode === 'delivery' ? 'Despacho a Obra' : 'Retiro en Patio';
    Swal.fire({
      toast: true,
      position: 'top-end',
      icon: 'success',
      title: 'Disponibilidad en Tiempo Real',
      html: `Ubicación: <strong>${locationVal || 'Bogotá D.C. & Sabana'}</strong><br>` +
            `Modalidad: <span class="badge bg-success">${modeText}</span><br>` +
            `Periodo: <strong>${startVal}</strong> al <strong>${endVal}</strong>`,
      showConfirmButton: false,
      timer: 3500,
      timerProgressBar: true,
      background: '#ffffff',
      color: '#101820'
    });
  }

  function showCoverageModal() {
    Swal.fire({
      title: '<i class="fa fa-map-marked-alt text-success me-2"></i>Cobertura Operativa & Patios de Maquinaria',
      html: `
        <div class="text-start small text-dark p-2">
          <p class="mb-3">RentaTools PRO cuenta con <strong>despacho logístico en cama baja</strong> y patios de maquinaria certificados en Bogotá D.C. y la Sabana:</p>
          <div class="row g-2 mb-3">
            <div class="col-12 col-md-6">
              <div class="p-3 border rounded bg-light h-100">
                <strong class="text-success"><i class="fa fa-city me-1"></i> Bogotá D.C.:</strong>
                <ul class="mb-0 ps-3 mt-1 text-muted">
                  <li><strong>Norte:</strong> Usaquén, Suba, Calle 170</li>
                  <li><strong>Occidente:</strong> Fontibón, Engativá, Zona Franca</li>
                  <li><strong>Centro / Industrial:</strong> Puente Aranda, Kennedy</li>
                  <li><strong>Sur:</strong> Bosa, Tunjuelito, Autopista Sur</li>
                </ul>
              </div>
            </div>
            <div class="col-12 col-md-6">
              <div class="p-3 border rounded bg-light h-100">
                <strong class="text-warning text-dark"><i class="fa fa-mountain me-1"></i> Sabana de Bogotá:</strong>
                <ul class="mb-0 ps-3 mt-1 text-muted">
                  <li><strong>Sabana Centro:</strong> Chía, Cajicá, Cota, Zipaquirá</li>
                  <li><strong>Norte Industrial:</strong> Tocancipá, Gachancipá</li>
                  <li><strong>Sabana Occidente:</strong> Mosquera, Madrid, Funza</li>
                  <li><strong>Sur / Soacha:</strong> Soacha, Sibaté</li>
                </ul>
              </div>
            </div>
          </div>
          <div class="alert alert-success d-flex align-items-center gap-2 mb-0 py-2">
            <i class="fa fa-clock fa-lg"></i>
            <div><strong>Tiempos de Despacho:</strong> Menos de 4 horas para emergencias y entregas el mismo día con flota de camas bajas activa.</div>
          </div>
        </div>
      `,
      confirmButtonText: 'Ver Maquinaria Disponible',
      confirmButtonColor: '#00843D',
      background: '#ffffff',
      color: '#101820',
      width: '650px'
    }).then(() => {
      const catalogSec = document.getElementById('section-catalog');
      if (catalogSec) catalogSec.scrollIntoView({ behavior: 'smooth' });
    });
  }

  function handleContractorQuickAccess() {
    const user = API.getUser();
    if (user) {
      if (user.role === 'ROLE_PROVEEDOR' || user.role === 'ROLE_ADMIN') {
        switchView('provider-dashboard');
      } else {
        switchView('client-dashboard');
      }
    } else {
      Swal.fire({
        title: '<i class="fa fa-dolly-flatbed text-warning me-2"></i>Acceso Contratistas & Proveedores',
        html: `
          <p class="text-muted text-start mb-3">Accede a tu cuenta de RentaTools para publicar maquinaria pesada, gestionar alquileres activos o cotizar para tus frentes de obra.</p>
          <div class="d-flex flex-column gap-2">
            <button class="btn btn-sb-green w-100 py-2 fw-bold" onclick="Swal.close(); App.openAuthModal('login');">
              <i class="fa fa-sign-in-alt me-1"></i> Iniciar Sesión en mi Portal
            </button>
            <button class="btn btn-outline-secondary w-100 py-2 fw-bold" onclick="Swal.close(); App.openAuthModal('register');">
              <i class="fa fa-user-plus me-1"></i> Registrarse como Proveedor / Contratista
            </button>
          </div>
        `,
        showConfirmButton: false,
        showCloseButton: true,
        background: '#ffffff',
        color: '#101820'
      });
    }
  }

  function handleProviderDashboardAccess() {
    const user = API.getUser();
    if (user && (user.role === 'ROLE_PROVEEDOR' || user.role === 'ROLE_ADMIN')) {
      switchView('provider-dashboard');
    } else {
      Swal.fire({
        icon: 'info',
        title: 'Gestión Proveedor',
        html: '<p class="text-muted">Tu sesión actual tiene rol <strong>CLIENTE</strong>. La vista de <em>Gestión Proveedor</em> está reservada para empresas propietarias de maquinaria.</p><p class="small text-muted mb-0">Para publicar equipos, contáctanos a soporte@rentatools.com o regístrate con perfil PROVEEDOR.</p>',
        confirmButtonColor: '#00843D',
        confirmButtonText: 'Entendido'
      });
    }
  }

  function handleAddToolAccess() {
    const user = API.getUser();
    if (user && (user.role === 'ROLE_PROVEEDOR' || user.role === 'ROLE_ADMIN')) {
      openAddToolModal();
    } else {
      Swal.fire({
        icon: 'info',
        title: 'Publicar Maquinaria',
        html: '<p class="text-muted">Para añadir maquinaria a la flota oficial de RentaTools se requiere rol de <strong>PROVEEDOR</strong> verificado.</p><p class="small text-muted mb-0">Inicia sesión con tu cuenta de proveedor o regístrate para monetizar tus activos de construcción.</p>',
        confirmButtonColor: '#00843D',
        confirmButtonText: 'Entendido'
      });
    }
  }

  const industriesMap = {
    'construccion-civil': {
      name: 'Construcción Civil & Edificación',
      categories: ['Concreto y Mezclado', 'Trabajo en Altura y Andamios', 'Formaletas y Encofrados Metálicos', 'Demolición y Rotura', 'Herramienta Eléctrica de Taller', 'Excavación y Movimiento de Tierras'],
      keywords: ['allanadora', 'mezcladora', 'vibrador', 'andamio', 'formaleta', 'taladro', 'regla', 'concreto', 'helicóptero']
    },
    'infraestructura-vial': {
      name: 'Infraestructura Vial & Asfalto',
      categories: ['Compactación de Terreno', 'Corte y Perforación de Suelos', 'Seguridad y Señalización Vial', 'Excavación y Movimiento de Tierras'],
      keywords: ['rodillo', 'compactador', 'cortadora', 'pavimento', 'bailarina', 'placa', 'canguro', 'vías', 'asfalto']
    },
    'mineria-agregados': {
      name: 'Minería & Canteras de Agregados',
      categories: ['Excavación y Movimiento de Tierras', 'Bombeo y Evacuación de Agua', 'Compresores y Aire Comprimido', 'Demolición y Rotura'],
      keywords: ['oruga', 'cat 320d', 'retroexcavadora', 'martillo', 'motobomba', 'compresor', 'lodos', 'minería', 'rompedor']
    },
    'energia-utilities': {
      name: 'Energía & Utilities',
      categories: ['Generación de Energía y Plantas Eléctricas', 'Iluminación de Obras Nocturnas', 'Soldadura Industrial', 'Compresores y Aire Comprimido'],
      keywords: ['planta eléctrica', 'cummins', 'torre de iluminación', 'motosoldadora', 'generador', 'energía', 'diésel']
    },
    'eventos-montajes': {
      name: 'Eventos, Bodegas & Montajes',
      categories: ['Elevación y Montacargas', 'Trabajo en Altura y Andamios', 'Iluminación de Obras Nocturnas', 'Generación de Energía y Plantas Eléctricas'],
      keywords: ['tijera', 'plataforma', 'andamio', 'torre', 'malacate', 'elevadora', 'montacargas']
    },
    'agroindustria': {
      name: 'Agroindustria & Adecuación de Tierras',
      categories: ['Jardinería y Descapote de Terreno', 'Bombeo y Evacuación de Agua', 'Carga y Transporte Ligero', 'Excavación y Movimiento de Tierras'],
      keywords: ['bobcat', 'minicargador', 'motobomba', 'lodos', 'dúmper', 'ahoyadora', 'jcb', 'descapote', 'riego']
    }
  };

  let activeIndustryKey = null;

  function filterByIndustry(industryKey) {
    const indData = industriesMap[industryKey];
    if (!indData) return;

    activeIndustryKey = industryKey;

    // Resaltar tarjeta visualmente en sección de industrias
    document.querySelectorAll('.sb-industry-card').forEach(card => {
      if (card.dataset.industry === industryKey) {
        card.classList.add('active-industry-card');
      } else {
        card.classList.remove('active-industry-card');
      }
    });

    // Resetear pills de categoría visualmente a 'TODAS'
    selectedCategory = 'TODAS';
    const container = document.getElementById('category-pills-container');
    if (container) {
      container.querySelectorAll('.category-pill').forEach(p => {
        p.classList.remove('active');
        if (p.dataset.category === 'TODAS') p.classList.add('active');
      });
    }

    // Filtrar herramientas por categorías y keywords de la industria
    const targetCats = indData.categories.map(c => c.toLowerCase());
    const targetKeys = indData.keywords.map(k => k.toLowerCase());

    filteredTools = allTools.filter(tool => {
      const toolCat = (tool.categoryName || tool.category || '').toLowerCase();
      const toolName = (tool.name || '').toLowerCase();
      const toolDesc = (tool.description || '').toLowerCase();
      const toolBrand = (tool.brand || '').toLowerCase();

      const matchesCat = targetCats.some(cat => toolCat.includes(cat) || cat.includes(toolCat));
      const matchesKey = targetKeys.some(key => 
        toolName.includes(key) || toolDesc.includes(key) || toolBrand.includes(key) || toolCat.includes(key)
      );

      return matchesCat || matchesKey;
    });

    renderTools(filteredTools);
    if (typeof MapManager !== 'undefined' && MapManager.updateToolsOnMap) {
      MapManager.updateToolsOnMap(filteredTools);
    }

    // Mostrar Banner Informativo arriba del catálogo
    const banner = document.getElementById('industry-active-banner');
    const titleEl = document.getElementById('industry-banner-title');
    const countEl = document.getElementById('industry-banner-count');
    if (banner && titleEl && countEl) {
      titleEl.textContent = indData.name;
      countEl.textContent = filteredTools.length;
      banner.style.setProperty('display', 'flex', 'important');
    }

    // Scroll suave hacia el catálogo
    const catalogSec = document.getElementById('section-catalog');
    if (catalogSec) {
      catalogSec.scrollIntoView({ behavior: 'smooth' });
    }

    // Toast de confirmación
    Swal.fire({
      toast: true,
      position: 'top-end',
      icon: 'info',
      title: `Sector: ${indData.name}`,
      text: `${filteredTools.length} maquinarias y equipos encontrados para esta industria`,
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true
    });
  }

  function clearIndustryFilter() {
    activeIndustryKey = null;
    document.querySelectorAll('.sb-industry-card').forEach(card => card.classList.remove('active-industry-card'));
    const banner = document.getElementById('industry-active-banner');
    if (banner) banner.style.setProperty('display', 'none', 'important');

    selectedCategory = 'TODAS';
    filteredTools = [...allTools];
    renderTools(filteredTools);
    if (typeof MapManager !== 'undefined' && MapManager.updateToolsOnMap) {
      MapManager.updateToolsOnMap(filteredTools);
    }
  }

  function downloadCatalogPdf() {
    Swal.fire({
      title: 'Generando Catálogo Oficial (PDF)',
      html: '<p class="text-muted">Preparando el catálogo técnico con fichas de maquinaria, especificaciones y tarifas en COP para impresión y descarga...</p><div class="spinner-border text-success my-2" role="status"></div>',
      showConfirmButton: false,
      timer: 1200
    }).then(() => {
      const printWindow = window.open('', '_blank');
      if (!printWindow) {
        Swal.fire({
          icon: 'warning',
          title: 'Ventana Emergente Bloqueada',
          text: 'Por favor permite las ventanas emergentes en tu navegador para ver y descargar el catálogo en PDF.',
          confirmButtonColor: '#00843D'
        });
        return;
      }

      const toolsList = (allTools && allTools.length > 0) ? allTools : [
        { name: 'Retroexcavadora Oruga CAT 320D', brand: 'Caterpillar', model: '320D', dailyRate: 850000, depositAmount: 2500000, categoryName: 'Excavación y Movimiento de Tierras' },
        { name: 'Minicargador Bobcat S570', brand: 'Bobcat', model: 'S570', dailyRate: 350000, depositAmount: 1200000, categoryName: 'Excavación y Movimiento de Tierras' },
        { name: 'Planta Eléctrica Diésel 50kVA Insonorizada', brand: 'Cummins', model: 'C55D5', dailyRate: 280000, depositAmount: 1000000, categoryName: 'Generación de Energía' },
        { name: 'Rodillo Vibrocompactador Doble 1.2 Ton', brand: 'Dynapac', model: 'CC1200', dailyRate: 310000, depositAmount: 1100000, categoryName: 'Compactación de Terreno' },
        { name: 'Plataforma Elevadora Tijera Eléctrica 8m', brand: 'Genie', model: 'GS-1930', dailyRate: 180000, depositAmount: 650000, categoryName: 'Trabajo en Altura' }
      ];

      const rows = toolsList.map((t, idx) => `
        <tr>
          <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; font-weight: bold; color: #101820;">${idx + 1}. ${t.name}</td>
          <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; color: #475569;">${t.brand || ''} ${t.model || ''}</td>
          <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; color: #475569;">${t.categoryName || t.category || 'Maquinaria'}</td>
          <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; font-weight: bold; color: #00843D;">${API.formatCurrency(t.dailyRate || t.dailyPrice || 0)}/día</td>
          <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; color: #64748b;">${API.formatCurrency(t.depositAmount || 0)}</td>
        </tr>
      `).join('');

      const htmlContent = `
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8">
          <title>Catálogo Oficial de Maquinaria y Equipos 2026 - RentaTools PRO</title>
          <style>
            body { font-family: 'Helvetica Neue', Arial, sans-serif; margin: 30px; color: #101820; line-height: 1.4; }
            .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid #00843D; padding-bottom: 16px; margin-bottom: 24px; }
            .logo-text { font-size: 26px; font-weight: 900; color: #101820; letter-spacing: -0.5px; }
            .logo-sub { font-size: 13px; font-weight: 700; color: #00843D; letter-spacing: 1px; }
            .company-info { text-align: right; font-size: 12px; color: #59636e; line-height: 1.4; }
            .badge-cert { background: #FFC72C; color: #101820; padding: 4px 10px; border-radius: 4px; font-weight: bold; font-size: 11px; display: inline-block; margin-top: 4px; }
            h2 { font-size: 18px; color: #00843D; margin-top: 0; margin-bottom: 14px; text-transform: uppercase; letter-spacing: 0.5px; }
            table { width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 30px; }
            th { background: #00843D; color: #ffffff; text-align: left; padding: 10px 12px; font-weight: 700; font-size: 12px; }
            .footer-notes { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 16px; font-size: 11px; color: #475569; line-height: 1.5; }
            @media print {
              .no-print { display: none; }
              body { margin: 15mm; }
            }
          </style>
        </head>
        <body>
          <div class="no-print" style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; background: #e8f5e9; padding: 12px 18px; border-radius: 8px;">
            <span><strong>Catálogo Oficial RentaTools 2026</strong> listo para impresión o guardar como PDF.</span>
            <button onclick="window.print()" style="background: #00843D; color: white; border: none; padding: 8px 18px; border-radius: 6px; font-weight: bold; cursor: pointer;">
              Imprimir / Guardar en PDF
            </button>
          </div>
          <div class="header">
            <div>
              <div class="logo-text">RENTATOOLS PRO</div>
              <div class="logo-sub">ALQUILER DE MAQUINARIA PESADA & HERRAMIENTAS</div>
              <span class="badge-cert">Catálogo Oficial 2026 &bull; Facturación DIAN</span>
            </div>
            <div class="company-info">
              <strong>Sede Central & Patios Operativos:</strong><br>
              Bogotá D.C. & Sabana (Chía, Cajicá, Mosquera, Tocancipá)<br>
              Línea Obras: (601) 745-9000 &bull; WhatsApp: 310 999 8888<br>
              Portal: www.rentatools.com &bull; Correo: operaciones@rentatools.com
            </div>
          </div>

          <h2>Inventario Oficial de Maquinaria y Equipos Disponibles (${toolsList.length} Referencias)</h2>
          <table>
            <thead>
              <tr>
                <th style="width: 32%;">Equipo / Maquinaria</th>
                <th style="width: 18%;">Marca / Modelo</th>
                <th style="width: 25%;">Especialidad / Categoría</th>
                <th style="width: 13%;">Tarifa Día</th>
                <th style="width: 12%;">Garantía</th>
              </tr>
            </thead>
            <tbody>
              ${rows}
            </tbody>
          </table>

          <div class="footer-notes">
            <strong>Condiciones de Alquiler y Despacho RentaTools PRO:</strong><br>
            &bull; <strong>Despacho a Obra:</strong> Coordinación de transporte en cama baja para maquinaria pesada en menos de 4 horas en Bogotá y Sabana.<br>
            &bull; <strong>Seguridad y Certificación:</strong> Maquinaria 100% inspeccionada, horómetros certificados y pólizas de protección todo riesgo vigentes.<br>
            &bull; <strong>Facturación DIAN:</strong> Tarifas expresadas en COP sin IVA. IVA del 19% aplicable en facturación electrónica reglamentaria.<br>
            &bull; <strong>Garantía:</strong> El depósito es 100% reembolsable al retorno del equipo tras inspección técnica de horómetro y fluidos.
          </div>
        </body>
        </html>
      `;

      printWindow.document.open();
      printWindow.document.write(htmlContent);
      printWindow.document.close();
      printWindow.focus();
    });
  }

  function toggleMapSection() {
    const mapContainer = document.getElementById('map-collapse-container');
    if (mapContainer) {
      const bsCollapse = bootstrap.Collapse.getInstance(mapContainer) || new bootstrap.Collapse(mapContainer);
      bsCollapse.toggle();
    }
  }

  function updateCartBadge() {
    const hasQuote = Boolean(activeToolForBooking && activeToolForBooking.calculatedQuote);
    const count = hasQuote ? 1 : 0;
    const badgeEl = document.getElementById('sb-cart-count');
    if (badgeEl) badgeEl.textContent = count;
    const modalBadgeEl = document.getElementById('cart-modal-badge');
    if (modalBadgeEl) modalBadgeEl.textContent = count;
  }

  function renderCartModal() {
    const container = document.getElementById('cart-modal-body');
    if (!container) return;

    updateCartBadge();

    if (!activeToolForBooking || !activeToolForBooking.calculatedQuote) {
      // ESTADO DE CARRITO VACÍO (IDÉNTICO A SUNBELT RENTALS - CAPTURA 8)
      container.innerHTML = `
        <div class="text-center py-4">
          <div class="sb-cart-empty-art mx-auto">
            <i class="fa fa-shopping-basket fa-3x"></i>
          </div>
          <h4 class="fw-bold text-dark mb-2">Tu carrito está actualmente vacío</h4>
          <p class="text-muted small mb-4 mx-auto" style="max-width: 360px;">
            Explora nuestro catálogo de maquinaria pesada y herramientas de construcción para cotizar y reservar en línea con tarifas por día, semana o mes.
          </p>
          <div class="d-flex flex-column gap-2">
            <button type="button" class="btn btn-sb-green w-100 py-2 fw-bold" data-bs-dismiss="modal" onclick="document.getElementById('section-catalog')?.scrollIntoView({ behavior: 'smooth' });">
              <i class="fa fa-search me-1"></i> Explorar Catálogo de Maquinaria
            </button>
            <div class="p-2 rounded bg-light border text-muted small mt-2">
              <i class="fa fa-phone-alt me-1 text-success"></i> ¿Necesitas asesoría para tu obra? Llámanos: <strong>(601) 745-9000</strong>
            </div>
          </div>
        </div>
      `;
      return;
    }

    // SI HAY UNA COTIZACIÓN ACTIVA EN CURSO: RESUMEN RÁPIDO CON OPCIÓN DE IR DIRECTO A PAGAR
    const tool = activeToolForBooking;
    const q = tool.calculatedQuote;
    const details = tool.bookingDetails || {};
    const deliveryAddress = details.deliveryAddress || document.getElementById('book-delivery-address')?.value || 'Bogotá D.C. & Sabana';
    const camaBajaNotes = details.camaBajaNotes || document.getElementById('book-cama-baja-notes')?.value || '';

    container.innerHTML = `
      <div class="cart-active-quote-wrap">
        <div class="d-flex align-items-center justify-content-between pb-3 border-bottom mb-3">
          <div class="d-flex align-items-center gap-3">
            <img src="${getToolImageUrl(tool)}" class="rounded border p-1" style="width: 72px; height: 72px; object-fit: contain; background: #f8fafc;" alt="${tool.name}" onerror="handleToolImageError(this);">
            <div>
              <span class="badge bg-success mb-1"><i class="fa fa-check-circle me-1"></i> Cotización en Curso</span>
              <h6 class="fw-bold text-dark mb-0">${tool.name}</h6>
              <div class="text-muted small">${tool.categoryName || tool.category || 'Maquinaria'} | CAT CLASS: RT-${String(tool.id).padStart(4, '0')}</div>
            </div>
          </div>
          <button class="btn btn-sm btn-outline-danger" onclick="App.clearCart()" title="Vaciar Carrito">
            <i class="fa fa-trash-alt"></i>
          </button>
        </div>

        <div class="bg-light p-3 rounded-3 border mb-3">
          <div class="d-flex justify-content-between small text-muted mb-2">
            <span><i class="fa fa-calendar-alt text-success me-1"></i> Periodo de Reserva:</span>
            <strong class="text-dark">${q.startDate} al ${q.endDate} (${q.days} días)</strong>
          </div>
          <div class="d-flex justify-content-between small text-muted mb-2">
            <span><i class="fa fa-map-marker-alt text-danger me-1"></i> Frente de Obra:</span>
            <strong class="text-dark text-truncate ms-2" style="max-width: 230px;" title="${deliveryAddress}">${deliveryAddress}</strong>
          </div>
          ${camaBajaNotes ? `
          <div class="d-flex justify-content-between small text-muted mb-2">
            <span><i class="fa fa-truck-pickup text-primary me-1"></i> Cama Baja:</span>
            <span class="text-muted text-truncate ms-2" style="max-width: 230px;" title="${camaBajaNotes}">${camaBajaNotes}</span>
          </div>` : ''}
          <hr class="my-2">
          <div class="d-flex justify-content-between small text-muted mb-1">
            <span>Subtotal Alquiler (${q.days} días):</span>
            <span class="text-dark fw-bold">${API.formatCurrency(q.subtotal)}</span>
          </div>
          <div class="d-flex justify-content-between small text-muted mb-1">
            <span>IVA (19% Factura DIAN):</span>
            <span class="text-dark fw-bold">${API.formatCurrency(q.iva)}</span>
          </div>
          <div class="d-flex justify-content-between small text-muted mb-1">
            <span>Depósito Garantía (Retornable):</span>
            <span class="text-primary fw-bold">${API.formatCurrency(q.deposit)}</span>
          </div>
          <hr class="my-2">
          <div class="d-flex justify-content-between align-items-center">
            <span class="fw-bold text-dark">TOTAL ESTIMADO COP:</span>
            <span class="h4 text-success fw-bold mb-0">${API.formatCurrency(q.total)}</span>
          </div>
        </div>

        <div class="d-flex flex-column gap-2">
          <button type="button" class="btn btn-sb-green w-100 py-2 fw-bold" onclick="App.proceedToPaymentFromCart()">
            <i class="fa fa-credit-card me-2"></i> Ir Directo a Pagar (${API.formatCurrency(q.total)})
          </button>
          <div class="row g-2">
            <div class="col-6">
              <button type="button" class="btn btn-outline-secondary w-100 py-1 btn-sm" onclick="App.modifyBookingFromCart()">
                <i class="fa fa-edit me-1"></i> Modificar Fechas
              </button>
            </div>
            <div class="col-6">
              <button type="button" class="btn btn-outline-danger w-100 py-1 btn-sm" onclick="App.clearCart()">
                <i class="fa fa-trash-alt me-1"></i> Vaciar Carrito
              </button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  function openCartModal() {
    renderCartModal();
    const cartModalEl = document.getElementById('cartModal');
    if (cartModalEl) {
      const modal = bootstrap.Modal.getInstance(cartModalEl) || new bootstrap.Modal(cartModalEl);
      modal.show();
    }
  }

  function proceedToPaymentFromCart() {
    if (!activeToolForBooking || !activeToolForBooking.calculatedQuote) return;
    const cartModalEl = document.getElementById('cartModal');
    const cartModal = bootstrap.Modal.getInstance(cartModalEl);
    if (cartModal) cartModal.hide();

    // Sincronizar dirección de obra si no está fijada aún
    const addr = activeToolForBooking.bookingDetails?.deliveryAddress || document.getElementById('book-delivery-address')?.value || 'Bogotá D.C. & Sabana';
    const notes = activeToolForBooking.bookingDetails?.camaBajaNotes || document.getElementById('book-cama-baja-notes')?.value || '';
    activeToolForBooking.bookingDetails = { deliveryAddress: addr, camaBajaNotes: notes };

    // Abrir Modal de Pago
    const q = activeToolForBooking.calculatedQuote;
    document.getElementById('pay-tool-title').textContent = activeToolForBooking.name;
    document.getElementById('pay-summary-total').textContent = API.formatCurrency(q.total);
    document.getElementById('pay-summary-breakdown').textContent = `${q.days} días de alquiler + IVA 19% (${API.formatCurrency(q.iva)}) + Garantía (${API.formatCurrency(q.deposit)})`;
    const payDelivery = document.getElementById('pay-summary-delivery');
    if (payDelivery) {
      payDelivery.style.display = 'block';
      payDelivery.innerHTML = `<i class="fa fa-map-marker-alt text-danger me-1"></i> Frente de obra: <strong>${addr}</strong>` +
        (notes ? `<br><small class="text-muted"><i class="fa fa-truck-pickup me-1"></i> Cama baja: ${notes}</small>` : '');
    }

    const paymentModal = new bootstrap.Modal(document.getElementById('paymentModal'));
    paymentModal.show();
  }

  function modifyBookingFromCart() {
    if (!activeToolForBooking) return;
    const cartModalEl = document.getElementById('cartModal');
    const cartModal = bootstrap.Modal.getInstance(cartModalEl);
    if (cartModal) cartModal.hide();

    const bookingModalEl = document.getElementById('bookingModal');
    const bookingModal = bootstrap.Modal.getInstance(bookingModalEl) || new bootstrap.Modal(bookingModalEl);
    bookingModal.show();
  }

  function clearCart() {
    activeToolForBooking = null;
    updateCartBadge();
    renderCartModal();
  }

  function renderTools(tools) {
    const counterEl = document.getElementById('tools-counter-num');
    if (counterEl) {
      counterEl.textContent = tools.length;
    }

    const grid = document.getElementById('tools-grid');
    if (!grid) return;

    if (tools.length === 0) {
      grid.innerHTML = `
        <div class="col-12 text-center py-5 bg-white rounded-3 border">
          <i class="fa fa-search fa-3x text-secondary mb-3"></i>
          <h5 class="text-dark fw-bold">No encontramos maquinaria con los filtros seleccionados</h5>
          <p class="text-muted">Prueba ampliando el radio de búsqueda o cambiando de categoría.</p>
        </div>`;
      return;
    }

    grid.innerHTML = tools.map(t => {
      const daily = t.dailyRate || 0;
      const weekly = Math.round(daily * 3.5);
      const monthly = Math.round(daily * 12);
      const catClass = `RT-${String(t.id).padStart(4, '0')}`;
      
      let specsHtml = '';
      if (t.specifications && Array.isArray(t.specifications) && t.specifications.length > 0) {
        specsHtml = t.specifications.slice(0, 3).map(s => `<li><strong>${s.key || s.specName || 'Dato'}:</strong> ${s.value || s.specValue || ''}</li>`).join('');
      } else {
        specsHtml = `<li><strong>Marca:</strong> ${t.brand || 'Industrial'} ${t.model || ''}</li><li><strong>Potencia:</strong> Motor Industrial Certificado</li>`;
      }

      return `
        <div class="sb-tool-card mb-3">
          <!-- Columna 1: Imagen y Badge de Estado -->
          <div class="sb-tool-img-wrap position-relative">
            <img src="${getToolImageUrl(t)}" alt="${t.name}" onerror="handleToolImageError(this);">
            <span class="badge ${t.status === 'DISPONIBLE' ? 'bg-success' : 'bg-secondary'} position-absolute top-0 start-0 m-2" style="font-size: 0.7rem;">
              <i class="fa ${t.status === 'DISPONIBLE' ? 'fa-check-circle' : 'fa-clock'} me-1"></i> ${t.status}
            </span>
          </div>

          <!-- Columna 2: Ficha Técnica e Inventario -->
          <div class="sb-tool-info-col">
            <div class="sb-tool-cat-class">CAT CLASS: ${catClass} | ${t.categoryName || t.category || 'Maquinaria'}</div>
            <h4 class="sb-tool-name">${t.name}</h4>
            <div class="sb-tool-specs-header">ESPECIFICACIONES TÉCNICAS</div>
            <ul class="sb-tool-specs-list">
              <li><strong>Marca / Modelo:</strong> ${t.brand || 'Industrial'} ${t.model || ''}</li>
              ${specsHtml}
              <li><strong>Garantía retornable:</strong> ${API.formatCurrency(t.depositAmount)}</li>
            </ul>
            <div class="sb-tool-location-badge">
              <i class="fa fa-map-marker-alt"></i> <span>${t.locationName || 'Bogotá D.C. & Cobertura Sabana'}</span>
            </div>
          </div>

          <!-- Columna 3: Tarifas Tabulares y Botón -->
          <div class="sb-tool-rate-box">
            <div class="sb-rate-grid">
              <div class="sb-rate-col">
                <span class="sb-rate-value">${API.formatCurrency(daily)}</span>
                <span class="sb-rate-label">1 Día</span>
              </div>
              <div class="sb-rate-col">
                <span class="sb-rate-value">${API.formatCurrency(weekly)}</span>
                <span class="sb-rate-label">1 Sem</span>
              </div>
              <div class="sb-rate-col">
                <span class="sb-rate-value">${API.formatCurrency(monthly)}</span>
                <span class="sb-rate-label">4 Sem</span>
              </div>
            </div>
            <button class="btn btn-sb-green w-100" onclick="App.openBookingModal('${t.id}')">
              <i class="fa fa-calendar-check me-1"></i> Cotizar & Reservar
            </button>
            <div class="sb-tool-availability-note">
              <i class="fa fa-check-circle me-1"></i> Disponibilidad garantizada en Bogotá
            </div>
          </div>
        </div>
      `;
    }).join('');
  }

  function openBookingModal(toolId) {
    const user = API.getUser();
    if (!user) {
      Swal.fire({
        icon: 'info',
        title: 'Iniciar Sesión Requerido',
        text: 'Debes iniciar sesión para realizar cotizaciones y reservas de maquinaria en línea.',
        confirmButtonColor: '#00843D',
        confirmButtonText: 'Ir a Iniciar Sesión',
        background: '#ffffff',
        color: '#101820'
      }).then(() => {
        openAuthModal('login');
      });
      return;
    }

    const tool = allTools.find(t => String(t.id) === String(toolId));
    if (!tool) {
      console.warn('Herramienta no encontrada para ID:', toolId);
      return;
    }

    activeToolForBooking = tool;
    document.getElementById('book-tool-name').textContent = tool.name;
    document.getElementById('book-tool-rate').textContent = `${API.formatCurrency(tool.dailyRate)} / día`;
    document.getElementById('book-tool-deposit').textContent = API.formatCurrency(tool.depositAmount);
    const bookImg = document.getElementById('book-tool-img');
    if (bookImg) {
      bookImg.src = getToolImageUrl(tool);
      bookImg.onerror = function() { handleToolImageError(this); };
    }

    // Fechas: tomadas del Hero widget si están seleccionadas y válidas (mínimo mañana), o por defecto mañana y 3 días
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = tomorrow.toISOString().split('T')[0];

    const startDateInput = document.getElementById('book-start-date');
    const endDateInput = document.getElementById('book-end-date');
    if (startDateInput) startDateInput.min = tomorrowStr;

    const heroStartVal = document.getElementById('hero-start-date')?.value;
    const heroEndVal = document.getElementById('hero-end-date')?.value;

    let startToUse = '';
    let endToUse = '';

    if (heroStartVal && heroEndVal && new Date(heroStartVal) < new Date(heroEndVal) && heroStartVal >= tomorrowStr) {
      startToUse = heroStartVal;
      endToUse = heroEndVal;
    } else {
      const end = new Date(tomorrow);
      end.setDate(end.getDate() + 3);

      startToUse = tomorrowStr;
      endToUse = end.toISOString().split('T')[0];
    }

    if (startDateInput) startDateInput.value = startToUse;
    if (endDateInput) {
      endDateInput.value = endToUse;
      const nextDay = new Date(startToUse);
      nextDay.setDate(nextDay.getDate() + 1);
      endDateInput.min = nextDay.toISOString().split('T')[0];
    }

    // Modalidad y Ubicación transferidas del Hero
    const heroLoc = document.getElementById('hero-location-input')?.value || 'Bogotá D.C. & Sabana';
    const modeText = heroDispatchMode === 'delivery' ? 'Despacho a Obra' : 'Retiro en Patio';
    const dispatchNoteText = document.getElementById('book-dispatch-note-text');
    if (dispatchNoteText) {
      dispatchNoteText.innerHTML = `Modalidad: <strong>${modeText}</strong> &bull; Destino: <strong>${heroLoc}</strong>`;
    }

    // Dirección de obra y autocompletado de Bogotá y Cundinamarca
    const addressInput = document.getElementById('book-delivery-address');
    if (addressInput) {
      if (!addressInput.value) {
        if (heroLoc && heroLoc.trim() && heroLoc !== 'Bogotá D.C. & Sabana') {
          addressInput.value = heroLoc;
        } else if (user.address) {
          addressInput.value = user.address;
        } else {
          addressInput.value = 'Bogotá D.C. - Usaquén (Cra 7 # 127, Obra Residencial)';
        }
      }
      addressInput.classList.remove('is-invalid');
    }

    recalculateBookingQuote();

    const modal = new bootstrap.Modal(document.getElementById('bookingModal'));
    modal.show();
  }

  function recalculateBookingQuote() {
    if (!activeToolForBooking) return;

    const startInput = document.getElementById('book-start-date');
    const endInput = document.getElementById('book-end-date');
    if (!startInput || !endInput) return;

    let startVal = startInput.value;
    let endVal = endInput.value;
    if (!startVal || !endVal) return;

    let startDate = new Date(startVal + 'T00:00:00');
    let endDate = new Date(endVal + 'T00:00:00');

    // Si fecha final es anterior o igual a la inicial, ajustar automáticamente a 1 día después
    if (endDate <= startDate) {
      const adjusted = new Date(startDate);
      adjusted.setDate(adjusted.getDate() + 1);
      endVal = adjusted.toISOString().split('T')[0];
      endInput.value = endVal;
      endDate = new Date(endVal + 'T00:00:00');
    }
    endInput.min = startVal;

    let diffDays = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));
    if (diffDays <= 0) diffDays = 1;

    const dailyRate = activeToolForBooking.dailyRate || 0;
    const deposit = activeToolForBooking.depositAmount || 0;

    const subtotal = dailyRate * diffDays;
    const iva = Math.round(subtotal * 0.19); // IVA 19% Factura DIAN
    const total = subtotal + iva + deposit;

    document.getElementById('quote-days').textContent = `${diffDays} día(s)`;
    document.getElementById('quote-subtotal').textContent = API.formatCurrency(subtotal);
    document.getElementById('quote-iva').textContent = API.formatCurrency(iva);
    document.getElementById('quote-deposit').textContent = API.formatCurrency(deposit);
    document.getElementById('quote-total').textContent = API.formatCurrency(total);

    // Guardar cálculo para proceso de pago y carrito
    activeToolForBooking.calculatedQuote = {
      days: diffDays,
      subtotal,
      iva,
      deposit,
      total,
      startDate: startVal,
      endDate: endVal
    };

    updateCartBadge();
  }

  async function proceedToPaymentFromBooking() {
    if (!activeToolForBooking || !activeToolForBooking.calculatedQuote) return;

    const addressInput = document.getElementById('book-delivery-address');
    const deliveryAddress = (addressInput?.value || '').trim();
    if (!deliveryAddress) {
      addressInput?.classList.add('is-invalid');
      Swal.fire({
        icon: 'warning',
        title: 'Dirección de Obra Requerida',
        text: 'Por favor indica la dirección o frente de obra en Bogotá o Cundinamarca para el despacho de la maquinaria.',
        confirmButtonColor: '#00843D',
        background: '#ffffff',
        color: '#101820'
      });
      return;
    }
    addressInput?.classList.remove('is-invalid');

    const camaBajaNotes = (document.getElementById('book-cama-baja-notes')?.value || '').trim();
    activeToolForBooking.bookingDetails = {
      deliveryAddress,
      camaBajaNotes
    };

    const bookingModalEl = document.getElementById('bookingModal');
    const bookingModal = bootstrap.Modal.getInstance(bookingModalEl);
    if (bookingModal) bookingModal.hide();

    // Abrir Modal de Pago con desglose completo
    const q = activeToolForBooking.calculatedQuote;
    document.getElementById('pay-tool-title').textContent = activeToolForBooking.name;
    document.getElementById('pay-summary-total').textContent = API.formatCurrency(q.total);
    document.getElementById('pay-summary-breakdown').textContent = `${q.days} días de renta + IVA 19% (${API.formatCurrency(q.iva)}) + Garantía (${API.formatCurrency(q.deposit)})`;
    const payDelivery = document.getElementById('pay-summary-delivery');
    if (payDelivery) {
      payDelivery.style.display = 'block';
      payDelivery.innerHTML = `<i class="fa fa-map-marker-alt text-danger me-1"></i> Despacho: <strong>${deliveryAddress}</strong>` +
        (camaBajaNotes ? `<br><small class="text-muted"><i class="fa fa-truck-pickup me-1"></i> Cama baja: ${camaBajaNotes}</small>` : '');
    }

    const paymentModal = new bootstrap.Modal(document.getElementById('paymentModal'));
    paymentModal.show();
  }

  async function submitPayment() {
    const user = API.getUser();
    if (!user || !activeToolForBooking) return;

    const q = activeToolForBooking.calculatedQuote;
    const details = activeToolForBooking.bookingDetails || {};
    const deliveryAddress = details.deliveryAddress || document.getElementById('book-delivery-address')?.value || user.address || 'Bogotá D.C. & Sabana';
    const camaBajaNotes = details.camaBajaNotes || document.getElementById('book-cama-baja-notes')?.value || '';
    const selectedMethod = document.querySelector('input[name="paymentMethod"]:checked')?.value || 'PSE';

    // VALIDACIÓN ESTRICTA DE CAMPOS OBLIGATORIOS SEGÚN MÉTODO DE PAGO
    if (selectedMethod === 'CREDIT_CARD' || selectedMethod === 'DEBIT_CARD') {
      const holderInput = document.getElementById('card-holder-name');
      const numberInput = document.getElementById('card-number');
      const expiryInput = document.getElementById('card-expiry');
      const cvcInput = document.getElementById('card-cvc');

      const holder = (holderInput?.value || '').trim();
      const rawNumber = (numberInput?.value || '').replace(/\s+/g, '');
      const expiry = (expiryInput?.value || '').trim();
      const cvc = (cvcInput?.value || '').trim();

      let hasError = false;
      let errorMsg = '';

      if (!holder || holder.length < 3) {
        holderInput?.classList.add('is-invalid');
        hasError = true;
        errorMsg = 'Ingresa el nombre del titular tal como figura en la tarjeta.';
      } else {
        holderInput?.classList.remove('is-invalid');
      }

      if (!rawNumber || rawNumber.length < 15 || rawNumber.length > 16 || !/^\d+$/.test(rawNumber)) {
        numberInput?.classList.add('is-invalid');
        hasError = true;
        if (!errorMsg) errorMsg = 'El número de tarjeta debe tener entre 15 y 16 dígitos válidos.';
      } else {
        numberInput?.classList.remove('is-invalid');
      }

      // Validar formato MM/AA y que la tarjeta no esté vencida
      const expiryMatch = expiry.match(/^(0[1-9]|1[0-2])\/(\d{2})$/);
      if (!expiryMatch) {
        expiryInput?.classList.add('is-invalid');
        hasError = true;
        if (!errorMsg) errorMsg = 'La fecha de expiración debe tener formato MM/AA válido (ej. 12/28).';
      } else {
        const expMonth = parseInt(expiryMatch[1], 10);
        const expYear = parseInt('20' + expiryMatch[2], 10);
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1; // 1 - 12
        if (expYear < currentYear || (expYear === currentYear && expMonth < currentMonth)) {
          expiryInput?.classList.add('is-invalid');
          hasError = true;
          if (!errorMsg) errorMsg = 'La tarjeta de crédito/débito ingresada se encuentra vencida.';
        } else {
          expiryInput?.classList.remove('is-invalid');
        }
      }

      if (!cvc || cvc.length < 3 || cvc.length > 4 || !/^\d+$/.test(cvc)) {
        cvcInput?.classList.add('is-invalid');
        hasError = true;
        if (!errorMsg) errorMsg = 'El código CVC / CVV debe contener 3 o 4 dígitos.';
      } else {
        cvcInput?.classList.remove('is-invalid');
      }

      if (hasError) {
        Swal.fire({
          icon: 'warning',
          title: 'Datos de Tarjeta Incompletos',
          text: errorMsg,
          confirmButtonColor: '#00843D',
          background: '#ffffff',
          color: '#101820'
        });
        return;
      }
    } else if (selectedMethod === 'PSE') {
      const bankSelect = document.getElementById('pse-bank');
      const emailInput = document.getElementById('pse-email');

      const bank = bankSelect?.value;
      const email = (emailInput?.value || '').trim();
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

      let hasError = false;
      let errorMsg = '';

      if (!bank) {
        bankSelect?.classList.add('is-invalid');
        hasError = true;
        errorMsg = 'Debes seleccionar una entidad bancaria para continuar con PSE.';
      } else {
        bankSelect?.classList.remove('is-invalid');
      }

      if (!email || !emailRegex.test(email)) {
        emailInput?.classList.add('is-invalid');
        hasError = true;
        if (!errorMsg) errorMsg = 'Ingresa un correo electrónico registrado en PSE válido.';
      } else {
        emailInput?.classList.remove('is-invalid');
      }

      if (hasError) {
        Swal.fire({
          icon: 'warning',
          title: 'Datos de PSE Incompletos',
          text: errorMsg,
          confirmButtonColor: '#00843D',
          background: '#ffffff',
          color: '#101820'
        });
        return;
      }
    }

    // ESTADO DE CARGA EN EL BOTÓN DE CONFIRMACIÓN
    const btnSubmit = document.getElementById('btn-submit-payment');
    const originalBtnHtml = btnSubmit ? btnSubmit.innerHTML : '';
    if (btnSubmit) {
      btnSubmit.disabled = true;
      btnSubmit.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span> Procesando Pago Seguro...';
    }

    Swal.fire({
      title: 'Procesando Pago Seguro...',
      text: 'Conectando con la pasarela de pagos, validando fondos y emitiendo factura electrónica con la DIAN...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
      background: '#ffffff',
      color: '#101820'
    });

    try {
      const effectiveClientId = user.clientId || user.id;

      // 1. Crear Reserva con dirección de obra y notas para operador de cama baja
      const rentalNotes = [
        `Reserva para ${activeToolForBooking.name}`,
        camaBajaNotes ? `Instrucciones cama baja: ${camaBajaNotes}` : ''
      ].filter(Boolean).join(' | ');

      const rental = await API.rentals.create({
        toolId: activeToolForBooking.id,
        clientId: effectiveClientId,
        startDate: q.startDate,
        endDate: q.endDate,
        deliveryAddress: deliveryAddress,
        notes: rentalNotes
      });

      // 2. Generar Factura con desglose fiscal
      const invoice = await API.invoices.generate({
        rentalId: rental.id,
        clientId: effectiveClientId,
        clientName: user.fullName || user.username,
        clientTaxId: user.clientProfile?.taxId || user.taxId || '900.000.000-1',
        depositAmount: q.deposit,
        items: [{
          toolId: activeToolForBooking.id,
          toolName: activeToolForBooking.name,
          dailyRate: activeToolForBooking.dailyRate,
          days: q.days
        }]
      });

      // 3. Procesar Pago
      const payment = await API.payments.process({
        invoiceId: invoice.id,
        rentalId: rental.id,
        clientId: effectiveClientId,
        amount: q.total,
        paymentMethod: selectedMethod,
        transactionReference: `PAY-${Date.now().toString().slice(-6)}`,
        notes: `Pago verificado vía ${selectedMethod} para ${activeToolForBooking.name}`
      });

      // 4. Confirmar automáticamente el estado de la reserva al estar pagada
      await API.rentals.updateStatus(rental.id, 'CONFIRMADA');

      // Limpiar cotización activa del carrito
      activeToolForBooking = null;
      updateCartBadge();

      // Actualizar datos en memoria y vistas
      await loadTools();
      loadClientRentals();
      loadProviderRequests();

      // Cerrar modal de pago
      const payModal = bootstrap.Modal.getInstance(document.getElementById('paymentModal'));
      if (payModal) payModal.hide();

      // Restaurar estado del botón
      if (btnSubmit) {
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = originalBtnHtml;
      }

      // SWEETALERT DE ÉXITO CON NÚMERO DE FACTURA Y BOTÓN DIRECTO PARA DESCARGAR EL PDF
      const pdfUrl = API.invoices.downloadPdfUrl(invoice.id);
      Swal.fire({
        icon: 'success',
        title: '¡Pago y Reserva Confirmados!',
        html: `
          <div class="text-start p-2">
            <p class="text-dark mb-2">Tu transacción <strong>${payment.transactionReference}</strong> ha sido aprobada exitosamente a través de <strong>${selectedMethod}</strong>.</p>
            <div class="d-flex align-items-center gap-2 mb-3">
              <span class="badge bg-success px-3 py-2 fs-6"><i class="fa fa-check-circle me-1"></i> Estado: CONFIRMADA</span>
              <span class="badge bg-dark px-3 py-2 fs-6"><i class="fa fa-file-invoice me-1"></i> Factura Nº: ${invoice.invoiceNumber || 'FAC-' + invoice.id}</span>
            </div>
            
            <div class="bg-light p-3 rounded border mb-3 small">
              <div class="d-flex justify-content-between mb-1">
                <span>Maquinaria:</span>
                <strong class="text-dark">${rental.toolName || activeToolForBooking?.name || 'Equipo Industrial'}</strong>
              </div>
              <div class="d-flex justify-content-between mb-1">
                <span>Frente de Obra:</span>
                <strong class="text-dark text-truncate ms-2" style="max-width: 250px;">${deliveryAddress}</strong>
              </div>
              <div class="d-flex justify-content-between mb-1">
                <span>Total Pagado:</span>
                <strong class="text-success">${API.formatCurrency(q.total)}</strong>
              </div>
            </div>

            <div class="text-center mt-3">
              <a href="${pdfUrl}" target="_blank" class="btn btn-sb-green w-100 py-2 fw-bold shadow-sm" id="btn-download-pdf">
                <i class="fa fa-file-pdf me-2"></i> Descargar Factura Oficial PDF
              </a>
              <div class="text-muted small mt-1">Generada en vivo por el backend (${pdfUrl})</div>
            </div>
          </div>
        `,
        confirmButtonColor: '#00843D',
        confirmButtonText: (user.role === 'ROLE_ADMIN') ? 'Ver Alquileres Activos' : 'Ver mis Alquileres',
        background: '#ffffff',
        color: '#101820'
      }).then(() => {
        switchView('client-dashboard');
      });

    } catch (err) {
      if (btnSubmit) {
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = originalBtnHtml;
      }
      Swal.fire({
        icon: 'error',
        title: 'Error en la transacción',
        text: err.message || 'No se pudo procesar el pago.',
        confirmButtonColor: '#00843D',
        background: '#ffffff',
        color: '#101820'
      });
    }
  }

  function togglePaymentFields(method) {
    const cardFields = document.getElementById('card-fields');
    const pseFields = document.getElementById('pse-fields');
    const transferFields = document.getElementById('transfer-fields') || document.getElementById('trans-fields');
    if (cardFields) cardFields.style.display = (method === 'CREDIT_CARD' || method === 'DEBIT_CARD') ? 'block' : 'none';
    if (pseFields) pseFields.style.display = (method === 'PSE') ? 'block' : 'none';
    if (transferFields) transferFields.style.display = (method === 'BANK_TRANSFER') ? 'block' : 'none';
  }

  // --- DASHBOARDS POR ROL ---

  async function downloadInvoicePdfForRental(rentalId) {
    try {
      const inv = await API.invoices.getByRental(rentalId);
      if (inv && inv.id) {
        window.open(API.invoices.downloadPdfUrl(inv.id), '_blank');
      } else {
        window.open(API.invoices.downloadPdfUrl(rentalId), '_blank');
      }
    } catch (e) {
      window.open(API.invoices.downloadPdfUrl(rentalId), '_blank');
    }
  }

  async function loadClientRentals() {
    const container = document.getElementById('client-rentals-list');
    if (!container) return;

    try {
      const rentals = await API.rentals.getAll();
      cachedRentals = rentals || [];
      if (rentals.length === 0) {
        container.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No tienes alquileres registrados todavía</td></tr>`;
        return;
      }

      container.innerHTML = rentals.map(r => {
        const tool = allTools.find(t => String(t.id) === String(r.toolId));
        const toolName = tool ? tool.name : (r.toolName || `Maquinaria #${r.toolId}`);
        return `
        <tr>
          <td><span class="fw-bold text-light">#${r.id}</span></td>
          <td><span class="text-warning fw-semibold">${toolName}</span></td>
          <td>${r.startDate} al ${r.endDate} <small class="text-muted">(${r.days} d)</small></td>
          <td class="fw-bold">${API.formatCurrency(r.total)}</td>
          <td>
            <span class="badge ${r.status === 'CONFIRMADA' ? 'badge-available' : (r.status === 'DEVUELTO' || r.status === 'COMPLETADA' ? 'bg-secondary' : 'badge-rented')}">
              ${r.status}
            </span>
          </td>
          <td>
            <button onclick="App.downloadInvoicePdfForRental('${r.id}')" class="btn btn-sm btn-neu py-1 px-3" title="Descargar comprobante en PDF">
              <i class="fa fa-file-pdf text-danger me-1"></i> PDF
            </button>
          </td>
        </tr>
      `;
      }).join('');
    } catch (e) {
      console.warn('Error cargando alquileres cliente:', e);
    }
  }

  async function loadProviderRequests() {
    const container = document.getElementById('provider-requests-list');
    if (!container) return;

    try {
      const rentals = await API.rentals.getAll();
      cachedRentals = rentals || [];
      if (!rentals || rentals.length === 0) {
        container.innerHTML = `<tr><td colspan="7" class="text-center text-slate-300 py-4"><i class="fa fa-inbox me-2 text-warning"></i>No hay solicitudes de alquiler registradas actualmente</td></tr>`;
        return;
      }

      container.innerHTML = rentals.map(r => {
        const tool = allTools.find(t => String(t.id) === String(r.toolId));
        const toolName = tool ? tool.name : (r.toolName || `Maquinaria #${r.toolId}`);
        return `
        <tr>
          <td>#${r.id}</td>
          <td class="fw-bold text-light">${toolName}</td>
          <td>${r.clientName || 'Cliente Constructora'}</td>
          <td>${r.days} días</td>
          <td>${API.formatCurrency(r.total)}</td>
          <td><span class="badge ${r.status === 'CONFIRMADA' ? 'badge-available' : (r.status === 'COMPLETADA' ? 'bg-secondary' : 'badge-rented')}">${r.status}</span></td>
          <td>
            <div class="provider-actions-cell">
            ${r.status === 'PENDIENTE_PAGO' || r.status === 'PENDIENTE' ? `
              <button class="btn btn-sm btn-success py-1 px-2" onclick="App.updateRentalStatus('${r.id}', 'CONFIRMADA')">Aprobar</button>
              <button class="btn btn-sm btn-danger py-1 px-2" onclick="App.updateRentalStatus('${r.id}', 'RECHAZADA')">Rechazar</button>
            ` : (r.status === 'CONFIRMADA' ? `
              <button class="btn btn-sm btn-neu-cyan py-1 px-2 text-nowrap" onclick="App.openReturnModal('${r.id}')">
                <i class="fa fa-clipboard-check me-1"></i> Inspección Devolución
              </button>
            ` : `<span class="small text-muted">Completado</span>`)}
            </div>
          </td>
        </tr>
      `;
      }).join('');
    } catch (e) {
      console.warn('Error cargando solicitudes del proveedor:', e);
    }
  }

  async function updateRentalStatus(rentalId, newStatus) {
    try {
      await API.rentals.updateStatus(rentalId, newStatus);
      Swal.fire({
        icon: 'success',
        title: 'Estado Actualizado',
        text: `La reserva #${rentalId} ahora está ${newStatus}`,
        timer: 2000,
        showConfirmButton: false,
        background: '#0f172a',
        color: '#f8fafc'
      });
      await loadTools();
      loadClientRentals();
      loadProviderRequests();
    } catch (e) {
      Swal.fire('Error', e.message, 'error');
    }
  }

  function openReturnModal(rentalId) {
    document.getElementById('return-rental-id').value = rentalId;

    // Buscar detalles de la reserva para poblar la ficha técnica de devolución
    const rentalBadge = document.getElementById('return-badge-id');
    const toolNameEl = document.getElementById('return-tool-name');
    const clientInfoEl = document.getElementById('return-client-info');
    const depositValEl = document.getElementById('return-deposit-val');

    if (rentalBadge) rentalBadge.textContent = `Reserva #${rentalId}`;

    const r = cachedRentals.find(x => String(x.id) === String(rentalId));
    if (r) {
      const tool = allTools.find(t => String(t.id) === String(r.toolId));
      const toolName = tool ? tool.name : (r.toolName || 'Maquinaria Pesada');
      if (toolNameEl) toolNameEl.textContent = toolName;
      if (clientInfoEl) clientInfoEl.textContent = `Cliente: ${r.clientName || 'Cliente Constructora'} | Duración: ${r.days || 3} días (${r.startDate} al ${r.endDate})`;
      if (depositValEl) depositValEl.textContent = API.formatCurrency(r.depositAmount || (tool?.depositAmount) || 0);
    } else {
      if (toolNameEl) toolNameEl.textContent = 'Maquinaria Pesada en Devolución';
      if (clientInfoEl) clientInfoEl.textContent = 'Frente de Obra / Bogotá & Sabana';
      if (depositValEl) depositValEl.textContent = '$ 0';
    }

    // Resetear campos del formulario de inspección técnica
    const horometroInput = document.getElementById('return-horometro');
    if (horometroInput) {
      horometroInput.value = '';
      horometroInput.classList.remove('is-invalid');
    }
    const fuel100 = document.getElementById('fuel-100');
    if (fuel100) fuel100.checked = true;

    const hydraulicSelect = document.getElementById('return-hydraulic-status');
    if (hydraulicSelect) hydraulicSelect.selectedIndex = 0;

    const damageCheck = document.getElementById('damage-check');
    if (damageCheck) {
      damageCheck.checked = false;
      const damageContainer = document.getElementById('damage-cost-container');
      if (damageContainer) damageContainer.style.display = 'none';
    }
    const damageCost = document.getElementById('damage-cost');
    if (damageCost) {
      damageCost.value = '';
      damageCost.classList.remove('is-invalid');
    }

    const notesInput = document.getElementById('return-notes');
    if (notesInput) notesInput.value = '';

    const user = API.getUser();
    const inspectorInput = document.getElementById('return-inspector-name');
    if (inspectorInput) {
      inspectorInput.value = user?.fullName || user?.username || 'Ing. Carlos Mendoza - Jefe de Patio';
    }

    const modal = new bootstrap.Modal(document.getElementById('returnInspectionModal'));
    modal.show();
  }

  async function submitReturnInspection() {
    const rentalId = document.getElementById('return-rental-id').value;
    const horometroInput = document.getElementById('return-horometro');
    const horometroVal = (horometroInput?.value || '').trim();

    if (!horometroVal || isNaN(parseFloat(horometroVal)) || parseFloat(horometroVal) < 0) {
      horometroInput?.classList.add('is-invalid');
      Swal.fire({
        icon: 'warning',
        title: 'Horómetro Requerido',
        text: 'Por favor ingresa la lectura del horómetro de motor al momento de la entrega técnica.',
        confirmButtonColor: '#00843D',
        background: '#ffffff',
        color: '#101820'
      });
      return;
    }
    horometroInput?.classList.remove('is-invalid');

    const fuelLevel = document.querySelector('input[name="returnFuelLevel"]:checked')?.value || '100% (Lleno)';
    const hydraulicStatus = document.getElementById('return-hydraulic-status')?.value || 'Óptimo';
    const inspectorName = (document.getElementById('return-inspector-name')?.value || '').trim() || 'Inspector Técnico';
    const rawNotes = (document.getElementById('return-notes')?.value || '').trim();

    const damageDetected = document.getElementById('damage-check').checked;
    const damageType = damageDetected ? document.getElementById('return-damage-type')?.value : 'Ninguno';
    const damageCostInput = document.getElementById('damage-cost');
    const damageCost = damageDetected ? parseFloat(damageCostInput?.value || 0) : 0;

    if (damageDetected && (!damageCost || damageCost <= 0)) {
      damageCostInput?.classList.add('is-invalid');
      Swal.fire({
        icon: 'warning',
        title: 'Costo de Reparación Requerido',
        text: 'Al reportar averías fuera del desgaste normal, debes indicar el valor estimado a deducir de la garantía.',
        confirmButtonColor: '#00843D',
        background: '#ffffff',
        color: '#101820'
      });
      return;
    }
    damageCostInput?.classList.remove('is-invalid');

    // Compilación estructurada de la Ficha Técnica de Inspección
    const compiledInspectionReport = [
      `[ACTA TÉCNICA DE DEVOLUCIÓN]`,
      `Horómetro Final: ${horometroVal} hrs`,
      `Combustible: ${fuelLevel}`,
      `Sistema Hidráulico: ${hydraulicStatus}`,
      `Inspector: ${inspectorName}`,
      damageDetected ? `Daño Reportado: ${damageType} (Cargo garantía: ${API.formatCurrency(damageCost)})` : `Condición Mecánica: Desgaste normal (Sin averías)`,
      rawNotes ? `Observaciones: ${rawNotes}` : ''
    ].filter(Boolean).join(' | ');

    try {
      await API.rentals.return(rentalId, compiledInspectionReport, damageDetected, damageCost);
      const inspectionModalEl = document.getElementById('returnInspectionModal');
      const inspectionModal = bootstrap.Modal.getInstance(inspectionModalEl);
      if (inspectionModal) inspectionModal.hide();

      Swal.fire({
        icon: 'success',
        title: 'Acta de Devolución & Inspección Registrada',
        html: `
          <div class="text-start p-2">
            <p class="text-dark mb-2">La maquinaria de la reserva <strong>#${rentalId}</strong> ha sido inspeccionada e ingresada a patio satisfactoriamente.</p>
            <div class="bg-light p-3 rounded border mb-2 small">
              <div><strong>Horómetro registrado:</strong> ${horometroVal} horas</div>
              <div><strong>Nivel de Combustible:</strong> ${fuelLevel}</div>
              <div><strong>Sistema Hidráulico:</strong> ${hydraulicStatus}</div>
              <div><strong>Inspector a cargo:</strong> ${inspectorName}</div>
              <hr class="my-2">
              ${damageDetected ? `
                <div class="text-danger fw-bold"><i class="fa fa-exclamation-triangle me-1"></i> Daño extraordinario: ${damageType}</div>
                <div class="text-danger">Deducción de garantía: <strong>${API.formatCurrency(damageCost)}</strong></div>
              ` : `
                <div class="text-success fw-bold"><i class="fa fa-check-circle me-1"></i> Sin averías extraordinarias. Depósito de garantía liberado 100% para desembolso al cliente.</div>
              `}
            </div>
          </div>
        `,
        confirmButtonColor: '#00843D',
        background: '#ffffff',
        color: '#101820'
      });

      await loadTools();
      loadClientRentals();
      loadProviderRequests();
    } catch (e) {
      Swal.fire({
        icon: 'error',
        title: 'Error al registrar acta',
        text: e.message || 'No se pudo registrar la devolución técnica.',
        confirmButtonColor: '#00843D',
        background: '#ffffff',
        color: '#101820'
      });
    }
  }

  async function loadAdminDashboard() {
    try {
      const data = await API.reports.getDashboard();
      document.getElementById('admin-total-revenue').textContent = API.formatCurrency(data.totalRevenue);
      document.getElementById('admin-active-rentals').textContent = data.activeRentals;
      document.getElementById('admin-occupancy').textContent = `${data.occupancyRate}%`;
      document.getElementById('admin-total-users').textContent = data.totalUsers;

      // Render actividades
      const actList = document.getElementById('admin-activities-list');
      if (actList && data.recentActivities) {
        actList.innerHTML = data.recentActivities.map(a => `
          <li class="list-group-item admin-activity-item text-light d-flex flex-column flex-sm-row justify-content-between align-items-start align-items-sm-center gap-2">
            <div>
              <strong class="text-warning">${a.type}</strong>: <span class="text-slate-300 small">${a.description}</span>
            </div>
            <span class="badge bg-dark text-warning border border-secondary border-opacity-50 text-nowrap">${a.time}</span>
          </li>
        `).join('');
      }
    } catch (e) {
      console.warn('Error cargando KPIs admin:', e);
    }
  }

  // --- VISTAS & SESIÓN ---

  function switchView(viewName) {
    document.querySelectorAll('.app-view').forEach(v => v.style.display = 'none');
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

    const targetView = document.getElementById(`view-${viewName}`);
    if (targetView) targetView.style.display = 'block';

    const targetNavLink = document.getElementById(`nav-${viewName}-link`) || document.getElementById(`nav-${viewName}`);
    if (targetNavLink) {
      if (targetNavLink.classList.contains('nav-link')) {
        targetNavLink.classList.add('active');
      } else {
        targetNavLink.querySelector('.nav-link')?.classList.add('active');
      }
    }

    if (viewName === 'catalog') {
      const mapCollapseEl = document.getElementById('map-collapse-container');
      if (mapCollapseEl && mapCollapseEl.classList.contains('show')) {
        setTimeout(() => {
          if (typeof MapManager !== 'undefined' && MapManager) {
            MapManager.init();
            if (MapManager.invalidateSize) MapManager.invalidateSize();
          }
        }, 150);
      }
    } else if (viewName === 'client-dashboard') {
      loadClientRentals();
    } else if (viewName === 'provider-dashboard') {
      loadProviderRequests();
    } else if (viewName === 'admin-dashboard') {
      loadAdminDashboard();
    }
  }

  function checkAuthStatus() {
    const user = API.getUser();
    const guestNav = document.getElementById('nav-guest-actions');
    const userNav = document.getElementById('nav-user-actions');
    const avatarEl = document.getElementById('nav-user-avatar');
    const shortNameEl = document.getElementById('nav-user-short-name');
    const dropdownNameEl = document.getElementById('nav-user-dropdown-name');
    const dropdownEmailEl = document.getElementById('nav-user-dropdown-email');
    const roleBadge = document.getElementById('nav-user-role-badge');
    const clientDropdownItem = document.getElementById('dropdown-client-item');
    const providerDropdownItem = document.getElementById('dropdown-provider-item');
    const dropdownAddToolItem = document.getElementById('dropdown-add-tool-item');
    const adminDropdownItem = document.getElementById('dropdown-admin-item');

    if (user) {
      if (guestNav) {
        guestNav.style.setProperty('display', 'none', 'important');
        guestNav.classList.add('d-none');
      }
      if (userNav) {
        userNav.style.setProperty('display', 'block', 'important');
        userNav.classList.remove('d-none');
      }

      const displayName = user.fullName || user.username || 'Usuario';
      // Primer nombre para la píldora compacta
      const firstName = displayName.trim().split(' ')[0];

      if (avatarEl) {
        avatarEl.textContent = firstName.charAt(0).toUpperCase();
      }
      if (shortNameEl) {
        shortNameEl.textContent = firstName;
      }
      if (dropdownNameEl) {
        dropdownNameEl.textContent = displayName;
      }
      if (dropdownEmailEl) {
        dropdownEmailEl.textContent = user.email || `${user.username.toLowerCase()}@rentatools.com`;
      }
      const cleanRole = user.role ? user.role.replace('ROLE_', '') : 'CLIENTE';
      const rolePill = document.getElementById('nav-user-role-badge-pill');

      if (roleBadge) {
        roleBadge.textContent = cleanRole;
        if (cleanRole === 'PROVEEDOR') {
          roleBadge.className = 'badge bg-warning text-dark fw-bold px-2 py-1';
        } else if (cleanRole === 'ADMIN') {
          roleBadge.className = 'badge bg-primary text-white fw-bold px-2 py-1';
        } else {
          roleBadge.className = 'badge bg-success text-white fw-bold px-2 py-1';
        }
      }

      if (rolePill) {
        rolePill.textContent = cleanRole;
        if (cleanRole === 'PROVEEDOR') {
          rolePill.className = 'badge rounded-pill bg-warning text-dark border ms-1';
        } else if (cleanRole === 'ADMIN') {
          rolePill.className = 'badge rounded-pill bg-primary text-white border ms-1';
        } else {
          rolePill.className = 'badge rounded-pill bg-success text-white border ms-1';
        }
      }

      // Habilitar pestañas contextuales según el rol
      const clientNav = document.getElementById('nav-client-dashboard');
      const providerNav = document.getElementById('nav-provider-dashboard');
      const adminNav = document.getElementById('nav-admin-dashboard');

      if (clientNav) clientNav.style.display = (user.role === 'ROLE_CLIENTE' || user.role === 'ROLE_ADMIN') ? 'block' : 'none';
      if (providerNav) providerNav.style.display = (user.role === 'ROLE_PROVEEDOR' || user.role === 'ROLE_ADMIN') ? 'block' : 'none';
      if (adminNav) adminNav.style.display = (user.role === 'ROLE_ADMIN') ? 'block' : 'none';

      // Rótulos contextuales según el rol (Admin -> "Alquileres Activos", Cliente -> "Mis Alquileres")
      const clientNavLabel = document.getElementById('nav-client-dashboard-label');
      const clientDropdownLabel = document.getElementById('dropdown-client-label');
      const clientHeaderTitle = document.getElementById('client-dashboard-header-title');

      if (user.role === 'ROLE_ADMIN') {
        if (clientNavLabel) clientNavLabel.textContent = 'Alquileres Activos';
        if (clientDropdownLabel) clientDropdownLabel.textContent = 'Alquileres Activos & Facturas';
        if (clientHeaderTitle) clientHeaderTitle.textContent = 'Alquileres Activos & Facturación';
      } else {
        if (clientNavLabel) clientNavLabel.textContent = 'Mis Alquileres';
        if (clientDropdownLabel) clientDropdownLabel.textContent = 'Mis Alquileres & Facturas';
        if (clientHeaderTitle) clientHeaderTitle.textContent = 'Mis Alquileres & Facturación';
      }

      // Enlaces del menú desplegable del usuario: Mis Alquileres & Facturas, Gestión Proveedor, Añadir Maquinaria
      if (clientDropdownItem) clientDropdownItem.style.display = 'block';
      if (providerDropdownItem) providerDropdownItem.style.display = 'block';
      if (dropdownAddToolItem) dropdownAddToolItem.style.display = 'block';
      if (adminDropdownItem) adminDropdownItem.style.display = (user.role === 'ROLE_ADMIN') ? 'block' : 'none';

      // Botón rápido en la barra superior para añadir maquinaria (Solo PROVEEDOR o ADMIN)
      const isProvider = (user.role === 'ROLE_PROVEEDOR' || user.role === 'ROLE_ADMIN');
      const navAddToolBtn = document.getElementById('nav-add-tool-btn');
      if (navAddToolBtn) navAddToolBtn.style.display = isProvider ? 'inline-flex' : 'none';
    } else {
      if (guestNav) {
        guestNav.style.setProperty('display', 'flex', 'important');
        guestNav.classList.remove('d-none');
      }
      if (userNav) {
        userNav.style.setProperty('display', 'none', 'important');
        userNav.classList.add('d-none');
      }

      const clientNav = document.getElementById('nav-client-dashboard');
      const providerNav = document.getElementById('nav-provider-dashboard');
      const adminNav = document.getElementById('nav-admin-dashboard');
      if (clientNav) clientNav.style.display = 'none';
      if (providerNav) providerNav.style.display = 'none';
      if (adminNav) adminNav.style.display = 'none';

      if (clientDropdownItem) clientDropdownItem.style.display = 'none';
      if (providerDropdownItem) providerDropdownItem.style.display = 'none';
      if (adminDropdownItem) adminDropdownItem.style.display = 'none';

      const navAddToolBtn = document.getElementById('nav-add-tool-btn');
      if (navAddToolBtn) navAddToolBtn.style.display = 'none';
      if (dropdownAddToolItem) dropdownAddToolItem.style.display = 'none';

      const clientNavLabel = document.getElementById('nav-client-dashboard-label');
      const clientDropdownLabel = document.getElementById('dropdown-client-label');
      const clientHeaderTitle = document.getElementById('client-dashboard-header-title');
      if (clientNavLabel) clientNavLabel.textContent = 'Mis Alquileres';
      if (clientDropdownLabel) clientDropdownLabel.textContent = 'Mis Alquileres & Facturas';
      if (clientHeaderTitle) clientHeaderTitle.textContent = 'Mis Alquileres & Facturación';
    }

    // Cargar notificaciones persistentes del usuario activo o limpiar para invitado
    if (typeof RealtimeManager !== 'undefined' && RealtimeManager.loadUserNotifications) {
      RealtimeManager.loadUserNotifications();
    }
  }

  function handleRoleChange() {
    const roleSelect = document.getElementById('reg-role');
    const companyContainer = document.getElementById('reg-company-container');
    const companyInput = document.getElementById('reg-company');
    const fullnameLabel = document.getElementById('reg-fullname-label');
    const fullnameInput = document.getElementById('reg-fullname');

    if (!roleSelect || !companyContainer) return;

    if (roleSelect.value === 'ROLE_PROVEEDOR') {
      companyContainer.style.display = 'block';
      if (companyInput) companyInput.required = true;
      if (fullnameLabel) fullnameLabel.textContent = 'Nombre del Representante Legal:';
      if (fullnameInput) fullnameInput.placeholder = 'Ej. Carlos Sánchez';
    } else {
      companyContainer.style.display = 'none';
      if (companyInput) {
        companyInput.required = false;
        companyInput.value = '';
        companyInput.classList.remove('is-invalid');
      }
      if (fullnameLabel) fullnameLabel.textContent = 'Nombre Completo:';
      if (fullnameInput) fullnameInput.placeholder = 'Ej. Juan Pérez';
    }
  }

  function openAuthModal(mode = 'login') {
    const isLogin = mode === 'login';
    const titleEl = document.getElementById('auth-modal-title') || document.getElementById('authModalLabel');
    if (titleEl) titleEl.textContent = isLogin ? 'Iniciar Sesión en RentaTools' : 'Crear Cuenta Profesional';
    const loginFields = document.getElementById('login-fields');
    if (loginFields) loginFields.style.display = isLogin ? 'block' : 'none';
    const registerFields = document.getElementById('register-fields');
    if (registerFields) registerFields.style.display = isLogin ? 'none' : 'block';
    const submitBtn = document.getElementById('btn-auth-submit');
    if (submitBtn) submitBtn.textContent = isLogin ? 'Entrar al Sistema' : 'Completar Registro';
    const toggleBtn = document.getElementById('auth-mode-toggle');
    if (toggleBtn) toggleBtn.textContent = isLogin ? '¿No tienes cuenta? Regístrate aquí' : '¿Ya tienes cuenta? Inicia sesión aquí';
    const authModalEl = document.getElementById('authModal');
    if (authModalEl) authModalEl.dataset.mode = isLogin ? 'login' : 'register';

    if (!isLogin) {
      handleRoleChange();
    }

    const modal = new bootstrap.Modal(document.getElementById('authModal'));
    modal.show();
  }

  function toggleAuthMode() {
    const currentMode = document.getElementById('authModal').dataset.mode;
    openAuthModal(currentMode === 'login' ? 'register' : 'login');
  }

  async function submitAuth() {
    const mode = document.getElementById('authModal').dataset.mode;
    if (mode === 'login') {
      const id = document.getElementById('login-id').value;
      const pass = document.getElementById('login-pass').value;
      if (!id || !pass) {
        Swal.fire('Atención', 'Por favor ingresa usuario y contraseña', 'warning');
        return;
      }
      try {
        const resp = await API.auth.login(id, pass);
        API.setToken(resp.token);
        API.setUser({
          id: resp.id,
          clientId: resp.clientId,
          providerId: resp.providerId,
          username: resp.username,
          fullName: resp.fullName,
          role: resp.role
        });

        bootstrap.Modal.getInstance(document.getElementById('authModal')).hide();
        checkAuthStatus();

        Swal.fire({
          icon: 'success',
          title: `¡Bienvenido, ${resp.fullName || resp.username}!`,
          text: `Sesión iniciada con rol: ${resp.role}`,
          timer: 2000,
          showConfirmButton: false,
          background: '#0f172a',
          color: '#f8fafc'
        });

        RealtimeManager.init();
      } catch (err) {
        Swal.fire({
          icon: 'error',
          title: 'Error de Autenticación',
          text: err.message,
          background: '#0f172a',
          color: '#f8fafc'
        });
      }
    } else {
      // Registro Dinámico
      const role = document.getElementById('reg-role')?.value || 'ROLE_CLIENTE';
      const companyInput = document.getElementById('reg-company');
      const companyName = (companyInput?.value || '').trim();

      // Validación condicional de empresa según el rol
      if (role === 'ROLE_PROVEEDOR' && !companyName) {
        companyInput?.classList.add('is-invalid');
        Swal.fire({
          icon: 'warning',
          title: 'Campo Requerido',
          text: 'El nombre de la empresa o razón social es obligatorio para proveedores de maquinaria.',
          confirmButtonColor: '#f59e0b',
          background: '#0f172a',
          color: '#f8fafc'
        });
        return;
      }

      const username = document.getElementById('reg-username')?.value?.trim();
      const email = document.getElementById('reg-email')?.value?.trim();
      const password = document.getElementById('reg-password')?.value;

      if (!username || !email || !password) {
        Swal.fire('Atención', 'Por favor completa usuario, correo y contraseña.', 'warning');
        return;
      }

      const user = {
        username,
        email,
        password,
        fullName: (document.getElementById('reg-fullname')?.value || '').trim() || username,
        phone: (document.getElementById('reg-phone')?.value || '').trim(),
        role: role,
        companyName: role === 'ROLE_PROVEEDOR' ? companyName : '',
        taxId: (document.getElementById('reg-taxid')?.value || '').trim(),
        address: (document.getElementById('reg-address')?.value || '').trim(),
        city: 'Bogotá'
      };

      try {
        const resp = await API.auth.register(user);
        API.setToken(resp.token);
        API.setUser({
          id: resp.id,
          clientId: resp.clientId,
          providerId: resp.providerId,
          username: resp.username,
          fullName: resp.fullName,
          role: resp.role
        });

        bootstrap.Modal.getInstance(document.getElementById('authModal')).hide();
        checkAuthStatus();

        Swal.fire({
          icon: 'success',
          title: 'Registro Completado',
          text: 'Tu cuenta ha sido creada exitosamente.',
          background: '#0f172a',
          color: '#f8fafc'
        });

        RealtimeManager.init();
      } catch (err) {
        Swal.fire({
          icon: 'error',
          title: 'Error en el Registro',
          text: err.message,
          background: '#0f172a',
          color: '#f8fafc'
        });
      }
    }
  }

  function openAddToolModal() {
    const user = API.getUser();
    if (!user || (user.role !== 'ROLE_PROVEEDOR' && user.role !== 'ROLE_ADMIN')) {
      Swal.fire({
        icon: 'warning',
        title: 'Acceso Restringido',
        text: 'Debes iniciar sesión con una cuenta de Proveedor para publicar maquinaria en la plataforma.',
        confirmButtonColor: '#f59e0b',
        confirmButtonText: 'Iniciar Sesión',
        background: '#0f172a',
        color: '#f8fafc'
      }).then(() => {
        openAuthModal('login');
      });
      return;
    }

    const form = document.getElementById('form-add-tool');
    if (form) form.reset();

    const codeInput = document.getElementById('add-tool-code');
    if (codeInput) {
      codeInput.value = `MAQ-${Math.floor(1000 + Math.random() * 9000)}`;
    }

    const select = document.getElementById('add-tool-image-select');
    if (select) {
      handleNewToolImageSelect(select.value);
    }

    const modal = new bootstrap.Modal(document.getElementById('addToolModal'));
    modal.show();
  }

  function handleNewToolImageSelect(val) {
    const customInput = document.getElementById('add-tool-image-url');
    const previewImg = document.getElementById('add-tool-preview-img');
    if (val === '__custom__') {
      if (customInput) {
        customInput.style.display = 'block';
        customInput.oninput = () => {
          if (previewImg) previewImg.src = customInput.value || window.TOOL_FALLBACK_IMG;
        };
      }
      if (previewImg) previewImg.src = customInput?.value || window.TOOL_FALLBACK_IMG;
    } else {
      if (customInput) customInput.style.display = 'none';
      if (previewImg) previewImg.src = val;
    }
  }

  async function submitNewTool(e) {
    if (e && e.preventDefault) e.preventDefault();

    const user = API.getUser();
    if (!user) return;

    const name = document.getElementById('add-tool-name')?.value?.trim();
    const categoryId = document.getElementById('add-tool-category')?.value;
    const brand = document.getElementById('add-tool-brand')?.value?.trim() || 'Genérica';
    const model = document.getElementById('add-tool-model')?.value?.trim() || 'Estándar';
    let code = document.getElementById('add-tool-code')?.value?.trim();
    if (!code) code = `MAQ-${Date.now().toString().slice(-6)}`;

    const dailyPrice = parseFloat(document.getElementById('add-tool-price')?.value);
    const depositAmount = parseFloat(document.getElementById('add-tool-deposit')?.value);
    const lat = parseFloat(document.getElementById('add-tool-lat')?.value) || 4.6534;
    const lng = parseFloat(document.getElementById('add-tool-lng')?.value) || -74.0836;

    const imgSelect = document.getElementById('add-tool-image-select')?.value;
    let imageUrl = imgSelect;
    if (imgSelect === '__custom__') {
      imageUrl = document.getElementById('add-tool-image-url')?.value?.trim() || 'assets/img/machinery/retroexcavadora-cat-420f2.jpg';
    }

    const powerSpec = document.getElementById('add-tool-spec-power')?.value?.trim();
    const fuelSpec = document.getElementById('add-tool-spec-fuel')?.value;

    const specifications = [];
    if (powerSpec) specifications.push({ key: 'Potencia / Capacidad', value: powerSpec });
    if (fuelSpec) specifications.push({ key: 'Tipo de Energía', value: fuelSpec });

    if (!name || isNaN(dailyPrice) || dailyPrice <= 0 || isNaN(depositAmount)) {
      Swal.fire('Atención', 'Por favor diligencia todos los campos obligatorios correctamente.', 'warning');
      return;
    }

    Swal.fire({
      title: 'Publicando Maquinaria...',
      text: 'Guardando especificaciones técnicas e incorporando al catálogo oficial...',
      allowOutsideClick: false,
      didOpen: () => { Swal.showLoading(); },
      background: '#0f172a',
      color: '#f8fafc'
    });

    try {
      const payload = {
        providerId: user.providerId || user.id || 1,
        categoryId: parseInt(categoryId, 10),
        code,
        name,
        brand,
        model,
        dailyPrice,
        depositAmount,
        latitude: lat,
        longitude: lng,
        imageUrls: [imageUrl],
        specifications
      };

      await API.tools.create(payload);

      // Cerrar modal
      const modalEl = document.getElementById('addToolModal');
      const modalInstance = bootstrap.Modal.getInstance(modalEl);
      if (modalInstance) modalInstance.hide();

      // Recargar catálogo y mapa
      await loadTools();
      if (typeof MapManager !== 'undefined' && MapManager.updateToolsOnMap) {
        MapManager.updateToolsOnMap(filteredTools);
      }

      Swal.fire({
        icon: 'success',
        title: '¡Maquinaria Publicada con Éxito!',
        text: `El equipo "${name}" ya se encuentra activo en el catálogo público y disponible para alquiler.`,
        confirmButtonColor: '#f59e0b',
        background: '#0f172a',
        color: '#f8fafc'
      });
    } catch (err) {
      console.error('Error publicando maquinaria:', err);
      Swal.fire({
        icon: 'error',
        title: 'Error al Publicar Maquinaria',
        text: err.message || 'No fue posible registrar la maquinaria en el sistema.',
        background: '#0f172a',
        color: '#f8fafc'
      });
    }
  }

  function logout() {
    API.setToken(null);
    API.setUser(null);
    if (typeof RealtimeManager !== 'undefined' && RealtimeManager.disconnect) {
      RealtimeManager.disconnect();
    }
    checkAuthStatus();
    switchView('catalog');
    Swal.fire({
      icon: 'info',
      title: 'Sesión Cerrada',
      timer: 1500,
      showConfirmButton: false,
      background: '#ffffff',
      color: '#101820'
    });
  }

  function openChatSupport() {
    Swal.fire({
      title: 'Atención al Cliente Sunbelt / RentaTools',
      html: `
        <div class="text-start">
          <p class="mb-3 text-dark">Nuestro equipo de soporte técnico y logística para obras está activo 24/7 en Bogotá D.C. y Sabana.</p>
          <div class="p-3 bg-light rounded-3 border mb-3">
            <div class="d-flex align-items-center gap-2 mb-2">
              <i class="fa fa-phone-alt text-success fa-lg"></i>
              <div><strong>Línea de Obras:</strong> (601) 745-9000</div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <i class="fab fa-whatsapp text-success fa-lg"></i>
              <div><strong>WhatsApp Inmediato:</strong> +57 310 999 8888</div>
            </div>
          </div>
          <p class="small text-muted mb-0">¿Necesitas despacho prioritario en cama baja o asesoría técnica para tu obra? Chatea con nosotros.</p>
        </div>
      `,
      showCancelButton: true,
      confirmButtonText: '<i class="fab fa-whatsapp me-1"></i> Abrir WhatsApp',
      confirmButtonColor: '#00843D',
      cancelButtonText: 'Cerrar',
      cancelButtonColor: '#59636E'
    }).then((result) => {
      if (result.isConfirmed) {
        window.open('https://wa.me/573109998888?text=Hola,%20necesito%20cotizar%20maquinaria%20en%20RentaTools', '_blank');
      }
    });
  }

  return {
    init,
    openBookingModal,
    recalculateBookingQuote,
    proceedToPaymentFromBooking,
    submitPayment,
    switchView,
    openAuthModal,
    toggleAuthMode,
    handleRoleChange,
    submitAuth,
    openAddToolModal,
    handleNewToolImageSelect,
    submitNewTool,
    logout,
    updateRentalStatus,
    openReturnModal,
    submitReturnInspection,
    setHeroDispatchMode,
    applyHeroBookingFilter,
    selectHeroLocation,
    showCoverageModal,
    handleContractorQuickAccess,
    handleProviderDashboardAccess,
    handleAddToolAccess,
    filterByIndustry,
    clearIndustryFilter,
    downloadCatalogPdf,
    filterByCategory,
    toggleMapSection,
    openCartModal,
    proceedToPaymentFromCart,
    modifyBookingFromCart,
    clearCart,
    downloadInvoicePdfForRental,
    openChatSupport
  };
})();

document.addEventListener('DOMContentLoaded', App.init);
