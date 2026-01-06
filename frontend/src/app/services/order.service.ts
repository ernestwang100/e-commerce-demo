import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { OrderRequest, OrderResponse } from '../models/order.model';

@Injectable({
    providedIn: 'root'
})
export class OrderService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) { }

    // User endpoints
    placeOrder(request: OrderRequest): Observable<OrderResponse> {
        return this.http.post<OrderResponse>(`${this.apiUrl}/orders`, request);
    }

    getUserOrders(): Observable<OrderResponse[]> {
        return this.http.get<OrderResponse[]>(`${this.apiUrl}/orders/all`);
    }

    cancelOrder(orderId: number): Observable<string> {
        return this.http.patch(`${this.apiUrl}/orders/${orderId}/cancel`, {}, { responseType: 'text' });
    }

    // Admin endpoints
    getAdminOrders(page: number = 1): Observable<OrderResponse[]> {
        return this.http.get<OrderResponse[]>(`${this.apiUrl}/orders/all?page=${page}`);
    }

    completeOrder(orderId: number): Observable<string> {
        return this.http.patch(`${this.apiUrl}/orders/${orderId}/complete`, {}, { responseType: 'text' });
    }

    cancelOrderAdmin(orderId: number): Observable<string> {
        return this.http.patch(`${this.apiUrl}/orders/${orderId}/cancel`, {}, { responseType: 'text' });
    }
}
