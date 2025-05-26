import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router, RouterOutlet } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-sick-notes',
  templateUrl: './sick-notes.component.html',
  imports: [RouterOutlet],
})
export class SickNotesComponent implements OnInit {
  constructor(
    private _authService: AuthService,
    private readonly _router: Router,
    private _toastr: ToastrService,
  ) {}

  ngOnInit(): void {
    const roles = this._authService.getUserRoles();

    if (roles.includes("SUPERVISOR")) {
      this._router.navigateByUrl('/sick-notes/supervisor').then();
    } else if (roles.includes("EMPLOYEE")) {
      this._router.navigateByUrl('/sick-notes/employee').then();
    } else {
      this._router.navigateByUrl('/home').then();
    }
  }
}
