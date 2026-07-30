// import { Component, OnInit, ChangeDetectorRef } 
//   from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { UserService } 
//   from '../../core/services/user.service';
// import { DepartmentService } 
//   from '../../core/services/department.service';
// import { RoleService } 
//   from '../../core/services/role.service';
// import { ToastService } 
//   from '../../core/services/toast.service';
// import { User } from '../../core/models/user.model';
// import { Department } 
//   from '../../core/models/department.model';
// import { Role } from '../../core/models/role.model';
// import { ConfirmModal }
//   from '../../shared/components/confirm-modal/confirm-modal';

// @Component({
//   selector: 'app-users',
//   standalone: true,
//   imports: [CommonModule, FormsModule, ConfirmModal],
//   templateUrl: './users.html',
//   styleUrl: './users.css'
  
// })
// export class Users implements OnInit {

//   users: User[] = [];
//   departments: Department[] = [];
//   roles: Role[] = [];

//   isLoading = true;
//   isSubmitting = false;
//   showForm = false;
//   isEditing = false;

//   searchText = '';
//   selectedDepartment = '';

//   editingId: number | null = null;

//   showDeleteModal = false;
//   deleteModalMessage = '';
//   userToDelete: User | null = null;

//   form = {
//     employeeId: '',
//     firstName: '',
//     lastName: '',
//     email: '',
//     phoneNumber: '',
//     active: true,
//     password: '',
//     departmentId: null as number | null,
//     roleIds: [] as number[]
//   };

//   constructor(
//     private userService: UserService,
//     private departmentService: DepartmentService,
//     private roleService: RoleService,
//     private toastService: ToastService,
//     private cdr: ChangeDetectorRef
//   ) {}

//   ngOnInit(): void {
//     this.loadUsers();
//     this.loadDepartments();
//     this.loadRoles();
//   }

//   loadUsers(): void {
//     this.isLoading = true;
//     this.userService.getAll().subscribe({
//       next: (data) => {
//         this.users = data;
//         this.isLoading = false;
//         this.cdr.detectChanges();
//       },
//       error: () => {
//         this.toastService.error('Failed to load users');
//         this.isLoading = false;
//         this.cdr.detectChanges();
//       }
//     });
//   }

//   loadDepartments(): void {
//     this.departmentService.getAll().subscribe({
//       next: (data) => {
//         this.departments = data;
//         this.cdr.detectChanges();
//       },
//       error: () => {}
//     });
//   }

//   loadRoles(): void {
//     this.roleService.getAll().subscribe({
//       next: (data) => {
//         this.roles = data;
//         this.cdr.detectChanges();
//       },
//       error: () => {}
//     });
//   }

//   get filteredUsers(): User[] {
//     let result = [...this.users];
//     if (this.searchText.trim()) {
//       const s = this.searchText.toLowerCase();
//       result = result.filter(u =>
//         u.firstName.toLowerCase().includes(s) ||
//         u.lastName.toLowerCase().includes(s) ||
//         u.email.toLowerCase().includes(s) ||
//         u.employeeId.toLowerCase().includes(s)
//       );
//     }
//     if (this.selectedDepartment) {
//       result = result.filter(
//         u => u.departmentName === this.selectedDepartment);
//     }
//     return result;
//   }

//   openAddForm(): void {
//     this.form = {
//       employeeId: '',
//       firstName: '',
//       lastName: '',
//       email: '',
//       phoneNumber: '',
//       active: true,
//       password: '',
//       departmentId: null,
//       roleIds: []
//     };
//     this.isEditing = false;
//     this.editingId = null;
//     this.showForm = true;
//     this.cdr.detectChanges();
//   }

// openEditForm(user: User): void {
//   this.form = {
//     employeeId: user.employeeId,
//     firstName: user.firstName,
//     lastName: user.lastName,
//     email: user.email,
//     phoneNumber: user.phoneNumber,
//     active: user.active,
//     password: '',
//     departmentId: user.departmentId,
//     roleIds: user.roleIds || []
//   };
//   this.isEditing = true;
//   this.editingId = user.id || null;
//   this.showForm = true;
//   this.cdr.detectChanges();

