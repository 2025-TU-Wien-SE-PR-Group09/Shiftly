import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DepartmentsComponent } from './departments.component';
import { DepartmentDetailSupervisorComponent } from './pages/department-detail-supervisor/department-detail-supervisor.component';
import { DepartmentDetailAdminComponent } from './pages/department-detail-admin/department-detail-admin.component';
import { DepartmentShiftplanBlueprintComponent } from './pages/department-shiftplan-blueprint/department-shiftplan-blueprint.component';
import { DepartmentResolver } from './resolvers/department.resolver';
import { BlueprintsResolver } from './resolvers/blueprints.resolver';
import { RoleGuard } from 'src/app/core/guards/role.guard';
import { DepartmentGuard } from 'src/app/core/guards/department.guard';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
const routes: Routes = [
  {
    path: '',
    component: DepartmentsComponent,
    children: [
      {
        path: 'admin',
        component: DepartmentDetailAdminComponent,
        canActivate: [AuthGuard, RoleGuard],
        data: {
          roles: ['ADMIN'],
        },
      },
      {
        path: 'supervisor',
        component: DepartmentDetailSupervisorComponent,
        canActivate: [AuthGuard, RoleGuard, DepartmentGuard],
        data: {
          roles: ['SUPERVISOR'],
        },
      },
      {
        path: ':depName/shiftplan-editor',
        component: DepartmentShiftplanBlueprintComponent,
        resolve: {
          department: DepartmentResolver, /* Loads department and puts it into this.route.snapshot.data['department']*/
          blueprints: BlueprintsResolver, /* Loads blueprints and puts it into this.route.snapshot.data['blueprints']*/
        },
        canActivate: [AuthGuard, RoleGuard, DepartmentGuard], /* DepartmentGuard takes :depName or :depId and checks if user has access */
        data: {
          roles: ['ADMIN', 'SUPERVISOR'], /* Admins and Supervisors can access*/
        },
      }
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DepartmentsRoutingModule { }
