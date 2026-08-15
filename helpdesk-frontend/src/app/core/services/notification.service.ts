import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppNotification }
  from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private baseUrl =
    'http://localhost:8080/api/notifications';

  constructor(private http: HttpClient) {}

  getNotifications(
    userId: number): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(
      `${this.baseUrl}/user/${userId}`);
  }

  getUnreadCount(
    userId: number): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(
      `${this.baseUrl}/user/${userId}/count`);
  }

  markAsRead(id: number): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/${id}/read`, {});
  }

  markAllAsRead(userId: number): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/user/${userId}/read-all`, {});
  }

  clearAllNotifications(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/user/${userId}`);
}
}