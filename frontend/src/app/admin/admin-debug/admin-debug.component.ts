import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Component({
    selector: 'app-admin-debug',
    templateUrl: './admin-debug.component.html',
    styleUrls: ['./admin-debug.component.css']
})
export class AdminDebugComponent implements OnInit {
    apiUrl = environment.apiUrl + '/api/system';
    health: any = {};
    logs: string[] = [];
    loading = false;
    syncing = false;
    flushing = false;
    error = '';

    constructor(private http: HttpClient) { }

    ngOnInit(): void {
        this.refreshHealth();
        this.fetchLogs();
    }

    refreshHealth() {
        this.loading = true;
        this.http.get(this.apiUrl + '/health').subscribe({
            next: (data) => {
                this.health = data;
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Failed to fetch health: ' + err.message;
                this.loading = false;
            }
        });
    }

    fetchLogs() {
        this.http.get<string[]>(this.apiUrl + '/logs').subscribe({
            next: (data) => {
                this.logs = data;
            },
            error: (err) => {
                console.error('Failed to fetch logs', err);
            }
        });
    }

    syncProducts() {
        this.syncing = true;
        // We don't have a direct backend endpoint for this in SystemController yet?
        // Wait, implementation plan said POST /api/system/sync-products
        // I missed adding that to SystemController!
        // I should add it. For now, I'll just rely on the script, but better to add it.
        // Let's add it to SystemController later.
        // Actually, I can call the one in ProductController if available? No, sync is internal.
        // I will add the endpoint to SystemController in a moment.

        this.http.post(this.apiUrl + '/sync-products', {}).subscribe({
            next: () => {
                this.syncing = false;
                alert('Sync triggered successfully');
                this.fetchLogs();
            },
            error: (err) => {
                this.syncing = false;
                alert('Sync failed: ' + err.message);
            }
        });
    }

    flushCache() {
        this.flushing = true;
        this.http.post(this.apiUrl + '/flush-cache', {}).subscribe({
            next: () => {
                this.flushing = false;
                alert('Cache flushed successfully');
                this.refreshHealth();
            },
            error: (err) => {
                this.flushing = false;
                alert('Flush failed: ' + err.message);
            }
        });
    }
}
