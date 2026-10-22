import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { EmptyComponent } from '../../components/empty-component/empty-component';

@Component({
  selector: 'app-not-found-page',
  imports: [RouterLink, MatButtonModule, TranslatePipe, EmptyComponent],
  template: `
    <app-empty-component icon="explore_off" [message]="'notFound.message' | translate" />
    <div class="actions">
      <a mat-flat-button routerLink="/inicio">{{ 'notFound.back' | translate }}</a>
    </div>
  `,
  styles: `
    .actions {
      display: flex;
      justify-content: center;
    }
  `,
})
export class NotFoundPage {}
