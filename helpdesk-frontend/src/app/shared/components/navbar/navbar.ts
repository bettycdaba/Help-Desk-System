import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive }
  from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService }
  from '../../../core/services/auth.service';
import { LoginResponse }
  from '../../../core/models/auth.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent implements OnInit {

  currentUser: LoginResponse | null = null;
  isCollapsed = false;

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  getDashboardRoute(): string {
    return '/dashboard';
  }

  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}