//   setTimeout(() => {
//     window.scrollTo({ top: 0, behavior: 'smooth' });
//   }, 50);
// }

//   closeForm(): void {
//     this.showForm = false;
//     this.editingId = null;
//     this.cdr.detectChanges();
//   }

//   isRoleSelected(roleId: number | undefined): boolean {
//     if (!roleId) return false;
//     return this.form.roleIds.includes(roleId);
//   }

//   toggleRole(roleId: number | undefined): void {
//     if (!roleId) return;
//     const index = this.form.roleIds.indexOf(roleId);
//     if (index === -1) {
//       this.form.roleIds.push(roleId);
//     } else {
//       this.form.roleIds.splice(index, 1);
//     }
//     this.cdr.detectChanges();
//   }

//   onSubmit(): void {
//     if (!this.form.firstName.trim() ||
//         !this.form.lastName.trim() ||
//         !this.form.email.trim() ||
//         !this.form.employeeId.trim() ||
//         !this.form.phoneNumber.trim() ||
//         !this.form.departmentId) {
//       this.toastService.error(
//         'Please fill in all required fields');
//       return;
//     }
//     if (!this.isEditing && !this.form.password.trim()) {
//       this.toastService.error('Password is required');
//       return;
//     }

//     this.isSubmitting = true;

//     const payload: User = {
//       employeeId: this.form.employeeId,
//       firstName: this.form.firstName,
//       lastName: this.form.lastName,
//       email: this.form.email,
//       phoneNumber: this.form.phoneNumber,
//       active: this.form.active,
//       password: this.form.password || undefined,
//       departmentId: this.form.departmentId!,
//       roleIds: this.form.roleIds
//     };

//     // if (this.isEditing && this.editingId) {
//     //   this.userService.update(
//     //     this.editingId, payload).subscribe({
//     //     next: (updated) => {
//     //       const index = this.users.findIndex(
//     //         u => u.id === this.editingId);
//     //       if (index !== -1) this.users[index] = updated;
//     //       this.isSubmitting = false;
//     //       this.closeForm();
//     //       this.toastService.success(
//     //         'User updated successfully');
//     //       this.cdr.detectChanges();
//     //     },
//     //     error: () => {
//     //       this.isSubmitting = false;
//     //       this.toastService.error('Failed to update user');
//     //       this.cdr.detectChanges();
//     //     }
//     //   });
//     // } else {
//     //   this.userService.create(payload).subscribe({
//     //     next: (created) => {
//     //       this.users.push(created);
//     //       this.isSubmitting = false;
//     //       this.closeForm();
//     //       this.toastService.success(
//     //         'User created successfully');
//     //       this.cdr.detectChanges();
//     //     },
//     //     error: () => {
//     //       this.isSubmitting = false;
//     //       this.toastService.error('Failed to create user');
//     //       this.cdr.detectChanges();
//     //     }
//     //   });
//     //}
//     if (this.isEditing && this.editingId) {
//   const updatePayload: User = {
//     employeeId: this.form.employeeId,
//     firstName: this.form.firstName,
//     lastName: this.form.lastName,
//     email: this.form.email,
//     phoneNumber: this.form.phoneNumber,
//     active: this.form.active,
//     departmentId: this.form.departmentId!,
//     roleIds: this.form.roleIds
//   };

//   if (this.form.password && this.form.password.trim()) {
//     updatePayload.password = this.form.password;
//   }

//   this.userService.update(
//     this.editingId, updatePayload).subscribe({
//     next: (updated) => {
//       const index = this.users.findIndex(
//         u => u.id === this.editingId);
//       if (index !== -1) {
//         this.users = [
//           ...this.users.slice(0, index),
//           updated,
//           ...this.users.slice(index + 1)
//         ];
//       }
//       this.isSubmitting = false;
//       this.closeForm();
//       this.toastService.success(
//         'User updated successfully');
//       this.cdr.detectChanges();
//     },
//     error: () => {
//       this.isSubmitting = false;
//       this.toastService.error('Failed to update user');
//       this.cdr.detectChanges();
//     }
//   });
// }
//   }

