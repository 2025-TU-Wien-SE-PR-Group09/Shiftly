import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { AdminHomeComponent } from './pages/admin-home/admin-home.component';
import { SupervisorHomeComponent } from './pages/supervisor-home/supervisor-home.component';
import { EmployeeHomeComponent } from './pages/employee-home/employee-home.component';

const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    children: [
      {
        path: 'admin',
        component: AdminHomeComponent,
      },
      {
        path: 'supervisor',
        component: SupervisorHomeComponent,
      },
      {
        path: 'employee',
        component: EmployeeHomeComponent,
      },
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class HomeRoutingModule {}
