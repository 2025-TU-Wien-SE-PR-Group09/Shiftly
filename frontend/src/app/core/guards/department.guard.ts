import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class DepartmentGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const userRoles = this.auth.getUserRoles();
    const userDeptId = this.auth.getDepartmentId();
    const userDeptName = this.auth.getDepartmentName();

    // ADMINs can access anything
    if (userRoles.includes('ADMIN')) {
      return true;
    }

    // These roles need department-based restriction
    const restrictedRoles = ['SUPERVISOR', 'EMPLOYEE'];
    const requiresDepartmentCheck = userRoles.some((role) => restrictedRoles.includes(role));

    if (!requiresDepartmentCheck) {
      this.router.navigate(['/errors/403']);
      return false;
    }

    // Route may contain either departmentId or departmentName
    const routeDeptId = route.paramMap.get('depId');
    const routeDeptName = route.paramMap.get('depName');

    const matchesDeptId = routeDeptId ? Number(routeDeptId) === userDeptId : true;
    const matchesDeptName = routeDeptName ? routeDeptName.toLowerCase() === userDeptName.toLowerCase() : true;

    if (matchesDeptId && matchesDeptName) {
      return true;
    }

    this.router.navigate(['/errors/403']);
    return false;
  }
}
