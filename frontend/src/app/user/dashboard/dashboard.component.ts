import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { Product } from '../../models/product.model';
import { first } from 'rxjs/operators';
import { PageEvent } from '@angular/material/paginator';

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

  // Pagination state
  pageIndex = 0;
  pageSize = 12;
  totalElements = 0;

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

    const min = this.minPrice ? this.minPrice : undefined;
    const max = this.maxPrice ? this.maxPrice : undefined;

    this.productService.searchProducts(this.searchQuery, min, max, this.pageIndex, this.pageSize)
      .pipe(first())
      .subscribe({
        next: response => {
          this.products = response.content;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: error => {
          this.error = 'Failed to load products';
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.searchProducts();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.minPrice = null;
    this.maxPrice = null;
    this.pageIndex = 0;
    this.searchProducts();
  }

  viewDetails(product: Product): void {
    this.router.navigate(['/user/products', product.id]);
  }

  addToCart(product: Product): void {
    this.cartService.addToCart(product, 1);
  }
}
