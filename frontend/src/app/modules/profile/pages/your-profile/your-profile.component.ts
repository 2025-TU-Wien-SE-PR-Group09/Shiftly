import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { AuthService } from '../../../../core/services/auth.service';
import { UserEndpointService, UserProfileRestDto } from '../../../../rest_client';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-your-profile',
  imports: [ButtonComponent, NgIf],
  templateUrl: './your-profile.component.html',
  styleUrl: './your-profile.component.css',
})
export class YourProfileComponent implements OnInit {
  userData: Partial<UserProfileRestDto> = {
    name: '',
    email: '',
    role: '',
    department: ''
  };

  constructor(private router: Router,
              private userEndpoint: UserEndpointService) {}


  ngOnInit(): void {
    this.userEndpoint.getCurrentUserProfile().subscribe({
      next: (data) => {
        this.userData = data;
      },
      error: (err) => {
        console.error('Failed to load user profile', err);
      },
    });
  }

  changePassword(): void {
    this.router.navigate(['/profile/new-password']);
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  isAdmin(): boolean {
    return this.userData.role === 'ADMIN';
  }

}