//   confirmDelete(user: User): void {
//   this.userToDelete = user;
//   this.deleteModalMessage =
//     `Are you sure you want to delete "${user.firstName} ${user.lastName}"? This action cannot be undone.`;
//   this.showDeleteModal = true;
//   this.cdr.detectChanges();
// }

// onDeleteConfirmed(): void {
//   if (!this.userToDelete) return;
//   this.showDeleteModal = false;

//   this.userService.delete(this.userToDelete.id!).subscribe({
//     next: () => {
//       this.users = this.users.filter(
//         u => u.id !== this.userToDelete!.id);
//       this.userToDelete = null;
//       this.toastService.success('User deleted successfully');
//       this.cdr.detectChanges();
//     },
//     error: () => {
//       this.toastService.error('Failed to delete user');
//       this.userToDelete = null;
//     }
//   });
// }

// onDeleteCancelled(): void {
//   this.showDeleteModal = false;
//   this.userToDelete = null;
//   this.cdr.detectChanges();
// }
// }



import { Component, OnInit, ChangeDetectorRef }
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService }
  from '../../core/services/user.service';
import { DepartmentService }
  from '../../core/services/department.service';
import { RoleService }
  from '../../core/services/role.service';
import { ToastService }
  from '../../core/services/toast.service';
import { User } from '../../core/models/user.model';
import { Department }
  from '../../core/models/department.model';
import { Role } from '../../core/models/role.model';
import { ConfirmModal }
  from '../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmModal],
  templateUrl: './users.html',
  styleUrl: './users.css'
})
export class Users implements OnInit {

  users: User[] = [];
  departments: Department[] = [];
  roles: Role[] = [];

  isLoading = true;
  isSubmitting = false;
  showForm = false;
  isEditing = false;

  searchText = '';
  selectedDepartment = '';

  editingId: number | null = null;

  showDeleteModal = false;
  deleteModalMessage = '';
  userToDelete: User | null = null;

  form = {
    employeeId: '',
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    active: true,
    password: '',
    departmentId: null as number | null,
    roleIds: [] as number[]
  };

