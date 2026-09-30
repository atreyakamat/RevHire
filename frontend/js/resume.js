/**
 * RevHire Structured Resume Builder Module
 */

let existingResumeId = null;

document.addEventListener('DOMContentLoaded', () => {
  if (!AUTH.requireAuth('JOB_SEEKER')) return;

  UI.renderNavbar('resume');
  UI.renderFooter();

  loadResume();
  initResumeEvents();
});

async function loadResume() {
  const userId = AUTH.getUserId();
  if (!userId) return;

  try {
    const resume = await API.get(CONFIG.ENDPOINTS.RESUMES.BY_USER(userId));
    if (resume && resume.id) {
      existingResumeId = resume.id;
      document.getElementById('resume-summary').value = resume.summary || '';
      document.getElementById('save-resume-btn').innerHTML = '<i class="bi bi-check2-circle me-1"></i>Update Resume';
      
      const deleteBtn = document.getElementById('delete-resume-btn');
      if (deleteBtn) deleteBtn.classList.remove('d-none');

      // Populate Education
      const eduContainer = document.getElementById('education-list-container');
      eduContainer.innerHTML = '';
      if (resume.educationList && resume.educationList.length > 0) {
        resume.educationList.forEach(addEducationRow);
      } else {
        addEducationRow();
      }

      // Populate Experience
      const expContainer = document.getElementById('experience-list-container');
      expContainer.innerHTML = '';
      if (resume.experienceList && resume.experienceList.length > 0) {
        resume.experienceList.forEach(addExperienceRow);
      } else {
        addExperienceRow();
      }

      // Populate Skills
      const skillContainer = document.getElementById('skills-list-container');
      skillContainer.innerHTML = '';
      if (resume.skills && resume.skills.length > 0) {
        resume.skills.forEach(addSkillRow);
      } else {
        addSkillRow();
      }
    } else {
      setupDefaultEmptyRows();
    }
  } catch {
    // 404 / no resume yet
    setupDefaultEmptyRows();
  }
}

function setupDefaultEmptyRows() {
  addEducationRow();
  addExperienceRow();
  addSkillRow();
}

function initResumeEvents() {
  document.getElementById('add-education-btn')?.addEventListener('click', () => addEducationRow());
  document.getElementById('add-experience-btn')?.addEventListener('click', () => addExperienceRow());
  document.getElementById('add-skill-btn')?.addEventListener('click', () => addSkillRow());

  const form = document.getElementById('resume-form');
  if (form) {
    form.addEventListener('submit', handleSaveResume);
  }

  const deleteBtn = document.getElementById('delete-resume-btn');
  if (deleteBtn) {
    deleteBtn.addEventListener('click', confirmDeleteResume);
  }
}

