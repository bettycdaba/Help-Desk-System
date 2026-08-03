import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8080/api';

  private currentUserSubject =
    new BehaviorSubject<LoginResponse | null>(null);

  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      this.currentUserSubject.next(JSON.parse(stored));
    }
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${this.baseUrl}/auth/login`, request
    ).pipe(
      tap(response => {
        localStorage.setItem('currentUser',
          JSON.stringify(response));
        localStorage.setItem('token', response.token);
        this.currentUserSubject.next(response);
      })
    );
  }

  register(user: User): Observable<User> {
    return this.http.post<User>(
      `${this.baseUrl}/auth/register`, user);
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): LoginResponse | null {
    return this.currentUserSubject.value;
  }
  getUserRoles(): string[] {
    const user = this.getCurrentUser();
    if (!user) return [];
    return user.roles || [];
  }

  getUserPermissions(): string[] {
    const user = this.getCurrentUser();
    if (!user) return [];
    return user.permissions || [];
  }

  hasPermission(permission: string): boolean {
    const permissions = this.getUserPermissions();
    return permissions.includes(permission);
  }

  hasAnyPermission(permissions: string[]): boolean {
    return permissions.some(p => this.hasPermission(p));
  }

  hasAllPermissions(permissions: string[]): boolean {
    return permissions.every(p => this.hasPermission(p));
  }

  isAdmin(): boolean {
    const roles = this.getUserRoles();
    return roles.includes('ADMIN');
  }

  isSupportOfficer(): boolean {
    const roles = this.getUserRoles();
    return roles.includes('SUPPORT_OFFICER');
  }

  isSupervisor(): boolean {
    const roles = this.getUserRoles();
    return roles.includes('SUPERVISOR');
  }

  isEmployee(): boolean {
    const roles = this.getUserRoles();
    return roles.includes('EMPLOYEE') && !this.isAdmin();
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/auth/forgot-password`, { email });
  }

  resetPassword(data: {
    email: string;
    temporaryPassword: string;
    newPassword: string;
  }): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/auth/reset-password`, data);
  }

  checkMustChangePassword(email: string): Observable<any> {
    return this.http.get(
      `${this.baseUrl}/auth/must-change-password`,
      { params: { email } }
    );
  
  }  
}
