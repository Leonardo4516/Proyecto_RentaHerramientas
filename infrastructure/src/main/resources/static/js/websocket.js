const RealtimeManager = (() => {
  let stompClient = null;
  let notifications = [];
  const listeners = [];
  const recentMessageKeys = new Set();
  let isConnecting = false;

  function getStorageKey() {
    const user = (typeof API !== 'undefined' && API.getUser) ? API.getUser() : null;
    if (user && user.role) {
      return `rentatools_notifs_${user.role}_${(user.username || user.id || 'user').toLowerCase()}`;
    }
    return 'rentatools_notifs_guest';
  }

  function loadUserNotifications() {
    try {
      const raw = localStorage.getItem(getStorageKey());
      if (raw) {
        notifications = JSON.parse(raw);
      } else {
        const user = (typeof API !== 'undefined' && API.getUser) ? API.getUser() : null;
        if (user) {
          notifications = [{
            id: Date.now(),
            channel: 'Sistema',
            content: `Bienvenido al marketplace, ${user.fullName || user.username}. Tu canal en tiempo real está activo.`,
            time: new Date().toLocaleTimeString()
          }];
          saveUserNotifications();
        } else {
          notifications = [];
        }
      }
    } catch (e) {
      notifications = [];
    }
    updateNotificationUI();
  }

  function saveUserNotifications() {
    try {
      localStorage.setItem(getStorageKey(), JSON.stringify(notifications.slice(0, 25)));
    } catch (_) {}
  }

  function clearNotifications() {
    notifications = [];
    saveUserNotifications();
    updateNotificationUI();
  }

  function init() {
    // Si ya existe cliente conectado o conectándose, desconectar limpiamente para evitar conexiones duplicadas
    if (stompClient) {
      try {
        stompClient.disconnect(() => {});
      } catch (_) {}
      stompClient = null;
    }

    loadUserNotifications();

    const socketUrl = `${window.location.origin}/ws-rentatools`;

    try {
      isConnecting = true;
      const socket = new SockJS(socketUrl);
      stompClient = Stomp.over(socket);
      stompClient.debug = () => {}; // Silenciar logs verbosos en consola

      stompClient.connect({}, () => {
        isConnecting = false;
        console.log('[STOMP] Conexión WebSocket establecida con éxito');
        updateConnectionBadge(true);

        // 1. Canal global de catálogo
        stompClient.subscribe('/topic/catalog', (message) => {
          handleIncomingMessage('Catálogo', message.body);
        });

        // 2. Canales específicos según el usuario logueado
        const user = (typeof API !== 'undefined' && API.getUser) ? API.getUser() : null;
        if (user && user.id) {
          if (user.role === 'ROLE_CLIENTE') {
            stompClient.subscribe(`/topic/client/${user.id}`, (message) => {
              handleIncomingMessage('Cliente', message.body);
            });
          } else if (user.role === 'ROLE_PROVEEDOR') {
            stompClient.subscribe(`/topic/provider/${user.id}`, (message) => {
              handleIncomingMessage('Proveedor', message.body);
            });
          } else if (user.role === 'ROLE_ADMIN') {
            stompClient.subscribe('/topic/admin/activity', (message) => {
              handleIncomingMessage('Admin', message.body);
            });
          }
        }
      }, (error) => {
        isConnecting = false;
        console.warn('[STOMP] No se pudo conectar al WebSocket o reconectando:', error);
        updateConnectionBadge(false);
        // Reintento espaciado en 10 segundos
        setTimeout(() => {
          if (!stompClient || !stompClient.connected) {
            init();
          }
        }, 10000);
      });
    } catch (e) {
      isConnecting = false;
      console.error('Error al inicializar cliente WebSocket:', e);
      updateConnectionBadge(false);
    }
  }

  function handleIncomingMessage(channel, rawBody) {
    let content = rawBody;
    try {
      const parsed = JSON.parse(rawBody);
      content = parsed.data || parsed.message || rawBody;
    } catch (_) {}

    const textContent = (typeof content === 'object') ? JSON.stringify(content) : String(content);

    // Filtro estricto de deduplicación (evita mensajes dobles si llegan por broadcast y privado dentro de 3.5 segundos)
    const dedupKey = `${channel}::${textContent.trim()}`;
    if (recentMessageKeys.has(dedupKey)) {
      return;
    }
    recentMessageKeys.add(dedupKey);
    setTimeout(() => {
      recentMessageKeys.delete(dedupKey);
    }, 3500);

    const notif = {
      id: Date.now(),
      channel,
      content: textContent,
      time: new Date().toLocaleTimeString()
    };

    notifications.unshift(notif);
    saveUserNotifications();
    updateNotificationUI();
    showToastNotification(notif);

    // Anuncio a tecnologías de asistencia mediante región aria-live
    const announcer = document.getElementById('a11y-live-announcer');
    if (announcer) {
      announcer.textContent = `Nueva notificación de ${notif.channel}: ${notif.content}`;
    }

    listeners.forEach(cb => {
      try { cb(notif); } catch (_) {}
    });
  }

  // Modern Web Performance: Reconexión inteligente al reactivar pestaña
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
      if (!stompClient || !stompClient.connected) {
        init();
      }
    }
  });

  function showToastNotification(notif) {
    if (typeof Swal !== 'undefined') {
      Swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'info',
        title: `Alerta en Tiempo Real [${notif.channel}]`,
        text: notif.content,
        showConfirmButton: false,
        timer: 4000,
        timerProgressBar: true,
        background: '#0f172a',
        color: '#f8fafc',
        customClass: {
          popup: 'glass-panel border-warning'
        }
      });
    }
  }

  function updateNotificationUI() {
    const badge = document.getElementById('notif-badge');
    const list = document.getElementById('notif-list');
    if (badge) {
      badge.textContent = notifications.length;
      badge.style.display = notifications.length > 0 ? 'inline-block' : 'none';
    }
    if (list) {
      if (notifications.length === 0) {
        list.innerHTML = '<li class="p-4 text-center small text-muted"><i class="fa fa-bell-slash me-2 text-warning"></i>Sin notificaciones pendientes</li>';
      } else {
        list.innerHTML = notifications.slice(0, 15).map(n => `
          <li class="dropdown-item notif-item mb-2 p-2">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <span class="badge bg-warning text-dark fw-bold px-2 py-0" style="font-size: 0.72rem;">${n.channel}</span>
              <span class="notif-item-time" style="color: #64748b !important; font-size: 0.75rem;">${n.time}</span>
            </div>
            <div class="notif-item-content mt-1" style="color: #1e293b !important; font-weight: 500; font-size: 0.84rem; white-space: normal; line-height: 1.4;">${n.content}</div>
          </li>
        `).join('');
      }
    }
  }

  function updateConnectionBadge(isConnected) {
    const dot = document.getElementById('ws-dot');
    const text = document.getElementById('ws-text');
    const indicator = document.getElementById('ws-status-indicator');
    if (dot) {
      dot.className = isConnected ? 'ws-pulse-dot' : 'ws-pulse-dot disconnected';
    }
    if (text) {
      text.textContent = isConnected ? 'En línea' : 'Reconectando';
    }
    if (indicator) {
      indicator.title = isConnected ? 'Conexión en tiempo real activa (STOMP)' : 'Desconectado - Intentando reconectar...';
    }
  }

  function onNotification(cb) {
    listeners.push(cb);
  }

  // Prevenir que hacer clic dentro del desplegable cierre el menú de notificaciones
  document.addEventListener('DOMContentLoaded', () => {
    const notifMenu = document.querySelector('.notification-dropdown-menu');
    if (notifMenu) {
      notifMenu.addEventListener('click', (e) => {
        if (!e.target.closest('.notif-clear-btn')) {
          e.stopPropagation();
        }
      });
    }
  });

  function disconnect() {
    if (stompClient) {
      try { stompClient.disconnect(() => {}); } catch (_) {}
      stompClient = null;
    }
    notifications = [];
    updateNotificationUI();
    updateConnectionBadge(false);
  }

  return {
    init,
    disconnect,
    onNotification,
    loadUserNotifications,
    clearNotifications,
    getNotifications: () => notifications
  };
})();
