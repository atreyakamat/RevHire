/**
 * RevHire Landing Page Logic
 */

document.addEventListener('DOMContentLoaded', () => {
  UI.renderNavbar('home');
  UI.renderFooter();

  initSearchForm();
  loadRecentJobs();
});

function initSearchForm() {
  const form = document.getElementById('hero-search-form');
  if (!form) return;

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const skills = document.getElementById('search-keyword')?.value?.trim() || '';
    const location = document.getElementById('search-location')?.value?.trim() || '';

    const params = new URLSearchParams();
    if (skills) params.append('skills', skills);
    if (location) params.append('location', location);

    window.location.href = `jobs.html?${params.toString()}`;
  });
}

async function loadRecentJobs() {
  const container = document.getElementById('featured-jobs-container');
  if (!container) return;

  UI.showLoading('featured-jobs-container', 'Loading recent job openings...');

  try {
    // Fetch latest active jobs from backend Job Service
    const response = await API.get(`${CONFIG.ENDPOINTS.JOBS.BASE}?page=0&size=6&status=ACTIVE`);
    const jobs = response && response.content ? response.content : [];

    if (jobs.length === 0) {
      UI.showEmpty(
        'featured-jobs-container',
        'No Active Job Postings Yet',
        'Employers have not published any open positions yet. Check back soon or register as an employer to post the first job!',
        AUTH.getRole() === 'EMPLOYER' ? { label: 'Post a Job', url: 'post-job.html' } : { label: 'Register as Employer', url: 'register.html?role=EMPLOYER' }
      );
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
                <h5 class="card-title fw-bold text-dark mb-0">
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
                ${UI.escapeHtml(job.description ? job.description.substring(0, 110) + (job.description.length > 110 ? '...' : '') : 'No description provided')}
              </p>
              
              <div class="mb-3">
                ${skillsBadge}
              </div>
              
              <div class="d-flex justify-content-between align-items-center pt-2 border-top mt-auto">
                <small class="text-muted"><i class="bi bi-clock me-1"></i>${UI.formatDate(job.createdAt)}</small>
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
  } catch (err) {
    console.error('Failed to load recent jobs:', err);
    UI.showAlert('featured-jobs-container', 'Unable to retrieve live job postings from the server at this time.', 'danger');
  }
}
