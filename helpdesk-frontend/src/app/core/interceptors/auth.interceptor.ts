import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn =
  (req, next) => {

  const authService = inject(AuthService);
  const token = authService.getToken();
  // Do not attach Authorization header for authentication endpoints
  const rawUrl = req.url || '';
  let path = rawUrl;
  try {
    const parsed = new URL(rawUrl);
    path = parsed.pathname;
  } catch {
    // not an absolute URL, use as-is
    path = rawUrl;
  }

  const isPublicAuthEndpoint = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/auth/forgot-password',
    '/api/auth/reset-password',
    '/api/auth/must-change-password'
  ].some(endpoint => path.startsWith(endpoint));

  if (token && !isPublicAuthEndpoint) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(cloned);
  }

  return next(req);
};
