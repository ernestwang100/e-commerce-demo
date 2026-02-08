import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StatsService } from '../../services/stats.service';
import { OrderService } from '../../services/order.service';
import { AdminStats } from '../../models/stats.model';
import { OrderResponse } from '../../models/order.model';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  stats: AdminStats | null = null;
  recentOrders: OrderResponse[] = [];
  loadingStats = true;
  loadingOrders = true;
  error = '';

  constructor(
    private statsService: StatsService,
    private orderService: OrderService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadStats();
    this.loadRecentOrders();
  }

  loadStats(): void {
    this.statsService.getAdminStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.loadingStats = false;
      },
      error: () => {
        this.error = 'Failed to load statistics';
        this.loadingStats = false;
      }
    });
  }

  loadRecentOrders(): void {
    this.orderService.getAdminOrders(1, 5).subscribe({
      next: (response) => {
        this.recentOrders = response.content;
        this.loadingOrders = false;
      },
      error: () => {
        this.loadingOrders = false;
      }
    });
  }

  goToProducts(): void {
    this.router.navigate(['/admin/products']);
  }

  goToOrders(): void {
    this.router.navigate(['/admin/orders']);
  }

  getStatusClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'completed': return 'status-completed';
      case 'cancelled': case 'canceled': return 'status-cancelled';
      case 'processing': return 'status-processing';
      default: return 'status-pending';
    }
  }
}
