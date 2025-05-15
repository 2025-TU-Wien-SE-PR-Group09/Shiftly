import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DepartmentsComponent } from './departments.component';
import { DepartmentDetailSupervisorComponent } from './pages/department-detail-supervisor/department-detail-supervisor.component';
import { DepartmentDetailAdminComponent } from './pages/department-detail-admin/department-detail-admin.component';
import { DepartmentShiftplanComponent } from './pages/department-shiftplan/department-shiftplan.component';

const routes: Routes = [
  {
    path: '',
    component: DepartmentsComponent,
    children: [
      {
        path: 'admin',
        component: DepartmentDetailAdminComponent,
      },
      {
        path: 'supervisor',
        component: DepartmentDetailSupervisorComponent,
      },
      {
        path: ':name/shiftplan',
        component: DepartmentShiftplanComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DepartmentsRoutingModule {}
