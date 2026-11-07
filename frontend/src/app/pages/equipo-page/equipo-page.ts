import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { TranslatePipe } from '@ngx-translate/core';
import { filter, forkJoin, Observable } from 'rxjs';
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
} from '../../components/confirm-dialog-component/confirm-dialog-component';
import { ApiError } from '../../models/api/problem-detail.model';
import { AreaResumen, Rol } from '../../models/auth/session.model';
import { UsuarioAdmin } from '../../models/usuario/usuario.model';
import { AreaService } from '../../services/area/area.service';
import { AuthService } from '../../services/auth/auth.service';
import { LanguageService } from '../../services/language/language.service';
import { NotificationService } from '../../services/notification/notification.service';
import { UsuarioService } from '../../services/usuario/usuario.service';
import { iniciales, oracion } from '../../utils/format.util';
import { PersonaDialog, PersonaDialogData } from './persona-dialog';

const ORDEN: Rol[] = ['ADMINISTRADOR', 'SUPERVISOR', 'OPERADOR'];

/**
 * Quien trabaja en el negocio y que areas cubre. La promocion a supervisor la decide el
 * dominio: si a la persona le falta un area o retiro su consentimiento, el backend lo dice.
 */
@Component({
  selector: 'app-equipo-page',
  imports: [DatePipe, MatButtonModule, MatIconModule, MatMenuModule, TranslatePipe],
  templateUrl: './equipo-page.html',
  styleUrl: './equipo-page.scss',
})
export class EquipoPage implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly areaService = inject(AreaService);
  private readonly notification = inject(NotificationService);
  private readonly dialog = inject(MatDialog);
  protected readonly auth = inject(AuthService);
  protected readonly language = inject(LanguageService);

  protected readonly usuarios = signal<UsuarioAdmin[]>([]);
  protected readonly areas = signal<AreaResumen[]>([]);
  protected readonly iniciales = iniciales;
  protected readonly oracion = oracion;

  protected readonly grupos = computed(() =>
    ORDEN.map((perfil) => ({
      perfil,
      usuarios: this.usuarios().filter((usuario) => usuario.perfil === perfil),
    })).filter((grupo) => grupo.usuarios.length),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected agregar(): void {
    this.abrir({ usuario: null, areas: this.areas() }, 'equipo.agregado');
  }

  protected cambiarAreas(usuario: UsuarioAdmin): void {
    this.abrir({ usuario, areas: this.areas() }, 'equipo.areasGuardadas');
  }

  protected promover(usuario: UsuarioAdmin): void {
    this.confirmar(
      {
        titleKey: 'equipo.promoverTitulo',
        messageKey: 'equipo.promoverTexto',
        params: { name: usuario.nombre },
        confirmKey: 'equipo.promover',
      },
      () => this.usuarioService.promover(usuario.id),
      'equipo.promovido',
      usuario.nombre,
    );
  }

  protected desactivar(usuario: UsuarioAdmin): void {
    this.confirmar(
      {
        titleKey: 'equipo.desactivarTitulo',
        messageKey: 'equipo.desactivarTexto',
        params: { name: usuario.nombre },
        confirmKey: 'equipo.desactivar',
      },
      () => this.usuarioService.delete(usuario.id),
      'equipo.desactivado',
      usuario.nombre,
    );
  }

  private abrir(data: PersonaDialogData, mensaje: string): void {
    this.dialog
      .open(PersonaDialog, { data, width: '520px', maxWidth: 'calc(100vw - 32px)' })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe((usuario) => {
        this.notification.success(mensaje, { name: usuario.nombre });
        this.cargar();
      });
  }

  private confirmar(
    data: ConfirmDialogData,
    accion: () => Observable<unknown>,
    mensaje: string,
    nombre: string,
  ): void {
    this.dialog
      .open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, { data })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() =>
        accion().subscribe({
          next: () => {
            this.notification.success(mensaje, { name: nombre });
            this.cargar();
          },
          error: (error: unknown) => {
            if (error instanceof ApiError && error.status < 500 && error.status !== 0) {
              this.notification.raw(error.message);
            }
          },
        }),
      );
  }

  private cargar(): void {
    forkJoin({
      usuarios: this.usuarioService.findList(),
      areas: this.areaService.findList({}, { page: 1, size: 100 }),
    }).subscribe(({ usuarios, areas }) => {
      this.usuarios.set(usuarios);
      this.areas.set(
        areas.data
          .map(({ id, nombre }) => ({ id, nombre }))
          .sort((a, b) => a.nombre.localeCompare(b.nombre)),
      );
    });
  }
}
