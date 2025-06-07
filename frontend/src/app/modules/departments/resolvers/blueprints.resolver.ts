import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DepartmentService } from 'src/app/rest_client';
import { PlanBlueprintResponse } from 'src/app/rest_client';
@Injectable({
  providedIn: 'root',
})
export class BlueprintsResolver implements Resolve<Array<PlanBlueprintResponse>> {
  constructor(private departmentService: DepartmentService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Array<PlanBlueprintResponse>> {
    const name = route.paramMap.get('depName');

    if (!name) {
      this.router.navigate(['/not-found']);
      return EMPTY;
    }

    return this.departmentService.getShiftplanBlueprints(name).pipe(
      catchError((_) => {
        this.router.navigate(['/not-found']);
        return EMPTY;
      }),
    );
  }
}
