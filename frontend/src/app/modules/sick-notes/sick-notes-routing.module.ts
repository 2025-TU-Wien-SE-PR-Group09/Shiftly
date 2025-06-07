import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SickNotesComponent } from './sick-notes.component';
import { EmployeeSickNotesComponent } from './pages/employee-sick-notes/employee-sick-notes.component';
import { SupervisorSickNotesComponent } from './pages/supervisor-sick-notes/supervisor-sick-notes.component';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
import { RoleGuard } from 'src/app/core/guards/role.guard';
import { AdminSickNotesComponent } from './pages/admin-sick-notes/admin-sick-notes.component';

const routes: Routes = [
  {
    path: '',
    component: SickNotesComponent,
    children: [
      {
        path: 'admin',
        component: AdminSickNotesComponent,
        canActivate: [AuthGuard, RoleGuard],
        data: {
          roles: ['ADMIN']
        }
      },
      {
        path: 'supervisor',
        component: SupervisorSickNotesComponent,
        canActivate: [AuthGuard, RoleGuard],
        data: {
          roles: ['SUPERVISOR'],
        },
      },
      {
        path: 'employee',
        component: EmployeeSickNotesComponent,
        canActivate: [AuthGuard, RoleGuard],
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
export class SickNotesRoutingModule { }
