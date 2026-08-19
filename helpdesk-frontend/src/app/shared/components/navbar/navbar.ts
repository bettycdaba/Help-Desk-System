import {
  Component, OnInit, OnDestroy,
  ChangeDetectorRef, HostListener
} from '@angular/core';
import { Router, RouterLink, RouterLinkActive }
  from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService }
  from '../../../core/services/auth.service';
import { NotificationService }
  from '../../../core/services/notification.service';
import { AppNotification }
  from '../../../core/models/notification.model';
import { LoginResponse }
  from '../../../core/models/auth.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent implements OnInit, OnDestroy {

  currentUser: LoginResponse | null = null;
  isCollapsed = false;

  notifications: AppNotification[] = [];
  unreadCount = 0;
  showPanel = false;
  isLoadingNotifications = false;

  private pollInterval: any;

  constructor(
    public authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user?.id) {
        this.loadNotifications();
        this.startPolling();
      } else {
        this.stopPolling();
      }
      this.cdr.detectChanges();
    });
    this.router.events.subscribe(event => {
      if (event.constructor.name === 'NavigationEnd' && this.currentUser?.id) {
        this.refreshCount();
      }
    });
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  startPolling(): void {
    this.stopPolling();
    this.pollInterval = setInterval(() => {
      if (this.currentUser?.id) {
        this.refreshCount();
      }
    }, 2000);
  }

  stopPolling(): void {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
      this.pollInterval = null;
    }
  }

  loadNotifications(): void {
    if (!this.currentUser?.id) return;
    this.isLoadingNotifications = true;

    this.notificationService
      .getNotifications(this.currentUser.id)
      .subscribe({
        next: (data) => {
          this.notifications = data;
          this.unreadCount = data.filter(
            n => !n.isRead).length;
          this.isLoadingNotifications = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.isLoadingNotifications = false;
        }
      });
  }

  refreshCount(): void {
    if (!this.currentUser?.id) return;
    this.notificationService
      .getUnreadCount(this.currentUser.id)
      .subscribe({
        next: (res) => {
          this.unreadCount = res.count;
          this.cdr.detectChanges();
        },
        error: () => {}
      });
  }

  togglePanel(event: Event): void {
    event.stopPropagation();
    this.showPanel = !this.showPanel;
    if (this.showPanel) {
      this.loadNotifications();
    }
    this.cdr.detectChanges();
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    if (this.showPanel) {
      this.showPanel = false;
      this.cdr.detectChanges();
    }
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
            this.cdr.detectChanges();
          }
        });
    }

    this.showPanel = false;
    if (n.ticketId) {
      this.router.navigate(['/tickets', n.ticketId]);
    }
  }

  markAllRead(event: Event): void {
    event.stopPropagation();
    if (!this.currentUser?.id) return;

    this.notificationService
      .markAllAsRead(this.currentUser.id)
      .subscribe({
        next: () => {
          this.notifications.forEach(n => n.isRead = true);
          this.unreadCount = 0;
          this.cdr.detectChanges();
        }
      });
  }

  getIcon(type: string): string {
    switch (type) {
      case 'assign':   return 'bi-person-check-fill';
      case 'status':   return 'bi-arrow-repeat';
      case 'comment':  return 'bi-chat-dots-fill';
      case 'resolved': return 'bi-check-circle-fill';
      case 'new_ticket': return 'bi-plus-circle-fill';
      default:         return 'bi-bell-fill';
    }
  }

  getIconClass(type: string): string {
    switch (type) {
      case 'assign':   return 'icon-assign';
      case 'status':   return 'icon-status';
      case 'comment':  return 'icon-comment';
      case 'resolved': return 'icon-resolved';
      case 'new_ticket': return 'icon-new';
      default:         return 'icon-default';
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
    if (hours < 24) {
      return hours + 'h ago';
    }
    const days = Math.floor(hours / 24);
    if (days === 1) return 'yesterday';
    if (days < 7) return days + 'd ago';
    return date.toLocaleDateString();
  }

  isActive(): boolean {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      return JSON.parse(stored).active !== false;
    }
    return true;
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  getDashboardRoute(): string {
    return this.authService.isAdmin()
      ? '/admin/dashboard'
      : '/dashboard';
  }

  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
  isSupervisor(): boolean {
    return this.authService.isSupervisor();
}
}