import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {jwtDecode} from 'jwt-decode';
import { LoginEndpointService, LoginResponseRestDto, UserDataRestDto } from '../../rest_client';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private loginEndpoint: LoginEndpointService ) {
  }

  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param authRequest User data
   */
  loginUser(authRequest: UserDataRestDto): Observable<LoginResponseRestDto> {
    return this.loginEndpoint.login(authRequest)
      .pipe(
        tap((authResponse: LoginResponseRestDto) => this.setToken(authResponse.jwt!))
      );
  }


  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    return !!this.getToken() && (this.getTokenExpirationDate(this.getToken()!).valueOf() > new Date().valueOf());
  }

  logoutUser() {
    console.log('Logout');
    localStorage.removeItem('authToken');
  }

  getToken() {
    return localStorage.getItem('authToken');
  }

  /**
   * Returns the user roles based on the current token
   */
  getUserRoles() {
    if (this.getToken() != null) {
      const decoded: any = jwtDecode(this.getToken()!);
      const authInfo: string[] = decoded.rol;
      return authInfo;
    }

    return [];
  }

  private setToken(authResponse: string) {
    localStorage.setItem('authToken', authResponse);
  }

  private getTokenExpirationDate(token: string): Date {

    const decoded: any = jwtDecode(token);
    if (decoded.exp === undefined) {
      return new Date(0);
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }

  getUserEmail() {
    if (this.getToken() != null) {
      const decoded: any = jwtDecode(this.getToken()!);
      return decoded.sub;
    }

    return 'none';
  }
}
