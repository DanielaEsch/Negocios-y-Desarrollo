import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, Observable, tap } from 'rxjs';
import { Endpoints } from '../../../environments/endpoints';
import { AppConfig } from '../../constants/app-config';
import { SingleResponse } from '../../models/api/api-response.model';
import { AuthToken, LoginRequest, Rol, Session } from '../../models/auth/session.model';
import { ConsumeService } from '../consume/consume.service';

/**
 * Sesion contra POST /auth/login. Guarda el token en sessionStorage (dura lo que la
 * pestana) y el interceptor lo manda como Authorization: Bearer.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly router = inject(Router);
  private readonly consume = inject(ConsumeService);
  private readonly current = signal<Session | null>(this.restore());

  readonly session = this.current.asReadonly();
  readonly usuario = computed(() => this.current()?.usuario ?? null);
  readonly isAuthenticated = computed(() => {
    const session = this.current();
    return session !== null && new Date(session.expiraEn).getTime() > Date.now();
  });

  login(request: LoginRequest): Observable<Session> {
    return this.consume
      .httpPost<SingleResponse<AuthToken>>(Endpoints.login, {
        correo: request.correo.trim().toLowerCase(),
        contrasena: request.contrasena,
      })
      .pipe(
        map((response) => response.data),
        tap((session) => {
          this.current.set(session);
          this.persist(session);
        }),
      );
  }

  logout(): void {
    this.current.set(null);
    this.persist(null);
    void this.router.navigate(['/login']);
  }

  hasRole(...roles: Rol[]): boolean {
    const perfil = this.current()?.usuario.perfil;
    return perfil !== undefined && roles.includes(perfil);
  }

  /** Pantalla de inicio de cada perfil: el operador va a su turno; los demas, al tablero. */
  home(): string {
    return this.hasRole('OPERADOR') ? '/turno' : '/tablero';
  }

  private restore(): Session | null {
    try {
      const raw = sessionStorage.getItem(AppConfig.storage.session);
      const session = raw ? (JSON.parse(raw) as Session) : null;
      return session?.token && session.usuario ? session : null;
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
