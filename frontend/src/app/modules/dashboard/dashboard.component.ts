import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { SupervisorDashboardComponent } from './pages/supervisor-dashboard/supervisor-dashboard.component';
import { WorkerDashboardComponent } from './pages/worker-dashboard/worker-dashboard.component';
import { AuthService } from '../../core/services/auth.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  imports: [RouterOutlet],
})
export class DashboardComponent implements OnInit {
  ngOnInit(): void {}
}
