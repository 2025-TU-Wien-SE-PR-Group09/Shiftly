import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { catchError, EMPTY, Observable, throwError } from 'rxjs';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { SKIP_EXCEPTION_INTERCEPTOR } from './skip-exception-interceptor';

@Injectable()
export class ExceptionInterceptor implements HttpInterceptor {
  constructor(private toastr: ToastrService, private router: Router, private authService: AuthService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const shouldSkip = req.context.get(SKIP_EXCEPTION_INTERCEPTOR);
    if (shouldSkip) {
      return next.handle(req); // Skip logic
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        console.log(error)
        if ((error.status >= 500 && error.status < 600) || error.status === 0) {
          this.router.navigate(['/errors/500']);
          return throwError(() => error);
        }

        if (error.status === 401) {
          this.toastr.error('Your session has expired. Please log in again.');
          this.authService.logoutUser();            // drop any stale token
          this.router.navigate(['/auth/sign-in']).then();
          return EMPTY;
        }
        if (error.status===413){
          this.toastr.error("File Size can not exceed 5MB","Error occurred")
          return EMPTY;
        }

        if (error.error?.errors) {
          // If errors is an object or array, iterate and show each message
          const errors = error.error.errors;
          console.log(errors);
          if (Array.isArray(errors)) {
            errors.forEach(err => this.toastr.error(err, "Error occurred"));
          } else if (typeof errors === 'object') {
            Object.values(errors).forEach(errArray => {
              if (Array.isArray(errArray)) {
                errArray.forEach(err => this.toastr.error(err));
              } else {
                this.toastr.error(errArray as string, "Error occurred");
              }
            });
          } else {
            this.toastr.error(errors, "Error occurred");
          }
        } else {
          this.toastr.error(error.message || 'An unknown error occurred', "Error occurred");
        }
        return throwError(() => error);
      })
    );
  }

}
