import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn =
  (req, next) => {

  const authService = inject(AuthService);
  const token = authService.getToken();
  // Do not attach Authorization header for authentication endpoints
  const url = req.url || '';
  const isAuthEndpoint = url.includes('/api/auth/');

  if (token && !isAuthEndpoint) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(cloned);
  }

  return next(req);
};