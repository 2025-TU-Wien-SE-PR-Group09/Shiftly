import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SickNotesComponent } from './sick-notes.component';
import { SupervisorVacationsComponent } from '../vacations/pages/supervisor-vacations/supervisor-vacations.component';
import { WorkerVacationsComponent } from '../vacations/pages/worker-vacations/worker-vacations.component';
import { WorkerSickNotesComponent } from './pages/worker-sick-notes/worker-sick-notes.component';
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
        path: 'worker',
        component: WorkerSickNotesComponent,
      }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SickNotesRoutingModule {}
