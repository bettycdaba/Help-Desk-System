import { Component, OnInit, ChangeDetectorRef, OnDestroy } 
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } 
  from '@angular/router';
import { TicketService } 
  from '../../../core/services/ticket.service';
import { UserService } 
  from '../../../core/services/user.service';
import { ToastService } 
  from '../../../core/services/toast.service';
import {
  Ticket, TicketComment,
  TicketAssignmentHistory, TicketStatusHistory
} from '../../../core/models/ticket.model';
import { User } from '../../../core/models/user.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ticket-detail.html',
  styleUrl: './ticket-detail.css'
})
export class TicketDetail implements OnInit, OnDestroy {

  ticket: Ticket | null = null;
  comments: TicketComment[] = [];
  assignmentHistory: TicketAssignmentHistory[] = [];
  statusHistory: TicketStatusHistory[] = [];
  users: User[] = [];

  isLoading = true;
  newComment = '';
  isSubmittingComment = false;

  selectedAssignee: number | null = null;
  selectedStatus = '';
  isAssigning = false;
  isUpdatingStatus = false;

  activeTab = 'comments';
  ticketId = 0;

  private subscriptions: Subscription[] = [];

  statuses = [
    'OPEN','ASSIGNED','IN_PROGRESS',
    'PENDING','RESOLVED','CLOSED','REOPENED'
  ];

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private userService: UserService,
    private toastService: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('🟢 TicketDetail ngOnInit called');
    
    const sub = this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      console.log('📌 Route param id:', id);
      
      if (id && id > 0) {
        this.ticketId = id;
        this.isLoading = true;
        console.log('✅ Loading ticket with ID:', this.ticketId);
        this.loadTicket(this.ticketId);
        this.loadUsers();
        this.loadComments(this.ticketId);
        this.loadHistory(this.ticketId);
      } else {
        console.error('❌ Invalid ticket ID:', id);
        this.toastService.error('Invalid ticket ID');
        this.router.navigate(['/tickets']);
      }
    });
    
    this.subscriptions.push(sub);
  }

  ngOnDestroy(): void {
    // Clean up subscriptions
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  loadTicket(id: number): void {
    console.log('🔄 Loading ticket:', id);
    
    this.ticketService.getById(id).subscribe({
      next: (ticket) => {
        console.log('✅ Ticket loaded:', ticket);
        this.ticket = ticket;
        this.selectedStatus = ticket.status || '';
        this.selectedAssignee = ticket.assignedToId || null;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Failed to load ticket:', err);
        this.toastService.error('Ticket not found or failed to load');
        this.isLoading = false;
        this.cdr.detectChanges();
        // Don't navigate away immediately, let user see the error
        setTimeout(() => {
          this.router.navigate(['/tickets']);
        }, 2000);
      }
    });
  }

  loadUsers(): void {
    this.userService.getAll().subscribe({
      next: (users) => {
        console.log('👥 Users loaded:', users?.length);
        this.users = users;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Failed to load users:', err);
      }
    });
  }

  loadComments(id: number): void {
    console.log('💬 Loading comments for ticket:', id);
    this.ticketService.getComments(id).subscribe({
      next: (comments) => {
        console.log('✅ Comments loaded:', comments?.length);
        this.comments = comments;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Failed to load comments:', err);
      }
    });
  }

  loadHistory(id: number): void {
    console.log('📜 Loading history for ticket:', id);
    
    this.ticketService.getAssignmentHistory(id).subscribe({
      next: (h) => {
        console.log('✅ Assignment history loaded:', h?.length);
        this.assignmentHistory = h;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Failed to load assignment history:', err);
      }
    });
    
    this.ticketService.getStatusHistory(id).subscribe({
      next: (h) => {
        console.log('✅ Status history loaded:', h?.length);
        this.statusHistory = h;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Failed to load status history:', err);
      }
    });
  }

  getCurrentUserId(): number {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      try {
        const user = JSON.parse(stored);
        return user.id || 1;
      } catch (e) {
        console.error('Error parsing user from localStorage:', e);
      }
    }
    return 1;
  }

  addComment(): void {
    if (!this.newComment.trim()) {
      this.toastService.error('Comment cannot be empty');
      return;
    }

    if (!this.ticket?.id) {
      this.toastService.error('No ticket selected');
      return;
    }

    this.isSubmittingComment = true;
    const comment: TicketComment = {
      comment: this.newComment,
      userId: this.getCurrentUserId()
    };

    this.ticketService.addComment(this.ticket.id, comment).subscribe({
      next: (c) => {
        this.comments = [...this.comments, c];
        this.newComment = '';
        this.isSubmittingComment = false;
        this.toastService.success('Comment added');
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSubmittingComment = false;
        this.toastService.error('Failed to add comment');
        this.cdr.detectChanges();
      }
    });
  }

  assignTicket(): void {
    if (!this.selectedAssignee) {
      this.toastService.error('Please select a user');
      return;
    }

    if (!this.ticket?.id) {
      this.toastService.error('No ticket selected');
      return;
    }

    this.isAssigning = true;
    this.ticketService.assign(this.ticket.id, {
      newAssigneeId: this.selectedAssignee,
      assignedById: this.getCurrentUserId()
    }).subscribe({
      next: (updated) => {
        this.ticket = updated;
        this.isAssigning = false;
        this.toastService.success('Ticket assigned successfully');
        this.loadHistory(this.ticket!.id!);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Assign error:', err);
        this.isAssigning = false;
        this.toastService.error('Failed to assign ticket');
        this.cdr.detectChanges();
      }
    });
  }

  updateStatus(): void {
    if (!this.selectedStatus) {
      this.toastService.error('Please select a status');
      return;
    }

    if (!this.ticket?.id) {
      this.toastService.error('No ticket selected');
      return;
    }

    this.isUpdatingStatus = true;
    this.ticketService.updateStatus(this.ticket.id, {
      newStatus: this.selectedStatus,
      changedById: this.getCurrentUserId()
    }).subscribe({
      next: (updated) => {
        this.ticket = updated;
        this.isUpdatingStatus = false;
        this.toastService.success('Status updated successfully');
        this.loadHistory(this.ticket!.id!);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Update status error:', err);
        this.isUpdatingStatus = false;
        this.toastService.error('Failed to update status');
        this.cdr.detectChanges();
      }
    });
  }

  deleteComment(commentId: number | undefined): void {
    if (!commentId) return;
    if (!confirm('Delete this comment?')) return;
    
    if (!this.ticket?.id) return;

    this.ticketService.deleteComment(this.ticket.id, commentId).subscribe({
      next: () => {
        this.comments = this.comments.filter(c => c.id !== commentId);
        this.toastService.success('Comment deleted');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Delete comment error:', err);
        this.toastService.error('Failed to delete comment');
      }
    });
  }

  getStatusClass(status: string | undefined): string {
    return `status-${status}`;
  }

  getPriorityClass(priority: string | undefined): string {
    return `priority-${priority}`;
  }

  setTab(tab: string): void {
    this.activeTab = tab;
    this.cdr.detectChanges();
  }
}