import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VacationsComponent } from './vacations.component';
import {
  EmployeeVacationsComponent
} from './pages/employee-vacations/employee-vacations.component';
import { SupervisorVacationsComponent } from './pages/supervisor-vacations/supervisor-vacations.component';
import { RoleGuard } from '../../core/guards/role.guard';

const routes: Routes = [
  {
    path: '',
    component: VacationsComponent,
    children: [
      {
        path: 'employee',
        component: EmployeeVacationsComponent,
        canActivate: [RoleGuard],
        data: {
          roles: ['EMPLOYEE'],
        },
      },
      {
        path: 'supervisor',
        component: SupervisorVacationsComponent,
        canActivate: [RoleGuard],
        data: {
          roles: ['SUPERVISOR'],
        },
      },
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class VacationsRoutingModule {}
