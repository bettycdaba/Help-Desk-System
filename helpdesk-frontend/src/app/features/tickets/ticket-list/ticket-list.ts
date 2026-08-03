import { Component, OnInit, ChangeDetectorRef } 
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TicketService } 
  from '../../../core/services/ticket.service';
import { CategoryService } 
  from '../../../core/services/category.service';
import { AuthService } 
  from '../../../core/services/auth.service';
import { ToastService } 
  from '../../../core/services/toast.service';
import { Ticket } from '../../../core/models/ticket.model';
import { TicketCategory } 
  from '../../../core/models/category.model';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css'
})
export class TicketList implements OnInit {

  tickets: Ticket[] = [];
  filteredTickets: Ticket[] = [];
  categories: TicketCategory[] = [];
  isLoading = true;

  searchText = '';
  selectedStatus = '';
  selectedPriority = '';
  selectedCategory = '';

  currentPage = 1;
  pageSize = 10;

  statuses = [
    'OPEN', 'ASSIGNED', 'IN_PROGRESS',
    'PENDING', 'RESOLVED', 'CLOSED', 'REOPENED'
  ];
  priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(
    private ticketService: TicketService,
    private categoryService: CategoryService,
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadTickets();
    this.loadCategories();
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  getCurrentUserId(): number {
    const user = this.authService.getCurrentUser();
    return user?.id || 0;
  }

  loadTickets(): void {
    this.isLoading = true;

    if (this.isAdmin()) {
      this.ticketService.getAll().subscribe({
        next: (tickets) => {
          this.tickets = tickets;
          this.applyFilters();
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.toastService.error('Failed to load tickets');
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      const userId = this.getCurrentUserId();
      this.ticketService.getByCreatedBy(userId).subscribe({
        next: (tickets) => {
          this.tickets = tickets;
          this.applyFilters();
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.toastService.error('Failed to load tickets');
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  loadCategories(): void {
    this.categoryService.getAll().subscribe({
      next: (cats) => {
        this.categories = cats;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  // NEW: Delete ticket method
  deleteTicket(ticket: Ticket, event: Event): void {
    event.stopPropagation(); // Prevent row click navigation
    
    if (!ticket.id) return;
    
    const confirmDelete = confirm(
      `Are you sure you want to delete ticket ${ticket.ticketNumber}?\n\n` +
      `Subject: ${ticket.subject}\n\n` +
      `This action cannot be undone.`
    );
    
    if (confirmDelete) {
      this.ticketService.delete(ticket.id).subscribe({
        next: () => {
          this.toastService.success(
            `Ticket ${ticket.ticketNumber} deleted successfully`
          );
          this.loadTickets(); // Reload the list
        },
        error: () => {
          this.toastService.error('Failed to delete ticket');
        }
      });
    }
  }

  applyFilters(): void {
    let result = [...this.tickets];

    if (this.searchText.trim()) {
      const search = this.searchText.toLowerCase();
      result = result.filter(t =>
        t.subject?.toLowerCase().includes(search) ||
        t.ticketNumber?.toLowerCase().includes(search)
      );
    }

    if (this.selectedStatus) {
      result = result.filter(
        t => t.status === this.selectedStatus);
    }

    if (this.selectedPriority) {
      result = result.filter(
        t => t.priority === this.selectedPriority);
    }

    if (this.selectedCategory) {
      result = result.filter(
        t => t.categoryName === this.selectedCategory);
    }

    this.filteredTickets = result;
    this.currentPage = 1;
    this.cdr.detectChanges();
  }

  clearFilters(): void {
    this.searchText = '';
    this.selectedStatus = '';
    this.selectedPriority = '';
    this.selectedCategory = '';
    this.applyFilters();
  }

  get paginatedTickets(): Ticket[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredTickets.slice(
      start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(
      this.filteredTickets.length / this.pageSize);
  }

  get pages(): number[] {
    return Array.from(
      { length: this.totalPages }, (_, i) => i + 1);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.cdr.detectChanges();
    }
  }

  goToTicket(id: number | undefined): void {
    if (id) this.router.navigate(['/tickets', id]);
  }

  getStatusClass(status: string | undefined): string {
    return `status-${status}`;
  }

  getPriorityClass(priority: string | undefined): string {
    return `priority-${priority}`;
  }
}