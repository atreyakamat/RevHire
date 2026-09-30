/**
 * RevHire HTTP Client Module
 * Provides standardized Fetch API wrappers with authentication, error handling, and header injection.
 */
const API = {
  /**
   * Performs an HTTP request against the RevHire API Gateway.
   * @param {string} endpoint - API path (e.g. /api/jobs)
   * @param {object} options - Fetch options including method, body, headers
   * @returns {Promise<any>}
   */
  async request(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${CONFIG.API_BASE_URL}${endpoint}`;
    const token = AUTH.getToken();

    const headers = {
      'Accept': 'application/json',
      ...options.headers
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(options.body);
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);

      // Handle 204 No Content
      if (response.status === 204) {
        return null;
      }

      // Read response body safely
      const contentType = response.headers.get('content-type') || '';
      let data = null;
      if (contentType.includes('application/json')) {
        data = await response.json();
      } else {
        const text = await response.text();
        try {
          data = JSON.parse(text);
        } catch {
          data = text;
        }
      }

      if (!response.ok) {
        let errorMessage = 'Request failed';
        if (data && typeof data === 'object') {
          errorMessage = data.message || data.error || (data.errors ? JSON.stringify(data.errors) : `HTTP ${response.status}`);
        } else if (typeof data === 'string' && data.length > 0) {
          errorMessage = data;
        } else {
          errorMessage = `HTTP Error ${response.status} (${response.statusText})`;
        }

        const error = new Error(errorMessage);
        error.status = response.status;
        error.data = data;

        // Session expired / unauthorized
        if (response.status === 401 && token) {
          // If token is invalid or expired, trigger logout and notify
          console.warn('Session expired or unauthorized. Redirecting to login.');
          AUTH.clearAuth();
          if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('index.html')) {
            window.location.href = 'login.html?expired=true';
          }
        }

        throw error;
      }

      return data;
    } catch (err) {
      if (!err.status) {
        // Network error / Gateway unreachable
        console.error('Network request failed: Gateway unreachable or CORS issue.');
        const networkError = new Error('Could not connect to RevHire Gateway (http://localhost:8080). Please ensure services are running.');
        networkError.status = 0;
        throw networkError;
      }
      throw err;
    }
  },

  get(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'GET' });
  },

  post(endpoint, body, options = {}) {
    return this.request(endpoint, { ...options, method: 'POST', body });
  },

  put(endpoint, body, options = {}) {
    return this.request(endpoint, { ...options, method: 'PUT', body });
  },

  delete(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'DELETE' });
  }
};
