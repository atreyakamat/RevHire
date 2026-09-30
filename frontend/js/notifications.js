/**
 * RevHire Notifications Center Module
 */

let notifPage = 0;
const notifSize = 20;

document.addEventListener('DOMContentLoaded', () => {
  if (!AUTH.requireAuth()) return;

  UI.renderNavbar('notifications');
  UI.renderFooter();

  loadNotifications();
  initNotificationEvents();
});

function initNotificationEvents() {
  document.getElementById('mark-all-read-btn')?.addEventListener('click', handleMarkAllRead);
}

async function loadNotifications() {
  const userId = AUTH.getUserId();
  const container = document.getElementById('notifications-list-container');
  if (!container || !userId) return;

  UI.showLoading('notifications-list-container', 'Loading notifications...');

  try {
    const data = await API.get(CONFIG.ENDPOINTS.NOTIFICATIONS.BY_USER(userId, notifPage, notifSize));
    const items = data && data.content ? data.content : [];

    // Also update unread count header
    try {
      const countRes = await API.get(CONFIG.ENDPOINTS.NOTIFICATIONS.UNREAD_COUNT(userId));
      const countHeader = document.getElementById('unread-count-text');
      if (countHeader) {
        countHeader.textContent = `${countRes.unreadCount || 0} unread`;
      }
    } catch {
      // Ignore count fetch errors
    }

    if (items.length === 0) {
      UI.showEmpty(
        'notifications-list-container',
        'No Notifications',
        'You have no notifications right now. System updates, application alerts, and job activities will appear here.'
      );
      renderNotifPagination(0, 0);
      return;
    }

    let listHtml = '<div class="list-group shadow-sm">';
    items.forEach(n => {
      const isUnread = !n.isRead;
      listHtml += `
        <div class="list-group-item notification-item p-3 ${isUnread ? 'unread' : ''}" id="notif-item-${n.id}">
          <div class="d-flex justify-content-between align-items-start gap-2">
            <div class="d-flex align-items-start gap-3">
              <div class="mt-1">
                <i class="bi bi-${getNotificationIcon(n.type)} fs-4 text-${isUnread ? 'primary' : 'secondary'}"></i>
              </div>
              <div>
                <div class="d-flex align-items-center gap-2 mb-1">
                  <h6 class="fw-bold mb-0 text-dark">${UI.escapeHtml(n.title || 'Notification')}</h6>
                  ${isUnread ? '<span class="badge bg-primary" style="font-size: 0.65rem;">New</span>' : ''}
                </div>
                <p class="text-secondary small mb-1">${UI.escapeHtml(n.message || '')}</p>
                <small class="text-muted"><i class="bi bi-clock me-1"></i>${UI.formatDateTime(n.createdAt)}</small>
              </div>
            </div>

            <div class="d-flex gap-1">
              ${isUnread ? `
                <button class="btn btn-outline-primary btn-sm py-1 px-2" onclick="markNotificationRead(${n.id})" title="Mark as read">
                  <i class="bi bi-check2"></i>
                </button>
              ` : ''}
              <button class="btn btn-outline-danger btn-sm py-1 px-2" onclick="deleteNotification(${n.id})" title="Delete">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
        </div>
      `;
    });
    listHtml += '</div>';

    container.innerHTML = listHtml;
    renderNotifPagination(notifPage, data.totalPages || 1);
  } catch (err) {
    console.error('Failed to load notifications:', err);
    UI.showAlert('notifications-list-container', `Failed to load notifications: ${err.message}`, 'danger');
  }
}

function getNotificationIcon(type) {
  const t = String(type || '').toUpperCase();
  if (t.includes('APPLICATION')) return 'file-earmark-check';
  if (t.includes('JOB')) return 'briefcase';
  if (t.includes('STATUS')) return 'arrow-repeat';
  if (t.includes('USER')) return 'person';
  return 'bell';
}

window.markNotificationRead = async function(id) {
  try {
    await API.put(CONFIG.ENDPOINTS.NOTIFICATIONS.MARK_READ(id));
    const item = document.getElementById(`notif-item-${id}`);
    if (item) {
      item.classList.remove('unread');
      const badge = item.querySelector('.badge.bg-primary');
      if (badge) badge.remove();
      const readBtn = item.querySelector('button[title="Mark as read"]');
      if (readBtn) readBtn.remove();
    }
    const userId = AUTH.getUserId();
    if (userId) UI.updateNotificationBadge(userId);
  } catch (err) {
    console.error('Failed to mark notification read:', err);
  }
};

window.deleteNotification = async function(id) {
  try {
    await API.delete(CONFIG.ENDPOINTS.NOTIFICATIONS.DELETE(id));
    const item = document.getElementById(`notif-item-${id}`);
    if (item) item.remove();
    const userId = AUTH.getUserId();
    if (userId) UI.updateNotificationBadge(userId);
  } catch (err) {
    console.error('Failed to delete notification:', err);
    UI.showAlert('notif-alert-placeholder', `Failed to delete notification: ${err.message}`, 'danger');
  }
};

async function handleMarkAllRead() {
  const userId = AUTH.getUserId();
  if (!userId) return;

  const btn = document.getElementById('mark-all-read-btn');
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Marking...';
  }

  try {
    await API.put(CONFIG.ENDPOINTS.NOTIFICATIONS.MARK_ALL_READ(userId));
    loadNotifications();
    UI.updateNotificationBadge(userId);
    UI.showAlert('notif-alert-placeholder', 'All notifications marked as read.', 'success');
  } catch (err) {
    console.error('Failed to mark all notifications read:', err);
    UI.showAlert('notif-alert-placeholder', `Failed to mark all as read: ${err.message}`, 'danger');
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = '<i class="bi bi-check2-all me-1"></i>Mark All as Read';
    }
  }
}

function renderNotifPagination(current, total) {
  const paginationContainer = document.getElementById('notifications-pagination');
  if (!paginationContainer) return;

  if (total <= 1) {
    paginationContainer.innerHTML = '';
    return;
  }

  let html = '<ul class="pagination justify-content-center mt-4">';
  html += `
    <li class="page-item ${current === 0 ? 'disabled' : ''}">
      <button class="page-link" onclick="changeNotifPage(${current - 1})">&laquo;</button>
    </li>
  `;

  for (let i = 0; i < total; i++) {
    html += `
      <li class="page-item ${i === current ? 'active' : ''}">
        <button class="page-link" onclick="changeNotifPage(${i})">${i + 1}</button>
      </li>
    `;
  }

  html += `
    <li class="page-item ${current >= total - 1 ? 'disabled' : ''}">
      <button class="page-link" onclick="changeNotifPage(${current + 1})">&raquo;</button>
    </li>
  `;
  html += '</ul>';

  paginationContainer.innerHTML = html;
}

window.changeNotifPage = function(newPage) {
  notifPage = newPage;
  loadNotifications();
  window.scrollTo({ top: 0, behavior: 'smooth' });
};
