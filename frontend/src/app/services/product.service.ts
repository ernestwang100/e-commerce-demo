import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Product, ProductRequest } from '../models/product.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor(private http: HttpClient) { }

  // Old getAllProducts (still used by User Dashboard?) - Check if User Dashboard uses getAll endpoint
  // User Dashboard uses /products/search or /products/all? 
  // It calls `searchProducts` (which hits /products/search).
  // Admin uses `getProducts`? No, Admin uses `getProducts()`.
  // Let's see `ProductService.ts` first.

  // I will replace getProducts() with getProducts(page, size)
  getProducts(page: number, size: number): Observable<PageResponse<Product>> {
    return this.http.get<PageResponse<Product>>(`${environment.apiUrl}/products?page=${page}&size=${size}`);
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${environment.apiUrl}/products/${id}`);
  }

  searchProducts(query: string, minPrice?: number, maxPrice?: number, page: number = 0, size: number = 12): Observable<PageResponse<Product>> {
    let params: any = {};
    if (query) params.query = query;
    if (minPrice) params.minPrice = minPrice;
    if (maxPrice) params.maxPrice = maxPrice;
    params.page = page;
    params.size = size;

    return this.http.get<PageResponse<Product>>(`${environment.apiUrl}/products/search`, { params });
  }

  // Admin methods
  createProduct(product: ProductRequest): Observable<Product> {
    return this.http.post<Product>(`${environment.apiUrl}/products`, product);
  }

  updateProduct(id: number, product: ProductRequest): Observable<Product> {
    return this.http.patch<Product>(`${environment.apiUrl}/products/${id}`, product);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/products/${id}`);
  }

  uploadProductImage(id: number, formData: FormData): Observable<string> {
    return this.http.post(`${environment.apiUrl}/products/${id}/image`, formData, { responseType: 'text' });
  }

  getProductImageBlob(id: number): Observable<Blob> {
    return this.http.get(`${environment.apiUrl}/products/${id}/image`, { responseType: 'blob' });
  }
}
