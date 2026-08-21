import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { permissionGuard } from './core/guards/permission.guard';
import { notAdminGuard } from './core/guards/not-admin.guard';


export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () =>
      import('./features/landing/landing')
        .then(m => m.Landing)
  },
  {
    path: 'dash',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'admin/dashboard',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard')
        .then(m => m.Dashboard)
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login')
        .then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register')
        .then(m => m.Register)
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password')
        .then(m => m.ForgotPassword)
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password')
        .then(m => m.ResetPassword)
  },
  {
    path: 'tickets',
    canActivate: [authGuard, permissionGuard('VIEW_TICKETS')],
    loadComponent: () =>
      import('./features/tickets/ticket-list/ticket-list')
        .then(m => m.TicketList)
  },
  {
    path: 'tickets/new',
    canActivate: [authGuard, permissionGuard('CREATE_TICKET'), notAdminGuard],
    loadComponent: () =>
      import('./features/tickets/ticket-create/ticket-create')
        .then(m => m.TicketCreate)
  },
  {
    path: 'tickets/:id',
    canActivate: [authGuard, permissionGuard('VIEW_TICKETS')],
    loadComponent: () =>
      import('./features/tickets/ticket-detail/ticket-detail')
        .then(m => m.TicketDetail)
  },
  {
    path: 'users',
    canActivate: [authGuard, permissionGuard('VIEW_USERS')],
    loadComponent: () =>
      import('./features/users/users')
        .then(m => m.Users)
  },
  {
    path: 'departments',
    canActivate: [authGuard, permissionGuard('VIEW_DEPARTMENTS')],
    loadComponent: () =>
      import('./features/departments/departments')
        .then(m => m.Departments)
  },
  {
    path: 'roles',
    canActivate: [authGuard, permissionGuard('VIEW_ROLES')],
    loadComponent: () =>
      import('./features/roles/roles')
        .then(m => m.Roles)
  },
  {
    path: 'categories',
    canActivate: [authGuard, permissionGuard('VIEW_CATEGORIES')],
    loadComponent: () =>
      import('./features/categories/categories')
        .then(m => m.Categories)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/profile')
        .then(m => m.Profile)
  },
  {
    path: 'notifications',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/notifications/notifications')
        .then(m => m.Notifications)
  },
  {
  path: 'team-workload',
  canActivate: [authGuard, permissionGuard('VIEW_TICKETS')],
  loadComponent: () =>
    import('./features/team-workload/team-workload')
      .then(m => m.TeamWorkload)
},
  {
    path: '**',
    loadComponent: () =>
      import('./features/not-found/not-found')
        .then(m => m.NotFound)
  }
];