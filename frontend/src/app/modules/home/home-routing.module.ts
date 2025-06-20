import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { AdminHomeComponent } from './pages/admin-home/admin-home.component';
import { SupervisorHomeComponent } from './pages/supervisor-home/supervisor-home.component';
import { EmployeeHomeComponent } from './pages/employee-home/employee-home.component';
import { RoleGuard } from 'src/app/core/guards/role.guard';
import { AuthGuard } from 'src/app/core/guards/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    children: [
      {
        path: 'admin',
        component: AdminHomeComponent,
        canActivate: [AuthGuard, RoleGuard],
        data: {
          roles: ['ADMIN'],
        },
      },
      {
        path: 'supervisor',
        component: SupervisorHomeComponent,
        canActivate: [AuthGuard, RoleGuard],
        data: {
          roles: ['SUPERVISOR'],
        },
      },
      {
        path: 'employee',
        component: EmployeeHomeComponent,
        canActivate: [AuthGuard, RoleGuard],
        data: {
          roles: ['EMPLOYEE', 'JUMPER'],
        },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class HomeRoutingModule {}
