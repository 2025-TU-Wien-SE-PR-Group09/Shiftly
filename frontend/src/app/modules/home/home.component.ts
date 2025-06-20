import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  imports: [RouterOutlet],
})
export class HomeComponent implements OnInit {
  constructor(private _authService: AuthService, private readonly _router: Router) {}

  ngOnInit(): void {
    const roles = this._authService.getUserRoles();

    if (roles.includes('ADMIN')) {
      this._router.navigateByUrl('/home/admin').then();
    } else if (roles.includes('EMPLOYEE') || roles.includes('JUMPER')) {
      this._router.navigateByUrl('/home/employee').then();
    } else if (roles.includes('SUPERVISOR')) {
      this._router.navigateByUrl('/home/supervisor').then();
    } else {
      this._router.navigateByUrl('/no-role').then();
    }
  }
}
