/**
 * RevHire API Configuration
 * Centralized endpoint URLs, gateway base URL, and storage keys.
 */
const CONFIG = {
  API_BASE_URL: window.REVHIRE_API_URL || 'http://localhost:8080',
  STORAGE_KEYS: {
    TOKEN: 'revhire_token',
    USER: 'revhire_user'
  },
  ENDPOINTS: {
    AUTH: {
      LOGIN: '/api/auth/login',
      REGISTER: '/api/auth/register'
    },
    USERS: {
      ME: '/api/users/me',
      BY_ID: (id) => `/api/users/${id}`
    },
    JOBS: {
      BASE: '/api/jobs',
      BY_ID: (id) => `/api/jobs/${id}`,
      BY_EMPLOYER: (employerId) => `/api/jobs/employer/${employerId}`
    },
    APPLICATIONS: {
      BASE: '/api/applications',
      BY_ID: (id) => `/api/applications/${id}`,
      BY_USER: (userId) => `/api/applications/user/${userId}`,
      BY_JOB: (jobId) => `/api/applications/job/${jobId}`,
      UPDATE_STATUS: (id, status) => `/api/applications/${id}/status?status=${encodeURIComponent(status)}`
    },
    RESUMES: {
      BASE: '/api/resumes',
      BY_ID: (id) => `/api/resumes/${id}`,
      BY_USER: (userId) => `/api/resumes/user/${userId}`
    },
    NOTIFICATIONS: {
      BY_USER: (userId, page = 0, size = 20) => `/api/notifications/user/${userId}?page=${page}&size=${size}`,
      UNREAD_COUNT: (userId) => `/api/notifications/user/${userId}/unread-count`,
      MARK_READ: (id) => `/api/notifications/${id}/read`,
      MARK_ALL_READ: (userId) => `/api/notifications/user/${userId}/read`,
      DELETE: (id) => `/api/notifications/${id}`
    }
  }
};
