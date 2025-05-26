import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DepartmentService } from 'src/app/rest_client';
import { DepartmentDetailRestResponseDto } from 'src/app/rest_client';
@Injectable({
  providedIn: 'root',
})
export class DepartmentResolver implements Resolve<DepartmentDetailRestResponseDto> {
  constructor(private departmentService: DepartmentService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<DepartmentDetailRestResponseDto> {
    const name = route.paramMap.get('name');

    if (!name) {
      this.router.navigate(['/not-found']);
      return EMPTY;
    }

    return this.departmentService.getDepartmentByName(name).pipe(
      catchError((_) => {
        this.router.navigate(['/not-found']);
        return EMPTY;
      }),
    );
  }
}
