import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, map, of } from 'rxjs';

export const permissionGuard = (requiredPermission: string) => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.refreshCurrentUser().pipe(
      map(() => {
        if (authService.hasPermission(requiredPermission)) {
          return true;
        }
        router.navigate(['/dashboard']);
        return false;
      }),
      catchError(() => {
        authService.logout();
        router.navigate(['/login']);
        return of(false);
      })
    );
  };
};
