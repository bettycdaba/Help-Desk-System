import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const supervisorAdminGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  if (authService.isAdmin() || authService.isSupervisor()) {
    return true;
  }

  toastService.error(
    'Access denied. Supervisor or Admin privileges required.');
  router.navigate(['/dashboard']);
  return false;
};
