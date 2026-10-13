const TOOL_FALLBACK_SVG = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='600' height='400' viewBox='0 0 600 400'%3E%3Cdefs%3E%3ClinearGradient id='bg' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' stop-color='%230f172a'/%3E%3Cstop offset='100%25' stop-color='%231e293b'/%3E%3C/linearGradient%3E%3ClinearGradient id='gold' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' stop-color='%23f59e0b'/%3E%3Cstop offset='100%25' stop-color='%23d97706'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='600' height='400' fill='url(%23bg)'/%3E%3Ccircle cx='300' cy='160' r='55' fill='none' stroke='url(%23gold)' stroke-width='4' stroke-dasharray='6 6' opacity='0.6'/%3E%3Cpath d='M275 145 h50 v25 h-50 z M265 170 h70 v15 h-70 z M280 125 h40 v20 h-40 z' fill='url(%23gold)'/%3E%3Ctext x='300' y='265' text-anchor='middle' fill='%23f8fafc' font-family='sans-serif' font-size='20' font-weight='700'%3ERENTATOOLS PRO%3C/text%3E%3Ctext x='300' y='295' text-anchor='middle' fill='%2394a3b8' font-family='sans-serif' font-size='13'%3EEquipo de Construcción Verificado%3C/text%3E%3C/svg%3E";
window.TOOL_FALLBACK_IMG = TOOL_FALLBACK_SVG;

