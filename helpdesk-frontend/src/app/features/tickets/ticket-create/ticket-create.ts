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

  priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(
    private ticketService: TicketService,
    private categoryService: CategoryService,
    private userService: UserService,
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
        this.isSubmitting = false;
        this.cdr.detectChanges();
        this.toastService.success(
          `Ticket ${created.ticketNumber} created!`);
        setTimeout(() => {
          this.router.navigate(['/tickets', created.id]);
        }, 500);
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
}