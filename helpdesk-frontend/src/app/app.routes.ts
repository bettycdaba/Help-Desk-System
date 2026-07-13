import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login')
        .then(m => m.Login)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard')
        .then(m => m.Dashboard)
  },
  {
    path: 'tickets',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tickets/ticket-list/ticket-list')
        .then(m => m.TicketList)
  },
  {
    path: 'tickets/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tickets/ticket-create/ticket-create')
        .then(m => m.TicketCreate)
  },
  {
    path: 'tickets/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tickets/ticket-detail/ticket-detail')
        .then(m => m.TicketDetail)
  },
  {
    path: 'users',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/users/users')
        .then(m => m.Users)
  },
  {
    path: 'departments',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/departments/departments')
        .then(m => m.Departments)
  },
  {
    path: 'roles',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/roles/roles')
        .then(m => m.Roles)
  },
  {
    path: 'categories',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/categories/categories')
        .then(m => m.Categories)
  },
  {
    path: '**',
    loadComponent: () =>
      import('./features/not-found/not-found')
        .then(m => m.NotFound)
  }
];