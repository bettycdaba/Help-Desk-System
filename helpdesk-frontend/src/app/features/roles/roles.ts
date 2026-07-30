import { Component, OnInit, ChangeDetectorRef } 
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RoleService } 
  from '../../core/services/role.service';
import { ToastService } 
  from '../../core/services/toast.service';
import { Role } from '../../core/models/role.model';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './roles.html',
  styleUrl: './roles.css'
})
export class Roles implements OnInit {

  roles: Role[] = [];
  isLoading = true;
  isSubmitting = false;
  showForm = false;
  isEditing = false;

  form: Role = { name: '', description: '' };
  editingId: number | null = null;
  searchText = '';

  constructor(
    private roleService: RoleService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.isLoading = true;
    this.roleService.getAll().subscribe({
      next: (data) => {
        this.roles = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error('Failed to load roles');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get filteredRoles(): Role[] {
    if (!this.searchText.trim()) return this.roles;
    return this.roles.filter(r =>
      r.name.toLowerCase().includes(
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

  openEditForm(role: Role): void {
    this.form = { ...role };
    this.isEditing = true;
    this.editingId = role.id || null;
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
      this.toastService.error('Role name is required');
      return;
    }

    this.isSubmitting = true;

    if (this.isEditing && this.editingId) {
      this.roleService.update(
        this.editingId, this.form).subscribe({
        next: (updated) => {
          const index = this.roles.findIndex(
            r => r.id === this.editingId);
          if (index !== -1) this.roles[index] = updated;
          this.isSubmitting = false;
          this.closeForm();
          this.toastService.success(
            'Role updated successfully');
          this.cdr.detectChanges();
        },
        error: () => {
          this.isSubmitting = false;
          this.toastService.error('Failed to update role');
          this.cdr.detectChanges();
        }
      });
    } else {
      this.roleService.create(this.form).subscribe({
        next: (created) => {
          this.roles.push(created);
          this.isSubmitting = false;
          this.closeForm();
          this.toastService.success(
            'Role created successfully');
          this.cdr.detectChanges();
        },
        error: () => {
          this.isSubmitting = false;
          this.toastService.error('Failed to create role');
          this.cdr.detectChanges();
        }
      });
    }
  }

  delete(role: Role): void {
    if (!confirm(`Delete role "${role.name}"?`)) return;
    this.roleService.delete(role.id!).subscribe({
      next: () => {
        this.roles = this.roles.filter(
          r => r.id !== role.id);
        this.toastService.success('Role deleted');
        this.cdr.detectChanges();
      },
      error: () =>
        this.toastService.error('Failed to delete role')
    });
  }
}