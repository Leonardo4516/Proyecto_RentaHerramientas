const MapManager = (() => {
  let map = null;
  let markersLayer = null;
  let userCircle = null;
  let userLocation = { lat: 4.6500, lng: -74.0800 }; // Coordenadas centrales por defecto (Bogotá)

  function init() {
    const container = document.getElementById('map-container');
    if (!container || map) return;

    // Crear mapa centrado en coordenadas industriales
    map = L.map('map-container', {
      zoomControl: true,
      attributionControl: false
    }).setView([userLocation.lat, userLocation.lng], 12);

    // Capa de mosaicos oficial OpenStreetMap (100% abierta y sin API Key)
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    markersLayer = L.layerGroup().addTo(map);

    // Intentar geolocalización real del navegador
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          userLocation = { lat: pos.coords.latitude, lng: pos.coords.longitude };
          map.setView([userLocation.lat, userLocation.lng], 13);
          drawUserLocation(userLocation);
        },
        () => {
          drawUserLocation(userLocation);
        }
      );
    } else {
      drawUserLocation(userLocation);
    }
  }

  function drawUserLocation(loc) {
    if (userCircle) map.removeLayer(userCircle);

    // Radio de proximidad con brillo cian
    userCircle = L.circle([loc.lat, loc.lng], {
      color: '#06b6d4',
      fillColor: '#06b6d4',
      fillOpacity: 0.12,
      radius: 12000 // 12 km
    }).addTo(map);

    // Marcador de la obra del usuario
    const userIcon = L.divIcon({
      className: 'user-pin-icon',
      html: '<div style="background: #06b6d4; width: 18px; height: 18px; border-radius: 50%; border: 3px solid #fff; box-shadow: 0 0 15px #06b6d4;"></div>',
      iconSize: [20, 20],
      iconAnchor: [10, 10]
    });

    L.marker([loc.lat, loc.lng], { icon: userIcon })
      .bindPopup('<strong style="color: #38bdf8;">📍 Ubicación de tu Obra / Proyecto</strong>')
      .addTo(map);
  }

  function updateToolsOnMap(tools) {
    if (!map || !markersLayer) return;
    markersLayer.clearLayers();

    tools.forEach(tool => {
      if (tool.latitude && tool.longitude) {
        const toolIcon = L.divIcon({
          className: 'machinery-pin-icon',
          html: `<div style="background: #f59e0b; color: #0b0f19; width: 34px; height: 34px; border-radius: 50%; border: 2px solid #fff; display: flex; align-items: center; justify-content: center; font-size: 15px; font-weight: bold; box-shadow: 0 0 18px rgba(245, 158, 11, 0.7);"><i class="fa fa-truck-pickup"></i></div>`,
          iconSize: [36, 36],
          iconAnchor: [18, 18]
        });

        const popupContent = `
          <div style="min-width: 200px;">
            <img src="${tool.imageUrl || window.TOOL_FALLBACK_IMG}" style="width: 100%; height: 95px; object-fit: cover; border-radius: 6px; margin-bottom: 8px;" onerror="this.onerror=null; this.src=window.TOOL_FALLBACK_IMG;">
            <h6 style="color: #fbbf24; margin: 0 0 4px 0; font-weight: bold;">${tool.name}</h6>
            <div style="font-size: 0.8rem; color: #94a3b8; margin-bottom: 6px;">📍 ${tool.locationName || 'Bogotá'}</div>
            <div style="font-size: 0.85rem; font-weight: bold; color: #f8fafc; margin-bottom: 8px;">Tarifa: ${API.formatCurrency(tool.dailyRate)}/día</div>
            <button class="btn btn-sm btn-neu-amber w-100 py-1" onclick="App.openBookingModal(${tool.id})">
              <i class="fa fa-calendar-check me-1"></i> Cotizar
            </button>
          </div>
        `;

        L.marker([tool.latitude, tool.longitude], { icon: toolIcon })
          .bindPopup(popupContent)
          .addTo(markersLayer);
      }
    });
  }

  function filterByRadius(radiusKm, allTools) {
    if (userCircle) {
      userCircle.setRadius(radiusKm * 1000);
    }
    // Filtrar herramientas dentro del radio
    return allTools.filter(t => {
      if (!t.latitude || !t.longitude) return false;
      const d = getDistanceFromLatLonInKm(userLocation.lat, userLocation.lng, t.latitude, t.longitude);
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

  // Recalcular tamaño de mapa en cambios de viewport
  window.addEventListener('resize', () => {
    if (map) {
      map.invalidateSize();
    }
  });

  function invalidateSize() {
    if (map) {
      map.invalidateSize();
    }
  }

  return {
    init,
    updateToolsOnMap,
    filterByRadius,
    invalidateSize,
    getUserLocation: () => userLocation
  };
})();
