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
import { AuthService } from '../../core/services/auth.service';
import { ConfirmModal } from '../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-departments',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmModal],
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
  showStatusModal = false;
  statusTarget: Department | null = null;
  statusTargetActive = false;
  isUpdatingStatus = false;

  constructor(
    private departmentService: DepartmentService,
    private toastService: ToastService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDepartments();
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
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

  toggleStatus(dept: Department): void {
    this.statusTarget = dept;
    this.statusTargetActive = dept.active !== true;
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
    this.departmentService.updateStatus(
      this.statusTarget.id, this.statusTargetActive).subscribe({
      next: (updated) => {
        const index = this.departments.findIndex(
          d => d.id === updated.id);
        if (index !== -1) this.departments[index] = updated;
        this.isUpdatingStatus = false;
        const state = updated.active ? 'activated' : 'deactivated';
        this.toastService.success(`Department ${state} successfully`);
        this.closeStatusModal();
      },
      error: () => {
        this.isUpdatingStatus = false;
        this.toastService.error('Failed to update department status');
        this.cdr.detectChanges();
      }
    });
  }
}
