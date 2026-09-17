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
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';
import { ConfirmModal } from '../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmModal],
  templateUrl: './roles.html',
  styleUrl: './roles.css'
})
export class Roles implements OnInit {

  roles: Role[] = [];
  supervisors: User[] = [];
  permissions: Permission[] = [];
  selectedRolePermissions: RolePermissions | null = null;
  selectedPermissionIds: number[] = [];
  
  isLoading = true;
  isSubmitting = false;
  isSavingPermissions = false;
  showForm = false;
  showPermissions = false;
  isEditing = false;

  form: Role = { name: '', description: '', supervisorIds: [] };
  editingId: number | null = null;
  searchText = '';
  selectedRoleId: number | null = null;
  showStatusModal = false;
  statusTarget: Role | null = null;
  statusTargetActive = false;
  isUpdatingStatus = false;

  constructor(
    private roleService: RoleService,
    private permissionService: PermissionService,
    private userService: UserService,
    private toastService: ToastService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadRoles();
    this.loadSupervisors();
    if (this.hasPermission('MANAGE_PERMISSIONS')) {
      this.loadPermissions();
    }
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
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

  loadSupervisors(): void {
    this.userService.getAll().subscribe({
      next: (users) => {
        this.supervisors = users.filter(user =>
          user.active && user.roleNames?.includes('SUPERVISOR'));
        this.cdr.detectChanges();
      },
      error: () => this.toastService.error('Failed to load supervisors')
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
    this.form = { name: '', description: '', supervisorIds: [] };
    this.isEditing = false;
    this.editingId = null;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  openEditForm(role: Role): void {
    this.form = {
      ...role,
      supervisorIds: role.supervisorIds ? [...role.supervisorIds] : []
    };
    this.isEditing = true;
    this.editingId = role.id || null;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  closeForm(): void {
    this.showForm = false;
    this.form = { name: '', description: '', supervisorIds: [] };
    this.editingId = null;
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (!this.form.name.trim()) {
      this.toastService.error('Role name is required');
      return;
    }

    if ((this.form.supervisorIds?.length || 0) > 2) {
      this.toastService.error('A role can have at most two supervisors');
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

  isSupervisorSelected(userId: number | undefined): boolean {
    return userId !== undefined
      && (this.form.supervisorIds || []).includes(userId);
  }

  toggleSupervisor(userId: number | undefined): void {
    if (userId === undefined) return;
    const supervisorIds = this.form.supervisorIds || [];
    const index = supervisorIds.indexOf(userId);
    if (index >= 0) {
      supervisorIds.splice(index, 1);
    } else if (supervisorIds.length < 2) {
      supervisorIds.push(userId);
    } else {
      this.toastService.error('A role can have at most two supervisors');
    }
    this.form.supervisorIds = supervisorIds;
    this.cdr.detectChanges();
  }

  toggleStatus(role: Role): void {
    this.statusTarget = role;
    this.statusTargetActive = role.active !== true;
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
    this.roleService.updateStatus(
      this.statusTarget.id, this.statusTargetActive).subscribe({
      next: (updated) => {
        const index = this.roles.findIndex(
          r => r.id === updated.id);
        if (index !== -1) this.roles[index] = updated;
        this.isUpdatingStatus = false;
        const state = updated.active ? 'activated' : 'deactivated';
        this.toastService.success(`Role ${state} successfully`);
        this.closeStatusModal();
      },
      error: () => {
        this.isUpdatingStatus = false;
        this.toastService.error('Failed to update role status');
        this.cdr.detectChanges();
      }
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

