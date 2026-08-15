import { Component, OnInit, ChangeDetectorRef }
  from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NotificationService }
  from '../../core/services/notification.service';
import { AuthService }
  from '../../core/services/auth.service';
import { AppNotification }
  from '../../core/models/notification.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css'
})
export class Notifications implements OnInit {

  notifications: AppNotification[] = [];
  isLoading = true;

  currentPage = 1;
  pageSize = 10;

  showClearModal = false;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
  }

  getCurrentUserId(): number {
    return this.authService.getCurrentUser()?.id || 0;
  }

  load(): void {
    const userId = this.getCurrentUserId();
    if (!userId) return;

    this.isLoading = true;
    this.notificationService
      .getNotifications(userId)
      .subscribe({
        next: (data) => {
          this.notifications = data;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
  }

  get paginated(): AppNotification[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.notifications.slice(
      start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(
      this.notifications.length / this.pageSize);
  }

  get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize + 1;
  }

  get endIndex(): number {
    return Math.min(
      this.currentPage * this.pageSize,
      this.notifications.length);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.cdr.detectChanges();
    }
  }

  markAsRead(
    event: Event,
    n: AppNotification): void {
    event.stopPropagation();
    if (n.isRead) return;

    this.notificationService
      .markAsRead(n.id)
      .subscribe({
        next: () => {
          n.isRead = true;
          this.cdr.detectChanges();
        }
      });
  }

  markAllRead(): void {
    const userId = this.getCurrentUserId();
    if (!userId) return;

    this.notificationService
      .markAllAsRead(userId)
      .subscribe({
        next: () => {
          this.notifications.forEach(
            n => n.isRead = true);
          this.cdr.detectChanges();
        }
      });
  }

  goToTicket(n: AppNotification): void {
    if (!n.isRead) {
      this.notificationService.markAsRead(n.id)
        .subscribe({
          next: () => { n.isRead = true; }
        });
    }
    if (n.ticketId) {
      this.router.navigate(['/tickets', n.ticketId]);
    }
  }

  get unreadCount(): number {
    return this.notifications.filter(
      n => !n.isRead).length;
  }

  getIcon(type: string): string {
    switch (type) {
      case 'assign':   return 'bi-person-check-fill';
      case 'status':   return 'bi-arrow-repeat';
      case 'comment':  return 'bi-chat-dots-fill';
      case 'resolved': return 'bi-check-circle-fill';
      default:         return 'bi-bell-fill';
    }
  }

  getIconClass(type: string): string {
    switch (type) {
      case 'assign':   return 'icon-assign';
      case 'status':   return 'icon-status';
      case 'comment':  return 'icon-comment';
      case 'resolved': return 'icon-resolved';
      default:         return 'icon-default';
    }
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'assign':   return 'Assigned';
      case 'status':   return 'Status';
      case 'comment':  return 'Comment';
      case 'resolved': return 'Resolved';
      default:         return 'Update';
    }
  }

  timeAgo(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const seconds = Math.floor(
      (now.getTime() - date.getTime()) / 1000);

    if (seconds < 60) return 'Just now';
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return minutes + ' min ago';
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return hours + 'h ago';
    const days = Math.floor(hours / 24);
    if (days === 1) return 'Yesterday';
    if (days < 7) return days + ' days ago';
    return date.toLocaleDateString('en-GB', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  }

  // ── Clear All with Custom Modal ──

  requestClear(): void {
    this.showClearModal = true;
  }

  onClearConfirmed(): void {
    this.showClearModal = false;
    const userId = this.getCurrentUserId();
    if (!userId) return;

    this.notificationService.clearAllNotifications(userId).subscribe({
      next: () => {
        this.notifications = [];
        this.currentPage = 1;
        this.cdr.detectChanges();
      }
    });
  }

  onClearCancelled(): void {
    this.showClearModal = false;
  }
}