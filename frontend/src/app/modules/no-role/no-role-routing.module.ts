import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NoRoleComponent } from './no-role.component';

const routes: Routes = [
  {
    path: '',
    component: NoRoleComponent,
    children: [
      { path: '', redirectTo: '', pathMatch: 'full' },
      { path: '**', redirectTo: '' },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class NoRoleRoutingModule {}
