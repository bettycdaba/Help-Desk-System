import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TicketService } from '../../../core/services/ticket.service';
import { Ticket } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css',
})
export class TicketList implements OnInit {
  tickets: Ticket[] = [];
  searchTerm = '';
  selectedStatus = '';
  selectedPriority = '';
  loading = false;
  errorMessage = '';

  readonly statuses = [
    'OPEN', 'ASSIGNED', 'IN_PROGRESS', 'PENDING',
    'RESOLVED', 'CLOSED', 'REOPENED'
  ];

  readonly priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(private ticketService: TicketService) {}

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.loading = true;
    this.errorMessage = '';

    this.ticketService.getAll().subscribe({
      next: (tickets) => {
        this.tickets = tickets ?? [];
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load tickets. Please try again.';
        this.loading = false;
      }
    });
  }

  get filteredTickets(): Ticket[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.tickets.filter(ticket => {
      const matchesSearch =
        !search ||
        ticket.ticketNumber?.toLowerCase().includes(search) ||
        ticket.subject.toLowerCase().includes(search) ||
        ticket.categoryName?.toLowerCase().includes(search) ||
        ticket.createdByName?.toLowerCase().includes(search) ||
        ticket.assignedToName?.toLowerCase().includes(search);

      const matchesStatus =
        !this.selectedStatus || ticket.status === this.selectedStatus;

      const matchesPriority =
        !this.selectedPriority || ticket.priority === this.selectedPriority;

      return matchesSearch && matchesStatus && matchesPriority;
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.selectedPriority = '';
  }
}