// import { Component, OnInit, ChangeDetectorRef }
//   from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { RouterLink, Router } from '@angular/router';
// import { TicketService }
//   from '../../../core/services/ticket.service';
// import { AuthService }
//   from '../../../core/services/auth.service';
// import { ToastService }
//   from '../../../core/services/toast.service';
// import { Ticket } from '../../../core/models/ticket.model';
// import { LoginResponse }
//   from '../../../core/models/auth.model';

// @Component({
//   selector: 'app-user-dashboard',
//   standalone: true,
//   imports: [CommonModule, RouterLink],
//   templateUrl: './user-dashboard.html',
//   styleUrl: './user-dashboard.css'
// })
// export class UserDashboard implements OnInit {

//   currentUser: LoginResponse | null = null;
//   myTickets: Ticket[] = [];
//   isLoading = true;

//   myOpenTickets = 0;
//   myInProgressTickets = 0;
//   myResolvedTickets = 0;
//   myTotalTickets = 0;

//   constructor(
//     private ticketService: TicketService,
//     private authService: AuthService,
//     private toastService: ToastService,
//     private router: Router,
//     private cdr: ChangeDetectorRef
//   ) {}

//   ngOnInit(): void {
//     this.currentUser = this.authService.getCurrentUser();
//     this.loadMyTickets();
//   }

//   loadMyTickets(): void {
//     this.isLoading = true;
//     const userId = this.currentUser?.id;

//     if (!userId) {
//       this.isLoading = false;
//       this.cdr.detectChanges();
//       return;
//     }

//     this.ticketService.getByCreatedBy(userId).subscribe({
//       next: (tickets) => {
//         this.myTickets = tickets;
//         this.calculateStats(tickets);
//         this.isLoading = false;
//         this.cdr.detectChanges();
//       },
//       error: () => {
//         this.toastService.error('Failed to load your tickets');
//         this.isLoading = false;
//         this.cdr.detectChanges();
//       }
//     });
//   }

//   calculateStats(tickets: Ticket[]): void {
//     this.myTotalTickets = tickets.length;
//     this.myOpenTickets = tickets.filter(
//       t => t.status === 'OPEN').length;
//     this.myInProgressTickets = tickets.filter(
//       t => t.status === 'IN_PROGRESS' ||
//            t.status === 'ASSIGNED').length;
//     this.myResolvedTickets = tickets.filter(
//       t => t.status === 'RESOLVED' ||
//            t.status === 'CLOSED').length;
//   }

//   getRecentTickets(): Ticket[] {
//     return this.myTickets.slice(0, 5);
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
// }