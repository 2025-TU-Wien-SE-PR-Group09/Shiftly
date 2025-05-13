import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-vacations',
  templateUrl: './vacations.component.html',
  imports: [RouterOutlet],
})
export class VacationsComponent implements OnInit {
  constructor(private _authService: AuthService) {}

  ngOnInit(): void {}
}
