import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ButtonComponent } from '../../shared/components/button/button.component';

@Component({
  selector: 'app-error',
  imports: [AngularSvgIconModule, ButtonComponent, RouterLink],
  templateUrl: './no-role.component.html',
})
export class NoRoleComponent {}