// Dynamic Education Row
function addEducationRow(data = {}) {
  const container = document.getElementById('education-list-container');
  const div = document.createElement('div');
  div.className = 'dynamic-row education-item';
  div.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-2">
      <span class="fw-semibold text-secondary small">Education Entry</span>
      <button type="button" class="btn btn-outline-danger btn-sm py-0 px-2" onclick="this.closest('.education-item').remove()" title="Remove">
        <i class="bi bi-trash"></i>
      </button>
    </div>
    <div class="row g-2">
      <div class="col-md-6">
        <label class="form-label small">Degree / Major</label>
        <input type="text" class="form-control form-control-sm edu-degree" value="${UI.escapeHtml(data.degree || '')}" placeholder="B.S. in Computer Science">
      </div>
      <div class="col-md-6">
        <label class="form-label small">Institution</label>
        <input type="text" class="form-control form-control-sm edu-institution" value="${UI.escapeHtml(data.institution || '')}" placeholder="University Name">
      </div>
      <div class="col-md-6">
        <label class="form-label small">Start Date</label>
        <input type="text" class="form-control form-control-sm edu-start" value="${UI.escapeHtml(data.startDate || '')}" placeholder="YYYY or YYYY-MM">
      </div>
      <div class="col-md-6">
        <label class="form-label small">End Date</label>
        <input type="text" class="form-control form-control-sm edu-end" value="${UI.escapeHtml(data.endDate || '')}" placeholder="YYYY or Present">
      </div>
    </div>
  `;
  container.appendChild(div);
}

// Dynamic Experience Row
function addExperienceRow(data = {}) {
  const container = document.getElementById('experience-list-container');
  const div = document.createElement('div');
  div.className = 'dynamic-row experience-item';
  div.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-2">
      <span class="fw-semibold text-secondary small">Experience Entry</span>
      <button type="button" class="btn btn-outline-danger btn-sm py-0 px-2" onclick="this.closest('.experience-item').remove()" title="Remove">
        <i class="bi bi-trash"></i>
      </button>
    </div>
    <div class="row g-2">
      <div class="col-md-6">
        <label class="form-label small">Job Title</label>
        <input type="text" class="form-control form-control-sm exp-title" value="${UI.escapeHtml(data.jobTitle || '')}" placeholder="Software Engineer">
      </div>
      <div class="col-md-6">
        <label class="form-label small">Company</label>
        <input type="text" class="form-control form-control-sm exp-company" value="${UI.escapeHtml(data.company || '')}" placeholder="Company Name">
      </div>
      <div class="col-md-6">
        <label class="form-label small">Start Date</label>
        <input type="text" class="form-control form-control-sm exp-start" value="${UI.escapeHtml(data.startDate || '')}" placeholder="YYYY-MM">
      </div>
      <div class="col-md-6">
        <label class="form-label small">End Date</label>
        <input type="text" class="form-control form-control-sm exp-end" value="${UI.escapeHtml(data.endDate || '')}" placeholder="YYYY-MM or Present">
      </div>
      <div class="col-12">
        <label class="form-label small">Description / Responsibilities</label>
        <textarea class="form-control form-control-sm exp-desc" rows="2" placeholder="Key achievements and technologies used">${UI.escapeHtml(data.description || '')}</textarea>
      </div>
    </div>
  `;
  container.appendChild(div);
}

// Dynamic Skill Row
function addSkillRow(data = {}) {
  const container = document.getElementById('skills-list-container');
  const div = document.createElement('div');
  div.className = 'dynamic-row skill-item';
  div.innerHTML = `
    <div class="row g-2 align-items-center">
      <div class="col-md-6">
        <input type="text" class="form-control form-control-sm skill-name" value="${UI.escapeHtml(data.name || '')}" placeholder="Skill (e.g. Java, Docker, REST APIs)">
      </div>
      <div class="col-md-5">
        <select class="form-select form-select-sm skill-proficiency">
          <option value="Beginner" ${data.proficiency === 'Beginner' ? 'selected' : ''}>Beginner</option>
          <option value="Intermediate" ${!data.proficiency || data.proficiency === 'Intermediate' ? 'selected' : ''}>Intermediate</option>
          <option value="Advanced" ${data.proficiency === 'Advanced' ? 'selected' : ''}>Advanced</option>
          <option value="Expert" ${data.proficiency === 'Expert' ? 'selected' : ''}>Expert</option>
        </select>
      </div>
      <div class="col-md-1 text-end">
        <button type="button" class="btn btn-outline-danger btn-sm py-0 px-2" onclick="this.closest('.skill-item').remove()" title="Remove">
          <i class="bi bi-trash"></i>
        </button>
      </div>
    </div>
  `;
  container.appendChild(div);
}

