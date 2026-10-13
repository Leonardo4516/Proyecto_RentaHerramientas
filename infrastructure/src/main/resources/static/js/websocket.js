const RealtimeManager = (() => {
  let stompClient = null;
  let notifications = [];
  const listeners = [];

  function init() {
    const socketUrl = `${window.location.origin}/ws-rentatools`;
    console.log('Conectando a WebSocket en:', socketUrl);

    try {
      // Intentar STOMP sobre SockJS o WebSocket nativo
      const socket = new SockJS(socketUrl);
      stompClient = Stomp.over(socket);
      stompClient.debug = () => {}; // Silenciar logs verbosos en consola

      stompClient.connect({}, (frame) => {
        console.log('[STOMP] Conexión WebSocket establecida con éxito');
        updateConnectionBadge(true);

        // 1. Canal global de catálogo
        stompClient.subscribe('/topic/catalog', (message) => {
          handleIncomingMessage('Catálogo', message.body);
        });

        // 2. Canales según el usuario logueado
        const user = API.getUser();
        if (user && user.id) {
          if (user.role === 'ROLE_CLIENTE') {
            stompClient.subscribe(`/topic/client/${user.id}`, (message) => {
              handleIncomingMessage('Cliente', message.body);
            });
          } else if (user.role === 'ROLE_PROVEEDOR') {
            stompClient.subscribe(`/topic/provider/${user.id}`, (message) => {
              handleIncomingMessage('Proveedor', message.body);
            });
          }
        }
      }, (error) => {
        console.warn('[STOMP] No se pudo conectar al WebSocket o servidor en reinicio:', error);
        updateConnectionBadge(false);
        // Reintento en 8 segundos
        setTimeout(init, 8000);
      });
    } catch (e) {
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

    const notif = {
      id: Date.now(),
      channel,
      content: typeof content === 'object' ? JSON.stringify(content) : content,
      time: new Date().toLocaleTimeString()
    };

    notifications.unshift(notif);
    updateNotificationUI();
    showToastNotification(notif);

    listeners.forEach(cb => cb(notif));
  }

  function showToastNotification(notif) {
    if (typeof Swal !== 'undefined') {
      Swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'info',
        title: `Alerta en Tiempo Real [${notif.channel}]`,
        text: notif.content,
        showConfirmButton: false,
        timer: 4500,
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
        list.innerHTML = '<li class="p-3 text-center small text-slate-300" style="color: #cbd5e1 !important;"><i class="fa fa-bell-slash me-2 text-warning"></i>Sin notificaciones pendientes</li>';
      } else {
        list.innerHTML = notifications.slice(0, 5).map(n => `
          <li class="dropdown-item notif-item mb-2 p-2">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <span class="badge bg-warning text-dark fw-bold px-2 py-0" style="font-size: 0.72rem;">${n.channel}</span>
              <span class="notif-item-time">${n.time}</span>
            </div>
            <div class="notif-item-content mt-1">${n.content}</div>
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

  return {
    init,
    onNotification,
    getNotifications: () => notifications
  };
})();
