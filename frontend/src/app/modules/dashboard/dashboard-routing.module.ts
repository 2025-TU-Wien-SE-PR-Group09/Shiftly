import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { SupervisorDashboardComponent } from './pages/supervisor-dashboard/supervisor-dashboard.component';
import { WorkerDashboardComponent } from './pages/worker-dashboard/worker-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: DashboardComponent,
    children: [
      {
        path: 'admin',
        component: AdminDashboardComponent,
      },
      {
        path: 'supervisor',
        component: SupervisorDashboardComponent,
      },
      {
        path: 'worker',
        component: WorkerDashboardComponent,
      },
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DashboardRoutingModule {}
