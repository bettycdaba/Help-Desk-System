import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } 
  from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { NavbarComponent } 
  from './shared/components/navbar/navbar';
import { ToastComponent } 
  from './shared/components/toast/toast';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet, 
    CommonModule,
    NavbarComponent, 
    ToastComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  title = 'helpdesk-frontend';
  showSidebar = false;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.showSidebar = !event.url.includes('/login');
    });
  }
}