import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-auth',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  imports: [AngularSvgIconModule, RouterOutlet, NgIf],
})
export class ProfileComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit(): void {}

  isPasswordPage(): boolean {
    return this.router.url.includes('new-password');
  }
}
