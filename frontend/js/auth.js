/**
 * RevHire Authentication & Session Management Module
 */
const AUTH = {
  /**
   * Get JWT Token from storage.
   * @returns {string|null}
   */
  getToken() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.TOKEN);
  },

  /**
   * Get User metadata from storage.
   * @returns {{userId: number, role: string, email?: string}|null}
   */
  getUser() {
    const raw = localStorage.getItem(CONFIG.STORAGE_KEYS.USER);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  },

  /**
   * Store authentication response and optional email.
   * @param {{token: string, userId: number, role: string}} authData 
   * @param {string} [email] 
   */
  setAuth(authData, email) {
    if (!authData || !authData.token) return;
    localStorage.setItem(CONFIG.STORAGE_KEYS.TOKEN, authData.token);
    
    const user = {
      userId: authData.userId,
      role: authData.role,
      email: email || ''
    };
    localStorage.setItem(CONFIG.STORAGE_KEYS.USER, JSON.stringify(user));
  },

  /**
   * Clear authentication state.
   */
  clearAuth() {
    localStorage.removeItem(CONFIG.STORAGE_KEYS.TOKEN);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.USER);
  },

  /**
   * Checks if user is authenticated.
   * @returns {boolean}
   */
  isLoggedIn() {
    return Boolean(this.getToken() && this.getUser());
  },

  /**
   * Get the current user's role.
   * @returns {string|null} 'JOB_SEEKER' | 'EMPLOYER' | 'ADMIN' | null
   */
  getRole() {
    const user = this.getUser();
    return user ? user.role : null;
  },

  /**
   * Get the current user's ID.
   * @returns {number|null}
   */
  getUserId() {
    const user = this.getUser();
    return user ? user.userId : null;
  },

  /**
   * Protect private pages. Redirects unauthenticated users to login.
   * Optionally checks for specific required role.
   * @param {string|string[]} [requiredRoles] 
   */
  requireAuth(requiredRoles) {
    if (!this.isLoggedIn()) {
      const currentPath = window.location.pathname.split('/').pop() || 'index.html';
      window.location.href = `login.html?redirect=${encodeURIComponent(currentPath)}`;
      return false;
    }

    if (requiredRoles) {
      const userRole = this.getRole();
      const rolesArray = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];
      if (!rolesArray.includes(userRole)) {
        console.warn(`Access denied. Role ${userRole} is not permitted for this page.`);
        // Redirect to user's appropriate dashboard
        if (userRole === 'EMPLOYER') {
          window.location.href = 'employer-dashboard.html';
        } else {
          window.location.href = 'seeker-dashboard.html';
        }
        return false;
      }
    }

    return true;
  },

  /**
   * For login / register pages: redirect already-logged-in users to their dashboard.
   */
  redirectIfLoggedIn() {
    if (this.isLoggedIn()) {
      const role = this.getRole();
      if (role === 'EMPLOYER') {
        window.location.href = 'employer-dashboard.html';
      } else {
        window.location.href = 'seeker-dashboard.html';
      }
    }
  },

  /**
   * Logs out the user and redirects to landing page.
   */
  logout() {
    this.clearAuth();
    window.location.href = 'index.html';
  }
};
