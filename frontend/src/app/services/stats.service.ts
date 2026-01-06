import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AdminStats, UserStats } from '../models/stats.model';

@Injectable({
    providedIn: 'root'
})
export class StatsService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) { }

    getAdminStats(): Observable<AdminStats> {
        // Postman has individual endpoints. For the UI, we might need to aggregate 
        // if the UI expects a single object, or update the UI. 
        // Aligning the service methods to the new paths.
        return new Observable(observer => {
            const stats: AdminStats = { totalSoldItems: 0, mostPopular: [], mostProfitable: [] };
            this.http.get<any>(`${environment.apiUrl}/products/popular/3`).subscribe(popular => {
                stats.mostPopular = popular;
                this.http.get<any>(`${environment.apiUrl}/products/profit/3`).subscribe(profit => {
                    stats.mostProfitable = profit;
                    observer.next(stats);
                    observer.complete();
                });
            });
        });
    }

    getUserStats(): Observable<UserStats> {
        return new Observable(observer => {
            const stats: UserStats = { mostRecent: [], mostFrequent: [] };
            this.http.get<string[]>(`${environment.apiUrl}/products/recent/3`).subscribe(recent => {
                stats.mostRecent = recent;
                this.http.get<string[]>(`${environment.apiUrl}/products/frequent/3`).subscribe(frequent => {
                    stats.mostFrequent = frequent;
                    observer.next(stats);
                    observer.complete();
                });
            });
        });
    }
}
