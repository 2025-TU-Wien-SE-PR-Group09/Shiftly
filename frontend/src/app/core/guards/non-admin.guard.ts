import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { map } from 'rxjs/operators';
import { UserEndpointService } from '../../rest_client';

export const nonAdminGuard: CanActivateFn = () => {
  const userEndpoint = inject(UserEndpointService);
  const toastr = inject(ToastrService);

  return userEndpoint.getCurrentUserProfile().pipe(
    map(user => {
      if (user.role === 'ADMIN') {
        toastr.error('Admins are not allowed to access this page.');
        return false;
      }
      return true;
    })
  );
};
