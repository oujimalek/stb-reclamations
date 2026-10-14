import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Session } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  session = signal<Session | null>(this.lire());

  login(email: string, motDePasse: string) {
    return this.http.post<Session>(`${environment.apiUrl}/auth/login`, { email, motDePasse }).pipe(
      tap(s => { localStorage.setItem('session', JSON.stringify(s)); this.session.set(s); }));
  }

  logout() {
    localStorage.removeItem('session');
    this.session.set(null);
    this.router.navigate(['/login']);
  }

  estSuperviseur() { const r = this.session()?.role; return r === 'SUPERVISEUR' || r === 'ADMIN'; }

  private lire(): Session | null {
    try { return JSON.parse(localStorage.getItem('session') ?? 'null'); } catch { return null; }
  }
}
