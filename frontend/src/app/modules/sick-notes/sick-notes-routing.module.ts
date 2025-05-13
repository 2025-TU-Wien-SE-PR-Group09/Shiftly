import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SickNotesComponent } from './sick-notes.component';
import { EmployeeSickNotesComponent } from './pages/employee-sick-notes/employee-sick-notes.component';
import { SupervisorSickNotesComponent } from './pages/supervisor-sick-notes/supervisor-sick-notes.component';

const routes: Routes = [
  {
    path: '',
    component: SickNotesComponent,
    children: [
      {
        path: 'supervisor',
        component: SupervisorSickNotesComponent,
      },
      {
        path: 'employee',
        component: EmployeeSickNotesComponent,
      }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SickNotesRoutingModule {}
