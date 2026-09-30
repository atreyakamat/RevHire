/**
 * RevHire Common UI Utilities
 * Dynamic Navbar, Footer, XSS-safe escaping, formatting, and UI helpers.
 */

const UI = {
  /**
   * Escape HTML entities to prevent XSS injection.
   * @param {string|any} str 
   * @returns {string}
   */
  escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  },

  /**
   * Format ISO date/datetime string to localized readable format.
   * @param {string} dateStr 
   * @returns {string}
   */
  formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    try {
      const d = new Date(dateStr);
      if (isNaN(d.getTime())) return dateStr;
      return d.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch {
      return dateStr;
    }
  },

  /**
   * Format datetime to date and time.
   * @param {string} dateStr 
   * @returns {string}
   */
  formatDateTime(dateStr) {
    if (!dateStr) return 'N/A';
    try {
      const d = new Date(dateStr);
      if (isNaN(d.getTime())) return dateStr;
      return d.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateStr;
    }
  },

  /**
   * Format numeric salary into standard currency.
   * @param {number|string} amount 
   * @returns {string}
   */
  formatSalary(amount) {
    if (amount === null || amount === undefined || isNaN(Number(amount))) return 'Not specified';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0
    }).format(Number(amount));
  },

  /**
   * Format Job Status badge HTML.
   * @param {string} status 
   * @returns {string}
   */
  getJobStatusBadge(status) {
    const s = String(status || '').toUpperCase();
    if (s === 'ACTIVE') {
      return '<span class="badge bg-success-subtle text-success border border-success-subtle">Active</span>';
    } else if (s === 'DRAFT') {
      return '<span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">Draft</span>';
    } else if (s === 'CLOSED') {
      return '<span class="badge bg-danger-subtle text-danger border border-danger-subtle">Closed</span>';
    }
    return `<span class="badge bg-light text-dark">${this.escapeHtml(s)}</span>`;
  },

  /**
   * Format Job Type badge HTML.
   * @param {string} type 
   * @returns {string}
   */
  getJobTypeBadge(type) {
    const t = String(type || '').toUpperCase();
    let label = t.replace('_', ' ');
    return `<span class="badge bg-primary-subtle text-primary border border-primary-subtle">${this.escapeHtml(label)}</span>`;
  },

  /**
   * Format Application Status badge HTML.
   * @param {string} status 
   * @returns {string}
   */
  getApplicationStatusBadge(status) {
    const s = String(status || '').toUpperCase();
    switch (s) {
      case 'APPLIED':
        return '<span class="badge bg-info-subtle text-info border border-info-subtle">Applied</span>';
      case 'UNDER_REVIEW':
        return '<span class="badge bg-warning-subtle text-warning border border-warning-subtle">Under Review</span>';
      case 'SHORTLISTED':
        return '<span class="badge bg-primary-subtle text-primary border border-primary-subtle">Shortlisted</span>';
      case 'HIRED':
        return '<span class="badge bg-success text-white">Hired</span>';
      case 'REJECTED':
        return '<span class="badge bg-danger-subtle text-danger border border-danger-subtle">Rejected</span>';
      case 'WITHDRAWN':
        return '<span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">Withdrawn</span>';
      default:
        return `<span class="badge bg-light text-dark">${this.escapeHtml(s)}</span>`;
    }
  },

  /**
   * Display alert box in a container.
   * @param {string} containerId 
   * @param {string} message 
   * @param {'danger'|'success'|'warning'|'info'} [type='danger'] 
   */
  showAlert(containerId, message, type = 'danger') {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = `
      <div class="alert alert-${type} alert-dismissible fade show shadow-sm" role="alert">
        <div class="d-flex align-items-center">
          <i class="bi bi-${type === 'success' ? 'check-circle' : type === 'warning' ? 'exclamation-triangle' : 'exclamation-circle'} me-2"></i>
          <div>${this.escapeHtml(message)}</div>
        </div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
      </div>
    `;
  },

  /**
   * Clears alerts in a container.
   * @param {string} containerId 
   */
  clearAlert(containerId) {
    const el = document.getElementById(containerId);
    if (el) el.innerHTML = '';
  },

  /**
   * Show full loading indicator in a container.
   * @param {string} containerId 
   * @param {string} [message='Loading...'] 
   */
  showLoading(containerId, message = 'Loading...') {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = `
      <div class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="text-muted mt-2">${this.escapeHtml(message)}</p>
      </div>
    `;
  },

  /**
   * Show clean empty state inside a container.
   * @param {string} containerId 
   * @param {string} title 
   * @param {string} message 
   * @param {{label: string, url: string}} [actionBtn] 
   */
  showEmpty(containerId, title, message, actionBtn = null) {
    const el = document.getElementById(containerId);
    if (!el) return;
    let btnHtml = '';
    if (actionBtn) {
      btnHtml = `<a href="${actionBtn.url}" class="btn btn-primary mt-3">${this.escapeHtml(actionBtn.label)}</a>`;
    }
    el.innerHTML = `
      <div class="text-center py-5 border rounded-3 bg-light p-4">
        <i class="bi bi-inbox fs-1 text-muted"></i>
        <h5 class="mt-3 text-secondary">${this.escapeHtml(title)}</h5>
        <p class="text-muted mb-0">${this.escapeHtml(message)}</p>
        ${btnHtml}
      </div>
    `;
  },

  /**
   * Render dynamic responsive Navbar.
   * @param {string} activePage - current page identifier for active class
   */
  renderNavbar(activePage = '') {
    const navPlaceholder = document.getElementById('navbar-placeholder');
    if (!navPlaceholder) return;

    const isLoggedIn = AUTH.isLoggedIn();
    const role = AUTH.getRole();
    const user = AUTH.getUser();

    let brandLink = 'index.html';
    if (isLoggedIn) {
      brandLink = role === 'EMPLOYER' ? 'employer-dashboard.html' : 'seeker-dashboard.html';
    }

    let navItems = '';

    if (!isLoggedIn) {
      navItems = `
        <li class="nav-item">
          <a class="nav-link ${activePage === 'home' ? 'active' : ''}" href="index.html">
            <i class="bi bi-house me-1"></i>Home
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link ${activePage === 'jobs' ? 'active' : ''}" href="jobs.html">
            <i class="bi bi-briefcase me-1"></i>Browse Jobs
          </a>
        </li>
        <li class="nav-item ms-lg-2">
          <a class="btn btn-outline-primary btn-sm px-3 mt-1 mt-lg-0" href="login.html">
            <i class="bi bi-box-arrow-in-right me-1"></i>Login
          </a>
        </li>
        <li class="nav-item ms-lg-2">
          <a class="btn btn-primary btn-sm px-3 mt-1 mt-lg-0" href="register.html">
            <i class="bi bi-person-plus me-1"></i>Register
          </a>
        </li>
      `;
    } else if (role === 'EMPLOYER') {
      navItems = `
        <li class="nav-item">
          <a class="nav-link ${activePage === 'dashboard' ? 'active' : ''}" href="employer-dashboard.html">
            <i class="bi bi-speedometer2 me-1"></i>Dashboard
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link ${activePage === 'post-job' ? 'active' : ''}" href="post-job.html">
            <i class="bi bi-plus-circle me-1"></i>Post Job
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link ${activePage === 'manage-jobs' ? 'active' : ''}" href="manage-jobs.html">
            <i class="bi bi-briefcase me-1"></i>Manage Jobs
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link ${activePage === 'applications' ? 'active' : ''}" href="applications.html">
            <i class="bi bi-file-earmark-person me-1"></i>Applications
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link position-relative ${activePage === 'notifications' ? 'active' : ''}" href="notifications.html">
            <i class="bi bi-bell me-1"></i>Notifications
            <span id="nav-unread-badge" class="position-absolute top-1 start-100 translate-middle badge rounded-pill bg-danger d-none" style="font-size: 0.65rem;">
              0
            </span>
          </a>
        </li>
        <li class="nav-item dropdown ms-lg-2">
          <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            <i class="bi bi-person-circle fs-5 me-1 text-primary"></i>
            <span>Employer</span>
          </a>
          <ul class="dropdown-menu dropdown-menu-end shadow-sm">
            <li><a class="dropdown-item ${activePage === 'profile' ? 'active' : ''}" href="profile.html"><i class="bi bi-person me-2"></i>Company Profile</a></li>
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="AUTH.logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
          </ul>
        </li>
      `;
    } else {
      // Default: JOB_SEEKER
      navItems = `
        <li class="nav-item">
          <a class="nav-link ${activePage === 'dashboard' ? 'active' : ''}" href="seeker-dashboard.html">
            <i class="bi bi-speedometer2 me-1"></i>Dashboard
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link ${activePage === 'jobs' ? 'active' : ''}" href="jobs.html">
            <i class="bi bi-search me-1"></i>Browse Jobs
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link ${activePage === 'my-applications' ? 'active' : ''}" href="my-applications.html">
            <i class="bi bi-file-earmark-check me-1"></i>My Applications
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link ${activePage === 'resume' ? 'active' : ''}" href="resume.html">
            <i class="bi bi-file-earmark-text me-1"></i>My Resume
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link position-relative ${activePage === 'notifications' ? 'active' : ''}" href="notifications.html">
            <i class="bi bi-bell me-1"></i>Notifications
            <span id="nav-unread-badge" class="position-absolute top-1 start-100 translate-middle badge rounded-pill bg-danger d-none" style="font-size: 0.65rem;">
              0
            </span>
          </a>
        </li>
        <li class="nav-item dropdown ms-lg-2">
          <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            <i class="bi bi-person-circle fs-5 me-1 text-primary"></i>
            <span>Seeker</span>
          </a>
          <ul class="dropdown-menu dropdown-menu-end shadow-sm">
            <li><a class="dropdown-item ${activePage === 'profile' ? 'active' : ''}" href="profile.html"><i class="bi bi-person me-2"></i>Profile</a></li>
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="AUTH.logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
          </ul>
        </li>
      `;
    }

    navPlaceholder.innerHTML = `
      <nav class="navbar navbar-expand-lg navbar-light bg-white border-bottom shadow-sm sticky-top">
        <div class="container">
          <a class="navbar-brand d-flex align-items-center fw-bold text-primary" href="${brandLink}">
            <i class="bi bi-layers-fill fs-3 me-2"></i>
            <span>Rev<span class="text-dark">Hire</span></span>
          </a>
          <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#revhireNavbar" aria-controls="revhireNavbar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="revhireNavbar">
            <ul class="navbar-nav ms-auto align-items-lg-center">
              ${navItems}
            </ul>
          </div>
        </div>
      </nav>
    `;

    // Fetch unread notification count if user is logged in
    if (isLoggedIn && user && user.userId) {
      this.updateNotificationBadge(user.userId);
    }
  },

  /**
   * Updates notification badge with real count from API.
   * @param {number} userId 
   */
  async updateNotificationBadge(userId) {
    try {
      const res = await API.get(CONFIG.ENDPOINTS.NOTIFICATIONS.UNREAD_COUNT(userId));
      const badge = document.getElementById('nav-unread-badge');
      if (badge && res && res.unreadCount > 0) {
        badge.textContent = res.unreadCount > 99 ? '99+' : res.unreadCount;
        badge.classList.remove('d-none');
      }
    } catch {
      // Graceful fallback if notification service is idle or unread count fails
    }
  },

  /**
   * Render standard Footer.
   */
  renderFooter() {
    const footerPlaceholder = document.getElementById('footer-placeholder');
    if (!footerPlaceholder) return;

    footerPlaceholder.innerHTML = `
      <footer class="bg-dark text-white pt-5 pb-4 mt-auto">
        <div class="container">
          <div class="row g-4">
            <div class="col-lg-4 col-md-6">
              <h5 class="fw-bold text-white d-flex align-items-center mb-3">
                <i class="bi bi-layers-fill fs-4 text-primary me-2"></i>
                <span>Rev<span class="text-info">Hire</span></span>
              </h5>
              <p class="text-muted small">
                RevHire is a modern recruitment and job discovery platform connecting top talent with industry-leading employers through transparent microservice architecture.
              </p>
            </div>
            <div class="col-lg-2 col-md-6 col-6">
              <h6 class="text-white fw-bold mb-3">For Candidates</h6>
              <ul class="list-unstyled small text-muted">
                <li class="mb-2"><a href="jobs.html" class="text-decoration-none text-muted">Browse Jobs</a></li>
                <li class="mb-2"><a href="seeker-dashboard.html" class="text-decoration-none text-muted">Candidate Portal</a></li>
                <li class="mb-2"><a href="resume.html" class="text-decoration-none text-muted">Resume Builder</a></li>
                <li class="mb-2"><a href="my-applications.html" class="text-decoration-none text-muted">Track Applications</a></li>
              </ul>
            </div>
            <div class="col-lg-2 col-md-6 col-6">
              <h6 class="text-white fw-bold mb-3">For Employers</h6>
              <ul class="list-unstyled small text-muted">
                <li class="mb-2"><a href="post-job.html" class="text-decoration-none text-muted">Post a Job</a></li>
                <li class="mb-2"><a href="employer-dashboard.html" class="text-decoration-none text-muted">Employer Portal</a></li>
                <li class="mb-2"><a href="manage-jobs.html" class="text-decoration-none text-muted">Manage Postings</a></li>
                <li class="mb-2"><a href="applications.html" class="text-decoration-none text-muted">Review Candidates</a></li>
              </ul>
            </div>
            <div class="col-lg-4 col-md-6">
              <h6 class="text-white fw-bold mb-3">Platform Architecture</h6>
              <p class="text-muted small mb-2">
                Powered by Spring Boot microservices, Spring Cloud Gateway, Eureka Service Discovery, and MySQL persistence.
              </p>
              <div class="d-flex gap-2">
                <span class="badge bg-secondary">Gateway :8080</span>
                <span class="badge bg-secondary">Job Service</span>
                <span class="badge bg-secondary">Resume Service</span>
              </div>
            </div>
          </div>
          <hr class="border-secondary my-4">
          <div class="d-flex flex-column flex-sm-row justify-content-between align-items-center small text-muted">
            <p class="mb-0">&copy; ${new Date().getFullYear()} RevHire Platform. All rights reserved.</p>
            <p class="mb-0">Built with HTML5, Bootstrap 5 &amp; Vanilla JavaScript.</p>
          </div>
        </div>
      </footer>
    `;
  }
};
