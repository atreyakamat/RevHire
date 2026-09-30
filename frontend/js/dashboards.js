/**
 * RevHire Seeker & Employer Dashboards Logic
 */

document.addEventListener('DOMContentLoaded', () => {
  const isSeekerDashboard = window.location.pathname.endsWith('seeker-dashboard.html');
  const isEmployerDashboard = window.location.pathname.endsWith('employer-dashboard.html');

  if (isSeekerDashboard) {
    if (!AUTH.requireAuth('JOB_SEEKER')) return;
    UI.renderNavbar('dashboard');
    UI.renderFooter();
    initSeekerDashboard();
  } else if (isEmployerDashboard) {
    if (!AUTH.requireAuth('EMPLOYER')) return;
    UI.renderNavbar('dashboard');
    UI.renderFooter();
    initEmployerDashboard();
  }
});

// ==========================================
// CANDIDATE DASHBOARD
// ==========================================
async function initSeekerDashboard() {
  const userId = AUTH.getUserId();
  if (!userId) return;

  // 1. Fetch user profile for personal greeting
  try {
    const profile = await API.get(CONFIG.ENDPOINTS.USERS.ME);
    const nameEl = document.getElementById('seeker-welcome-name');
    if (nameEl) {
      const name = `${profile.firstName || ''} ${profile.lastName || ''}`.trim();
      nameEl.textContent = name || profile.email || 'Candidate';
    }
  } catch (err) {
    console.warn('Could not fetch user profile:', err);
  }

  // 2. Fetch real counts: Applications, Resume, Notifications
  loadSeekerStats(userId);
  loadSeekerRecentApplications();
}

async function loadSeekerStats(userId) {
  // Applications count
  try {
    const apps = await API.get(CONFIG.ENDPOINTS.APPLICATIONS.BASE);
    const countEl = document.getElementById('stat-applications-count');
    if (countEl) countEl.textContent = apps ? apps.length : 0;
  } catch {
    const countEl = document.getElementById('stat-applications-count');
    if (countEl) countEl.textContent = '0';
  }

  // Resume status
  try {
    const resume = await API.get(CONFIG.ENDPOINTS.RESUMES.BY_USER(userId));
    const resumeStatusEl = document.getElementById('stat-resume-status');
    if (resumeStatusEl) {
      if (resume && resume.id) {
        resumeStatusEl.innerHTML = '<span class="text-success"><i class="bi bi-check-circle me-1"></i>Active</span>';
      } else {
        resumeStatusEl.innerHTML = '<span class="text-warning"><i class="bi bi-exclamation-circle me-1"></i>Not Created</span>';
      }
    }
  } catch {
    const resumeStatusEl = document.getElementById('stat-resume-status');
    if (resumeStatusEl) resumeStatusEl.innerHTML = '<span class="text-muted">Not Found</span>';
  }

  // Unread notifications
  try {
    const notifs = await API.get(CONFIG.ENDPOINTS.NOTIFICATIONS.UNREAD_COUNT(userId));
    const notifEl = document.getElementById('stat-unread-notifs');
    if (notifEl) notifEl.textContent = notifs ? (notifs.unreadCount || 0) : 0;
  } catch {
    const notifEl = document.getElementById('stat-unread-notifs');
    if (notifEl) notifEl.textContent = '0';
  }
}

async function loadSeekerRecentApplications() {
  const container = document.getElementById('recent-applications-container');
  if (!container) return;

  try {
    const apps = await API.get(CONFIG.ENDPOINTS.APPLICATIONS.BASE);
    if (!apps || apps.length === 0) {
      UI.showEmpty(
        'recent-applications-container',
        'No Applications Yet',
        'You have not applied to any positions. Start searching our job catalog today.',
        { label: 'Browse Open Positions', url: 'jobs.html' }
      );
      return;
    }

    const recent = apps.slice(0, 5);
    const enriched = await Promise.all(recent.map(async (app) => {
      try {
        const job = await API.get(CONFIG.ENDPOINTS.JOBS.BY_ID(app.jobId));
        return { ...app, job };
      } catch {
        return { ...app, job: null };
      }
    }));

    let rowsHtml = '';
    enriched.forEach(app => {
      const jobTitle = app.job ? app.job.title : `Job #${app.jobId}`;
      rowsHtml += `
        <tr>
          <td>
            <div class="fw-semibold text-dark">${UI.escapeHtml(jobTitle)}</div>
            <small class="text-muted">${app.job ? UI.escapeHtml(app.job.location || 'Remote') : ''}</small>
          </td>
          <td>${UI.formatDate(app.appliedAt)}</td>
          <td>${UI.getApplicationStatusBadge(app.status)}</td>
          <td class="text-end">
            <a href="my-applications.html" class="btn btn-outline-primary btn-sm">View</a>
          </td>
        </tr>
      `;
    });

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-custom table-hover align-middle mb-0">
          <thead>
            <tr>
              <th>Job</th>
              <th>Applied Date</th>
              <th>Status</th>
              <th class="text-end">Details</th>
            </tr>
          </thead>
          <tbody>
            ${rowsHtml}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    console.error('Failed to load recent applications:', err);
    UI.showAlert('recent-applications-container', `Error loading recent applications: ${err.message}`, 'danger');
  }
}

