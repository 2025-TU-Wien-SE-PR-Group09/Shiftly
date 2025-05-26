import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DepartmentsComponent } from './departments.component';
import { DepartmentDetailSupervisorComponent } from './pages/department-detail-supervisor/department-detail-supervisor.component';
import { DepartmentDetailAdminComponent } from './pages/department-detail-admin/department-detail-admin.component';
import { DepartmentShiftplanBlueprintComponent } from './pages/department-shiftplan-blueprint/department-shiftplan-blueprint.component';
import { DepartmentResolver } from './resolvers/department.resolver';
import { BlueprintsResolver } from './resolvers/blueprints.resolver';
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
        path: ':name/shiftplan-editor',
        component: DepartmentShiftplanBlueprintComponent,
        resolve: {
          department: DepartmentResolver,
          blueprints: BlueprintsResolver,
        },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DepartmentsRoutingModule {}
