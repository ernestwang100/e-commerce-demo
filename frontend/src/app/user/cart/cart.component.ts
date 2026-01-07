import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { CartItem } from '../../models/cart.model';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { OrderRequest } from '../../models/order.model';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit, OnDestroy {
  cartItems: CartItem[] = [];
  displayedColumns = ['product', 'price', 'quantity', 'subtotal', 'actions'];
  processing = false;
  private cartSubscription: Subscription | null = null;

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.cartSubscription = this.cartService.getCart().subscribe(items => {
      this.cartItems = items;
    });
  }

  ngOnDestroy(): void {
    this.cartSubscription?.unsubscribe();
  }

  updateQuantity(productId: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const quantity = parseInt(input.value, 10);
    if (quantity > 0) {
      this.cartService.updateQuantity(productId, quantity);
    }
  }

  removeItem(productId: number): void {
    this.cartService.removeFromCart(productId);
    this.snackBar.open('Item removed from cart', 'Close', { duration: 2000 });
  }

  clearCart(): void {
    this.cartService.clearCart();
    this.snackBar.open('Cart cleared', 'Close', { duration: 2000 });
  }

  getTotal(): number {
    return this.cartService.getCartTotal();
  }

  checkout(): void {
    if (!this.authService.isLoggedIn()) {
      this.snackBar.open('Please login to checkout', 'Close', { duration: 3000 });
      this.router.navigate(['/auth/login']);
      return;
    }

    if (this.cartItems.length === 0) {
      this.snackBar.open('Your cart is empty', 'Close', { duration: 3000 });
      return;
    }

    this.processing = true;
    const orderRequest: OrderRequest = {
      order: this.cartItems.map(item => ({
        productId: item.product.id,
        quantity: item.quantity
      }))
    };

    this.orderService.placeOrder(orderRequest).subscribe({
      next: (order) => {
        this.processing = false;
        this.cartService.clearCart();
        this.snackBar.open('Order placed successfully!', 'View Orders', {
          duration: 5000
        }).onAction().subscribe(() => {
          this.router.navigate(['/user/orders']);
        });
      },
      error: (err) => {
        this.processing = false;
        const message = err.error?.message || 'Failed to place order';
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }

  continueShopping(): void {
    this.router.navigate(['/user']);
  }
}
