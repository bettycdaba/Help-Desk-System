import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Ticket,
  TicketComment,
  TicketAssignmentHistory,
  TicketStatusHistory,
  TicketAssignRequest,
  TicketStatusUpdateRequest
} from '../models/ticket.model';

@Injectable({
  providedIn: 'root'
})
export class TicketService {

  private baseUrl = 'http://localhost:8080/api/tickets';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(this.baseUrl);
  }

  getById(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.baseUrl}/${id}`);
  }

  getByNumber(ticketNumber: string): Observable<Ticket> {
    return this.http.get<Ticket>(
      `${this.baseUrl}/number/${ticketNumber}`);
  }

  getByStatus(status: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(
      `${this.baseUrl}/status/${status}`);
  }

  getByPriority(priority: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(
      `${this.baseUrl}/priority/${priority}`);
  }

  getByCreatedBy(userId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(
      `${this.baseUrl}/created-by/${userId}`);
  }

  getByAssignedTo(userId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(
      `${this.baseUrl}/assigned-to/${userId}`);
  }

  create(ticket: Ticket): Observable<Ticket> {
    return this.http.post<Ticket>(this.baseUrl, ticket);
  }

  update(id: number, ticket: Ticket): Observable<Ticket> {
    return this.http.put<Ticket>(
      `${this.baseUrl}/${id}`, ticket);
  }

  assign(id: number,
    request: TicketAssignRequest): Observable<Ticket> {
    return this.http.patch<Ticket>(
      `${this.baseUrl}/${id}/assign`, request);
  }

  updateStatus(id: number,
    request: TicketStatusUpdateRequest): Observable<Ticket> {
    return this.http.patch<Ticket>(
      `${this.baseUrl}/${id}/status`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getComments(ticketId: number): Observable<TicketComment[]> {
    return this.http.get<TicketComment[]>(
      `${this.baseUrl}/${ticketId}/comments`);
  }

  addComment(ticketId: number,
    comment: TicketComment): Observable<TicketComment> {
    return this.http.post<TicketComment>(
      `${this.baseUrl}/${ticketId}/comments`, comment);
  }

  deleteComment(ticketId: number,
    commentId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${ticketId}/comments/${commentId}`);
  }

  getAssignmentHistory(
    ticketId: number): Observable<TicketAssignmentHistory[]> {
    return this.http.get<TicketAssignmentHistory[]>(
      `${this.baseUrl}/${ticketId}/history/assignments`);
  }

  getStatusHistory(
    ticketId: number): Observable<TicketStatusHistory[]> {
    return this.http.get<TicketStatusHistory[]>(
      `${this.baseUrl}/${ticketId}/history/status`);
  }

  getAttachments(ticketId: number): Observable<any[]> {
  return this.http.get<any[]>(
    `${this.baseUrl}/${ticketId}/attachments`);
}

uploadAttachment(
  ticketId: number,
  file: File,
  uploadedById: number): Observable<any> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('uploadedById', 
    uploadedById.toString());
  return this.http.post<any>(
    `${this.baseUrl}/${ticketId}/attachments`,
    formData);
}

downloadAttachment(
  ticketId: number,
  attachmentId: number): Observable<Blob> {
  return this.http.get(
    `${this.baseUrl}/${ticketId}/attachments/${attachmentId}/download`,
    { responseType: 'blob' });
}

deleteAttachment(
  ticketId: number,
  attachmentId: number): Observable<void> {
  return this.http.delete<void>(
    `${this.baseUrl}/${ticketId}/attachments/${attachmentId}`);
}
}

