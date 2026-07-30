import { Component, OnInit, ChangeDetectorRef } 
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } 
  from '../../../core/services/auth.service';
import { DepartmentService } 
  from '../../../core/services/department.service';
import { RoleService } 
  from '../../../core/services/role.service';
import { ToastService } 
  from '../../../core/services/toast.service';
import { Department } 
  from '../../../core/models/department.model';
import { Role } from '../../../core/models/role.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register implements OnInit {

  employeeId = '';
  firstName = '';
  lastName = '';
  email = '';
  phoneNumber = '';
  password = '';
  confirmPassword = '';
  departmentId: number | null = null;
  selectedRoleIds: number[] = [];

  departments: Department[] = [];
  roles: Role[] = [];

  isLoading = false;
  showPassword = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private departmentService: DepartmentService,
    private roleService: RoleService,
    private toastService: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.departmentService.getAll().subscribe({
      next: (d) => {
        this.departments = d;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
    this.roleService.getAll().subscribe({
      next: (r) => {
        this.roles = r.filter(
          role => role.name !== 'ADMIN' && 
                  role.name !== 'SUPERVISOR');
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }
  toggleRole(id: number | undefined): void {
    if (!id) return;
    const idx = this.selectedRoleIds.indexOf(id);
    if (idx === -1) {
      this.selectedRoleIds.push(id);
    } else {
      this.selectedRoleIds.splice(idx, 1);
    }
  }

  isRoleSelected(id: number | undefined): boolean {
    if (!id) return false;
    return this.selectedRoleIds.includes(id);
  }

  onRegister(): void {
    this.errorMessage = '';

    if (!this.firstName || !this.lastName ||
        !this.email || !this.password) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    if (this.password.length < 8) {
      this.errorMessage =
        'Password must be at least 8 characters.';
      return;
    }

    if (!this.departmentId) {
      this.errorMessage = 'Please select a department.';
      return;
    }

    this.isLoading = true;

    const user = {
      employeeId: this.employeeId,
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      phoneNumber: this.phoneNumber,
      password: this.password,
      active: true,
      departmentId: this.departmentId,
      roleIds: this.selectedRoleIds
    };

    this.authService.register(user as any).subscribe({
      next: () => {
        this.isLoading = false;
        this.toastService.success(
          'Account created! Please log in.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage =
          err?.error?.message ||
          'Registration failed. Please try again.';
        this.cdr.detectChanges();
      }
    });
  }
}