import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SickNotesComponent } from './sick-notes.component';
import { EmployeeSickNotesComponent } from './pages/employee-sick-notes/employee-sick-notes.component';
import { SupervisorSickNotesComponent } from './pages/supervisor-sick-notes/supervisor-sick-notes.component';
import { AdminSickNotesComponent } from './pages/admin-sick-notes/admin-sick-notes.component';
import { RoleGuard } from '../../core/guards/role.guard';

const routes: Routes = [
  {
    path: '',
    component: SickNotesComponent,
    children: [
      {
        path: 'admin',
        component: AdminSickNotesComponent,
      },
      {
        path: 'supervisor',
        component: SupervisorSickNotesComponent,
        canActivate: [RoleGuard],
        data: {
          roles: ['SUPERVISOR'],
        },
      },
      {
        path: 'employee',
        component: EmployeeSickNotesComponent,
        canActivate: [RoleGuard],
        data: {
          roles: ['EMPLOYEE'],
        },
      }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SickNotesRoutingModule {}
