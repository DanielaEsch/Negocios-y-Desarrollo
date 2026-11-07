import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { AreaResumen } from '../../../models/auth/session.model';
import { ApiError } from '../../../models/api/problem-detail.model';
import { Frecuencia, Rutina, RutinaRequest } from '../../../models/rutina/rutina.model';
import { RutinaService } from '../../../services/rutina/rutina.service';
import { hora, oracion } from '../../../utils/format.util';
import { describirFrecuencia } from '../../../utils/frecuencia.util';

export interface RutinaDialogData {
  rutina: Rutina | null;
  areas: AreaResumen[];
}

/** Limites de SaveRutinaRequestVO (RutinaConstant). */
const MAX_NOMBRE = 100;
const MAX_DESCRIPCION = 250;
const MAX_TAREA = 150;
const MAX_TAREAS = 30;

/** Que campos de horario pide cada frecuencia; replica las reglas del dominio. */
const CAMPOS: Record<Frecuencia, { inicio: boolean; fin: boolean; dia: boolean }> = {
  CADA_2_HORAS: { inicio: true, fin: true, dia: false },
  DIARIA: { inicio: true, fin: false, dia: false },
  SEMANAL: { inicio: true, fin: false, dia: true },
  POR_EVENTO: { inicio: false, fin: false, dia: false },
};

/**
 * Alta y edicion de rutina. La frecuencia se elige primero y el formulario muestra solo
 * los campos que esa frecuencia necesita; abajo se lee en una frase como quedo.
 */
@Component({
  selector: 'app-rutina-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe,
  ],
  templateUrl: './rutina-dialog.html',
  styleUrl: './rutina-dialog.scss',
})
export class RutinaDialog {
  protected readonly data = inject<RutinaDialogData>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<RutinaDialog, Rutina>);
  private readonly rutinaService = inject(RutinaService);
  private readonly translate = inject(TranslateService);

  protected readonly max = {
    nombre: MAX_NOMBRE,
    descripcion: MAX_DESCRIPCION,
    tarea: MAX_TAREA,
    tareas: MAX_TAREAS,
  };
  protected readonly frecuencias: Frecuencia[] = [
    'CADA_2_HORAS',
    'DIARIA',
    'SEMANAL',
    'POR_EVENTO',
  ];
  protected readonly dias = [1, 2, 3, 4, 5, 6, 7];
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly oracion = oracion;

  private readonly rutina = this.data.rutina;

  protected readonly form = new FormGroup({
    idArea: new FormControl<number | null>(
      this.rutina?.idArea ?? this.data.areas[0]?.id ?? null,
      Validators.required,
    ),
    nombre: new FormControl(oracion(this.rutina?.nombre), {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(MAX_NOMBRE)],
    }),
    descripcion: new FormControl(oracion(this.rutina?.descripcion), {
      nonNullable: true,
      validators: [Validators.maxLength(MAX_DESCRIPCION)],
    }),
    frecuencia: new FormControl<Frecuencia>(this.rutina?.frecuencia ?? 'DIARIA', {
      nonNullable: true,
    }),
    horaInicio: new FormControl(hora(this.rutina?.horaInicio) || '08:00', { nonNullable: true }),
    horaFin: new FormControl(hora(this.rutina?.horaFin) || '20:00', { nonNullable: true }),
    diaSemana: new FormControl<number>(this.rutina?.diaSemana ?? 1, { nonNullable: true }),
    tareas: new FormArray(
      (this.rutina?.tareas.map((tarea) => tarea.nombre) ?? ['']).map((nombre) =>
        this.tareaControl(nombre),
      ),
    ),
  });

  private readonly valores = toSignal(this.form.valueChanges, {
    initialValue: this.form.getRawValue(),
  });
  protected readonly campos = computed(() => CAMPOS[this.valores().frecuencia ?? 'DIARIA']);
  protected readonly frase = computed(() => {
    const valores = this.valores();
    return describirFrecuencia(
      {
        frecuencia: valores.frecuencia ?? 'DIARIA',
        horaInicio: valores.horaInicio ?? null,
        horaFin: valores.horaFin ?? null,
        diaSemana: valores.diaSemana ?? null,
      },
      this.translate,
    );
  });

  protected get tareas(): FormArray<FormControl<string>> {
    return this.form.controls.tareas;
  }

  protected agregarTarea(): void {
    if (this.tareas.length < MAX_TAREAS) {
      this.tareas.push(this.tareaControl(''));
    }
  }

  protected quitarTarea(indice: number): void {
    if (this.tareas.length > 1) {
      this.tareas.removeAt(indice);
    }
  }

  protected subir(indice: number): void {
    if (indice > 0) {
      const control = this.tareas.at(indice);
      this.tareas.removeAt(indice);
      this.tareas.insert(indice - 1, control);
    }
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valores = this.form.getRawValue();
    const campos = CAMPOS[valores.frecuencia];
    const body: RutinaRequest = {
      idArea: valores.idArea as number,
      nombre: valores.nombre.trim(),
      descripcion: valores.descripcion.trim() || null,
      frecuencia: valores.frecuencia,
      horaInicio: campos.inicio ? valores.horaInicio : null,
      horaFin: campos.fin ? valores.horaFin : null,
      diaSemana: campos.dia ? valores.diaSemana : null,
      tareas: valores.tareas.map((tarea) => tarea.trim()).filter(Boolean),
    };
    const peticion: Observable<Rutina> = this.rutina
      ? this.rutinaService.update(this.rutina.id, body)
      : this.rutinaService.create(body);
    this.guardando.set(true);
    this.error.set(null);
    peticion.subscribe({
      next: (rutina) => this.ref.close(rutina),
      error: (error: unknown) => {
        this.guardando.set(false);
        if (error instanceof ApiError && error.status < 500) {
          this.error.set(error.problem.invalid_params?.[0]?.reason ?? error.message);
        }
      },
    });
  }

  private tareaControl(nombre: string): FormControl<string> {
    return new FormControl(nombre, {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(MAX_TAREA)],
    });
  }
}
