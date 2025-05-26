import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-vacations',
  templateUrl: './departments.component.html',
  imports: [RouterOutlet],
})
export class DepartmentsComponent implements OnInit {
  constructor(
    private _authService: AuthService,
    private readonly _router: Router,
    private _toastr: ToastrService,
  ) {}

  ngOnInit(): void {
    const roles = this._authService.getUserRoles();

    if (roles.includes("ADMIN")) {
      this._router.navigateByUrl('/departments/admin').then();
    } else if (roles.includes("SUPERVISOR")) {
      this._router.navigateByUrl('/departments/supervisor').then();
    } else {
      this._router.navigateByUrl('/home').then();
    }
  }
}
