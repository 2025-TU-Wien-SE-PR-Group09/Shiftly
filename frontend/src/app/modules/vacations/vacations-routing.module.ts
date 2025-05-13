import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VacationsComponent } from './vacations.component';
import {
  EmployeeVacationsComponent
} from './pages/employee-vacations/employee-vacations.component';
import { SupervisorVacationsComponent } from './pages/supervisor-vacations/supervisor-vacations.component';

const routes: Routes = [
  {
    path: '',
    component: VacationsComponent,
    children: [
      {
        path: 'employee',
        component: EmployeeVacationsComponent
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
