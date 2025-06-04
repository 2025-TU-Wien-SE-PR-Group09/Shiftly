import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { AdminHomeComponent } from './pages/admin-home/admin-home.component';
import { SupervisorHomeComponent } from './pages/supervisor-home/supervisor-home.component';
import { EmployeeHomeComponent } from './pages/employee-home/employee-home.component';
import { RoleGuard } from 'src/app/core/guards/role.guard';

const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    children: [
      {
        path: 'admin',
        component: AdminHomeComponent,
        canActivate: [RoleGuard],
        data: {
          roles: ['ADMIN'],
        },
      },
      {
        path: 'supervisor',
        component: SupervisorHomeComponent,
        canActivate: [RoleGuard],
        data: {
          roles: ['SUPERVISOR'],
        },
      },
      {
        path: 'employee',
        component: EmployeeHomeComponent,
        canActivate: [RoleGuard],
        data: {
          roles: ['EMPLOYEE'],
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
