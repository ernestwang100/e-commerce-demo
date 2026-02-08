import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { OrderRequest, OrderResponse } from '../models/order.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({
    providedIn: 'root'
})
export class OrderService {

    constructor(private http: HttpClient) { }

    // User endpoints
    placeOrder(orderRequest: OrderRequest): Observable<OrderResponse> {
        return this.http.post<OrderResponse>(`${environment.apiUrl}/orders`, orderRequest);
    }

    getUserOrders(): Observable<OrderResponse[]> {
        return this.http.get<OrderResponse[]>(`${environment.apiUrl}/orders/all`);
    }

    cancelOrder(orderId: number): Observable<string> {
        return this.http.patch(`${environment.apiUrl}/orders/${orderId}/cancel`, {}, { responseType: 'text' });
    }

    // Admin endpoints
    getAdminOrders(page: number, size: number): Observable<PageResponse<OrderResponse>> {
        return this.http.get<PageResponse<OrderResponse>>(`${environment.apiUrl}/orders/all?page=${page}&size=${size}`);
    }

    completeOrder(orderId: number): Observable<any> {
        return this.http.patch(`${environment.apiUrl}/orders/${orderId}/complete`, {});
    }

    cancelOrderAdmin(orderId: number): Observable<string> {
        return this.http.patch(`${environment.apiUrl}/orders/${orderId}/cancel`, {}, { responseType: 'text' });
    }
}
