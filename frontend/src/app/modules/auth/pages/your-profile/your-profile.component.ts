import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-your-profile',
  imports: [ButtonComponent],
  templateUrl: './your-profile.component.html',
  styleUrl: './your-profile.component.css',
})
export class YourProfileComponent implements OnInit {
  userData = {
    name: '',
    role: '',
    department: '',
    email: '',
  };

  constructor(private router: Router,
              private authService: AuthService) {}


  ngOnInit(): void {
    const roles = this.authService.getUserRoles();
    this.userData.email = this.authService.getUserEmail();
    this.userData.role = roles.length > 0 ? roles[0] : 'Unknown';
    this.userData.name = this.userData.email.split('@')[0];
  }

  changePassword(): void {
    this.router.navigate(['/auth/new-password']);
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

}
