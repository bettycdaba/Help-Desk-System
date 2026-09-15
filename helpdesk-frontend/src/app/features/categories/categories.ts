import { Component, OnInit, ChangeDetectorRef } 
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } 
  from '../../core/services/category.service';
import { ToastService } 
  from '../../core/services/toast.service';
import { TicketCategory } 
  from '../../core/models/category.model';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmModal } from '../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmModal],
  templateUrl: './categories.html',
  styleUrl: './categories.css'
})
export class Categories implements OnInit {

  categories: TicketCategory[] = [];
  isLoading = true;
  isSubmitting = false;
  showForm = false;
  isEditing = false;

  form: TicketCategory = { name: '', description: '' };
  editingId: number | null = null;
  searchText = '';
  showStatusModal = false;
  statusTarget: TicketCategory | null = null;
  statusTargetActive = false;
  isUpdatingStatus = false;

  constructor(
    private categoryService: CategoryService,
    private toastService: ToastService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  loadCategories(): void {
    this.isLoading = true;
    this.categoryService.getAll().subscribe({
      next: (data) => {
        this.categories = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error(
          'Failed to load categories');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get filteredCategories(): TicketCategory[] {
    if (!this.searchText.trim()) return this.categories;
    return this.categories.filter(c =>
      c.name.toLowerCase().includes(
        this.searchText.toLowerCase())
    );
  }

  openAddForm(): void {
    this.form = { name: '', description: '' };
    this.isEditing = false;
    this.editingId = null;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  openEditForm(cat: TicketCategory): void {
    this.form = { ...cat };
    this.isEditing = true;
    this.editingId = cat.id || null;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  closeForm(): void {
    this.showForm = false;
    this.form = { name: '', description: '' };
    this.editingId = null;
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (!this.form.name.trim()) {
      this.toastService.error('Category name is required');
      return;
    }

    this.isSubmitting = true;

    if (this.isEditing && this.editingId) {
      this.categoryService.update(
        this.editingId, this.form).subscribe({
        next: (updated) => {
          const index = this.categories.findIndex(
            c => c.id === this.editingId);
          if (index !== -1) {
            this.categories[index] = updated;
          }
          this.isSubmitting = false;
          this.closeForm();
          this.toastService.success(
            'Category updated successfully');
          this.cdr.detectChanges();
        },
        error: () => {
          this.isSubmitting = false;
          this.toastService.error(
            'Failed to update category');
          this.cdr.detectChanges();
        }
      });
    } else {
      this.categoryService.create(this.form).subscribe({
        next: (created) => {
          this.categories.push(created);
          this.isSubmitting = false;
          this.closeForm();
          this.toastService.success(
            'Category created successfully');
          this.cdr.detectChanges();
        },
        error: () => {
          this.isSubmitting = false;
          this.toastService.error(
            'Failed to create category');
          this.cdr.detectChanges();
        }
      });
    }
  }

  toggleStatus(cat: TicketCategory): void {
    this.statusTarget = cat;
    this.statusTargetActive = cat.active !== true;
    this.showStatusModal = true;
    this.cdr.detectChanges();
  }

  closeStatusModal(): void {
    this.showStatusModal = false;
    this.statusTarget = null;
    this.cdr.detectChanges();
  }

  confirmStatusChange(): void {
    if (!this.statusTarget?.id) return;
    this.isUpdatingStatus = true;
    this.categoryService.updateStatus(
      this.statusTarget.id, this.statusTargetActive).subscribe({
      next: (updated) => {
        const index = this.categories.findIndex(
          c => c.id === updated.id);
        if (index !== -1) this.categories[index] = updated;
        this.isUpdatingStatus = false;
        const state = updated.active ? 'activated' : 'deactivated';
        this.toastService.success(`Category ${state} successfully`);
        this.closeStatusModal();
      },
      error: () => {
        this.isUpdatingStatus = false;
        this.toastService.error('Failed to update category status');
        this.cdr.detectChanges();
      }
    });
  }
}
