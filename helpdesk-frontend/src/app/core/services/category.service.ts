import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TicketCategory } from '../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private baseUrl = 'http://localhost:8081/api/categories';

  constructor(private http: HttpClient) {}

  getAll(): Observable<TicketCategory[]> {
    return this.http.get<TicketCategory[]>(this.baseUrl);
  }

  getById(id: number): Observable<TicketCategory> {
    return this.http.get<TicketCategory>(
      `${this.baseUrl}/${id}`);
  }

  create(category: TicketCategory): Observable<TicketCategory> {
    return this.http.post<TicketCategory>(
      this.baseUrl, category);
  }

  update(id: number,
    category: TicketCategory): Observable<TicketCategory> {
    return this.http.put<TicketCategory>(
      `${this.baseUrl}/${id}`, category);
  }

  updateStatus(id: number, active: boolean): Observable<TicketCategory> {
    return this.http.patch<TicketCategory>(
      `${this.baseUrl}/${id}/status`, null, { params: { active } });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}