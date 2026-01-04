import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
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

  constructor(private productService: ProductService) { }

  ngOnInit(): void {
    this.loading = true;
    this.productService.getAllProducts()
      .pipe(first())
      .subscribe({
        next: products => {
          this.products = products;
          this.loading = false;
        },
        error: error => {
          this.error = error;
          this.loading = false;
        }
      });
  }
}
