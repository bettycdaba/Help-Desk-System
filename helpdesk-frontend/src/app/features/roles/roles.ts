import { Component, OnInit, ChangeDetectorRef } 
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RoleService } 
  from '../../core/services/role.service';
import { PermissionService } 
  from '../../core/services/permission.service';
import { ToastService } 
  from '../../core/services/toast.service';
import { Role } from '../../core/models/role.model';
import { Permission, RolePermissions } 
  from '../../core/models/permission.model';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './roles.html',
  styleUrl: './roles.css'
})
export class Roles implements OnInit {

  roles: Role[] = [];
  permissions: Permission[] = [];
  selectedRolePermissions: RolePermissions | null = null;
  selectedPermissionIds: number[] = [];
  
  isLoading = true;
  isSubmitting = false;
  isSavingPermissions = false;
  showForm = false;
  showPermissions = false;
  isEditing = false;

  form: Role = { name: '', description: '' };
  editingId: number | null = null;
  searchText = '';
  selectedRoleId: number | null = null;

  constructor(
    private roleService: RoleService,
    private permissionService: PermissionService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadRoles();
    this.loadPermissions();
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

  loadPermissions(): void {
    this.permissionService.getAllPermissions().subscribe({
      next: (data) => {
        this.permissions = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error('Failed to load permissions');
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


openPermissions(role: Role): void {
  console.log('🔍 Opening permissions for role:', role);
  this.selectedRoleId = role.id || null;
  this.showPermissions = true;
  
  console.log('🔍 All permissions loaded:', this.permissions);
  console.log('🔍 Permission groups:', this.getPermissionGroups());
  
  if (role.id) {
    this.permissionService.getRolePermissions(role.id).subscribe({
      next: (data) => {
        console.log('✅ Role permissions from API:', data);
        this.selectedRolePermissions = data;
        this.selectedPermissionIds = [...data.permissionIds];
        console.log('✅ Selected permission IDs:', this.selectedPermissionIds);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Failed to load role permissions:', err);
        this.toastService.error('Failed to load role permissions');
      }
    });
  }
}

  // Close permissions panel
  closePermissions(): void {
    this.showPermissions = false;
    this.selectedRolePermissions = null;
    this.selectedPermissionIds = [];
    this.selectedRoleId = null;
    this.cdr.detectChanges();
  }

  // Toggle permission checkbox
  togglePermission(permissionId: number): void {
    const index = this.selectedPermissionIds.indexOf(permissionId);
    if (index === -1) {
      this.selectedPermissionIds.push(permissionId);
    } else {
      this.selectedPermissionIds.splice(index, 1);
    }
  }

  // Check if permission is selected
  isPermissionSelected(permissionId: number): boolean {
    return this.selectedPermissionIds.includes(permissionId);
  }

  // Save permission changes
  savePermissions(): void {
    if (!this.selectedRoleId) return;
    
    this.isSavingPermissions = true;
    this.permissionService.updateRolePermissions(
      this.selectedRoleId, 
      this.selectedPermissionIds
    ).subscribe({
      next: (data) => {
        this.selectedRolePermissions = data;
        this.isSavingPermissions = false;
        this.toastService.success('Permissions updated successfully');
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSavingPermissions = false;
        this.toastService.error('Failed to update permissions');
        this.cdr.detectChanges();
      }
    });
  }

  // Group permissions by category for display
  getPermissionGroups(): { category: string, permissions: Permission[] }[] {
    const groups: { [key: string]: Permission[] } = {};
    
    this.permissions.forEach(p => {
      let category = 'Other';
      if (p.name.includes('USER')) category = 'Users';
      else if (p.name.includes('TICKET')) category = 'Tickets';
      else if (p.name.includes('DEPARTMENT')) category = 'Departments';
      else if (p.name.includes('ROLE')) category = 'Roles';
      else if (p.name.includes('CATEGOR')) category = 'Categories';
      else if (p.name.includes('PERMISSION')) category = 'System';
      
      if (!groups[category]) groups[category] = [];
      groups[category].push(p);
    });
    
    return Object.keys(groups).map(key => ({
      category: key,
      permissions: groups[key]
    }));
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
      this.roleService.update(this.editingId, this.form).subscribe({
        next: (updated) => {
          const index = this.roles.findIndex(r => r.id === this.editingId);
          if (index !== -1) this.roles[index] = updated;
          this.isSubmitting = false;
          this.closeForm();
          this.toastService.success('Role updated successfully');
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
          this.toastService.success('Role created successfully');
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
        this.roles = this.roles.filter(r => r.id !== role.id);
        this.toastService.success('Role deleted');
        this.cdr.detectChanges();
      },
      error: () =>
        this.toastService.error('Failed to delete role')
    });
  }
  formatPermissionName(name: string): string {
  return name
    .replace(/_/g, ' ')
    .split(' ')
    .map(word => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
}
}

