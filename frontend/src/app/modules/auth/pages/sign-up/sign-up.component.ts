import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { ToastrService } from 'ngx-toastr';
import { CommonModule, NgClass } from '@angular/common';
import { RegistrationEndpointService } from '../../../../rest_client';

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.css'],
  imports: [FormsModule, RouterLink, AngularSvgIconModule, ButtonComponent, ReactiveFormsModule, NgClass, CommonModule],
})
export class SignUpComponent implements OnInit {
  form!: FormGroup;
  submitted = false;
  passwordTextType!: boolean;

  constructor(
    private readonly _formBuilder: FormBuilder,
    private _registrationEndpoint: RegistrationEndpointService,
    private readonly _router: Router,
    private readonly _toastr: ToastrService,
  ) {}

  ngOnInit(): void {
    this.form = this._formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      authCode: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(6)
      ]],
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
    const { email, password, authCode } = this.form.value;

    console.log(email, password, authCode);
    if (this.form.invalid) {
      return;
    }

    this._registrationEndpoint.registerUser({email, password, code: authCode}).subscribe({
      next: (resp) => {
        this._toastr.success('Registration successful');
        this._router.navigateByUrl('/login').then();
      },
      error: (error) => {
        this._toastr.error('Registration failed');
      },
    })

  }
}
