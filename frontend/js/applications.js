/**
 * RevHire Applications Module
 * Handles Candidate's "My Applications" and Employer's "Candidate Applications Review".
 */

document.addEventListener('DOMContentLoaded', () => {
  const isMyApplications = window.location.pathname.endsWith('my-applications.html');

  if (isMyApplications) {
    if (!AUTH.requireAuth('JOB_SEEKER')) return;
    UI.renderNavbar('my-applications');
    UI.renderFooter();
    initSeekerApplications();
  } else {
    // Employer applications review page (applications.html)
    if (!AUTH.requireAuth('EMPLOYER')) return;
    UI.renderNavbar('applications');
    UI.renderFooter();
    initEmployerApplications();
  }
});

// ==========================================
// JOB SEEKER APPLICATIONS (my-applications.html)
// ==========================================
async function initSeekerApplications() {
  const container = document.getElementById('my-applications-container');
  if (!container) return;

  UI.showLoading('my-applications-container', 'Loading your submitted applications...');

  try {
    const applications = await API.get(CONFIG.ENDPOINTS.APPLICATIONS.BASE);

    if (!applications || applications.length === 0) {
      UI.showEmpty(
        'my-applications-container',
        'No Applications Submitted',
        'You have not applied to any job postings yet. Explore open roles and submit your applications!',
        { label: 'Browse Jobs', url: 'jobs.html' }
      );
      return;
    }

    // Enrich applications with Job details
    const enrichedList = await Promise.all(
      applications.map(async (app) => {
        try {
          const job = await API.get(CONFIG.ENDPOINTS.JOBS.BY_ID(app.jobId));
          return { ...app, job };
        } catch {
          return { ...app, job: null };
        }
      })
    );

    let rowsHtml = '';
    enrichedList.forEach(app => {
      const jobTitle = app.job ? app.job.title : `Job #${app.jobId}`;
      const jobLocation = app.job ? app.job.location : 'N/A';
      const jobType = app.job ? app.job.jobType : 'N/A';
      const canWithdraw = app.status === 'APPLIED' || app.status === 'UNDER_REVIEW';

      rowsHtml += `
        <tr>
          <td>
            <div class="fw-bold text-dark">
              ${app.job ? `<a href="job-details.html?id=${app.job.id}" class="text-decoration-none text-dark">${UI.escapeHtml(jobTitle)}</a>` : UI.escapeHtml(jobTitle)}
            </div>
            <small class="text-muted"><i class="bi bi-geo-alt me-1"></i>${UI.escapeHtml(jobLocation)} &bull; ${UI.escapeHtml(jobType)}</small>
          </td>
          <td>${UI.formatDate(app.appliedAt)}</td>
          <td>${UI.getApplicationStatusBadge(app.status)}</td>
          <td class="text-end">
            ${canWithdraw ? `
              <button class="btn btn-outline-danger btn-sm" onclick="confirmWithdrawApplication(${app.id})" title="Withdraw Application">
                <i class="bi bi-x-circle me-1"></i>Withdraw
              </button>
            ` : `
              <span class="text-muted small">&mdash;</span>
            `}
          </td>
        </tr>
      `;
    });

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-custom table-hover align-middle mb-0">
          <thead>
            <tr>
              <th>Job Position</th>
              <th>Applied Date</th>
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
    console.error('Failed to load applications:', err);
    UI.showAlert('my-applications-container', `Failed to load applications: ${err.message}`, 'danger');
  }
}

let appToWithdrawId = null;

window.confirmWithdrawApplication = function(appId) {
  appToWithdrawId = appId;
  const modalEl = document.getElementById('withdrawAppModal');
  if (modalEl) {
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }
};

window.executeWithdrawApplication = async function() {
  if (!appToWithdrawId) return;

  const btn = document.getElementById('confirm-withdraw-btn');
  const modalEl = document.getElementById('withdrawAppModal');

  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Withdrawing...';
  }

  try {
    await API.delete(CONFIG.ENDPOINTS.APPLICATIONS.BY_ID(appToWithdrawId));
    if (modalEl) {
      const modal = bootstrap.Modal.getInstance(modalEl);
      if (modal) modal.hide();
    }
    UI.showAlert('alert-placeholder', 'Application withdrawn successfully.', 'success');
    initSeekerApplications();
  } catch (err) {
    console.error('Failed to withdraw application:', err);
    UI.showAlert('alert-placeholder', `Failed to withdraw application: ${err.message}`, 'danger');
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = 'Yes, Withdraw Application';
    }
    appToWithdrawId = null;
  }
};

// ==========================================
// EMPLOYER APPLICATIONS (applications.html)
// ==========================================
let employerJobsList = [];

