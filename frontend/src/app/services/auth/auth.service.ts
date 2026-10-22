import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { delay, Observable, of, tap } from 'rxjs';
import { AppConfig } from '../../constants/app-config';
import { LoginRequest, Rol, Session } from '../../models/auth/session.model';

/**
 * Sesion SIMULADA. El backend aun no tiene Spring Security: cualquier correo con una
 * contrasena valida entra con el rol elegido. Cuando exista el endpoint de login solo
 * cambia `login()`; guards, interceptor y menu ya leen la sesion desde aqui.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly router = inject(Router);
  private readonly current = signal<Session | null>(this.restore());

  readonly session = this.current.asReadonly();
  readonly isAuthenticated = computed(() => this.current() !== null);

  login(request: LoginRequest): Observable<Session> {
    const session: Session = {
      nombre: this.nameFromEmail(request.correo),
      correo: request.correo.trim().toLowerCase(),
      rol: request.rol,
    };
    return of(session).pipe(
      delay(300),
      tap((value) => {
        this.current.set(value);
        this.persist(value);
      }),
    );
  }

  logout(): void {
    this.current.set(null);
    this.persist(null);
    void this.router.navigate(['/login']);
  }

  hasRole(...roles: Rol[]): boolean {
    const rol = this.current()?.rol;
    return rol !== undefined && roles.includes(rol);
  }

  private nameFromEmail(correo: string): string {
    const user = correo.split('@')[0] ?? '';
    return user
      .split(/[._-]/)
      .filter(Boolean)
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private restore(): Session | null {
    try {
      const raw = sessionStorage.getItem(AppConfig.storage.session);
      return raw ? (JSON.parse(raw) as Session) : null;
    } catch {
      return null;
    }
  }

  private persist(session: Session | null): void {
    try {
      if (session) {
        sessionStorage.setItem(AppConfig.storage.session, JSON.stringify(session));
      } else {
        sessionStorage.removeItem(AppConfig.storage.session);
      }
    } catch {
      // Sin almacenamiento (modo privado): la sesion vive solo en memoria.
    }
  }
}
