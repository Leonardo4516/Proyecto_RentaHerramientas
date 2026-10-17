/**
 * MapManager - Gestión Geoespacial Multi-Rol de Maquinaria y Cobertura (Leaflet.js)
 * 1. Cliente (Catálogo): Círculo azul de cobertura, detección de ubicación real GPS por navegador,
 *    diferenciación de herramientas a su disposición (verde brillante con pulso) vs disponibles para cotizar (ámbar).
 * 2. Proveedor: Proyecta su flota exclusiva (en patio verde vs en obra naranja con datos completos del cliente).
 * 3. Administrador: Telemetría 360° global de la flota (proveedores, estado de renta, clientes activos).
 */
const MapManager = (() => {
  // Instancias independientes de mapas
  let clientMap = null;
  let clientMarkersLayer = null;
  let userCircle = null;
  let userMarker = null;
  let currentRadiusMeters = 20000;

  let providerMap = null;
  let providerMarkersLayer = null;

  let adminMap = null;
  let adminMarkersLayer = null;
  let adminCurrentFilter = 'ALL';

  let cachedAllTools = [];
  let cachedAllRentals = [];

  // Coordenadas base por defecto (Bogotá D.C.)
  const DEFAULT_LOCATION = { lat: 4.6500, lng: -74.0800 };
  let userLocation = { ...DEFAULT_LOCATION };

  // Cargar ubicación previa guardada si existe
  try {
    const savedLoc = localStorage.getItem('rentatools_user_location');
    if (savedLoc) {
      const parsed = JSON.parse(savedLoc);
      if (parsed.lat && parsed.lng) {
        userLocation = parsed;
      }
    }
  } catch (_) {}

  // Dispersión determinista para herramientas sin lat/lng
  const BOGOTA_ZONES = [
    { lat: 4.6534, lng: -74.0836, name: 'Patio Central Cra 68' },
    { lat: 4.6782, lng: -74.1354, name: 'Zona Industrial Fontibón' },
    { lat: 4.6923, lng: -74.0921, name: 'Patio Calle 80' },
    { lat: 4.7450, lng: -74.0430, name: 'Zona Norte Toberín' },
    { lat: 4.6097, lng: -74.1082, name: 'Corredor Puente Aranda' },
    { lat: 4.5820, lng: -74.1620, name: 'Parque Industrial Sur' },
    { lat: 4.7110, lng: -74.1480, name: 'Engativá Álamos' },
    { lat: 4.8210, lng: -74.0530, name: 'Chía / Autopista Norte' }
  ];

  function resolveCoordinates(tool) {
    if (tool.latitude && tool.longitude && !isNaN(tool.latitude) && !isNaN(tool.longitude)) {
      return { lat: parseFloat(tool.latitude), lng: parseFloat(tool.longitude) };
    }
    if (tool.lat && tool.lng && !isNaN(tool.lat) && !isNaN(tool.lng)) {
      return { lat: parseFloat(tool.lat), lng: parseFloat(tool.lng) };
    }
    const hash = Math.abs(parseInt(tool.id || 1, 10)) % BOGOTA_ZONES.length;
    const base = BOGOTA_ZONES[hash];
    const offsetLat = ((tool.id * 37) % 20 - 10) * 0.0008;
    const offsetLng = ((tool.id * 53) % 20 - 10) * 0.0008;
    return { lat: base.lat + offsetLat, lng: base.lng + offsetLng };
  }

  // Micro-dispersión radial determinista para evitar que múltiples máquinas en el mismo patio/obra queden superpuestas
  function applyCoordinateJitter(baseCoords, indexAtCoord, totalAtCoord) {
    if (!totalAtCoord || totalAtCoord <= 1) return { ...baseCoords };
    const angle = (indexAtCoord * (2 * Math.PI / totalAtCoord));
    // 0.0035 grados ~ 380 metros en el mapa de Bogotá: perfectamente distinguible visualmente
    const offsetDist = 0.0034;
    return {
      lat: baseCoords.lat + Math.sin(angle) * offsetDist,
      lng: baseCoords.lng + Math.cos(angle) * offsetDist
    };
  }

  // ==========================================
  // 1. MAPA DEL CLIENTE (Storefront / Catálogo)
  // ==========================================
  function initClientMap() {
    const container = document.getElementById('map-container');
    if (!container) return;

    if (!clientMap) {
      if (container._leaflet_id) {
        container._leaflet_id = null;
      }

      clientMap = L.map('map-container', {
        zoomControl: true,
        attributionControl: false
      }).setView([userLocation.lat, userLocation.lng], 12);

      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(clientMap);

      clientMarkersLayer = L.layerGroup().addTo(clientMap);

      // Dibujar inmediatamente la ubicación actual y el círculo azul
      drawUserLocation(userLocation);

      // Solicitar ubicación real por navegador
      requestRealLocation(false);
    }

    setTimeout(() => {
      if (clientMap) clientMap.invalidateSize();
    }, 120);
    setTimeout(() => {
      if (clientMap) clientMap.invalidateSize();
    }, 300);
  }

  function drawUserLocation(loc, radiusKm = null) {
    if (!clientMap) return;
    if (userCircle) clientMap.removeLayer(userCircle);
    if (userMarker) clientMap.removeLayer(userMarker);

    const slider = document.getElementById('radius-slider');
    let radius = currentRadiusMeters;
    if (radiusKm) {
      radius = radiusKm * 1000;
    } else if (slider) {
      radius = parseInt(slider.value, 10) * 1000;
    }
    currentRadiusMeters = radius;

    // CÍRCULO AZUL CIAN DE COBERTURA
    userCircle = L.circle([loc.lat, loc.lng], {
      color: '#06b6d4',
      fillColor: '#06b6d4',
      fillOpacity: 0.12,
      weight: 2,
      radius: radius
    }).addTo(clientMap);

    // Marcador de la obra del usuario con brillo cian
    const userIcon = L.divIcon({
      className: 'user-pin-icon',
      html: `
        <div style="position: relative; width: 24px; height: 24px;">
          <div style="position: absolute; top: -5px; left: -5px; width: 34px; height: 34px; border-radius: 50%; background: rgba(6, 182, 212, 0.45); animation: mapPulse 2s infinite ease-in-out;"></div>
          <div style="position: relative; z-index: 2; background: #06b6d4; width: 24px; height: 24px; border-radius: 50%; border: 3px solid #ffffff; box-shadow: 0 0 15px #06b6d4; display: flex; align-items: center; justify-content: center; color: #ffffff; font-size: 11px;">
            <i class="fa fa-crosshairs"></i>
          </div>
        </div>
      `,
      iconSize: [24, 24],
      iconAnchor: [12, 12]
    });

    userMarker = L.marker([loc.lat, loc.lng], { 
      icon: userIcon,
      draggable: true
    })
      .bindPopup(`
        <div style="font-family: inherit; min-width: 200px;">
          <strong style="color: #0284c7;"><i class="fa fa-location-arrow me-1"></i>Ubicación de tu Obra (GPS)</strong>
          <div class="small text-muted mt-1">Radio de cobertura: <strong>${Math.round(radius / 1000)} km</strong></div>
          <div class="small text-muted mb-2">Centro de despacho para tu maquinaria</div>
          <div class="alert alert-info py-1 px-2 mb-0 small" style="font-size: 0.75rem;">
            <i class="fa fa-hand-pointer me-1"></i> Puedes arrastrar este pin o hacer clic en el mapa para ajustar tu obra.
          </div>
        </div>
      `)
      .addTo(clientMap);

    userMarker.on('dragend', function (e) {
      const pos = e.target.getLatLng();
      userLocation = { lat: pos.lat, lng: pos.lng };
      try {
        localStorage.setItem('rentatools_user_location', JSON.stringify(userLocation));
      } catch (_) {}
      if (userCircle) userCircle.setLatLng(pos);
      if (typeof Swal !== 'undefined') {
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Ubicación de obra actualizada',
          text: `Coordenadas: ${pos.lat.toFixed(4)}, ${pos.lng.toFixed(4)}`,
          showConfirmButton: false,
          timer: 2000
        });
      }
    });

    clientMap.off('click');
    clientMap.on('click', function (e) {
      userLocation = { lat: e.latlng.lat, lng: e.latlng.lng };
      try {
        localStorage.setItem('rentatools_user_location', JSON.stringify(userLocation));
      } catch (_) {}
      drawUserLocation(userLocation);
      if (typeof Swal !== 'undefined') {
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'info',
          title: 'Obra reubicada en el mapa',
          text: `${e.latlng.lat.toFixed(4)}, ${e.latlng.lng.toFixed(4)}`,
          showConfirmButton: false,
          timer: 1800
        });
      }
    });
  }

  // Solicitar permiso explícito al navegador para usar GPS real
  function requestRealLocation(showAlert = false) {
    if (!navigator.geolocation) {
      if (showAlert && typeof Swal !== 'undefined') {
        Swal.fire({
          icon: 'warning',
          title: 'Geolocalización no soportada',
          text: 'Tu navegador no soporta el servicio de ubicación por GPS.',
          confirmButtonColor: '#00843D'
        });
      }
      return;
    }

    if (showAlert && typeof Swal !== 'undefined') {
      Swal.fire({
        title: 'Detectando tu ubicación real...',
        text: 'Por favor confirma el permiso de ubicación en tu navegador para posicionar tu obra en el mapa.',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
      });
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        userLocation = {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude
        };
        try {
          localStorage.setItem('rentatools_user_location', JSON.stringify(userLocation));
        } catch (_) {}

        if (clientMap) {
          clientMap.setView([userLocation.lat, userLocation.lng], 13, { animate: true });
          drawUserLocation(userLocation);
        }

        if (showAlert && typeof Swal !== 'undefined') {
          Swal.fire({
            icon: 'success',
            title: '¡Ubicación Sincronizada con Éxito!',
            html: `<p class="small text-muted mb-0">Coordenadas detectadas: <strong>${userLocation.lat.toFixed(4)}, ${userLocation.lng.toFixed(4)}</strong>.<br>El círculo azul ahora delimita el radio de entrega desde tu obra.</p>`,
            timer: 2500,
            showConfirmButton: false
          });
        }
      },
      (err) => {
        console.warn('Geolocalización no permitida o error:', err);
        drawUserLocation(userLocation);

        if (showAlert && typeof Swal !== 'undefined') {
          Swal.fire({
            icon: 'info',
            title: 'Permisos de Ubicación Requeridos',
            html: '<p class="small text-muted">Para que el mapa ubique automáticamente tu obra, haz clic en el icono del candado o la chincheta junto a la barra de direcciones de tu navegador y selecciona <strong>Permitir ubicación</strong>.</p>',
            confirmButtonColor: '#00843D',
            confirmButtonText: 'Entendido'
          });
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  }

  function updateToolsOnMap(tools, activeRentals = []) {
    initClientMap();
    if (!clientMap || !clientMarkersLayer) return;

    clientMarkersLayer.clearLayers();
    cachedAllTools = tools || [];

    const user = (typeof API !== 'undefined' && API.getUser) ? API.getUser() : null;
    
    // Obtener TODOS los alquileres activos correspondientes al cliente autenticado
    const clientRentals = (activeRentals || []).filter(r => {
      if (!user) return false;
      const isClientOwner = String(r.clientId) === String(user.clientId) ||
                            String(r.clientId) === String(user.id) ||
                            (user.role === 'ROLE_CLIENTE' && (!r.clientId || String(r.clientId) === '1' || String(r.clientId) === String(user.clientId || user.id)));
      const isActiveStatus = ['CONFIRMADA', 'EN_CURSO', 'ACTIVO', 'PENDIENTE'].includes(r.status);
      return isClientOwner && isActiveStatus;
    });

    // Mapear qué herramientas están actualmente en arriendo activo por este cliente
    const rentedToolIds = new Set(clientRentals.map(r => String(r.toolId)));
    const bounds = [];

    // 1. DIBUJAR CADA UNO DE LOS ALQUILERES ACTIVOS DEL CLIENTE (A su disposición: Verde Esmeralda con pulso)
    clientRentals.forEach((rentalInfo, idx) => {
      const tool = tools.find(t => String(t.id) === String(rentalInfo.toolId)) || {
        id: rentalInfo.toolId,
        name: rentalInfo.toolName || `Maquinaria #${rentalInfo.toolId}`,
        brand: 'Certificada',
        model: 'Alta Gama',
        providerName: rentalInfo.providerName || 'Proveedor Aliado'
      };

      const baseCoords = resolveCoordinates(tool);
      // Aplicar jitter si hay múltiples alquileres para que cada máquina tenga su propio pin nítido
      const coords = applyCoordinateJitter(baseCoords, idx, clientRentals.length);
      bounds.push([coords.lat, coords.lng]);

      const toolImg = (window.getToolImageUrl ? window.getToolImageUrl(tool) : tool.imageUrl) || (window.TOOL_FALLBACK_IMG || 'assets/img/machinery/placeholder-tool.svg');

      const iconHtml = `
        <div style="position: relative; width: 42px; height: 42px;">
          <div style="position: absolute; top: -5px; left: -5px; width: 52px; height: 52px; border-radius: 50%; background: rgba(16, 185, 129, 0.45); animation: mapPulse 1.8s infinite ease-in-out;"></div>
          <div style="position: relative; z-index: 2; background: #10b981; color: #ffffff; width: 42px; height: 42px; border-radius: 50%; border: 3px solid #ffffff; display: flex; align-items: center; justify-content: center; font-size: 17px; box-shadow: 0 4px 16px rgba(16, 185, 129, 0.75);">
            <i class="fa fa-check-circle"></i>
          </div>
        </div>
      `;

      const popupContent = `
        <div style="min-width: 260px; font-family: inherit;">
          <div class="d-flex align-items-center justify-content-between mb-2">
            <span class="badge" style="background: #10b981; color: #fff;"><i class="fa fa-shield-alt me-1"></i> A TU DISPOSICIÓN</span>
            <span class="badge bg-dark">#${rentalInfo.id}</span>
          </div>
          <img src="${toolImg}" style="width: 100%; height: 95px; object-fit: cover; border-radius: 6px; margin-bottom: 8px;" onerror="if (window.handleToolImageError) window.handleToolImageError(this); else { this.onerror=null; this.src='assets/img/machinery/placeholder-tool.svg'; }">
          <h6 style="color: #075C35; margin: 0 0 4px 0; font-weight: bold;">${tool.name}</h6>
          <div class="small text-muted mb-2"><i class="fa fa-building text-secondary me-1"></i> ${tool.providerName || rentalInfo.providerName || 'Proveedor Aliado'}</div>
          
          <div class="p-2 rounded-2 border bg-light small mb-2">
            <div class="text-dark fw-bold mb-1"><i class="fa fa-calendar-check text-success me-1"></i> Arriendo Activo:</div>
            <div><strong>Periodo:</strong> ${rentalInfo.startDate} al ${rentalInfo.endDate}</div>
            <div><strong>Destino:</strong> ${rentalInfo.deliveryAddress || 'Tu Obra Registrada'}</div>
            <div><strong>Estado:</strong> <span class="badge bg-success">${rentalInfo.status}</span></div>
          </div>

          <button class="btn btn-sm btn-outline-success w-100 py-1" onclick="App.switchView('client-dashboard')">
            <i class="fa fa-file-invoice me-1"></i> Ver en Mis Alquileres
          </button>
        </div>
      `;

      const toolIcon = L.divIcon({
        className: 'tool-pin-icon-active',
        html: iconHtml,
        iconSize: [42, 42],
        iconAnchor: [21, 21]
      });

      L.marker([coords.lat, coords.lng], { icon: toolIcon })
        .bindPopup(popupContent)
        .addTo(clientMarkersLayer);
    });

    // 2. DIBUJAR LAS HERRAMIENTAS DISPONIBLES EN EL CATÁLOGO PARA COTIZAR (Ámbar industrial)
    tools.forEach((tool, tIdx) => {
      // Si la herramienta ya está en arriendo activo por este cliente, omitir para no duplicar pin
      if (rentedToolIds.has(String(tool.id))) return;

      const baseCoords = resolveCoordinates(tool);
      const coords = applyCoordinateJitter(baseCoords, tIdx % 4, 4);
      bounds.push([coords.lat, coords.lng]);

      const toolImg = (window.getToolImageUrl ? window.getToolImageUrl(tool) : tool.imageUrl) || (window.TOOL_FALLBACK_IMG || 'assets/img/machinery/placeholder-tool.svg');

      const iconHtml = `
        <div style="background: #f59e0b; color: #0b0f19; width: 34px; height: 34px; border-radius: 50%; border: 2.5px solid #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: bold; box-shadow: 0 4px 12px rgba(245, 158, 11, 0.6);">
          <i class="fa fa-truck-pickup"></i>
        </div>
      `;

      const popupContent = `
        <div style="min-width: 220px; font-family: inherit;">
          <img src="${toolImg}" style="width: 100%; height: 95px; object-fit: cover; border-radius: 6px; margin-bottom: 8px;" onerror="if (window.handleToolImageError) window.handleToolImageError(this); else { this.onerror=null; this.src='assets/img/machinery/placeholder-tool.svg'; }">
          <h6 style="color: #d97706; margin: 0 0 4px 0; font-weight: bold;">${tool.name}</h6>
          <div class="small text-muted mb-1"><i class="fa fa-building text-secondary me-1"></i> ${tool.providerName || 'Proveedor Verificado'}</div>
          <div style="font-size: 0.8rem; color: #64748b; margin-bottom: 6px;"><i class="fa fa-map-marker-alt text-danger me-1"></i> ${tool.locationName || 'Bogotá D.C.'}</div>
          <div style="font-size: 0.85rem; font-weight: bold; color: #075C35; margin-bottom: 8px;">Tarifa: ${API.formatCurrency(tool.dailyPrice || tool.dailyRate)}/día</div>
          <button class="btn btn-sm btn-sb-green w-100 py-1" onclick="App.openBookingModal('${tool.id}')">
            <i class="fa fa-calendar-check me-1"></i> Cotizar / Reservar
          </button>
        </div>
      `;

      const toolIcon = L.divIcon({
        className: 'tool-pin-icon-available',
        html: iconHtml,
        iconSize: [34, 34],
        iconAnchor: [17, 17]
      });

      L.marker([coords.lat, coords.lng], { icon: toolIcon })
        .bindPopup(popupContent)
        .addTo(clientMarkersLayer);
    });
  }

  // ==========================================
  // 2. MAPA DEL PROVEEDOR (Portal Proveedor)
  // ==========================================
  function renderProviderMap(containerId, providerTools, allRentals) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!providerMap) {
      if (container._leaflet_id) {
        container._leaflet_id = null;
      }
      providerMap = L.map(containerId, {
        zoomControl: true,
        attributionControl: false
      }).setView([DEFAULT_LOCATION.lat, DEFAULT_LOCATION.lng], 12);

      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(providerMap);

      providerMarkersLayer = L.layerGroup().addTo(providerMap);
    } else {
      if (providerMarkersLayer) {
        providerMarkersLayer.clearLayers();
      }
    }

    const bounds = [];
    const rentalsList = allRentals || [];
    const toolsList = (providerTools && providerTools.length > 0) ? providerTools : cachedAllTools;
    const providerToolIds = new Set(toolsList.map(t => String(t.id)));

    // Extraer TODOS los alquileres activos de la maquinaria de este proveedor
    const activeProviderRentals = rentalsList.filter(r => 
      (providerToolIds.has(String(r.toolId)) || !providerTools || providerTools.length === 0) && 
      ['CONFIRMADA', 'EN_CURSO', 'ACTIVO', 'PENDIENTE'].includes(r.status)
    );

    const rentedToolIds = new Set(activeProviderRentals.map(r => String(r.toolId)));

    // 1. DIBUJAR CADA UNO DE LOS ALQUILERES ACTIVOS (EN OBRA / ALQUILADA: Naranja Fuego con datos del cliente)
    activeProviderRentals.forEach((activeRental, rIdx) => {
      const tool = toolsList.find(t => String(t.id) === String(activeRental.toolId)) || {
        id: activeRental.toolId,
        name: activeRental.toolName || `Maquinaria #${activeRental.toolId}`,
        code: 'OBRA-ACT',
        model: 'En Operación'
      };

      const baseCoords = resolveCoordinates(tool);
      const coords = applyCoordinateJitter(baseCoords, rIdx, Math.max(activeProviderRentals.length, 2));
      bounds.push([coords.lat, coords.lng]);

      const iconHtml = `
        <div style="background: #ea580c; color: #ffffff; width: 38px; height: 38px; border-radius: 50%; border: 3px solid #fff; display: flex; align-items: center; justify-content: center; font-size: 15px; box-shadow: 0 4px 14px rgba(234, 88, 12, 0.75);">
          <i class="fa fa-hard-hat"></i>
        </div>
      `;

      const popupContent = `
        <div style="min-width: 270px; max-width: 320px; font-family: inherit;">
          <div class="d-flex align-items-center justify-content-between mb-2">
            <span class="badge bg-warning text-dark"><i class="fa fa-hard-hat me-1"></i> EN OBRA / ALQUILADA</span>
            <span class="badge bg-dark">#${activeRental.id}</span>
          </div>
          
          <h6 class="fw-bold text-dark mb-1">${tool.name}</h6>
          <div class="small text-muted mb-2">Código: ${tool.code || 'N/A'} | Modelo: ${tool.model || 'N/A'}</div>

          <!-- Ficha completa del cliente -->
          <div class="p-2.5 rounded-3 bg-light border mb-2" style="font-size: 0.85rem;">
            <div class="fw-bold text-dark mb-1"><i class="fa fa-user-circle text-primary me-1"></i> Cliente que la alquiló:</div>
            <div class="text-dark fw-bold fs-6">${activeRental.clientName || 'Cliente Constructora'}</div>
            ${activeRental.clientDocument ? `<div class="text-muted small mb-1"><i class="fa fa-id-card me-1 text-secondary"></i> ${activeRental.clientDocument}</div>` : ''}
            
            <div class="mt-2 pt-1 border-top">
              <a href="tel:${activeRental.clientPhone || '+573204451023'}" class="text-success text-decoration-none d-block small fw-bold mb-1">
                <i class="fa fa-phone-alt me-1"></i> ${activeRental.clientPhone || '+57 320 445 1023'}
              </a>
              <a href="mailto:${activeRental.clientEmail || 'compras@constructora.com'}" class="text-primary text-decoration-none d-block small">
                <i class="fa fa-envelope me-1"></i> ${activeRental.clientEmail || 'compras@constructora.com'}
              </a>
            </div>
          </div>

          <div class="small text-secondary mb-2" style="font-size: 0.82rem;">
            <div><i class="fa fa-map-marker-alt text-danger me-1"></i> <strong>Destino Obra:</strong> ${activeRental.deliveryAddress || 'Sector Obra Central'}</div>
            <div><i class="fa fa-calendar-alt text-primary me-1"></i> <strong>Periodo:</strong> ${activeRental.startDate} al ${activeRental.endDate} (${activeRental.days || activeRental.totalDays || 1} días)</div>
            <div><i class="fa fa-coins text-warning me-1"></i> <strong>Total Liquidado:</strong> ${API.formatCurrency(activeRental.subtotal || activeRental.total)}</div>
          </div>

          <button class="btn btn-sm btn-outline-primary w-100 py-1" onclick="App.openReturnModal('${activeRental.id}')">
            <i class="fa fa-clipboard-check me-1"></i> Inspección / Devolución
          </button>
        </div>
      `;

      const icon = L.divIcon({
        className: 'provider-tool-pin-rented',
        html: iconHtml,
        iconSize: [38, 38],
        iconAnchor: [19, 19]
      });

      L.marker([coords.lat, coords.lng], { icon })
        .bindPopup(popupContent)
        .addTo(providerMarkersLayer);
    });

    // 2. DIBUJAR HERRAMIENTAS DISPONIBLES EN PATIO (Verde Esmeralda)
    toolsList.forEach((tool, tIdx) => {
      // Si la herramienta está actualmente activa en arriendo, ya fue dibujada arriba con sus datos de obra
      if (rentedToolIds.has(String(tool.id))) return;

      const baseCoords = resolveCoordinates(tool);
      const coords = applyCoordinateJitter(baseCoords, tIdx, Math.max(toolsList.length, 2));
      bounds.push([coords.lat, coords.lng]);

      const toolImg = (window.getToolImageUrl ? window.getToolImageUrl(tool) : tool.imageUrl) || (window.TOOL_FALLBACK_IMG || 'assets/img/machinery/placeholder-tool.svg');

      const iconHtml = `
        <div style="background: #10b981; color: #ffffff; width: 36px; height: 36px; border-radius: 50%; border: 3px solid #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.6);">
          <i class="fa fa-warehouse"></i>
        </div>
      `;

      const popupContent = `
        <div style="min-width: 220px; font-family: inherit;">
          <div class="d-flex align-items-center justify-content-between mb-2">
            <span class="badge bg-success"><i class="fa fa-check me-1"></i> EN PATIO / DISPONIBLE</span>
            <span class="badge bg-light text-dark border">${tool.code || 'ACT-01'}</span>
          </div>
          <img src="${toolImg}" style="width: 100%; height: 95px; object-fit: cover; border-radius: 6px; margin-bottom: 8px;" onerror="if (window.handleToolImageError) window.handleToolImageError(this); else { this.onerror=null; this.src='assets/img/machinery/placeholder-tool.svg'; }">
          <h6 class="fw-bold text-dark mb-1">${tool.name}</h6>
          <div class="small text-muted mb-2">Marca: ${tool.brand || 'N/A'} | Modelo: ${tool.model || 'N/A'}</div>
          <div class="small fw-bold text-success mb-1">Tarifa pública: ${API.formatCurrency(tool.dailyPrice)}/día</div>
          <div class="small text-secondary mb-2">Días acumulados en obra: <strong>${tool.accumulatedRentalDays || 0} d</strong></div>
          <div class="alert alert-light border py-1 px-2 mb-0 text-center small text-muted">
            Equipo listo y verificado para despacho
          </div>
        </div>
      `;

      const icon = L.divIcon({
        className: 'provider-tool-pin-patio',
        html: iconHtml,
        iconSize: [36, 36],
        iconAnchor: [18, 18]
      });

      L.marker([coords.lat, coords.lng], { icon })
        .bindPopup(popupContent)
        .addTo(providerMarkersLayer);
    });

    if (bounds.length > 1 && providerMap) {
      providerMap.fitBounds(bounds, { padding: [40, 40], maxZoom: 13 });
    } else if (bounds.length === 1 && providerMap) {
      providerMap.setView(bounds[0], 12);
    } else if (providerMap) {
      providerMap.setView([DEFAULT_LOCATION.lat, DEFAULT_LOCATION.lng], 12);
    }

    setTimeout(() => {
      if (providerMap) providerMap.invalidateSize();
    }, 120);
    setTimeout(() => {
      if (providerMap) providerMap.invalidateSize();
    }, 350);
  }

  // ==========================================
  // 3. MAPA DEL ADMINISTRADOR (Portal Admin)
  // ==========================================
  function renderAdminMap(containerId, allTools, allRentals, filter = 'ALL') {
    const container = document.getElementById(containerId);
    if (!container) return;

    adminCurrentFilter = filter;
    cachedAllTools = allTools || [];
    cachedAllRentals = allRentals || [];

    if (!adminMap) {
      if (container._leaflet_id) {
        container._leaflet_id = null;
      }
      adminMap = L.map(containerId, {
        zoomControl: true,
        attributionControl: false
      }).setView([DEFAULT_LOCATION.lat, DEFAULT_LOCATION.lng], 11);

      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(adminMap);

      adminMarkersLayer = L.layerGroup().addTo(adminMap);
    } else {
      if (adminMarkersLayer) {
        adminMarkersLayer.clearLayers();
      }
    }

    const bounds = [];
    const rentalsList = cachedAllRentals;

    let displayTools = cachedAllTools;
    if (filter === 'DISPONIBLE') {
      displayTools = cachedAllTools.filter(t => {
        const active = rentalsList.find(r => String(r.toolId) === String(t.id) && ['CONFIRMADA', 'EN_CURSO', 'ACTIVO'].includes(r.status));
        return !active;
      });
    } else if (filter === 'ALQUILADO') {
      displayTools = cachedAllTools.filter(t => {
        const active = rentalsList.find(r => String(r.toolId) === String(t.id) && ['CONFIRMADA', 'EN_CURSO', 'ACTIVO'].includes(r.status));
        return !!active;
      });
    }

    displayTools.forEach(tool => {
      const coords = resolveCoordinates(tool);
      const activeRental = rentalsList.find(r => 
        String(r.toolId) === String(tool.id) && 
        ['CONFIRMADA', 'EN_CURSO', 'ACTIVO'].includes(r.status)
      );

      const isRented = !!activeRental;
      const toolImg = (window.getToolImageUrl ? window.getToolImageUrl(tool) : tool.imageUrl) || (window.TOOL_FALLBACK_IMG || 'assets/img/machinery/placeholder-tool.svg');

      let iconHtml = '';
      let popupContent = '';

      if (isRented) {
        // Rentada: Naranja / Fuego
        iconHtml = `
          <div style="background: #ea580c; color: #fff; width: 36px; height: 36px; border-radius: 50%; border: 2.5px solid #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; box-shadow: 0 4px 12px rgba(234, 88, 12, 0.6);">
            <i class="fa fa-truck-moving"></i>
          </div>
        `;

        popupContent = `
          <div style="min-width: 280px; max-width: 330px; font-family: inherit;">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="badge bg-danger"><i class="fa fa-circle me-1 small"></i> ALQUILADA EN OBRA</span>
              <span class="badge bg-dark">Tool #${tool.id}</span>
            </div>
            <h6 class="fw-bold text-dark mb-1">${tool.name}</h6>
            <div class="small text-muted mb-2">Activo: ${tool.code} | Tarifa: ${API.formatCurrency(tool.dailyPrice)}/d</div>

            <!-- Datos del Proveedor -->
            <div class="p-2 rounded bg-light border mb-2 small">
              <div class="fw-bold text-primary"><i class="fa fa-building me-1"></i> Proveedor Propietario:</div>
              <div class="text-dark fw-bold">${tool.providerName || activeRental.providerName || 'Proveedor Registrado'}</div>
              <div class="text-muted">ID Proveedor: #${tool.providerId || '1'}</div>
            </div>

            <!-- Datos del Cliente Arrendatario -->
            <div class="p-2 rounded bg-light border mb-2 small">
              <div class="fw-bold text-success"><i class="fa fa-user-check me-1"></i> Cliente Arrendatario:</div>
              <div class="text-dark fw-bold">${activeRental.clientName || 'Cliente Constructora'}</div>
              <div><i class="fa fa-phone me-1 text-success"></i> ${activeRental.clientPhone || '+57 320 000 0023'}</div>
              <div><i class="fa fa-envelope me-1 text-info"></i> ${activeRental.clientEmail || 'cliente@constructora.com'}</div>
              <div><i class="fa fa-map-marker-alt me-1 text-danger"></i> Obra: ${activeRental.deliveryAddress || 'Sector Obra Central'}</div>
              <div><i class="fa fa-calendar-alt me-1 text-primary"></i> ${activeRental.startDate} al ${activeRental.endDate}</div>
              <div><i class="fa fa-coins me-1 text-warning"></i> Total: ${API.formatCurrency(activeRental.subtotal || activeRental.total)}</div>
            </div>
          </div>
        `;
      } else {
        // Disponible: Verde Esmeralda
        iconHtml = `
          <div style="background: #10b981; color: #fff; width: 34px; height: 34px; border-radius: 50%; border: 2.5px solid #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.5);">
            <i class="fa fa-check"></i>
          </div>
        `;

        popupContent = `
          <div style="min-width: 250px; font-family: inherit;">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="badge bg-success"><i class="fa fa-check-circle me-1"></i> DISPONIBLE</span>
              <span class="badge bg-light text-dark border">Tool #${tool.id}</span>
            </div>
            <img src="${toolImg}" style="width: 100%; height: 95px; object-fit: cover; border-radius: 6px; margin-bottom: 8px;" onerror="if (window.handleToolImageError) window.handleToolImageError(this); else { this.onerror=null; this.src='assets/img/machinery/placeholder-tool.svg'; }">
            <h6 class="fw-bold text-dark mb-1">${tool.name}</h6>
            <div class="small text-muted mb-2">Marca: ${tool.brand} | Modelo: ${tool.model}</div>
            
            <div class="p-2 rounded bg-light border mb-2 small">
              <div class="fw-bold text-primary"><i class="fa fa-building me-1"></i> Proveedor:</div>
              <div class="text-dark fw-bold">${tool.providerName || 'Proveedor Aliado'}</div>
              <div class="text-muted">Tarifa: ${API.formatCurrency(tool.dailyPrice)}/día</div>
              <div class="text-muted">Días acumulados: ${tool.accumulatedRentalDays || 0} d</div>
            </div>

            <div class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 w-100 py-1 text-center">
              En bodega / Lista para renta
            </div>
          </div>
        `;
      }

      const icon = L.divIcon({
        className: 'admin-tool-pin',
        html: iconHtml,
        iconSize: [36, 36],
        iconAnchor: [18, 18]
      });

      L.marker([coords.lat, coords.lng], { icon })
        .bindPopup(popupContent)
        .addTo(adminMarkersLayer);

      bounds.push([coords.lat, coords.lng]);
    });

    if (bounds.length > 0 && adminMap) {
      adminMap.fitBounds(bounds, { padding: [40, 40], maxZoom: 12 });
    }

    setTimeout(() => {
      if (adminMap) adminMap.invalidateSize();
    }, 120);
    setTimeout(() => {
      if (adminMap) adminMap.invalidateSize();
    }, 350);
  }

  function filterAdminMap(filterType) {
    if (adminMap) {
      renderAdminMap('admin-map-container', cachedAllTools, cachedAllRentals, filterType);
    }
  }

  // Filtrado por radio de proximidad para el cliente (actualiza el círculo azul)
  function filterByRadius(radiusKm, allTools) {
    currentRadiusMeters = radiusKm * 1000;
    if (userCircle) {
      userCircle.setRadius(currentRadiusMeters);
    }
    return (allTools || []).filter(t => {
      const coords = resolveCoordinates(t);
      const d = getDistanceFromLatLonInKm(userLocation.lat, userLocation.lng, coords.lat, coords.lng);
      return d <= radiusKm;
    });
  }

  function getDistanceFromLatLonInKm(lat1, lon1, lat2, lon2) {
    const R = 6371;
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  function invalidateSize() {
    if (clientMap) clientMap.invalidateSize();
    if (providerMap) providerMap.invalidateSize();
    if (adminMap) adminMap.invalidateSize();
  }

  window.addEventListener('resize', invalidateSize);

  return {
    init: initClientMap,
    initClientMap,
    updateToolsOnMap,
    renderProviderMap,
    renderAdminMap,
    filterAdminMap,
    filterByRadius,
    requestRealLocation,
    invalidateSize,
    getUserLocation: () => userLocation
  };
})();

// Inyección de estilos de animación de pulso para Leaflet
(function injectMapStyles() {
  if (document.getElementById('rentatools-map-styles')) return;
  const style = document.createElement('style');
  style.id = 'rentatools-map-styles';
  style.textContent = `
    @keyframes mapPulse {
      0% { transform: scale(0.9); opacity: 0.9; }
      50% { transform: scale(1.4); opacity: 0.15; }
      100% { transform: scale(0.9); opacity: 0.9; }
    }
    .leaflet-popup-content-wrapper {
      border-radius: 10px !important;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.2), 0 8px 10px -6px rgba(0, 0, 0, 0.1) !important;
      padding: 4px !important;
    }
    .leaflet-popup-content {
      margin: 12px !important;
      line-height: 1.4 !important;
    }
    .provider-map-card, .admin-map-card {
      transition: box-shadow 0.2s ease-out;
    }
    .provider-map-card:hover, .admin-map-card:hover {
      box-shadow: 0 12px 28px rgba(0,0,0,0.08) !important;
    }
  `;
  document.head.appendChild(style);
})();
