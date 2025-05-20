import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { UserEndpointService } from '../../../../rest_client';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-new-password',
  templateUrl: './new-password.component.html',
  styleUrls: ['./new-password.component.css'],
  standalone: true,
  imports: [FormsModule, RouterLink, AngularSvgIconModule, ButtonComponent, NgIf],
})
export class NewPasswordComponent implements OnInit {
  newPassword: string = '';
  confirmPassword: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  passwordTextType: boolean = false;

  constructor(private userEndpoint: UserEndpointService, private router: Router) {}

  ngOnInit(): void {}

  togglePasswordTextType(): void {
    this.passwordTextType = !this.passwordTextType;
  }

  submitPasswordChange(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.userEndpoint
      .changePassword({
        newPassword: this.newPassword,
      })
      .subscribe({
        next: () => {
          this.successMessage = 'Password successfully changed.';
          setTimeout(() => this.router.navigate(['/home']), 1500);
        },
        error: (err) => {
          if (err?.status === 403 && err?.error?.message?.includes('Admin password cannot be changed')) {
            this.errorMessage = 'Admin password cannot be changed.';
          } else if (err?.status === 403) {
            this.errorMessage = 'Access denied. You might not have permission.';
          } else if (err?.status === 400) {
            const validationErrors = err?.error?.['Validation errors'];
            if (Array.isArray(validationErrors) && validationErrors.length > 0) {
              this.errorMessage = validationErrors[0].split(' ').slice(1).join(' ');
            } else {
              this.errorMessage = 'Invalid request. Check your input.';
            }
          } else if (err?.status === 500) {
            this.errorMessage = 'Internal server error. Please try again later.';
          } else {
            this.errorMessage = err?.error?.message || 'Password change failed. Please try again.';
          }
        },
      });
  }
}
