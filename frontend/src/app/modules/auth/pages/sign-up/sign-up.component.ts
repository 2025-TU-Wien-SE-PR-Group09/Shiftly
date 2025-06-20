import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { ToastrService } from 'ngx-toastr';
import { CommonModule, NgClass } from '@angular/common';
import { RegisterTokenRequestDto, RegistrationEndpointService } from '../../../../rest_client';

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
  token: string | null = null;
  isValidToken = false;

  constructor(
    private readonly _formBuilder: FormBuilder,
    private _registrationEndpoint: RegistrationEndpointService,
    private readonly _router: Router,
    private readonly _toastr: ToastrService,
    private readonly _route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Token aus URL-Parameter auslesen
    this._route.queryParams.subscribe(params => {
      this.token = params['token'];
      this.isValidToken = !!this.token;

      if (!this.isValidToken) {
        // Wenn kein Token vorhanden ist, zur Login-Seite weiterleiten
        this._toastr.error('Registration requires a valid invitation token. Please use the link from your invitation email.');
        this._router.navigateByUrl('/auth/sign-in');
      }

      this.initializeForm();
    });
  }

  initializeForm(): void {
    // Grundformular erstellen ohne Auth-Code
    this.form = this._formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
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

    if (this.form.invalid || !this.token) {
      return;
    }

    const userData: RegisterTokenRequestDto = {
      email: this.form.value.email,
      password: this.form.value.password,
      firstName: this.form.value.firstName,
      lastName: this.form.value.lastName
    };

    // Token-basierte Registrierung
    this._registrationEndpoint.registerWithToken(this.token, userData).subscribe({
      next: (resp) => {
        this._toastr.success('Registration successful');
        this._router.navigateByUrl('/auth/sign-in').then();
      },
      error: (error) => {
        if (error.status === 400) {
          this._toastr.error('Registration failed. Token may be expired or already used.');
        } else {
          this._toastr.error('Registration failed. Please try again later.');
        }
      },
    });
  }
}
