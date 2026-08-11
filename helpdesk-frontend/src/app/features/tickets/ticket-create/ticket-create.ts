import { Component, OnInit, ChangeDetectorRef }
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TicketService }
  from '../../../core/services/ticket.service';
import { CategoryService }
  from '../../../core/services/category.service';
import { UserService }
  from '../../../core/services/user.service';
import { ToastService }
  from '../../../core/services/toast.service';
import { TicketCategory }
  from '../../../core/models/category.model';
import { User } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-ticket-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ticket-create.html',
  styleUrl: './ticket-create.css'
})
export class TicketCreate implements OnInit {

  subject = '';
  description = '';
  priority = 'MEDIUM';
  categoryId: number | null = null;
  assignedToId: number | null = null;

  categories: TicketCategory[] = [];
  users: User[] = [];
  isSubmitting = false;

  selectedFiles: File[] = [];
  isDragging = false;

  priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(
    private ticketService: TicketService,
    private categoryService: CategoryService,
    private userService: UserService,
     private authService: AuthService,
    private toastService: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadUsers();
  }

  loadCategories(): void {
    this.categoryService.getAll().subscribe({
      next: (cats) => {
        this.categories = cats;
        this.cdr.detectChanges();
      },
      error: () =>
        this.toastService.error('Failed to load categories')
    });
  }

  loadUsers(): void {
    this.userService.getAll().subscribe({
      next: (users) => {
        this.users = users;
        this.cdr.detectChanges();
      },
      error: () =>
        this.toastService.error('Failed to load users')
    });
  }

  getCurrentUserId(): number {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      const user = JSON.parse(stored);
      return user.id || user.userId || 1;
    }
    return 1;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.addFiles(Array.from(input.files));
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    if (event.dataTransfer?.files) {
      this.addFiles(Array.from(event.dataTransfer.files));
    }
  }

  addFiles(files: File[]): void {
    const maxSize = 10 * 1024 * 1024;
    const allowed = [
      'image/jpeg', 'image/png', 'image/gif',
      'image/webp', 'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'text/plain', 'application/zip'
    ];

    for (const file of files) {
      if (file.size > maxSize) {
        this.toastService.error(
          `${file.name} is too large. Max 10MB.`);
        continue;
      }
      if (!allowed.includes(file.type)) {
        this.toastService.error(
          `${file.name} — file type not allowed.`);
        continue;
      }
      if (this.selectedFiles.length >= 5) {
        this.toastService.error(
          'Maximum 5 files allowed per ticket.');
        break;
      }
      const exists = this.selectedFiles.some(
        f => f.name === file.name && 
             f.size === file.size);
      if (!exists) {
        this.selectedFiles.push(file);
      }
    }
    this.cdr.detectChanges();
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.cdr.detectChanges();
  }

  getFileIcon(file: File): string {
    if (file.type.startsWith('image/')) {
      return 'bi-file-image text-success';
    }
    if (file.type === 'application/pdf') {
      return 'bi-file-pdf text-danger';
    }
    if (file.type.includes('word')) {
      return 'bi-file-word text-primary';
    }
    if (file.type === 'application/zip') {
      return 'bi-file-zip text-warning';
    }
    return 'bi-file-text text-secondary';
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) {
      return (bytes / 1024).toFixed(1) + ' KB';
    }
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  onSubmit(): void {
    if (!this.subject.trim()) {
      this.toastService.error('Subject is required');
      return;
    }
    if (!this.categoryId) {
      this.toastService.error('Please select a category');
      return;
    }

    this.isSubmitting = true;
    this.cdr.detectChanges();

    const payload: any = {
      subject: this.subject,
      description: this.description,
      priority: this.priority,
      categoryId: this.categoryId,
      createdById: this.getCurrentUserId()
    };

    if (this.assignedToId) {
      payload.assignedToId = this.assignedToId;
    }

    this.ticketService.create(payload).subscribe({
      next: (created) => {
        if (this.selectedFiles.length > 0) {
          this.uploadFilesForTicket(
            created.id!, created.ticketNumber || '');
        } else {
          this.isSubmitting = false;
          this.cdr.detectChanges();
          this.toastService.success(
            `Ticket ${created.ticketNumber} created!`);
          setTimeout(() => {
            this.router.navigate(['/tickets', created.id]);
          }, 500);
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
        this.toastService.error(
          err?.error?.message ||
          'Failed to create ticket. Please try again.');
      }
    });
  }

  private uploadFilesForTicket(
    ticketId: number,
    ticketNumber: string): void {

    const userId = this.getCurrentUserId();
    const uploadPromises = this.selectedFiles.map(file =>
      new Promise<void>((resolve) => {
        this.ticketService.uploadAttachment(
          ticketId, file, userId).subscribe({
          next: () => resolve(),
          error: () => {
            this.toastService.error(
              `Failed to upload: ${file.name}`);
            resolve();
          }
        });
      })
    );

    Promise.all(uploadPromises).then(() => {
      this.isSubmitting = false;
      this.cdr.detectChanges();
      this.toastService.success(
        `Ticket ${ticketNumber} created with attachments!`);
      setTimeout(() => {
        this.router.navigate(['/tickets', ticketId]);
      }, 500);
    });
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
}
}