  constructor(
    private userService: UserService,
    private departmentService: DepartmentService,
    private roleService: RoleService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadDepartments();
    this.loadRoles();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getAll().subscribe({
      next: (data) => {
        this.users = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error('Failed to load users');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadDepartments(): void {
    this.departmentService.getAll().subscribe({
      next: (data) => {
        this.departments = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  loadRoles(): void {
    this.roleService.getAll().subscribe({
      next: (data) => {
        this.roles = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  get filteredUsers(): User[] {
    let result = [...this.users];
    if (this.searchText.trim()) {
      const s = this.searchText.toLowerCase();
      result = result.filter(u =>
        u.firstName.toLowerCase().includes(s) ||
        u.lastName.toLowerCase().includes(s) ||
        u.email.toLowerCase().includes(s) ||
        u.employeeId.toLowerCase().includes(s)
      );
    }
    if (this.selectedDepartment) {
      result = result.filter(
        u => u.departmentName === this.selectedDepartment);
    }
    return result;
  }

  openAddForm(): void {
    this.form = {
      employeeId: '',
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: '',
      active: true,
      password: '',
      departmentId: null,
      roleIds: []
    };
    this.isEditing = false;
    this.editingId = null;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  openEditForm(user: User): void {
    this.form = {
      employeeId: user.employeeId,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      phoneNumber: user.phoneNumber,
      active: user.active,
      password: '',
      departmentId: user.departmentId,
      roleIds: user.roleIds ? [...user.roleIds] : []
    };
    this.isEditing = true;
    this.editingId = user.id || null;
    this.showForm = true;
    this.cdr.detectChanges();

    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 50);
  }

  closeForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.isSubmitting = false;
    this.cdr.detectChanges();
  }

  isRoleSelected(roleId: number | undefined): boolean {
    if (!roleId) return false;
    return this.form.roleIds.includes(roleId);
  }

  toggleRole(roleId: number | undefined): void {
    if (!roleId) return;
    const index = this.form.roleIds.indexOf(roleId);
    if (index === -1) {
      this.form.roleIds.push(roleId);
    } else {
      this.form.roleIds.splice(index, 1);
    }
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (!this.form.firstName.trim()) {
      this.toastService.error('First name is required');
      return;
    }
    if (!this.form.lastName.trim()) {
      this.toastService.error('Last name is required');
      return;
    }
    if (!this.form.email.trim()) {
      this.toastService.error('Email is required');
      return;
    }
    if (!this.form.employeeId.trim()) {
      this.toastService.error('Employee ID is required');
      return;
    }
    if (!this.form.phoneNumber.trim()) {
      this.toastService.error('Phone number is required');
      return;
    }
    if (!this.form.departmentId) {
      this.toastService.error('Department is required');
      return;
    }
    if (!this.isEditing) {
      if (!this.form.password ||
          this.form.password.trim().length < 8) {
        this.toastService.error(
          'Password must be at least 8 characters');
        return;
      }
    }

    this.isSubmitting = true;
    this.cdr.detectChanges();

    if (this.isEditing && this.editingId) {

      const updatePayload: User = {
        employeeId: this.form.employeeId,
        firstName: this.form.firstName,
        lastName: this.form.lastName,
        email: this.form.email,
        phoneNumber: this.form.phoneNumber,
        active: this.form.active,
        departmentId: this.form.departmentId!,
        roleIds: this.form.roleIds
      };

      if (this.form.password &&
          this.form.password.trim().length >= 8) {
        updatePayload.password = this.form.password;
      }

      this.userService.update(
        this.editingId, updatePayload).subscribe({
        next: (updated) => {
          const index = this.users.findIndex(
            u => u.id === this.editingId);
          if (index !== -1) {
            this.users = [
              ...this.users.slice(0, index),
              updated,
              ...this.users.slice(index + 1)
            ];
          }
          this.isSubmitting = false;
          this.closeForm();
          this.toastService.success(
            'User updated successfully');
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.log(err)
          this.isSubmitting = false;
          const message =
            err?.error?.message || err||
            'Failed to update user. Please try again.';
          this.toastService.error(message);
          this.cdr.detectChanges();
        }
      });

    } else {

      const createPayload: User = {
        employeeId: this.form.employeeId,
        firstName: this.form.firstName,
        lastName: this.form.lastName,
        email: this.form.email,
        phoneNumber: this.form.phoneNumber,
        active: this.form.active,
        password: this.form.password,
        departmentId: this.form.departmentId!,
        roleIds: this.form.roleIds
      };

      this.userService.create(createPayload).subscribe({
        next: (created) => {
          this.users = [...this.users, created];
          this.isSubmitting = false;
          this.closeForm();
          this.toastService.success(
            'User created successfully');
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isSubmitting = false;
          let message = 'Failed to create user.';
          if (err?.error?.message) {
            message = err.error.message;
          } else if (err?.error?.errors) {
            message = Object.values(
              err.error.errors).join(', ');
          }
          this.toastService.error(message);
          this.cdr.detectChanges();
        }
      });
    }
  }

  confirmDelete(user: User): void {
    this.userToDelete = user;
    this.deleteModalMessage =
      `Are you sure you want to delete "${user.firstName} ${user.lastName}"? This action cannot be undone.`;
    this.showDeleteModal = true;
    this.cdr.detectChanges();
  }

  onDeleteConfirmed(): void {
    if (!this.userToDelete) return;
    this.showDeleteModal = false;

    this.userService.delete(this.userToDelete.id!).subscribe({
      next: () => {
        this.users = this.users.filter(
          u => u.id !== this.userToDelete!.id);
        this.userToDelete = null;
        this.toastService.success(
          'User deleted successfully');
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error('Failed to delete user');
        this.userToDelete = null;
        this.cdr.detectChanges();
      }
    });
  }

  onDeleteCancelled(): void {
    this.showDeleteModal = false;
    this.userToDelete = null;
    this.cdr.detectChanges();
  }
}