import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Permission, RolePermissions } from '../models/permission.model';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {

  private baseUrl = 'http://localhost:8080/api/permissions';

  constructor(private http: HttpClient) {}

  getAllPermissions(): Observable<Permission[]> {
    return this.http.get<Permission[]>(this.baseUrl);
  }

  getRolePermissions(roleId: number): Observable<RolePermissions> {
    return this.http.get<RolePermissions>(`${this.baseUrl}/role/${roleId}`);
  }

  updateRolePermissions(roleId: number, permissionIds: number[]): Observable<RolePermissions> {
    return this.http.put<RolePermissions>(
      `${this.baseUrl}/role/${roleId}`, permissionIds
    );
  }
}