import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { WatchlistItem } from '../models/watchlist.model';

@Injectable({
    providedIn: 'root'
})
export class WatchlistService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) { }

    getWatchlist(): Observable<WatchlistItem[]> {
        return this.http.get<WatchlistItem[]>(`${this.apiUrl}/user/watchlist`);
    }

    addToWatchlist(productId: number): Observable<string> {
        return this.http.post(`${this.apiUrl}/user/watchlist/${productId}`, {}, { responseType: 'text' });
    }

    removeFromWatchlist(productId: number): Observable<string> {
        return this.http.delete(`${this.apiUrl}/user/watchlist/${productId}`, { responseType: 'text' });
    }
}
