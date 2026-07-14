import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TicketService } from '../../../core/services/ticket.service';
import { CategoryService } from '../../../core/services/category.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Ticket } from '../../../core/models/ticket.model';
import { TicketCategory } from '../../../core/models/category.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-ticket-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ticket-create.html',
  styleUrls: ['./ticket-create.css'],
})
export class TicketCreate implements OnInit {
  subject = '';
  description = '';
  categoryId: number | null = null;
  assignedToId: number | null = null;
  priority = 'LOW';

  categories: TicketCategory[] = [];
  users: User[] = [];

  loading = false;
  saving = false;
  error: string | null = null;

  priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(
    private ticketService: TicketService,
    private categoryService: CategoryService,
    private userService: UserService,
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = null;

    this.categoryService.getAll().subscribe({
      next: cats => { this.categories = cats || []; },
      error: () => { /* ignore for now */ }
    });

    this.userService.getAll().subscribe({
      next: users => { this.users = users || []; },
      error: () => { /* ignore */ }
    });

    this.loading = false;
  }

  onSubmit(): void {
    if (!this.subject) {
      this.error = 'Subject is required.';
      return;
    }

    const current = this.authService.getCurrentUser();
    const createdById = current ? current.id : undefined;

    const payload: Ticket = {
      subject: this.subject,
      description: this.description,
      categoryId: this.categoryId || undefined,
      assignedToId: this.assignedToId || undefined,
      priority: this.priority,
      createdById: createdById
    } as Ticket;

    this.saving = true;
    this.ticketService.create(payload).subscribe({
      next: (created) => {
        this.saving = false;
        this.toastService.success('Ticket created successfully');
        if (created && created.id) {
          this.router.navigate(['/tickets', created.id]);
        } else {
          this.router.navigate(['/tickets']);
        }
      },
      error: (err) => {
        this.saving = false;
        this.error = err && err.message ? err.message : 'Failed to create ticket.';
        this.toastService.error(this.error || 'Failed to create ticket.');
      }
    });
  }
}
