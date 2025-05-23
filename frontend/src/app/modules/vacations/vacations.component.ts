import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router, RouterOutlet } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-vacations',
  templateUrl: './vacations.component.html',
  imports: [RouterOutlet],
})
export class VacationsComponent implements OnInit {
  constructor(
    private _authService: AuthService,
    private readonly _router: Router,
    private _toastr: ToastrService,
  ) {}

  ngOnInit(): void {
    const roles = this._authService.getUserRoles();

    if (roles.includes("SUPERVISOR")) {
      this._router.navigateByUrl('/vacations/supervisor').then();
    } else if (roles.includes("EMPLOYEE")) {
      this._router.navigateByUrl('/vacations/employee').then();
    } else {
      this._router.navigateByUrl('/home').then();
    }
  }
}
