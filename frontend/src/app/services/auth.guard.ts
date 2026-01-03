import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router, private authService: AuthService) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const user = this.authService.userValue;
    if (user) {
      // Check if route is restricted by role
      if (route.data['roles'] && route.data['roles'].indexOf(user.isAdmin ? 'ADMIN' : 'USER') === -1) {
        // Role not authorised so redirect to home page
        this.router.navigate(['/']);
        return false;
      }
      // Authorised so return true
      return true;
    }

    // Not logged in so redirect to login page with the return url
    this.router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
}
