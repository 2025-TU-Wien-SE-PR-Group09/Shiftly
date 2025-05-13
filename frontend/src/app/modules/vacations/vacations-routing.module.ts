import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VacationsComponent } from './vacations.component';
import { WorkerVacationsComponent } from './pages/worker-vacations/worker-vacations.component';
import { SupervisorVacationsComponent } from './pages/supervisor-vacations/supervisor-vacations.component';

const routes: Routes = [
  {
    path: '',
    component: VacationsComponent,
    children: [
      {
        path: 'worker',
        component: WorkerVacationsComponent
      },
      {
        path: 'supervisor',
        component: SupervisorVacationsComponent
      },
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class VacationsRoutingModule {}
