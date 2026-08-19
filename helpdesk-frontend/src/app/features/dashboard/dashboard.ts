import {
  Component,
  OnInit,
  ChangeDetectorRef,
  AfterViewInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

import { Ticket } from '../../core/models/ticket.model';
import { LoginResponse } from '../../core/models/auth.model';

import { Chart, registerables } from 'chart.js';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit, AfterViewInit {

  currentUser: LoginResponse | null = null;

  tickets: Ticket[] = [];

  isLoading = true;

  lastUpdated = '';

  private charts: Chart[] = [];

  /* =====================================================
     MAIN STATISTICS
     ===================================================== */

  totalTickets = 0;
  openTickets = 0;
  assignedTickets = 0;
  inProgressTickets = 0;
  pendingTickets = 0;
  resolvedTickets = 0;
  closedTickets = 0;

  /* =====================================================
     PRIORITY
     ===================================================== */

  criticalCount = 0;
  highCount = 0;
  mediumCount = 0;
  lowCount = 0;

  /* =====================================================
     ATTENTION
     ===================================================== */

  unassignedCount = 0;
  needsAttention = 0;

  /* =====================================================
     ROLE INFORMATION
     ===================================================== */

  dashboardTitle = 'Help Desk Dashboard';

  dashboardSubtitle =
    'Here is an overview of your help desk activity.';

  /* =====================================================
     CONSTRUCTOR
     ===================================================== */

  constructor(
    private ticketService: TicketService,
    public authService: AuthService,
    private toastService: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  /* =====================================================
     INIT
     ===================================================== */

  ngOnInit(): void {

    this.currentUser =
      this.authService.getCurrentUser();

    this.setDashboardText();

    this.loadTickets();
  }

  ngAfterViewInit(): void {}

  /* =====================================================
     DASHBOARD TEXT
     ===================================================== */

  setDashboardText(): void {

    if (this.authService.isAdmin()) {

      this.dashboardTitle = 'Help Desk Dashboard';

      this.dashboardSubtitle =
        'Here is an overview of your entire help desk operation.';

      return;
    }

    if (this.authService.isSupervisor()) {

      this.dashboardTitle = 'Support Dashboard';

      this.dashboardSubtitle =
        'Monitor tickets assigned to you and your support activity.';

      return;
    }

    if (this.authService.isSupportOfficer()) {

      this.dashboardTitle = 'My Support Dashboard';

      this.dashboardSubtitle =
        'Manage your assigned tickets and support requests.';

      return;
    }

    this.dashboardTitle = 'My Help Desk';

    this.dashboardSubtitle =
      'Track your support requests and ticket activity.';
  }

  /* =====================================================
     LOAD TICKETS
     ===================================================== */

  loadTickets(): void {

    this.isLoading = true;

    const userId =
      this.currentUser?.id;

    if (!userId) {

      this.toastService.error(
        'Unable to identify the current user'
      );

      this.isLoading = false;

      return;
    }

    /*
     * ADMIN
     * -----
     * Can see every ticket.
     */

    if (this.authService.isAdmin()) {

      this.ticketService.getAll().subscribe({

        next: (tickets) => {

          this.tickets = tickets;

          this.finishLoading();
        },

        error: () => {

          this.toastService.error(
            'Failed to load dashboard data'
          );

          this.isLoading = false;

          this.cdr.detectChanges();
        }

      });

      return;
    }

    /*
     * SUPPORT OFFICER / SUPERVISOR
     *
     * They should see:
     *
     * 1. Tickets they created
     * 2. Tickets assigned to them
     */

    if (
      this.authService.isSupportOfficer() ||
      this.authService.isSupervisor()
    ) {

      this.loadUserTickets(userId);

      return;
    }

    /*
     * EMPLOYEE
     *
     * Employees see tickets they created.
     */

    this.ticketService
      .getByCreatedBy(userId)
      .subscribe({

        next: (tickets) => {

          this.tickets = tickets;

          this.finishLoading();
        },

        error: () => {

          this.toastService.error(
            'Failed to load dashboard data'
          );

          this.isLoading = false;

          this.cdr.detectChanges();
        }

      });
  }

  /* =====================================================
     LOAD USER + ASSIGNED TICKETS
     ===================================================== */

  private loadUserTickets(userId: number): void {

    let createdTickets: Ticket[] = [];
    let assignedTickets: Ticket[] = [];

    let completedRequests = 0;

    const finish = () => {

      completedRequests++;

      if (completedRequests < 2) {
        return;
      }

      /*
       * Remove duplicate tickets.
       */

      const combined = [
        ...createdTickets,
        ...assignedTickets
      ];

      const uniqueTickets =
        combined.filter(
          (ticket, index, self) =>
            index === self.findIndex(
              t => t.id === ticket.id
            )
        );

      this.tickets = uniqueTickets;

      this.finishLoading();
    };

    this.ticketService
      .getByCreatedBy(userId)
      .subscribe({

        next: (tickets) => {

          createdTickets = tickets;

          finish();
        },

        error: () => {

          finish();
        }

      });

    this.ticketService
      .getByAssignedTo(userId)
      .subscribe({

        next: (tickets) => {

          assignedTickets = tickets;

          finish();
        },

        error: () => {

          finish();
        }

      });
  }

  /* =====================================================
     FINISH LOADING
     ===================================================== */

  private finishLoading(): void {

    this.calculateStats();

    this.isLoading = false;

    this.lastUpdated =
      new Date().toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
      });

    this.cdr.detectChanges();

    setTimeout(() => {

      this.destroyCharts();

      this.renderCharts();

    }, 100);
  }

  /* =====================================================
     CALCULATE STATISTICS
     ===================================================== */

  calculateStats(): void {

    this.totalTickets =
      this.tickets.length;

    this.openTickets =
      this.countStatus('OPEN');

    this.assignedTickets =
      this.countStatus('ASSIGNED');

    this.inProgressTickets =
      this.countStatus('IN_PROGRESS');

    this.pendingTickets =
      this.countStatus('PENDING');

    this.resolvedTickets =
      this.countStatus('RESOLVED');

    this.closedTickets =
      this.countStatus('CLOSED');

    /* Priority */

    this.criticalCount =
      this.countPriority('CRITICAL');

    this.highCount =
      this.countPriority('HIGH');

    this.mediumCount =
      this.countPriority('MEDIUM');

    this.lowCount =
      this.countPriority('LOW');

    /* Unassigned */

    this.unassignedCount =
      this.tickets.filter(
        ticket =>
          !ticket.assignedToId
      ).length;

    /*
     * Tickets needing attention:
     *
     * OPEN
     * + ASSIGNED
     * + PENDING
     */

    this.needsAttention =
      this.openTickets +
      this.assignedTickets +
      this.pendingTickets;
  }

  /* =====================================================
     STATUS COUNT
     * ===================================================== */

  countStatus(status: string): number {

    return this.tickets.filter(
      ticket =>
        ticket.status === status
    ).length;
  }

  /* =====================================================
     PRIORITY COUNT
     * ===================================================== */

  countPriority(priority: string): number {

    return this.tickets.filter(
      ticket =>
        ticket.priority === priority
    ).length;
  }

  /* =====================================================
     CHARTS
     * ===================================================== */

  renderCharts(): void {

    this.renderStatusChart();

    this.renderPriorityChart();
  }

  /* =====================================================
     STATUS CHART
     * ===================================================== */

  renderStatusChart(): void {

    const canvas =
      document.getElementById(
        'statusChart'
      ) as HTMLCanvasElement;

    if (!canvas) {
      return;
    }

    const chart =
      new Chart(canvas, {

        type: 'doughnut',

        data: {

          labels: [
            'Open',
            'Assigned',
            'In Progress',
            'Pending',
            'Resolved',
            'Closed'
          ],

          datasets: [{

            data: [
              this.openTickets,
              this.assignedTickets,
              this.inProgressTickets,
              this.pendingTickets,
              this.resolvedTickets,
              this.closedTickets
            ],

            backgroundColor: [
              '#2563eb',
              '#7c3aed',
              '#f59e0b',
              '#f97316',
              '#16a34a',
              '#64748b'
            ],

            borderWidth: 0
          }]
        },

        options: {

          responsive: true,

          maintainAspectRatio: false,

          cutout: '70%',

          plugins: {

            legend: {

              position: 'bottom',

              labels: {

                usePointStyle: true,

                padding: 16
              }
            }
          }
        }
      });

    this.charts.push(chart);
  }

  /* =====================================================
     PRIORITY CHART
     * ===================================================== */

  renderPriorityChart(): void {

    const canvas =
      document.getElementById(
        'priorityChart'
      ) as HTMLCanvasElement;

    if (!canvas) {
      return;
    }

    const chart =
      new Chart(canvas, {

        type: 'bar',

        data: {

          labels: [
            'Critical',
            'High',
            'Medium',
            'Low'
          ],

          datasets: [{

            label: 'Tickets',

            data: [
              this.criticalCount,
              this.highCount,
              this.mediumCount,
              this.lowCount
            ],

            backgroundColor: [
              '#dc2626',
              '#ea580c',
              '#eab308',
              '#16a34a'
            ],

            borderRadius: 6,

            borderSkipped: false
          }]
        },

        options: {

          responsive: true,

          maintainAspectRatio: false,

          plugins: {

            legend: {
              display: false
            }
          },

          scales: {

            y: {

              beginAtZero: true,

              ticks: {
                precision: 0
              }
            },

            x: {

              grid: {
                display: false
              }
            }
          }
        }
      });

    this.charts.push(chart);
  }

  /* =====================================================
     DESTROY CHARTS
     * ===================================================== */

  private destroyCharts(): void {

    this.charts.forEach(
      chart => chart.destroy()
    );

    this.charts = [];
  }

  /* =====================================================
     RECENT TICKETS
     * ===================================================== */

  getRecentTickets(): Ticket[] {

    return [...this.tickets]

      .sort((a, b) => {

        const dateA =
          a.createdAt
            ? new Date(a.createdAt).getTime()
            : 0;

        const dateB =
          b.createdAt
            ? new Date(b.createdAt).getTime()
            : 0;

        return dateB - dateA;

      })

      .slice(0, 6);
  }

  /* =====================================================
     CLICK TICKET
     * ===================================================== */

  goToTicket(
    id: number | undefined
  ): void {

    if (!id) {
      return;
    }

    this.router.navigate([
      '/tickets',
      id
    ]);
  }

  /* =====================================================
     STATUS CLASS
     * ===================================================== */

  getStatusClass(
    status: string | undefined
  ): string {

    return `status-${status?.toLowerCase()}`;
  }

  /* =====================================================
     PRIORITY CLASS
     * ===================================================== */

  getPriorityClass(
    priority: string | undefined
  ): string {

    return `priority-${priority?.toLowerCase()}`;
  }

  /* =====================================================
     TIME AGO
     * ===================================================== */

  timeAgo(
    dateStr: string | undefined
  ): string {

    if (!dateStr) {
      return '';
    }

    const diff =
      Math.floor(
        (
          Date.now() -
          new Date(dateStr).getTime()
        ) / 1000
      );

    if (diff < 60) {
      return 'Just now';
    }

    if (diff < 3600) {

      return (
        Math.floor(diff / 60) +
        ' min ago'
      );
    }

    if (diff < 86400) {

      return (
        Math.floor(diff / 3600) +
        'h ago'
      );
    }

    return (
      Math.floor(diff / 86400) +
      'd ago'
    );
  }

  /* =====================================================
     ROLE HELPERS
     * ===================================================== */

  isAdmin(): boolean {

    return this.authService.isAdmin();
  }

  isSupportOfficer(): boolean {

    return this.authService.isSupportOfficer();
  }

  isSupervisor(): boolean {

    return this.authService.isSupervisor();
  }

  /* =====================================================
     GREETING
     * ===================================================== */

  getGreeting(): string {

    const hour =
      new Date().getHours();

    if (hour < 12) {
      return 'Good morning';
    }

    if (hour < 18) {
      return 'Good afternoon';
    }

    return 'Good evening';
  }

  /* =====================================================
     EXPORT PDF
     * ===================================================== */

  exportToPDF(): void {

    const doc =
      new jsPDF();

    doc.setFontSize(20);

    doc.text(
      'Help Desk Dashboard',
      14,
      20
    );

    doc.setFontSize(10);

    doc.text(
      `Generated: ${new Date().toLocaleString()}`,
      14,
      28
    );

    doc.text(
      `Total Tickets: ${this.totalTickets}`,
      14,
      36
    );

    doc.text(
      `Open: ${this.openTickets}`,
      14,
      42
    );

    doc.text(
      `Assigned: ${this.assignedTickets}`,
      14,
      48
    );

    doc.text(
      `In Progress: ${this.inProgressTickets}`,
      14,
      54
    );

    doc.text(
      `Resolved: ${this.resolvedTickets}`,
      14,
      60
    );

    autoTable(doc, {

      startY: 68,

      head: [[
        'Ticket',
        'Subject',
        'Priority',
        'Status'
      ]],

      body:
        this.getRecentTickets()
          .map(ticket => [

            ticket.ticketNumber || '—',

            ticket.subject || '—',

            ticket.priority || '—',

            ticket.status || '—'

          ]),

      styles: {
        fontSize: 9
      },

      headStyles: {
        fillColor: [
          30,
          58,
          95
        ]
      }
    });

    doc.save(
      `help-desk-dashboard-${
        new Date()
          .toISOString()
          .slice(0, 10)
      }.pdf`
    );

    this.toastService.success(
      'Dashboard report exported successfully'
    );
  }
}