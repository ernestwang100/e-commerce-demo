import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { WatchlistService } from '../../services/watchlist.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  loading = true;
  error = '';
  quantity = 1;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private watchlistService: WatchlistService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadProduct(+id);
    }
  }

  loadProduct(id: number): void {
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.product = product;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load product';
        this.loading = false;
      }
    });
  }

  addToCart(): void {
    if (this.product) {
      this.cartService.addToCart(this.product, this.quantity);
      this.snackBar.open(`Added ${this.quantity} ${this.product.name} to cart`, 'Close', {
        duration: 3000
      });
    }
  }

  addToWatchlist(): void {
    if (this.product && this.authService.isLoggedIn()) {
      this.watchlistService.addToWatchlist(this.product.id).subscribe({
        next: () => {
          this.snackBar.open('Added to watchlist', 'Close', { duration: 3000 });
        },
        error: () => {
          this.snackBar.open('Failed to add to watchlist', 'Close', { duration: 3000 });
        }
      });
    } else if (!this.authService.isLoggedIn()) {
      this.snackBar.open('Please login to add to watchlist', 'Close', { duration: 3000 });
    }
  }

  goBack(): void {
    this.router.navigate(['/user']);
  }

  incrementQuantity(): void {
    this.quantity++;
  }

  decrementQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }
}
