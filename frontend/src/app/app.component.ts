import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from './services/auth.service';
import { CartService } from './services/cart.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'shopping-frontend';
  cartCount = 0;
  private cartSubscription: Subscription | null = null;

  constructor(
    public authService: AuthService,
    private cartService: CartService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.cartSubscription = this.cartService.getCart().subscribe(items => {
      this.cartCount = items.reduce((count, item) => count + item.quantity, 0);
    });
  }

  ngOnDestroy(): void {
    this.cartSubscription?.unsubscribe();
  }

  logout() {
    this.authService.logout();
    this.cartService.clearCart();
    this.router.navigate(['/auth/login']);
  }

  get isAdmin(): boolean {
    return this.authService.userValue?.isAdmin ?? false;
  }
}

