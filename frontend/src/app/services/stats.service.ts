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
        return this.http.get<AdminStats>(`${this.apiUrl}/admin/stats`);
    }

    getUserStats(): Observable<UserStats> {
        return this.http.get<UserStats>(`${this.apiUrl}/user/stats`);
    }
}
