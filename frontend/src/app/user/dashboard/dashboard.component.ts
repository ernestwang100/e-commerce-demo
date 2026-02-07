import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { Product } from '../../models/product.model';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  products: Product[] = [];
  loading = false;
  error = '';

  // Search state
  searchQuery = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.searchProducts();
  }

  searchProducts(): void {
    this.loading = true;
    this.error = '';

    // Convert empty strings to undefined/null for the service
    const min = this.minPrice ? this.minPrice : undefined;
    const max = this.maxPrice ? this.maxPrice : undefined;

    this.productService.searchProducts(this.searchQuery, min, max)
      .pipe(first())
      .subscribe({
        next: products => {
          this.products = products;
          this.loading = false;
        },
        error: error => {
          this.error = 'Failed to load products. Please try again.';
          this.loading = false;
          console.error('Error loading products:', error);
        }
      });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.minPrice = null;
    this.maxPrice = null;
    this.searchProducts();
  }

  viewDetails(product: Product): void {
    this.router.navigate(['/user/products', product.id]);
  }

  addToCart(product: Product): void {
    this.cartService.addToCart(product, 1);
  }
}
