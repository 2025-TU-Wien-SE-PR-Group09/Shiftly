import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AngularSvgIconModule } from 'angular-svg-icon';

@Component({
  selector: 'app-error',
  imports: [AngularSvgIconModule],
  templateUrl: './no-role.component.html',
})
export class NoRoleComponent {}
