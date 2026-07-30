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

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
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

  constructor(
    private categoryService: CategoryService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCategories();
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

  delete(cat: TicketCategory): void {
    if (!confirm(
      `Delete category "${cat.name}"?`)) return;
    this.categoryService.delete(cat.id!).subscribe({
      next: () => {
        this.categories = this.categories.filter(
          c => c.id !== cat.id);
        this.toastService.success('Category deleted');
        this.cdr.detectChanges();
      },
      error: () =>
        this.toastService.error(
          'Failed to delete category')
    });
  }
}