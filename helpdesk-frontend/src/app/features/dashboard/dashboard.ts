import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { Chart, ChartConfiguration, registerables } from 'chart.js';

import { Ticket } from '../../core/models/ticket.model';
import { AuthService } from '../../core/services/auth.service';
import { TicketService } from '../../core/services/ticket.service';
import { ToastService } from '../../core/services/toast.service';

Chart.register(...registerables);

interface FAQ {
  question: string;
  answer: string;
  isOpen: boolean;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('statusChart') statusChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('priorityChart') priorityChartRef!: ElementRef<HTMLCanvasElement>;

  private statusChart?: Chart;
  private priorityChart?: Chart;

  tickets: Ticket[] = [];
  currentUser: any = null;
  isLoading = true;
  lastUpdated = '';

  totalTickets = 0;
  openTickets = 0;
  assignedTickets = 0;
  inProgressTickets = 0;
  pendingTickets = 0;
  resolvedTickets = 0;
  closedTickets = 0;
  reopenedTickets = 0;
  unassignedCount = 0;

  criticalCount = 0;
  highCount = 0;
  mediumCount = 0;
  lowCount = 0;

  faqs: FAQ[] = [
    {
      question: 'How do I create a ticket?',
      answer: 'Go to the Create Ticket page, enter the required information, select the appropriate priority and category, then submit the ticket.',
      isOpen: false
    },
    {
      question: 'How can I check the status of my ticket?',
      answer: 'Open the Tickets page to view your tickets and their current status.',
      isOpen: false
    },
    {
      question: 'What does "Pending" mean?',
      answer: 'Pending means the support team is waiting for additional information or another action before continuing with the ticket.',
      isOpen: false
    },
    {
      question: 'What happens when my ticket is resolved?',
      answer: 'Once the support officer resolves your ticket, you can review the resolution and take the appropriate action.',
      isOpen: false
    },
    {
      question: 'Can I reopen a resolved ticket?',
      answer: 'If the issue has not actually been resolved, you can reopen the ticket according to your system workflow.',
      isOpen: false
    },
    {
      question: 'Who should I contact if I need additional help?',
      answer: 'Use the Help Desk system to create a ticket. Your request will be routed to the appropriate support team.',
      isOpen: false
    }
  ];

  constructor(
    private ticketService: TicketService,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadTickets();
  }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void {
    this.destroyCharts();
  }

  isAdmin(): boolean { return this.authService.isAdmin(); }
  isSupervisor(): boolean { return this.authService.isSupervisor(); }
  isSupportOfficer(): boolean { return this.authService.isSupportOfficer(); }
  isEmployee(): boolean { return this.authService.isEmployee(); }

  get dashboardTitle(): string {
    if (this.isAdmin()) return 'Admin Dashboard';
    if (this.isSupervisor()) return 'Supervisor Dashboard';
    if (this.isSupportOfficer()) return 'Support Dashboard';
    return 'My Dashboard';
  }

  get dashboardSubtitle(): string {
    if (this.isAdmin()) return 'Here is an overview of your help desk.';
    if (this.isSupervisor()) return 'Monitor your team and ticket workload.';
    if (this.isSupportOfficer()) return 'Here is your current support workload.';
    return 'Here is an overview of your support requests.';
  }

  getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  }

  loadTickets(): void {
    this.isLoading = true;
    const userId = this.currentUser?.id;

    if (!userId) {
      this.isLoading = false;
      this.toastService.error('Unable to identify the current user.');
      this.cdr.detectChanges();
      return;
    }

    if (this.isAdmin() || this.isSupervisor()) {
      this.ticketService.getAll().subscribe({
        next: tickets => this.setTickets(tickets),
        error: () => this.handleLoadError()
      });
      return;
    }

    if (this.isSupportOfficer()) {
      this.ticketService.getByAssignedTo(userId).subscribe({
        next: assignedTickets => {
          this.ticketService.getByCreatedBy(userId).subscribe({
            next: createdTickets => {
              const combined = [...assignedTickets, ...createdTickets];
              this.setTickets(this.removeDuplicateTickets(combined));
            },
            error: () => this.handleLoadError()
          });
        },
        error: () => this.handleLoadError()
      });
      return;
    }

    this.ticketService.getByCreatedBy(userId).subscribe({
      next: tickets => this.setTickets(tickets),
      error: () => this.handleLoadError()
    });
  }

  private setTickets(tickets: Ticket[]): void {
    this.tickets = tickets ?? [];
    this.calculateStatistics();
    this.isLoading = false;
    this.lastUpdated = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    this.cdr.detectChanges();

    setTimeout(() => {
      this.createCharts();
      this.cdr.detectChanges();
    });
  }

  private handleLoadError(): void {
    this.isLoading = false;
    this.toastService.error('Failed to load dashboard data.');
    this.cdr.detectChanges();
  }

  private removeDuplicateTickets(tickets: Ticket[]): Ticket[] {
    const map = new Map<number, Ticket>();
    tickets.forEach(ticket => {
      if (ticket.id !== undefined) map.set(ticket.id, ticket);
    });
    return Array.from(map.values());
  }

  private calculateStatistics(): void {
    this.totalTickets = this.tickets.length;
    this.openTickets = this.countStatus('OPEN');
    this.assignedTickets = this.countStatus('ASSIGNED');
    this.inProgressTickets = this.countStatus('IN_PROGRESS');
    this.pendingTickets = this.countStatus('PENDING');
    this.resolvedTickets = this.countStatus('RESOLVED');
    this.closedTickets = this.countStatus('CLOSED');
    this.reopenedTickets = this.countStatus('REOPENED');
    this.unassignedCount = this.countStatus('UNASSIGNED');

    this.criticalCount = this.countPriority('CRITICAL');
    this.highCount = this.countPriority('HIGH');
    this.mediumCount = this.countPriority('MEDIUM');
    this.lowCount = this.countPriority('LOW');
  }

  private countStatus(status: string): number {
    return this.tickets.filter(ticket => ticket.status === status).length;
  }

  private countPriority(priority: string): number {
    return this.tickets.filter(ticket => ticket.priority === priority).length;
  }

  get needsAttention(): number {
    return this.unassignedCount + this.pendingTickets + this.reopenedTickets;
  }

  getRecentTickets(): Ticket[] {
    return [...this.tickets]
      .sort((a, b) => new Date(b.createdAt ?? 0).getTime() - new Date(a.createdAt ?? 0).getTime())
      .slice(0, 6);
  }

  goToTicket(id?: number): void {
    if (!id) return;
    this.router.navigate(['/tickets', id]);
  }

  getStatusClass(status?: string): string {
    switch (status) {
      case 'OPEN': return 'status-open';
      case 'ASSIGNED': return 'status-assigned';
      case 'IN_PROGRESS': return 'status-progress';
      case 'PENDING': return 'status-pending';
      case 'RESOLVED': return 'status-resolved';
      case 'CLOSED': return 'status-closed';
      case 'REOPENED': return 'status-reopened';
      case 'UNASSIGNED': return 'status-unassigned';
      default: return 'status-default';
    }
  }

  getPriorityClass(priority?: string): string {
    switch (priority) {
      case 'CRITICAL': return 'priority-critical';
      case 'HIGH': return 'priority-high';
      case 'MEDIUM': return 'priority-medium';
      case 'LOW': return 'priority-low';
      default: return 'priority-default';
    }
  }

  timeAgo(date?: string): string {
    if (!date) return '';
    const seconds = Math.floor((Date.now() - new Date(date).getTime()) / 1000);
    if (seconds < 60) return 'Just now';
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d ago`;
    return new Date(date).toLocaleDateString();
  }

  toggleFaq(index: number): void {
    this.faqs[index].isOpen = !this.faqs[index].isOpen;
    this.cdr.detectChanges();
  }

  private createCharts(): void {
    this.destroyCharts();
    if (!this.statusChartRef?.nativeElement || !this.priorityChartRef?.nativeElement) return;
    this.createStatusChart();
    this.createPriorityChart();
  }

  private createStatusChart(): void {
    const ctx = this.statusChartRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const config: ChartConfiguration<'doughnut'> = {
      type: 'doughnut',
      data: {
        labels: ['Open', 'Assigned', 'In Progress', 'Pending', 'Resolved', 'Closed', 'Reopened'],
        datasets: [{
          data: [this.openTickets, this.assignedTickets, this.inProgressTickets, this.pendingTickets, this.resolvedTickets, this.closedTickets, this.reopenedTickets],
          backgroundColor: ['#f59e0b', '#8b5cf6', '#06b6d4', '#f97316', '#22c55e', '#64748b', '#ef4444'],
          borderWidth: 0,
          hoverOffset: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '68%',
        plugins: {
          legend: { position: 'bottom', labels: { usePointStyle: true, pointStyle: 'circle', padding: 14 } }
        }
      }
    };

    this.statusChart = new Chart(ctx, config);
  }

  private createPriorityChart(): void {
    const ctx = this.priorityChartRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const config: ChartConfiguration<'bar'> = {
      type: 'bar',
      data: {
        labels: ['Critical', 'High', 'Medium', 'Low'],
        datasets: [{
          label: 'Tickets',
          data: [this.criticalCount, this.highCount, this.mediumCount, this.lowCount],
          backgroundColor: ['#dc2626', '#f97316', '#eab308', '#22c55e'],
          borderRadius: 8,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: { grid: { display: false } },
          y: { beginAtZero: true, ticks: { precision: 0 }, grid: { color: 'rgba(148, 163, 184, 0.15)' } }
        }
      }
    };

    this.priorityChart = new Chart(ctx, config);
  }

  private destroyCharts(): void {
    if (this.statusChart) { this.statusChart.destroy(); this.statusChart = undefined; }
    if (this.priorityChart) { this.priorityChart.destroy(); this.priorityChart = undefined; }
  }

  exportToPDF(): void {
    window.print();
  }
}