import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

interface Workload {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  assignedCount: number;
  inProgressCount: number;
  pendingCount: number;
  resolvedCount: number;
  totalCount: number;
}

@Component({
  selector: 'app-team-workload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './team-workload.html',
  styleUrl: './team-workload.css'
})
export class TeamWorkload implements OnInit {

  workload: Workload[] = [];
  isLoading = true;

  constructor(
    private ticketService: TicketService,
    private authService: AuthService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadWorkload();
  }

  loadWorkload(): void {
    this.isLoading = true;
    this.ticketService.getTeamWorkload().subscribe({
      next: (data) => {
        this.workload = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error('Failed to load team workload');
        this.isLoading = false;
      }
    });
  }

  isSupervisor(): boolean {
    return this.authService.isSupervisor();
  }
}