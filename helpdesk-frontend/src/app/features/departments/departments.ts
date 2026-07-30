import { Component, OnInit, ChangeDetectorRef } 
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DepartmentService } 
  from '../../core/services/department.service';
import { ToastService } 
  from '../../core/services/toast.service';
import { Department } 
  from '../../core/models/department.model';

@Component({
  selector: 'app-departments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './departments.html',
  styleUrl: './departments.css'
})
export class Departments implements OnInit {

  departments: Department[] = [];
  isLoading = true;
  isSubmitting = false;
  showForm = false;
  isEditing = false;

  form: Department = { name: '', description: '' };
  editingId: number | null = null;
  searchText = '';

  constructor(
    private departmentService: DepartmentService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.isLoading = true;
    this.departmentService.getAll().subscribe({
      next: (data) => {
        this.departments = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error(
          'Failed to load departments');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get filteredDepartments(): Department[] {
    if (!this.searchText.trim()) {
      return this.departments;
    }
    return this.departments.filter(d =>
      d.name.toLowerCase().includes(
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

  openEditForm(dept: Department): void {
    this.form = { ...dept };
    this.isEditing = true;
    this.editingId = dept.id || null;
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
      this.toastService.error('Name is required');
      return;
    }

    this.isSubmitting = true;

    if (this.isEditing && this.editingId) {
      this.departmentService.update(
        this.editingId, this.form).subscribe({
        next: (updated) => {
          const index = this.departments.findIndex(
            d => d.id === this.editingId);
          if (index !== -1) {
            this.departments[index] = updated;
          }
          this.isSubmitting = false;
          this.closeForm();
          this.toastService.success(
            'Department updated successfully');
          this.cdr.detectChanges();
        },
        error: () => {
          this.isSubmitting = false;
          this.toastService.error(
            'Failed to update department');
          this.cdr.detectChanges();
        }
      });
    } else {
      this.departmentService.create(this.form).subscribe({
        next: (created) => {
          this.departments.push(created);
          this.isSubmitting = false;
          this.closeForm();
          this.toastService.success(
            'Department created successfully');
          this.cdr.detectChanges();
        },
        error: () => {
          this.isSubmitting = false;
          this.toastService.error(
            'Failed to create department');
          this.cdr.detectChanges();
        }
      });
    }
  }

  delete(dept: Department): void {
    if (!confirm(
      `Delete department "${dept.name}"?`)) return;

    this.departmentService.delete(dept.id!).subscribe({
      next: () => {
        this.departments = this.departments.filter(
          d => d.id !== dept.id);
        this.toastService.success(
          'Department deleted successfully');
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error(
          'Failed to delete department');
      }
    });
  }
}