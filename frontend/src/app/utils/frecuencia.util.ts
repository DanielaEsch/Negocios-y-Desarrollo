import { TranslateService } from '@ngx-translate/core';
import { Rutina } from '../models/rutina/rutina.model';
import { hora } from './format.util';

/**
 * La frecuencia como la diria una persona: "Cada 2 horas, de 08:00 a 20:00",
 * "Cada lunes a las 09:00". Las claves viven en frecuencia.texto.* de i18n.
 */
export function describirFrecuencia(
  rutina: Pick<Rutina, 'frecuencia' | 'horaInicio' | 'horaFin' | 'diaSemana'>,
  translate: TranslateService,
): string {
  return translate.instant('frecuencia.texto.' + rutina.frecuencia, {
    inicio: hora(rutina.horaInicio),
    fin: hora(rutina.horaFin),
    dia: rutina.diaSemana ? translate.instant('dias.' + rutina.diaSemana) : '',
  });
}
