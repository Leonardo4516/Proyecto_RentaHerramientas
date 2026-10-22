const API = (() => {
  const BASE_URL = window.location.origin;

  function getToken() {
    return localStorage.getItem('rentatools_token');
  }

  function setToken(token) {
    if (token) {
      localStorage.setItem('rentatools_token', token);
    } else {
      localStorage.removeItem('rentatools_token');
    }
  }

  function getUser() {
    const raw = localStorage.getItem('rentatools_user');
    return raw ? JSON.parse(raw) : null;
  }

  function setUser(user) {
    if (user) {
      localStorage.setItem('rentatools_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('rentatools_user');
    }
  }

  async function request(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    const token = getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(`${BASE_URL}${endpoint}`, config);
      if (response.status === 401) {
        // Token vencido o inválido
        if (getToken()) {
          console.warn('Sesión expirada o token inválido.');
        }
      }

      if (!response.ok) {
        let errorData;
        try {
          errorData = await response.json();
        } catch (_) {
          errorData = { message: `Error HTTP ${response.status}: ${response.statusText}` };
        }
        throw new Error(errorData.message || 'Error en la petición al servidor');
      }

      if (response.headers.get('content-type')?.includes('application/json')) {
        return await response.json();
      }
      return response;
    } catch (err) {
      console.error(`Error en API [${endpoint}]:`, err);
      throw err;
    }
  }

  function formatCurrency(amount) {
    if (amount === undefined || amount === null) return '$ 0';
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    }).format(amount);
  }

  return {
    getToken,
    setToken,
    getUser,
    setUser,
    formatCurrency,

    auth: {
      login: (usernameOrEmail, password) =>
        request('/api/v1/auth/login', {
          method: 'POST',
          body: JSON.stringify({ usernameOrEmail, password })
        }),
      register: (userData) =>
        request('/api/v1/auth/register', {
          method: 'POST',
          body: JSON.stringify(userData)
        }),
      me: () => request('/api/v1/auth/me')
    },

    tools: {
      getAll: () => request('/api/v1/tools/public'),
      getCategories: () => request('/api/v1/tools/categories'),
      getNearby: (lat, lng, radiusKm = 20) =>
        request(`/api/v1/tools/nearby?lat=${lat}&lng=${lng}&radiusKm=${radiusKm}`),
      search: (q, categoryId, minPrice, maxPrice, status) => {
        const params = new URLSearchParams();
        if (q) params.append('q', q);
        if (categoryId) params.append('categoryId', categoryId);
        if (minPrice) params.append('minPrice', minPrice);
        if (maxPrice) params.append('maxPrice', maxPrice);
        if (status) params.append('status', status);
        const queryStr = params.toString();
        return request(`/api/v1/tools/search${queryStr ? '?' + queryStr : ''}`);
      },
      create: (toolData) =>
        request('/api/v1/tools', {
          method: 'POST',
          body: JSON.stringify(toolData)
        })
    },

    rentals: {
      create: (rentalData) =>
        request('/api/v1/rentals', {
          method: 'POST',
          body: JSON.stringify(rentalData)
        }),
      getAll: (params = '') => {
        if (typeof params === 'object') {
          const sp = new URLSearchParams();
          Object.entries(params).forEach(([k, v]) => {
            if (v !== undefined && v !== null && v !== '') sp.append(k, v);
          });
          const qs = sp.toString();
          return request(`/api/v1/rentals${qs ? '?' + qs : ''}`);
        }
        return request('/api/v1/rentals' + (params ? (params.startsWith('?') ? params : '?' + params) : ''));
      },
      getMyRentals: (params = '') => {
        if (typeof params === 'object') {
          const sp = new URLSearchParams();
          Object.entries(params).forEach(([k, v]) => {
            if (v !== undefined && v !== null && v !== '') sp.append(k, v);
          });
          const qs = sp.toString();
          return request(`/api/v1/rentals/my-rentals${qs ? '?' + qs : ''}`);
        }
        return request('/api/v1/rentals/my-rentals' + (params ? (params.startsWith('?') ? params : '?' + params) : ''));
      },
      getByToolId: (toolId) => request(`/api/v1/rentals/tool/${toolId}`),
      checkAvailability: (toolId, startDate, endDate) =>
        request(`/api/v1/rentals/check-availability?toolId=${encodeURIComponent(toolId)}&startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`),
      updateStatus: (id, status) =>
        request(`/api/v1/rentals/${id}/status`, {
          method: 'PUT',
          body: JSON.stringify({ status })
        }),
      return: (id, notes, damageDetected, damageCost) =>
        request(`/api/v1/rentals/${id}/return`, {
          method: 'POST',
          body: JSON.stringify({ notes, damageDetected, damageCost })
        }),
      getTimeline: (id) => request(`/api/v1/rentals/${id}/timeline`)
    },

    invoices: {
      getAll: (paid) => {
        const url = paid !== undefined && paid !== null 
          ? `/api/v1/invoices?paid=${encodeURIComponent(paid)}`
          : '/api/v1/invoices';
        return request(url);
      },
      getById: (id) => request(`/api/v1/invoices/${id}`),
      getByClient: (clientId) => request(`/api/v1/invoices/client/${clientId}`),
      getByRental: (rentalId) => request(`/api/v1/invoices/rental/${rentalId}`),
      generate: (invoiceData) =>
        request('/api/v1/invoices', {
          method: 'POST',
          body: JSON.stringify(invoiceData)
        }),
      downloadPdfUrl: (id) => `${BASE_URL}/api/v1/invoices/${id}/pdf`
    },

    payments: {
      getAll: (paymentMethod, status) => {
        const params = new URLSearchParams();
        if (paymentMethod) params.append('paymentMethod', paymentMethod);
        if (status) params.append('status', status);
        const queryStr = params.toString();
        return request(`/api/v1/payments${queryStr ? '?' + queryStr : ''}`);
      },
      process: (paymentData) =>
        request('/api/v1/payments', {
          method: 'POST',
          body: JSON.stringify(paymentData)
        }),
      getByInvoice: (invoiceId) => request(`/api/v1/payments/invoice/${invoiceId}`)
    },

    reports: {
      getDashboard: () => request('/api/v1/reports/dashboard'),
      getProvider: (providerId) => request(`/api/v1/reports/provider/${providerId}`)
    },

    cart: {
      get: () => request('/api/v1/cart'),
      add: (toolId, days, startDate, endDate) =>
        request('/api/v1/cart', {
          method: 'POST',
          body: JSON.stringify({ toolId, days, startDate, endDate })
        }),
      update: (toolId, days, startDate, endDate) =>
        request(`/api/v1/cart/${toolId}`, {
          method: 'PUT',
          body: JSON.stringify({ toolId, days, startDate, endDate })
        }),
      remove: (toolId) =>
        request(`/api/v1/cart/${toolId}`, {
          method: 'DELETE'
        }),
      clear: () =>
        request('/api/v1/cart', {
          method: 'DELETE'
        })
    }
  };
})();
