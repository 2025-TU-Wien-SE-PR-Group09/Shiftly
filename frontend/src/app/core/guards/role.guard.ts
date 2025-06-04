import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class RoleGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const expectedRoles: string[] = route.data['roles'];
    const userRoles = this.auth.getUserRoles();
    console.log(userRoles);

    const hasRole = expectedRoles.some((role) => userRoles.includes(role));
    if (!hasRole) {
      this.router.navigate(['/errors/403']);
      return false;
    }
    return true;
  }
}
