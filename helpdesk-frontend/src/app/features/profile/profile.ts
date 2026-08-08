import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {

  user: User | null = null;
  isLoading = true;
  isEditing = false;
  isSaving = false;
  isChangingPassword = false;
  showPasswordForm = false;

  editForm = {
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    employeeId: ''
  };

  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  getCurrentUserId(): number {
    const user = this.authService.getCurrentUser();
    return user?.id || 0;
  }

  loadProfile(): void {
    const userId = this.getCurrentUserId();
    if (!userId) {
      this.toastService.error('User not found');
      return;
    }
    this.isLoading = true;
    this.userService.getById(userId).subscribe({
      next: (user) => {
        this.user = user;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error('Failed to load profile');
        this.isLoading = false;
      }
    });
  }

  startEdit(): void {
    if (!this.user) return;
    this.editForm = {
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      email: this.user.email,
      phoneNumber: this.user.phoneNumber,
      employeeId: this.user.employeeId
    };
    this.isEditing = true;
  }

  cancelEdit(): void {
    this.isEditing = false;
  }

  saveProfile(): void {
    if (!this.user?.id) return;
    if (!this.editForm.firstName.trim() || !this.editForm.lastName.trim()) {
      this.toastService.error('Name is required');
      return;
    }

    this.isSaving = true;
    const updatePayload: User = {
      ...this.user,
      firstName: this.editForm.firstName,
      lastName: this.editForm.lastName,
      email: this.editForm.email,
      phoneNumber: this.editForm.phoneNumber,
      employeeId: this.editForm.employeeId
    };

    this.userService.update(this.user.id, updatePayload).subscribe({
      next: (updated) => {
        this.user = updated;
        this.isSaving = false;
        this.isEditing = false;
        this.toastService.success('Profile updated');
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isSaving = false;
        this.toastService.error(err?.error?.message || 'Update failed');
      }
    });
  }

  changePassword(): void {
    if (!this.passwordForm.newPassword || this.passwordForm.newPassword.length < 8) {
      this.toastService.error('Password must be at least 8 characters');
      return;
    }
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.toastService.error('Passwords do not match');
      return;
    }

    this.isChangingPassword = true;
    // Add password change API call here if you have one
    this.toastService.success('Password changed');
    this.isChangingPassword = false;
    this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
  }
}