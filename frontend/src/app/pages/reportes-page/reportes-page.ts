import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Incidencia } from '../../models/incidencia/incidencia.model';
import { IncidenciaService } from '../../services/incidencia/incidencia.service';
import { LanguageService } from '../../services/language/language.service';
import { hora, oracion } from '../../utils/format.util';

/** Pasos que ve el operador; coinciden con la maquina de estados de Incidencia. */
const PASOS = ['PENDIENTE', 'REVISION', 'RESUELTA'] as const;

/**
 * Lo que el operador reporto y en que va cada cosa. El backend solo le devuelve sus
 * propios reportes, asi que no hay filtros: es una lista corta, lo mas reciente arriba.
 */
@Component({
  selector: 'app-reportes-page',
  imports: [DatePipe, RouterLink, MatIconModule, TranslatePipe],
  templateUrl: './reportes-page.html',
  styleUrl: './reportes-page.scss',
})
export class ReportesPage implements OnInit {
  private readonly incidenciaService = inject(IncidenciaService);
  protected readonly language = inject(LanguageService);

  protected readonly incidencias = signal<Incidencia[]>([]);
  protected readonly cargado = signal(false);
  protected readonly pasos = PASOS;
  protected readonly hora = hora;
  protected readonly oracion = oracion;

  ngOnInit(): void {
    this.incidenciaService.findList({}, 50).subscribe({
      next: (incidencias) => {
        this.incidencias.set(incidencias);
        this.cargado.set(true);
      },
      error: () => this.cargado.set(true),
    });
  }

  /** Un paso esta hecho si la incidencia ya llego a el o lo paso. */
  protected alcanzado(incidencia: Incidencia, paso: string): boolean {
    return PASOS.indexOf(incidencia.estado) >= PASOS.indexOf(paso as (typeof PASOS)[number]);
  }
}
