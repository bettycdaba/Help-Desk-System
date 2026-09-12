import {
  Component, OnInit, OnDestroy,
  ChangeDetectorRef, Output, EventEmitter,
  HostListener
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NotificationService }
  from '../../../core/services/notification.service';
import { AuthService }
  from '../../../core/services/auth.service';
import { AppNotification }
  from '../../../core/models/notification.model';

@Component({
  selector: 'app-notification-panel',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notification-panel.html',
  styleUrl: './notification-panel.css'
})
export class NotificationPanel
  implements OnInit, OnDestroy {

  @Output() closed = new EventEmitter<void>();
  @Output() unreadCountChanged =
    new EventEmitter<number>();

  notifications: AppNotification[] = [];
  unreadCount = 0;
  isLoading = false;

  private currentUserId: number | null = null;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user?.id) {
      this.currentUserId = user.id;
      this.load();
    }
  }

  ngOnDestroy(): void {}

  @HostListener('click', ['$event'])
  stopPropagation(event: Event): void {
    event.stopPropagation();
  }

  load(): void {
    if (!this.currentUserId) return;
    this.isLoading = true;

    this.notificationService
      .getNotifications(this.currentUserId)
      .subscribe({
        next: (data) => {
          this.notifications = data;
          this.unreadCount = data.filter(
            n => !n.isRead).length;
          this.unreadCountChanged.emit(
            this.unreadCount);
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
  }

  markAllRead(event: Event): void {
    event.stopPropagation();
    if (!this.currentUserId) return;

    this.notificationService
      .markAllAsRead(this.currentUserId)
      .subscribe({
        next: () => {
          this.notifications.forEach(
            n => n.isRead = true);
          this.unreadCount = 0;
          this.unreadCountChanged.emit(0);
          this.cdr.detectChanges();
        }
      });
  }

  onNotificationClick(
    event: Event,
    n: AppNotification): void {
    event.stopPropagation();

    if (!n.isRead) {
      this.notificationService
        .markAsRead(n.id)
        .subscribe({
          next: () => {
            n.isRead = true;
            this.unreadCount = Math.max(
              0, this.unreadCount - 1);
            this.unreadCountChanged.emit(
              this.unreadCount);
            this.cdr.detectChanges();
          }
        });
    }

    this.closed.emit();

    if (n.ticketId) {
      this.router.navigate(['/tickets', n.ticketId]);
    }
  }

  close(): void {
    this.closed.emit();
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

  getLabel(type: string): string {
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

    if (seconds < 60) return 'just now';
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) {
      return minutes + 'm ago';
    }
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return hours + 'h ago';
    const days = Math.floor(hours / 24);
    if (days === 1) return 'yesterday';
    if (days < 7) return days + 'd ago';
    return date.toLocaleDateString('en-GB', {
      day: 'numeric', month: 'short'
    });
  }
}