async function initEmployerApplications() {
  const userId = AUTH.getUserId();
  if (!userId) return;

  const jobSelect = document.getElementById('job-filter-select');
  const container = document.getElementById('employer-applications-container');
  if (!container) return;

  UI.showLoading('employer-applications-container', 'Loading candidate applications...');

  try {
    // 1. Fetch employer's jobs
    const jobsData = await API.get(`${CONFIG.ENDPOINTS.JOBS.BY_EMPLOYER(userId)}?page=0&size=100`);
    employerJobsList = jobsData && jobsData.content ? jobsData.content : [];

    if (employerJobsList.length === 0) {
      UI.showEmpty(
        'employer-applications-container',
        'No Jobs Posted Yet',
        'You have not posted any jobs yet. Post a job to start receiving candidate applications.',
        { label: 'Post a Job', url: 'post-job.html' }
      );
      return;
    }

    // Populate job select dropdown
    const urlParams = new URLSearchParams(window.location.search);
    const selectedJobId = urlParams.get('jobId');

    if (jobSelect) {
      jobSelect.innerHTML = '<option value="">All Posted Jobs</option>' + 
        employerJobsList.map(j => `<option value="${j.id}" ${selectedJobId == j.id ? 'selected' : ''}>${UI.escapeHtml(j.title)}</option>`).join('');

      jobSelect.addEventListener('change', () => {
        loadApplicationsForEmployer(jobSelect.value);
      });
    }

    await loadApplicationsForEmployer(selectedJobId || '');
  } catch (err) {
    console.error('Failed to initialize employer applications:', err);
    UI.showAlert('employer-applications-container', `Failed to load applications: ${err.message}`, 'danger');
  }
}

async function loadApplicationsForEmployer(filterJobId) {
  const container = document.getElementById('employer-applications-container');
  if (!container) return;

  UI.showLoading('employer-applications-container', 'Loading applications...');

  try {
    let allApps = [];

    if (filterJobId) {
      // Fetch for single job
      const apps = await API.get(CONFIG.ENDPOINTS.APPLICATIONS.BY_JOB(filterJobId));
      allApps = apps || [];
    } else {
      // Fetch for all jobs owned by employer
      const appsPromises = employerJobsList.map(async (job) => {
        try {
          return await API.get(CONFIG.ENDPOINTS.APPLICATIONS.BY_JOB(job.id));
        } catch {
          return [];
        }
      });
      const results = await Promise.all(appsPromises);
      allApps = results.flat();
    }

    if (allApps.length === 0) {
      UI.showEmpty(
        'employer-applications-container',
        'No Candidate Applications',
        filterJobId ? 'No applications have been received for this specific position yet.' : 'No candidates have applied to your active job listings yet.'
      );
      return;
    }

    // Build map of jobs for title lookup
    const jobMap = new Map();
    employerJobsList.forEach(j => jobMap.set(j.id, j));

    let rowsHtml = '';
    allApps.forEach(app => {
      const job = jobMap.get(app.jobId);
      const jobTitle = job ? job.title : `Job #${app.jobId}`;

      rowsHtml += `
        <tr id="app-row-${app.id}">
          <td>
            <div class="fw-bold text-dark">Candidate #${app.userId}</div>
            <small class="text-muted">Application ID: ${app.id}</small>
          </td>
          <td>
            <div class="fw-semibold text-dark">${UI.escapeHtml(jobTitle)}</div>
          </td>
          <td>${UI.formatDate(app.appliedAt)}</td>
          <td>
            <button class="btn btn-outline-primary btn-sm" onclick="viewCandidateResume(${app.userId})" title="View Resume">
              <i class="bi bi-file-earmark-person me-1"></i>View Resume
            </button>
          </td>
          <td id="status-cell-${app.id}">
            ${UI.getApplicationStatusBadge(app.status)}
          </td>
          <td class="text-end">
            <div class="dropdown">
              <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                Update Status
              </button>
              <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                <li><button class="dropdown-item" onclick="changeApplicationStatus(${app.id}, 'UNDER_REVIEW')">Under Review</button></li>
                <li><button class="dropdown-item" onclick="changeApplicationStatus(${app.id}, 'SHORTLISTED')">Shortlist</button></li>
                <li><button class="dropdown-item text-success" onclick="changeApplicationStatus(${app.id}, 'HIRED')">Hire Candidate</button></li>
                <li><button class="dropdown-item text-danger" onclick="changeApplicationStatus(${app.id}, 'REJECTED')">Reject Application</button></li>
              </ul>
            </div>
          </td>
        </tr>
      `;
    });

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-custom table-hover align-middle mb-0">
          <thead>
            <tr>
              <th>Applicant</th>
              <th>Applied For</th>
              <th>Date Applied</th>
              <th>Resume</th>
              <th>Current Status</th>
              <th class="text-end">Manage Status</th>
            </tr>
          </thead>
          <tbody>
            ${rowsHtml}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    console.error('Failed to load employer applications:', err);
    UI.showAlert('employer-applications-container', `Failed to load applications: ${err.message}`, 'danger');
  }
}

