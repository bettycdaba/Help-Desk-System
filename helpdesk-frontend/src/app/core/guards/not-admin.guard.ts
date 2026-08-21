import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const notAdminGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  if (authService.isAdmin()) {
    toastService.error('Administrators cannot create tickets.');
    router.navigate(['/tickets']);
    return false;
  }

  return true;
};