import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Product, ProductRequest } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) { }

  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${environment.apiUrl}/products/all`);
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${environment.apiUrl}/products/${id}`);
  }

  searchProducts(query: string, minPrice?: number, maxPrice?: number): Observable<Product[]> {
    let params: any = {};
    if (query) params.query = query;
    if (minPrice) params.minPrice = minPrice;
    if (maxPrice) params.maxPrice = maxPrice;

    return this.http.get<Product[]>(`${environment.apiUrl}/products/search`, { params });
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
