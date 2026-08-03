import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const permissionGuard = (requiredPermission: string) => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.hasPermission(requiredPermission)) {
      return true;
    }

    router.navigate(['/dashboard']);
    return false;
  };
};