import { Component, OnInit, ChangeDetectorRef } 
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } 
  from '@angular/router';
import { AuthService } 
  from '../../../core/services/auth.service';
import { ToastService } 
  from '../../../core/services/toast.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPassword implements OnInit {

  email = '';
  temporaryPassword = '';
  newPassword = '';
  confirmPassword = '';

  isLoading = false;
  errorMessage = '';
  showPasswords = false;

  constructor(
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || '';
    });
  }

  onSubmit(): void {
    this.errorMessage = '';

    if (!this.email || !this.temporaryPassword ||
        !this.newPassword) {
      this.errorMessage = 'All fields are required.';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'New passwords do not match.';
      return;
    }

    if (this.newPassword.length < 8) {
      this.errorMessage =
        'New password must be at least 8 characters.';
      return;
    }

    this.isLoading = true;

    this.authService.resetPassword({
      email: this.email,
      temporaryPassword: this.temporaryPassword,
      newPassword: this.newPassword
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.toastService.success(
          'Password reset successfully! Please log in.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage =
          err?.error?.message ||
          'Reset failed. Check your temporary password.';
        this.cdr.detectChanges();
      }
    });
  }
}