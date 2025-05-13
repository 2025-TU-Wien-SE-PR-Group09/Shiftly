import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './layout.component';

const routes: Routes = [
  {
    path: 'dashboard',
    component: LayoutComponent,
    loadChildren: () => import('../dashboard/dashboard.module').then((m) => m.DashboardModule),
  },
  {
    path: 'vacations',
    component: LayoutComponent,
    loadChildren: () => import('../vacations/vacations.module').then((m) => m.VacationsModule),
  },
  {
    path: 'sick-notes',
    component: LayoutComponent,
    loadChildren: () => import('../sick-notes/sick-notes.module').then((m) => m.SickNotesModule),
  },
  {
    path: 'components',
    component: LayoutComponent,
    loadChildren: () => import('../uikit/uikit.module').then((m) => m.UikitModule),
  },
  {
    path: 'departments',
    component: LayoutComponent,
    loadChildren: () => import('../departments/departments.module').then((m) => m.DepartmentsModule),
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: 'error/404' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LayoutRoutingModule {}
