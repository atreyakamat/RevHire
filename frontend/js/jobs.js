/**
 * RevHire Jobs Browse & Management Module
 */

document.addEventListener('DOMContentLoaded', () => {
  const isManagePage = window.location.pathname.endsWith('manage-jobs.html');

  if (isManagePage) {
    if (!AUTH.requireAuth('EMPLOYER')) return;
    UI.renderNavbar('manage-jobs');
    UI.renderFooter();
    initEmployerManageJobs();
  } else {
    // Browse jobs page (publicly accessible)
    UI.renderNavbar('jobs');
    UI.renderFooter();
    initBrowseJobs();
  }
});

// ==========================================
// BROWSE JOBS LOGIC (jobs.html)
// ==========================================
let currentPage = 0;
const pageSize = 9;

function initBrowseJobs() {
  const urlParams = new URLSearchParams(window.location.search);
  const keywordInput = document.getElementById('filter-keyword');
  const locationInput = document.getElementById('filter-location');
  const jobTypeSelect = document.getElementById('filter-type');
  const minSalaryInput = document.getElementById('filter-salary');

  if (keywordInput) keywordInput.value = urlParams.get('skills') || '';
  if (locationInput) locationInput.value = urlParams.get('location') || '';
  if (jobTypeSelect) jobTypeSelect.value = urlParams.get('jobType') || '';
  if (minSalaryInput) minSalaryInput.value = urlParams.get('minSalary') || '';

  const filterForm = document.getElementById('jobs-filter-form');
  if (filterForm) {
    filterForm.addEventListener('submit', (e) => {
      e.preventDefault();
      currentPage = 0;
      fetchAndRenderJobs();
    });

    const resetBtn = document.getElementById('btn-reset-filters');
    if (resetBtn) {
      resetBtn.addEventListener('click', () => {
        filterForm.reset();
        currentPage = 0;
        fetchAndRenderJobs();
      });
    }
  }

  fetchAndRenderJobs();
}

async function fetchAndRenderJobs() {
  const container = document.getElementById('jobs-list-container');
  const countEl = document.getElementById('jobs-count-text');
  if (!container) return;

  UI.showLoading('jobs-list-container', 'Loading jobs...');

  const skills = document.getElementById('filter-keyword')?.value?.trim();
  const location = document.getElementById('filter-location')?.value?.trim();
  const jobType = document.getElementById('filter-type')?.value?.trim();
  const minSalary = document.getElementById('filter-salary')?.value?.trim();

  const params = new URLSearchParams();
  params.append('page', currentPage);
  params.append('size', pageSize);
  params.append('status', 'ACTIVE');

  if (skills) params.append('skills', skills);
  if (location) params.append('location', location);
  if (jobType) params.append('jobType', jobType);
  if (minSalary) params.append('minSalary', minSalary);

  try {
    const data = await API.get(`${CONFIG.ENDPOINTS.JOBS.BASE}?${params.toString()}`);
    const jobs = data && data.content ? data.content : [];
    const totalElements = data ? (data.totalElements || jobs.length) : 0;
    const totalPages = data ? (data.totalPages || 1) : 1;

    if (countEl) {
      countEl.textContent = `Showing ${jobs.length} of ${totalElements} jobs`;
    }

    if (jobs.length === 0) {
      UI.showEmpty(
        'jobs-list-container',
        'No Jobs Found',
        'No jobs match your current search filters. Try adjusting keywords, location, or clearing filters.'
      );
      renderPagination(0, 0);
      return;
    }

    let cardsHtml = '<div class="row g-4">';
    jobs.forEach(job => {
      const skillsBadge = job.skills 
        ? job.skills.split(',').slice(0, 3).map(s => `<span class="badge bg-light text-dark border me-1">${UI.escapeHtml(s.trim())}</span>`).join('')
        : '';

      cardsHtml += `
        <div class="col-lg-4 col-md-6">
          <div class="card h-100 card-hover">
            <div class="card-body d-flex flex-column">
              <div class="d-flex justify-content-between align-items-start mb-2">
                <h5 class="card-title fw-bold mb-0">
                  <a href="job-details.html?id=${job.id}" class="text-decoration-none text-dark">
                    ${UI.escapeHtml(job.title)}
                  </a>
                </h5>
                ${UI.getJobTypeBadge(job.jobType)}
              </div>
              
              <div class="job-meta-item mb-2">
                <i class="bi bi-geo-alt"></i>
                <span>${UI.escapeHtml(job.location || 'Remote')}</span>
              </div>
              
              <div class="job-meta-item mb-3">
                <i class="bi bi-cash-stack"></i>
                <span class="fw-semibold text-dark">${UI.formatSalary(job.salary)}</span>
              </div>
              
              <p class="card-text text-muted small flex-grow-1">
                ${UI.escapeHtml(job.description ? job.description.substring(0, 120) + (job.description.length > 120 ? '...' : '') : 'No description provided')}
              </p>
              
              <div class="mb-3">
                ${skillsBadge}
              </div>
              
              <div class="d-flex justify-content-between align-items-center pt-2 border-top mt-auto">
                <small class="text-muted"><i class="bi bi-calendar3 me-1"></i>${UI.formatDate(job.createdAt)}</small>
                <a href="job-details.html?id=${job.id}" class="btn btn-outline-primary btn-sm">
                  View Details
                </a>
              </div>
            </div>
          </div>
        </div>
      `;
    });
    cardsHtml += '</div>';

    container.innerHTML = cardsHtml;
    renderPagination(currentPage, totalPages);
  } catch (err) {
    console.error('Error fetching jobs:', err);
    UI.showAlert('jobs-list-container', `Failed to load jobs: ${err.message}`, 'danger');
  }
}

