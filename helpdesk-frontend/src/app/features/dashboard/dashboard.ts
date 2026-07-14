import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TicketService } from '../../core/services/ticket.service';
import { Ticket } from '../../core/models/ticket.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
})
export class Dashboard implements OnInit {
  tickets: Ticket[] = [];
  recent: Ticket[] = [];
  loading = false;
  error: string | null = null;

  total = 0;
  open = 0;
  resolved = 0;
  closed = 0;

  private openStatuses = new Set([
    'OPEN', 'ASSIGNED', 'IN_PROGRESS', 'PENDING', 'REOPENED'
  ]);

  constructor(private ticketService: TicketService) {}

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.loading = true;
    this.error = null;
    this.ticketService.getAll().subscribe({
      next: (tickets) => {
        this.tickets = tickets || [];
        this.computeStats();
        this.loading = false;
      },
      error: (err) => {
        this.error = (err && err.message) ? err.message : 'Failed to load tickets';
        this.loading = false;
      }
    });
  }

  private computeStats(): void {
    this.total = this.tickets.length;
    this.open = this.tickets.filter(t => this.openStatuses.has((t.status || '').toUpperCase())).length;
    this.resolved = this.tickets.filter(t => (t.status || '').toUpperCase() === 'RESOLVED').length;
    this.closed = this.tickets.filter(t => (t.status || '').toUpperCase() === 'CLOSED').length;

    this.recent = [...this.tickets]
      .sort((a, b) => {
        const at = a.createdAt ? Date.parse(a.createdAt) : 0;
        const bt = b.createdAt ? Date.parse(b.createdAt) : 0;
        return bt - at;
      })
      .slice(0, 5);
  }
}
