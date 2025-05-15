import { NgClass, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.css'],
  imports: [FormsModule, ReactiveFormsModule, RouterLink, AngularSvgIconModule, NgIf, ButtonComponent, NgClass],
})
export class SignInComponent implements OnInit {
  form!: FormGroup;
  submitted = false;
  passwordTextType!: boolean;

  constructor(
    private readonly _formBuilder: FormBuilder,
    private _authService: AuthService,
    private readonly _router: Router,
    private readonly _toastr: ToastrService,
  ) {}

  onClick() {
    console.log('Button clicked');
  }

  ngOnInit(): void {
    this.form = this._formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  get f() {
    return this.form.controls;
  }

  togglePasswordTextType() {
    this.passwordTextType = !this.passwordTextType;
  }

  onSubmit() {
    this.submitted = true;
    const { email, password } = this.form.value;

    if (this.form.invalid) {
      return;
    }

    this._authService.loginUser({ email, password }).subscribe({
      next: (resp) => {
        const roles = this._authService.getUserRoles();

        if (roles.includes("ADMIN")) {
          this._router.navigateByUrl('/home/admin').then();
        } else if (roles.includes("EMPLOYEE")) {
          this._router.navigateByUrl('/home/employee').then();
        } else if (roles.includes("SUPERVISOR")) {
          this._router.navigateByUrl('/home/supervisor').then();
        } else {
          this._router.navigateByUrl('/home/employee').then();
        }
        //todo remove this ^^^^^^
      },
      error: (error) => {
        this._toastr.error('Login Failed');
      },
    });
  }
}
