import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { UserEndpointService } from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-new-password',
  templateUrl: './new-password.component.html',
  styleUrls: ['./new-password.component.css'],
  standalone: true,
  imports: [FormsModule, RouterLink, AngularSvgIconModule, ButtonComponent],
})
export class NewPasswordComponent implements OnInit {
  newPassword: string = '';
  confirmPassword: string = '';
  successMessage: string = '';
  passwordTextType: boolean = false;

  constructor(private userEndpoint: UserEndpointService, private router: Router, private toastrService: ToastrService) {}

  ngOnInit(): void {}

  togglePasswordTextType(): void {
    this.passwordTextType = !this.passwordTextType;
  }

  submitPasswordChange(): void {
    this.successMessage = '';

    if (this.newPassword !== this.confirmPassword) {
      return;
    }

    this.userEndpoint
      .changePassword({
        newPassword: this.newPassword,
      })
      .subscribe({
        next: () => {
          this.toastrService.success("Password changes successfully");
          this.router.navigate(['/home']).then();
        },
      });
  }
}