async function handleSaveResume(e) {
  e.preventDefault();
  UI.clearAlert('resume-alert-placeholder');

  const summary = document.getElementById('resume-summary')?.value?.trim() || '';

  // Extract Education
  const educationList = [];
  document.querySelectorAll('.education-item').forEach(el => {
    const degree = el.querySelector('.edu-degree')?.value?.trim();
    const institution = el.querySelector('.edu-institution')?.value?.trim();
    const startDate = el.querySelector('.edu-start')?.value?.trim();
    const endDate = el.querySelector('.edu-end')?.value?.trim();
    if (degree || institution) {
      educationList.push({ degree, institution, startDate, endDate });
    }
  });

  // Extract Experience
  const experienceList = [];
  document.querySelectorAll('.experience-item').forEach(el => {
    const jobTitle = el.querySelector('.exp-title')?.value?.trim();
    const company = el.querySelector('.exp-company')?.value?.trim();
    const startDate = el.querySelector('.exp-start')?.value?.trim();
    const endDate = el.querySelector('.exp-end')?.value?.trim();
    const description = el.querySelector('.exp-desc')?.value?.trim();
    if (jobTitle || company) {
      experienceList.push({ jobTitle, company, startDate, endDate, description });
    }
  });

  // Extract Skills
  const skills = [];
  document.querySelectorAll('.skill-item').forEach(el => {
    const name = el.querySelector('.skill-name')?.value?.trim();
    const proficiency = el.querySelector('.skill-proficiency')?.value;
    if (name) {
      skills.push({ name, proficiency });
    }
  });

  const payload = {
    summary,
    educationList,
    experienceList,
    skills
  };

  const saveBtn = document.getElementById('save-resume-btn');
  saveBtn.disabled = true;
  saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Saving...';

  try {
    if (existingResumeId) {
      await API.put(CONFIG.ENDPOINTS.RESUMES.BY_ID(existingResumeId), payload);
      UI.showAlert('resume-alert-placeholder', 'Resume updated successfully!', 'success');
    } else {
      const created = await API.post(CONFIG.ENDPOINTS.RESUMES.BASE, payload);
      if (created && created.id) existingResumeId = created.id;
      UI.showAlert('resume-alert-placeholder', 'Resume created successfully!', 'success');
      saveBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i>Update Resume';
      const deleteBtn = document.getElementById('delete-resume-btn');
      if (deleteBtn) deleteBtn.classList.remove('d-none');
    }
  } catch (err) {
    console.error('Failed to save resume:', err);
    UI.showAlert('resume-alert-placeholder', `Failed to save resume: ${err.message}`, 'danger');
  } finally {
    saveBtn.disabled = false;
    if (existingResumeId) {
      saveBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i>Update Resume';
    } else {
      saveBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i>Create Resume';
    }
  }
}

function confirmDeleteResume() {
  if (!existingResumeId) return;
  const modalEl = document.getElementById('deleteResumeModal');
  if (modalEl) {
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }
}

window.executeDeleteResume = async function() {
  if (!existingResumeId) return;

  const btn = document.getElementById('confirm-delete-resume-btn');
  const modalEl = document.getElementById('deleteResumeModal');

  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Deleting...';
  }

  try {
    await API.delete(CONFIG.ENDPOINTS.RESUMES.BY_ID(existingResumeId));
    existingResumeId = null;
    if (modalEl) {
      const modal = bootstrap.Modal.getInstance(modalEl);
      if (modal) modal.hide();
    }
    UI.showAlert('resume-alert-placeholder', 'Resume deleted successfully.', 'success');
    document.getElementById('resume-form').reset();
    document.getElementById('education-list-container').innerHTML = '';
    document.getElementById('experience-list-container').innerHTML = '';
    document.getElementById('skills-list-container').innerHTML = '';
    setupDefaultEmptyRows();
    document.getElementById('delete-resume-btn').classList.add('d-none');
    document.getElementById('save-resume-btn').innerHTML = '<i class="bi bi-check2-circle me-1"></i>Create Resume';
  } catch (err) {
    console.error('Failed to delete resume:', err);
    UI.showAlert('resume-alert-placeholder', `Failed to delete resume: ${err.message}`, 'danger');
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = 'Delete Resume';
    }
  }
};
