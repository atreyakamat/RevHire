/**
 * RevHire Job Details & Application Submission Module
 */

let currentJob = null;
let userResume = null;

document.addEventListener('DOMContentLoaded', () => {
  UI.renderNavbar('jobs');
  UI.renderFooter();

  loadJobDetails();
});

async function loadJobDetails() {
  const container = document.getElementById('job-details-container');
  if (!container) return;

  const urlParams = new URLSearchParams(window.location.search);
  const jobId = urlParams.get('id');

  if (!jobId) {
    UI.showAlert('job-details-container', 'No job ID specified in the URL.', 'warning');
    return;
  }

  UI.showLoading('job-details-container', 'Loading job details...');

  try {
    currentJob = await API.get(CONFIG.ENDPOINTS.JOBS.BY_ID(jobId));
    if (!currentJob) {
      UI.showEmpty('job-details-container', 'Job Not Found', 'The requested job posting does not exist or has been removed.');
      return;
    }

    renderJob(currentJob);

    // If seeker is logged in, preload resume to prepare application submission
    if (AUTH.isLoggedIn() && AUTH.getRole() === 'JOB_SEEKER') {
      preloadSeekerResume();
    }
  } catch (err) {
    console.error('Failed to load job details:', err);
    UI.showAlert('job-details-container', `Error loading job: ${err.message}`, 'danger');
  }
}

async function preloadSeekerResume() {
  const userId = AUTH.getUserId();
  if (!userId) return;

  try {
    userResume = await API.get(CONFIG.ENDPOINTS.RESUMES.BY_USER(userId));
  } catch {
    userResume = null;
  }
}

function renderJob(job) {
  const container = document.getElementById('job-details-container');
  if (!container) return;

  const isLoggedIn = AUTH.isLoggedIn();
  const role = AUTH.getRole();
  const userId = AUTH.getUserId();

  let actionButtonHtml = '';

  if (!isLoggedIn) {
    actionButtonHtml = `
      <a href="login.html?redirect=job-details.html?id=${job.id}" class="btn btn-primary btn-lg w-100 shadow-sm">
        <i class="bi bi-box-arrow-in-right me-2"></i>Login to Apply
      </a>
    `;
  } else if (role === 'JOB_SEEKER') {
    if (job.status !== 'ACTIVE') {
      actionButtonHtml = `
        <button class="btn btn-secondary btn-lg w-100" disabled>
          <i class="bi bi-x-circle me-2"></i>Job Closed
        </button>
      `;
    } else {
      actionButtonHtml = `
        <button id="btn-apply-job" class="btn btn-primary btn-lg w-100 shadow-sm" onclick="handleApplyClick()">
          <i class="bi bi-send me-2"></i>Apply for this Job
        </button>
      `;
    }
  } else if (role === 'EMPLOYER') {
    if (job.employerId && Number(job.employerId) === Number(userId)) {
      actionButtonHtml = `
        <div class="d-grid gap-2">
          <a href="applications.html?jobId=${job.id}" class="btn btn-info text-white">
            <i class="bi bi-people me-2"></i>View Job Applicants
          </a>
          <a href="post-job.html?id=${job.id}" class="btn btn-outline-primary">
            <i class="bi bi-pencil me-2"></i>Edit Job Details
          </a>
        </div>
      `;
    } else {
      actionButtonHtml = `
        <div class="alert alert-light border small text-muted text-center mb-0">
          <i class="bi bi-info-circle me-1"></i>You are viewing this job as an employer.
        </div>
      `;
    }
  }

  const skillsList = job.skills
    ? job.skills.split(',').map(s => `<span class="badge bg-light text-dark border px-3 py-2 me-2 mb-2">${UI.escapeHtml(s.trim())}</span>`).join('')
    : '<span class="text-muted">Not specified</span>';

  container.innerHTML = `
    <div class="row g-4">
      <!-- Main Content Column -->
      <div class="col-lg-8">
        <div class="card p-4 mb-4">
          <div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-3">
            <div>
              <h2 class="fw-bold text-dark mb-1">${UI.escapeHtml(job.title)}</h2>
              <div class="d-flex align-items-center gap-3 text-muted">
                <span class="job-meta-item"><i class="bi bi-geo-alt"></i>${UI.escapeHtml(job.location || 'Remote')}</span>
                <span class="job-meta-item"><i class="bi bi-calendar3"></i>Posted ${UI.formatDate(job.createdAt)}</span>
              </div>
            </div>
            <div>
              ${UI.getJobTypeBadge(job.jobType)}
              ${UI.getJobStatusBadge(job.status)}
            </div>
          </div>

          <hr class="my-4">

          <h5 class="fw-bold text-dark mb-3"><i class="bi bi-file-text me-2 text-primary"></i>Job Description</h5>
          <div class="text-secondary mb-4" style="white-space: pre-line; line-height: 1.7;">
            ${UI.escapeHtml(job.description || 'No detailed description provided.')}
          </div>

          <h5 class="fw-bold text-dark mb-3"><i class="bi bi-tools me-2 text-primary"></i>Required Skills &amp; Qualifications</h5>
          <div class="d-flex flex-wrap">
            ${skillsList}
          </div>
        </div>
      </div>

      <!-- Sidebar / Action Card -->
      <div class="col-lg-4">
        <div class="card p-4 sticky-top" style="top: 85px;">
          <h5 class="fw-bold text-dark mb-3">Job Summary</h5>
          
          <div class="d-flex justify-content-between py-2 border-bottom">
            <span class="text-muted">Offered Compensation</span>
            <span class="fw-bold text-dark">${UI.formatSalary(job.salary)}</span>
          </div>
          
          <div class="d-flex justify-content-between py-2 border-bottom">
            <span class="text-muted">Job Classification</span>
            <span class="fw-semibold text-dark">${UI.escapeHtml(job.jobType || 'N/A')}</span>
          </div>

          <div class="d-flex justify-content-between py-2 border-bottom">
            <span class="text-muted">Location</span>
            <span class="fw-semibold text-dark">${UI.escapeHtml(job.location || 'Remote')}</span>
          </div>

          <div class="d-flex justify-content-between py-2 border-bottom mb-4">
            <span class="text-muted">Current Status</span>
            <span>${UI.getJobStatusBadge(job.status)}</span>
          </div>

          <div id="apply-feedback-placeholder"></div>
          ${actionButtonHtml}

          <div class="mt-4 pt-3 border-top text-center">
            <a href="jobs.html" class="text-decoration-none small text-muted">
              <i class="bi bi-arrow-left me-1"></i>Back to all jobs
            </a>
          </div>
        </div>
      </div>
    </div>
  `;
}

