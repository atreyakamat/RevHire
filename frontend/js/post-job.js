/**
 * RevHire Post / Edit Job Logic (Employer)
 */

let editingJobId = null;

document.addEventListener('DOMContentLoaded', () => {
  if (!AUTH.requireAuth('EMPLOYER')) return;

  UI.renderNavbar('post-job');
  UI.renderFooter();

  checkEditMode();
  initPostJobForm();
});

async function checkEditMode() {
  const urlParams = new URLSearchParams(window.location.search);
  const jobId = urlParams.get('id');

  if (jobId) {
    editingJobId = jobId;
    document.getElementById('page-title').textContent = 'Edit Job Posting';
    document.getElementById('page-subtitle').textContent = 'Update the details and requirements for this position';
    document.getElementById('submit-job-btn').innerHTML = '<i class="bi bi-check2-circle me-1"></i>Update Job';

    // Show status selector in edit mode (JobStatus: DRAFT, ACTIVE, CLOSED)
    const statusContainer = document.getElementById('status-container');
    if (statusContainer) statusContainer.classList.remove('d-none');

    // Load existing job details
    try {
      const job = await API.get(CONFIG.ENDPOINTS.JOBS.BY_ID(jobId));
      if (job) {
        document.getElementById('job-title').value = job.title || '';
        document.getElementById('job-description').value = job.description || '';
        document.getElementById('job-location').value = job.location || '';
        document.getElementById('job-skills').value = job.skills || '';
        document.getElementById('job-salary').value = job.salary || '';
        document.getElementById('job-type').value = job.jobType || 'FULL_TIME';
        if (document.getElementById('job-status')) {
          document.getElementById('job-status').value = job.status || 'ACTIVE';
        }
      }
    } catch (err) {
      console.error('Failed to load job for editing:', err);
      UI.showAlert('form-alert-placeholder', `Failed to load job data: ${err.message}`, 'danger');
    }
  }
}

function initPostJobForm() {
  const form = document.getElementById('job-form');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    UI.clearAlert('form-alert-placeholder');

    const title = document.getElementById('job-title').value.trim();
    const description = document.getElementById('job-description').value.trim();
    const location = document.getElementById('job-location').value.trim();
    const skills = document.getElementById('job-skills').value.trim();
    const salary = document.getElementById('job-salary').value.trim();
    const jobType = document.getElementById('job-type').value;

    if (!title || !description || !location || !skills || !salary || !jobType) {
      UI.showAlert('form-alert-placeholder', 'Please fill in all required fields.', 'warning');
      return;
    }

    if (isNaN(Number(salary)) || Number(salary) < 0) {
      UI.showAlert('form-alert-placeholder', 'Please provide a valid non-negative salary amount.', 'warning');
      return;
    }

    const submitBtn = document.getElementById('submit-job-btn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Saving...';

    try {
      if (editingJobId) {
        // Edit mode (UpdateJobRequest requires status)
        const status = document.getElementById('job-status')?.value || 'ACTIVE';
        const payload = {
          title,
          description,
          location,
          skills,
          salary: Number(salary),
          jobType,
          status
        };

        await API.put(CONFIG.ENDPOINTS.JOBS.BY_ID(editingJobId), payload);
        UI.showAlert('form-alert-placeholder', 'Job posting updated successfully! Redirecting to Manage Jobs...', 'success');
        setTimeout(() => {
          window.location.href = 'manage-jobs.html';
        }, 1200);
      } else {
        // Create mode (CreateJobRequest)
        const payload = {
          title,
          description,
          location,
          skills,
          salary: Number(salary),
          jobType,
          employerId: AUTH.getUserId()
        };

        const created = await API.post(CONFIG.ENDPOINTS.JOBS.BASE, payload);
        UI.showAlert('form-alert-placeholder', 'Job posted successfully! Redirecting to Manage Jobs...', 'success');
        setTimeout(() => {
          window.location.href = 'manage-jobs.html';
        }, 1200);
      }
    } catch (err) {
      console.error('Failed to save job:', err);
      UI.showAlert('form-alert-placeholder', `Failed to save job: ${err.message}`, 'danger');
      submitBtn.disabled = false;
      submitBtn.innerHTML = editingJobId ? '<i class="bi bi-check2-circle me-1"></i>Update Job' : '<i class="bi bi-plus-circle me-1"></i>Publish Job';
    }
  });
}
