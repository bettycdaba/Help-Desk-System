import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } 
  from '../../../core/services/auth.service';
import { ToastService } 
  from '../../../core/services/toast.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {

  email = '';
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onSubmit(): void {
    if (!this.email.trim()) {
      this.errorMessage = 'Please enter your email.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage =
          'A temporary password has been sent to your email. Check your inbox.';
        this.cdr.detectChanges();
        setTimeout(() => {
          this.router.navigate(['/reset-password'], {
            queryParams: { email: this.email }
          });
        }, 3000);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage =
          err?.error?.message ||
          'Email not found. Please check and try again.';
        this.cdr.detectChanges();
      }
    });
  }
}