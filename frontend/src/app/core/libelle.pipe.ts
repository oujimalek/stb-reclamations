import { Pipe, PipeTransform } from '@angular/core';
import { LIBELLES } from './models';

@Pipe({ name: 'libelle', standalone: true })
export class LibellePipe implements PipeTransform {
  transform(v: string | null | undefined): string { return v ? (LIBELLES[v] ?? v) : '—'; }
}
