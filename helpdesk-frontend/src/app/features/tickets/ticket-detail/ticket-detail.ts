import { Component, OnInit, ChangeDetectorRef } 
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
import { WebSocketService } 
  from '../../../core/services/websocket.services';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ticket-detail.html',
  styleUrl: './ticket-detail.css'
})
export class TicketDetail implements OnInit {

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
    private cdr: ChangeDetectorRef,
    private wsService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id && id > 0) {
        this.ticketId = id;
        this.isLoading = true;
        this.loadTicket(id);
        this.loadUsers();
        this.loadComments(id);
        this.loadHistory(id);
      } else {
        this.toastService.error('Invalid ticket ID');
        this.router.navigate(['/tickets']);
      }
    });
  }

  loadTicket(id: number): void {
  this.ticketService.getById(id).subscribe({
    next: (ticket) => {
      this.ticket = ticket;
      this.selectedStatus = ticket.status || '';
      this.selectedAssignee = ticket.assignedToId || null;
      this.isLoading = false;
      this.cdr.detectChanges();
    },

    error: () => {
      this.toastService.error(
        'Ticket not found or failed to load'
      );

      this.isLoading = false;
      this.cdr.detectChanges();
      this.router.navigate(['/tickets']);
    }
  });
}

  // loadTicket(id: number): void {
  //   this.ticketService.getById(id).subscribe({
  //     next: (ticket) => {
  //       this.ticket = ticket;
  //       this.selectedStatus = ticket.status || '';
  //       this.selectedAssignee = ticket.assignedToId || null;
  //       this.isLoading = false;
  //       this.cdr.detectChanges();
  //     },
  //     error: (err) => {
  //       this.toastService.error(
  //         'Ticket not found or failed to load');
  //       this.isLoading = false;
  //       this.cdr.detectChanges();
  //       this.router.navigate(['/tickets']);
  //     }
  //     this.wsService.connect();

  //     this.wsService.ticketUpdates$.subscribe(update => {
  //       if (update.data?.id === this.ticketId) {
  //         this.ticket = update.data;
  //         this.loadHistory(this.ticketId);
  //         this.cdr.detectChanges();
  //       }
  //     });

  //     this.wsService.commentUpdates$.subscribe(comment => {
  //       if (comment.ticketId === this.ticketId) {
  //         this.comments = [...this.comments, comment];
  //         this.cdr.detectChanges();
  //       }
  //     });
  //   });
  // }

  loadUsers(): void {
    this.userService.getAll().subscribe({
      next: (users) => {
        this.users = users;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  loadComments(id: number): void {
    this.ticketService.getComments(id).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  loadHistory(id: number): void {
    this.ticketService.getAssignmentHistory(id).subscribe({
      next: (h) => {
        this.assignmentHistory = h;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
    this.ticketService.getStatusHistory(id).subscribe({
      next: (h) => {
        this.statusHistory = h;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  getCurrentUserId(): number {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      const user = JSON.parse(stored);
      return user.id || 1;
    }
    return 1;
  }

  addComment(): void {
    if (!this.newComment.trim()) {
      this.toastService.error('Comment cannot be empty');
      return;
    }

    this.isSubmittingComment = true;
    const comment: TicketComment = {
      comment: this.newComment,
      userId: this.getCurrentUserId()
    };

    this.ticketService.addComment(
      this.ticket!.id!, comment).subscribe({
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

    this.isAssigning = true;
    this.ticketService.assign(this.ticket!.id!, {
      newAssigneeId: this.selectedAssignee,
      assignedById: this.getCurrentUserId()
    }).subscribe({
      next: (updated) => {
        this.ticket = updated;
        this.isAssigning = false;
        this.toastService.success(
          'Ticket assigned successfully');
        this.loadHistory(this.ticket.id!);
        this.cdr.detectChanges();
      },
      error: () => {
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

    this.isUpdatingStatus = true;
    this.ticketService.updateStatus(this.ticket!.id!, {
      newStatus: this.selectedStatus,
      changedById: this.getCurrentUserId()
    }).subscribe({
      next: (updated) => {
        this.ticket = updated;
        this.isUpdatingStatus = false;
        this.toastService.success(
          'Status updated successfully');
        this.loadHistory(this.ticket.id!);
        this.cdr.detectChanges();
      },
      error: () => {
        this.isUpdatingStatus = false;
        this.toastService.error('Failed to update status');
        this.cdr.detectChanges();
      }
    });
  }

  deleteComment(commentId: number | undefined): void {
    if (!commentId) return;
    if (!confirm('Delete this comment?')) return;

    this.ticketService.deleteComment(
      this.ticket!.id!, commentId).subscribe({
      next: () => {
        this.comments = this.comments.filter(
          c => c.id !== commentId);
        this.toastService.success('Comment deleted');
        this.cdr.detectChanges();
      },
      error: () =>
        this.toastService.error('Failed to delete comment')
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