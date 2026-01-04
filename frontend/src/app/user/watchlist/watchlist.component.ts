import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { WatchlistItem } from '../../models/watchlist.model';
import { WatchlistService } from '../../services/watchlist.service';
import { CartService } from '../../services/cart.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-watchlist',
  templateUrl: './watchlist.component.html',
  styleUrls: ['./watchlist.component.css']
})
export class WatchlistComponent implements OnInit {
  items: WatchlistItem[] = [];
  loading = true;
  error = '';

  constructor(
    private watchlistService: WatchlistService,
    private cartService: CartService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.loadWatchlist();
  }

  loadWatchlist(): void {
    this.loading = true;
    this.watchlistService.getWatchlist().subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load watchlist';
        this.loading = false;
      }
    });
  }

  removeFromWatchlist(productId: number): void {
    this.watchlistService.removeFromWatchlist(productId).subscribe({
      next: () => {
        this.items = this.items.filter(item => item.productId !== productId);
        this.snackBar.open('Removed from watchlist', 'Close', { duration: 2000 });
      },
      error: () => {
        this.snackBar.open('Failed to remove', 'Close', { duration: 2000 });
      }
    });
  }

  addToCart(item: WatchlistItem): void {
    const product: Product = {
      id: item.productId,
      name: item.productName,
      description: item.description,
      retailPrice: item.retailPrice
    };
    this.cartService.addToCart(product, 1);
    this.snackBar.open('Added to cart', 'Close', { duration: 2000 });
  }

  viewProduct(productId: number): void {
    this.router.navigate(['/user/product', productId]);
  }

  continueShopping(): void {
    this.router.navigate(['/user']);
  }
}
