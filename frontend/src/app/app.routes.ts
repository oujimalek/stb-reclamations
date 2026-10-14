import { Routes } from '@angular/router';
import { authGuard } from './core/auth.interceptor';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login.component').then(m => m.LoginComponent) },
  { path: 'portail', loadComponent: () => import('./pages/portail.component').then(m => m.PortailComponent) },
  {
    path: '', canActivate: [authGuard],
    loadComponent: () => import('./pages/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'tableau-de-bord' },
      { path: 'tableau-de-bord', loadComponent: () => import('./pages/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'reclamations', loadComponent: () => import('./pages/liste.component').then(m => m.ListeComponent) },
      { path: 'reclamations/nouvelle', loadComponent: () => import('./pages/nouvelle.component').then(m => m.NouvelleComponent) },
      { path: 'reclamations/:id', loadComponent: () => import('./pages/detail.component').then(m => m.DetailComponent) },
    ],
  },
  { path: '**', redirectTo: '' },
];
