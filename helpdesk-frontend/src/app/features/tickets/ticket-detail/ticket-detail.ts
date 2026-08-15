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
import { CategoryService } 
  from '../../../core/services/category.service';
import { ToastService } 
  from '../../../core/services/toast.service';
import {
  Ticket, TicketComment,
  TicketAssignmentHistory, TicketStatusHistory, TicketAttachment
} from '../../../core/models/ticket.model';
import { TicketCategory } from '../../../core/models/category.model';
import { User } from '../../../core/models/user.model';
import { Subscription } from 'rxjs';
import { AuthService }
  from '../../../core/services/auth.service';


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
  categories: TicketCategory[] = [];

  isLoading = true;
  newComment = '';
  isSubmittingComment = false;

  selectedAssignee: number | null = null;
  selectedStatus = '';
  isAssigning = false;
  isUpdatingStatus = false;

  // Edit ticket form
  isEditingTicket = false;
  isSavingTicket = false;
  editForm = {
    subject: '',
    description: '',
    categoryId: null as number | null,
    priority: ''
  };

  activeTab = 'comments';

  attachments: any[] = [];
  isUploadingFile = false;
  isDraggingOnDetail = false;

  ticketId = 0;

  private subscriptions: Subscription[] = [];

  statuses = [
    'OPEN','ASSIGNED','IN_PROGRESS',
    'PENDING','RESOLVED','CLOSED','REOPENED'
  ];

  priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private userService: UserService,
    private categoryService: CategoryService,
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const sub = this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id && id > 0) {
        this.ticketId = id;
        this.isLoading = true;
        this.loadTicket(this.ticketId);
        this.loadUsers();
        this.loadCategories();
        this.loadComments(this.ticketId);
        this.loadHistory(this.ticketId);
        this.loadAttachments(this.ticketId);
      } else {
        this.toastService.error('Invalid ticket ID');
        this.router.navigate(['/tickets']);
      }
    });
    this.subscriptions.push(sub);
  }

  loadAttachments(ticketId: number): void {
    this.ticketService.getAttachments(ticketId).subscribe({
      next: (attachments) => {
        this.attachments = attachments;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  onDetailFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.uploadFile(input.files[0]);
      input.value = '';
    }
  }

  onDetailDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDraggingOnDetail = true;
  }

  onDetailDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDraggingOnDetail = false;
  }

  onDetailDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDraggingOnDetail = false;
    if (event.dataTransfer?.files?.length) {
      this.uploadFile(event.dataTransfer.files[0]);
    }
  }

  uploadFile(file: File): void {
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      this.toastService.error('File too large. Max 10MB.');
      return;
    }

    this.isUploadingFile = true;
    this.cdr.detectChanges();

    this.ticketService.uploadAttachment(
      this.ticketId,
      file,
      this.getCurrentUserId()
    ).subscribe({
      next: (attachment) => {
        this.attachments = [...this.attachments, attachment];
        this.isUploadingFile = false;
        this.toastService.success('File uploaded successfully');
        this.cdr.detectChanges();
      },
      error: () => {
        this.isUploadingFile = false;
        this.toastService.error('Failed to upload file');
        this.cdr.detectChanges();
      }
    });
  }

  downloadFile(attachment: any): void {
    this.ticketService.downloadAttachment(
      this.ticketId, attachment.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = attachment.fileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: () =>
        this.toastService.error('Failed to download file')
    });
  }

  deleteAttachment(attachmentId: number): void {
    if (!confirm('Delete this attachment?')) return;

    this.ticketService.deleteAttachment(
      this.ticketId, attachmentId).subscribe({
      next: () => {
        this.attachments = this.attachments.filter(
          a => a.id !== attachmentId);
        this.toastService.success('Attachment deleted');
        this.cdr.detectChanges();
      },
      error: () =>
        this.toastService.error('Failed to delete attachment')
    });
  }

  getFileIcon(fileType: string): string {
    switch (fileType?.toLowerCase()) {
      case 'jpg': case 'jpeg':
      case 'png': case 'gif':
      case 'webp':
        return 'bi-file-image text-success';
      case 'pdf':
        return 'bi-file-pdf text-danger';
      case 'doc': case 'docx':
        return 'bi-file-word text-primary';
      case 'zip': case 'rar':
        return 'bi-file-zip text-warning';
      default:
        return 'bi-file-text text-secondary';
    }
  }

  formatFileSize(bytes: number): string {
    if (!bytes) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) {
      return (bytes / 1024).toFixed(1) + ' KB';
    }
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  isImageFile(fileType: string): boolean {
    return ['jpg', 'jpeg', 'png', 'gif', 'webp']
      .includes(fileType?.toLowerCase());
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  isOwner(): boolean {
    const userId = this.getCurrentUserId();
    return this.ticket?.createdById === userId;
  }

  canManageTicket(): boolean {
    return this.isAdmin() || this.isOwner();
  }

  canAssignOrChangeStatus(): boolean {
    return this.isAdmin();
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
        this.toastService.error('Ticket not found or failed to load');
        this.isLoading = false;
        this.router.navigate(['/tickets']);
      }
    });
  }

  loadUsers(): void {
    this.userService.getActiveUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.cdr.detectChanges();
      },
      error: () => {}
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
    const user = this.authService.getCurrentUser();
    if (user?.id) return user.id;

    const stored = localStorage.getItem('currentUser');
    if (stored) {
      const parsed = JSON.parse(stored);
      return parsed.id || parsed.userId || 0;
    }
    return 0;
  }

  openEditForm(): void {
    if (!this.ticket) return;
    this.editForm = {
      subject: this.ticket.subject || '',
      description: this.ticket.description || '',
      categoryId: this.ticket.categoryId || null,
      priority: this.ticket.priority || 'MEDIUM'
    };
    this.isEditingTicket = true;
    this.cdr.detectChanges();
  }

  cancelEdit(): void {
    this.isEditingTicket = false;
    this.cdr.detectChanges();
  }

  saveTicket(): void {
    if (!this.ticket?.id) return;
    if (!this.editForm.subject.trim()) {
      this.toastService.error('Subject is required');
      return;
    }

    this.isSavingTicket = true;
    
    const updatedTicket: Ticket = {
      ...this.ticket,
      subject: this.editForm.subject,
      description: this.editForm.description,
      categoryId: this.editForm.categoryId ?? undefined,
      priority: this.editForm.priority
    };

    this.ticketService.update(this.ticket.id, updatedTicket).subscribe({
      next: (updated) => {
        this.ticket = updated;
        this.isSavingTicket = false;
        this.isEditingTicket = false;
        this.toastService.success('Ticket updated successfully');
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSavingTicket = false;
        this.toastService.error('Failed to update ticket');
        this.cdr.detectChanges();
      }
    });
  }

  addComment(): void {
    if (!this.newComment.trim()) {
      this.toastService.error('Comment cannot be empty');
      return;
    }
    if (!this.ticket?.id) return;

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
    if (!this.ticket?.id) return;

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
    if (!this.ticket?.id) return;

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
    if (!this.ticket?.id) return;

    this.ticketService.deleteComment(this.ticket.id, commentId).subscribe({
      next: () => {
        this.comments = this.comments.filter(c => c.id !== commentId);
        this.toastService.success('Comment deleted');
        this.cdr.detectChanges();
      },
      error: () => this.toastService.error('Failed to delete comment')
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