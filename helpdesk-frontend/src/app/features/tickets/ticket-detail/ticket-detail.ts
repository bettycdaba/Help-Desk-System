import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TicketService } from '../../../core/services/ticket.service';
import {
  Ticket,
  TicketAssignmentHistory,
  TicketComment,
  TicketStatusHistory
} from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ticket-detail.html',
  styleUrl: './ticket-detail.css',
})
export class TicketDetail implements OnInit {
  ticket: Ticket | null = null;
  comments: TicketComment[] = [];
  assignmentHistory: TicketAssignmentHistory[] = [];
  statusHistory: TicketStatusHistory[] = [];

  loading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    const ticketId = Number(this.route.snapshot.paramMap.get('id'));

    if (!ticketId) {
      this.loading = false;
      this.errorMessage = 'Invalid ticket ID.';
      return;
    }

    this.loadTicket(ticketId);
  }

  loadTicket(ticketId: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.ticketService.getById(ticketId).subscribe({
      next: (ticket) => {
        this.ticket = ticket;
        this.loading = false;
        this.loadActivity(ticketId);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Unable to load this ticket.';
      }
    });
  }

  private loadActivity(ticketId: number): void {
    this.ticketService.getComments(ticketId).subscribe({
      next: comments => this.comments = comments ?? []
    });

    this.ticketService.getAssignmentHistory(ticketId).subscribe({
      next: history => this.assignmentHistory = history ?? []
    });

    this.ticketService.getStatusHistory(ticketId).subscribe({
      next: history => this.statusHistory = history ?? []
    });
  }
}