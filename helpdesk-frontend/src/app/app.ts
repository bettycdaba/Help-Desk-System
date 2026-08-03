// // 

// import { Component, OnInit, OnDestroy } 
//   from '@angular/core';
// import { Router, RouterOutlet, NavigationEnd }
//   from '@angular/router';
// import { CommonModule } from '@angular/common';
// import { filter } from 'rxjs/operators';
// import { NavbarComponent }
//   from './shared/components/navbar/navbar';
// import { ToastComponent }
//   from './shared/components/toast/toast';
// import { IdleService }
//   from './core/services/idle.service';
// import { AuthService }
//   from './core/services/auth.service';

// @Component({
//   selector: 'app-root',
//   standalone: true,
//   imports: [
//     RouterOutlet,
//     CommonModule,
//     NavbarComponent,
//     ToastComponent
//   ],
//   templateUrl: './app.html',
//   styleUrl: './app.css'
// })
// export class App implements OnInit, OnDestroy {
//   title = 'helpdesk-frontend';
//   showSidebar = false;

//   constructor(
//     private router: Router,
//     private idleService: IdleService,
//     private authService: AuthService
//   ) {}

//   // ngOnInit(): void {
//   //   this.router.events.pipe(
//   //     filter(event => event instanceof NavigationEnd)
//   //   ).subscribe((event: any) => {
//   //     const hideSidebarRoutes = [
//   //       '/login', '/home', '/register', 
//   //       '/forgot-password', '/reset-password'
//   //     ];
//   //     this.showSidebar = !hideSidebarRoutes.some(
//   //       route => event.url.includes(route));

//   //     if (this.authService.isLoggedIn()) {
//   //       this.idleService.startWatching();
//   //     } else {
//   //       this.idleService.stopWatching();
//   //     }
//   //   });
//   // }
//   ngOnInit(): void {
//     this.checkRoute(this.router.url);

//     this.router.events.pipe(
//       filter(event => event instanceof NavigationEnd)
//     ).subscribe((event: any) => {
//       this.checkRoute(event.url);

//       if (this.authService.isLoggedIn()) {
//         this.idleService.startWatching();
//       } else {
//         this.idleService.stopWatching();
//       }
//     });
//   }

//   private checkRoute(url: string): void {
//     const hideSidebarRoutes = [
//       '/login',
//       '/home',
//       '/register',
//       '/forgot-password',
//       '/reset-password'
//     ];
//     this.showSidebar = !hideSidebarRoutes.some(
//       route => url === route ||
//               url.startsWith(route + '?') ||
//               url.startsWith(route + '#')
//     );
//   }

//   ngOnDestroy(): void {
//     this.idleService.stopWatching();
//   }
// }


import { Component, OnInit, OnDestroy }
  from '@angular/core';
import { Router, RouterOutlet, NavigationEnd }
  from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { NavbarComponent }
  from './shared/components/navbar/navbar';
import { ToastComponent }
  from './shared/components/toast/toast';
import { IdleService }
  from './core/services/idle.service';
import { AuthService }
  from './core/services/auth.service';

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
export class App implements OnInit, OnDestroy {
  title = 'helpdesk-frontend';
  showSidebar = false;

  private hiddenRoutes = [
    '/home',
    '/login',
    '/register',
    '/forgot-password',
    '/reset-password'
  ];

  constructor(
    private router: Router,
    private idleService: IdleService,
    private authService: AuthService
  ) {
    this.showSidebar = this.shouldShowSidebar(
      window.location.pathname);
  }

  ngOnInit(): void {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.showSidebar = this.shouldShowSidebar(event.url);

      if (this.authService.isLoggedIn()) {
        this.idleService.startWatching();
      } else {
        this.idleService.stopWatching();
      }
    });
  }

  private shouldShowSidebar(url: string): boolean {
    return !this.hiddenRoutes.some(
      route =>
        url === route ||
        url.startsWith(route + '?') ||
        url.startsWith(route + '#') ||
        url === '/'
    );
  }

  ngOnDestroy(): void {
    this.idleService.stopWatching();
  }
}