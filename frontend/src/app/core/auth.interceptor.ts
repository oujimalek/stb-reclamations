import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.session()?.token;
  const r = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(r).pipe(catchError(err => {
    if (err.status === 401 && token) auth.logout();
    return throwError(() => err);
  }));
};

export const authGuard = () => {
  const auth = inject(AuthService);
  if (auth.session()) return true;
  auth.logout();
  return false;
};