const App = (() => {
  let allTools = [];
  let filteredTools = [];
  let selectedCategory = 'TODAS';
  let activeToolForBooking = null;

  async function init() {
    setupEventListeners();
    checkAuthStatus();
    RealtimeManager.init();

    try {
      await loadTools();
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

    // Píldoras de categoría
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
    document.getElementById('book-start-date')?.addEventListener('change', recalculateBookingQuote);
    document.getElementById('book-end-date')?.addEventListener('change', recalculateBookingQuote);

    // Selector de método de pago
    document.querySelectorAll('input[name="paymentMethod"]').forEach(radio => {
      radio.addEventListener('change', (e) => {
        togglePaymentFields(e.target.value);
      });
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

  async function loadTools() {
    try {
      allTools = await API.tools.getAll();
      filteredTools = [...allTools];
      renderTools(filteredTools);
      MapManager.updateToolsOnMap(filteredTools);
    } catch (err) {
      console.error('Error cargando maquinaria:', err);
    }
  }

  function filterTools(query = '') {
    const q = query.trim().toLowerCase();
    filteredTools = allTools.filter(tool => {
      const matchCat = (selectedCategory === 'TODAS' || tool.category?.toUpperCase().includes(selectedCategory));
      const matchQuery = !q || tool.name.toLowerCase().includes(q) || tool.description.toLowerCase().includes(q) || tool.locationName.toLowerCase().includes(q);
      return matchCat && matchQuery;
    });
    renderTools(filteredTools);
    if (typeof MapManager !== 'undefined' && MapManager.updateToolsOnMap) {
      MapManager.updateToolsOnMap(filteredTools);
    }
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
        <div class="col-12 text-center py-5">
          <i class="fa fa-search fa-3x text-secondary mb-3"></i>
          <h5 class="text-light">No encontramos maquinaria con los filtros seleccionados</h5>
          <p class="text-muted">Prueba ampliando el radio de búsqueda o cambiando de categoría.</p>
        </div>`;
      return;
    }

    grid.innerHTML = tools.map(t => `
      <div class="col-md-6 col-lg-4 mb-4">
        <div class="card glass-card h-100 p-0 overflow-hidden">
          <div class="position-relative">
            <img src="${t.imageUrl || window.TOOL_FALLBACK_IMG}" class="card-img-top" style="height: 200px; object-fit: cover;" alt="${t.name}" onerror="this.onerror=null; this.src=window.TOOL_FALLBACK_IMG;">
            <div class="position-absolute top-0 start-0 m-3">
              <span class="badge ${t.status === 'DISPONIBLE' ? 'badge-available' : 'badge-rented'}">
                <i class="fa ${t.status === 'DISPONIBLE' ? 'fa-check-circle' : 'fa-clock'} me-1"></i> ${t.status}
              </span>
            </div>
            <div class="position-absolute top-0 end-0 m-3">
              <span class="badge bg-dark bg-opacity-75 text-light px-2 py-1 rounded">
                <i class="fa fa-map-marker-alt text-warning me-1"></i> ${t.locationName || 'Obra'}
              </span>
            </div>
          </div>
          <div class="card-body p-4 d-flex flex-column">
            <div class="small text-warning fw-bold mb-1">${t.category}</div>
            <h5 class="card-title text-light fw-bold">${t.name}</h5>
            <p class="card-text text-muted small flex-grow-1">${t.description}</p>
            
            <div class="p-3 my-3 rounded" style="background: rgba(0, 0, 0, 0.35); border: 1px solid rgba(255, 255, 255, 0.05);">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <span class="text-muted small">Tarifa diaria:</span>
                <span class="text-warning fw-bold">${API.formatCurrency(t.dailyRate)}</span>
              </div>
              <div class="d-flex justify-content-between align-items-center">
                <span class="text-muted small">Garantía retornable:</span>
                <span class="text-light small">${API.formatCurrency(t.depositAmount)}</span>
              </div>
            </div>

            <button class="btn btn-neu-amber w-100" onclick="App.openBookingModal(${t.id})">
              <i class="fa fa-calendar-check me-2"></i> Cotizar & Reservar
            </button>
          </div>
        </div>
      </div>
    `).join('');
  }

  function openBookingModal(toolId) {
    const user = API.getUser();
    if (!user) {
      Swal.fire({
        icon: 'info',
        title: 'Iniciar Sesión',
        text: 'Debes iniciar sesión para realizar cotizaciones y reservas de maquinaria.',
        confirmButtonColor: '#f59e0b',
        confirmButtonText: 'Ir a Iniciar Sesión',
        background: '#0f172a',
        color: '#f8fafc'
      }).then(() => {
        openAuthModal('login');
      });
      return;
    }

    const tool = allTools.find(t => t.id === toolId);
    if (!tool) return;

    activeToolForBooking = tool;
    document.getElementById('book-tool-name').textContent = tool.name;
    document.getElementById('book-tool-rate').textContent = `${API.formatCurrency(tool.dailyRate)} / día`;
    document.getElementById('book-tool-deposit').textContent = API.formatCurrency(tool.depositAmount);
    const bookImg = document.getElementById('book-tool-img');
    if (bookImg) {
      bookImg.src = tool.imageUrl || window.TOOL_FALLBACK_IMG;
      bookImg.onerror = function() { this.onerror = null; this.src = window.TOOL_FALLBACK_IMG; };
    }

    // Fechas por defecto: Mañana y 4 días después
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const end = new Date(tomorrow);
    end.setDate(end.getDate() + 3);

    document.getElementById('book-start-date').value = tomorrow.toISOString().split('T')[0];
    document.getElementById('book-end-date').value = end.toISOString().split('T')[0];

    recalculateBookingQuote();

    const modal = new bootstrap.Modal(document.getElementById('bookingModal'));
    modal.show();
  }

  function recalculateBookingQuote() {
    if (!activeToolForBooking) return;

    const startVal = document.getElementById('book-start-date').value;
    const endVal = document.getElementById('book-end-date').value;
    if (!startVal || !endVal) return;

    const startDate = new Date(startVal);
    const endDate = new Date(endVal);
    let diffDays = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));
    if (diffDays <= 0) diffDays = 1;

    const dailyRate = activeToolForBooking.dailyRate || 0;
    const deposit = activeToolForBooking.depositAmount || 0;

    const subtotal = dailyRate * diffDays;
    const iva = subtotal * 0.19; // IVA 19%
    const total = subtotal + iva + deposit;

    document.getElementById('quote-days').textContent = `${diffDays} día(s)`;
    document.getElementById('quote-subtotal').textContent = API.formatCurrency(subtotal);
    document.getElementById('quote-deposit').textContent = API.formatCurrency(deposit);
    document.getElementById('quote-iva').textContent = API.formatCurrency(iva);
    document.getElementById('quote-total').textContent = API.formatCurrency(total);

    // Guardar cálculo para proceso de pago
    activeToolForBooking.calculatedQuote = {
      days: diffDays,
      subtotal,
      iva,
      deposit,
      total,
      startDate: startVal,
      endDate: endVal
    };
  }

  async function proceedToPaymentFromBooking() {
    if (!activeToolForBooking || !activeToolForBooking.calculatedQuote) return;

    const bookingModalEl = document.getElementById('bookingModal');
    const bookingModal = bootstrap.Modal.getInstance(bookingModalEl);
    if (bookingModal) bookingModal.hide();

    // Abrir Modal de Pago
    const q = activeToolForBooking.calculatedQuote;
    document.getElementById('pay-tool-title').textContent = activeToolForBooking.name;
    document.getElementById('pay-summary-total').textContent = API.formatCurrency(q.total);
    document.getElementById('pay-summary-breakdown').textContent = `${q.days} días de renta + Garantía (${API.formatCurrency(q.deposit)}) + IVA 19%`;

    const paymentModal = new bootstrap.Modal(document.getElementById('paymentModal'));
    paymentModal.show();
  }

  async function submitPayment() {
    const user = API.getUser();
    if (!user || !activeToolForBooking) return;

    const q = activeToolForBooking.calculatedQuote;
    const selectedMethod = document.querySelector('input[name="paymentMethod"]:checked')?.value || 'PSE';

    Swal.fire({
      title: 'Procesando Transacción...',
      text: 'Conectando con la pasarela de pagos segura y emitiendo factura electrónica...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
      background: '#0f172a',
      color: '#f8fafc'
    });

    try {
      // 1. Crear Reserva
      const rental = await API.rentals.create({
        toolId: activeToolForBooking.id,
        toolName: activeToolForBooking.name,
        clientId: user.id,
        clientName: user.fullName || user.username,
        startDate: q.startDate,
        endDate: q.endDate,
        days: q.days,
        dailyRate: activeToolForBooking.dailyRate,
        depositAmount: q.deposit,
        total: q.total
      });

      // 2. Generar Factura con desglose fiscal
      const invoice = await API.invoices.generate({
        rentalId: rental.id,
        clientId: user.id,
        clientName: user.fullName || user.username,
        clientTaxId: user.clientProfile?.taxId || '900.000.000-1',
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
        clientId: user.id,
        amount: q.total,
        paymentMethod: selectedMethod,
        transactionReference: `PAY-${Date.now().toString().slice(-6)}`,
        notes: `Pago verificado vía ${selectedMethod} para ${activeToolForBooking.name}`
      });

      // Cerrar modal de pago
      const payModal = bootstrap.Modal.getInstance(document.getElementById('paymentModal'));
      if (payModal) payModal.hide();

      Swal.fire({
        icon: 'success',
        title: '¡Pago y Reserva Confirmados!',
        html: `
          <p class="text-light">Tu transacción <strong>${payment.transactionReference}</strong> fue aprobada exitosamente.</p>
          <p class="text-muted small">Se ha emitido la factura oficial <strong>${invoice.invoiceNumber}</strong>.</p>
          <div class="mt-3">
            <a href="${API.invoices.downloadPdfUrl(invoice.id)}" target="_blank" class="btn btn-neu-amber py-2 px-4">
              <i class="fa fa-file-pdf me-2"></i> Descargar Factura Oficial PDF
            </a>
          </div>
        `,
        confirmButtonColor: '#06b6d4',
        confirmButtonText: (user.role === 'ROLE_ADMIN') ? 'Ver Alquileres Activos' : 'Ver mis Alquileres',
        background: '#0f172a',
        color: '#f8fafc'
      }).then(() => {
        switchView('client-dashboard');
      });

    } catch (err) {
      Swal.fire({
        icon: 'error',
        title: 'Error en la transacción',
        text: err.message || 'No se pudo procesar el pago.',
        confirmButtonColor: '#f59e0b',
        background: '#0f172a',
        color: '#f8fafc'
      });
    }
  }

  function togglePaymentFields(method) {
    document.getElementById('card-fields').style.display = (method === 'CREDIT_CARD' || method === 'DEBIT_CARD') ? 'block' : 'none';
    document.getElementById('pse-fields').style.display = (method === 'PSE') ? 'block' : 'none';
    document.getElementById('transfer-fields').style.display = (method === 'BANK_TRANSFER') ? 'block' : 'none';
  }

  // --- DASHBOARDS POR ROL ---

  async function loadClientRentals() {
    const container = document.getElementById('client-rentals-list');
    if (!container) return;

    try {
      const rentals = await API.rentals.getAll();
      if (rentals.length === 0) {
        container.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No tienes alquileres registrados todavía</td></tr>`;
        return;
      }

      container.innerHTML = rentals.map(r => `
        <tr>
          <td><span class="fw-bold text-light">#${r.id}</span></td>
          <td><span class="text-warning fw-semibold">${r.toolName}</span></td>
          <td>${r.startDate} al ${r.endDate} <small class="text-muted">(${r.days} d)</small></td>
          <td class="fw-bold">${API.formatCurrency(r.total)}</td>
          <td>
            <span class="badge ${r.status === 'CONFIRMADA' ? 'badge-available' : (r.status === 'DEVUELTO' ? 'bg-secondary' : 'badge-rented')}">
              ${r.status}
            </span>
          </td>
          <td>
            <a href="${API.invoices.downloadPdfUrl(1)}" target="_blank" class="btn btn-sm btn-neu py-1 px-3" title="Descargar comprobante en PDF">
              <i class="fa fa-file-pdf text-danger me-1"></i> PDF
            </a>
          </td>
        </tr>
      `).join('');
    } catch (e) {
      console.warn('Error cargando alquileres cliente:', e);
    }
  }

  async function loadProviderRequests() {
    const container = document.getElementById('provider-requests-list');
    if (!container) return;

    try {
      const rentals = await API.rentals.getAll();
      if (!rentals || rentals.length === 0) {
        container.innerHTML = `<tr><td colspan="7" class="text-center text-slate-300 py-4"><i class="fa fa-inbox me-2 text-warning"></i>No hay solicitudes de alquiler registradas actualmente</td></tr>`;
        return;
      }

      container.innerHTML = rentals.map(r => `
        <tr>
          <td>#${r.id}</td>
          <td class="fw-bold text-light">${r.toolName}</td>
          <td>${r.clientName || 'Cliente Constructora'}</td>
          <td>${r.days} días</td>
          <td>${API.formatCurrency(r.total)}</td>
          <td><span class="badge ${r.status === 'CONFIRMADA' ? 'badge-available' : 'badge-rented'}">${r.status}</span></td>
          <td>
            <div class="provider-actions-cell">
            ${r.status === 'PENDIENTE_PAGO' || r.status === 'PENDIENTE' ? `
              <button class="btn btn-sm btn-success py-1 px-2" onclick="App.updateRentalStatus(${r.id}, 'CONFIRMADA')">Aprobar</button>
              <button class="btn btn-sm btn-danger py-1 px-2" onclick="App.updateRentalStatus(${r.id}, 'RECHAZADA')">Rechazar</button>
            ` : (r.status === 'CONFIRMADA' ? `
              <button class="btn btn-sm btn-neu-cyan py-1 px-2 text-nowrap" onclick="App.openReturnModal(${r.id})">
                <i class="fa fa-clipboard-check me-1"></i> Inspección Devolución
              </button>
            ` : `<span class="small text-muted">Completado</span>`)}
            </div>
          </td>
        </tr>
      `).join('');
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
      loadProviderRequests();
    } catch (e) {
      Swal.fire('Error', e.message, 'error');
    }
  }

  function openReturnModal(rentalId) {
    document.getElementById('return-rental-id').value = rentalId;
    const modal = new bootstrap.Modal(document.getElementById('returnInspectionModal'));
    modal.show();
  }

  async function submitReturnInspection() {
    const rentalId = document.getElementById('return-rental-id').value;
    const notes = document.getElementById('return-notes').value;
    const damageDetected = document.getElementById('damage-check').checked;
    const damageCost = damageDetected ? parseFloat(document.getElementById('damage-cost').value || 0) : 0;

    try {
      await API.rentals.return(rentalId, notes, damageDetected, damageCost);
      bootstrap.Modal.getInstance(document.getElementById('returnInspectionModal')).hide();

      Swal.fire({
        icon: 'success',
        title: 'Devolución Registrada',
        text: damageDetected ? `Se reportaron daños por ${API.formatCurrency(damageCost)}. Se descontará de la garantía.` : 'Equipo recibido en óptimas condiciones. Garantía liberada.',
        background: '#0f172a',
        color: '#f8fafc'
      });
      loadProviderRequests();
    } catch (e) {
      Swal.fire('Error', e.message, 'error');
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
      if (roleBadge) {
        roleBadge.textContent = user.role.replace('ROLE_', '');
        roleBadge.className = 'badge badge-role';
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
        if (clientDropdownLabel) clientDropdownLabel.textContent = 'Alquileres Activos';
        if (clientHeaderTitle) clientHeaderTitle.textContent = 'Alquileres Activos & Facturación';
      } else {
        if (clientNavLabel) clientNavLabel.textContent = 'Mis Alquileres';
        if (clientDropdownLabel) clientDropdownLabel.textContent = 'Mis Alquileres & Facturas';
        if (clientHeaderTitle) clientHeaderTitle.textContent = 'Mis Alquileres & Facturación';
      }

      // Enlaces contextuales en el menú desplegable del avatar (Cliente, Proveedor, Admin)
      if (clientDropdownItem) clientDropdownItem.style.display = (user.role === 'ROLE_CLIENTE' || user.role === 'ROLE_ADMIN') ? 'block' : 'none';
      if (providerDropdownItem) providerDropdownItem.style.display = (user.role === 'ROLE_PROVEEDOR' || user.role === 'ROLE_ADMIN') ? 'block' : 'none';
      if (adminDropdownItem) adminDropdownItem.style.display = (user.role === 'ROLE_ADMIN') ? 'block' : 'none';
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

      const clientNavLabel = document.getElementById('nav-client-dashboard-label');
      const clientDropdownLabel = document.getElementById('dropdown-client-label');
      const clientHeaderTitle = document.getElementById('client-dashboard-header-title');
      if (clientNavLabel) clientNavLabel.textContent = 'Mis Alquileres';
      if (clientDropdownLabel) clientDropdownLabel.textContent = 'Mis Alquileres & Facturas';
      if (clientHeaderTitle) clientHeaderTitle.textContent = 'Mis Alquileres & Facturación';
    }
  }

  function openAuthModal(mode = 'login') {
    const isLogin = mode === 'login';
    document.getElementById('auth-modal-title').textContent = isLogin ? 'Iniciar Sesión en RentaTools' : 'Crear Cuenta Profesional';
    document.getElementById('login-fields').style.display = isLogin ? 'block' : 'none';
    document.getElementById('register-fields').style.display = isLogin ? 'none' : 'block';
    document.getElementById('btn-auth-submit').textContent = isLogin ? 'Entrar al Sistema' : 'Completar Registro';
    document.getElementById('auth-mode-toggle').textContent = isLogin ? '¿No tienes cuenta? Regístrate aquí' : '¿Ya tienes cuenta? Inicia sesión aquí';
    document.getElementById('authModal').dataset.mode = isLogin ? 'login' : 'register';

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
      // Registro
      const user = {
        username: document.getElementById('reg-username').value,
        email: document.getElementById('reg-email').value,
        password: document.getElementById('reg-password').value,
        fullName: document.getElementById('reg-fullname').value,
        phone: document.getElementById('reg-phone').value,
        role: document.getElementById('reg-role').value,
        companyName: document.getElementById('reg-company').value,
        taxId: document.getElementById('reg-taxid').value,
        address: document.getElementById('reg-address').value,
        city: 'Bogotá'
      };

      try {
        const resp = await API.auth.register(user);
        API.setToken(resp.token);
        API.setUser({
          id: resp.id,
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

  function logout() {
    API.setToken(null);
    API.setUser(null);
    checkAuthStatus();
    switchView('catalog');
    Swal.fire({
      icon: 'info',
      title: 'Sesión Cerrada',
      timer: 1500,
      showConfirmButton: false,
      background: '#0f172a',
      color: '#f8fafc'
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
    submitAuth,
    logout,
    updateRentalStatus,
    openReturnModal,
    submitReturnInspection
  };
})();

document.addEventListener('DOMContentLoaded', App.init);
