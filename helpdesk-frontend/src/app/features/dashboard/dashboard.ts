// import { Component, OnInit, ChangeDetectorRef } 
//   from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { Router, RouterLink } from '@angular/router';
// import { TicketService } 
//   from '../../core/services/ticket.service';
// import { AuthService } 
//   from '../../core/services/auth.service';
// import { ToastService } 
//   from '../../core/services/toast.service';
// import { Ticket } from '../../core/models/ticket.model';
// import { LoginResponse } 
//   from '../../core/models/auth.model';

// @Component({
//   selector: 'app-dashboard',
//   standalone: true,
//   imports: [CommonModule, RouterLink],
//   templateUrl: './dashboard.html',
//   styleUrl: './dashboard.css'
// })
// export class Dashboard implements OnInit {

//   currentUser: LoginResponse | null = null;
//   tickets: Ticket[] = [];
//   isLoading = true;

//   totalTickets = 0;
//   openTickets = 0;
//   inProgressTickets = 0;
//   resolvedTickets = 0;

//   constructor(
//     private ticketService: TicketService,
//     private authService: AuthService,
//     private toastService: ToastService,
//     private router: Router,
//     private cdr: ChangeDetectorRef
//   ) {}

//   ngOnInit(): void {
//     this.currentUser = this.authService.getCurrentUser();
//     this.loadTickets();
//   }

//   loadTickets(): void {
//     this.isLoading = true;
//     this.ticketService.getAll().subscribe({
//       next: (tickets) => {
//         this.tickets = tickets;
//         this.calculateStats(tickets);
//         this.isLoading = false;
//         this.cdr.detectChanges();
//       },
//       error: () => {
//         this.toastService.error('Failed to load tickets');
//         this.isLoading = false;
//         this.cdr.detectChanges();
//       }
//     });
//   }

//   calculateStats(tickets: Ticket[]): void {
//     this.totalTickets = tickets.length;
//     this.openTickets = tickets.filter(
//       t => t.status === 'OPEN').length;
//     this.inProgressTickets = tickets.filter(
//       t => t.status === 'IN_PROGRESS').length;
//     this.resolvedTickets = tickets.filter(
//       t => t.status === 'RESOLVED').length;
//   }

//   getRecentTickets(): Ticket[] {
//     return this.tickets.slice(0, 5);
//   }

//   goToTicket(id: number | undefined): void {
//     if (id) this.router.navigate(['/tickets', id]);
//   }

//   getStatusClass(status: string | undefined): string {
//     return `status-${status}`;
//   }

//   getPriorityClass(priority: string | undefined): string {
//     return `priority-${priority}`;
//   }
//   isAdmin(): boolean {
//     return this.authService.isAdmin();
//   }

//   isEmployee(): boolean {
//     const roles = this.authService.getUserRoles();
//     return roles.includes('EMPLOYEE') && !this.isAdmin();
//   }
// }


import { Component, OnInit, ChangeDetectorRef }
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TicketService }
  from '../../core/services/ticket.service';
import { AuthService }
  from '../../core/services/auth.service';
import { ToastService }
  from '../../core/services/toast.service';
import { Ticket } from '../../core/models/ticket.model';
import { LoginResponse }
  from '../../core/models/auth.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  currentUser: LoginResponse | null = null;
  tickets: Ticket[] = [];
  isLoading = true;

  totalTickets = 0;
  openTickets = 0;
  inProgressTickets = 0;
  resolvedTickets = 0;

  constructor(
    private ticketService: TicketService,
    public authService: AuthService,
    private toastService: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadTickets();
  }

  loadTickets(): void {
    this.isLoading = true;

    if (this.authService.isAdmin()) {
      this.ticketService.getAll().subscribe({
        next: (tickets) => {
          this.tickets = tickets;
          this.calculateStats(tickets);
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
      const userId = this.currentUser?.id || 0;
      this.ticketService.getByCreatedBy(userId).subscribe({
        next: (tickets) => {
          this.tickets = tickets;
          this.calculateStats(tickets);
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

  calculateStats(tickets: Ticket[]): void {
    this.totalTickets = tickets.length;
    this.openTickets = tickets.filter(
      t => t.status === 'OPEN').length;
    this.inProgressTickets = tickets.filter(
      t => t.status === 'IN_PROGRESS').length;
    this.resolvedTickets = tickets.filter(
      t => t.status === 'RESOLVED').length;
  }

  getRecentTickets(): Ticket[] {
    return this.tickets.slice(0, 5);
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

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }
}