// ==========================================
// EMPLOYER DASHBOARD
// ==========================================
async function initEmployerDashboard() {
  const userId = AUTH.getUserId();
  if (!userId) return;

  // 1. Fetch employer profile
  try {
    const profile = await API.get(CONFIG.ENDPOINTS.USERS.ME);
    const welcomeEl = document.getElementById('employer-welcome-name');
    if (welcomeEl) {
      welcomeEl.textContent = profile.companyName || profile.contactName || profile.email || 'Employer';
    }
  } catch (err) {
    console.warn('Could not fetch employer profile:', err);
  }

  // 2. Fetch stats: posted jobs count & applications count
  loadEmployerDashboardData(userId);
}

async function loadEmployerDashboardData(userId) {
  const container = document.getElementById('employer-recent-jobs-container');
  if (!container) return;

  try {
    const jobsData = await API.get(`${CONFIG.ENDPOINTS.JOBS.BY_EMPLOYER(userId)}?page=0&size=10`);
    const jobs = jobsData && jobsData.content ? jobsData.content : [];

    // Stat 1: Jobs Count
    const jobsCountEl = document.getElementById('stat-jobs-count');
    if (jobsCountEl) jobsCountEl.textContent = jobsData ? (jobsData.totalElements || jobs.length) : 0;

    // Stat 2 & 3: Total candidate applications & unread notifications
    let totalApps = 0;
    const appsPromises = jobs.map(async (j) => {
      try {
        const apps = await API.get(CONFIG.ENDPOINTS.APPLICATIONS.BY_JOB(j.id));
        return apps ? apps.length : 0;
      } catch {
        return 0;
      }
    });
    const appsCounts = await Promise.all(appsPromises);
    totalApps = appsCounts.reduce((acc, curr) => acc + curr, 0);

    const appsCountEl = document.getElementById('stat-applicants-count');
    if (appsCountEl) appsCountEl.textContent = totalApps;

    try {
      const notifs = await API.get(CONFIG.ENDPOINTS.NOTIFICATIONS.UNREAD_COUNT(userId));
      const notifEl = document.getElementById('stat-unread-notifs');
      if (notifEl) notifEl.textContent = notifs ? (notifs.unreadCount || 0) : 0;
    } catch {
      const notifEl = document.getElementById('stat-unread-notifs');
      if (notifEl) notifEl.textContent = '0';
    }

    if (jobs.length === 0) {
      UI.showEmpty(
        'employer-recent-jobs-container',
        'No Jobs Posted Yet',
        'Start recruiting today by posting your company vacancies.',
        { label: 'Post a New Job', url: 'post-job.html' }
      );
      return;
    }

    let rowsHtml = '';
    jobs.slice(0, 5).forEach((job, idx) => {
      const appCount = appsCounts[idx] || 0;
      rowsHtml += `
        <tr>
          <td>
            <div class="fw-bold text-dark">${UI.escapeHtml(job.title)}</div>
            <small class="text-muted">${UI.escapeHtml(job.location || 'Remote')} &bull; ${UI.getJobTypeBadge(job.jobType)}</small>
          </td>
          <td>${UI.formatDate(job.createdAt)}</td>
          <td>
            <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1">
              ${appCount} candidate${appCount === 1 ? '' : 's'}
            </span>
          </td>
          <td>${UI.getJobStatusBadge(job.status)}</td>
          <td class="text-end">
            <a href="applications.html?jobId=${job.id}" class="btn btn-outline-info btn-sm me-1" title="View Applicants">
              <i class="bi bi-people"></i>
            </a>
            <a href="post-job.html?id=${job.id}" class="btn btn-outline-primary btn-sm" title="Edit">
              <i class="bi bi-pencil"></i>
            </a>
          </td>
        </tr>
      `;
    });

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-custom table-hover align-middle mb-0">
          <thead>
            <tr>
              <th>Job Posting</th>
              <th>Posted Date</th>
              <th>Applicants</th>
              <th>Status</th>
              <th class="text-end">Actions</th>
            </tr>
          </thead>
          <tbody>
            ${rowsHtml}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    console.error('Failed to load employer dashboard data:', err);
    UI.showAlert('employer-recent-jobs-container', `Error loading data: ${err.message}`, 'danger');
  }
}