window.handleApplyClick = async function() {
  if (!currentJob) return;

  const userId = AUTH.getUserId();
  if (!userId) {
    window.location.href = `login.html?redirect=job-details.html?id=${currentJob.id}`;
    return;
  }

  // Check if resume is preloaded
  if (!userResume) {
    try {
      userResume = await API.get(CONFIG.ENDPOINTS.RESUMES.BY_USER(userId));
    } catch {
      userResume = null;
    }
  }

  if (!userResume || !userResume.id) {
    // Seeker does not have a structured resume yet
    const modalEl = document.getElementById('noResumeModal');
    if (modalEl) {
      const modal = new bootstrap.Modal(modalEl);
      modal.show();
    } else {
      alert('You need to create a profile resume before applying to jobs. Redirecting to Resume builder...');
      window.location.href = 'resume.html';
    }
    return;
  }

  // Open Apply Confirmation Modal
  const resumeSummaryEl = document.getElementById('apply-resume-summary');
  if (resumeSummaryEl) {
    resumeSummaryEl.textContent = userResume.summary || 'Resume on file';
  }

  const modalEl = document.getElementById('applyConfirmationModal');
  if (modalEl) {
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }
};

window.submitApplication = async function() {
  if (!currentJob || !userResume) return;

  const btn = document.getElementById('confirm-submit-application-btn');
  const modalEl = document.getElementById('applyConfirmationModal');

  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Submitting...';
  }

  try {
    const payload = {
      jobId: currentJob.id,
      userId: AUTH.getUserId(),
      resumeId: userResume.id
    };

    await API.post(CONFIG.ENDPOINTS.APPLICATIONS.BASE, payload);

    if (modalEl) {
      const modal = bootstrap.Modal.getInstance(modalEl);
      if (modal) modal.hide();
    }

    UI.showAlert('apply-feedback-placeholder', 'Application submitted successfully! You can track status under My Applications.', 'success');

    const applyBtn = document.getElementById('btn-apply-job');
    if (applyBtn) {
      applyBtn.disabled = true;
      applyBtn.className = 'btn btn-success btn-lg w-100';
      applyBtn.innerHTML = '<i class="bi bi-check-circle me-2"></i>Application Submitted';
    }
  } catch (err) {
    console.error('Failed to submit application:', err);
    UI.showAlert('apply-modal-alert-placeholder', `Failed to apply: ${err.message}`, 'danger');
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = 'Confirm &amp; Submit Application';
    }
  }
};
