import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { NgIf } from '@angular/common';
import { SupervisorVacationsComponent } from './pages/supervisor-vacations/supervisor-vacations.component';
import { WorkerVacationsComponent } from './pages/worker-vacations/worker-vacations.component';
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
