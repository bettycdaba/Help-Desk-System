import { Component, OnInit, ChangeDetectorRef } 
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } 
  from '../../../core/services/auth.service';
import { DepartmentService } 
  from '../../../core/services/department.service';
import { ToastService } 
  from '../../../core/services/toast.service';
import { Department } 
  from '../../../core/models/department.model';


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
    departments: Department[] = [];
  

  isLoading = false;
  showPassword = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private departmentService: DepartmentService,
    private toastService: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.departmentService.getAll().subscribe({
      next: (d) => {
        this.departments = d || [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to fetch departments:', err);
      }
    });
  }

  get activeDepartments(): Department[] {
    if (!this.departments) return [];
    return this.departments.filter(dept => dept && dept.active !== false);
  }




  onRegister(): void {
    this.errorMessage = '';

    if (!this.employeeId || !this.firstName || !this.lastName ||
        !this.email || !this.phoneNumber || !this.password) {
        this.errorMessage = 'Please fill in all required fields.';
        return;
    }

    if (!this.isValidEmail(this.email)) {
      this.errorMessage = 'Please enter a valid email address.';
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
  departmentId: this.departmentId
};

    this.authService.register(user as any).subscribe({
      next: () => {
        this.isLoading = false;
        this.toastService.success(
          'Account created! Check your email for the verification code.');
        this.router.navigate(['/verify-registration'], {
          queryParams: { email: this.email }
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = this.registrationErrorMessage(err);
        this.cdr.detectChanges();
      }
    });
  }

  private isValidEmail(email: string): boolean {
    return /^[A-Za-z0-9](?:[A-Za-z0-9._%+-]*[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$/.test(email.trim());
  }

  private registrationErrorMessage(err: any): string {
    if (err?.error?.message) {
      return err.error.message;
    }

    const fieldErrors = err?.error?.errors;
    if (fieldErrors && typeof fieldErrors === 'object') {
      return Object.values(fieldErrors)[0] as string;
    }

    return 'Registration failed. Please try again.';
  }
}
