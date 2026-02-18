import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Address } from '../models/address.model';
import { PaymentMethod } from '../models/payment.model';

export interface UserProfile {
    id: number;
    username: string;
    email: string;
    role: string;
    isAdmin: boolean;
    hasProfilePicture: boolean;
}

export interface ProfileUpdateRequest {
    email?: string;
    password?: string;
}

@Injectable({
    providedIn: 'root'
})
export class UserService {

    private apiUrl = `${environment.apiUrl}/profile`;

    constructor(private http: HttpClient) { }

    getProfile(): Observable<UserProfile> {
        return this.http.get<UserProfile>(this.apiUrl);
    }

    updateProfile(request: ProfileUpdateRequest): Observable<string> {
        return this.http.put(this.apiUrl, request, { responseType: 'text' });
    }

    uploadProfilePicture(formData: FormData): Observable<string> {
        return this.http.post(`${this.apiUrl}/picture`, formData, { responseType: 'text' });
    }

    getProfilePictureUrl(): string {
        return `${this.apiUrl}/picture`;
    }

    getProfilePictureBlob(): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/picture`, { responseType: 'blob' });
    }

    getAddresses(): Observable<Address[]> {
        return this.http.get<Address[]>(`${this.apiUrl}/addresses`);
    }

    addAddress(address: Address): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/addresses`, address);
    }

    getPaymentMethods(): Observable<PaymentMethod[]> {
        return this.http.get<PaymentMethod[]>(`${this.apiUrl}/payment-methods`);
    }

    addPaymentMethod(paymentMethod: PaymentMethod): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/payment-methods`, paymentMethod);
    }
}
