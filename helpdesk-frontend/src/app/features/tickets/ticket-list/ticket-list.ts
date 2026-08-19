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
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

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

  sortColumn = 'createdAt';
  sortDirection: 'asc' | 'desc' = 'desc';

  ticketView: 'all' | 'assigned' | 'unassigned' = 'all';

  statuses = [
    'OPEN', 'ASSIGNED', 'IN_PROGRESS',
    'PENDING', 'RESOLVED', 'CLOSED', 'REOPENED', 'UNASSIGNED'
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
  isSupervisor(): boolean {
  return this.authService.isSupervisor();
}

  getCurrentUserId(): number {
    const user = this.authService.getCurrentUser();
    return user?.id || 0;
  }

  canDeleteTicket(ticket: Ticket): boolean {
    if (this.isAdmin()) return true;
    const userId = this.getCurrentUserId();
    return ticket.createdById === userId && ticket.assignedToId !== userId;
  }


  showExportModal = false;
exportFileName = '';

openExportModal(): void {
    this.exportFileName = `tickets-report-${new Date().toISOString().slice(0, 10)}`;
    this.showExportModal = true;
    this.cdr.detectChanges();
}

closeExportModal(): void {
    this.showExportModal = false;
    this.cdr.detectChanges();
}

exportToPDF(): void {
    const data = this.displayTickets;
    
    if (data.length === 0) {
      this.toastService.error('No tickets to export');
      return;
    }

    // Ask user for filename
    const defaultName = `tickets-report-${new Date().toISOString().slice(0, 10)}`;
    const customName = prompt('Enter filename for PDF export:', defaultName);
    
    if (customName === null) return; // User cancelled
    
    const finalName = customName.trim() || defaultName;
    const fileName = finalName.endsWith('.pdf') ? finalName : finalName + '.pdf';

    const doc = new jsPDF();

    doc.setFontSize(18);
    doc.setTextColor(30, 58, 95);
    doc.text('Help Desk - Ticket Report', 14, 20);

    doc.setFontSize(11);
    doc.setTextColor(100, 100, 100);
    doc.text(
      `Generated: ${new Date().toLocaleString()}  |  Total: ${data.length} tickets`,
      14, 28
    );

    autoTable(doc, {
      startY: 35,
      head: [['Ticket #', 'Subject', 'Category', 'Priority', 'Status', 'Assigned To', 'Created By']],
      body: data.map(t => [
        t.ticketNumber || '—',
        t.subject || '—',
        t.categoryName || '—',
        t.priority || '—',
        t.status || '—',
        t.assignedToName || 'Unassigned',
        t.createdByName || '—'
      ]),
      styles: { fontSize: 9, cellPadding: 3 },
      headStyles: { fillColor: [30, 58, 95], textColor: [255, 255, 255], fontStyle: 'bold' },
      alternateRowStyles: { fillColor: [240, 247, 255] }
    });

    doc.save(fileName);
    this.toastService.success(`PDF exported as "${fileName}"`);
}

  loadTickets(): void {
    this.isLoading = true;
    const userId = this.getCurrentUserId();

    if (this.isAdmin() || this.isSupervisor()) {
      this.ticketService.getAll().subscribe({
        next: (tickets) => {
          this.tickets = tickets;
          this.applyFiltersAndSort();
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
      this.loadUserTickets(userId);
    }
  }

  loadUserTickets(userId: number): void {
    let createdTickets: Ticket[] = [];
    let assignedTickets: Ticket[] = [];
    let completed = 0;

    const checkDone = () => {
      completed++;
      if (completed === 2) {
        const allIds = new Set<number>();
        const merged: Ticket[] = [];
        
        [...createdTickets, ...assignedTickets].forEach(t => {
          if (t.id && !allIds.has(t.id)) {
            allIds.add(t.id);
            merged.push(t);
          }
        });
        
        this.tickets = merged;
        this.applyFiltersAndSort();
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    };

    this.ticketService.getByCreatedBy(userId).subscribe({
      next: (tickets) => { createdTickets = tickets; checkDone(); },
      error: () => { checkDone(); }
    });

    this.ticketService.getByAssignedTo(userId).subscribe({
      next: (tickets) => { assignedTickets = tickets; checkDone(); },
      error: () => { checkDone(); }
    });
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

get displayTickets(): Ticket[] {
  const userId = this.getCurrentUserId();
  
  if (this.isAdmin() || this.isSupervisor()) {
    switch (this.ticketView) {
      case 'assigned':
        return this.filteredTickets.filter(
          t => t.assignedToId === userId
        );

      case 'unassigned':
        return this.filteredTickets.filter(
          t => !t.assignedToId
        );

      default:
        return this.filteredTickets;
    }
  } else {
    switch (this.ticketView) {
      case 'assigned':
        return this.filteredTickets.filter(
          t => t.assignedToId === userId
        );

      case 'unassigned':
        return this.filteredTickets.filter(
          t => !t.assignedToId
        );

      default:
        return this.filteredTickets.filter(
          t => t.createdById === userId ||
               t.assignedToId === userId
        );
    }
  }
}

  sortBy(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.applyFiltersAndSort();
  }

  getSortIcon(column: string): string {
    if (this.sortColumn !== column) return 'bi-arrow-down-up text-muted';
    return this.sortDirection === 'asc' ? 'bi-sort-up' : 'bi-sort-down';
  }









  deleteTicket(ticket: Ticket, event: Event): void {
    event.stopPropagation();
    if (!ticket.id) return;
    const confirmDelete = confirm(
      `Are you sure you want to delete ticket ${ticket.ticketNumber}?\n\n` +
      `Subject: ${ticket.subject}\n\nThis action cannot be undone.`
    );
    if (confirmDelete) {
      this.ticketService.delete(ticket.id).subscribe({
        next: () => {
          this.toastService.success(`Ticket ${ticket.ticketNumber} deleted`);
          this.loadTickets();
        },
        error: () => this.toastService.error('Failed to delete ticket')
      });
    }
  }

  applyFilters(): void {
    this.applyFiltersAndSort();
  }

  applyFiltersAndSort(): void {
    let result = [...this.tickets];

    if (this.searchText.trim()) {
      const search = this.searchText.toLowerCase();
      result = result.filter(t =>
        t.subject?.toLowerCase().includes(search) ||
        t.ticketNumber?.toLowerCase().includes(search)
      );
    }
    if (this.selectedStatus) {
      result = result.filter(t => t.status === this.selectedStatus);
    }
    if (this.selectedPriority) {
      result = result.filter(t => t.priority === this.selectedPriority);
    }
    if (this.selectedCategory) {
      result = result.filter(t => t.categoryName === this.selectedCategory);
    }

    result.sort((a, b) => {
      let valA: any, valB: any;
      switch (this.sortColumn) {
        case 'ticketNumber':
          valA = a.ticketNumber || ''; valB = b.ticketNumber || ''; break;
        case 'subject':
          valA = (a.subject || '').toLowerCase(); valB = (b.subject || '').toLowerCase(); break;
        case 'categoryName':
          valA = (a.categoryName || '').toLowerCase(); valB = (b.categoryName || '').toLowerCase(); break;
        case 'priority':
          const order: any = { 'CRITICAL': 4, 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 };
          valA = order[a.priority || 'MEDIUM'] || 0; valB = order[b.priority || 'MEDIUM'] || 0; break;
        case 'status':
          valA = (a.status || '').toLowerCase(); valB = (b.status || '').toLowerCase(); break;
        case 'assignedToName':
          valA = (a.assignedToName || '').toLowerCase(); valB = (b.assignedToName || '').toLowerCase(); break;
        case 'createdByName':
          valA = (a.createdByName || '').toLowerCase(); valB = (b.createdByName || '').toLowerCase(); break;
        case 'createdAt':
        default:
          valA = new Date(a.createdAt || '').getTime(); valB = new Date(b.createdAt || '').getTime(); break;
      }
      if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredTickets = result;
    this.currentPage = 1;
    this.cdr.detectChanges();
  }

  clearFilters(): void {
    this.searchText = '';
    this.selectedStatus = '';
    this.selectedPriority = '';
    this.selectedCategory = '';
    this.sortColumn = 'createdAt';
    this.sortDirection = 'desc';
    this.ticketView = 'all';
    this.applyFiltersAndSort();
  }

  get paginatedTickets(): Ticket[] {
    const display = this.displayTickets;
    const start = (this.currentPage - 1) * this.pageSize;
    return display.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.displayTickets.length / this.pageSize);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
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