window.changeApplicationStatus = async function(appId, newStatus) {
  try {
    const updated = await API.put(CONFIG.ENDPOINTS.APPLICATIONS.UPDATE_STATUS(appId, newStatus));
    const cell = document.getElementById(`status-cell-${appId}`);
    if (cell && updated) {
      cell.innerHTML = UI.getApplicationStatusBadge(updated.status);
    }
    UI.showAlert('alert-placeholder', `Status updated to ${newStatus.replace('_', ' ')}.`, 'success');
  } catch (err) {
    console.error('Failed to update application status:', err);
    UI.showAlert('alert-placeholder', `Failed to update status: ${err.message}`, 'danger');
  }
};

window.viewCandidateResume = async function(candidateId) {
  const modalEl = document.getElementById('viewResumeModal');
  const bodyEl = document.getElementById('view-resume-modal-body');
  if (!modalEl || !bodyEl) return;

  bodyEl.innerHTML = `
    <div class="text-center py-4">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="text-muted mt-2">Loading candidate profile &amp; resume...</p>
    </div>
  `;

  const modal = new bootstrap.Modal(modalEl);
  modal.show();

  try {
    // 1. Candidate user profile
    let profile = null;
    try {
      profile = await API.get(CONFIG.ENDPOINTS.USERS.BY_ID(candidateId));
    } catch {
      profile = null;
    }

    // 2. Candidate resume
    let resume = null;
    try {
      resume = await API.get(CONFIG.ENDPOINTS.RESUMES.BY_USER(candidateId));
    } catch {
      resume = null;
    }

    let contentHtml = '';

    if (profile) {
      contentHtml += `
        <div class="border-bottom pb-3 mb-3">
          <h5 class="fw-bold text-dark mb-1">
            ${UI.escapeHtml((profile.firstName || '') + ' ' + (profile.lastName || '')).trim() || 'Candidate #' + candidateId}
          </h5>
          <div class="text-muted small">
            <span><i class="bi bi-envelope me-1"></i>${UI.escapeHtml(profile.email || 'N/A')}</span>
            ${profile.phone ? `<span class="ms-3"><i class="bi bi-telephone me-1"></i>${UI.escapeHtml(profile.phone)}</span>` : ''}
          </div>
        </div>
      `;
    }

    if (!resume) {
      contentHtml += `
        <div class="alert alert-warning small mb-0">
          <i class="bi bi-exclamation-triangle me-1"></i>No structured resume data found for this candidate.
        </div>
      `;
    } else {
      contentHtml += `
        <div class="mb-3">
          <h6 class="fw-bold text-dark">Professional Summary</h6>
          <p class="text-secondary small mb-3">${UI.escapeHtml(resume.summary || 'No summary provided')}</p>
        </div>
      `;

      if (resume.educationList && resume.educationList.length > 0) {
        contentHtml += `<h6 class="fw-bold text-dark mb-2">Education</h6><ul class="list-group list-group-flush mb-3">`;
        resume.educationList.forEach(e => {
          contentHtml += `
            <li class="list-group-item px-0">
              <div class="fw-semibold text-dark">${UI.escapeHtml(e.degree || 'Degree')}</div>
              <small class="text-muted">${UI.escapeHtml(e.institution || '')} (${UI.escapeHtml(e.startDate || '')} - ${UI.escapeHtml(e.endDate || 'Present')})</small>
            </li>
          `;
        });
        contentHtml += `</ul>`;
      }

      if (resume.experienceList && resume.experienceList.length > 0) {
        contentHtml += `<h6 class="fw-bold text-dark mb-2">Experience</h6><ul class="list-group list-group-flush mb-3">`;
        resume.experienceList.forEach(exp => {
          contentHtml += `
            <li class="list-group-item px-0">
              <div class="fw-semibold text-dark">${UI.escapeHtml(exp.jobTitle || 'Role')} at ${UI.escapeHtml(exp.company || '')}</div>
              <small class="text-muted d-block">${UI.escapeHtml(exp.startDate || '')} - ${UI.escapeHtml(exp.endDate || 'Present')}</small>
              <small class="text-secondary">${UI.escapeHtml(exp.description || '')}</small>
            </li>
          `;
        });
        contentHtml += `</ul>`;
      }

      if (resume.skills && resume.skills.length > 0) {
        contentHtml += `<h6 class="fw-bold text-dark mb-2">Skills</h6><div class="d-flex flex-wrap gap-2">`;
        resume.skills.forEach(s => {
          contentHtml += `<span class="badge bg-light text-dark border">${UI.escapeHtml(s.name || '')} (${UI.escapeHtml(s.proficiency || 'Proficient')})</span>`;
        });
        contentHtml += `</div>`;
      }
    }

    bodyEl.innerHTML = contentHtml;
  } catch (err) {
    console.error('Failed to view resume:', err);
    bodyEl.innerHTML = `
      <div class="alert alert-danger small mb-0">
        Could not load candidate information: ${UI.escapeHtml(err.message)}
      </div>
    `;
  }
};
