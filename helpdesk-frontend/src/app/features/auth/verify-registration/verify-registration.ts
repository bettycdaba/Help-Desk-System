import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-verify-registration',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './verify-registration.html',
  styleUrl: './verify-registration.css'
})
export class VerifyRegistration implements OnInit {
  email = '';
  code = '';
  errorMessage = '';
  isLoading = false;
  isResending = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.email = this.route.snapshot.queryParamMap.get('email') || '';
  }

  onVerify(): void {
    this.errorMessage = '';
    if (!this.email || !/^\d{6}$/.test(this.code)) {
      this.errorMessage = 'Enter the email address and six-digit verification code.';
      return;
    }

    this.isLoading = true;
    this.authService.verifyEmail(this.email, this.code).subscribe({
      next: response => {
        this.isLoading = false;
        this.toastService.success(response.message);
        this.router.navigate(['/login']);
      },
      error: err => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || 'Verification failed. Please try again.';
      }
    });
  }

  onResend(): void {
    if (!this.email) {
      this.errorMessage = 'Enter your email address before requesting another code.';
      return;
    }

    this.isResending = true;
    this.authService.resendVerificationCode(this.email).subscribe({
      next: response => {
        this.isResending = false;
        this.toastService.success(response.message);
      },
      error: err => {
        this.isResending = false;
        this.errorMessage = err?.error?.message || 'Could not resend the code. Please try again.';
      }
    });
  }
}
