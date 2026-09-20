import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Department } from '../models/department.model';

@Injectable({
  providedIn: 'root'
})
export class DepartmentService {

  private baseUrl = 'http://localhost:8081/api/departments';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Department[]> {
    return this.http.get<Department[]>(this.baseUrl);
  }

  getById(id: number): Observable<Department> {
    return this.http.get<Department>(`${this.baseUrl}/${id}`);
  }

  create(department: Department): Observable<Department> {
    return this.http.post<Department>(this.baseUrl, department);
  }

  update(id: number,
    department: Department): Observable<Department> {
    return this.http.put<Department>(
      `${this.baseUrl}/${id}`, department);
  }

  updateStatus(id: number, active: boolean): Observable<Department> {
    return this.http.patch<Department>(
      `${this.baseUrl}/${id}/status`, null, { params: { active } });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}