function renderPagination(current, total) {
  const paginationContainer = document.getElementById('jobs-pagination');
  if (!paginationContainer) return;

  if (total <= 1) {
    paginationContainer.innerHTML = '';
    return;
  }

  let html = '<ul class="pagination justify-content-center">';
  html += `
    <li class="page-item ${current === 0 ? 'disabled' : ''}">
      <button class="page-link" onclick="changeJobPage(${current - 1})" aria-label="Previous">&laquo;</button>
    </li>
  `;

  for (let i = 0; i < total; i++) {
    html += `
      <li class="page-item ${i === current ? 'active' : ''}">
        <button class="page-link" onclick="changeJobPage(${i})">${i + 1}</button>
      </li>
    `;
  }

  html += `
    <li class="page-item ${current >= total - 1 ? 'disabled' : ''}">
      <button class="page-link" onclick="changeJobPage(${current + 1})" aria-label="Next">&raquo;</button>
    </li>
  `;
  html += '</ul>';

  paginationContainer.innerHTML = html;
}

window.changeJobPage = function(newPage) {
  currentPage = newPage;
  fetchAndRenderJobs();
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// ==========================================
// EMPLOYER MANAGE JOBS (manage-jobs.html)
// ==========================================
let employerJobsPage = 0;

function initEmployerManageJobs() {
  const userId = AUTH.getUserId();
  if (!userId) return;
  loadEmployerJobs(userId);
}

async function loadEmployerJobs(employerId) {
  const container = document.getElementById('employer-jobs-container');
  if (!container) return;

  UI.showLoading('employer-jobs-container', 'Loading your job postings...');

  try {
    const data = await API.get(`${CONFIG.ENDPOINTS.JOBS.BY_EMPLOYER(employerId)}?page=${employerJobsPage}&size=20`);
    const jobs = data && data.content ? data.content : [];

    if (jobs.length === 0) {
      UI.showEmpty(
        'employer-jobs-container',
        'No Jobs Posted Yet',
        'You have not published any job openings yet. Start attracting candidates by creating your first job listing.',
        { label: 'Post a New Job', url: 'post-job.html' }
      );
      return;
    }

    let rowsHtml = '';
    jobs.forEach(job => {
      rowsHtml += `
        <tr>
          <td>
            <div class="fw-bold text-dark">${UI.escapeHtml(job.title)}</div>
            <small class="text-muted"><i class="bi bi-clock me-1"></i>Posted ${UI.formatDate(job.createdAt)}</small>
          </td>
          <td>${UI.escapeHtml(job.location || 'Remote')}</td>
          <td>${UI.getJobTypeBadge(job.jobType)}</td>
          <td>${UI.formatSalary(job.salary)}</td>
          <td>${UI.getJobStatusBadge(job.status)}</td>
          <td class="text-end">
            <div class="btn-group btn-group-sm">
              <a href="applications.html?jobId=${job.id}" class="btn btn-outline-info" title="View Applicants">
                <i class="bi bi-people me-1"></i>Applicants
              </a>
              <a href="post-job.html?id=${job.id}" class="btn btn-outline-primary" title="Edit Job">
                <i class="bi bi-pencil"></i>
              </a>
              <button class="btn btn-outline-danger" onclick="confirmDeleteJob(${job.id}, '${UI.escapeHtml(job.title.replace(/'/g, "\\'"))}')" title="Delete Job">
                <i class="bi bi-trash"></i>
              </button>
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
              <th>Job Title</th>
              <th>Location</th>
              <th>Type</th>
              <th>Salary</th>
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
    console.error('Error loading employer jobs:', err);
    UI.showAlert('employer-jobs-container', `Failed to load posted jobs: ${err.message}`, 'danger');
  }
}

let jobToDeleteId = null;

window.confirmDeleteJob = function(jobId, title) {
  jobToDeleteId = jobId;
  const titleEl = document.getElementById('delete-job-title');
  if (titleEl) titleEl.textContent = title;

  const modalEl = document.getElementById('deleteJobModal');
  if (modalEl) {
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }
};

window.executeDeleteJob = async function() {
  if (!jobToDeleteId) return;
  const modalEl = document.getElementById('deleteJobModal');
  const btn = document.getElementById('confirm-delete-job-btn');

  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Deleting...';
  }

  try {
    await API.delete(CONFIG.ENDPOINTS.JOBS.BY_ID(jobToDeleteId));
    if (modalEl) {
      const modal = bootstrap.Modal.getInstance(modalEl);
      if (modal) modal.hide();
    }
    UI.showAlert('manage-alert-placeholder', 'Job posting deleted successfully.', 'success');
    const userId = AUTH.getUserId();
    if (userId) loadEmployerJobs(userId);
  } catch (err) {
    console.error('Failed to delete job:', err);
    UI.showAlert('manage-alert-placeholder', `Failed to delete job: ${err.message}`, 'danger');
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = 'Delete Job';
    }
    jobToDeleteId = null;
  }
};
