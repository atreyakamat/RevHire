/**
 * RevHire Profile Management Module
 */

let currentProfile = null;

document.addEventListener('DOMContentLoaded', () => {
  if (!AUTH.requireAuth()) return;

  UI.renderNavbar('profile');
  UI.renderFooter();

  loadUserProfile();
  initProfileForm();
});

async function loadUserProfile() {
  const container = document.getElementById('profile-form-container');
  if (!container) return;

  try {
    currentProfile = await API.get(CONFIG.ENDPOINTS.USERS.ME);
    if (!currentProfile) return;

    // Static header information
    document.getElementById('profile-email').textContent = currentProfile.email || 'N/A';
    document.getElementById('profile-role-badge').textContent = currentProfile.role || '';
    document.getElementById('profile-user-id').textContent = `ID: #${currentProfile.id}`;

    // Fill common fields
    const phoneInput = document.getElementById('profile-phone');
    if (phoneInput) phoneInput.value = currentProfile.phone || '';

    if (currentProfile.role === 'JOB_SEEKER') {
      const seekerSection = document.getElementById('seeker-fields');
      if (seekerSection) seekerSection.classList.remove('d-none');

      document.getElementById('profile-firstname').value = currentProfile.firstName || '';
      document.getElementById('profile-lastname').value = currentProfile.lastName || '';
      document.getElementById('profile-dob').value = currentProfile.dateOfBirth || '';
    } else if (currentProfile.role === 'EMPLOYER') {
      const employerSection = document.getElementById('employer-fields');
      if (employerSection) employerSection.classList.remove('d-none');

      document.getElementById('profile-company').value = currentProfile.companyName || '';
      document.getElementById('profile-contact').value = currentProfile.contactName || '';
      document.getElementById('profile-website').value = currentProfile.website || '';
    }
  } catch (err) {
    console.error('Failed to load profile:', err);
    UI.showAlert('profile-alert-placeholder', `Failed to load profile details: ${err.message}`, 'danger');
  }
}

function initProfileForm() {
  const form = document.getElementById('profile-form');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    UI.clearAlert('profile-alert-placeholder');

    const phone = document.getElementById('profile-phone')?.value?.trim() || null;
    const payload = { phone };

    if (currentProfile && currentProfile.role === 'JOB_SEEKER') {
      payload.firstName = document.getElementById('profile-firstname')?.value?.trim() || null;
      payload.lastName = document.getElementById('profile-lastname')?.value?.trim() || null;
      payload.dateOfBirth = document.getElementById('profile-dob')?.value || null;
    } else if (currentProfile && currentProfile.role === 'EMPLOYER') {
      payload.companyName = document.getElementById('profile-company')?.value?.trim() || null;
      payload.contactName = document.getElementById('profile-contact')?.value?.trim() || null;
      payload.website = document.getElementById('profile-website')?.value?.trim() || null;
    }

    const saveBtn = document.getElementById('save-profile-btn');
    saveBtn.disabled = true;
    saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Saving...';

    try {
      const updated = await API.put(CONFIG.ENDPOINTS.USERS.ME, payload);
      currentProfile = updated;
      UI.showAlert('profile-alert-placeholder', 'Profile updated successfully!', 'success');
    } catch (err) {
      console.error('Failed to update profile:', err);
      UI.showAlert('profile-alert-placeholder', `Failed to update profile: ${err.message}`, 'danger');
    } finally {
      saveBtn.disabled = false;
      saveBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i>Save Profile Changes';
    }
  });
}
